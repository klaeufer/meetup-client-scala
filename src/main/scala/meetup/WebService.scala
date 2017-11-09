package edu.luc.etl.connectorspace.meetup

import akka.actor.ActorSystem
import com.typesafe.scalalogging.Logger
import play.api.libs.ws.ahc.AhcWSClient
import play.api.mvc.Results
import play.api.libs.json._
import play.api.routing.sird._
import play.core.server.{ AkkaHttpServer, ServerConfig }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Properties

object WebService extends MeetupAPIClient {

  override def system = ActorSystem()

  override def wsClient = AhcWSClient()

  case class Effort()

  def run(): Unit = {

    val logger = Logger[WebService.type]

    val config = ServerConfig(
      port = Some(WebServerPort),
      address = "0.0.0.0"
    )
    logger.debug(s"creating and starting embedded HTTP server instance ${config.address}")
    AkkaHttpServer.fromRouterWithComponents(config) { components =>
      {
        case GET(p"/effort" ? q"from=$from" & q_?"to=$to") => components.defaultActionBuilder.async {
          logger.debug(s"retrieving events from $from to $to")
          timeAtEventsLastYear().map { effort =>
            Results.Ok(Json.toJson(effort))
          } recover {
            case ex => Results.InternalServerError(ex.getStackTrace.mkString(Properties.lineSeparator))
          }
        }
      }
    }
    logger.debug(s"HTTP server now running at ${config.address}")
  }
}
