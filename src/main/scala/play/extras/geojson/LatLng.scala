package play.extras.geojson

import scala.collection.immutable.Seq
import play.api.libs.json._
import play.api.libs.functional._
import play.api.libs.functional.syntax._

/**
 * A latitude longitude CRS, for use with WGS84 ( == EPSG:4326).
 *
 * @param lat The latitude.
 * @param lng The longitude.
 */
case class LatLng(lat: Double, lng: Double)

object LatLng {
  implicit val latLngFormat: Format[LatLng] = Wgs84Format.format
  implicit val latLngCrs: CrsFormat[LatLng] = Wgs84Format
}

/**
 * The WGS84 CRS format. Equals to EPSG:4326 CRS format.
 */
object Wgs84Format extends CrsFormat[LatLng] {
  val crs = NamedCrs("urn:ogc:def:crs:OGC:1.3:CRS84")
  val format = Format[LatLng](
    __.read[Seq[Double]].map {
      case Seq(lat, lng) => LatLng(lat, lng)
    }, Writes(latLng => Json.arr(latLng.lat, latLng.lng))
  )

  override def isDefault = true
}