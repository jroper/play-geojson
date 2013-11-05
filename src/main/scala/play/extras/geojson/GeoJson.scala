package play.extras.geojson

import scala.collection.immutable.Seq
import play.api.libs.json._
import play.api.libs.functional._
import play.api.libs.functional.syntax._
import play.api.data.validation.ValidationError

/**
 * A GeoJSON object.
 *
 * @tparam C The object used to model the CRS that this GeoJSON object uses.
 */
sealed trait GeoJson[C] {
  val bbox: Option[(C, C)]
}

object GeoJson {
  implicit def geoJsonWrites[C](implicit crs: CrsFormat[C]): Writes[GeoJson[C]] =
    GeoFormats.writesWithCrs(GeoFormats.geoJsonFormat[C](crs.format))
  implicit def geoJsonReads[C](implicit crs: CrsFormat[C]): Reads[GeoJson[C]] =
    GeoFormats.geoJsonFormat(crs.format)
}

/**
 * A GeoJSON Geometry object.
 *
 * @tparam C The object used to model the CRS that this GeoJSON object uses.
 */
sealed trait Geometry[C] extends GeoJson[C]

object Geometry {
  implicit def geometryReads[C](implicit crs: CrsFormat[C]): Reads[Geometry[C]] =
    GeoFormats.geometryFormat(crs.format)
}

/**
 * A GeoJSON Point object.
 *
 * @param coordinates The coordinates of this point.
 * @param bbox The bounding box of the point, if any.
 * @tparam C The object used to model the CRS that this GeoJSON object uses.
 */
case class Point[C](coordinates: C, bbox: Option[(C, C)] = None) extends Geometry[C]

object Point {
  implicit def pointReads[C](implicit crs: CrsFormat[C]): Reads[Point[C]] =
    GeoFormats.pointFormat(crs.format)
}

/**
 * A GeoJSON MultiPoint object.
 *
 * @param coordinates The sequence coordinates for the points.
 * @param bbox The bounding box for the points, if any.
 * @tparam C The object used to model the CRS that this GeoJSON object uses.
 */
case class MultiPoint[C](coordinates: Seq[C], bbox: Option[(C, C)] = None) extends Geometry[C]

object MultiPoint {
  implicit def multiPointReads[C](implicit crs: CrsFormat[C]): Reads[MultiPoint[C]] =
    GeoFormats.multiPointFormat(crs.format)
}

/**
 * A GeoJSON LineString object.
 *
 * @param coordinates The sequence of coordinates for the line.
 * @param bbox The bounding box for the line, if any.
 * @tparam C The object used to model the CRS that this GeoJSON object uses.
 */
case class LineString[C](coordinates: Seq[C], bbox: Option[(C, C)] = None) extends Geometry[C]

object LineString {
  implicit def lineStringReads[C](implicit crs: CrsFormat[C]): Reads[LineString[C]] =
    GeoFormats.lineStringFormat(crs.format)
}

/**
 * A GeoJSON MultiLineString object.
 *
 * @param coordinates The sequence of lines.
 * @param bbox The bounding box for the lines, if any.
 * @tparam C The object used to model the CRS that this GeoJSON object uses.
 */
case class MultiLineString[C](coordinates: Seq[Seq[C]], bbox: Option[(C, C)] = None) extends Geometry[C]

object MultiLineString {
  implicit def multiLineStringReads[C](implicit crs: CrsFormat[C]): Reads[MultiLineString[C]] =
    GeoFormats.multiLineStringFormat(crs.format)
}

/**
 * A GeoJSON Polygon object.
 *
 * @param coordinates The sequence of corners in the polygon.
 * @param bbox The bounding box for the polygon, if any.
 * @tparam C The object used to model the CRS that this GeoJSON object uses.
 */
case class Polygon[C](coordinates: Seq[C], bbox: Option[(C, C)] = None) extends Geometry[C]

object Polygon {
  implicit def polygonReads[C](implicit crs: CrsFormat[C]): Reads[Polygon[C]] =
    GeoFormats.polygonFormat(crs.format)
}

/**
 * A GeoJSON MultiPolygon object.
 *
 * @param coordinates The sequence of polygons.
 * @param bbox The bounding box for the polygons, if any.
 * @tparam C The object used to model the CRS that this GeoJSON object uses.
 */
case class MultiPolygon[C](coordinates: Seq[Seq[C]], bbox: Option[(C, C)] = None) extends Geometry[C]

