package edu.luc.etl.connectorspace.meetup

import akka.actor.ActorSystem
import com.github.nscala_time.time.Imports._
import com.typesafe.scalalogging.Logger
import org.joda.time.DateTime.{ parse => parseDateTime }
import org.specs2.mutable._
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

class MeetupIntegrationTest extends Specification with MeetupAPIClient {

  override val logger = Logger[WebService.type]

  override def system = ActorSystem()

  override def wsClient = AhcWSClient()

  val apiKey = sys.env(KeyApiKey)

  "The timeAtEventsDuring method" should {
    "return 540 minutes of effort in 2015 and 2016" in {
      val fromTime = parseDateTime("2015-01-01")
      val toTime = parseDateTime("2017-01-01")
      val interval = fromTime to toTime
      val result = Effort(fromTime, toTime, 540.minutes)
      val future = timeAtEventsDuring(interval)(_.addQueryStringParameters("key" -> apiKey))(
        onSuccess = Some.apply,
        onParseError = r => { println(s"parse error: $r"); None },
        onOtherError = r => { println(s"other error: $r"); None },
        onTimeout = ex => { println(s"timeout: $ex"); None }
      )
      Await.result(future, 5.seconds.toDuration.toScalaDuration) must be equalTo(Some(result))
    }
  }
}
