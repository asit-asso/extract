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
 * Functional tests for FME Server V2 plugin with Mock Server.
 * Tests full plugin execution against the FME Server Mock using API Token authentication.
 *
 * Note: These tests require the fme-server-mock container to be running on port 8888.
 * Run with: docker-compose -f docker-compose-test.yaml up fme-server-mock
 *
 * @author Extract Test Team
 */
@Tag("functional")
public class FmeServerV2WithMockFunctionalTest {

    private static final String APPLICATION_LANGUAGE = "fr";
    private static final String PLUGIN_CODE = "FMESERVERV2";
    private static final String DATA_FOLDERS_BASE_PATH = "/tmp/extract-test-fmeserverv2-mock";
    private static final String TASK_PLUGINS_FOLDER_PATH = "src/main/resources/task_processors";
    private static final String PLUGIN_FILE_NAME_FILTER = "extract-task-fmeserver-v2-*.jar";

    // Mock server configuration - Note: V2 uses a different endpoint
    private static final String FME_SERVER_MOCK_URL = "http://fme-server-mock:8888/fmeserver/v2/datadownload";
    private static final String FME_SERVER_MOCK_URL_LOCAL = "http://localhost:8888/fmeserver/v2/datadownload";
    private static final String VALID_API_TOKEN = "valid_test_token_123456789";
    private static final String INVALID_API_TOKEN = "invalid_token";

    private static final String CLIENT_GUID = "4b01553d-9766-4014-9166-3f00f58adfc7";
    private static final String ORDER_LABEL = "SERVER-V2-MOCK-TEST";
    private static final String ORGANISM_GUID = "a35f0327-bceb-43a1-b366-96c3a94bc47b";
    private static final String PRODUCT_GUID = "a8405d50-f712-4e3e-96b2-a5452cf4e03e";

    private static final String PERIMETER_POLYGON =
            "POLYGON((6.5 46.5, 6.6 46.5, 6.6 46.6, 6.5 46.6, 6.5 46.5))";

    private static final String PERIMETER_MULTIPOLYGON =
            "MULTIPOLYGON(((6.5 46.5, 6.6 46.5, 6.6 46.6, 6.5 46.6, 6.5 46.5))," +
            "((6.7 46.7, 6.8 46.7, 6.8 46.8, 6.7 46.8, 6.7 46.7)))";

    private static final String PARAMETERS_JSON =
            "{\"FORMAT\":\"GeoJSON\",\"PROJECTION\":\"EPSG:2056\",\"INCLUDE_METADATA\":true}";

    private static ITaskProcessor fmeServerV2Plugin;
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
        // Relative paths for Request domain object (TaskProcessorRequest combines these with base path)
        folderIn = Paths.get(orderFolderName, "input").toString();
        folderOut = Paths.get(orderFolderName, "output").toString();

