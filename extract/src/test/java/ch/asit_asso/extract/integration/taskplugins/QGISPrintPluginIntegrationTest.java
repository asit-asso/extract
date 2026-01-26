/*
 * Copyright (C) 2017 arx iT
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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the QGIS Print plugin.
 * Tests plugin discovery, parameter validation, and metadata.
 * Note: Full execution tests require a running QGIS Server.
 */
@Tag("integration")
public class QGISPrintPluginIntegrationTest {

    private static final String APPLICATION_LANGUAGE = "fr";
    private static final String PLUGIN_CODE = "QGISPRINT";
    private static final String DATA_FOLDERS_BASE_PATH = "/tmp/extract-test-qgisprint-integration";
    private static final String TASK_PLUGINS_FOLDER_PATH = "src/main/resources/task_processors";
    private static final String PLUGIN_FILE_NAME_FILTER = "extract-task-qgisprint-*.jar";

    private static ITaskProcessor qgisprintPlugin;
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
        String orderFolderName = "ORDER-QGISPRINT-TEST";
        folderIn = Paths.get(orderFolderName, "input").toString();
        folderOut = Paths.get(orderFolderName, "output").toString();

        Path basePath = Paths.get(DATA_FOLDERS_BASE_PATH, orderFolderName);
        Files.createDirectories(basePath.resolve("input"));
        Files.createDirectories(basePath.resolve("output"));

        objectMapper = new ObjectMapper();

