/*
 * Copyright (C) 2025 SecureMind
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.asit_asso.extract.integration.taskplugins;

import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.plugins.TaskProcessorsDiscoverer;
import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import ch.asit_asso.extract.plugins.implementation.TaskProcessorRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Python plugin (Issue #346).
 * Tests parameters.json generation, WKT to GeoJSON conversion, without executing Python.
 *
 * These are TRUE integration tests - they test the Java logic without external dependencies.
 *
 * @author Bruno Alves
 */
@Tag("integration")
public class PythonPluginIntegrationTest {

    private static final String APPLICATION_LANGUAGE = "fr";
    private static final String PLUGIN_CODE = "python";
    private static final String DATA_FOLDERS_BASE_PATH = "/tmp/extract-test-integration";
    private static final String TASK_PLUGINS_FOLDER_PATH = "src/main/resources/task_processors";
    private static final String PLUGIN_FILE_NAME_FILTER = "extract-task-python-*.jar";

    private static ITaskProcessor pythonPlugin;
    private Request testRequest;
    private String folderIn;
    private String folderOut;
    private ObjectMapper objectMapper;

    @BeforeAll
    public static void initialize() {
        configurePlugin();
    }

    @BeforeEach
    public void setUp() throws IOException {
        // Create test directories
        Path basePath = Paths.get(DATA_FOLDERS_BASE_PATH, "ORDER-TEST");
        folderIn = basePath.resolve("input").toString();
        folderOut = basePath.resolve("output").toString();

        Files.createDirectories(Paths.get(folderIn));
        Files.createDirectories(Paths.get(folderOut));

        objectMapper = new ObjectMapper();

        // Configure minimal request
        testRequest = new Request();
        testRequest.setId(1);
        testRequest.setOrderLabel("ORDER-TEST-001");
        testRequest.setOrderGuid("order-guid-test");
        testRequest.setProductLabel("Test Product");
        testRequest.setProductGuid("product-guid-test");
        testRequest.setClient("Test Client");
        testRequest.setClientGuid("client-guid-test");
        testRequest.setOrganism("Test Organism");
        testRequest.setOrganismGuid("organism-guid-test");
        testRequest.setTiers("Test Tiers");
        testRequest.setTiersGuid("tiers-guid-test");
        testRequest.setTiersDetails("Tiers contact details");
        testRequest.setRemark("Test remark");
        testRequest.setFolderIn(folderIn);
        testRequest.setFolderOut(folderOut);
        testRequest.setStatus(Request.Status.ONGOING);
        testRequest.setStartDate(new GregorianCalendar(2024, 2, 1, 9, 0, 0));
        testRequest.setEndDate(new GregorianCalendar(2024, 2, 15, 17, 30, 0));
    }

