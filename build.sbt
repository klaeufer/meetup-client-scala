name := "meetup-client-scala"

version := "0.1"

scalaVersion := "2.12.4"

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-ahc-ws-standalone" % "1.1.2",
  "com.typesafe.play" %% "play-ws-standalone-json" % "1.1.2",
  "org.slf4j" % "slf4j-simple" % "1.7.25"
)
