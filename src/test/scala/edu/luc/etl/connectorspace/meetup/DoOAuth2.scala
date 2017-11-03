package edu.luc.etl.connectorspace.meetup

import java.io.{File, PrintWriter}
import java.net.URLDecoder
import java.util.Properties

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.Logger
import play.api.libs.json._
import play.api.libs.ws.ahc._

import scala.io.{Source, StdIn}

object DoOAuth2 extends App {

  val logger = Logger[TimeAtEvents.type]

  logger.debug("authenticating...")

  val PROP_FILE_NAME = "local.properties"

  val props = new Properties
  val reader = Source.fromFile(PROP_FILE_NAME).reader
  props.load(reader)
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

  implicit val groupFormat = Json.format[Group]
  implicit val eventFormat = Json.format[Event]

  // TODO figure out how to write these successive requests sequentially (monadically)
  val wsClient = StandaloneAhcWSClient()

  wsClient.url(authUrl).withFollowRedirects(false).post(authArgs).map { response =>
    val locationHeader = response.headers("Location")(0)
    val locationQSMap = locationHeader.split("&").map { kv => val arr = kv.split("=", 2) ; arr(0) -> arr(1) }.toMap
    val returnUri = URLDecoder.decode(locationQSMap("returnUri"))

    println(s"to authorize this client, visit ${returnUri})")
    println("in your browser and, if asked, press Allow")
    print("then copy and paste the string after http://localhost:8080/?code= here> ")
    val code = StdIn.readLine().trim()

    logger.debug(s"using code ${code}")

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
      println(Json.prettyPrint(json))
      val accessToken = json("access_token").as[String]
      val refreshToken = json("refresh_token").as[String]
      logger.debug(s"storing access and refresh tokens = ${accessToken} ${refreshToken}")
      props.setProperty("accessToken", accessToken)
      props.setProperty("refreshToken", refreshToken)
      val pw = new PrintWriter(new File(PROP_FILE_NAME))
      props.store(pw, "updated OAuth2 access code")

      sys.exit()
    }
  }
}