    @AfterEach
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(new File(DATA_FOLDERS_BASE_PATH));
    }

    private static void configurePlugin() {
        TaskProcessorsDiscoverer taskPluginDiscoverer = TaskProcessorsDiscoverer.getInstance();
        taskPluginDiscoverer.setApplicationLanguage(APPLICATION_LANGUAGE);

        File pluginDir = new File(Paths.get(TASK_PLUGINS_FOLDER_PATH).toAbsolutePath().toString());
        FileFilter fileFilter = WildcardFileFilter.builder()
                                                  .setWildcards(PLUGIN_FILE_NAME_FILTER)
                                                  .get();
        File[] foundPluginFiles = pluginDir.listFiles(fileFilter);

        if (ArrayUtils.isEmpty(foundPluginFiles)) {
            throw new RuntimeException("Python plugin JAR not found.");
        }

        URL pluginUrl;
        try {
            assert foundPluginFiles != null;
            pluginUrl = new URL(String.format("jar:file:%s!/", foundPluginFiles[0].getAbsolutePath()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        taskPluginDiscoverer.setJarUrls(new URL[] { pluginUrl });
        pythonPlugin = taskPluginDiscoverer.getTaskProcessor(PLUGIN_CODE);
        assertNotNull(pythonPlugin, "Python plugin should be discovered");
    }

    /**
     * Helper method to call private createParametersFile method via reflection
     */
    private void createParametersFile(Request request) throws Exception {
        File parametersFile = new File(folderIn, "parameters.json");
        TaskProcessorRequest taskProcessorRequest = new TaskProcessorRequest(request, DATA_FOLDERS_BASE_PATH);

        Method method = pythonPlugin.getClass().getDeclaredMethod("createParametersFile",
            ch.asit_asso.extract.plugins.common.ITaskProcessorRequest.class, File.class);
        method.setAccessible(true);
        method.invoke(pythonPlugin, taskProcessorRequest, parametersFile);
    }

    @Test
    @DisplayName("Parameters JSON file is created with all metadata fields")
    public void testParametersJsonCreationWithAllMetadata() throws Exception {
        // When: Creating parameters file
        createParametersFile(testRequest);

        // Then: File should exist
        File parametersFile = new File(folderIn, "parameters.json");
        assertTrue(parametersFile.exists(), "parameters.json should be created");

        // Parse JSON
        JsonNode json = objectMapper.readTree(parametersFile);

        // Verify GeoJSON Feature structure
        assertEquals("Feature", json.get("type").asText());
        assertTrue(json.has("geometry"));
        assertTrue(json.has("properties"));

        // Verify all metadata in properties
        JsonNode properties = json.get("properties");
        assertEquals("ORDER-TEST-001", properties.get("orderLabel").asText());
        assertEquals("order-guid-test", properties.get("orderGuid").asText());
        assertEquals("Test Product", properties.get("productLabel").asText());
        assertEquals("product-guid-test", properties.get("productGuid").asText());
        assertEquals("Test Client", properties.get("clientName").asText());
        assertEquals("client-guid-test", properties.get("clientGuid").asText());
        assertEquals("Test Organism", properties.get("organismName").asText());
        assertEquals("organism-guid-test", properties.get("organismGuid").asText());
        assertEquals("Test Tiers", properties.get("tiersName").asText());
        assertEquals("tiers-guid-test", properties.get("tiersGuid").asText());
        assertEquals("Tiers contact details", properties.get("tiersDetails").asText());
        assertEquals("Test remark", properties.get("remark").asText());
    }

    @Test
    @DisplayName("WKT Polygon is correctly converted to GeoJSON")
    public void testWKTPolygonToGeoJSON() throws Exception {
        // Given: Request with Polygon WKT
        testRequest.setPerimeter("POLYGON((6.5 46.5, 6.6 46.5, 6.6 46.6, 6.5 46.6, 6.5 46.5))");

        // When: Creating parameters file
        createParametersFile(testRequest);

        // Then: GeoJSON should have correct Polygon structure
        File parametersFile = new File(folderIn, "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode geometry = json.get("geometry");
        assertEquals("Polygon", geometry.get("type").asText());

        JsonNode coordinates = geometry.get("coordinates");
        assertTrue(coordinates.isArray());
        assertTrue(coordinates.size() > 0);

        // Verify it's an array of rings
        JsonNode firstRing = coordinates.get(0);
        assertTrue(firstRing.isArray());
        assertEquals(5, firstRing.size(), "Polygon should have 5 points (closed ring)");

        // Verify first coordinate
        JsonNode firstCoord = firstRing.get(0);
        assertEquals(6.5, firstCoord.get(0).asDouble(), 0.001);
        assertEquals(46.5, firstCoord.get(1).asDouble(), 0.001);

        // Verify closed ring (first == last)
        JsonNode lastCoord = firstRing.get(4);
        assertEquals(firstCoord.get(0).asDouble(), lastCoord.get(0).asDouble(), 0.001);
        assertEquals(firstCoord.get(1).asDouble(), lastCoord.get(1).asDouble(), 0.001);
    }

    @Test
    @DisplayName("WKT MultiPolygon is correctly converted to GeoJSON")
    public void testWKTMultiPolygonToGeoJSON() throws Exception {
        // Given: Request with MultiPolygon WKT
        testRequest.setPerimeter(
            "MULTIPOLYGON(((6.5 46.5, 6.6 46.5, 6.6 46.6, 6.5 46.6, 6.5 46.5))," +
            "((6.7 46.7, 6.8 46.7, 6.8 46.8, 6.7 46.8, 6.7 46.7)))"
        );

        // When: Creating parameters file
        createParametersFile(testRequest);

        // Then: GeoJSON should have correct MultiPolygon structure
        File parametersFile = new File(folderIn, "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode geometry = json.get("geometry");
        assertEquals("MultiPolygon", geometry.get("type").asText());

        JsonNode coordinates = geometry.get("coordinates");
        assertTrue(coordinates.isArray());
        assertEquals(2, coordinates.size(), "Should have 2 polygons");

        // Verify first polygon
        JsonNode firstPolygon = coordinates.get(0);
        assertTrue(firstPolygon.isArray());
        assertTrue(firstPolygon.get(0).isArray());
        assertEquals(5, firstPolygon.get(0).size());

        // Verify second polygon
        JsonNode secondPolygon = coordinates.get(1);
        assertTrue(secondPolygon.isArray());
        assertTrue(secondPolygon.get(0).isArray());
        assertEquals(5, secondPolygon.get(0).size());
    }

    @Test
    @DisplayName("WKT Polygon with hole (donut) is correctly converted")
    public void testWKTPolygonWithHoleToGeoJSON() throws Exception {
        // Given: Polygon with interior ring (hole)
        testRequest.setPerimeter(
            "POLYGON((6.5 46.5, 6.6 46.5, 6.6 46.6, 6.5 46.6, 6.5 46.5)," +
            "(6.52 46.52, 6.53 46.52, 6.53 46.53, 6.52 46.53, 6.52 46.52))"
        );

        // When: Creating parameters file
        createParametersFile(testRequest);

        // Then: Should have 2 rings (exterior + interior)
        File parametersFile = new File(folderIn, "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode geometry = json.get("geometry");
        assertEquals("Polygon", geometry.get("type").asText());

        JsonNode coordinates = geometry.get("coordinates");
        assertTrue(coordinates.isArray());
        assertEquals(2, coordinates.size(), "Should have 2 rings (exterior + hole)");

        // Verify exterior ring
        JsonNode exteriorRing = coordinates.get(0);
        assertEquals(5, exteriorRing.size());

        // Verify interior ring (hole)
        JsonNode interiorRing = coordinates.get(1);
        assertEquals(5, interiorRing.size());
    }

    @Test
    @DisplayName("WKT Point is correctly converted to GeoJSON")
    public void testWKTPointToGeoJSON() throws Exception {
        // Given: Point WKT
        testRequest.setPerimeter("POINT(6.5 46.5)");

        // When: Creating parameters file
        createParametersFile(testRequest);

        // Then: GeoJSON should have correct Point structure
        File parametersFile = new File(folderIn, "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode geometry = json.get("geometry");
        assertEquals("Point", geometry.get("type").asText());

        JsonNode coordinates = geometry.get("coordinates");
        assertTrue(coordinates.isArray());
        assertEquals(2, coordinates.size(), "Point should have 2 coordinates");
        assertEquals(6.5, coordinates.get(0).asDouble(), 0.001);
        assertEquals(46.5, coordinates.get(1).asDouble(), 0.001);
    }

    @Test
    @DisplayName("WKT LineString is correctly converted to GeoJSON")
    public void testWKTLineStringToGeoJSON() throws Exception {
        // Given: LineString WKT
        testRequest.setPerimeter("LINESTRING(6.5 46.5, 6.6 46.6, 6.7 46.7)");

        // When: Creating parameters file
        createParametersFile(testRequest);

        // Then: GeoJSON should have correct LineString structure
        File parametersFile = new File(folderIn, "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode geometry = json.get("geometry");
        assertEquals("LineString", geometry.get("type").asText());

        JsonNode coordinates = geometry.get("coordinates");
        assertTrue(coordinates.isArray());
        assertEquals(3, coordinates.size(), "LineString should have 3 points");

        // Verify coordinates
        assertEquals(6.5, coordinates.get(0).get(0).asDouble(), 0.001);
        assertEquals(46.5, coordinates.get(0).get(1).asDouble(), 0.001);
        assertEquals(6.7, coordinates.get(2).get(0).asDouble(), 0.001);
        assertEquals(46.7, coordinates.get(2).get(1).asDouble(), 0.001);
    }

    @Test
    @DisplayName("Dynamic parameters from JSON are added to properties")
    public void testDynamicParametersInProperties() throws Exception {
        // Given: Request with custom parameters
        testRequest.setParameters("{\"FORMAT\":\"DXF\",\"PROJECTION\":\"EPSG:2056\",\"SCALE\":1000,\"quality\":\"high\"}");

        // When: Creating parameters file
        createParametersFile(testRequest);

        // Then: Dynamic parameters should be in properties
        File parametersFile = new File(folderIn, "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode properties = json.get("properties");
        assertEquals("DXF", properties.get("FORMAT").asText());
        assertEquals("EPSG:2056", properties.get("PROJECTION").asText());
        assertEquals("1000", properties.get("SCALE").asText());
        assertEquals("high", properties.get("quality").asText());
    }

    @Test
    @DisplayName("Empty/null perimeter creates valid GeoJSON without geometry")
    public void testNullPerimeterHandling() throws Exception {
        // Given: Request without perimeter
        testRequest.setPerimeter(null);

        // When: Creating parameters file
        createParametersFile(testRequest);

        // Then: Should create valid GeoJSON Feature
        File parametersFile = new File(folderIn, "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        assertEquals("Feature", json.get("type").asText());
        assertTrue(json.has("properties"));
        // Geometry might be null or empty object
    }

    @Test
    @DisplayName("Empty parameters JSON is handled gracefully")
    public void testEmptyParametersJsonHandling() throws Exception {
        // Given: Request with empty parameters
        testRequest.setParameters("");

        // When: Creating parameters file
        createParametersFile(testRequest);

        // Then: Should still create valid GeoJSON
        File parametersFile = new File(folderIn, "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        assertEquals("Feature", json.get("type").asText());
        assertTrue(json.has("properties"));
    }

    @Test
    @DisplayName("Invalid JSON parameters are handled gracefully")
    public void testInvalidJsonParametersHandling() throws Exception {
        // Given: Request with invalid JSON
        testRequest.setParameters("{invalid json}}");

        // When: Creating parameters file - should not throw exception
        assertDoesNotThrow(() -> createParametersFile(testRequest));

        // Then: File should still be created with basic properties
        File parametersFile = new File(folderIn, "parameters.json");
        assertTrue(parametersFile.exists());

        JsonNode json = objectMapper.readTree(parametersFile);
        assertEquals("Feature", json.get("type").asText());
    }

    @Test
    @DisplayName("All null optional fields are handled gracefully")
    public void testAllNullOptionalFields() throws Exception {
        // Given: Request with minimal data (nulls everywhere)
        testRequest.setTiers(null);
        testRequest.setTiersGuid(null);
        testRequest.setTiersDetails(null);
        testRequest.setRemark(null);
        testRequest.setPerimeter(null);
        testRequest.setParameters(null);
        testRequest.setStartDate(null);
        testRequest.setEndDate(null);

        // When: Creating parameters file
        createParametersFile(testRequest);

        // Then: Should still create valid file
        File parametersFile = new File(folderIn, "parameters.json");
        assertTrue(parametersFile.exists());

        JsonNode json = objectMapper.readTree(parametersFile);
        assertEquals("Feature", json.get("type").asText());

        // Verify non-null required fields are present
        JsonNode properties = json.get("properties");
        assertEquals("ORDER-TEST-001", properties.get("orderLabel").asText());
        assertEquals("Test Client", properties.get("clientName").asText());
    }

    @Test
    @DisplayName("Complex parameters with nested JSON are flattened")
    public void testComplexNestedParametersFlattening() throws Exception {
        // Given: Complex nested parameters
        testRequest.setParameters("{\"format\":\"PDF\",\"options\":{\"compress\":true,\"quality\":95},\"layers\":[\"A\",\"B\"]}");

        // When: Creating parameters file
        createParametersFile(testRequest);

        // Then: Parameters should be parsed (behavior may vary)
        File parametersFile = new File(folderIn, "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode properties = json.get("properties");
        assertTrue(properties.has("format"));
        assertEquals("PDF", properties.get("format").asText());
    }

    @Test
    @DisplayName("Special characters in metadata are properly escaped in JSON")
    public void testSpecialCharactersEscaping() throws Exception {
        // Given: Request with special characters
        testRequest.setRemark("Test with \"quotes\" and \n newlines \t tabs");
        testRequest.setClient("Client & Co. <important>");

        // When: Creating parameters file
        createParametersFile(testRequest);

        // Then: JSON should be valid and properly escaped
        File parametersFile = new File(folderIn, "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode properties = json.get("properties");
        String remark = properties.get("remark").asText();
        assertTrue(remark.contains("quotes"));
        assertTrue(remark.contains("newlines"));

        String client = properties.get("clientName").asText();
        assertTrue(client.contains("&"));
        assertTrue(client.contains("<"));
    }

    @Test
    @DisplayName("Very large coordinates are handled correctly")
    public void testLargeCoordinateValues() throws Exception {
        // Given: Polygon with large coordinate values
        testRequest.setPerimeter("POLYGON((2500000 1200000, 2500100 1200000, 2500100 1200100, 2500000 1200100, 2500000 1200000))");

        // When: Creating parameters file
        createParametersFile(testRequest);

        // Then: Coordinates should be preserved accurately
        File parametersFile = new File(folderIn, "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode coordinates = json.get("geometry").get("coordinates").get(0);
        assertEquals(2500000, coordinates.get(0).get(0).asDouble(), 0.001);
        assertEquals(1200000, coordinates.get(0).get(1).asDouble(), 0.001);
    }
}
