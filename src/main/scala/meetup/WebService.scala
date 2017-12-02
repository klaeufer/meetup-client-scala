package edu.luc.etl.connectorspace.meetup

import akka.actor.ActorSystem
import com.typesafe.scalalogging.Logger
import com.github.nscala_time.time.Imports._
import org.joda.time.DateTime.{ parse => parseDateTime }
import play.api.libs.ws.ahc.AhcWSClient
import play.api.mvc.Results
import play.api.libs.json._
import play.api.routing.sird._
import play.core.server.{ AkkaHttpServer, ServerConfig }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Properties

object WebService extends MeetupAPIClient {

  override val logger = Logger[WebService.type]

  override def system = ActorSystem()

  override def wsClient = AhcWSClient()

  def run(): Unit = {

    val config = ServerConfig(
      port = Some(WebServerPort),
      address = WebServerAddress
    )
    logger.debug(s"creating and starting embedded HTTP server instance ${config.address}")
    AkkaHttpServer.fromRouterWithComponents(config) { components =>
      {
        case GET(p"/effort" ? q_?"from=$fromString" & q_?"to=$untilString") => components.defaultActionBuilder.async { request =>

          val authHeader = KeyAuthorization -> request.headers.get(KeyAuthorization).get
          logger.debug(s"using header $authHeader")
          logger.debug(s"retrieving events from $fromString to $toString")
          val fromDateTime = fromString map parseDateTime getOrElse DateTime.lastMonth
          val toDateTime = untilString map parseDateTime getOrElse DateTime.now
          val interval = fromDateTime to toDateTime

          timeAtEventsDuring(interval)(_.url(ServiceUrl).addHttpHeaders(authHeader))(
            onSuccess = effort =>
              Results.Ok(Json.toJson(effort)),
            onParseError = response =>
              Results.BadGateway(s"could not parse Meetup API server response as JSON: ${response.body}"),
            onOtherError = response =>
              Results.BadGateway(s"received error from Meetup API server with status code ${response.status}"),
            onTimeout = ex =>
              Results.InternalServerError(ex.getStackTrace mkString Properties.lineSeparator)
          )
        }
      }
    }
    logger.debug(s"HTTP server now running at ${config.address}")
  }
}
