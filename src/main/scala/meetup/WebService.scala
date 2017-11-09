package edu.luc.etl.connectorspace.meetup

import java.awt.Desktop
import java.io.{File, PrintWriter}
import java.net.{URI, URLDecoder}
import java.nio.charset.StandardCharsets
import java.util.Properties

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.Logger
import play.api.libs.json.Json
import play.api.libs.ws.ahc.AhcWSClient
import play.api.mvc.Results
import play.api.routing.sird._
import play.core.server.{AkkaHttpServer, ServerConfig}

import scala.concurrent.Promise
import scala.io.{Source, StdIn}

object WebService {

  def run(): Unit = {

    val logger = Logger[CliClient.type]

    val config = ServerConfig(
      port = Some(ServerPort),
      address = "0.0.0.0"
    )
    logger.debug(s"creating and starting embedded HTTP server instance ${config.address}")
    val httpServer = AkkaHttpServer.fromRouterWithComponents(config) { components => {
      case GET(p"/effort/" ? q"from=$from" & q_?"to=$to") => components.defaultActionBuilder {
        logger.debug(s"retrieving events from $from to $to")
        Results.Ok(s"retrieving events from $from to $to")
      }
    }
    }
    logger.debug(s"HTTP server now running at ${config.address}")
  }
}