object MultiPolygon {
  implicit def multiPolygonReads[C](implicit crs: CrsFormat[C]): Reads[MultiPolygon[C]] =
    GeoFormats.multiPolygonFormat(crs.format)
}

/**
 * A GeoJSON GeometryCollection object.
 *
 * @param geometries The sequence of geometries.
 * @param bbox The bounding box for the geometries, if any.
 * @tparam C The object used to model the CRS that this GeoJSON object uses.
 */
case class GeometryCollection[C](geometries: Seq[Geometry[C]], bbox: Option[(C, C)] = None) extends Geometry[C]

object GeometryCollection {
  implicit def geometryCollectionReads[C](implicit crs: CrsFormat[C]): Reads[GeometryCollection[C]] =
    GeoFormats.geometryCollectionFormat(crs.format)
}

/**
 * A GeoJSON FeatureCollection object.
 *
 * @param features The sequence of features.
 * @param bbox The bounding box for the sequence of features, if any.
 * @tparam C The object used to model the CRS that this GeoJSON object uses.
 */
case class FeatureCollection[C](features: Seq[Feature[C]], bbox: Option[(C, C)] = None) extends GeoJson[C]

object FeatureCollection {
  implicit def featureCollectionReads[C](implicit crs: CrsFormat[C]): Reads[FeatureCollection[C]] =
    GeoFormats.featureCollectionFormat(crs.format)
}

/**
 * A GeoJSON Feature object.
 *
 * @param geometry The geometry for the feature.
 * @param properties The properties for the feature, if any.
 * @param id The id of the feature, if any.
 * @param bbox The bounding box for the feature, if any.
 * @tparam C The object used to model the CRS that this GeoJSON object uses.
 */
case class Feature[C](geometry: Geometry[C],
                      properties: Option[JsObject] = None,
                      id: Option[JsValue] = None,
                      bbox: Option[(C, C)] = None) extends GeoJson[C]

object Feature {
  implicit def featureReads[C](implicit crs: CrsFormat[C]): Reads[Feature[C]] =
    GeoFormats.featureFormat(crs.format)
}

/**
 * A GeoJSON coordinate reference system (CRS).
 */
sealed trait Crs

object Crs {
  implicit val crsFormat: Format[Crs] = Format(
    Reads { json =>
      (json \ "type").asOpt[String] match {
        case Some("name") => NamedCrs.namedCrsFormat.reads(json)
        case Some("link") => LinkedCrs.linkedCrsFormat.reads(json)
        case _ => JsError("Not a CRS")
      }
    },
    Writes {
      case named: NamedCrs => NamedCrs.namedCrsFormat.writes(named)
      case linked: LinkedCrs => LinkedCrs.linkedCrsFormat.writes(linked)
    }
  )
}

/**
 * A GeoJSON named CRS.
 *
 * @param name The name of the CRS.
 */
case class NamedCrs(name: String) extends Crs

object NamedCrs {
  implicit val namedCrsFormat: Format[NamedCrs] =
    GeoFormats.geoJsonFormatFor("name", (__ \ "properties").format(Json.format[NamedCrs]))
}

/**
 * A GeoJSON linked CRS.
 *
 * @param href The href for the CRS.
 * @param type The type of the link, if any.
 */
case class LinkedCrs(href: String, `type`: Option[String]) extends Crs

object LinkedCrs {
  implicit val linkedCrsFormat: Format[LinkedCrs] =
    GeoFormats.geoJsonFormatFor("link", (__ \ "properties").format(Json.format[LinkedCrs]))
}

/**
 * A latitude longitude CRS, for use with WGS84.
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
 * A CRS format
 */
trait CrsFormat[C] {
  /**
   * The CRS for the CRS format
   */
  def crs: Crs

  /**
   * The format to use to write the CRS.
   */
  def format: Format[C]

  /**
   * Whether this is the default CRS format.  If so, no CRS information will be added to the GeoJSON object when
   * serialised.
   */
  def isDefault = false
}

/**
 * The WGS84 CRS format.
 */
object Wgs84Format extends CrsFormat[LatLng] {
  val crs = NamedCrs("urn:ogc:def:crs:OGC:1.3:CRS84")
  val format = Format[LatLng](
    __.read[Seq[Double]].map {
      case Seq(lng, lat) => LatLng(lat, lng)
    }, Writes(latLng => Json.arr(latLng.lng, latLng.lat))
  )

  override def isDefault = true
}

/**
 * These are the raw "internal" formats.  They do not add the CRS parameter when serialising.
 */
