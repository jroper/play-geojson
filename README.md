# Play GeoJSON Library

This library provides [`play-json`](https://github.com/playframework/play-json) `Reads`/`Writes`/`Formats` for [GeoJSON](https://geojson.org/).

I don't actively maintain this project, but occassionally, if I have time, I do merge pull requests and cut releases. That said, it is a very simple project and many people have found it useful. There has been no need for a bug fix in four years, the code is stable and mature, the only changes have been upgrades of dependencies. If you are planning to use it in a production application, you will most likely have no problems with it, if you do have problems, you may find help here, but you should be prepared to fork the codebase (a little over 500 lines of code including comments) and maintain it yourself in the worst case.

## Features

* Supports all GeoJSON types, including Point, MultiPoint, LineString, MultiLineString, Polygon, MultiPolygon, GeometryCollection, Feature, FeatureCollection.
* Supports any CRS, with `WGS84` (latitude, longitude) provided out of the box.
* Type safe and polymorphic serialisation and deserialisation.
* Correctly adds the `crs` parameter according to the spec.
* Supports bounding boxes.
* Supports optional ids and properties of features.

## Installation instructions

Add the following dependency to `build.sbt`:

```scala
resolvers += Resolver.bintrayRepo("jroper", "maven")
libraryDependencies += "au.id.jazzy" %% "play-geojson" % "1.6.0"
```

### 1.5.0 migration notes

Starting version 1.5.0, `play-geojson` is now deployed to bintray rather than Maven central, the group id/organization has changed from `com.typesafe.play.extras` to `au.id.jazzy.play`, and the package name has changed from `play.extras.geojson` to `au.id.jazzy.play.geojson`. So be sure to add the bintray repo above to your build and migrate your code accordingly.

The motivation behind this change is that this has never been (and probably will never be) an official Play library maintained by Lightbend, I have always maintained it in my spare time, and so the package name and organisation have been updated to reflect this, so as not to set incorrect expectations.

### Version compatibility Matrix

| **play-geojson version** | **play-json version** |
|--------------------------|-----------------------|
| 1.0.x                    | 2.2.x                 |
| 1.1.x                    | 2.3.x                 |
| 1.2.x                    | 2.3.x                 |
| 1.3.x                    | 2.4.x                 |
| 1.4.x                    | 2.5.x                 |
| 1.5.x                    | 2.6.x                 |
| 1.6.x                    | 2.7.x                 |

## Usage instructions

Import the types you need:

```scala
import au.id.jazzy.play.geojson._
```

Serialise a GeoJSON object to a JSON:

```scala
val sydney = Feature(Point(LatLng(-33.86, 151.2111)), 
    properties = Json.obj("name" -> "Sydney"))
val json = Json.toJson(sydney)
```

Deserialise some JSON into a GeoJSON object:

```scala
val json = Json.obj(
  "type" -> "Feature",
  "geometry" -> Json.obj(
    "type" -> "Point",
    "coordinates" -> Seq(151.2111, -33.86)
  ),
  "properties" -> Json.obj(
    "name" -> "Sydney"
  )
)
val sydney = Json.fromJson[Feature[LatLng]](json)
```

Implement a custom CRS:

```scala
case class SphericalMercator(x: Double, y: Double)

object SphericalMercator {
  implicit val format: Format[SphericalMercator] = SphericalMercatorCrs.format
  implicit val crs: CrsFormat[SphericalMercator] = SphericalMercatorCrs
}

object SphericalMercatorCrs extends CrsFormat[SphericalMercator] {
  val crs = NamedCrs("urn:ogc:def:crs:EPSG::3857")
  val format = Format[SphericalMercator](
    __.read[Seq[Double]].map {
      case Seq(x, y) => SphericalMercator(x, y)
    }, Writes(m => Json.arr(m.x, m.y))
  )
}
```

## License

This software is licensed under the Apache 2 license.
