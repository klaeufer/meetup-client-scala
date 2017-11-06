name := "meetup-client-scala"

version := "0.1"

scalaVersion := "2.12.4"

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")

val vPlay = "2.6.7"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-ahc-ws" % vPlay,
  "com.typesafe.play" %% "play-json" % vPlay,
  "com.typesafe.play" %% "play-netty-server" % vPlay,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.github.nscala-time" %% "nscala-time" % "2.16.0",
  "com.github.scopt" %% "scopt" % "3.7.0"
)
