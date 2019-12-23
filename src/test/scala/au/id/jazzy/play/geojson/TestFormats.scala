package au.id.jazzy.play.geojson

import play.api.libs.json.Format

object TestFormats {

  implicit val featureFormat: Format[Feature[LatLng]] = GeoFormats.featureFormat
  implicit val featureCollectionFormat: Format[FeatureCollection[LatLng]] = GeoFormats.featureCollectionFormat
  implicit val geometryCollectionFormat: Format[GeometryCollection[LatLng]] = GeoFormats.geometryCollectionFormat
  implicit val geometryFormat: Format[Geometry[LatLng]] = GeoFormats.geometryFormat
  implicit val lineStringFormat: Format[LineString[LatLng]] = GeoFormats.lineStringFormat
  implicit val multiLineStringFormat: Format[MultiLineString[LatLng]] = GeoFormats.multiLineStringFormat
  implicit val multiPointFormat: Format[MultiPoint[LatLng]] = GeoFormats.multiPointFormat
  implicit val multiPolygonFormat: Format[MultiPolygon[LatLng]] = GeoFormats.multiPolygonFormat
  implicit val pointFormat: Format[Point[LatLng]] = GeoFormats.pointFormat
  implicit val polygonFormat: Format[Polygon[LatLng]] = GeoFormats.polygonFormat


}
