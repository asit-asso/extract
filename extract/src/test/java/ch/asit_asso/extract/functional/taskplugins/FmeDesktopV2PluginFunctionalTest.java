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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Functional tests for FME Desktop V2 plugin (Issue #347).
 * Tests full plugin execution with mock FME executable that validates parameters.json file.
 *
 * These are functional tests because they execute the full plugin flow with a mock executable.
 *
 * @author Bruno Alves
 */
@Tag("functional")
public class FmeDesktopV2PluginFunctionalTest {

    private static final String APPLICATION_LANGUAGE = "fr";
    private static final String PLUGIN_CODE = "FME2017V2";
    private static final String DATA_FOLDERS_BASE_PATH = "/tmp/extract-test-fmedesktopv2-functional";
    private static final String TASK_PLUGINS_FOLDER_PATH = "src/main/resources/task_processors";
    private static final String PLUGIN_FILE_NAME_FILTER = "extract-task-fmedesktop-v2-*.jar";

    private static final String SUCCESS_WORKSPACE = "src/test/java/ch/asit_asso/extract/functional/taskplugins/fme_scripts/workspace_v2.fmw";
    private static final String FAILURE_WORKSPACE = "src/test/java/ch/asit_asso/extract/functional/taskplugins/fme_scripts/workspace_v2_fails.fmw";
    private static final String NO_FILES_WORKSPACE = "src/test/java/ch/asit_asso/extract/functional/taskplugins/fme_scripts/workspace_v2_nofiles.fmw";
    private static final String MOCK_EXECUTABLE = "src/test/java/ch/asit_asso/extract/functional/taskplugins/fme_scripts/fme_desktop_v2_mock.sh";

    private static final String CLIENT_GUID = "4b01553d-9766-4014-9166-3f00f58adfc7";
    private static final String ORDER_LABEL = "443530";
    private static final String ORGANISM_GUID = "a35f0327-bceb-43a1-b366-96c3a94bc47b";
    private static final String PRODUCT_GUID = "a8405d50-f712-4e3e-96b2-a5452cf4e03e";

    private static final String PERIMETER_POLYGON =
            "POLYGON((7.008802763251656 46.245519329293245,7.008977478638646 46.24596978223839," +
            "7.010099318044382 46.24634512591109,7.011161356635566 46.24649533820254," +
            "7.011851394695592 46.24654742881326,7.012123110524144 46.24662042289713," +
            "7.012329750692657 46.246724655380014,7.012417623228246 46.24668000889588," +
            "7.012559036117633 46.24642191589558,7.012535717792058 46.246088985456616," +
            "7.012514122624683 46.245949469899564,7.012472496413521 46.245884093468234," +
            "7.012185407319924 46.24570534214322,7.01217302489515 46.24563108046702," +
            "7.011217983680352 46.24547903436611,7.009977076726536 46.244995300279086," +
            "7.009187734983265 46.24479663917551,7.008860662659381 46.24516646719812," +
            "7.008784739864421 46.24533934577381,7.008802763251656 46.245519329293245))";

    private static final String PERIMETER_MULTIPOLYGON =
            "MULTIPOLYGON(((6.5 46.5, 6.6 46.5, 6.6 46.6, 6.5 46.6, 6.5 46.5))," +
            "((6.7 46.7, 6.8 46.7, 6.8 46.8, 6.7 46.8, 6.7 46.7)))";

    private static final String PERIMETER_POINT = "POINT(6.5 46.5)";

    private static final String PERIMETER_LINESTRING = "LINESTRING(6.5 46.5, 6.6 46.6, 6.7 46.7)";

    private static final String PARAMETERS_JSON =
            "{\"FORMAT\":\"DXF\",\"PROJECTION\":\"SWITZERLAND95\",\"RAISON\":\"LOCALISATION\"," +
            "\"RAISON_LABEL\":\"Localisation en vue de projets\"," +
            "\"REMARK\":\"Ceci est un test\\nAvec retour à la ligne\"}";

    private static ITaskProcessor fmeDesktopV2Plugin;
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
        String orderFolderName = "ORDER-FME-V2-TEST";
        // Relative paths for the request (TaskProcessorRequest combines with base path)
        folderIn = Paths.get(orderFolderName, "input").toString();
        folderOut = Paths.get(orderFolderName, "output").toString();

        // Create actual directories on filesystem using absolute paths
        Files.createDirectories(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn));
        Files.createDirectories(Paths.get(DATA_FOLDERS_BASE_PATH, folderOut));

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

    private void configureRequest() {
        testRequest = new Request();
        testRequest.setId(1);
        testRequest.setOrderLabel(ORDER_LABEL);
        testRequest.setOrderGuid("order-guid-fme-v2");
        testRequest.setProductLabel("Test Product FME V2");
        testRequest.setProductGuid(PRODUCT_GUID);
        testRequest.setClient("Test Client FME V2");
        testRequest.setClientGuid(CLIENT_GUID);
        testRequest.setOrganism("Test Organism FME V2");
        testRequest.setOrganismGuid(ORGANISM_GUID);
        testRequest.setFolderIn(folderIn);
        testRequest.setFolderOut(folderOut);
        testRequest.setPerimeter(PERIMETER_POLYGON);
        testRequest.setParameters(PARAMETERS_JSON);
        testRequest.setStatus(Request.Status.ONGOING);
    }

    @Test
    @DisplayName("FME Desktop V2 plugin basic execution succeeds with parameters.json")
    public void testFmeDesktopV2BasicExecution() throws IOException {
        // Given: Plugin configured with mock executable
        pluginParameters.put("workbench", new File(SUCCESS_WORKSPACE).getAbsolutePath());
        pluginParameters.put("application", new File(MOCK_EXECUTABLE).getAbsolutePath());
        pluginParameters.put("instances", "1");

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeDesktopV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: parameters.json should be created
        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        assertTrue(parametersFile.exists(), "parameters.json should be created in FolderIn");

        // Verify GeoJSON structure
        JsonNode json = objectMapper.readTree(parametersFile);
        assertEquals("Feature", json.get("type").asText(), "Should be a GeoJSON Feature");
        assertTrue(json.has("geometry"), "Should have geometry");
        assertTrue(json.has("properties"), "Should have properties");
    }

    @Test
    @DisplayName("Parameters.json contains correct GeoJSON Polygon geometry")
    public void testParametersJsonWithPolygonGeometry() throws IOException {
        // Given: Request with Polygon perimeter
        testRequest.setPerimeter(PERIMETER_POLYGON);
        pluginParameters.put("workbench", new File(SUCCESS_WORKSPACE).getAbsolutePath());
        pluginParameters.put("application", new File(MOCK_EXECUTABLE).getAbsolutePath());
        pluginParameters.put("instances", "1");

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeDesktopV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        pluginInstance.execute(new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH), null);

        // Then: GeoJSON should have correct Polygon structure
        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode geometry = json.get("geometry");
        assertNotNull(geometry, "Geometry should be present");
        assertEquals("Polygon", geometry.get("type").asText(), "Geometry type should be Polygon");

        JsonNode coordinates = geometry.get("coordinates");
        assertTrue(coordinates.isArray(), "Coordinates should be an array");
        assertTrue(coordinates.size() > 0, "Coordinates should not be empty");
    }

    @Test
    @DisplayName("Parameters.json contains correct GeoJSON MultiPolygon geometry")
    public void testParametersJsonWithMultiPolygonGeometry() throws IOException {
        // Given: Request with MultiPolygon perimeter
        testRequest.setPerimeter(PERIMETER_MULTIPOLYGON);
        pluginParameters.put("workbench", new File(SUCCESS_WORKSPACE).getAbsolutePath());
        pluginParameters.put("application", new File(MOCK_EXECUTABLE).getAbsolutePath());
        pluginParameters.put("instances", "1");

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeDesktopV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        pluginInstance.execute(new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH), null);

        // Then: GeoJSON should have correct MultiPolygon structure
        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode geometry = json.get("geometry");
        assertEquals("MultiPolygon", geometry.get("type").asText(), "Geometry type should be MultiPolygon");
        assertEquals(2, geometry.get("coordinates").size(), "Should have 2 polygons");
    }

    @Test
    @DisplayName("Parameters.json contains correct GeoJSON Point geometry")
    public void testParametersJsonWithPointGeometry() throws IOException {
        // Given: Request with Point perimeter
        testRequest.setPerimeter(PERIMETER_POINT);
        pluginParameters.put("workbench", new File(SUCCESS_WORKSPACE).getAbsolutePath());
        pluginParameters.put("application", new File(MOCK_EXECUTABLE).getAbsolutePath());
        pluginParameters.put("instances", "1");

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeDesktopV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        pluginInstance.execute(new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH), null);

        // Then: GeoJSON should have correct Point structure
        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode geometry = json.get("geometry");
        assertEquals("Point", geometry.get("type").asText(), "Geometry type should be Point");
        assertEquals(2, geometry.get("coordinates").size(), "Point should have 2 coordinates");
    }

    @Test
    @DisplayName("Parameters.json contains correct GeoJSON LineString geometry")
    public void testParametersJsonWithLineStringGeometry() throws IOException {
        // Given: Request with LineString perimeter
        testRequest.setPerimeter(PERIMETER_LINESTRING);
        pluginParameters.put("workbench", new File(SUCCESS_WORKSPACE).getAbsolutePath());
        pluginParameters.put("application", new File(MOCK_EXECUTABLE).getAbsolutePath());
        pluginParameters.put("instances", "1");

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeDesktopV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        pluginInstance.execute(new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH), null);

        // Then: GeoJSON should have correct LineString structure
        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode geometry = json.get("geometry");
        assertEquals("LineString", geometry.get("type").asText(), "Geometry type should be LineString");
        assertEquals(3, geometry.get("coordinates").size(), "LineString should have 3 points");
    }

    @Test
    @DisplayName("Parameters.json contains nested Parameters object with custom values")
    public void testParametersJsonWithCustomParameters() throws IOException {
        // Given: Request with custom parameters
        testRequest.setParameters(PARAMETERS_JSON);
        pluginParameters.put("workbench", new File(SUCCESS_WORKSPACE).getAbsolutePath());
        pluginParameters.put("application", new File(MOCK_EXECUTABLE).getAbsolutePath());
        pluginParameters.put("instances", "1");

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeDesktopV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        pluginInstance.execute(new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH), null);

        // Then: Parameters should be nested in properties
        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode properties = json.get("properties");
        assertTrue(properties.has("Parameters"), "Should have Parameters object");

        JsonNode parameters = properties.get("Parameters");
        assertEquals("DXF", parameters.get("FORMAT").asText(), "FORMAT should be DXF");
        assertEquals("SWITZERLAND95", parameters.get("PROJECTION").asText(), "PROJECTION should be SWITZERLAND95");
    }

    @Test
    @DisplayName("Null perimeter creates valid GeoJSON with null geometry")
    public void testParametersJsonWithNullPerimeter() throws IOException {
        // Given: Request without perimeter
        testRequest.setPerimeter(null);
        pluginParameters.put("workbench", new File(SUCCESS_WORKSPACE).getAbsolutePath());
        pluginParameters.put("application", new File(MOCK_EXECUTABLE).getAbsolutePath());
        pluginParameters.put("instances", "1");

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeDesktopV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        pluginInstance.execute(new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH), null);

        // Then: GeoJSON should have null geometry
        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        assertEquals("Feature", json.get("type").asText());
        assertTrue(json.get("geometry").isNull(), "Geometry should be null");
    }

    @Test
    @DisplayName("Large Swiss coordinates are preserved accurately")
    public void testParametersJsonWithLargeCoordinates() throws IOException {
        // Given: Request with Swiss coordinates
        testRequest.setPerimeter("POLYGON((2500000 1200000, 2500100 1200000, 2500100 1200100, 2500000 1200100, 2500000 1200000))");
        pluginParameters.put("workbench", new File(SUCCESS_WORKSPACE).getAbsolutePath());
        pluginParameters.put("application", new File(MOCK_EXECUTABLE).getAbsolutePath());
        pluginParameters.put("instances", "1");

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeDesktopV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        pluginInstance.execute(new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH), null);

        // Then: Coordinates should be preserved
        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode coordinates = json.get("geometry").get("coordinates").get(0);
        assertEquals(2500000, coordinates.get(0).get(0).asDouble(), 0.001, "X coordinate should be preserved");
        assertEquals(1200000, coordinates.get(0).get(1).asDouble(), 0.001, "Y coordinate should be preserved");
    }

    @Test
    @DisplayName("Complex nested parameters are preserved in JSON")
    public void testParametersJsonWithComplexNestedParameters() throws IOException {
        // Given: Complex nested parameters
        testRequest.setParameters("{\"format\":\"PDF\",\"options\":{\"compress\":true,\"quality\":95},\"layers\":[\"A\",\"B\"]}");
        pluginParameters.put("workbench", new File(SUCCESS_WORKSPACE).getAbsolutePath());
        pluginParameters.put("application", new File(MOCK_EXECUTABLE).getAbsolutePath());
        pluginParameters.put("instances", "1");

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeDesktopV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        pluginInstance.execute(new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH), null);

        // Then: Nested structure should be preserved
        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode parameters = json.get("properties").get("Parameters");
        assertEquals("PDF", parameters.get("format").asText());
        assertTrue(parameters.get("options").get("compress").asBoolean());
        assertEquals(95, parameters.get("options").get("quality").asInt());
        assertEquals(2, parameters.get("layers").size());
    }

    @Test
    @DisplayName("Invalid JSON parameters are handled gracefully")
    public void testParametersJsonWithInvalidParameters() throws IOException {
        // Given: Invalid JSON parameters
        testRequest.setParameters("{invalid json}}");
        pluginParameters.put("workbench", new File(SUCCESS_WORKSPACE).getAbsolutePath());
        pluginParameters.put("application", new File(MOCK_EXECUTABLE).getAbsolutePath());
        pluginParameters.put("instances", "1");

        // When: Executing the plugin - should not throw
        ITaskProcessor pluginInstance = fmeDesktopV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        assertDoesNotThrow(() -> pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH), null));

        // Then: File should still be created
        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        assertTrue(parametersFile.exists(), "parameters.json should be created");
    }

    @Test
    @DisplayName("FolderOut is correctly set in properties")
    public void testParametersJsonContainsFolderOut() throws IOException {
        // Given: Standard configuration
        pluginParameters.put("workbench", new File(SUCCESS_WORKSPACE).getAbsolutePath());
        pluginParameters.put("application", new File(MOCK_EXECUTABLE).getAbsolutePath());
        pluginParameters.put("instances", "1");

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeDesktopV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        pluginInstance.execute(new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH), null);

        // Then: FolderOut should be in properties
        File parametersFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn).toString(), "parameters.json");
        JsonNode json = objectMapper.readTree(parametersFile);

        JsonNode properties = json.get("properties");
        assertTrue(properties.has("FolderOut"), "Should have FolderOut property");
        assertFalse(properties.get("FolderOut").asText().isEmpty(), "FolderOut should not be empty");
    }
}
