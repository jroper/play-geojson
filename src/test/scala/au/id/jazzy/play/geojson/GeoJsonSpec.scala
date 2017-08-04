package au.id.jazzy.play.geojson

import scala.collection.immutable.Seq
import org.specs2.mutable.Specification
import play.api.libs.json._

object GeoJsonSpec extends Specification {

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

  // Reusable objects
  val pointJson = Json.obj("type" -> "Point", "coordinates" -> Json.arr(2.3, 1.2))
  val point = Point(LatLng(1.2, 2.3))
  val pointJsonCustom = Json.obj("type" -> "Point", "coordinates" -> Json.arr(3, 4))
  val pointCustom = Point(SphericalMercator(3, 4))

  val multipointJson = Json.obj("type" -> "MultiPoint", "coordinates" -> Seq(Json.arr(2.3, 1.2), Json.arr(4.5, 6.7)))
  val multipoint = MultiPoint(Seq(LatLng(1.2, 2.3), LatLng(6.7, 4.5)))
  val multipointJsonCustom = Json.obj("type" -> "MultiPoint", "coordinates" -> Seq(Json.arr(3, 4), Json.arr(5, 6)))
  val multipointCustom = MultiPoint(Seq(SphericalMercator(3, 4), SphericalMercator(5, 6)))

  val featureJson = Json.obj("type" -> "Feature", "geometry" -> pointJson)
  val feature = Feature(point)
  val featureJsonCustom = Json.obj("type" -> "Feature", "geometry" -> pointJsonCustom)
  val featureCustom = Feature(pointCustom)

