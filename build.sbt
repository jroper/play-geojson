import ReleaseTransformations._

version := (version in ThisBuild).value

scalaVersion in ThisBuild := "2.12.2"

val playJsonVersion = "2.6.1"

lazy val root = project.in(file(".")).
  aggregate(playgeojsonJS, playgeojsonJVM).
  settings(
    publish := {},
    publishLocal := {}
  )

lazy val playgeojson = crossProject.in(file(".")).
  // common to both jvm and js
  settings(
  name := "play-geojson",
  version := (version in ThisBuild).value,
  organization := "com.typesafe.play.extras",
  homepage := Some(url("https://github.com/jroper/play-geojson")),
  licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play-json" % "2.6.1",
    "org.specs2" %% "specs2-core" % "3.9.1" % "test"
  ),
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
  },
  pomIncludeRepository := { _ => false },
  // Release settings
  sonatypeProfileName := "com.typesafe",
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  releaseCrossBuild := true,
  releaseTagName := (version in ThisBuild).value,

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
).
  jvmSettings(
    fork := true,
    javaOptions in compile += "-Xmx8G",
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xmx8G"),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Xlint"
    )
  ).
  jsSettings(
    skip in packageJSDependencies := false,
    scalaJSStage in Global := FullOptStage,
    jsDependencies += RuntimeDOM,
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2"
  )

lazy val playgeojsonJVM = playgeojson.jvm
lazy val playgeojsonJS = playgeojson.js
