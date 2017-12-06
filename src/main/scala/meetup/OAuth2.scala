package edu.luc.etl.connectorspace.meetup

import java.awt.Desktop
import java.io.{ File, FileNotFoundException, PrintWriter }
import java.net.URI
import java.util.Properties

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.Logger
import play.api.libs.json.Json
import play.api.libs.ws.DefaultBodyWritables._
import play.api.libs.ws.ahc.AhcWSClient
import play.api.mvc.Results
import play.api.routing.sird._
import play.core.server.{ AkkaHttpServer, ServerConfig }

import scala.collection.JavaConverters.propertiesAsScalaMapConverter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Future, Promise }
import scala.io.{ Source, StdIn }
import scala.util.Try

object OAuth2 {

  val logger = Logger[OAuth2.type]

  val props = new Properties

  private def checkExisting() = Try {
    logger.debug("looking for existing OAuth2 access token")
    val reader = Source.fromFile(PropFileName).reader
    props.load(reader)
    if (props.asScala.contains(KeyAccessToken)) {
      Console.print("found existing OAuth2 access token - force update? [yN]")
      if (StdIn.readLine().trim.toLowerCase != "y") {
        sys.exit(2)
      }
    }
  } recover {
    case ex: FileNotFoundException =>
      logger.debug(s"properties file $PropFileName not found, proceeding", ex)
  }

  def run(): Unit = {

    logger.debug("authenticating...")

    checkExisting()

    val clientId = sys.env(KeyClientId)
    val clientSecret = sys.env(KeyClientSecret)
    logger.debug(s"$KeyClientId = $clientId")
    logger.debug(s"$KeyClientSecret = $clientSecret")

    implicit val system = ActorSystem()
    implicit val mat = ActorMaterializer()
    val wsClient = AhcWSClient()

    val codePromise = Promise[String]()
    val config = ServerConfig(
      port = Some(RedirectServerPort),
      address = RedirectServerAddress
    )
    logger.debug(s"creating and starting embedded HTTP server instance ${config.address}")
    val httpServer = AkkaHttpServer.fromRouterWithComponents(config) { components =>
      {
        case GET(p"/" ? q"code=$code") => components.defaultActionBuilder {
          logger.debug(s"HTTP server got $code")
          codePromise.success(code)
          Results.Ok("authentication succeeded, please close this tab")
        }
      }
    }
    logger.debug(s"HTTP server now running at ${config.address}")

    // open the auth URI in the browser
    // this should result in a request to the embedded server
    val authArgs = Seq(
      "client_id" -> clientId,
      "response_type" -> "code",
      "redirect_uri" -> RedirectUrl
    )
    val authUrlWithArgs = wsClient.url(AuthUrl).addQueryStringParameters(authArgs: _*).uri
    if (Desktop.isDesktopSupported) {
      logger.debug(s"opening $authUrlWithArgs with default browser")
      Desktop.getDesktop.browse(authUrlWithArgs)
    } else {
      Console.println(s"to authorize this client, visit $authUrlWithArgs")
      Console.println("in your browser and, if asked, press Allow")
    }

    for { // entering Future monad
      code <- codePromise.future

      _ <- Future {
        logger.debug("waiting for pending request to complete before shutting down HTTP server")
        Thread.sleep(200)
        httpServer.stop()
        logger.debug("HTTP server shut down")
      }

      response <- {
        val tokenArgs = Map(
          "client_id" -> clientId,
          "client_secret" -> clientSecret,
          "grant_type" -> "authorization_code",
          "redirect_uri" -> RedirectUrl,
          "code" -> code
        )
        logger.debug(tokenArgs.toString)
        wsClient.url(TokenUrl).post(tokenArgs)
      }
    } {
      Try {
        val json = Json.parse(response.body)
        logger.debug(Json.prettyPrint(json))
        val accessToken = json("access_token").as[String]
        val refreshToken = json("refresh_token").as[String]

        logger.debug(s"storing access and refresh tokens = $accessToken $refreshToken")
        props.setProperty(KeyAccessToken, accessToken)
        props.setProperty(KeyRefreshToken, refreshToken)
        val pw = new PrintWriter(new File(PropFileName))
        props.store(pw, "updated OAuth2 access and refresh tokens")
        Console.println("updated OAuth2 access and refresh tokens")
      } recover {
        case ex => logger.debug("error parsing or storing access and refresh tokens", ex)
      }

      wsClient.close()
      system.terminate()
    }
  }
}
