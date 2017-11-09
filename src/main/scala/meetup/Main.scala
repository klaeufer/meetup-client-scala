package edu.luc.etl.connectorspace.meetup

object Main extends App {

  case class Config(
    isAuth: Boolean = false,
    isConsole: Boolean = false,
    isService: Boolean = false
  )

  val parser = new scopt.OptionParser[Config](AppName) {

    head(AppName, AppVersion)
    help("help").text("prints this usage text")
    version("version").text(s"prints version information")

    opt[Unit]('a', "auth").action { (_, c) =>
      c.copy(isAuth = true)
    }.text("(re)authorize via OAuth2")

    opt[Unit]('c', "console").action { (_, c) =>
      c.copy(isConsole = true)
    }.text("run as a console application")

    opt[Unit]('s', "service").action { (_, c) =>
      c.copy(isService = true)
    }.text("run as a web microservice")

    checkConfig {
      case Config(a, c, s) if Seq(a, c, s).count(identity) > 1 =>
        failure("at most one mode flag is required")
      case _ => success
    }
  }

  parser.parse(args, Config()).foreach {
    case Config(true, false, false) => OAuth2.run()
    case Config(false, _, false)    => Cli.run()
    case Config(false, false, true) => WebService.run()
  }
}
