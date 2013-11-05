organization := "com.typesafe.play.extras"

name := "play-geojson"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.2.1" % "provided",
  "org.specs2" %% "specs2" % "2.3.1" %  "test"
)

publishTo <<= (version) { version: String =>
  val nexus = "https://private-repo.typesafe.com/typesafe/"
  if (version.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "maven-snapshots/")
  else                                   Some("releases"  at nexus + "maven-releases/")
}

