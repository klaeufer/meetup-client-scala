package edu.luc.etl.connectorspace.meetup

import akka.actor.ActorSystem
import com.github.nscala_time.time.Imports._
import com.typesafe.scalalogging.Logger
import org.joda.time.DateTime.{ parse => parseDateTime }
import org.specs2.mutable._
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

class MeetupTest extends Specification with MeetupAPIClient {

  override val logger = Logger[WebService.type]

  override def system = ActorSystem()

  override def wsClient = AhcWSClient()

  val apiKey = Option(sys.env(KeyApiKey)).get

  "This unit test" should {
    "run" in {
      true
    }
  }
}
