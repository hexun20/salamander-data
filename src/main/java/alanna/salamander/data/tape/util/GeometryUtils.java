package alanna.salamander.data.tape.util;

import mil.nga.sf.geojson.FeatureConverter;
import mil.nga.sf.geojson.GeoJsonObject;
import mil.nga.sf.geojson.Geometry;
import org.geotools.geojson.geom.GeometryJSON;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.postgis.PGgeometry;

import java.io.IOException;
import java.sql.SQLException;

/**
 * PostGIS geometry util
 *
 * @author alanna
 * @since 0.1
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class GeometryUtils {

    /**
     * Pg-Geometry to GeoJSON
     *
     * @param pGeometry Pg-Geometry value
     * @return GeoJSON-Geometry
     * @throws SQLException SQLException
     */
    public static Geometry convert2GGeometry(PGgeometry pGeometry) throws SQLException {
        try {
            StringBuffer sb = new StringBuffer(200);
            pGeometry.getGeometry().outerWKT(sb);
            org.locationtech.jts.geom.Geometry geometry = new WKTReader().read(sb.toString());
            return FeatureConverter.toGeometry(new GeometryJSON(16).toString(geometry));
        } catch (ParseException e) {
            throw new SQLException(e);
        }
    }

    /**
     * WKT to GeoJSON
     *
     * @param wkt wkt value
     * @return GeoJSON-Geometry value
     * @throws ParseException ParseException
     */
    public static Geometry convertWKT2GGeometry(String wkt) throws ParseException {
        WKTReader wktReader = new WKTReader();
        org.locationtech.jts.geom.Geometry geometry = wktReader.read(wkt);
        return FeatureConverter.toGeometry(new GeometryJSON(16).toString(geometry));
    }

    /**
     * GeoJSON to Pg-Geometry
     *
     * @param gGeometry GeoJSON-Geometry
     * @return Pg-Geometry
     * @throws SQLException SQLException
     */
    public static PGgeometry convert2PGeometry(GeoJsonObject gGeometry) throws SQLException {
        if (null == gGeometry) {
            return null;
        }
        GeometryJSON geometryJSON = new GeometryJSON();
        org.locationtech.jts.geom.Geometry geometry;
        try {
            geometry = geometryJSON.read(FeatureConverter.toStringValue(gGeometry));
        } catch (IOException e) {
            throw new SQLException(e);
        }
        org.postgis.PGgeometry pGeometry = new PGgeometry();
        pGeometry.setValue(new WKTWriter().write(geometry));
        return pGeometry;
    }
}
