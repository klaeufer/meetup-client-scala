package edu.luc.etl.connectorspace.meetup

import akka.actor.ActorSystem
import com.typesafe.scalalogging.Logger
import com.github.nscala_time.time.Imports._
import org.joda.time.DateTime.{ parse => parseDateTime }
import play.api.libs.ws.ahc.AhcWSClient
import play.api.mvc.Results
import play.api.libs.json._
import play.api.libs.ws.WSRequest
import play.api.routing.sird._
import play.core.server.{ AkkaHttpServer, ServerConfig }

import scala.util.Properties

object WebService extends MeetupAPIClient {

  override val logger = Logger[WebService.type]

  override def system = ActorSystem()
  override def wsClient = AhcWSClient()

  def run(): Unit = {

    val port = Properties.envOrElse("PORT", DefaultWebServerPort).toInt
    val config = ServerConfig(
      port = Some(port),
      address = WebServerAddress
    )
    logger.debug(s"creating and starting embedded HTTP server instance ${config.address}")
    AkkaHttpServer.fromRouterWithComponents(config) { components =>
      {
        case GET(p"/") =>
          components.defaultActionBuilder {
            Results.Ok("nothing here, try /effort")
          }

        case GET(p"/effort" ? q_?"from=$fromString" & q_?"to=$untilString" & q_?"key=$optApiKey") =>
          components.defaultActionBuilder.async { request =>

            // TODO this looks like a reusable, request-independent key or token authorization technique
            val optAuthValue = request.headers.get(KeyAuthorization)
            val authorizeRequest: WSRequest => WSRequest = (optApiKey, optAuthValue) match {
              case (_, Some(authValue)) =>
                val authHeader = KeyAuthorization -> authValue
                logger.debug(s"using header $authHeader")
                _.addHttpHeaders(authHeader)
              case (Some(apiKey), _) =>
                logger.debug(s"using api key")
                _.addQueryStringParameters("key" -> apiKey)
              case _ => identity
            }

            logger.debug(s"retrieving events from $fromString to $toString")
            val fromDateTime = fromString map parseDateTime getOrElse DateTime.lastMonth
            val toDateTime = untilString map parseDateTime getOrElse DateTime.now
            val interval = fromDateTime to toDateTime

            timeAtEventsDuring(interval)(authorizeRequest)(
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
