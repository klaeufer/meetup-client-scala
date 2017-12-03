package edu.luc.etl.connectorspace.meetup

import java.util.{ Calendar, Properties }

import akka.actor.ActorSystem
import com.github.nscala_time.time.Imports._
import com.typesafe.scalalogging.Logger
import org.joda.time.format.PeriodFormat
import play.api.libs.ws.WSRequest
import play.api.libs.ws.ahc.AhcWSClient

import scala.collection.JavaConverters.propertiesAsScalaMapConverter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source
import scala.util.Try

object Cli extends MeetupAPIClient {

  val logger = Logger[Cli.type]

  override lazy val system = ActorSystem()

  override lazy val wsClient = AhcWSClient()

  def run(fromDate: Option[Calendar], toDate: Option[Calendar]): Unit = {

    // TODO this looks like a reusable technique for successive attempts to do something and exit if none succeeds
    val authorizeRequest = Try {
      logger.debug(s"looking for $KeyAccessToken in $PropFileName")
      val props = new Properties
      val reader = Source.fromFile(PropFileName).reader
      props.load(reader)
      val accessToken = props.asScala(KeyAccessToken)
      logger.debug("found access token")
      val authHeader = KeyAuthorization -> s"Bearer $accessToken"
      (request: WSRequest) => request.addHttpHeaders(authHeader)
    } orElse Try {
      logger.debug(s"looking for $KeyApiKey in environment")
      val apiKey = sys.env(KeyApiKey)
      logger.debug("found API key")
      (request: WSRequest) => request.addQueryStringParameters("key" -> apiKey)
    } getOrElse {
      logger.debug("no authorization information found, exiting")
      sys.exit(401)
    }

    val fromDateTime = fromDate map { cal => new DateTime(cal.getTime) } getOrElse DateTime.lastMonth
    val toDateTime = toDate map { cal => new DateTime(cal.getTime) } getOrElse DateTime.now
    val interval = fromDateTime to toDateTime

    timeAtEventsDuring(interval)(authorizeRequest)(
      onSuccess = effort => {
        val time = effort.duration.toStandardMinutes.toPeriod
        val timeString = PeriodFormat.getDefault.print(time)
        Console.println(s"spent a total of $timeString at events during $interval")
      },
      onParseError = response =>
        Console.println(s"could not parse Meetup API server response as JSON: ${response.body}"),
      onOtherError = response =>
        Console.println(s"received error from Meetup API server with status code ${response.status} and body ${response.body}"),
      onTimeout = ex =>
        ex.printStackTrace()
    ) foreach { _ =>
        // unconditionally shut everything down
        wsClient.close()
        system.terminate()
      }
  }
}
