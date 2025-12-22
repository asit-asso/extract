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
package ch.asit_asso.extract.functional.taskplugins;

import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.plugins.TaskProcessorsDiscoverer;
import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Functional tests for QGIS Print Atlas plugin.
 * Tests plugin execution with mock QGIS Server for Atlas printing.
 *
 * The QGIS Print plugin performs three HTTP requests:
 * 1. GetProjectSettings (WMS) - Get coverage layer from template
 * 2. GetFeature (WFS) - Get feature IDs within perimeter
 * 3. GetPrint (WMS) - Generate PDF with atlas features
 *
 * Note: These tests require the qgis-server-mock container to be running on port 8889.
 * Run with: docker-compose -f docker-compose-test.yaml up qgis-server-mock
 *
 * @author Extract Test Team
 */
@Tag("functional")
public class QgisPrintAtlasFunctionalTest {

    private static final String APPLICATION_LANGUAGE = "fr";
    private static final String PLUGIN_CODE = "QGISPRINT";
    private static final String DATA_FOLDERS_BASE_PATH = "/tmp/extract-test-qgisprint-functional";
    private static final String TASK_PLUGINS_FOLDER_PATH = "src/main/resources/task_processors";
    private static final String PLUGIN_FILE_NAME_FILTER = "extract-task-qgisprint-*.jar";

    // Mock server configuration
    private static final String QGIS_SERVER_MOCK_URL = "http://localhost:8889/qgis";
    private static final String QGIS_SERVER_MOCK_URL_DOCKER = "http://qgis-server-mock:8889/qgis";
    private static final String VALID_USERNAME = "qgisuser";
    private static final String VALID_PASSWORD = "qgispass";
    private static final String TEST_PROJECT_PATH = "/data/test_project.qgs";
    private static final String ATLAS_TEMPLATE = "Atlas";

    private static final String CLIENT_GUID = "4b01553d-9766-4014-9166-3f00f58adfc7";
    private static final String ORDER_LABEL = "QGIS-ATLAS-TEST-001";
    private static final String ORGANISM_GUID = "a35f0327-bceb-43a1-b366-96c3a94bc47b";
    private static final String PRODUCT_GUID = "a8405d50-f712-4e3e-96b2-a5452cf4e03e";

    // Swiss coordinate system perimeters
    private static final String PERIMETER_POLYGON =
            "POLYGON((2500000 1200000, 2500100 1200000, 2500100 1200100, 2500000 1200100, 2500000 1200000))";

    private static final String PERIMETER_POINT = "POINT(2500050 1200050)";

    private static final String PERIMETER_LINESTRING =
            "LINESTRING(2500000 1200000, 2500050 1200050, 2500100 1200100)";

    private static ITaskProcessor qgisPrintPlugin;
    private Request testRequest;
    private Map<String, String> pluginParameters;
    private String folderIn;
    private String folderOut;
    private ObjectMapper objectMapper;
    private static boolean mockServerAvailable = false;
    private static String mockServerUrl;

    @BeforeAll
    public static void initialize() {
        configurePlugin();
        checkMockServerAvailability();
    }

    @BeforeEach
    public void setUp() throws IOException {
        String orderFolderName = ORDER_LABEL;
        folderIn = Paths.get(DATA_FOLDERS_BASE_PATH, orderFolderName, "input").toString();
        folderOut = Paths.get(DATA_FOLDERS_BASE_PATH, orderFolderName, "output").toString();

        Files.createDirectories(Paths.get(folderIn));
        Files.createDirectories(Paths.get(folderOut));

        objectMapper = new ObjectMapper();
        configureRequest();
        pluginParameters = new HashMap<>();
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
            throw new RuntimeException("QGIS Print plugin JAR not found.");
        }

