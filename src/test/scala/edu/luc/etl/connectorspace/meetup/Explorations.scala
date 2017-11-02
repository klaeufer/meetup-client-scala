package edu.luc.etl.connectorspace.meetup

import java.util.Properties

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.libs.json._
import play.api.libs.ws.ahc._

import scala.concurrent.Await
import scala.io.Source

import com.github.nscala_time.time.Imports._

case class Event(
  time: Long,
  duration: Long,
  name: String,
  group: Group
)

case class Group(
  name: String,
  id: Int,
  urlname: String
)

object Explorations extends App {

  println("hello")

  val PROP_FILE_NAME = "local.properties"

  val props = new Properties
  val reader = Source.fromFile(PROP_FILE_NAME).reader
  props.load(reader)
  val apiKey = props.getProperty("apiKey")
  val serviceUrl = s"https://api.meetup.com/self/events?key=${apiKey}&desc=true"

  println(s"api key = ${apiKey}")

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val groupFormat = Json.format[Group]
  implicit val eventFormat = Json.format[Event]

  val wsClient = StandaloneAhcWSClient()
  val result = wsClient.url(serviceUrl).get().map { response =>
    val responseLength = response.body.length
    println(s"response length = ${responseLength}")
    val json = Json.parse(response.body)
//    println(Json.prettyPrint(json))

    // TODO figure out why we need to map explicitly
    // val events = Json.fromJson[IndexedSeq[Event]](json)
    val events = json.as[JsArray].value.map { _.validate[Event].asOpt }.flatten
    println(s"found ${events.length} events total")

    val lastYear = DateTime.lastYear to DateTime.now
    val eventsLastYear = events.filter { event => lastYear.contains(event.time) }
    println(s"found ${eventsLastYear.length} events last year")
  }

//  import scala.concurrent.duration._
//  Await.ready(result, 10.second)
//  sys.exit()
}
