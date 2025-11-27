package ch.asit_asso.extract.plugins.python;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for WKT to GeoJSON conversion
 */
class WKTToGeoJSONTest {
    
    private PythonPlugin plugin;
    private ObjectMapper objectMapper;
    private WKTReader wktReader;
    
    @BeforeEach
    void setUp() {
        plugin = new PythonPlugin();
        objectMapper = new ObjectMapper();
        wktReader = new WKTReader();
    }
    
    @Test
    void testPointConversion() throws Exception {
        String wkt = "POINT (30.5 10.25)";
        
        JsonNode geoJson = convertWKTToGeoJSON(wkt);
        
        assertNotNull(geoJson);
        assertEquals("Point", geoJson.get("type").asText());
        
        JsonNode coordinates = geoJson.get("coordinates");
        assertNotNull(coordinates);
        assertTrue(coordinates.isArray());
        assertEquals(2, coordinates.size());
        assertEquals(30.5, coordinates.get(0).asDouble(), 0.001);
        assertEquals(10.25, coordinates.get(1).asDouble(), 0.001);
    }
    
    @Test
    void testPolygonConversion() throws Exception {
        String wkt = "POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))";
        
        JsonNode geoJson = convertWKTToGeoJSON(wkt);
        
        assertNotNull(geoJson);
        assertEquals("Polygon", geoJson.get("type").asText());
        
        JsonNode coordinates = geoJson.get("coordinates");
        assertNotNull(coordinates);
        assertTrue(coordinates.isArray());
        assertEquals(1, coordinates.size()); // One ring (exterior)
        
        JsonNode ring = coordinates.get(0);
        assertTrue(ring.isArray());
        assertEquals(5, ring.size()); // 5 points (closed ring)
        
        // Check first point
        JsonNode firstPoint = ring.get(0);
        assertEquals(30, firstPoint.get(0).asDouble(), 0.001);
        assertEquals(10, firstPoint.get(1).asDouble(), 0.001);
        
        // Check last point (should be same as first for closed ring)
        JsonNode lastPoint = ring.get(4);
        assertEquals(30, lastPoint.get(0).asDouble(), 0.001);
        assertEquals(10, lastPoint.get(1).asDouble(), 0.001);
    }
    
    @Test
    void testPolygonWithHoleConversion() throws Exception {
        String wkt = "POLYGON ((35 10, 45 45, 15 40, 10 20, 35 10), " +
                     "(20 30, 35 35, 30 20, 20 30))";
        
        JsonNode geoJson = convertWKTToGeoJSON(wkt);
        
        assertNotNull(geoJson);
        assertEquals("Polygon", geoJson.get("type").asText());
        
        JsonNode coordinates = geoJson.get("coordinates");
        assertNotNull(coordinates);
        assertTrue(coordinates.isArray());
        assertEquals(2, coordinates.size()); // Exterior ring + one hole
        
        // Check exterior ring
        JsonNode exteriorRing = coordinates.get(0);
        assertTrue(exteriorRing.isArray());
        assertEquals(5, exteriorRing.size());
        
        // Check hole
        JsonNode hole = coordinates.get(1);
        assertTrue(hole.isArray());
        assertEquals(4, hole.size());
    }
    
    @Test
    void testMultiPolygonConversion() throws Exception {
        String wkt = "MULTIPOLYGON (((30 20, 45 40, 10 40, 30 20)), " +
                     "((15 5, 40 10, 10 20, 5 10, 15 5)))";
        
        JsonNode geoJson = convertWKTToGeoJSON(wkt);
        
        assertNotNull(geoJson);
        assertEquals("MultiPolygon", geoJson.get("type").asText());
        
        JsonNode coordinates = geoJson.get("coordinates");
        assertNotNull(coordinates);
        assertTrue(coordinates.isArray());
        assertEquals(2, coordinates.size()); // Two polygons
        
        // Check first polygon
        JsonNode firstPolygon = coordinates.get(0);
        assertTrue(firstPolygon.isArray());
        assertEquals(1, firstPolygon.size()); // One ring
        
        JsonNode firstRing = firstPolygon.get(0);
        assertTrue(firstRing.isArray());
        assertEquals(4, firstRing.size()); // 4 points
        
        // Check second polygon
        JsonNode secondPolygon = coordinates.get(1);
        assertTrue(secondPolygon.isArray());
        assertEquals(1, secondPolygon.size()); // One ring
    }
    
