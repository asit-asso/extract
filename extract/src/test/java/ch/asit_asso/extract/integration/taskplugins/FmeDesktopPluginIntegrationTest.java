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

import ch.asit_asso.extract.plugins.TaskProcessorsDiscoverer;
import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the FME Desktop plugin.
 * Tests plugin discovery, parameter validation, and metadata.
 * Note: Full execution tests require a running FME Desktop installation.
 */
@Tag("integration")
public class FmeDesktopPluginIntegrationTest {

    private static final String APPLICATION_LANGUAGE = "fr";
    private static final String PLUGIN_CODE = "FME2017";
    private static final String TASK_PLUGINS_FOLDER_PATH = "src/main/resources/task_processors";
    private static final String PLUGIN_FILE_NAME_FILTER = "extract-task-fmedesktop-*.jar";

    private static ITaskProcessor fmeDesktopPlugin;
    private ObjectMapper objectMapper;

    @BeforeAll
    public static void initialize() {
        configurePlugin();
    }

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
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
            throw new RuntimeException("FME Desktop plugin JAR not found. Build the project first.");
        }

        URL pluginUrl;
        try {
            assert foundPluginFiles != null;
            pluginUrl = new URL(String.format("jar:file:%s!/", foundPluginFiles[0].getAbsolutePath()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        taskPluginDiscoverer.setJarUrls(new URL[] { pluginUrl });
        fmeDesktopPlugin = taskPluginDiscoverer.getTaskProcessor(PLUGIN_CODE);
        assertNotNull(fmeDesktopPlugin, "FME Desktop plugin should be discovered");
    }

    @Test
    @DisplayName("Plugin is correctly discovered with expected code")
    public void testPluginDiscovery() {
        assertNotNull(fmeDesktopPlugin);
        assertEquals(PLUGIN_CODE, fmeDesktopPlugin.getCode());
    }

    @Test
    @DisplayName("Plugin has valid label and description")
    public void testPluginMetadata() {
        assertNotNull(fmeDesktopPlugin.getLabel());
        assertFalse(fmeDesktopPlugin.getLabel().isEmpty());

        assertNotNull(fmeDesktopPlugin.getDescription());
        assertFalse(fmeDesktopPlugin.getDescription().isEmpty());
    }

    @Test
    @DisplayName("Plugin has valid help content")
    public void testPluginHelp() {
        String help = fmeDesktopPlugin.getHelp();
        assertNotNull(help);
        assertFalse(help.isEmpty());
    }

    @Test
    @DisplayName("Plugin has valid pictogram class")
    public void testPluginPictogram() {
        String pictoClass = fmeDesktopPlugin.getPictoClass();
        assertNotNull(pictoClass);
        assertFalse(pictoClass.isEmpty());
        assertEquals("fa-cogs", pictoClass);
    }

    @Test
    @DisplayName("Plugin returns valid JSON parameters")
    public void testPluginParameters() throws Exception {
        String params = fmeDesktopPlugin.getParams();
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
    @DisplayName("Plugin parameters include FME path parameter")
    public void testPluginHasFmePathParameter() throws Exception {
        String params = fmeDesktopPlugin.getParams();
        JsonNode paramsArray = objectMapper.readTree(params);

        boolean hasFmePathParam = false;
        for (JsonNode param : paramsArray) {
            String code = param.get("code").asText();
            if (code.toLowerCase().contains("pathfme") || code.toLowerCase().contains("fme")) {
                hasFmePathParam = true;
                break;
            }
        }
        assertTrue(hasFmePathParam, "Plugin should have a FME path parameter");
    }

    @Test
    @DisplayName("Plugin parameters include workspace path parameter")
    public void testPluginHasWorkspacePathParameter() throws Exception {
        String params = fmeDesktopPlugin.getParams();
        JsonNode paramsArray = objectMapper.readTree(params);

        boolean hasPathParam = false;
        for (JsonNode param : paramsArray) {
            String code = param.get("code").asText();
            if (code.equals("path")) {
                hasPathParam = true;
                break;
            }
        }
        assertTrue(hasPathParam, "Plugin should have a workspace path parameter");
    }

    @Test
    @DisplayName("Plugin parameters include instances parameter")
    public void testPluginHasInstancesParameter() throws Exception {
        String params = fmeDesktopPlugin.getParams();
        JsonNode paramsArray = objectMapper.readTree(params);

        boolean hasInstancesParam = false;
        for (JsonNode param : paramsArray) {
            String code = param.get("code").asText();
            if (code.toLowerCase().contains("instances")) {
                hasInstancesParam = true;
                break;
            }
        }
        assertTrue(hasInstancesParam, "Plugin should have an instances parameter");
    }

    @Test
    @DisplayName("New instance creates independent copy")
    public void testNewInstanceIndependence() {
        Map<String, String> params1 = new HashMap<>();
        params1.put("path", "/path/to/workspace1.fmw");

        Map<String, String> params2 = new HashMap<>();
        params2.put("path", "/path/to/workspace2.fmw");

        ITaskProcessor instance1 = fmeDesktopPlugin.newInstance(APPLICATION_LANGUAGE, params1);
        ITaskProcessor instance2 = fmeDesktopPlugin.newInstance(APPLICATION_LANGUAGE, params2);

        assertNotSame(instance1, instance2);
        assertEquals(PLUGIN_CODE, instance1.getCode());
        assertEquals(PLUGIN_CODE, instance2.getCode());
    }

    @Test
    @DisplayName("New instance without parameters works")
    public void testNewInstanceWithoutParameters() {
        ITaskProcessor instance = fmeDesktopPlugin.newInstance(APPLICATION_LANGUAGE);

        assertNotNull(instance);
        assertEquals(PLUGIN_CODE, instance.getCode());
    }

    @Test
    @DisplayName("Plugin supports French language")
    public void testFrenchLanguageSupport() {
        ITaskProcessor frenchInstance = fmeDesktopPlugin.newInstance("fr");
        assertNotNull(frenchInstance);

        String label = frenchInstance.getLabel();
        assertNotNull(label);
        assertFalse(label.isEmpty());
    }

    @Test
    @DisplayName("Plugin supports German language")
    public void testGermanLanguageSupport() {
        ITaskProcessor germanInstance = fmeDesktopPlugin.newInstance("de");
        assertNotNull(germanInstance);

        String label = germanInstance.getLabel();
        assertNotNull(label);
        assertFalse(label.isEmpty());
    }

    @Test
    @DisplayName("Plugin supports English language")
    public void testEnglishLanguageSupport() {
        ITaskProcessor englishInstance = fmeDesktopPlugin.newInstance("en");
        assertNotNull(englishInstance);

        String label = englishInstance.getLabel();
        assertNotNull(label);
        assertFalse(label.isEmpty());
    }

    @Test
    @DisplayName("Plugin handles invalid language gracefully")
    public void testInvalidLanguageHandling() {
        ITaskProcessor instance = fmeDesktopPlugin.newInstance("invalid-language");
        assertNotNull(instance);

        // Should fall back to default language
        String label = instance.getLabel();
        assertNotNull(label);
        assertFalse(label.isEmpty());
    }

    @Test
    @DisplayName("Plugin parameter types are valid")
    public void testPluginParameterTypes() throws Exception {
        String params = fmeDesktopPlugin.getParams();
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
