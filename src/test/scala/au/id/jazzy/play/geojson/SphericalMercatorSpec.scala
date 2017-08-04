package au.id.jazzy.play.geojson

import org.specs2.mutable.Specification

class SphericalMercatorSpec extends Specification {

  "SphericalMercator.fromEPSG4326" should {

    "return LatLng(1290504.0696666553, 6132301.564388968)" in {
      val epsg3857 = SphericalMercator.fromEPSG4326(LatLng(48.1527311, 11.5927953))
      epsg3857.x should be ~(1290504.0696666553 +/- 0.00000001)
      epsg3857.y should be ~(6132301.564388968 +/- 0.00000001)
    }

    "return LatLng(1289983.283692877, 6131013.827170381)" in {
      val epsg3857 = SphericalMercator.fromEPSG4326(LatLng(48.145013, 11.588117))
      epsg3857.x should be ~(1289983.283692877 +/- 0.00000001)
      epsg3857.y should be ~(6131013.827170381 +/- 0.00000001)
    }
  }

  "SphericalMercator.toEPSG4326" should {
    "return LatLng(48.1527311, 11.5927953)" in {
      val epsg4326 = SphericalMercator.toEPSG4326(SphericalMercator(1290504.0696666553, 6132301.564388968))
      epsg4326.lat should be ~(48.1527311 +/- 0.00000001)
      epsg4326.lng should be ~(11.59279530 +/- 0.00000001)
    }

    "return LatLng(48.145013, 11.588117)" in {
      val epsg4326 = SphericalMercator.toEPSG4326(SphericalMercator(1289983.283692877, 6131013.827170381))
      epsg4326.lat should be ~(48.145013 +/- 0.00000001)
      epsg4326.lng should be ~(11.588117 +/- 0.00000001)

    }
  }

}
