organization := "com.typesafe.play.extras"

name := "play-geojson"

version := "1.1.0"

scalaVersion := "2.11.4"

crossScalaVersions := Seq("2.11.4", "2.10.4")

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.3.7" % "provided",
  "org.specs2" %% "specs2" % "2.3.12" %  "test"
)

publishTo <<= (version) { version: String =>
  val nexus = "https://private-repo.typesafe.com/typesafe/"
  if (version.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "maven-snapshots/")
  else                                   Some("releases"  at nexus + "maven-releases/")
}

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

