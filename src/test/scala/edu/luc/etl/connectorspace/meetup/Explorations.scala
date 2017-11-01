package edu.luc.etl.connectorspace.meetup

import java.util.Properties

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.libs.ws.ahc._

import scala.concurrent.Await
import scala.io.Source

object Explorations extends App {

  println("hello")

  val PROP_FILE_NAME = "local.properties"

  val props = new Properties
  val reader = Source.fromFile(PROP_FILE_NAME).reader()
  props.load(reader)
  val apiKey = props.getProperty("apiKey")
  val serviceUrl = s"https://api.meetup.com/self/events?key=${apiKey}"

  println(s"api key = ${apiKey}")

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  import scala.concurrent.ExecutionContext.Implicits.global

  val wsClient = StandaloneAhcWSClient()
  val result = wsClient.url(serviceUrl).get().map { response => response.body.length }
  // TODO process response as JSON - https://www.playframework.com/documentation/2.6.x/ScalaWS

  result.foreach { responseLength =>
    println(s"response length = ${responseLength}")
    sys.exit()
  }
}
