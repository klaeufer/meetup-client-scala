package edu.luc.etl.connectorspace.meetup

import java.util.Properties

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.nscala_time.time.Imports._
import com.typesafe.scalalogging.Logger
import play.api.libs.json._
import play.api.libs.ws.ahc.AhcWSClient

import scala.io.Source

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

object TimeAtEvents {

  def run(): Unit = {

    val logger = Logger[TimeAtEvents.type]

    logger.debug("retrieving access token")

    val props = new Properties
    val reader = Source.fromFile(PropFileName).reader
    props.load(reader)

    val accessToken = props.getProperty(KeyAccessToken)
    val authHeader = "Authorization" -> s"Bearer ${accessToken}"
    val serviceUrl = "https://api.meetup.com/self/events?desc=true"

    implicit val system = ActorSystem()
    implicit val mat = ActorMaterializer()
    import scala.concurrent.ExecutionContext.Implicits.global

    implicit val groupFormat = Json.format[Group]
    implicit val eventFormat = Json.format[Event]

    logger.debug(s"submitting request to ${serviceUrl}")

    val wsClient = AhcWSClient()
    val result = wsClient.url(serviceUrl).addHttpHeaders(authHeader).get().map { response =>
      val responseLength = response.body.length
      logger.debug(s"response length = ${responseLength}")
      val json = Json.parse(response.body)

      // TODO figure out why we need to map explicitly
      // val events = Json.fromJson[IndexedSeq[Event]](json)
      val events = json.as[JsArray].value.map {
        _.validate[Event].asOpt
      }.flatten
      logger.debug(s"found ${events.length} events total")

      val lastYear = DateTime.lastYear to DateTime.now
      val eventsLastYear = events.filter { event => lastYear.contains(event.time) }
      Console.println(s"found ${eventsLastYear.length} events last year")
      logger.debug(eventsLastYear.toString)

      // TODO use nscala/joda for this calculation
      val timeAtEventsLastYear = eventsLastYear.map {
        _.duration / 1000
      }.sum.toFloat / 3600
      Console.println(s"spent a total of ${timeAtEventsLastYear} hours at events last year")

      wsClient.close()
      system.terminate()
    }
  }
}