        testRequest = new Request();
        testRequest.setId(1);
        testRequest.setOrderLabel("ORDER-QGIS-001");
        testRequest.setOrderGuid("order-guid-qgis-test");
        testRequest.setProductLabel("Test Product");
        testRequest.setProductGuid("product-guid-test");
        testRequest.setClient("Test Client");
        testRequest.setClientGuid("client-guid-test");
        testRequest.setOrganism("Test Organism");
        testRequest.setOrganismGuid("organism-guid-test");
        testRequest.setRemark("Test remark");
        testRequest.setFolderIn(folderIn);
        testRequest.setFolderOut(folderOut);
        testRequest.setStatus(Request.Status.ONGOING);
        testRequest.setStartDate(new GregorianCalendar(2024, 2, 1, 9, 0, 0));
        testRequest.setEndDate(new GregorianCalendar(2024, 2, 15, 17, 30, 0));
        testRequest.setPerimeter("POLYGON((6.5 46.5, 6.6 46.5, 6.6 46.6, 6.5 46.6, 6.5 46.5))");
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
            throw new RuntimeException("QGIS Print plugin JAR not found. Build the project first.");
        }

        URL pluginUrl;
        try {
            assert foundPluginFiles != null;
            pluginUrl = new URL(String.format("jar:file:%s!/", foundPluginFiles[0].getAbsolutePath()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        taskPluginDiscoverer.setJarUrls(new URL[] { pluginUrl });
        qgisprintPlugin = taskPluginDiscoverer.getTaskProcessor(PLUGIN_CODE);
        assertNotNull(qgisprintPlugin, "QGIS Print plugin should be discovered");
    }

    @Test
    @DisplayName("Plugin is correctly discovered with expected code")
    public void testPluginDiscovery() {
        assertNotNull(qgisprintPlugin);
        assertEquals(PLUGIN_CODE, qgisprintPlugin.getCode());
    }

    @Test
    @DisplayName("Plugin has valid label and description")
    public void testPluginMetadata() {
        assertNotNull(qgisprintPlugin.getLabel());
        assertFalse(qgisprintPlugin.getLabel().isEmpty());

        assertNotNull(qgisprintPlugin.getDescription());
        assertFalse(qgisprintPlugin.getDescription().isEmpty());
    }

    @Test
    @DisplayName("Plugin has valid help content")
    public void testPluginHelp() {
        String help = qgisprintPlugin.getHelp();
        assertNotNull(help);
        assertFalse(help.isEmpty());
    }

    @Test
    @DisplayName("Plugin has valid pictogram class")
    public void testPluginPictogram() {
        String pictoClass = qgisprintPlugin.getPictoClass();
        assertNotNull(pictoClass);
        assertFalse(pictoClass.isEmpty());
    }

    @Test
    @DisplayName("Plugin returns valid JSON parameters")
    public void testPluginParameters() throws Exception {
        String params = qgisprintPlugin.getParams();
        assertNotNull(params);
        assertFalse(params.isEmpty());

        // Parse and validate JSON structure
        JsonNode paramsArray = objectMapper.readTree(params);
        assertTrue(paramsArray.isArray());
        assertTrue(paramsArray.size() > 0);

        // Each parameter should have required fields
        for (JsonNode param : paramsArray) {
            assertTrue(param.has("code"), "Parameter should have code");
            assertTrue(param.has("label"), "Parameter should have label");
            assertTrue(param.has("type"), "Parameter should have type");
        }
    }

    @Test
    @DisplayName("Plugin parameters include URL parameter")
    public void testPluginHasUrlParameter() throws Exception {
        String params = qgisprintPlugin.getParams();
        JsonNode paramsArray = objectMapper.readTree(params);

        boolean hasUrlParam = false;
        for (JsonNode param : paramsArray) {
            String code = param.get("code").asText();
            if (code.toLowerCase().contains("url")) {
                hasUrlParam = true;
                break;
            }
        }
        assertTrue(hasUrlParam, "Plugin should have a URL parameter for QGIS Server");
    }

    @Test
    @DisplayName("Plugin parameters include template/layout parameter")
    public void testPluginHasTemplateParameter() throws Exception {
        String params = qgisprintPlugin.getParams();
        JsonNode paramsArray = objectMapper.readTree(params);

        boolean hasTemplateParam = false;
        for (JsonNode param : paramsArray) {
            String code = param.get("code").asText().toLowerCase();
            if (code.contains("template") || code.contains("layout")) {
                hasTemplateParam = true;
                break;
            }
        }
        assertTrue(hasTemplateParam, "Plugin should have a template/layout parameter");
    }

    @Test
    @DisplayName("New instance creates independent copy")
    public void testNewInstanceIndependence() {
        Map<String, String> params1 = new HashMap<>();
        params1.put("url", "http://qgis-server-1/qgis");

        Map<String, String> params2 = new HashMap<>();
        params2.put("url", "http://qgis-server-2/qgis");

        ITaskProcessor instance1 = qgisprintPlugin.newInstance(APPLICATION_LANGUAGE, params1);
        ITaskProcessor instance2 = qgisprintPlugin.newInstance(APPLICATION_LANGUAGE, params2);

        assertNotSame(instance1, instance2);
        assertEquals(PLUGIN_CODE, instance1.getCode());
        assertEquals(PLUGIN_CODE, instance2.getCode());
    }

    @Test
    @DisplayName("New instance without parameters works")
    public void testNewInstanceWithoutParameters() {
        ITaskProcessor instance = qgisprintPlugin.newInstance(APPLICATION_LANGUAGE);

        assertNotNull(instance);
        assertEquals(PLUGIN_CODE, instance.getCode());
    }

    @Test
    @DisplayName("Plugin supports French language")
    public void testFrenchLanguageSupport() {
        ITaskProcessor frenchInstance = qgisprintPlugin.newInstance("fr");
        assertNotNull(frenchInstance);

        String label = frenchInstance.getLabel();
        assertNotNull(label);
        assertFalse(label.isEmpty());
    }

    @Test
    @DisplayName("Plugin supports German language")
    public void testGermanLanguageSupport() {
        ITaskProcessor germanInstance = qgisprintPlugin.newInstance("de");
        assertNotNull(germanInstance);

        String label = germanInstance.getLabel();
        assertNotNull(label);
        assertFalse(label.isEmpty());
    }

    @Test
    @DisplayName("Plugin handles invalid language gracefully")
    public void testInvalidLanguageHandling() {
        ITaskProcessor instance = qgisprintPlugin.newInstance("invalid-language");
        assertNotNull(instance);

        // Should fall back to default language
        String label = instance.getLabel();
        assertNotNull(label);
        assertFalse(label.isEmpty());
    }

    @Test
    @DisplayName("Plugin parameter types are valid")
    public void testPluginParameterTypes() throws Exception {
        String params = qgisprintPlugin.getParams();
        JsonNode paramsArray = objectMapper.readTree(params);

        String[] validTypes = {"text", "pass", "email", "multitext", "numeric", "boolean", "list", "list_msgs"};

        for (JsonNode param : paramsArray) {
            String type = param.get("type").asText();
            boolean isValidType = false;
            for (String validType : validTypes) {
                if (validType.equals(type)) {
                    isValidType = true;
                    break;
                }
            }
            assertTrue(isValidType, "Parameter type '" + type + "' should be valid");
        }
    }
}
