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

    timeAtEventsDuring(interval) map { effort =>
      val time = effort.effort.toStandardMinutes.toPeriod
      val timeString = PeriodFormat.getDefault.print(time)
      Console.println(s"spent a total of $timeString at events during $interval")
    } recover {
      case ex => ex.printStackTrace()
    } foreach { _ =>
      wsClient.close()
      system.terminate()
    }
  }
}