private object GeoFormats {

  /**
   * Adds contramap ops to functional builders
   */
  object ExtendedWrites {
    // adds pathWrite function to JsPath creates a JsObject from a path and value
    implicit class PathWrites(val path: JsPath) extends AnyVal {
      def pathWrite[A : Writes](a: A): OWrites[Any] = OWrites[Any](_ => JsPath.createObj(path -> (implicitly[Writes[A]].writes(a))))
    }

    implicit class FunctionalBuilderWithContraOps[M[_] : ContravariantFunctor : FunctionalCanBuild, A](val ma: M[A]) {
      def ~~> [B <: A](mb: M[B]): M[B] = implicitly[ContravariantFunctor[M]].contramap(
        implicitly[FunctionalCanBuild[M]].apply(ma,mb)
        , (b:B) => new play.api.libs.functional.~(b:A, b:B)
      )

      def <~~ [B >: A](mb: M[B]): M[A] = implicitly[ContravariantFunctor[M]].contramap(
        implicitly[FunctionalCanBuild[M]].apply(ma,mb),
        (a:A) => new play.api.libs.functional.~(a:A, a:B)
      )
    }

  }

  import ExtendedWrites._

  /**
   * Reads a GeoJSON type property with the given type.
   *
   * If the type is not the given name, a validation error is thrown.
   */
  def readType(geoJsonType: String): Reads[String] =
    (__ \ "type").read[String].filter(new ValidationError("Geometry is not a " + geoJsonType))(_ == geoJsonType)

  /**
   * Writes for the GeoJSON type property for the given type.
   */
  def writeType(geoJsonType: String): OWrites[Any] = (__ \ "type").pathWrite(geoJsonType)

  /**
   * Format for the bbox property.
   */
  def formatBbox[C : Format]: OFormat[Option[(C, C)]] = (__ \ "bbox").formatNullable[(C, C)]

  implicit def crsBoxFormat[C](implicit cFormat: Format[C]): Format[(C, C)] = Format(
    Reads[(C, C)] {
      case JsArray(seq) =>
        val (first, second) = seq.splitAt(seq.size / 2)
        for {
          f <- cFormat.reads(JsArray(first))
          s <- cFormat.reads(JsArray(second))
        } yield (f, s)
      case _ => JsError("bbox must be an array")
    }, Writes { (bbox: (C, C)) =>
      (cFormat.writes(bbox._1), cFormat.writes(bbox._2)) match {
        case (a: JsArray, b: JsArray) => a ++ b
        case _ => throw new RuntimeException("CRS format writes must produce a JsArray")
      }
    }
  )

  /**
   * Create a GeoJSON format with the given type name.
   */
  def geoJsonFormatFor[G](geoJsonType: String, format: OFormat[G]): Format[G] = Format[G](
    readType(geoJsonType) ~> format,
    writeType(geoJsonType) ~~> format
  )

  /**
   * Create a Geometry format with the given type name.
   */
  def geometryFormatFor[G, T : Format, C : Format](geoJsonType: String,
                                                  read: (T, Option[(C, C)]) => G,
                                                  write: G => Option[(T, Option[(C, C)])]): Format[G] =
    geoJsonFormatFor(geoJsonType,
      ((__ \ "coordinates").format[T] ~ formatBbox[C]).apply(read, unlift(write))
    )

  /*
   * Formats for each of the different GeoJSON types.  These are internal, they do not add a CRS property to the
   * output.
   */

  implicit def pointFormat[C : Format]: Format[Point[C]] =
    geometryFormatFor("Point", Point.apply, Point.unapply)

  implicit def multiPointFormat[C : Format]: Format[MultiPoint[C]] =
    geometryFormatFor("MultiPoint", MultiPoint.apply, MultiPoint.unapply)

  implicit def lineStringFormat[C : Format]: Format[LineString[C]] =
    geometryFormatFor("LineString", LineString.apply, LineString.unapply)

  implicit def multiLineStringFormat[C : Format]: Format[MultiLineString[C]] =
    geometryFormatFor("MultiLineString", MultiLineString.apply, MultiLineString.unapply)

  implicit def polygonFormat[C : Format]: Format[Polygon[C]] =
    geometryFormatFor("Polygon", Polygon.apply, Polygon.unapply)

  implicit def multiPolygonFormat[C : Format]: Format[MultiPolygon[C]] =
    geometryFormatFor("MultiPolygon", MultiPolygon.apply, MultiPolygon.unapply)

