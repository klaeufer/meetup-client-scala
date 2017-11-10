package edu.luc.etl.connectorspace.meetup

import akka.actor.ActorSystem
import com.typesafe.scalalogging.Logger
import org.joda.time.format.PeriodFormat
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.ExecutionContext.Implicits.global

object Cli extends MeetupAPIClient {

  val logger = Logger[Cli.type]

  override lazy val system = ActorSystem()

  override lazy val wsClient = AhcWSClient()

  def run(): Unit = {
    timeAtEventsLastYear().map { effort =>
      val time = effort.effort.toStandardMinutes.toPeriod
      val timeString = PeriodFormat.getDefault.print(time)
      Console.println(s"spent a total of $timeString at events last year")
    } recover {
      case ex => ex.printStackTrace()
    } foreach { _ =>
      wsClient.close()
      system.terminate()
    }
  }
}