    @Test
    void testLineStringConversion() throws Exception {
        String wkt = "LINESTRING (30 10, 10 30, 40 40)";
        
        JsonNode geoJson = convertWKTToGeoJSON(wkt);
        
        assertNotNull(geoJson);
        assertEquals("LineString", geoJson.get("type").asText());
        
        JsonNode coordinates = geoJson.get("coordinates");
        assertNotNull(coordinates);
        assertTrue(coordinates.isArray());
        assertEquals(3, coordinates.size()); // 3 points
        
        // Check first point
        JsonNode firstPoint = coordinates.get(0);
        assertEquals(30, firstPoint.get(0).asDouble(), 0.001);
        assertEquals(10, firstPoint.get(1).asDouble(), 0.001);
        
        // Check second point
        JsonNode secondPoint = coordinates.get(1);
        assertEquals(10, secondPoint.get(0).asDouble(), 0.001);
        assertEquals(30, secondPoint.get(1).asDouble(), 0.001);
        
        // Check third point
        JsonNode thirdPoint = coordinates.get(2);
        assertEquals(40, thirdPoint.get(0).asDouble(), 0.001);
        assertEquals(40, thirdPoint.get(1).asDouble(), 0.001);
    }
    
    @Test
    void testComplexPolygonConversion() throws Exception {
        // Test with a more complex polygon (square with precise coordinates)
        String wkt = "POLYGON ((0 0, 100 0, 100 100, 0 100, 0 0))";
        
        JsonNode geoJson = convertWKTToGeoJSON(wkt);
        
        assertNotNull(geoJson);
        assertEquals("Polygon", geoJson.get("type").asText());
        
        JsonNode coordinates = geoJson.get("coordinates");
        JsonNode ring = coordinates.get(0);
        
        // Verify all points
        assertEquals(0, ring.get(0).get(0).asDouble(), 0.001);
        assertEquals(0, ring.get(0).get(1).asDouble(), 0.001);
        
        assertEquals(100, ring.get(1).get(0).asDouble(), 0.001);
        assertEquals(0, ring.get(1).get(1).asDouble(), 0.001);
        
        assertEquals(100, ring.get(2).get(0).asDouble(), 0.001);
        assertEquals(100, ring.get(2).get(1).asDouble(), 0.001);
        
        assertEquals(0, ring.get(3).get(0).asDouble(), 0.001);
        assertEquals(100, ring.get(3).get(1).asDouble(), 0.001);
        
        assertEquals(0, ring.get(4).get(0).asDouble(), 0.001);
        assertEquals(0, ring.get(4).get(1).asDouble(), 0.001);
    }
    
    @Test
    void testInvalidWKTHandling() {
        String invalidWkt = "INVALID WKT STRING";
        
        // Should throw an exception or return null
        assertThrows(Exception.class, () -> {
            convertWKTToGeoJSON(invalidWkt);
        });
    }
    
    @Test
    void testEmptyGeometry() throws Exception {
        String wkt = "POINT EMPTY";
        
        // JTS supports EMPTY geometries
        Geometry geometry = wktReader.read(wkt);
        assertTrue(geometry.isEmpty());
    }
    
    @Test
    void testPrecisionHandling() throws Exception {
        // Test with high precision coordinates
        String wkt = "POINT (123.456789012345 -45.678901234567)";
        
        JsonNode geoJson = convertWKTToGeoJSON(wkt);
        
        JsonNode coordinates = geoJson.get("coordinates");
        assertEquals(123.456789012345, coordinates.get(0).asDouble(), 0.000000000001);
        assertEquals(-45.678901234567, coordinates.get(1).asDouble(), 0.000000000001);
    }
    
    /**
     * Helper method to test the private convertWKTToGeoJSON method
     */
    private JsonNode convertWKTToGeoJSON(String wkt) throws Exception {
        // Use reflection to access the private method
        Method method = PythonPlugin.class.getDeclaredMethod(
            "convertWKTToGeoJSON", String.class, ObjectMapper.class
        );
        method.setAccessible(true);
        
        return (JsonNode) method.invoke(plugin, wkt, objectMapper);
    }
}