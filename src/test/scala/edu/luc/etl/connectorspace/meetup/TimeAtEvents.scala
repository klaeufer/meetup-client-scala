package edu.luc.etl.connectorspace.meetup

import java.util.Properties

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.libs.json._
import play.api.libs.ws.ahc._

import scala.concurrent.Await
import scala.io.Source
import com.github.nscala_time.time.Imports._
import com.typesafe.scalalogging.Logger

case class Event(
  time: Long,
  duration: Long,
  name: String,
  group: Group
)

case class Group(
  name: String,
  id: Int,
  urlname: String
)

object TimeAtEvents extends App {

  val logger = Logger[TimeAtEvents.type]

  logger.debug("retrieving access token")

  val PROP_FILE_NAME = "local.properties"

  val props = new Properties
  val reader = Source.fromFile(PROP_FILE_NAME).reader
  props.load(reader)

  val accessToken = props.getProperty("accessToken")
  val authHeader = "Authorization" -> s"Bearer ${accessToken}"
  val serviceUrl = "https://api.meetup.com/self/events?desc=true"

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val groupFormat = Json.format[Group]
  implicit val eventFormat = Json.format[Event]

  logger.debug(s"submitting request to ${serviceUrl}")

  val wsClient = StandaloneAhcWSClient()
  val result = wsClient.url(serviceUrl).addHttpHeaders(authHeader).get().map { response =>
    val responseLength = response.body.length
    logger.debug(s"response length = ${responseLength}")
    val json = Json.parse(response.body)

    // TODO figure out why we need to map explicitly
    // val events = Json.fromJson[IndexedSeq[Event]](json)
    val events = json.as[JsArray].value.map { _.validate[Event].asOpt }.flatten
    logger.debug(s"found ${events.length} events total")

    val lastYear = DateTime.lastYear to DateTime.now
    val eventsLastYear = events.filter { event => lastYear.contains(event.time) }
    println(s"found ${eventsLastYear.length} events last year")
    logger.debug(eventsLastYear.toString)

    // TODO use nscala/joda for this calculation
    val timeAtEventsLastYear = eventsLastYear.map { _.duration / 1000 }.sum.toFloat / 3600
    println(s"spent a total of ${timeAtEventsLastYear} hours at events last year")

    sys.exit()
  }

//  import scala.concurrent.duration._
//  Await.ready(result, 10.second)
//  sys.exit()
}
