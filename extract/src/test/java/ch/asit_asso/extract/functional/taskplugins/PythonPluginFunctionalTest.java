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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Functional tests for Python plugin (Issue #346).
 * Tests script execution, parameters file creation, GeoJSON format, and various geometries.
 *
 * These are functional tests because they require Python to be installed on the system.
 *
 * @author Bruno Alves
 */
@Tag("functional")
public class PythonPluginFunctionalTest {

    private static final String APPLICATION_LANGUAGE = "fr";
    private static final String PLUGIN_CODE = "python";
    private static final String DATA_FOLDERS_BASE_PATH = "/tmp/extract-test";
    private static final String TASK_PLUGINS_FOLDER_PATH = "src/main/resources/task_processors";
    private static final String PLUGIN_FILE_NAME_FILTER = "extract-task-python-*.jar";

    private static final String SUCCESS_SCRIPT = "src/test/java/ch/asit_asso/extract/functional/taskplugins/python_scripts/test_success.py";
    private static final String READ_PARAMS_SCRIPT = "src/test/java/ch/asit_asso/extract/functional/taskplugins/python_scripts/test_read_parameters.py";
    private static final String VERIFY_GEOJSON_SCRIPT = "src/test/java/ch/asit_asso/extract/functional/taskplugins/python_scripts/test_verify_geojson.py";
    private static final String CHECK_PROPERTIES_SCRIPT = "src/test/java/ch/asit_asso/extract/functional/taskplugins/python_scripts/test_check_properties.py";
    private static final String FAILURE_SCRIPT = "src/test/java/ch/asit_asso/extract/functional/taskplugins/python_scripts/test_failure.py";
    private static final String CREATE_OUTPUT_SCRIPT = "src/test/java/ch/asit_asso/extract/functional/taskplugins/python_scripts/test_create_output.py";

    private static final String CLIENT_GUID = "client-guid-test-123";
    private static final String ORDER_LABEL = "ORDER-TEST-001";
    private static final String ORGANISM_GUID = "organism-guid-test-456";
    private static final String PRODUCT_GUID = "product-guid-test-789";

    private static final String PERIMETER_POLYGON =
        "POLYGON((6.5 46.5, 6.6 46.5, 6.6 46.6, 6.5 46.6, 6.5 46.5))";

    private static final String PERIMETER_MULTIPOLYGON =
        "MULTIPOLYGON(((6.5 46.5, 6.6 46.5, 6.6 46.6, 6.5 46.6, 6.5 46.5))," +
        "((6.7 46.7, 6.8 46.7, 6.8 46.8, 6.7 46.8, 6.7 46.7)))";

    private static final String PERIMETER_POLYGON_WITH_HOLE =
        "POLYGON((6.5 46.5, 6.6 46.5, 6.6 46.6, 6.5 46.6, 6.5 46.5)," +
        "(6.52 46.52, 6.53 46.52, 6.53 46.53, 6.52 46.53, 6.52 46.52))";

    private static final String PERIMETER_POINT = "POINT(6.5 46.5)";

    private static final String PERIMETER_LINESTRING =
        "LINESTRING(6.5 46.5, 6.6 46.6, 6.7 46.7)";

    private static final String PARAMETERS_JSON =
        "{\"FORMAT\":\"DXF\",\"PROJECTION\":\"EPSG:2056\",\"SCALE\":1000}";

    private static ITaskProcessor pythonPlugin;
    private Request testRequest;
    private Map<String, String> pluginParameters;
    private String folderIn;
    private String folderOut;
    private ObjectMapper objectMapper;

    @BeforeAll
    public static void initialize() {
        configurePlugin();
    }

    @BeforeEach
    public void setUp() throws IOException {
        // Use RELATIVE paths for the request (TaskProcessorRequest combines with base path)
        folderIn = Paths.get(ORDER_LABEL, "input").toString();
        folderOut = Paths.get(ORDER_LABEL, "output").toString();

        // Create actual directories on filesystem using ABSOLUTE paths
        Files.createDirectories(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn));
        Files.createDirectories(Paths.get(DATA_FOLDERS_BASE_PATH, folderOut));

        // Initialize object mapper
        objectMapper = new ObjectMapper();

        // Configure request
        configureRequest();

        // Configure plugin parameters
        pluginParameters = new HashMap<>();
    }

    @AfterEach
    public void tearDown() throws IOException {
        // Clean up test directories
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

    private void configureRequest() {
        testRequest = new Request();
        testRequest.setId(1);
        testRequest.setOrderLabel(ORDER_LABEL);
        testRequest.setOrderGuid("order-guid-test");
        testRequest.setProductLabel("Test Product");
        testRequest.setProductGuid(PRODUCT_GUID);
        testRequest.setClient("Test Client");
        testRequest.setClientGuid(CLIENT_GUID);
        testRequest.setOrganism("Test Organism");
        testRequest.setOrganismGuid(ORGANISM_GUID);
        testRequest.setFolderIn(folderIn);
        testRequest.setFolderOut(folderOut);
        testRequest.setPerimeter(PERIMETER_POLYGON);
        testRequest.setStatus(Request.Status.ONGOING);
    }

    @Test
    @DisplayName("Python plugin basic execution succeeds")
    public void testPythonPluginBasicExecution() {
        // Given: Script that exits with code 0
        pluginParameters.put("pythonInterpreter", getPythonInterpreter());
        pluginParameters.put("pythonScript", new File(SUCCESS_SCRIPT).getAbsolutePath());

        // When: Executing the plugin
        ITaskProcessor pluginInstance = pythonPlugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should succeed
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus(),
            "Status should be SUCCESS");
        assertNotNull(result.getMessage(),
            "Message should not be null");
        assertFalse(result.getMessage().isEmpty(),
            "Message should not be empty");
        assertTrue(result.getErrorCode() == null || result.getErrorCode().isEmpty(),
            "Error code should be null or empty on success");
    }

    @Test
    @DisplayName("Parameters JSON file is created with valid format")
    public void testPythonPluginParametersJsonFileCreation() throws IOException {
        // Given: Script that reads parameters.json
        pluginParameters.put("pythonInterpreter", getPythonInterpreter());
        pluginParameters.put("pythonScript", new File(READ_PARAMS_SCRIPT).getAbsolutePath());

        // When: Executing the plugin
        ITaskProcessor pluginInstance = pythonPlugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: parameters.json should exist and be valid (use absolute path for assertion)
        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        assertTrue(parametersFile.exists(),
            "parameters.json should be created in FolderIn");

        // Verify it's valid JSON
        JsonNode json = objectMapper.readTree(parametersFile);
        assertNotNull(json, "Should be valid JSON");

        // Verify GeoJSON Feature structure
        assertEquals("Feature", json.get("type").asText(),
            "Should be a GeoJSON Feature");
        assertTrue(json.has("geometry"),
            "Should have geometry");
        assertTrue(json.has("properties"),
            "Should have properties");

        // Script should succeed if JSON is valid
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus(),
            "Script should succeed after reading valid JSON");
    }

    @Test
    @DisplayName("GeoJSON format with Polygon geometry is correct")
    public void testPythonPluginGeoJsonFormatWithPolygonGeometry() throws IOException {
        // Given: Request with Polygon perimeter
        testRequest.setPerimeter(PERIMETER_POLYGON);
        pluginParameters.put("pythonInterpreter", getPythonInterpreter());
        pluginParameters.put("pythonScript", new File(VERIFY_GEOJSON_SCRIPT).getAbsolutePath());

        // When: Executing the plugin
        ITaskProcessor pluginInstance = pythonPlugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: GeoJSON should have correct Polygon structure
        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode geometry = json.get("geometry");
        assertNotNull(geometry, "Geometry should be present");
        assertEquals("Polygon", geometry.get("type").asText(),
            "Geometry type should be Polygon");

        JsonNode coordinates = geometry.get("coordinates");
        assertNotNull(coordinates, "Coordinates should be present");
        assertTrue(coordinates.isArray(), "Coordinates should be an array");
        assertTrue(coordinates.size() > 0, "Coordinates should not be empty");

        // Script should succeed
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus(),
            "Script should succeed with valid Polygon");
    }

    @Test
    @DisplayName("GeoJSON with MultiPolygon geometry is supported")
    public void testPythonPluginGeoJsonWithMultiPolygon() throws IOException {
        // Given: Request with MultiPolygon perimeter
        testRequest.setPerimeter(PERIMETER_MULTIPOLYGON);
        pluginParameters.put("pythonInterpreter", getPythonInterpreter());
        pluginParameters.put("pythonScript", new File(VERIFY_GEOJSON_SCRIPT).getAbsolutePath());

        // When: Executing the plugin
        ITaskProcessor pluginInstance = pythonPlugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: GeoJSON should have correct MultiPolygon structure
        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode geometry = json.get("geometry");
        assertEquals("MultiPolygon", geometry.get("type").asText(),
            "Geometry type should be MultiPolygon");

        JsonNode coordinates = geometry.get("coordinates");
        assertTrue(coordinates.isArray(), "Coordinates should be an array");
        assertEquals(2, coordinates.size(),
            "Should have 2 polygons");

        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
    }

    @Test
    @DisplayName("GeoJSON with donut geometry (interior ring) is supported")
    public void testPythonPluginGeoJsonWithDonutGeometry() throws IOException {
        // Given: Request with Polygon containing a hole
        testRequest.setPerimeter(PERIMETER_POLYGON_WITH_HOLE);
        pluginParameters.put("pythonInterpreter", getPythonInterpreter());
        pluginParameters.put("pythonScript", new File(VERIFY_GEOJSON_SCRIPT).getAbsolutePath());

        // When: Executing the plugin
        ITaskProcessor pluginInstance = pythonPlugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: GeoJSON should have exterior and interior rings
        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode geometry = json.get("geometry");
        assertEquals("Polygon", geometry.get("type").asText());

        JsonNode coordinates = geometry.get("coordinates");
        assertTrue(coordinates.size() >= 2,
            "Should have at least 2 rings (exterior + interior)");

        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
    }

    @Test
    @DisplayName("GeoJSON with Point geometry is supported")
    public void testPythonPluginGeoJsonWithPointGeometry() throws IOException {
        // Given: Request with Point perimeter
        testRequest.setPerimeter(PERIMETER_POINT);
        pluginParameters.put("pythonInterpreter", getPythonInterpreter());
        pluginParameters.put("pythonScript", new File(VERIFY_GEOJSON_SCRIPT).getAbsolutePath());

        // When: Executing the plugin
        ITaskProcessor pluginInstance = pythonPlugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: GeoJSON should have correct Point structure
        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode geometry = json.get("geometry");
        assertEquals("Point", geometry.get("type").asText(),
            "Geometry type should be Point");

        JsonNode coordinates = geometry.get("coordinates");
        assertTrue(coordinates.isArray(), "Coordinates should be an array");
        assertEquals(2, coordinates.size(),
            "Point should have 2 coordinates (x, y)");

        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
    }

    @Test
    @DisplayName("GeoJSON with LineString geometry is supported")
    public void testPythonPluginGeoJsonWithLineStringGeometry() throws IOException {
        // Given: Request with LineString perimeter
        testRequest.setPerimeter(PERIMETER_LINESTRING);
        pluginParameters.put("pythonInterpreter", getPythonInterpreter());
        pluginParameters.put("pythonScript", new File(VERIFY_GEOJSON_SCRIPT).getAbsolutePath());

        // When: Executing the plugin
        ITaskProcessor pluginInstance = pythonPlugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: GeoJSON should have correct LineString structure
        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode geometry = json.get("geometry");
        assertEquals("LineString", geometry.get("type").asText(),
            "Geometry type should be LineString");

        JsonNode coordinates = geometry.get("coordinates");
        assertTrue(coordinates.isArray(), "Coordinates should be an array");
        assertTrue(coordinates.size() >= 2,
            "LineString should have at least 2 points");

        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
    }

    @Test
    @DisplayName("Feature properties contain all metadata fields")
    public void testPythonPluginFeaturePropertiesWithMetadata() throws IOException {
        // Given: Request with all metadata fields
        pluginParameters.put("pythonInterpreter", getPythonInterpreter());
        pluginParameters.put("pythonScript", new File(CHECK_PROPERTIES_SCRIPT).getAbsolutePath());

        // When: Executing the plugin
        ITaskProcessor pluginInstance = pythonPlugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Properties should contain all metadata
        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode properties = json.get("properties");
        assertNotNull(properties, "Properties should be present");

        // Verify key metadata fields (using CamelCase as the plugin uses)
        assertEquals(CLIENT_GUID, properties.get("ClientGuid").asText(),
            "ClientGuid should be present");
        assertEquals("Test Client", properties.get("ClientName").asText(),
            "ClientName should be present");
        assertEquals("Test Organism", properties.get("OrganismName").asText(),
            "OrganismName should be present");
        assertEquals("Test Product", properties.get("ProductLabel").asText(),
            "ProductLabel should be present");
        assertEquals(ORDER_LABEL, properties.get("OrderLabel").asText(),
            "OrderLabel should be present");

        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
    }

    @Test
    @DisplayName("Dynamic parameters are added to properties")
    public void testPythonPluginDynamicParametersInProperties() throws IOException {
        // Given: Request with dynamic parameters
        testRequest.setParameters(PARAMETERS_JSON);
        pluginParameters.put("pythonInterpreter", getPythonInterpreter());
        pluginParameters.put("pythonScript", new File(CHECK_PROPERTIES_SCRIPT).getAbsolutePath());

        // When: Executing the plugin
        ITaskProcessor pluginInstance = pythonPlugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Dynamic parameters should be in nested Parameters object
        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode properties = json.get("properties");
        JsonNode parameters = properties.get("Parameters");
        assertNotNull(parameters, "Parameters nested object should be present");

        assertEquals("DXF", parameters.get("FORMAT").asText(),
            "FORMAT parameter should be present");
        assertEquals("EPSG:2056", parameters.get("PROJECTION").asText(),
            "PROJECTION parameter should be present");
        // Note: SCALE is an integer in JSON, but the plugin stores it as string value
        assertEquals(1000, parameters.get("SCALE").asInt(),
            "SCALE parameter should be present");

        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
    }

    @Test
    @DisplayName("Non-zero exit code results in ERROR status")
    public void testPythonPluginNonZeroExitCode() {
        // Given: Script that fails with exit code 1
        pluginParameters.put("pythonInterpreter", getPythonInterpreter());
        pluginParameters.put("pythonScript", new File(FAILURE_SCRIPT).getAbsolutePath());

        // When: Executing the plugin
        ITaskProcessor pluginInstance = pythonPlugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should return ERROR status
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus(),
            "Status should be ERROR when script fails");
        assertFalse(result.getErrorCode().isEmpty(),
            "Error code should not be empty");
    }

    @Test
    @DisplayName("Script not found results in ERROR status")
    public void testPythonPluginScriptNotFound() {
        // Given: Non-existent script path
        pluginParameters.put("pythonInterpreter", getPythonInterpreter());
        pluginParameters.put("pythonScript", "/nonexistent/script.py");

        // When: Executing the plugin
        ITaskProcessor pluginInstance = pythonPlugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should return ERROR status
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus(),
            "Status should be ERROR when script not found");
    }

    @Test
    @DisplayName("Output files are created in FolderOut")
    public void testPythonPluginOutputFilesInFolderOut() {
        // Given: Script that creates output file
        pluginParameters.put("pythonInterpreter", getPythonInterpreter());
        pluginParameters.put("pythonScript", new File(CREATE_OUTPUT_SCRIPT).getAbsolutePath());

        // When: Executing the plugin
        ITaskProcessor pluginInstance = pythonPlugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Output file should exist in FolderOut (use absolute path for assertion)
        File outputFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderOut).toString(), "result.txt");
        assertTrue(outputFile.exists(),
            "Output file should be created in FolderOut");
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
    }

    /**
     * Gets the absolute path to the Python interpreter.
     * The plugin requires an absolute path that exists and is executable.
     */
    private String getPythonInterpreter() {
        // Try to find python3 or python using which command
        String[] interpreters = {"python3", "python"};
        for (String interpreter : interpreters) {
            try {
                Process process = Runtime.getRuntime().exec(new String[]{"which", interpreter});
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(process.getInputStream()))) {
                        String path = reader.readLine();
                        if (path != null && !path.trim().isEmpty()) {
                            return path.trim();
                        }
                    }
                }
            } catch (Exception e) {
                // Continue to next interpreter
            }
        }
        return "/usr/bin/python3"; // Default fallback
    }
}
