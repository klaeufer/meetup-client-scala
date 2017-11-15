package edu.luc.etl.connectorspace.meetup

import java.util.Calendar

object Main extends App {

  case class Config(
    isAuth: Boolean = false,
    isConsole: Boolean = false,
    isService: Boolean = false,
    dateFrom: Option[Calendar] = None,
    dateUntil: Option[Calendar] = None
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
      c.copy(dateFrom = Some(x))
    } text "date from which to consider events"

    opt[Calendar]('t', "dateTo") action { (x, c) =>
      c.copy(dateUntil = Some(x))
    } text "date until which to consider events"

    checkConfig {
      case Config(a, c, s, f, u) if (Seq(a, c, s) count identity) > 1 =>
        failure("at most one mode flag is required")
      case _ => success
    }
  }

  parser.parse(args, Config()) foreach {
    case Config(true, false, false, None, None) => OAuth2.run()
    case Config(false, true, false, f, u)       => Cli.run(f, u)
    case Config(false, false, _, None, None)    => WebService.run()
  }
}
