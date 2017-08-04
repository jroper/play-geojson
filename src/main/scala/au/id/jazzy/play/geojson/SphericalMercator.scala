package au.id.jazzy.play.geojson

import play.api.libs.json._
import scala.math._
import play.api.libs.json.Json.toJsFieldJsValueWrapper

case class SphericalMercator(x: Double, y: Double)

/**
 * Code is partially translated from the OpenLayers.js project.
 *
 * @see https://github.com/openlayers/ol3/blob/master/src/ol/proj/epsg3857projection.js
 */
object SphericalMercator {
  implicit val format: Format[SphericalMercator] = SphericalMercatorCrs.format
  implicit val crs: CrsFormat[SphericalMercator] = SphericalMercatorCrs

  /** earth radius in meter */
  val RADIUS = 6378137
  
  /** pre calculated earthradius * PI */
  val HALF_SIZE = Pi * RADIUS

  val EXTENT = List(
    -HALF_SIZE, -HALF_SIZE, HALF_SIZE, HALF_SIZE
  )

  val WORLD_EXTENT = List(-180, -85, 180, 85)

  /**
   * Transformation from EPSG:4326 to EPSG:3857
   * 
   * @param input in LatLng format
   * @param factor, default is earth radius in meter
   * @return EPSG:3857/SphericalMercator projection
   */
  def fromEPSG4326(input: LatLng, factor: Double = RADIUS): SphericalMercator = {
    val y = factor * log(tan(Pi * (input.lat + 90) / 360))
    val x = factor * Pi * input.lng / 180

    SphericalMercator(x, y)
  }

  /**
   * Transformation from EPSG:3857 to EPSG:4326
   * 
   * @param input in EPSG:3857/SphericalMercator format
   * @param factor, default is earth radius in meter
   * @return LatLng in EPSG4326 format
   */
  def toEPSG4326(input: SphericalMercator, factor: Double = RADIUS): LatLng = {
    val lat = 360 * atan(exp(input.y / factor)) / Pi - 90
    val lng = 180 * input.x / (factor * Pi)
    LatLng(lat, lng)
  }

}

object SphericalMercatorCrs extends CrsFormat[SphericalMercator] {
  val crs = NamedCrs("urn:ogc:def:crs:EPSG::3857")
  val format = Format[SphericalMercator](
    __.read[Seq[Double]].map {
      case Seq(x, y) => SphericalMercator(x, y)
    }, Writes(m => Json.arr(m.x, m.y))
  )
}
