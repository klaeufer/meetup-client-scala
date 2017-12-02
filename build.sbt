organization := "edu.luc.etl"

name := "meetup-client-scala"

version := "0.1"

scalaVersion := "2.12.4"

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-encoding", "utf8")

val vPlay = "2.6.7"

lazy val root = (project in file(".")).configs(Configs.all: _*).settings(Testing.settings: _*)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % vPlay,
  "com.typesafe.play" %% "play-ahc-ws" % vPlay,
  "com.typesafe.play" %% "play-akka-http-server" % vPlay,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.github.nscala-time" %% "nscala-time" % "2.16.0",
  "com.github.scopt" %% "scopt" % "3.7.0",
  "com.typesafe.play" %% "play-specs2" % vPlay % Testing.AllScopes,
  "org.specs2" %% "specs2-matcher-extra" % "4.0.2" % Testing.AllScopes
)

Revolver.settings

enablePlugins(JavaAppPackaging)
