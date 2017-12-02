package edu.luc.etl.connectorspace.meetup

import akka.actor.ActorSystem
import com.github.nscala_time.time.Imports._
import com.typesafe.scalalogging.Logger
import org.joda.time.DateTime.{ parse => parseDateTime }
import org.specs2.mutable._
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.AhcWSClient
import org.specs2.concurrent.ExecutionEnv

import scala.concurrent.Await

class MeetupSpec extends Specification with MeetupAPIClient {

  override val logger = Logger[WebService.type]

  override def system = ActorSystem()

  override def wsClient = AhcWSClient()

  val apiKey = sys.env(KeyApiKey)
  require { apiKey != null }

  "The timeAtEventsDuring method" should {
    "return 765 minutes" in {

      val fromTime = parseDateTime("2015-01-01")
      val toTime = parseDateTime("2017-12-01")
      val result = Effort(fromTime, toTime, 765.minutes)
      val interval = fromTime to toTime
      def requestCreator(wsClient: WSClient) = wsClient.url(ServiceUrl).addQueryStringParameters("key" -> apiKey)
      val future = timeAtEventsDuring(interval)(requestCreator)(
        onSuccess = identity,
        onParseError = r => { println(s"parse error: $r"); null },
        onOtherError = r => { println(s"other error: $r"); null },
        onTimeout = ex => { println(s"timeout: $ex"); null }
      )
      import scala.concurrent.duration.Duration
      Await.result(future, Duration.Inf) must beEqualTo(result)
    }
  }
}