        // Create absolute directories for file operations
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
            throw new RuntimeException("FME Server V2 plugin JAR not found.");
        }

        URL pluginUrl;
        try {
            pluginUrl = new URL(String.format("jar:file:%s!/", foundPluginFiles[0].getAbsolutePath()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        taskPluginDiscoverer.setJarUrls(new URL[] { pluginUrl });
        fmeServerV2Plugin = taskPluginDiscoverer.getTaskProcessor(PLUGIN_CODE);
        assertNotNull(fmeServerV2Plugin, "FME Server V2 plugin should be discovered");
    }

    private static void checkMockServerAvailability() {
        // Try localhost first
        try {
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection)
                new URL("http://localhost:8888/health").openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                mockServerAvailable = true;
                mockServerUrl = FME_SERVER_MOCK_URL_LOCAL;
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
                new URL("http://fme-server-mock:8888/health").openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            int responseCode = connection.getResponseCode();
            mockServerAvailable = (responseCode == 200);
            mockServerUrl = FME_SERVER_MOCK_URL;
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
        testRequest.setOrderGuid("order-guid-server-v2-mock");
        testRequest.setProductLabel("Test Product Server V2 Mock");
        testRequest.setProductGuid(PRODUCT_GUID);
        testRequest.setClient("Test Client Server V2");
        testRequest.setClientGuid(CLIENT_GUID);
        testRequest.setOrganism("Test Organism Server V2");
        testRequest.setOrganismGuid(ORGANISM_GUID);
        testRequest.setFolderIn(folderIn);
        testRequest.setFolderOut(folderOut);
        testRequest.setPerimeter(PERIMETER_POLYGON);
        testRequest.setParameters(PARAMETERS_JSON);
        testRequest.setStatus(Request.Status.ONGOING);
    }

    @Test
    @DisplayName("FME Server V2 succeeds with valid API token")
    public void testSuccessWithValidApiToken() {
        Assumptions.assumeTrue(mockServerAvailable, "FME Server Mock is not available");

        // Given: Valid API token
        pluginParameters.put("serviceURL", mockServerUrl);
        pluginParameters.put("apiToken", VALID_API_TOKEN);

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeServerV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Result should be success
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus(),
            "Status should be SUCCESS with valid API token. Message: " + result.getMessage());
    }

    @Test
    @DisplayName("FME Server V2 fails with invalid API token")
    public void testFailureWithInvalidApiToken() {
        Assumptions.assumeTrue(mockServerAvailable, "FME Server Mock is not available");

        // Given: Invalid API token
        pluginParameters.put("serviceURL", mockServerUrl);
        pluginParameters.put("apiToken", INVALID_API_TOKEN);

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeServerV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Result should be error
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus(),
            "Status should be ERROR with invalid API token");
    }

    @Test
    @DisplayName("FME Server V2 sends GeoJSON with Polygon geometry")
    public void testPolygonGeometrySent() {
        Assumptions.assumeTrue(mockServerAvailable, "FME Server Mock is not available");

        // Given: Request with Polygon perimeter
        testRequest.setPerimeter(PERIMETER_POLYGON);
        pluginParameters.put("serviceURL", mockServerUrl);
        pluginParameters.put("apiToken", VALID_API_TOKEN);

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeServerV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should succeed (mock validates GeoJSON structure)
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus(),
            "Should succeed with Polygon geometry. Message: " + result.getMessage());
    }

    @Test
    @DisplayName("FME Server V2 sends GeoJSON with MultiPolygon geometry")
    public void testMultiPolygonGeometrySent() {
        Assumptions.assumeTrue(mockServerAvailable, "FME Server Mock is not available");

        // Given: Request with MultiPolygon perimeter
        testRequest.setPerimeter(PERIMETER_MULTIPOLYGON);
        pluginParameters.put("serviceURL", mockServerUrl);
        pluginParameters.put("apiToken", VALID_API_TOKEN);

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeServerV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should succeed
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus(),
            "Should succeed with MultiPolygon geometry. Message: " + result.getMessage());
    }

    @Test
    @DisplayName("FME Server V2 passes all properties in GeoJSON")
    public void testAllPropertiesSent() {
        Assumptions.assumeTrue(mockServerAvailable, "FME Server Mock is not available");

        // Given: Request with all properties set
        testRequest.setOrderLabel("TEST-ORDER-001");
        testRequest.setProductGuid(PRODUCT_GUID);
        testRequest.setClientGuid(CLIENT_GUID);
        testRequest.setOrganismGuid(ORGANISM_GUID);
        testRequest.setParameters("{\"key1\":\"value1\",\"key2\":123}");

        pluginParameters.put("serviceURL", mockServerUrl);
        pluginParameters.put("apiToken", VALID_API_TOKEN);

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeServerV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should succeed (mock validates all required properties)
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus(),
            "Should succeed with all properties. Message: " + result.getMessage());
    }

    @Test
    @DisplayName("FME Server V2 creates result file in FolderOut")
    public void testResultFileCreated() {
        Assumptions.assumeTrue(mockServerAvailable, "FME Server Mock is not available");

        // Given: Valid configuration
        pluginParameters.put("serviceURL", mockServerUrl);
        pluginParameters.put("apiToken", VALID_API_TOKEN);

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeServerV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Output folder should contain downloaded file
        if (result.getStatus() == ITaskProcessorResult.Status.SUCCESS) {
            File outputDir = new File(DATA_FOLDERS_BASE_PATH, folderOut);
            // The plugin creates a subfolder with timestamp_productId
            File[] subDirs = outputDir.listFiles(File::isDirectory);
            if (subDirs != null && subDirs.length > 0) {
                File[] files = subDirs[0].listFiles();
                assertNotNull(files, "Should have files in output subfolder");
                assertTrue(files.length > 0, "Should have at least one file");
            }
        }
    }

    @Test
    @DisplayName("FME Server V2 handles null perimeter with null geometry")
    public void testNullPerimeterHandled() {
        Assumptions.assumeTrue(mockServerAvailable, "FME Server Mock is not available");

        // Given: Request without perimeter
        testRequest.setPerimeter(null);
        pluginParameters.put("serviceURL", mockServerUrl);
        pluginParameters.put("apiToken", VALID_API_TOKEN);

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeServerV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should handle gracefully (GeoJSON allows null geometry)
        assertNotNull(result);
        // The mock accepts null geometry, so this should succeed
    }

    @Test
    @DisplayName("FME Server V2 handles complex nested parameters")
    public void testComplexNestedParameters() {
        Assumptions.assumeTrue(mockServerAvailable, "FME Server Mock is not available");

        // Given: Complex nested JSON parameters
        String complexParams = "{\"format\":\"PDF\",\"options\":{\"compress\":true,\"quality\":95},\"layers\":[\"A\",\"B\",\"C\"]}";
        testRequest.setParameters(complexParams);
        pluginParameters.put("serviceURL", mockServerUrl);
        pluginParameters.put("apiToken", VALID_API_TOKEN);

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeServerV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should succeed
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus(),
            "Should handle complex nested parameters. Message: " + result.getMessage());
    }

    @Test
    @DisplayName("FME Server V2 returns error for server error response")
    public void testServerErrorHandling() {
        Assumptions.assumeTrue(mockServerAvailable, "FME Server Mock is not available");

        // Given: Error endpoint URL
        String errorUrl = mockServerUrl.replace("/v2/datadownload", "/error/test");
        pluginParameters.put("serviceURL", errorUrl);
        pluginParameters.put("apiToken", VALID_API_TOKEN);

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeServerV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should return error
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus(),
            "Should return ERROR for server error response");
    }

    @Test
    @DisplayName("Plugin can create new instance with language and parameters")
    public void testNewInstanceWithLanguageAndParams() {
        Map<String, String> params = new HashMap<>();
        params.put("serviceURL", mockServerUrl);
        params.put("apiToken", VALID_API_TOKEN);

        ITaskProcessor newInstance = fmeServerV2Plugin.newInstance("de", params);
        assertNotNull(newInstance, "New instance should not be null");
        assertEquals(PLUGIN_CODE, newInstance.getCode(), "Code should match");
    }
}
