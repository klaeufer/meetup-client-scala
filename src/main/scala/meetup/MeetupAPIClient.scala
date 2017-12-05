package edu.luc.etl.connectorspace.meetup

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.nscala_time.time.Imports._
import com.typesafe.scalalogging.Logger
import play.api.http.Status
import play.api.libs.json._
import play.api.libs.ws.{ WSRequest, WSResponse }
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
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

  def apiCaller[R](authorizeRequest: WSRequest => WSRequest)(
    onSuccess: WSResponse => R,
    onParseError: WSResponse => R,
    onOtherError: WSResponse => R,
    onTimeout: Throwable => R
  ): Future[R] = {
    val request = authorizeRequest(wsClient.url(ServiceUrl))
    logger.debug(s"submitting request to ${request.url}")
    request.get() map { response =>
      response.status match {
        case Status.OK => Try { onSuccess(response) } getOrElse onParseError(response)
        case _         => onOtherError(response)
      }
    } recover {
      PartialFunction(onTimeout)
    }
  }

  def timeAtEventsDuring[R](interval: Interval)(authorizeRequest: WSRequest => WSRequest)(
    onSuccess: Effort => R,
    onParseError: WSResponse => R,
    onOtherError: WSResponse => R,
    onTimeout: Throwable => R
  ): Future[R] =
    apiCaller(authorizeRequest)(response => {
      val responseLength = response.body.length
      logger.debug(s"response length = $responseLength")
      val json = Json.parse(response.body)

      // TODO figure out why we need to map explicitly
      // val events = Json.fromJson[IndexedSeq[Event]](json)
      val events = json.as[JsArray].value flatMap { _.validate[Event].asOpt }
      logger.debug(s"found ${events.length} events total")

      val eventsDuringInterval = events filter { event => interval.contains(event.time) }
      logger.debug(s"found ${eventsDuringInterval.length} events during $interval")
      logger.debug(eventsDuringInterval.toString)

      val durationMillis = eventsDuringInterval.map { _.duration }.sum
      onSuccess(Effort(interval.start, interval.end, Duration.millis(durationMillis)))
    }, onParseError, onOtherError, onTimeout)

}