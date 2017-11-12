package edu.luc.etl.connectorspace.meetup

import java.util.Calendar

import akka.actor.ActorSystem
import com.github.nscala_time.time.Imports._
import com.typesafe.scalalogging.Logger
import org.joda.time.format.PeriodFormat
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.ExecutionContext.Implicits.global

object Cli extends MeetupAPIClient {

  val logger = Logger[Cli.type]

  override lazy val system = ActorSystem()

  override lazy val wsClient = AhcWSClient()

  def run(fromDate: Option[Calendar], toDate: Option[Calendar]): Unit = {

    val fromDateTime = fromDate map { cal => new DateTime(cal.getTime) } getOrElse DateTime.lastMonth
    val toDateTime = toDate map { cal => new DateTime(cal.getTime) } getOrElse DateTime.now
    val interval = fromDateTime to toDateTime

    timeAtEventsDuring(interval)(
      // Either.Right: everything OK
      effort => {
        val time = effort.duration.toStandardMinutes.toPeriod
        val timeString = PeriodFormat.getDefault.print(time)
        Console.println(s"spent a total of $timeString at events during $interval")
      },
      // Either.Left with status code OK
      response => Console.println(s"could not parse Meetup API server response as JSON: ${response.body}"),
      // Either.Left with other status code
      response => Console.println(s"received error from Meetup API server with status code ${response.status}"),
      // future timed out or failed in some other way
      ex => ex.printStackTrace()
    ) foreach { _ =>
        // unconditionally shut everything down
        wsClient.close()
        system.terminate()
      }
  }
}