  "GeoJson" should {

    def geoJsonAssertions[A <: GeoJson[LatLng] : Format](json: JsObject, a: A) = {
      "deserialisation" in {
        Json.fromJson[A](json).asOpt must beSome(a)
      }
      "serialisation" in {
        Json.toJson(a) must_== json
      }
      "geojson deserialisation" in {
        Json.fromJson[GeoJson[LatLng]](json).asOpt must beSome(a)
      }
      "geojson serialisation" in {
        Json.toJson[GeoJson[LatLng]](a) must_== json
      }
    }

    def bboxAssertions[A <: GeoJson[LatLng]: Format](json: JsObject, withBbox: (Option[(LatLng, LatLng)]) => A) = {
      val bboxJson = json + ("bbox" -> Json.arr(3.4, 1.2, 7.8, 5.6))
      val bboxObj = withBbox(Some((LatLng(1.2, 3.4), LatLng(5.6, 7.8))))
      "deserialisation with a bbox" in {
        Json.fromJson[A](bboxJson).asOpt must beSome(bboxObj)
      }
      "serialisation with a bbox" in {
        Json.toJson(bboxObj) must_== bboxJson
      }
    }

    def customCrsAssertions[A <: GeoJson[SphericalMercator]: Format](json: JsObject, a: A) = {
      val customJson = json + ("crs" -> Json.obj(
        "type" -> "name",
        "properties" -> Json.obj("name" -> SphericalMercatorCrs.crs.name)
      ))
      "deserialisation with a custom crs" in {
        Json.fromJson[A](customJson).asOpt must beSome(a)
      }
      "serialisation with a custom crs" in {
        Json.toJson(a) must_== customJson
      }
      "geojson deserialisation with a custom crs" in {
        Json.fromJson[GeoJson[SphericalMercator]](customJson).asOpt must beSome(a)
      }
      "geojson serialisation with a custom crs" in {
        Json.toJson[GeoJson[SphericalMercator]](a) must_== customJson
      }
    }

    def geometryAssertions[A <: Geometry[LatLng] : Format](json: JsObject, a: A) = {
      geoJsonAssertions(json, a)

      "geometry deserialisation" in {
        Json.fromJson[Geometry[LatLng]](json).asOpt must beSome(a)
      }

      "geometry serialisation" in {
        Json.toJson[Geometry[LatLng]](a) must_== json
      }
    }

    "support points" in {
      geometryAssertions(pointJson, point)
      bboxAssertions(pointJson, bbox => point.copy(bbox = bbox))
      customCrsAssertions(pointJsonCustom, pointCustom)
    }

    "support multipoints" in {
      geometryAssertions(multipointJson, multipoint)
      bboxAssertions(multipointJson, bbox => multipoint.copy(bbox = bbox))
      customCrsAssertions(multipointJsonCustom, multipointCustom)
    }

    "support linestrings" in {
      val json = Json.obj("type" -> "LineString", "coordinates" -> Seq(Json.arr(2.3, 1.2), Json.arr(4.5, 6.7)))
      val obj = LineString(Seq(LatLng(1.2, 2.3), LatLng(6.7, 4.5)))
      geometryAssertions(json, obj)
      bboxAssertions(json, bbox => obj.copy(bbox = bbox))
    }

    "support polygons" in {
      val json = Json.obj("type" -> "Polygon", "coordinates" -> Seq(Seq(Json.arr(2.3, 1.2), Json.arr(4.5, 6.7))))
      val obj = Polygon(Seq(Seq(LatLng(1.2, 2.3), LatLng(6.7, 4.5))))
      geometryAssertions(json, obj)
      bboxAssertions(json, bbox => obj.copy(bbox = bbox))
    }

    "support multilinestrings" in {
      val json = Json.obj("type" -> "MultiLineString", "coordinates" -> Seq(
        Json.arr(Json.arr(2.3, 1.2), Json.arr(4.5, 6.7)),
        Json.arr(Json.arr(8.9, 10.1))
      ))
      val obj = MultiLineString(Seq(Seq(LatLng(1.2, 2.3), LatLng(6.7, 4.5)), Seq(LatLng(10.1, 8.9))))
      geometryAssertions(json, obj)
      bboxAssertions(json, bbox => obj.copy(bbox = bbox))
    }

    "support multipolygons" in {
      val json = Json.obj("type" -> "MultiPolygon", "coordinates" -> Seq(
        Json.arr(Json.arr(Json.arr(2.3, 1.2), Json.arr(4.5, 6.7))),
        Json.arr(Json.arr(Json.arr(8.9, 10.1)))
      ))
      val obj = MultiPolygon(Seq(Seq(Seq(LatLng(1.2, 2.3), LatLng(6.7, 4.5))), Seq(Seq(LatLng(10.1, 8.9)))))
      geometryAssertions(json, obj)
      bboxAssertions(json, bbox => obj.copy(bbox = bbox))
    }

    "support geometry collections" in {
      val json = Json.obj("type" -> "GeometryCollection", "geometries" -> Seq(pointJson, multipointJson))
      val obj = GeometryCollection(Seq(point, multipoint))
      geometryAssertions(json, obj)
      bboxAssertions(json, bbox => obj.copy(bbox = bbox))
    }

    "support features" in {
      geoJsonAssertions(featureJson, feature)
      "with ids" in {
        val obj = feature.copy(id = Some(JsString("foo")))
        geoJsonAssertions(featureJson + ("id" -> JsString("foo")), obj)
      }
      "with properties" in {
        val obj = feature.copy(properties = Some(Json.obj("foo" -> "bar")))
        geoJsonAssertions(featureJson + ("properties" -> Json.obj("foo" -> "bar")), obj)
      }
      bboxAssertions(featureJson, bbox => feature.copy(bbox = bbox))
      customCrsAssertions(featureJsonCustom, featureCustom)
    }

    "support feature collections" in {
      val json = Json.obj("type" -> "FeatureCollection", "features" -> Seq(featureJson))
      val obj = FeatureCollection(Seq(feature))
      geoJsonAssertions(json, obj)
      bboxAssertions(json, bbox => obj.copy(bbox = bbox))
    }

    "fail gracefully for unknown geojson types" in {
      Json.fromJson[GeoJson[LatLng]](Json.obj("type" -> "Foo")) must beAnInstanceOf[JsError]
    }

    "fail gracefully for unknown geojson geometry types" in {
      Json.fromJson[Geometry[LatLng]](Json.obj("type" -> "Foo")) must beAnInstanceOf[JsError]
    }

    "fail gracefully if no geojson type attribute is specified" in {
      Json.fromJson[GeoJson[LatLng]](Json.obj()) must beAnInstanceOf[JsError]
    }

    "fail gracefully if no geojson geometry type attribute is specified" in {
      Json.fromJson[Geometry[LatLng]](Json.obj()) must beAnInstanceOf[JsError]
    }

  }

}
