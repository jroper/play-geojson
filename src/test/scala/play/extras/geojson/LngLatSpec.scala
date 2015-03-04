package play.extras.geojson

import scala.collection.immutable.Seq
import org.specs2.mutable.Specification
import play.api.libs.json._

class LngLatSpec extends Specification {

  "LngLat.fromLatLng" should {

    "return LngLat(6132301.564388968, 1290504.0696666553)" in {
      val latlng = LatLng(1290504.0696666553, 6132301.564388968)
      val lnglat = LngLat fromLatLng latlng
      lnglat.lat should be equalTo (latlng.lat)
      lnglat.lng should be equalTo (latlng.lng)
    }

  }

  "LngLat.asLatLng" should {

    "return LatLng(1290504.0696666553, 6132301.564388968)" in {
      val lnglat = LngLat(6132301.564388968, 1290504.0696666553)
      val latlng = LngLat asLatLng lnglat
      latlng.lat should be equalTo (lnglat.lat)
      latlng.lng should be equalTo (lnglat.lng)
    }
  }

}
