package edu.luc.etl.connectorspace.meetup

import java.util.Properties

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.nscala_time.time.Imports._
import com.typesafe.scalalogging.Logger
import play.api.http.Status
import play.api.libs.json._
import play.api.libs.ws.WSResponse
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source
import scala.util.Try

trait MeetupAPIClient {

  def logger: Logger

  implicit def system: ActorSystem

  implicit val mat = ActorMaterializer()

  def wsClient: AhcWSClient

  implicit val groupFormat = Json.format[Group]

  implicit val eventFormat = Json.format[Event]

  implicit val effortWrites = new Writes[Effort] {
    def writes(effort: Effort) = Json.obj(
      "from" -> effort.from.getMillis,
      "to" -> effort.to.getMillis,
      "effort" -> effort.duration.getMillis
    )
  }

  def timeAtEventsDuring(interval: Interval): Future[Either[WSResponse, Effort]] = {

    logger.debug("retrieving access token")

    val props = new Properties
    val reader = Source.fromFile(PropFileName).reader
    props.load(reader)

    val accessToken = props.getProperty(KeyAccessToken)
    val authHeader = "Authorization" -> s"Bearer $accessToken"
    val serviceUrl = "https://api.meetup.com/self/events?desc=true"

    logger.debug(s"submitting request to $serviceUrl")

    wsClient.url(serviceUrl).addHttpHeaders(authHeader).get() map { response =>
      response.status match {

        case Status.OK => Try {
          val responseLength = response.body.length
          logger.debug(s"response length = $responseLength")
          val json = Json.parse(response.body)

          // TODO figure out why we need to map explicitly
          // val events = Json.fromJson[IndexedSeq[Event]](json)
          val events = json.as[JsArray].value flatMap {
            _.validate[Event].asOpt
          }
          logger.debug(s"found ${events.length} events total")

          val eventsDuringInterval = events filter { event => interval.contains(event.time) }
          logger.debug(s"found ${eventsDuringInterval.length} events last during $interval")
          logger.debug(eventsDuringInterval.toString)

          val durationMillis = eventsDuringInterval.map {
            _.duration
          }.sum

          Right(Effort(interval.start, interval.end, Duration.millis(durationMillis)))
        } getOrElse Left(response)

        case _ => Left(response)
      }
    }
  }

  def handleClientResult[R](result: Future[Either[WSResponse, Effort]])(
    onSuccess: Effort => R,
    onParseError: WSResponse => R,
    onOtherError: WSResponse => R,
    onTimeout: Throwable => R
  ) = result map {
    _.fold(
      // Either.Left: something went wrong, probably when parsing the JSON
      response => response.status match {
        case Status.OK => onParseError(response)
        case _         => onOtherError(response)
      },
      // Either.Right: everything OK
      onSuccess
    )
  } recover {
    PartialFunction(onTimeout)
  }
}