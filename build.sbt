organization := "com.typesafe.play.extras"

name := "play-geojson"

scalaVersion := "2.11.6"

crossScalaVersions := Seq("2.11.6", "2.10.4")

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.4.2" % "provided",
  "org.specs2" %% "specs2-core" % "3.4" %  "test"
)

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Scalaz Bintray Repo" at "https://dl.bintray.com/scalaz/releases"

// Publish settings
publishTo <<= version { version: String =>
  val nexus = "https://private-repo.typesafe.com/typesafe/"
  if (version.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "maven-snapshots/")
  else                                   Some("releases"  at nexus + "maven-releases/")
}

homepage := Some(url("https://github.com/jroper/play-geojson"))

licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

pomExtra := {
  <scm>
    <url>https://github.com/jroper/play-geojson</url>
    <connection>scm:git:git@github.com:jroper/play-geojson.git</connection>
  </scm>
  <developers>
    <developer>
      <id>jroper</id>
      <name>James Roper</name>
      <url>https://jazzy.id.au</url>
    </developer>
  </developers>
}

pomIncludeRepository := { _ => false }

// Release settings
releaseSettings

ReleaseKeys.publishArtifactsAction := PgpKeys.publishSigned.value

ReleaseKeys.crossBuild := true

ReleaseKeys.tagName := (version in ThisBuild).value

