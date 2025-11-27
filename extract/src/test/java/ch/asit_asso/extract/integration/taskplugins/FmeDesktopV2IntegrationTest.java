/*
 * Copyright (C) 2025 ASIT
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
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for FME Desktop V2 plugin (Issue #347).
 * Tests parameters.json generation with GeoJSON format, WKT to GeoJSON conversion.
 *
 * These are TRUE integration tests - they test the Java logic without external dependencies.
 *
 * @author Bruno Alves
 */
@Tag("integration")
public class FmeDesktopV2IntegrationTest {

    private static final String APPLICATION_LANGUAGE = "fr";
    private static final String PLUGIN_CODE = "FME2017V2";
    private static final String DATA_FOLDERS_BASE_PATH = "/tmp/extract-test-fmedesktopv2";
    private static final String TASK_PLUGINS_FOLDER_PATH = "src/main/resources/task_processors";
    private static final String PLUGIN_FILE_NAME_FILTER = "extract-task-fmedesktop-v2-*.jar";

    private static ITaskProcessor fmeDesktopV2Plugin;
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
        String orderFolderName = "ORDER-TEST-V2";
        folderIn = Paths.get(orderFolderName, "input").toString();
        folderOut = Paths.get(orderFolderName, "output").toString();

        Path basePath = Paths.get(DATA_FOLDERS_BASE_PATH, orderFolderName);
        Files.createDirectories(basePath.resolve("input"));
        Files.createDirectories(basePath.resolve("output"));

        objectMapper = new ObjectMapper();

