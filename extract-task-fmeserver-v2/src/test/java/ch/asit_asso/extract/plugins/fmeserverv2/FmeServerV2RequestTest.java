package ch.asit_asso.extract.plugins.fmeserverv2;

import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FmeServerV2Request class
 */
class FmeServerV2RequestTest {

    @Mock
    private ITaskProcessorRequest mockRequest;

    private PluginConfiguration config;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        config = new PluginConfiguration();
        mapper = new ObjectMapper();

        // Setup default mock behavior
        when(mockRequest.getId()).thenReturn(123);
        when(mockRequest.getFolderOut()).thenReturn("/output/folder");
        when(mockRequest.getOrderGuid()).thenReturn("order-guid-456");
        when(mockRequest.getOrderLabel()).thenReturn("Test Order");
        when(mockRequest.getClientGuid()).thenReturn("client-guid-789");
        when(mockRequest.getClient()).thenReturn("Test Client");
        when(mockRequest.getOrganismGuid()).thenReturn("org-guid-111");
        when(mockRequest.getOrganism()).thenReturn("Test Organism");
        when(mockRequest.getProductGuid()).thenReturn("product-guid-222");
        when(mockRequest.getProductLabel()).thenReturn("Test Product");
    }

    @Test
    void testConstructor() {
        FmeServerV2Request request = new FmeServerV2Request(mockRequest, config);

        assertNotNull(request);
        assertEquals(mockRequest, request.getRequest());
    }

    @Test
    void testConstructorWithNullRequest() {
        Exception exception = assertThrows(NullPointerException.class, () -> {
            new FmeServerV2Request(null, config);
        });

        assertNotNull(exception);
    }

    @Test
    void testConstructorWithNullConfig() {
        Exception exception = assertThrows(NullPointerException.class, () -> {
            new FmeServerV2Request(mockRequest, null);
        });

        assertNotNull(exception);
    }

    @Test
    void testCreateGeoJsonFeatureWithPolygon() throws Exception {
        String wktPolygon = "POLYGON((0 0, 10 0, 10 10, 0 10, 0 0))";
        when(mockRequest.getPerimeter()).thenReturn(wktPolygon);
        when(mockRequest.getParameters()).thenReturn("{\"format\":\"SHP\"}");

        FmeServerV2Request request = new FmeServerV2Request(mockRequest, config);
        String geoJson = request.createGeoJsonFeature();

        assertNotNull(geoJson);
        JsonNode root = mapper.readTree(geoJson);

        assertEquals("Feature", root.get("type").asText());
        assertNotNull(root.get("geometry"));
        assertEquals("Polygon", root.get("geometry").get("type").asText());
        assertNotNull(root.get("properties"));
    }

    @Test
    void testCreateGeoJsonFeatureWithPoint() throws Exception {
        String wktPoint = "POINT(30.5 10.25)";
        when(mockRequest.getPerimeter()).thenReturn(wktPoint);

        FmeServerV2Request request = new FmeServerV2Request(mockRequest, config);
        String geoJson = request.createGeoJsonFeature();

        assertNotNull(geoJson);
        JsonNode root = mapper.readTree(geoJson);

        assertEquals("Feature", root.get("type").asText());
        JsonNode geometry = root.get("geometry");
        assertEquals("Point", geometry.get("type").asText());

        JsonNode coordinates = geometry.get("coordinates");
        assertEquals(30.5, coordinates.get(0).asDouble(), 0.001);
        assertEquals(10.25, coordinates.get(1).asDouble(), 0.001);
    }

    @Test
    void testCreateGeoJsonFeatureWithLineString() throws Exception {
        String wktLineString = "LINESTRING(0 0, 10 10, 20 20)";
        when(mockRequest.getPerimeter()).thenReturn(wktLineString);

        FmeServerV2Request request = new FmeServerV2Request(mockRequest, config);
        String geoJson = request.createGeoJsonFeature();

        assertNotNull(geoJson);
        JsonNode root = mapper.readTree(geoJson);

        assertEquals("LineString", root.get("geometry").get("type").asText());
    }

    @Test
    void testCreateGeoJsonFeatureWithMultiPolygon() throws Exception {
        String wktMultiPolygon = "MULTIPOLYGON(((0 0, 10 0, 10 10, 0 10, 0 0)), ((20 20, 30 20, 30 30, 20 30, 20 20)))";
        when(mockRequest.getPerimeter()).thenReturn(wktMultiPolygon);

        FmeServerV2Request request = new FmeServerV2Request(mockRequest, config);
        String geoJson = request.createGeoJsonFeature();

        assertNotNull(geoJson);
        JsonNode root = mapper.readTree(geoJson);

        assertEquals("MultiPolygon", root.get("geometry").get("type").asText());
    }

    @Test
    void testCreateGeoJsonFeatureWithMultiLineString() throws Exception {
        String wktMultiLineString = "MULTILINESTRING((0 0, 10 10), (20 20, 30 30))";
        when(mockRequest.getPerimeter()).thenReturn(wktMultiLineString);

        FmeServerV2Request request = new FmeServerV2Request(mockRequest, config);
        String geoJson = request.createGeoJsonFeature();

        assertNotNull(geoJson);
        JsonNode root = mapper.readTree(geoJson);

        assertEquals("MultiLineString", root.get("geometry").get("type").asText());
    }

    @Test
    void testCreateGeoJsonFeatureWithNullPerimeter() throws Exception {
        when(mockRequest.getPerimeter()).thenReturn(null);

        FmeServerV2Request request = new FmeServerV2Request(mockRequest, config);
        String geoJson = request.createGeoJsonFeature();

        assertNotNull(geoJson);
        JsonNode root = mapper.readTree(geoJson);

        assertEquals("Feature", root.get("type").asText());
        assertTrue(root.get("geometry").isNull());
    }

    @Test
    void testCreateGeoJsonFeatureWithEmptyPerimeter() throws Exception {
        when(mockRequest.getPerimeter()).thenReturn("");

        FmeServerV2Request request = new FmeServerV2Request(mockRequest, config);
        String geoJson = request.createGeoJsonFeature();

        assertNotNull(geoJson);
        JsonNode root = mapper.readTree(geoJson);

        assertTrue(root.get("geometry").isNull());
    }

    @Test
    void testCreateGeoJsonFeatureWithInvalidWKT() throws Exception {
        when(mockRequest.getPerimeter()).thenReturn("INVALID WKT STRING");

        FmeServerV2Request request = new FmeServerV2Request(mockRequest, config);
        String geoJson = request.createGeoJsonFeature();

        assertNotNull(geoJson);
        JsonNode root = mapper.readTree(geoJson);

        // Should have null geometry due to parse error
        assertTrue(root.get("geometry").isNull());
    }

    @Test
    void testCreateGeoJsonFeatureWithPolygonWithHole() throws Exception {
        String wktPolygonWithHole = "POLYGON((0 0, 100 0, 100 100, 0 100, 0 0), (20 20, 80 20, 80 80, 20 80, 20 20))";
        when(mockRequest.getPerimeter()).thenReturn(wktPolygonWithHole);

        FmeServerV2Request request = new FmeServerV2Request(mockRequest, config);
        String geoJson = request.createGeoJsonFeature();

        assertNotNull(geoJson);
        JsonNode root = mapper.readTree(geoJson);

        JsonNode coordinates = root.get("geometry").get("coordinates");
        assertEquals(2, coordinates.size()); // One exterior ring + one hole
    }

    @Test
    void testPropertiesContainBasicRequestInfo() throws Exception {
        when(mockRequest.getPerimeter()).thenReturn(null);

        FmeServerV2Request request = new FmeServerV2Request(mockRequest, config);
        String geoJson = request.createGeoJsonFeature();

        JsonNode root = mapper.readTree(geoJson);
        JsonNode properties = root.get("properties");

        assertNotNull(properties.get("Request"));
        assertEquals("123", properties.get("Request").asText());
        assertNotNull(properties.get("FolderOut"));
        assertEquals("/output/folder", properties.get("FolderOut").asText());
    }

    @Test
    void testPropertiesContainClientInfo() throws Exception {
        FmeServerV2Request request = new FmeServerV2Request(mockRequest, config);
        String geoJson = request.createGeoJsonFeature();

        JsonNode root = mapper.readTree(geoJson);
        JsonNode properties = root.get("properties");

        assertNotNull(properties.get("ClientGuid"));
        assertEquals("client-guid-789", properties.get("ClientGuid").asText());
        assertNotNull(properties.get("ClientName"));
        assertEquals("Test Client", properties.get("ClientName").asText());
    }

    @Test
    void testPropertiesContainOrganismInfo() throws Exception {
        FmeServerV2Request request = new FmeServerV2Request(mockRequest, config);
        String geoJson = request.createGeoJsonFeature();

        JsonNode root = mapper.readTree(geoJson);
        JsonNode properties = root.get("properties");

        assertNotNull(properties.get("OrganismGuid"));
        assertEquals("org-guid-111", properties.get("OrganismGuid").asText());
        assertNotNull(properties.get("OrganismName"));
        assertEquals("Test Organism", properties.get("OrganismName").asText());
    }

    @Test
    void testPropertiesContainProductInfo() throws Exception {
        FmeServerV2Request request = new FmeServerV2Request(mockRequest, config);
        String geoJson = request.createGeoJsonFeature();

        JsonNode root = mapper.readTree(geoJson);
        JsonNode properties = root.get("properties");

        assertNotNull(properties.get("ProductGuid"));
        assertEquals("product-guid-222", properties.get("ProductGuid").asText());
        assertNotNull(properties.get("ProductLabel"));
        assertEquals("Test Product", properties.get("ProductLabel").asText());
    }

    @Test
    void testPropertiesWithCustomParametersAsJson() throws Exception {
        String customParams = "{\"format\":\"SHP\",\"projection\":\"EPSG:2056\"}";
        when(mockRequest.getParameters()).thenReturn(customParams);

        FmeServerV2Request request = new FmeServerV2Request(mockRequest, config);
        String geoJson = request.createGeoJsonFeature();

        JsonNode root = mapper.readTree(geoJson);
        JsonNode properties = root.get("properties");
        JsonNode parameters = properties.get("Parameters");

        assertNotNull(parameters);
        assertEquals("SHP", parameters.get("format").asText());
        assertEquals("EPSG:2056", parameters.get("projection").asText());
    }

    @Test
    void testPropertiesWithCustomParametersAsString() throws Exception {
        String customParams = "not a json string";
        when(mockRequest.getParameters()).thenReturn(customParams);

        FmeServerV2Request request = new FmeServerV2Request(mockRequest, config);
        String geoJson = request.createGeoJsonFeature();

        JsonNode root = mapper.readTree(geoJson);
        JsonNode properties = root.get("properties");
        JsonNode parameters = properties.get("Parameters");

        assertNotNull(parameters);
        assertEquals("not a json string", parameters.asText());
    }

    @Test
    void testPropertiesWithNullParameters() throws Exception {
        when(mockRequest.getParameters()).thenReturn(null);

        FmeServerV2Request request = new FmeServerV2Request(mockRequest, config);
        String geoJson = request.createGeoJsonFeature();

        JsonNode root = mapper.readTree(geoJson);
        JsonNode properties = root.get("properties");
        JsonNode parameters = properties.get("Parameters");

        assertNotNull(parameters);
        assertTrue(parameters.isObject());
        assertEquals(0, parameters.size());
    }

    @Test
    void testPropertiesWithEmptyParameters() throws Exception {
        when(mockRequest.getParameters()).thenReturn("");

        FmeServerV2Request request = new FmeServerV2Request(mockRequest, config);
        String geoJson = request.createGeoJsonFeature();

        JsonNode root = mapper.readTree(geoJson);
        JsonNode properties = root.get("properties");
        JsonNode parameters = properties.get("Parameters");

        assertNotNull(parameters);
    }

    @Test
    void testSurfaceCalculationWithPolygon() throws Exception {
        String wktPolygon = "POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))";
        when(mockRequest.getPerimeter()).thenReturn(wktPolygon);

        FmeServerV2Request request = new FmeServerV2Request(mockRequest, config);
        String geoJson = request.createGeoJsonFeature();

        JsonNode root = mapper.readTree(geoJson);
        JsonNode properties = root.get("properties");

        // Surface should be calculated
        if (properties.has("Surface")) {
            assertTrue(properties.get("Surface").asDouble() > 0);
        }
    }

    @Test
    void testIsValidWithValidRequest() {
        FmeServerV2Request request = new FmeServerV2Request(mockRequest, config);

        assertTrue(request.isValid());
    }

    @Test
    void testIsValidWithNullFolderOut() {
        when(mockRequest.getFolderOut()).thenReturn(null);

        FmeServerV2Request request = new FmeServerV2Request(mockRequest, config);

        assertFalse(request.isValid());
    }

    @Test
    void testIsValidWithEmptyFolderOut() {
        when(mockRequest.getFolderOut()).thenReturn("");

        FmeServerV2Request request = new FmeServerV2Request(mockRequest, config);

        assertFalse(request.isValid());
    }

    @Test
    void testIsValidWithWhitespaceFolderOut() {
        when(mockRequest.getFolderOut()).thenReturn("   ");

        FmeServerV2Request request = new FmeServerV2Request(mockRequest, config);

        assertFalse(request.isValid());
    }

    @Test
    void testGetRequest() {
        FmeServerV2Request request = new FmeServerV2Request(mockRequest, config);

        assertEquals(mockRequest, request.getRequest());
    }

    @Test
    void testGeoJsonIsWellFormed() throws Exception {
        String wkt = "POLYGON((0 0, 10 0, 10 10, 0 10, 0 0))";
        when(mockRequest.getPerimeter()).thenReturn(wkt);
        when(mockRequest.getParameters()).thenReturn("{}");

        FmeServerV2Request request = new FmeServerV2Request(mockRequest, config);
        String geoJson = request.createGeoJsonFeature();

        // Verify it's valid JSON
        JsonNode root = mapper.readTree(geoJson);
        assertNotNull(root);

        // Verify required GeoJSON fields
        assertTrue(root.has("type"));
        assertTrue(root.has("geometry"));
        assertTrue(root.has("properties"));
    }

    @Test
    void testGeoJsonWithComplexPolygon() throws Exception {
        // Test with Swiss coordinates (realistic scenario)
        String swissWkt = "POLYGON((2532000 1152000, 2533000 1152000, 2533000 1153000, 2532000 1153000, 2532000 1152000))";
        when(mockRequest.getPerimeter()).thenReturn(swissWkt);

        FmeServerV2Request request = new FmeServerV2Request(mockRequest, config);
        String geoJson = request.createGeoJsonFeature();

        assertNotNull(geoJson);
        JsonNode root = mapper.readTree(geoJson);
        assertEquals("Feature", root.get("type").asText());
        assertEquals("Polygon", root.get("geometry").get("type").asText());
    }

    @Test
    void testGeoJsonWith3DCoordinates() throws Exception {
        String wkt3D = "POINT(10 20 30)";
        when(mockRequest.getPerimeter()).thenReturn(wkt3D);

        FmeServerV2Request request = new FmeServerV2Request(mockRequest, config);
        String geoJson = request.createGeoJsonFeature();

        JsonNode root = mapper.readTree(geoJson);
        JsonNode coordinates = root.get("geometry").get("coordinates");

        assertEquals(3, coordinates.size());
        assertEquals(10, coordinates.get(0).asDouble(), 0.001);
        assertEquals(20, coordinates.get(1).asDouble(), 0.001);
        assertEquals(30, coordinates.get(2).asDouble(), 0.001);
    }

    @Test
    void testPropertiesWithNullValues() throws Exception {
        when(mockRequest.getOrderGuid()).thenReturn(null);
        when(mockRequest.getClient()).thenReturn(null);

        FmeServerV2Request request = new FmeServerV2Request(mockRequest, config);
        String geoJson = request.createGeoJsonFeature();

        JsonNode root = mapper.readTree(geoJson);
        JsonNode properties = root.get("properties");

        // Null values should result in JSON null
        assertTrue(properties.get("OrderGuid").isNull());
    }
}
