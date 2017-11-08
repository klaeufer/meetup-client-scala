package edu.luc.etl.connectorspace

package object meetup {

  val AppName = "meetupConnector"

  val AppVersion = "0.1"

  val PropFileName = "local.properties"

  val KeyClientId = "clientId"

  val KeyClientSecret = "clientSecret"

  val KeyAccessToken = "accessToken"

  val KeyRefreshToken = "refreshToken"

  val AuthUrl = "https://secure.meetup.com/oauth2/authorize"

  val ServerPort = 8080

  val RedirectUrl = s"http://localhost:${ServerPort}"

  val TokenUrl = "https://secure.meetup.com/oauth2/access"
}
