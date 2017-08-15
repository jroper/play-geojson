# Play GeoJSON Library

This is an unofficial library that provides GeoJSON support to Play that you may find useful.

Since it is unofficial, don't expect any support, bug fixes or updates in any sort of timely manner.  Also don't expect any sort of backwards compatibility between releases.  But do expect to find comprehensive support for the GeoJSON spec using Play's JSON API.  This project may also be a useful tool to learning how to do some more complex things such as polymorphic structures in Play's JSON API.

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
libraryDependencies += "au.id.jazzy" %% "play-geojson" % "1.5.0"
```

### 1.5.0 migration notes

Starting version 1.5.0, `play-geojson` is now deployed to bintray rather than Maven central, the group id/organization has changed from `com.typesafe.play.extras` to `au.id.jazzy.play`, and the package name has changed from `play.extras.geojson` to `au.id.jazzy.play.geojson`. So be sure to add the bintray repo above to your build and migrate your code accordingly.

The motivation behind this change is that this has never been (and probably will never be) an official Play library maintained by Lightbend, I have always maintained it in my spare time, and so the package name and organisation have been updated to reflect this, so as not to set incorrect expectations.

### Version compatibility Matrix

| **play-geojson version** | **Play version** |
|--------------------------|------------------|
| 1.0.x                    | 2.2.x            |
| 1.1.x                    | 2.3.x            |
| 1.2.x                    | 2.3.x            |
| 1.3.x                    | 2.4.x            |
| 1.4.x                    | 2.5.x            |
| 1.5.x                    | 2.6.x            |

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
case class SphericalMercator(x: Int, y: Int)

object SphericalMercator {
  implicit val format: Format[SphericalMercator] = SphericalMercatorCrs.format
  implicit val crs: CrsFormat[SphericalMercator] = SphericalMercatorCrs
}

object SphericalMercatorCrs extends CrsFormat[SphericalMercator] {
  val crs = NamedCrs("urn:ogc:def:crs:EPSG::3857")
  val format = Format[SphericalMercator](
    __.read[Seq[Int]].map {
      case Seq(x, y) => SphericalMercator(x, y)
    }, Writes(m => Json.arr(m.x, m.y))
  )
}
```
