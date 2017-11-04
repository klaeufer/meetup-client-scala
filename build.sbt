name := "meetup-client-scala"

version := "0.1"

scalaVersion := "2.12.4"

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")

val vPlayStandalone = "1.1.3"
val vPlayFramework = "2.6.7"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-ahc-ws-standalone" % vPlayStandalone,
  "com.typesafe.play" %% "play-ws-standalone-json" % vPlayStandalone,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.nanohttpd" % "nanohttpd" % "2.3.1",
  "com.github.nscala-time" %% "nscala-time" % "2.16.0"
)
