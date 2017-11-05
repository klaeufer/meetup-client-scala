package edu.luc.etl.connectorspace.meetup

import java.awt.Desktop
import java.io.{File, PrintWriter}
import java.net.{URI, URLDecoder}
import java.util.Properties

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.Logger
import play.api.libs.json._
import play.api.libs.ws.ahc._

import scala.concurrent.{Await, Promise}
import scala.io.{Source, StdIn}
import scala.util.Try

object DoOAuth2 extends App {

  val logger = Logger[TimeAtEvents.type]

  logger.debug("authenticating...")

  val PROP_FILE_NAME = "local.properties"

  val props = new Properties
  val reader = Source.fromFile(PROP_FILE_NAME).reader
  props.load(reader)

  if (props.getProperty("accessToken") != null) {
    Console.print("found existing OAuth2 access token - force update? [yN]")
    if (StdIn.readLine().trim.toLowerCase != "y") {
      sys.exit(2)
    }
  }

  val clientId = props.getProperty("clientId")
  val clientSecret = props.getProperty("clientSecret")

  logger.debug(s"clientId = ${clientId}")
  logger.debug(s"clientSecret = ${clientSecret}")

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  import scala.concurrent.ExecutionContext.Implicits.global
  import play.api.libs.ws.DefaultBodyWritables._

  val authUrl = "https://secure.meetup.com/oauth2/authorize"
  val authArgs = Map("client_id" -> clientId, "response_type" -> "code", "redirect_uri" -> "http://localhost:8080")
  val tokenUrl = "https://secure.meetup.com/oauth2/access"
  val serviceUrl = "https://api.meetup.com/self/events"

  // TODO figure out how to write these successive requests sequentially (monadically)
  val wsClient = StandaloneAhcWSClient()

  wsClient.url(authUrl).withFollowRedirects(false).post(authArgs).map { response =>

    val codePromise = Promise[String]()
    val codeFuture = codePromise.future

    logger.debug("creating NanoHTTPD instance")
    import fi.iki.elonen.NanoHTTPD
    object httpServer extends NanoHTTPD(8080) {
      override def serve(session: NanoHTTPD.IHTTPSession) = {
        logger.debug("NanoHTTPD got " + session.getParameters)
        Try {
          val code = session.getParameters.get("code").get(0)
          codePromise.success(code)
          NanoHTTPD.newFixedLengthResponse("authentication succeeded, please close this tab")
        } getOrElse {
          NanoHTTPD.newFixedLengthResponse("failed to get code")
        }
      }
    }
    logger.debug("starting NanoHTTPD")
    httpServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
    logger.debug("NanoHTTPD running at http://localhost:8080/")

    val locationHeader = response.headers("Location")(0)
    val locationQSMap = locationHeader.split("&").map { kv => val arr = kv.split("=", 2) ; arr(0) -> arr(1) }.toMap
    val returnUri = URLDecoder.decode(locationQSMap("returnUri"))

    if (Desktop.isDesktopSupported) {
      logger.debug(s"opening ${returnUri} with default system handler")
      Desktop.getDesktop.browse(new URI(returnUri))
    } else {
      Console.println(s"to authorize this client, visit ${returnUri}")
      Console.println("in your browser and, if asked, press Allow")
    }

    codeFuture.foreach { code =>

      // wait for embedded server to shut down
      Thread.sleep(200)

      httpServer.stop()

      val tokenArgs = Map(
        "client_id" -> clientId,
        "client_secret" -> clientSecret,
        "grant_type" -> "authorization_code",
        "redirect_uri" -> "http://localhost:8080",
        "code" -> code
      )
      logger.debug(tokenArgs.toString)

      wsClient.url(tokenUrl).post(tokenArgs).map { response =>
        val json = Json.parse(response.body)
        logger.debug(Json.prettyPrint(json))
        val accessToken = json("access_token").as[String]
        val refreshToken = json("refresh_token").as[String]
        logger.debug(s"storing access and refresh tokens = ${accessToken} ${refreshToken}")
        props.setProperty("accessToken", accessToken)
        props.setProperty("refreshToken", refreshToken)
        val pw = new PrintWriter(new File(PROP_FILE_NAME))
        props.store(pw, "updated OAuth2 access and refresn tokens")
        Console.println("updated OAuth2 access and refresh tokens")

        sys.exit()
      }
    }
  }
}
