name := "simple-bot"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= {
  val akkaVersion       = "2.4.3"
  val scalaTestVersion  = "2.2.6"
  Seq(
    "com.typesafe" % "config" % "1.3.0",
    "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-xml-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaVersion,
    "org.scalatest"     %% "scalatest" % scalaTestVersion % "test",
    "org.specs2" %% "specs2-core" % "3.8.3" % "test"
  )
}
    