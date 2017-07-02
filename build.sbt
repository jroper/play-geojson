organization := "com.typesafe.play.extras"
name := "play-geojson"

scalaVersion := "2.12.2"
crossScalaVersions := Seq("2.12.2")

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.6.1",
  "org.specs2" %% "specs2-core" % "3.9.1" % "test"
)

resolvers += "Scalaz Bintray Repo" at "https://dl.bintray.com/scalaz/releases"

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
sonatypeProfileName := "com.typesafe"
releasePublishArtifactsAction := PgpKeys.publishSigned.value
releaseCrossBuild := true
releaseTagName := (version in ThisBuild).value

import ReleaseTransformations._
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  releaseStepCommand("sonatypeRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)
