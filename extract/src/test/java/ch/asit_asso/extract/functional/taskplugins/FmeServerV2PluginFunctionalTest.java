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
 * Functional tests for FME Server V2 plugin (Issue #353).
 * Tests plugin discovery, parameter validation, URL security, and error handling.
 *
 * Note: These tests do not require an actual FME Server as they focus on:
 * - Plugin configuration and discovery
 * - Parameter validation
 * - URL validation (SSRF protection)
 * - Error handling for missing/invalid parameters
 *
 * @author Bruno Alves
 */
@Tag("functional")
public class FmeServerV2PluginFunctionalTest {

    private static final String APPLICATION_LANGUAGE = "fr";
    private static final String PLUGIN_CODE = "FMESERVERV2";
    private static final String DATA_FOLDERS_BASE_PATH = "/tmp/extract-test-fmeserverv2-functional";
    private static final String TASK_PLUGINS_FOLDER_PATH = "src/main/resources/task_processors";
    private static final String PLUGIN_FILE_NAME_FILTER = "extract-task-fmeserver-v2-*.jar";

    private static final String CLIENT_GUID = "4b01553d-9766-4014-9166-3f00f58adfc7";
    private static final String ORDER_LABEL = "SERVER-V2-TEST";
    private static final String ORGANISM_GUID = "a35f0327-bceb-43a1-b366-96c3a94bc47b";
    private static final String PRODUCT_GUID = "a8405d50-f712-4e3e-96b2-a5452cf4e03e";

    private static final String PERIMETER_POLYGON =
            "POLYGON((6.5 46.5, 6.6 46.5, 6.6 46.6, 6.5 46.6, 6.5 46.5))";

    private static final String PARAMETERS_JSON =
            "{\"FORMAT\":\"DXF\",\"PROJECTION\":\"EPSG:2056\"}";

    // Test API token (fake)
    private static final String TEST_API_TOKEN = "abcdefghij1234567890";

    private static ITaskProcessor fmeServerV2Plugin;
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
            throw new RuntimeException("FME Server V2 plugin JAR not found.");
        }

        URL pluginUrl;
        try {
            assert foundPluginFiles != null;
            pluginUrl = new URL(String.format("jar:file:%s!/", foundPluginFiles[0].getAbsolutePath()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        taskPluginDiscoverer.setJarUrls(new URL[] { pluginUrl });
        fmeServerV2Plugin = taskPluginDiscoverer.getTaskProcessor(PLUGIN_CODE);
        assertNotNull(fmeServerV2Plugin, "FME Server V2 plugin should be discovered");
    }

    private void configureRequest() {
        testRequest = new Request();
        testRequest.setId(1);
        testRequest.setOrderLabel(ORDER_LABEL);
        testRequest.setOrderGuid("order-guid-server-v2");
        testRequest.setProductLabel("Test Product Server V2");
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
    @DisplayName("FME Server V2 plugin is correctly discovered")
    public void testPluginDiscovery() {
        assertNotNull(fmeServerV2Plugin, "Plugin should be discovered");
        assertEquals(PLUGIN_CODE, fmeServerV2Plugin.getCode(), "Plugin code should match");
    }

    @Test
    @DisplayName("Plugin has correct parameter structure with serviceURL and apiToken")
    public void testPluginParameterStructure() throws Exception {
        String params = fmeServerV2Plugin.getParams();
        assertNotNull(params, "Params should not be null");

        JsonNode paramsJson = objectMapper.readTree(params);
        assertTrue(paramsJson.isArray(), "Params should be an array");

        // Check serviceURL parameter
        boolean hasServiceUrl = false;
        boolean hasApiToken = false;

        for (JsonNode param : paramsJson) {
            String code = param.get("code").asText();
            if ("serviceURL".equals(code)) {
                hasServiceUrl = true;
                assertTrue(param.get("req").asBoolean(), "serviceURL should be required");
            }
            if ("apiToken".equals(code)) {
                hasApiToken = true;
                assertTrue(param.get("req").asBoolean(), "apiToken should be required");
                assertEquals("pass", param.get("type").asText(), "apiToken should be password type");
            }
        }

        assertTrue(hasServiceUrl, "Should have serviceURL parameter");
        assertTrue(hasApiToken, "Should have apiToken parameter");
    }

    @Test
    @DisplayName("Plugin returns error when no parameters provided")
    public void testMissingParameters() {
        // Given: Empty parameters
        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeServerV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should return error
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus(),
            "Status should be ERROR when no parameters");
    }

    @Test
    @DisplayName("Plugin returns error when serviceURL is missing")
    public void testMissingServiceUrl() {
        // Given: Only apiToken, no serviceURL
        pluginParameters.put("apiToken", TEST_API_TOKEN);

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeServerV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should return error
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus(),
            "Status should be ERROR when serviceURL missing");
    }

    @Test
    @DisplayName("Plugin returns error when apiToken is missing")
    public void testMissingApiToken() {
        // Given: Only serviceURL, no apiToken
        pluginParameters.put("serviceURL", "https://fme.example.com/fmedatadownload/service");

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeServerV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should return error
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus(),
            "Status should be ERROR when apiToken missing");
    }

    @Test
    @DisplayName("Plugin rejects localhost URLs (SSRF protection)")
    public void testRejectsLocalhostUrl() {
        // Given: Localhost URL
        pluginParameters.put("serviceURL", "http://localhost:8080/fme");
        pluginParameters.put("apiToken", TEST_API_TOKEN);

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeServerV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should return error (SSRF protection)
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus(),
            "Status should be ERROR for localhost URL (SSRF protection)");
    }

    @Test
    @DisplayName("Plugin rejects 127.0.0.1 URLs (SSRF protection)")
    public void testRejects127Url() {
        // Given: 127.0.0.1 URL
        pluginParameters.put("serviceURL", "http://127.0.0.1:8080/fme");
        pluginParameters.put("apiToken", TEST_API_TOKEN);

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeServerV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should return error
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus(),
            "Status should be ERROR for 127.0.0.1 URL (SSRF protection)");
    }

    @Test
    @DisplayName("Plugin rejects private network 10.x.x.x URLs (SSRF protection)")
    public void testRejectsPrivateNetwork10() {
        // Given: Private network URL
        pluginParameters.put("serviceURL", "http://10.0.0.1:8080/fme");
        pluginParameters.put("apiToken", TEST_API_TOKEN);

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeServerV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should return error
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus(),
            "Status should be ERROR for 10.x.x.x URL (SSRF protection)");
    }

    @Test
    @DisplayName("Plugin rejects private network 192.168.x.x URLs (SSRF protection)")
    public void testRejectsPrivateNetwork192() {
        // Given: Private network URL
        pluginParameters.put("serviceURL", "http://192.168.1.1:8080/fme");
        pluginParameters.put("apiToken", TEST_API_TOKEN);

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeServerV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should return error
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus(),
            "Status should be ERROR for 192.168.x.x URL (SSRF protection)");
    }

    @Test
    @DisplayName("Plugin rejects private network 172.16.x.x URLs (SSRF protection)")
    public void testRejectsPrivateNetwork172() {
        // Given: Private network URL
        pluginParameters.put("serviceURL", "http://172.16.0.1:8080/fme");
        pluginParameters.put("apiToken", TEST_API_TOKEN);

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeServerV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should return error
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus(),
            "Status should be ERROR for 172.16.x.x URL (SSRF protection)");
    }

    @Test
    @DisplayName("Plugin rejects invalid protocol (file://) URLs")
    public void testRejectsFileProtocol() {
        // Given: File protocol URL
        pluginParameters.put("serviceURL", "file:///etc/passwd");
        pluginParameters.put("apiToken", TEST_API_TOKEN);

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeServerV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should return error
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus(),
            "Status should be ERROR for file:// protocol");
    }

    @Test
    @DisplayName("Plugin rejects malformed URLs")
    public void testRejectsMalformedUrl() {
        // Given: Malformed URL
        pluginParameters.put("serviceURL", "not-a-valid-url");
        pluginParameters.put("apiToken", TEST_API_TOKEN);

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeServerV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should return error
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus(),
            "Status should be ERROR for malformed URL");
    }

    @Test
    @DisplayName("Plugin rejects too short API token")
    public void testRejectsTooShortApiToken() {
        // Given: Too short API token
        pluginParameters.put("serviceURL", "https://fme.example.com/fmedatadownload/service");
        pluginParameters.put("apiToken", "short");

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeServerV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should return error (token too short)
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus(),
            "Status should be ERROR for too short API token");
    }

    @Test
    @DisplayName("Plugin returns error when request is null")
    public void testNullRequest() {
        // Given: Valid parameters but null request
        pluginParameters.put("serviceURL", "https://fme.example.com/fmedatadownload/service");
        pluginParameters.put("apiToken", TEST_API_TOKEN);

        // When: Executing with null request
        ITaskProcessor pluginInstance = fmeServerV2Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(null, null);

        // Then: Should return error
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus(),
            "Status should be ERROR for null request");
    }

    @Test
    @DisplayName("Plugin has correct label and description")
    public void testPluginLabelAndDescription() {
        assertNotNull(fmeServerV2Plugin.getLabel(), "Label should not be null");
        assertNotNull(fmeServerV2Plugin.getDescription(), "Description should not be null");
        assertFalse(fmeServerV2Plugin.getLabel().isEmpty(), "Label should not be empty");
        assertFalse(fmeServerV2Plugin.getDescription().isEmpty(), "Description should not be empty");
    }

    @Test
    @DisplayName("Plugin has icon class defined")
    public void testPluginPictoClass() {
        assertNotNull(fmeServerV2Plugin.getPictoClass(), "Picto class should not be null");
        assertFalse(fmeServerV2Plugin.getPictoClass().isEmpty(), "Picto class should not be empty");
    }

    @Test
    @DisplayName("Plugin can create new instance with language")
    public void testNewInstanceWithLanguage() {
        ITaskProcessor newInstance = fmeServerV2Plugin.newInstance("en");
        assertNotNull(newInstance, "New instance should not be null");
        assertEquals(PLUGIN_CODE, newInstance.getCode(), "Code should match");
    }

    @Test
    @DisplayName("Plugin can create new instance with language and parameters")
    public void testNewInstanceWithLanguageAndParams() {
        Map<String, String> params = new HashMap<>();
        params.put("serviceURL", "https://fme.example.com/fmedatadownload/service");
        params.put("apiToken", TEST_API_TOKEN);

        ITaskProcessor newInstance = fmeServerV2Plugin.newInstance("de", params);
        assertNotNull(newInstance, "New instance should not be null");
        assertEquals(PLUGIN_CODE, newInstance.getCode(), "Code should match");
    }
}