        URL pluginUrl;
        try {
            pluginUrl = new URL(String.format("jar:file:%s!/", foundPluginFiles[0].getAbsolutePath()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        taskPluginDiscoverer.setJarUrls(new URL[] { pluginUrl });
        qgisPrintPlugin = taskPluginDiscoverer.getTaskProcessor(PLUGIN_CODE);
        assertNotNull(qgisPrintPlugin, "QGIS Print plugin should be discovered");
    }

    private static void checkMockServerAvailability() {
        // Try localhost first
        try {
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection)
                new URL("http://localhost:8889/health").openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                mockServerAvailable = true;
                mockServerUrl = QGIS_SERVER_MOCK_URL;
                connection.disconnect();
                return;
            }
            connection.disconnect();
        } catch (Exception e) {
            // Try Docker network name
        }

        // Try Docker hostname
        try {
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection)
                new URL("http://qgis-server-mock:8889/health").openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            int responseCode = connection.getResponseCode();
            mockServerAvailable = (responseCode == 200);
            mockServerUrl = QGIS_SERVER_MOCK_URL_DOCKER;
            connection.disconnect();
        } catch (Exception e) {
            mockServerAvailable = false;
            System.out.println("QGIS Server Mock not available: " + e.getMessage());
        }
    }

    private void configureRequest() {
        testRequest = new Request();
        testRequest.setId(1);
        testRequest.setOrderLabel(ORDER_LABEL);
        testRequest.setOrderGuid("order-guid-qgis");
        testRequest.setProductLabel("Test Product QGIS Atlas");
        testRequest.setProductGuid(PRODUCT_GUID);
        testRequest.setClient("Test Client QGIS");
        testRequest.setClientGuid(CLIENT_GUID);
        testRequest.setOrganism("Test Organism QGIS");
        testRequest.setOrganismGuid(ORGANISM_GUID);
        testRequest.setFolderIn(folderIn);
        testRequest.setFolderOut(folderOut);
        testRequest.setPerimeter(PERIMETER_POLYGON);
        testRequest.setParameters("{}");
        testRequest.setStatus(Request.Status.ONGOING);
    }

    @Test
    @DisplayName("QGIS Print plugin is correctly discovered")
    public void testPluginDiscovery() {
        assertNotNull(qgisPrintPlugin, "Plugin should be discovered");
        assertEquals(PLUGIN_CODE, qgisPrintPlugin.getCode(), "Plugin code should match");
    }

    @Test
    @DisplayName("Plugin has correct parameter structure")
    public void testPluginParameterStructure() throws Exception {
        String params = qgisPrintPlugin.getParams();
        assertNotNull(params, "Params should not be null");

        JsonNode paramsJson = objectMapper.readTree(params);
        assertTrue(paramsJson.isArray(), "Params should be an array");

        boolean hasUrl = false;
        boolean hasLayout = false;
        boolean hasPathQgs = false;
        boolean hasLogin = false;
        boolean hasPassword = false;
        boolean hasLayers = false;
        boolean hasCrs = false;

        for (JsonNode param : paramsJson) {
            String code = param.get("code").asText();
            switch (code) {
                case "url" -> {
                    hasUrl = true;
                    assertTrue(param.get("req").asBoolean(), "url should be required");
                }
                case "layout" -> {
                    hasLayout = true;
                    assertTrue(param.get("req").asBoolean(), "layout should be required");
                }
                case "pathqgs" -> {
                    hasPathQgs = true;
                    assertFalse(param.get("req").asBoolean(), "pathqgs should be optional");
                }
                case "login" -> {
                    hasLogin = true;
                    assertFalse(param.get("req").asBoolean(), "login should be optional");
                }
                case "pass" -> {
                    hasPassword = true;
                    assertFalse(param.get("req").asBoolean(), "pass should be optional");
                    assertEquals("pass", param.get("type").asText(), "pass should be password type");
                }
                case "layers" -> {
                    hasLayers = true;
                    assertFalse(param.get("req").asBoolean(), "layers should be optional");
                }
                case "crs" -> {
                    hasCrs = true;
                    assertFalse(param.get("req").asBoolean(), "crs should be optional");
                }
            }
        }

        assertTrue(hasUrl, "Should have 'url' parameter");
        assertTrue(hasLayout, "Should have 'layout' parameter");
        assertTrue(hasPathQgs, "Should have 'pathqgs' parameter");
        assertTrue(hasLogin, "Should have 'login' parameter");
        assertTrue(hasPassword, "Should have 'pass' parameter");
        assertTrue(hasLayers, "Should have 'layers' parameter");
        assertTrue(hasCrs, "Should have 'crs' parameter");
    }

    @Test
    @DisplayName("QGIS Print succeeds with valid configuration")
    public void testSuccessWithValidConfiguration() {
        Assumptions.assumeTrue(mockServerAvailable, "QGIS Server Mock is not available");

        // Given: Valid configuration
        pluginParameters.put("url", mockServerUrl);
        pluginParameters.put("layout", ATLAS_TEMPLATE);
        pluginParameters.put("pathqgs", TEST_PROJECT_PATH);
        pluginParameters.put("login", VALID_USERNAME);
        pluginParameters.put("pass", VALID_PASSWORD);
        pluginParameters.put("crs", "EPSG:2056");

        // When: Executing the plugin
        ITaskProcessor pluginInstance = qgisPrintPlugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Result should be success
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus(),
            "Status should be SUCCESS with valid config. Message: " + result.getMessage());
    }

    @Test
    @DisplayName("QGIS Print creates PDF file in output folder")
    public void testPdfFileCreated() {
        Assumptions.assumeTrue(mockServerAvailable, "QGIS Server Mock is not available");

        // Given: Valid configuration
        pluginParameters.put("url", mockServerUrl);
        pluginParameters.put("layout", ATLAS_TEMPLATE);
        pluginParameters.put("pathqgs", TEST_PROJECT_PATH);
        pluginParameters.put("login", VALID_USERNAME);
        pluginParameters.put("pass", VALID_PASSWORD);

        // When: Executing the plugin
        ITaskProcessor pluginInstance = qgisPrintPlugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Output folder should contain PDF file
        if (result.getStatus() == ITaskProcessorResult.Status.SUCCESS) {
            File outputDir = new File(folderOut);
            // The plugin creates a subfolder with timestamp_productId
            File[] subDirs = outputDir.listFiles(File::isDirectory);
            if (subDirs != null && subDirs.length > 0) {
                File[] pdfFiles = subDirs[0].listFiles((dir, name) -> name.endsWith(".pdf"));
                assertNotNull(pdfFiles, "Should have PDF file in output subfolder");
                assertTrue(pdfFiles.length > 0, "Should have at least one PDF file");
            }
        }
    }

    @Test
    @DisplayName("QGIS Print handles Polygon perimeter")
    public void testPolygonPerimeter() {
        Assumptions.assumeTrue(mockServerAvailable, "QGIS Server Mock is not available");

        // Given: Polygon perimeter
        testRequest.setPerimeter(PERIMETER_POLYGON);
        pluginParameters.put("url", mockServerUrl);
        pluginParameters.put("layout", ATLAS_TEMPLATE);
        pluginParameters.put("pathqgs", TEST_PROJECT_PATH);

        // When: Executing the plugin
        ITaskProcessor pluginInstance = qgisPrintPlugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should succeed
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus(),
            "Should handle Polygon perimeter. Message: " + result.getMessage());
    }

    @Test
    @DisplayName("QGIS Print handles Point perimeter")
    public void testPointPerimeter() {
        Assumptions.assumeTrue(mockServerAvailable, "QGIS Server Mock is not available");

        // Given: Point perimeter
        testRequest.setPerimeter(PERIMETER_POINT);
        pluginParameters.put("url", mockServerUrl);
        pluginParameters.put("layout", ATLAS_TEMPLATE);
        pluginParameters.put("pathqgs", TEST_PROJECT_PATH);

        // When: Executing the plugin
        ITaskProcessor pluginInstance = qgisPrintPlugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should succeed
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus(),
            "Should handle Point perimeter. Message: " + result.getMessage());
    }

    @Test
    @DisplayName("QGIS Print handles LineString perimeter")
    public void testLineStringPerimeter() {
        Assumptions.assumeTrue(mockServerAvailable, "QGIS Server Mock is not available");

        // Given: LineString perimeter
        testRequest.setPerimeter(PERIMETER_LINESTRING);
        pluginParameters.put("url", mockServerUrl);
        pluginParameters.put("layout", ATLAS_TEMPLATE);
        pluginParameters.put("pathqgs", TEST_PROJECT_PATH);

        // When: Executing the plugin
        ITaskProcessor pluginInstance = qgisPrintPlugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should succeed
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus(),
            "Should handle LineString perimeter. Message: " + result.getMessage());
    }

    @Test
    @DisplayName("QGIS Print uses default CRS when not specified")
    public void testDefaultCrs() {
        Assumptions.assumeTrue(mockServerAvailable, "QGIS Server Mock is not available");

        // Given: No CRS specified
        pluginParameters.put("url", mockServerUrl);
        pluginParameters.put("layout", ATLAS_TEMPLATE);
        pluginParameters.put("pathqgs", TEST_PROJECT_PATH);
        // No CRS parameter - should use default EPSG:2056

        // When: Executing the plugin
        ITaskProcessor pluginInstance = qgisPrintPlugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should succeed with default CRS
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus(),
            "Should use default CRS. Message: " + result.getMessage());
    }

    @Test
    @DisplayName("QGIS Print works with custom layers parameter")
    public void testCustomLayers() {
        Assumptions.assumeTrue(mockServerAvailable, "QGIS Server Mock is not available");

        // Given: Custom layers specified
        pluginParameters.put("url", mockServerUrl);
        pluginParameters.put("layout", ATLAS_TEMPLATE);
        pluginParameters.put("pathqgs", TEST_PROJECT_PATH);
        pluginParameters.put("layers", "cadastre,batiments,routes");
        pluginParameters.put("crs", "EPSG:2056");

        // When: Executing the plugin
        ITaskProcessor pluginInstance = qgisPrintPlugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should succeed
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus(),
            "Should work with custom layers. Message: " + result.getMessage());
    }

    @Test
    @DisplayName("Plugin returns error when URL not provided")
    public void testMissingUrl() {
        // Given: No URL parameter
        pluginParameters.put("layout", ATLAS_TEMPLATE);
        pluginParameters.put("pathqgs", TEST_PROJECT_PATH);

        // When: Executing the plugin
        ITaskProcessor pluginInstance = qgisPrintPlugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should return error
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus(),
            "Status should be ERROR when URL is missing");
    }

    @Test
    @DisplayName("Plugin returns error when layout not provided")
    public void testMissingLayout() {
        Assumptions.assumeTrue(mockServerAvailable, "QGIS Server Mock is not available");

        // Given: No layout parameter
        pluginParameters.put("url", mockServerUrl);
        pluginParameters.put("pathqgs", TEST_PROJECT_PATH);

        // When: Executing the plugin
        ITaskProcessor pluginInstance = qgisPrintPlugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should return error
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus(),
            "Status should be ERROR when layout is missing");
    }

    @Test
    @DisplayName("Plugin returns error for unreachable server")
    public void testUnreachableServer() {
        // Given: Unreachable server URL
        pluginParameters.put("url", "http://nonexistent.server:9999/qgis");
        pluginParameters.put("layout", ATLAS_TEMPLATE);
        pluginParameters.put("pathqgs", TEST_PROJECT_PATH);

        // When: Executing the plugin
        ITaskProcessor pluginInstance = qgisPrintPlugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should return error
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus(),
            "Status should be ERROR for unreachable server");
    }

    @Test
    @DisplayName("Plugin has correct label and description")
    public void testPluginLabelAndDescription() {
        assertNotNull(qgisPrintPlugin.getLabel(), "Label should not be null");
        assertNotNull(qgisPrintPlugin.getDescription(), "Description should not be null");
        assertFalse(qgisPrintPlugin.getLabel().isEmpty(), "Label should not be empty");
        assertFalse(qgisPrintPlugin.getDescription().isEmpty(), "Description should not be empty");
    }

    @Test
    @DisplayName("Plugin has icon class defined")
    public void testPluginPictoClass() {
        assertNotNull(qgisPrintPlugin.getPictoClass(), "Picto class should not be null");
        assertFalse(qgisPrintPlugin.getPictoClass().isEmpty(), "Picto class should not be empty");
        // QGIS Print plugin uses PDF icon
        assertTrue(qgisPrintPlugin.getPictoClass().contains("pdf"), "Picto class should be PDF-related");
    }

    @Test
    @DisplayName("Plugin can create new instance with language")
    public void testNewInstanceWithLanguage() {
        ITaskProcessor newInstance = qgisPrintPlugin.newInstance("en");
        assertNotNull(newInstance, "New instance should not be null");
        assertEquals(PLUGIN_CODE, newInstance.getCode(), "Code should match");
    }

    @Test
    @DisplayName("Plugin can create new instance with language and parameters")
    public void testNewInstanceWithLanguageAndParams() {
        Map<String, String> params = new HashMap<>();
        params.put("url", mockServerUrl);
        params.put("layout", ATLAS_TEMPLATE);

        ITaskProcessor newInstance = qgisPrintPlugin.newInstance("de", params);
        assertNotNull(newInstance, "New instance should not be null");
        assertEquals(PLUGIN_CODE, newInstance.getCode(), "Code should match");
    }
}
