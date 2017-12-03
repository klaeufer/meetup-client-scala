package edu.luc.etl.connectorspace

import com.github.nscala_time.time.Imports.{ DateTime, Duration }

package object meetup {

  val AppName = "meetupConnector"
  val AppVersion = "0.1"

  val PropFileName = "local.properties"
  val KeyClientId = "MEETUP_CLIENT_ID"
  val KeyClientSecret = "MEETUP_CLIENT_SECRET"
  val KeyApiKey = "MEETUP_API_KEY"
  val KeyAccessToken = "accessToken"
  val KeyRefreshToken = "refreshToken"
  val KeyAuthorization = "Authorization"

  val AuthUrl = "https://secure.meetup.com/oauth2/authorize"
  val TokenUrl = "https://secure.meetup.com/oauth2/access"
  val ServiceUrl = "https://api.meetup.com/self/events?desc=true"
  val RedirectServerPort = 8080
  val RedirectServerAddress = "0.0.0.0"
  val RedirectUrl = s"http://localhost:$RedirectServerPort"
  val DefaultWebServerPort = "8080"
  val WebServerAddress = "0.0.0.0"

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

  case class Effort(
    from: DateTime,
    to: DateTime,
    duration: Duration
  )
}
