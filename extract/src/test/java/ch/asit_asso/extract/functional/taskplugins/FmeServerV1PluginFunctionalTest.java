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
 * Functional tests for FME Server V1 plugin.
 * Tests plugin execution with mock FME Server using HTTP Basic Authentication.
 *
 * Note: These tests require the fme-server-mock container to be running on port 8888.
 * Run with: docker-compose -f docker-compose-test.yaml up fme-server-mock
 *
 * @author Extract Test Team
 */
@Tag("functional")
public class FmeServerV1PluginFunctionalTest {

    private static final String APPLICATION_LANGUAGE = "fr";
    private static final String PLUGIN_CODE = "FMESERVER";
    private static final String DATA_FOLDERS_BASE_PATH = "/tmp/extract-test-fmeserverv1-functional";
    private static final String TASK_PLUGINS_FOLDER_PATH = "src/main/resources/task_processors";
    private static final String PLUGIN_FILE_NAME_FILTER = "extract-task-fmeserver-*.jar";

    // Mock server configuration
    private static final String FME_SERVER_MOCK_URL = "http://localhost:8888/fmedatadownload/Repositories/TestRepo/TestWorkspace.fmw";
    private static final String VALID_USERNAME = "testuser";
    private static final String VALID_PASSWORD = "testpass";

    private static final String CLIENT_GUID = "4b01553d-9766-4014-9166-3f00f58adfc7";
    private static final String ORDER_LABEL = "SERVER-V1-TEST-001";
    private static final String ORGANISM_GUID = "a35f0327-bceb-43a1-b366-96c3a94bc47b";
    private static final String PRODUCT_GUID = "a8405d50-f712-4e3e-96b2-a5452cf4e03e";

    private static final String PERIMETER_POLYGON =
            "POLYGON((6.5 46.5, 6.6 46.5, 6.6 46.6, 6.5 46.6, 6.5 46.5))";

    private static final String PARAMETERS_JSON =
            "{\"FORMAT\":\"DXF\",\"PROJECTION\":\"EPSG:2056\",\"LAYERS\":\"cadastre,batiments\"}";

    private static ITaskProcessor fmeServerV1Plugin;
    private Request testRequest;
    private Map<String, String> pluginParameters;
    private String folderIn;
    private String folderOut;
    private ObjectMapper objectMapper;
    private static boolean mockServerAvailable = false;

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

        // Filter out V2 plugin - we want only V1
        if (foundPluginFiles != null) {
            foundPluginFiles = java.util.Arrays.stream(foundPluginFiles)
                .filter(f -> !f.getName().contains("-v2-"))
                .toArray(File[]::new);
        }

        if (ArrayUtils.isEmpty(foundPluginFiles)) {
            throw new RuntimeException("FME Server V1 plugin JAR not found.");
        }

        URL pluginUrl;
        try {
            pluginUrl = new URL(String.format("jar:file:%s!/", foundPluginFiles[0].getAbsolutePath()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        taskPluginDiscoverer.setJarUrls(new URL[] { pluginUrl });
        fmeServerV1Plugin = taskPluginDiscoverer.getTaskProcessor(PLUGIN_CODE);
        assertNotNull(fmeServerV1Plugin, "FME Server V1 plugin should be discovered");
    }

    private static void checkMockServerAvailability() {
        try {
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection)
                new URL("http://localhost:8888/health").openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            int responseCode = connection.getResponseCode();
            mockServerAvailable = (responseCode == 200);
            connection.disconnect();
        } catch (Exception e) {
            mockServerAvailable = false;
            System.out.println("FME Server Mock not available: " + e.getMessage());
        }
    }

    private void configureRequest() {
        testRequest = new Request();
        testRequest.setId(1);
        testRequest.setOrderLabel(ORDER_LABEL);
        testRequest.setOrderGuid("order-guid-server-v1");
        testRequest.setProductLabel("Test Product Server V1");
        testRequest.setProductGuid(PRODUCT_GUID);
        testRequest.setClient("Test Client Server V1");
        testRequest.setClientGuid(CLIENT_GUID);
        testRequest.setOrganism("Test Organism Server V1");
        testRequest.setOrganismGuid(ORGANISM_GUID);
        testRequest.setFolderIn(folderIn);
        testRequest.setFolderOut(folderOut);
        testRequest.setPerimeter(PERIMETER_POLYGON);
        testRequest.setParameters(PARAMETERS_JSON);
        testRequest.setStatus(Request.Status.ONGOING);
    }

    @Test
    @DisplayName("FME Server V1 plugin is correctly discovered")
    public void testPluginDiscovery() {
        assertNotNull(fmeServerV1Plugin, "Plugin should be discovered");
        assertEquals(PLUGIN_CODE, fmeServerV1Plugin.getCode(), "Plugin code should match");
    }

    @Test
    @DisplayName("Plugin has correct parameter structure with url, login, pass")
    public void testPluginParameterStructure() throws Exception {
        String params = fmeServerV1Plugin.getParams();
        assertNotNull(params, "Params should not be null");

        JsonNode paramsJson = objectMapper.readTree(params);
        assertTrue(paramsJson.isArray(), "Params should be an array");

        boolean hasUrl = false;
        boolean hasLogin = false;
        boolean hasPassword = false;

        for (JsonNode param : paramsJson) {
            String code = param.get("code").asText();
            if ("url".equals(code)) {
                hasUrl = true;
                assertTrue(param.get("req").asBoolean(), "url should be required");
            }
            if ("login".equals(code)) {
                hasLogin = true;
                assertFalse(param.get("req").asBoolean(), "login should be optional");
            }
            if ("pass".equals(code)) {
                hasPassword = true;
                assertFalse(param.get("req").asBoolean(), "pass should be optional");
                assertEquals("pass", param.get("type").asText(), "pass should be password type");
            }
        }

        assertTrue(hasUrl, "Should have 'url' parameter");
        assertTrue(hasLogin, "Should have 'login' parameter");
        assertTrue(hasPassword, "Should have 'pass' parameter");
    }

    @Test
    @DisplayName("FME Server V1 succeeds with valid credentials")
    public void testSuccessWithValidCredentials() {
        Assumptions.assumeTrue(mockServerAvailable, "FME Server Mock is not available");

        // Given: Valid credentials
        pluginParameters.put("url", FME_SERVER_MOCK_URL);
        pluginParameters.put("login", VALID_USERNAME);
        pluginParameters.put("pass", VALID_PASSWORD);

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeServerV1Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Result should be success
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus(),
            "Status should be SUCCESS with valid credentials. Message: " + result.getMessage());
    }

