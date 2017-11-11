package edu.luc.etl.connectorspace.meetup

import java.util.Calendar

object Main extends App {

  case class Config(
    isAuth: Boolean = false,
    isConsole: Boolean = false,
    isService: Boolean = false,
    dateFrom: Calendar = null,
    dateUntil: Calendar = null
  )

  val parser = new scopt.OptionParser[Config](AppName) {

    head(AppName, AppVersion)
    help("help") text "prints this usage text"
    version("version") text s"prints version information"

    opt[Unit]('a', "auth") action { (_, c) =>
      c.copy(isAuth = true)
    } text "(re)authorize via OAuth2"

    opt[Unit]('c', "console") action { (_, c) =>
      c.copy(isConsole = true)
    } text "run as a console application"

    opt[Unit]('s', "service") action { (_, c) =>
      c.copy(isService = true)
    } text "run as a web microservice"

    opt[Calendar]('f', "dateFrom") action { (x, c) =>
      c.copy(dateFrom = x)
    } text "date from which to consider events"

    opt[Calendar]('t', "dateTo") action { (x, c) =>
      c.copy(dateUntil = x)
    } text "date until which to consider events"

    checkConfig {
      case Config(a, c, s, f, u) if (Seq(a, c, s) count identity) > 1 =>
        failure("at most one mode flag is required")
      case _ => success
    }
  }

  parser.parse(args, Config()).foreach {
    case Config(true, false, false, null, null) => OAuth2.run()
    case Config(false, _, false, f, u)          => Cli.run(Option(f), Option(u))
    case Config(false, false, true, null, null) => WebService.run()
  }
}