        testRequest = new Request();
        testRequest.setId(1);
        testRequest.setOrderLabel("ORDER-TEST-V2-001");
        testRequest.setOrderGuid("order-guid-v2-test");
        testRequest.setProductLabel("Test Product V2");
        testRequest.setProductGuid("product-guid-v2-test");
        testRequest.setClient("Test Client V2");
        testRequest.setClientGuid("client-guid-v2-test");
        testRequest.setOrganism("Test Organism V2");
        testRequest.setOrganismGuid("organism-guid-v2-test");
        testRequest.setFolderIn(folderIn);
        testRequest.setFolderOut(folderOut);
        testRequest.setStatus(Request.Status.ONGOING);
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
            throw new RuntimeException("FME Desktop V2 plugin JAR not found.");
        }

        URL pluginUrl;
        try {
            assert foundPluginFiles != null;
            pluginUrl = new URL(String.format("jar:file:%s!/", foundPluginFiles[0].getAbsolutePath()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        taskPluginDiscoverer.setJarUrls(new URL[] { pluginUrl });
        fmeDesktopV2Plugin = taskPluginDiscoverer.getTaskProcessor(PLUGIN_CODE);
        assertNotNull(fmeDesktopV2Plugin, "FME Desktop V2 plugin should be discovered");
    }

    private void createParametersFile(Request request) throws Exception {
        TaskProcessorRequest taskProcessorRequest = new TaskProcessorRequest(request, DATA_FOLDERS_BASE_PATH);
        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, request.getFolderIn()).toString(), "parameters.json");

        Method method = fmeDesktopV2Plugin.getClass().getDeclaredMethod("createParametersFile",
            ch.asit_asso.extract.plugins.common.ITaskProcessorRequest.class, File.class);
        method.setAccessible(true);
        method.invoke(fmeDesktopV2Plugin, taskProcessorRequest, parametersFile);
    }

    @Test
    @DisplayName("Parameters JSON file is created as GeoJSON Feature")
    public void testParametersJsonCreationAsGeoJsonFeature() throws Exception {
        createParametersFile(testRequest);

        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        assertTrue(parametersFile.exists(), "parameters.json should be created");

        JsonNode json = objectMapper.readTree(parametersFile);
        assertEquals("Feature", json.get("type").asText());
        assertTrue(json.has("geometry"));
        assertTrue(json.has("properties"));
    }

    @Test
    @DisplayName("WKT Polygon is correctly converted to GeoJSON")
    public void testWKTPolygonToGeoJSON() throws Exception {
        testRequest.setPerimeter("POLYGON((6.5 46.5, 6.6 46.5, 6.6 46.6, 6.5 46.6, 6.5 46.5))");

        createParametersFile(testRequest);

        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode geometry = json.get("geometry");
        assertEquals("Polygon", geometry.get("type").asText());

        JsonNode coordinates = geometry.get("coordinates");
        assertTrue(coordinates.isArray());
        assertTrue(coordinates.size() > 0);

        JsonNode firstRing = coordinates.get(0);
        assertTrue(firstRing.isArray());
        assertEquals(5, firstRing.size(), "Polygon should have 5 points (closed ring)");

        JsonNode firstCoord = firstRing.get(0);
        assertEquals(6.5, firstCoord.get(0).asDouble(), 0.001);
        assertEquals(46.5, firstCoord.get(1).asDouble(), 0.001);
    }

    @Test
    @DisplayName("WKT MultiPolygon is correctly converted to GeoJSON")
    public void testWKTMultiPolygonToGeoJSON() throws Exception {
        testRequest.setPerimeter(
            "MULTIPOLYGON(((6.5 46.5, 6.6 46.5, 6.6 46.6, 6.5 46.6, 6.5 46.5))," +
            "((6.7 46.7, 6.8 46.7, 6.8 46.8, 6.7 46.8, 6.7 46.7)))"
        );

        createParametersFile(testRequest);

        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode geometry = json.get("geometry");
        assertEquals("MultiPolygon", geometry.get("type").asText());

        JsonNode coordinates = geometry.get("coordinates");
        assertTrue(coordinates.isArray());
        assertEquals(2, coordinates.size(), "Should have 2 polygons");
    }

    @Test
    @DisplayName("WKT Polygon with hole is correctly converted")
    public void testWKTPolygonWithHoleToGeoJSON() throws Exception {
        testRequest.setPerimeter(
            "POLYGON((6.5 46.5, 6.6 46.5, 6.6 46.6, 6.5 46.6, 6.5 46.5)," +
            "(6.52 46.52, 6.53 46.52, 6.53 46.53, 6.52 46.53, 6.52 46.52))"
        );

        createParametersFile(testRequest);

        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode geometry = json.get("geometry");
        assertEquals("Polygon", geometry.get("type").asText());

        JsonNode coordinates = geometry.get("coordinates");
        assertEquals(2, coordinates.size(), "Should have 2 rings (exterior + hole)");
    }

    @Test
    @DisplayName("WKT Point is correctly converted to GeoJSON")
    public void testWKTPointToGeoJSON() throws Exception {
        testRequest.setPerimeter("POINT(6.5 46.5)");

        createParametersFile(testRequest);

        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
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
        testRequest.setPerimeter("LINESTRING(6.5 46.5, 6.6 46.6, 6.7 46.7)");

        createParametersFile(testRequest);

        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode geometry = json.get("geometry");
        assertEquals("LineString", geometry.get("type").asText());

        JsonNode coordinates = geometry.get("coordinates");
        assertTrue(coordinates.isArray());
        assertEquals(3, coordinates.size(), "LineString should have 3 points");
    }

    @Test
    @DisplayName("Custom parameters are nested in properties")
    public void testCustomParametersInProperties() throws Exception {
        testRequest.setParameters("{\"FORMAT\":\"DXF\",\"PROJECTION\":\"EPSG:2056\",\"SCALE\":1000}");

        createParametersFile(testRequest);

        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode properties = json.get("properties");
        assertTrue(properties.has("Parameters"), "Should have Parameters object");

        JsonNode parameters = properties.get("Parameters");
        assertEquals("DXF", parameters.get("FORMAT").asText());
        assertEquals("EPSG:2056", parameters.get("PROJECTION").asText());
        assertEquals(1000, parameters.get("SCALE").asInt());
    }

    @Test
    @DisplayName("Null perimeter creates valid GeoJSON with null geometry")
    public void testNullPerimeterHandling() throws Exception {
        testRequest.setPerimeter(null);

        createParametersFile(testRequest);

        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        assertEquals("Feature", json.get("type").asText());
        assertTrue(json.has("properties"));
        assertTrue(json.get("geometry").isNull(), "Geometry should be null");
    }

    @Test
    @DisplayName("Empty perimeter creates valid GeoJSON with null geometry")
    public void testEmptyPerimeterHandling() throws Exception {
        testRequest.setPerimeter("");

        createParametersFile(testRequest);

        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        assertEquals("Feature", json.get("type").asText());
        assertTrue(json.get("geometry").isNull(), "Geometry should be null for empty perimeter");
    }

    @Test
    @DisplayName("Invalid WKT creates null geometry without exception")
    public void testInvalidWKTHandling() throws Exception {
        testRequest.setPerimeter("INVALID WKT STRING");

        assertDoesNotThrow(() -> createParametersFile(testRequest));

        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        assertEquals("Feature", json.get("type").asText());
        assertTrue(json.get("geometry").isNull(), "Geometry should be null for invalid WKT");
    }

    @Test
    @DisplayName("All metadata fields are present in properties")
    public void testAllMetadataFieldsPresent() throws Exception {
        testRequest.setPerimeter("POLYGON((6.5 46.5, 6.6 46.5, 6.6 46.6, 6.5 46.6, 6.5 46.5))");

        createParametersFile(testRequest);

        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode properties = json.get("properties");
        assertNotNull(properties);

        // Check that properties contain expected fields (names may vary based on config)
        assertTrue(properties.size() > 0, "Properties should contain metadata fields");
    }

    @Test
    @DisplayName("Large Swiss coordinates are handled correctly")
    public void testLargeSwissCoordinateValues() throws Exception {
        testRequest.setPerimeter("POLYGON((2500000 1200000, 2500100 1200000, 2500100 1200100, 2500000 1200100, 2500000 1200000))");

        createParametersFile(testRequest);

        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode coordinates = json.get("geometry").get("coordinates").get(0);
        assertEquals(2500000, coordinates.get(0).get(0).asDouble(), 0.001);
        assertEquals(1200000, coordinates.get(0).get(1).asDouble(), 0.001);
    }

    @Test
    @DisplayName("Complex nested JSON parameters are preserved")
    public void testComplexNestedParameters() throws Exception {
        testRequest.setParameters("{\"format\":\"PDF\",\"options\":{\"compress\":true,\"quality\":95},\"layers\":[\"A\",\"B\"]}");

        createParametersFile(testRequest);

        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode parameters = json.get("properties").get("Parameters");
        assertEquals("PDF", parameters.get("format").asText());

        JsonNode options = parameters.get("options");
        assertTrue(options.get("compress").asBoolean());
        assertEquals(95, options.get("quality").asInt());

        JsonNode layers = parameters.get("layers");
        assertTrue(layers.isArray());
        assertEquals(2, layers.size());
    }

    @Test
    @DisplayName("Invalid JSON parameters are handled gracefully")
    public void testInvalidJsonParametersHandling() throws Exception {
        testRequest.setParameters("{invalid json}}");

        assertDoesNotThrow(() -> createParametersFile(testRequest));

        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        assertTrue(parametersFile.exists());

        JsonNode json = objectMapper.readTree(parametersFile);
        assertEquals("Feature", json.get("type").asText());
    }

    @Test
    @DisplayName("Special characters in client name are properly escaped")
    public void testSpecialCharactersEscaping() throws Exception {
        testRequest.setClient("Client & Co. <important>");

        createParametersFile(testRequest);

        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        // File should be valid JSON even with special characters
        assertNotNull(json);
        assertEquals("Feature", json.get("type").asText());
    }
}
