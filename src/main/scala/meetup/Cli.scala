package edu.luc.etl.connectorspace.meetup

import java.util.{ Calendar, Properties }

import akka.actor.ActorSystem
import com.github.nscala_time.time.Imports._
import com.typesafe.scalalogging.Logger
import org.joda.time.format.PeriodFormat
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source
import scala.util.Try

object Cli extends MeetupAPIClient {

  val logger = Logger[Cli.type]

  override lazy val system = ActorSystem()

  override lazy val wsClient = AhcWSClient()

  def run(fromDate: Option[Calendar], toDate: Option[Calendar]): Unit = {

    val fromDateTime = fromDate map { cal => new DateTime(cal.getTime) } getOrElse DateTime.lastMonth
    val toDateTime = toDate map { cal => new DateTime(cal.getTime) } getOrElse DateTime.now
    val interval = fromDateTime to toDateTime

    logger.debug("retrieving access token")
    val props = new Properties
    val reader = Source.fromFile(PropFileName).reader
    props.load(reader)
    val accessToken = props.getProperty(KeyAccessToken)
    require { accessToken != null }
    val authHeader = KeyAuthorization -> s"Bearer $accessToken"

    timeAtEventsDuring(interval)(authHeader)(
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