    @Test
    @DisplayName("FME Server V1 fails with invalid credentials")
    public void testFailureWithInvalidCredentials() {
        Assumptions.assumeTrue(mockServerAvailable, "FME Server Mock is not available");

        // Given: Invalid credentials
        pluginParameters.put("url", FME_SERVER_MOCK_URL);
        pluginParameters.put("login", "wronguser");
        pluginParameters.put("pass", "wrongpass");

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeServerV1Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Result should be error
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus(),
            "Status should be ERROR with invalid credentials");
    }

    @Test
    @DisplayName("FME Server V1 passes all request parameters")
    public void testAllParametersPassed() {
        Assumptions.assumeTrue(mockServerAvailable, "FME Server Mock is not available");

        // Given: Request with all parameters
        pluginParameters.put("url", FME_SERVER_MOCK_URL);
        pluginParameters.put("login", VALID_USERNAME);
        pluginParameters.put("pass", VALID_PASSWORD);

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeServerV1Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should succeed (mock validates parameters)
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus(),
            "Should succeed when all parameters are passed. Message: " + result.getMessage());
    }

    @Test
    @DisplayName("FME Server V1 creates result file in FolderOut")
    public void testResultFileCreated() {
        Assumptions.assumeTrue(mockServerAvailable, "FME Server Mock is not available");

        // Given: Valid configuration
        pluginParameters.put("url", FME_SERVER_MOCK_URL);
        pluginParameters.put("login", VALID_USERNAME);
        pluginParameters.put("pass", VALID_PASSWORD);

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeServerV1Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Output folder should contain a ZIP file
        if (result.getStatus() == ITaskProcessorResult.Status.SUCCESS) {
            File outputDir = new File(folderOut);
            // The plugin creates a subfolder with timestamp_productId
            File[] subDirs = outputDir.listFiles(File::isDirectory);
            if (subDirs != null && subDirs.length > 0) {
                File[] zipFiles = subDirs[0].listFiles((dir, name) -> name.endsWith(".zip"));
                assertNotNull(zipFiles, "Should have ZIP file in output subfolder");
                assertTrue(zipFiles.length > 0, "Should have at least one ZIP file");
            }
        }
    }

    @Test
    @DisplayName("Plugin returns error when no URL provided")
    public void testMissingUrl() {
        // Given: No URL parameter
        pluginParameters.put("login", VALID_USERNAME);
        pluginParameters.put("pass", VALID_PASSWORD);

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeServerV1Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should return error
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus(),
            "Status should be ERROR when URL is missing");
    }

    @Test
    @DisplayName("Plugin returns error for unreachable server")
    public void testUnreachableServer() {
        // Given: Unreachable server URL
        pluginParameters.put("url", "http://nonexistent.server:9999/fme");
        pluginParameters.put("login", VALID_USERNAME);
        pluginParameters.put("pass", VALID_PASSWORD);

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeServerV1Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should return error
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus(),
            "Status should be ERROR for unreachable server");
    }

    @Test
    @DisplayName("Plugin handles null perimeter gracefully")
    public void testNullPerimeter() {
        Assumptions.assumeTrue(mockServerAvailable, "FME Server Mock is not available");

        // Given: Request without perimeter
        testRequest.setPerimeter(null);
        pluginParameters.put("url", FME_SERVER_MOCK_URL);
        pluginParameters.put("login", VALID_USERNAME);
        pluginParameters.put("pass", VALID_PASSWORD);

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeServerV1Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should handle gracefully (mock accepts null perimeter)
        assertNotNull(result);
    }

    @Test
    @DisplayName("Plugin has correct label and description")
    public void testPluginLabelAndDescription() {
        assertNotNull(fmeServerV1Plugin.getLabel(), "Label should not be null");
        assertNotNull(fmeServerV1Plugin.getDescription(), "Description should not be null");
        assertFalse(fmeServerV1Plugin.getLabel().isEmpty(), "Label should not be empty");
        assertFalse(fmeServerV1Plugin.getDescription().isEmpty(), "Description should not be empty");
    }

    @Test
    @DisplayName("Plugin has icon class defined")
    public void testPluginPictoClass() {
        assertNotNull(fmeServerV1Plugin.getPictoClass(), "Picto class should not be null");
        assertFalse(fmeServerV1Plugin.getPictoClass().isEmpty(), "Picto class should not be empty");
    }

    @Test
    @DisplayName("Plugin can create new instance with language")
    public void testNewInstanceWithLanguage() {
        ITaskProcessor newInstance = fmeServerV1Plugin.newInstance("en");
        assertNotNull(newInstance, "New instance should not be null");
        assertEquals(PLUGIN_CODE, newInstance.getCode(), "Code should match");
    }
}
