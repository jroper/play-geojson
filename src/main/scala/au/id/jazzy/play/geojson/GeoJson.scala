package au.id.jazzy.play.geojson

import scala.collection.immutable.Seq
import play.api.libs.json._
import play.api.libs.functional._
import play.api.libs.functional.syntax._

import scala.language.{higherKinds, implicitConversions}

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
 * @param coordinates A sequence of sequences of corners in the polygon.
 * @param bbox The bounding box for the polygon, if any.
 * @tparam C The object used to model the CRS that this GeoJSON object uses.
 */
case class Polygon[C](coordinates: Seq[Seq[C]], bbox: Option[(C, C)] = None) extends Geometry[C]

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
case class MultiPolygon[C](coordinates: Seq[Seq[Seq[C]]], bbox: Option[(C, C)] = None) extends Geometry[C]

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
  import GeoFormats._

  implicit val crsFormat: Format[Crs] = Format(
    readType.flatMap {
      case "name" => NamedCrs.namedCrsFormat
      case "link" => LinkedCrs.linkedCrsFormat
      case unknown => errorReads("Unknown CRS descriptor type: " + unknown)
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
 * These are the raw "internal" formats.  They do not add the CRS parameter when serialising.
 */
private object GeoFormats {

  /**
   * Adds contramap ops to functional builders
   */
  object ExtendedWrites {
    // adds pathWrite function to JsPath creates a JsObject from a path and value
    implicit class PathWrites(val path: JsPath) extends AnyVal {
      def pathWrite[A : Writes](a: A): OWrites[Any] = OWrites[Any](_ => JsPath.createObj(path -> implicitly[Writes[A]].writes(a)))
    }

    implicit class FunctionalBuilderWithContraOps[M[_] : ContravariantFunctor : FunctionalCanBuild, A](val ma: M[A]) {
      def ~~> [B <: A](mb: M[B]): M[B] = implicitly[ContravariantFunctor[M]].contramap(
        implicitly[FunctionalCanBuild[M]].apply(ma,mb)
        , (b:B) => play.api.libs.functional.~(b:A, b:B)
      )

      def <~~ [B >: A](mb: M[B]): M[A] = implicitly[ContravariantFunctor[M]].contramap(
        implicitly[FunctionalCanBuild[M]].apply(ma,mb),
        (a:A) => play.api.libs.functional.~(a:A, a:B)
      )
    }

  }

  import ExtendedWrites._

  /**
   * Reads the GeoJSON type property.
   */
  def readType: Reads[String] = (__ \ "type").read[String]
  
  /**
   * Reads a GeoJSON type property with the given type.
   *
   * If the type is not the given name, a validation error is thrown.
   */
  def filterType(geoJsonType: String): Reads[String] =
    readType.filter(JsonValidationError("Geometry is not a " + geoJsonType))(_ == geoJsonType)

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
    filterType(geoJsonType) ~> format,
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

  def errorReads[T](message: String) = Reads[T](_ => JsError(message))

  /**
   * Reads is invariant in its type parameter.  This function widens it.
   */
  implicit def widenReads[A, B >: A](reads: Reads[A]): Reads[B] = Reads[B](_.validate(reads))

  /*
   * Formats for each of the different GeoJSON types.  These are internal, they do not add a CRS property to the
   * output.
   */

  def pointFormat[C : Format]: Format[Point[C]] =
    geometryFormatFor("Point", Point.apply, Point.unapply)

  def multiPointFormat[C : Format]: Format[MultiPoint[C]] =
    geometryFormatFor("MultiPoint", MultiPoint.apply, MultiPoint.unapply)

  def lineStringFormat[C : Format]: Format[LineString[C]] =
    geometryFormatFor("LineString", LineString.apply, LineString.unapply)

  def multiLineStringFormat[C : Format]: Format[MultiLineString[C]] =
    geometryFormatFor("MultiLineString", MultiLineString.apply, MultiLineString.unapply)

  def polygonFormat[C : Format]: Format[Polygon[C]] =
    geometryFormatFor("Polygon", Polygon.apply, Polygon.unapply)

  def multiPolygonFormat[C : Format]: Format[MultiPolygon[C]] =
    geometryFormatFor("MultiPolygon", MultiPolygon.apply, MultiPolygon.unapply)

  def geometryCollectionFormat[C : Format]: Format[GeometryCollection[C]] = {
    implicit val gf = geometryFormat[C]
    geoJsonFormatFor("GeometryCollection",
      ((__ \ "geometries").format[Seq[Geometry[C]]] ~ formatBbox[C])
        .apply(GeometryCollection.apply, unlift(GeometryCollection.unapply))
    )
  }

  def featureFormat[C : Format]: Format[Feature[C]] = {
    geoJsonFormatFor("Feature", (
        (__ \ "geometry").format(geometryFormat[C]) ~
        (__ \ "properties").formatNullable[JsObject] ~
        // The spec isn't clear on what the id can be
        (__ \ "id").formatNullable[JsValue] ~
        formatBbox[C]
      ).apply(Feature.apply, unlift(Feature.unapply))
    )
  }

  def featureCollectionFormat[C : Format]: Format[FeatureCollection[C]] = {
    implicit val ff = featureFormat[C]
    geoJsonFormatFor("FeatureCollection",
      ((__ \ "features").format[Seq[Feature[C]]] ~ formatBbox[C])
        .apply(FeatureCollection.apply, unlift(FeatureCollection.unapply))
    )
  }

  def geometryFormat[C : Format]: Format[Geometry[C]] = Format(
    readType.flatMap {
      case "Point" => pointFormat[C]
      case "MultiPoint" => multiPointFormat[C]
      case "LineString" => lineStringFormat[C]
      case "MultiLineString" => multiLineStringFormat[C]
      case "Polygon" => polygonFormat[C]
      case "MultiPolygon" => multiPolygonFormat[C]
      case "GeometryCollection" => geometryCollectionFormat[C]
      case unknown => errorReads("Unknown GeoJSON Geometry type: " + unknown)
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
    (geometryFormat[C]: Reads[Geometry[C]]).or(
      readType.flatMap {
        case "Feature" => featureFormat[C]
        case "FeatureCollection" => featureCollectionFormat[C]
        case unknown => errorReads("Unknown GeoJSON type: " + unknown)
      }
    ),
    Writes {
      case geometry: Geometry[C] => geometryFormat[C].writes(geometry)
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
