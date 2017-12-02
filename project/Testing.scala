import sbt._
import Configs._
import Keys._

object Testing {
  lazy val testAll = TaskKey[Unit]("test-all")

  lazy val e2eSettings = inConfig(EndToEndTest)(Defaults.testSettings) ++ Seq(
    fork in EndToEndTest := false,
    parallelExecution in EndToEndTest := false,
    scalaSource in EndToEndTest := baseDirectory.value / "src/e2e/scala"
  )

  lazy val settings = Defaults.itSettings ++ e2eSettings ++ Seq(
    testAll := (test in EndToEndTest).dependsOn((test in IntegrationTest).dependsOn(test in Test)).value
  )

  lazy val AllScopes = Seq(Test, IntegrationTest, EndToEndTest).mkString(",")
}
