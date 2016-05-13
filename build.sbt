name := "simple-bot"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= {
  Seq(
    "com.typesafe" % "config" % "1.3.0",
    "com.typesafe.akka" %% "akka-http-experimental" % "2.4.2",
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.2",
    "com.typesafe.akka" %% "akka-http-xml-experimental" % "2.4.2"
  )
}
    