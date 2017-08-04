package au.id.jazzy.play.geojson

import scala.collection.immutable.Seq
import play.api.libs.json._

/**
 * LngLat is like LatLng except the ordering or latitude and longitude.
 *
 * In GeoJSON, and therefore Elasticsearch, the correct coordinate order 
 * is longitude, latitude (X, Y) within coordinate arrays. This differs 
 * from many Geospatial APIs (e.g., Google Maps) that generally use the 
 * colloquial latitude, longitude (Y, X).
 *
 * @see http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/mapping-geo-shape-type.html
 *
 */
case class LngLat(lng: Double, lat: Double)

object LngLat {
  implicit val lngLatFormat: Format[LngLat] = LngLatCrs.format
  implicit val lngLatCrs: CrsFormat[LngLat] = LngLatCrs

  def asLatLng(p: LngLat): LatLng = LatLng(p.lat, p.lng)
  def fromLatLng(p: LatLng): LngLat = LngLat(p.lng, p.lat)

}

object LngLatCrs extends CrsFormat[LngLat] {
  val crs = NamedCrs("urn:ogc:def:crs:EPSG::4326")
  val format = Format[LngLat](
    __.read[Seq[Double]].map {
      case Seq(lng, lat) => LngLat(lng, lat)
    }, Writes(m => Json.arr(m.lng, m.lat))
  )
  
  override def isDefault = true
}
