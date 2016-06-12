import sbt.Keys._

name := "simple-bot"

version := "1.0"

lazy val commonSettings = Seq(
  scalaVersion := "2.11.8",
  libraryDependencies ++= {
    val akkaVersion = "2.4.3"
    val scalaTestVersion = "2.2.6"
    Seq(
      "com.typesafe" % "config" % "1.3.0",
      "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
      "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaVersion,
      "com.typesafe.akka" %% "akka-http-xml-experimental" % akkaVersion,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
      "org.specs2" %% "specs2-core" % "3.8.3" % "test",
      "org.mockito" % "mockito-core" % "1.10.19" % "test"
    )
  }
)

lazy val root = (project in file(".")).settings(commonSettings: _*)
lazy val core = (project in file("modules/core"))
  .settings(name := "core")
  .settings(commonSettings: _*)
lazy val slack_bot = (project in file("modules/slack_bot"))
  .settings(name := "slack_bot")
  .settings(commonSettings: _*)
  //.settings(javacOptions += "-Dconfig.resource=slack-application.conf")
  .dependsOn(core)
lazy val typetalk_bot = (project in file("modules/typetalk_bot"))
  .settings(name := "typetalk_bot")
  .settings(commonSettings: _*)
  //.settings(javacOptions += "-Dconfig.resource=typetalk-application.conf")
  .dependsOn(core)
