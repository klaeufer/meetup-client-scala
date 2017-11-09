package edu.luc.etl.connectorspace.meetup

import akka.actor.ActorSystem
import org.joda.time.format.PeriodFormat
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.ExecutionContext.Implicits.global

object Cli extends MeetupAPIClient {

  override lazy val system = ActorSystem()

  override lazy val wsClient = AhcWSClient()

  def run(): Unit = {
    timeAtEventsLastYear().foreach { effort =>
      val time = effort.effort.toStandardMinutes.toPeriod
      val timeString = PeriodFormat.getDefault.print(time)
      Console.println(s"spent a total of $timeString at events last year")
      wsClient.close()
      system.terminate()
    }
  }
}
