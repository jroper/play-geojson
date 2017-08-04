package au.id.jazzy.play.geojson

import scala.collection.immutable.Seq
import org.specs2.mutable.Specification
import play.api.libs.json._

/**
 * examples from GeoJson Format specification
 * http://geojson.org/geojson-spec.html#appendix-a-geometry-examples
 */
object ExampleSpec extends Specification {

  "GeoJson" should {
    def geoJsonAssertions[A <: GeoJson[LatLng] : Format](json: String, a: A) = {
      val js = Json.parse(json)
      val res = Json.fromJson[A](js)
      res.asOpt must beSome(a)
    }

    "process examples from GeoJson.org" in {

       "Point" in {
         val text = """
         { "type": "Point", "coordinates": [100.0, 0.0] }
         """
         // "coordinates are in x, y order (... longitude, latitude for geographic coordinates)" http://geojson.org/geojson-spec.html#id2
         val obj = Point(LatLng(0.0, 100.0))
         geoJsonAssertions(text, obj)
       }

       "LineString" in {
         val text = """
         { "type": "LineString",
           "coordinates": [ [100.0, 0.0], [101.0, 1.0] ]
         }
         """

         val obj = LineString(Seq(LatLng(0.0, 100.0), LatLng(1.0, 101.0)))
         geoJsonAssertions(text, obj)
       }

       "Polygon, no holes" in {
         val text = """
         { "type": "Polygon",
           "coordinates": [
           [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0] ]
           ]
         }
         """

         val obj = Polygon(Seq(
           Seq(LatLng(0.0, 100.0), LatLng(0.0, 101.0), LatLng(1.0, 101.0), LatLng(1.0, 100.0), LatLng(0.0, 100.0))
         ))
         geoJsonAssertions(text, obj)
       }

       "Polygon with holes" in {
         val text = """
         { "type": "Polygon",
           "coordinates": [
           [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0] ],
           [ [100.2, 0.2], [100.8, 0.2], [100.8, 0.8], [100.2, 0.8], [100.2, 0.2] ]
           ]
         }
         """

         val obj = Polygon(Seq(
           Seq(LatLng(0.0, 100.0), LatLng(0.0, 101.0), LatLng(1.0, 101.0), LatLng(1.0, 100.0), LatLng(0.0, 100.0)),
           Seq(LatLng(0.2, 100.2), LatLng(0.2, 100.8), LatLng(0.8, 100.8), LatLng(0.8, 100.2), LatLng(0.2, 100.2))
         ))
         geoJsonAssertions(text, obj)
       }

       "MultiPoint" in {
         val text = """
         { "type": "MultiPoint",
           "coordinates": [ [100.0, 0.0], [101.0, 1.0] ]
         }
         """

         val obj = MultiPoint(Seq(LatLng(0.0, 100.0), LatLng(1.0, 101.0)))
         geoJsonAssertions(text, obj)
       }

       "MultiLineString" in {
         val text = """
         { "type": "MultiLineString",
           "coordinates": [
           [ [100.0, 0.0], [101.0, 1.0] ],
           [ [102.0, 2.0], [103.0, 3.0] ]
           ]
         }
         """

         val obj = MultiLineString(Seq(
           Seq(LatLng(0.0, 100.0), LatLng(1.0, 101.0)),
           Seq(LatLng(2.0, 102.0), LatLng(3.0, 103.0))
         ))
         geoJsonAssertions(text, obj)
       }

       "MultiPolygon" in {
         val text = """
         { "type": "MultiPolygon",
           "coordinates": [
           [[[102.0, 2.0], [103.0, 2.0], [103.0, 3.0], [102.0, 3.0], [102.0, 2.0]]],
           [[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]],
           [[100.2, 0.2], [100.8, 0.2], [100.8, 0.8], [100.2, 0.8], [100.2, 0.2]]]
           ]
         }
         """

         val obj = MultiPolygon(Seq(
           Seq(
             Seq(LatLng(2.0, 102.0), LatLng(2.0, 103.0), LatLng(3.0, 103.0), LatLng(3.0, 102.0), LatLng(2.0, 102.0))
           ),Seq(
             Seq(LatLng(0.0, 100.0), LatLng(0.0, 101.0), LatLng(1.0, 101.0), LatLng(1.0, 100.0), LatLng(0.0, 100.0)),
             Seq(LatLng(0.2, 100.2), LatLng(0.2, 100.8), LatLng(0.8, 100.8), LatLng(0.8, 100.2), LatLng(0.2, 100.2))
           )
          ))
         geoJsonAssertions(text, obj)
       }

       "GeometryCollection" in {
         val text = """
         { "type": "GeometryCollection",
           "geometries": [
           { "type": "Point",
             "coordinates": [100.0, 0.0]
           },
           { "type": "LineString",
             "coordinates": [ [101.0, 0.0], [102.0, 1.0] ]
           }
           ]
         }
         """

         val obj = GeometryCollection(Seq(
           Point(LatLng(0.0, 100.0)),
           LineString(Seq(LatLng(0.0, 101.0), LatLng(1.0, 102.0)))
         ))
         geoJsonAssertions(text, obj)
       }

       "issue#1 is fixed" in {
         val text =
           """
             {"type":"Feature","properties":{},"geometry":{
             "type":"Polygon",
             "coordinates":[[
             [0.017852783203125,52.202136224203464],[0.13629913330078125,52.142338229345874],[0.26744842529296875,52.171405040721886],[0.2032470703125,52.230953706180294],[0.017852783203125,52.202136224203464]
             ]]}}
           """.stripMargin
         val obj = Feature(Polygon(Seq(
           Seq(LatLng(52.202136224203464, 0.017852783203125), LatLng(52.142338229345874, 0.13629913330078125), LatLng(52.171405040721886, 0.26744842529296875), LatLng(52.230953706180294, 0.2032470703125), LatLng(52.202136224203464, 0.017852783203125))
         )), properties = Some(JsObject(Seq())))
         geoJsonAssertions(text, obj)
       }
     }
  }

}
