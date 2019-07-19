organization := "au.id.jazzy"
name := "play-geojson"

scalaVersion := "2.13.0"
crossScalaVersions := Seq("2.11.12", "2.12.8", "2.13.0")

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.7.4" % "provided",
  "org.specs2" %% "specs2-core" % "4.6.0" %  "test"
)

resolvers += "Scalaz Bintray Repo" at "https://dl.bintray.com/scalaz/releases"

homepage := Some(url("https://github.com/jroper/play-geojson"))
licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
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
bintrayRepository := "maven"
bintrayPackage := "play-geojson"