  implicit def geometryCollectionFormat[C : Format]: Format[GeometryCollection[C]] =
    geoJsonFormatFor("GeometryCollection",
      ((__ \ "geometries").format[Seq[Geometry[C]]] ~ formatBbox[C])
        .apply(GeometryCollection.apply, unlift(GeometryCollection.unapply))
    )

  implicit def featureFormat[C : Format]: Format[Feature[C]] =
    geoJsonFormatFor("Feature", (
        (__ \ "geometry").format[Geometry[C]] ~
        (__ \ "properties").formatNullable[JsObject] ~
        // The spec isn't clear on what the id can be
        (__ \ "id").formatNullable[JsValue] ~
        formatBbox[C]
      ).apply(Feature.apply, unlift(Feature.unapply))
    )

  implicit def featureCollectionFormat[C : Format]: Format[FeatureCollection[C]] =
    geoJsonFormatFor("FeatureCollection", 
      ((__ \ "features").format[Seq[Feature[C]]] ~ formatBbox[C])
        .apply(FeatureCollection.apply, unlift(FeatureCollection.unapply))
    )

  implicit def geometryFormat[C : Format]: Format[Geometry[C]] = Format(
    Reads { json =>
      (json \ "type").asOpt[String] match {
        case Some("Point") => json.validate(pointFormat[C])
        case Some("MultiPoint") => json.validate(multiPointFormat[C])
        case Some("LineString") => json.validate(lineStringFormat[C])
        case Some("MultiLineString") => json.validate(multiLineStringFormat[C])
        case Some("Polygon") => json.validate(polygonFormat[C])
        case Some("MultiPolygon") => json.validate(multiPolygonFormat[C])
        case Some("GeometryCollection") => json.validate(geometryCollectionFormat[C])
        case _ => JsError("Not a geometry")
      }
    },
    Writes {
      case point: Point[C] => pointFormat[C].writes(point)
      case multiPoint: MultiPoint[C] => multiPointFormat[C].writes(multiPoint)
      case lineString: LineString[C] => lineStringFormat[C].writes(lineString)
      case multiLineString: MultiLineString[C] => multiLineStringFormat[C].writes(multiLineString)
      case polygon: Polygon[C] => polygonFormat[C].writes(polygon)
      case multiPolygon: MultiPolygon[C] => multiPolygonFormat[C].writes(multiPolygon)
      case geometryCollection: GeometryCollection[C] => geometryCollectionFormat[C].writes(geometryCollection)
    }
  )

  def geoJsonFormat[C: Format]: Format[GeoJson[C]] = Format(
    Reads { json =>
      (json \ "type").asOpt[String] match {
        case Some("Point") => json.validate(pointFormat[C])
        case Some("MultiPoint") => json.validate(multiPointFormat[C])
        case Some("LineString") => json.validate(lineStringFormat[C])
        case Some("MultiLineString") => json.validate(multiLineStringFormat[C])
        case Some("Polygon") => json.validate(polygonFormat[C])
        case Some("MultiPolygon") => json.validate(multiPolygonFormat[C])
        case Some("GeometryCollection") => json.validate(geometryCollectionFormat[C])
        case Some("Feature") => json.validate(featureFormat[C])
        case Some("FeatureCollection") => json.validate(featureCollectionFormat[C])
        case _ => JsError("Not a geometry")
      }
    },
    Writes {
      case point: Point[C] => pointFormat[C].writes(point)
      case multiPoint: MultiPoint[C] => multiPointFormat[C].writes(multiPoint)
      case lineString: LineString[C] => lineStringFormat[C].writes(lineString)
      case multiLineString: MultiLineString[C] => multiLineStringFormat[C].writes(multiLineString)
      case polygon: Polygon[C] => polygonFormat[C].writes(polygon)
      case multiPolygon: MultiPolygon[C] => multiPolygonFormat[C].writes(multiPolygon)
      case geometryCollection: GeometryCollection[C] => geometryCollectionFormat[C].writes(geometryCollection)
      case feature: Feature[C] => featureFormat[C].writes(feature)
      case featureCollection: FeatureCollection[C] => featureCollectionFormat[C].writes(featureCollection)
    }
  )

  def writesWithCrs[C, G](writes: Writes[G])(implicit crs: CrsFormat[C]) = writes.transform { json =>
    if (crs.isDefault) {
      json
    } else {
      json match {
        case obj: JsObject => obj ++ Json.obj("crs" -> crs.crs)
        case other => other
      }
    }
  }

}
