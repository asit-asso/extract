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
 * Integration tests for the Archive plugin.
 * Tests plugin discovery, parameter validation, and metadata.
 */
@Tag("integration")
public class ArchivePluginIntegrationTest {

    private static final String APPLICATION_LANGUAGE = "fr";
    private static final String PLUGIN_CODE = "ARCHIVE";
    private static final String TASK_PLUGINS_FOLDER_PATH = "src/main/resources/task_processors";
    private static final String PLUGIN_FILE_NAME_FILTER = "extract-task-archive-*.jar";

    private static ITaskProcessor archivePlugin;
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
            throw new RuntimeException("Archive plugin JAR not found. Build the project first.");
        }

        URL pluginUrl;
        try {
            assert foundPluginFiles != null;
            pluginUrl = new URL(String.format("jar:file:%s!/", foundPluginFiles[0].getAbsolutePath()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        taskPluginDiscoverer.setJarUrls(new URL[] { pluginUrl });
        archivePlugin = taskPluginDiscoverer.getTaskProcessor(PLUGIN_CODE);
        assertNotNull(archivePlugin, "Archive plugin should be discovered");
    }

    @Test
    @DisplayName("Plugin is correctly discovered with expected code")
    public void testPluginDiscovery() {
        assertNotNull(archivePlugin);
        assertEquals(PLUGIN_CODE, archivePlugin.getCode());
    }

    @Test
    @DisplayName("Plugin has valid label and description")
    public void testPluginMetadata() {
        assertNotNull(archivePlugin.getLabel());
        assertFalse(archivePlugin.getLabel().isEmpty());

        assertNotNull(archivePlugin.getDescription());
        assertFalse(archivePlugin.getDescription().isEmpty());
    }

    @Test
    @DisplayName("Plugin has valid help content")
    public void testPluginHelp() {
        String help = archivePlugin.getHelp();
        assertNotNull(help);
        assertFalse(help.isEmpty());
    }

    @Test
    @DisplayName("Plugin has valid pictogram class")
    public void testPluginPictogram() {
        String pictoClass = archivePlugin.getPictoClass();
        assertNotNull(pictoClass);
        assertFalse(pictoClass.isEmpty());
    }

    @Test
    @DisplayName("Plugin returns valid JSON parameters")
    public void testPluginParameters() throws Exception {
        String params = archivePlugin.getParams();
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
    @DisplayName("Plugin parameters include path parameter")
    public void testPluginHasPathParameter() throws Exception {
        String params = archivePlugin.getParams();
        JsonNode paramsArray = objectMapper.readTree(params);

        boolean hasPathParam = false;
        for (JsonNode param : paramsArray) {
            String code = param.get("code").asText();
            if (code.toLowerCase().contains("path")) {
                hasPathParam = true;
                break;
            }
        }
        assertTrue(hasPathParam, "Plugin should have a path parameter for archive destination");
    }

    @Test
    @DisplayName("New instance creates independent copy")
    public void testNewInstanceIndependence() {
        Map<String, String> params1 = new HashMap<>();
        params1.put("path", "/archive/path1");

        Map<String, String> params2 = new HashMap<>();
        params2.put("path", "/archive/path2");

        ITaskProcessor instance1 = archivePlugin.newInstance(APPLICATION_LANGUAGE, params1);
        ITaskProcessor instance2 = archivePlugin.newInstance(APPLICATION_LANGUAGE, params2);

        assertNotSame(instance1, instance2);
        assertEquals(PLUGIN_CODE, instance1.getCode());
        assertEquals(PLUGIN_CODE, instance2.getCode());
    }

    @Test
    @DisplayName("New instance without parameters works")
    public void testNewInstanceWithoutParameters() {
        ITaskProcessor instance = archivePlugin.newInstance(APPLICATION_LANGUAGE);

        assertNotNull(instance);
        assertEquals(PLUGIN_CODE, instance.getCode());
    }

    @Test
    @DisplayName("Plugin supports French language")
    public void testFrenchLanguageSupport() {
        ITaskProcessor frenchInstance = archivePlugin.newInstance("fr");
        assertNotNull(frenchInstance);

        String label = frenchInstance.getLabel();
        assertNotNull(label);
        assertFalse(label.isEmpty());
    }

    @Test
    @DisplayName("Plugin supports German language")
    public void testGermanLanguageSupport() {
        ITaskProcessor germanInstance = archivePlugin.newInstance("de");
        assertNotNull(germanInstance);

        String label = germanInstance.getLabel();
        assertNotNull(label);
        assertFalse(label.isEmpty());
    }

    @Test
    @DisplayName("Plugin handles invalid language gracefully")
    public void testInvalidLanguageHandling() {
        ITaskProcessor instance = archivePlugin.newInstance("invalid-language");
        assertNotNull(instance);

        // Should fall back to default language
        String label = instance.getLabel();
        assertNotNull(label);
        assertFalse(label.isEmpty());
    }

    @Test
    @DisplayName("Plugin parameter types are valid")
    public void testPluginParameterTypes() throws Exception {
        String params = archivePlugin.getParams();
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
