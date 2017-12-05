package edu.luc.etl.connectorspace.meetup

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.nscala_time.time.Imports._
import org.joda.time.DateTime.{parse => parseDateTime}
import org.specs2.mutable._
import play.api.http.Status
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

class MeetupEndToEndTest extends Specification {

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  val wsClient = AhcWSClient()
  val apiKey = sys.env(KeyApiKey)

  implicit val effortReads: Reads[Effort] = (
    (JsPath \ "from").read[Long] and
    (JsPath \ "to").read[Long] and
    (JsPath \ "effort").read[Long]
  )((f, t, d) => Effort(new DateTime(f), new DateTime(t), new Duration(d)))

  "The Meetup connector service running locally" should {
    "return 540 minutes of effort in 2015 and 2016" in {
      val fromString = "2015-01-01"
      val untilString = "2017-01-01"
      val fromTime = parseDateTime(fromString)
      val untilTime = parseDateTime(untilString)
      val expected = Effort(fromTime, untilTime, 540.minutes)
      val request = wsClient.url("http://localhost:5000/effort").addQueryStringParameters(
        "key" -> apiKey,
        "from" -> fromString,
        "to" -> untilString
      )
      val result = for {
        response <- request.get()
        if response.status == Status.OK
        json = Json.parse(response.body)
        effort = json.as[JsObject].validate[Effort]
      } yield effort
      Await.result(result, 10.seconds.toDuration.toScalaDuration) must be equalTo JsSuccess(expected)
    }
  }
}
