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
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import ch.asit_asso.extract.plugins.implementation.TaskProcessorRequest;
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
 * Integration tests for the Remark plugin.
 * Tests the plugin behavior when loaded through the plugin discovery mechanism.
 */
@Tag("integration")
public class RemarkPluginIntegrationTest {

    private static final String APPLICATION_LANGUAGE = "fr";
    private static final String PLUGIN_CODE = "REMARK";
    private static final String DATA_FOLDERS_BASE_PATH = "/tmp/extract-test-remark-integration";
    private static final String TASK_PLUGINS_FOLDER_PATH = "src/main/resources/task_processors";
    private static final String PLUGIN_FILE_NAME_FILTER = "extract-task-remark-*.jar";

    private static ITaskProcessor remarkPlugin;
    private Request testRequest;
    private String folderIn;
    private String folderOut;

    @BeforeAll
    public static void initialize() {
        configurePlugin();
    }

    @BeforeEach
    public void setUp() throws IOException {
        String orderFolderName = "ORDER-REMARK-TEST";
        folderIn = Paths.get(orderFolderName, "input").toString();
        folderOut = Paths.get(orderFolderName, "output").toString();

        Path basePath = Paths.get(DATA_FOLDERS_BASE_PATH, orderFolderName);
        Files.createDirectories(basePath.resolve("input"));
        Files.createDirectories(basePath.resolve("output"));

        testRequest = new Request();
        testRequest.setId(1);
        testRequest.setOrderLabel("ORDER-REMARK-001");
        testRequest.setOrderGuid("order-guid-remark-test");
        testRequest.setProductLabel("Test Product");
        testRequest.setProductGuid("product-guid-test");
        testRequest.setClient("Test Client");
        testRequest.setClientGuid("client-guid-test");
        testRequest.setOrganism("Test Organism");
        testRequest.setOrganismGuid("organism-guid-test");
        testRequest.setRemark("Original remark");
        testRequest.setFolderIn(folderIn);
        testRequest.setFolderOut(folderOut);
        testRequest.setStatus(Request.Status.ONGOING);
        testRequest.setStartDate(new GregorianCalendar(2024, 2, 1, 9, 0, 0));
        testRequest.setEndDate(new GregorianCalendar(2024, 2, 15, 17, 30, 0));
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
            throw new RuntimeException("Remark plugin JAR not found. Build the project first.");
        }

        URL pluginUrl;
        try {
            assert foundPluginFiles != null;
            pluginUrl = new URL(String.format("jar:file:%s!/", foundPluginFiles[0].getAbsolutePath()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        taskPluginDiscoverer.setJarUrls(new URL[] { pluginUrl });
        remarkPlugin = taskPluginDiscoverer.getTaskProcessor(PLUGIN_CODE);
        assertNotNull(remarkPlugin, "Remark plugin should be discovered");
    }

    @Test
    @DisplayName("Plugin is correctly discovered with expected code")
    public void testPluginDiscovery() {
        assertNotNull(remarkPlugin);
        assertEquals(PLUGIN_CODE, remarkPlugin.getCode());
    }

    @Test
    @DisplayName("Plugin has valid label and description")
    public void testPluginMetadata() {
        assertNotNull(remarkPlugin.getLabel());
        assertFalse(remarkPlugin.getLabel().isEmpty());

        assertNotNull(remarkPlugin.getDescription());
        assertFalse(remarkPlugin.getDescription().isEmpty());
    }

    @Test
    @DisplayName("Plugin has valid help content")
    public void testPluginHelp() {
        String help = remarkPlugin.getHelp();
        assertNotNull(help);
        assertFalse(help.isEmpty());
    }

    @Test
    @DisplayName("Plugin returns valid JSON parameters")
    public void testPluginParameters() {
        String params = remarkPlugin.getParams();
        assertNotNull(params);
        assertTrue(params.contains("remark") || params.contains("overwrite"));
    }

    @Test
    @DisplayName("Execute with append mode adds to existing remark")
    public void testExecuteWithAppendMode() {
        String newRemark = "Additional automated remark";
        Map<String, String> taskParams = new HashMap<>();
        taskParams.put("remark", newRemark);
        taskParams.put("overwrite", "false");

        ITaskProcessor instance = remarkPlugin.newInstance(APPLICATION_LANGUAGE, taskParams);
        TaskProcessorRequest processorRequest = new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH);

        ITaskProcessorResult result = instance.execute(processorRequest, null);

        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        String updatedRemark = result.getRequestData().getRemark();
        assertTrue(updatedRemark.contains("Original remark"));
        assertTrue(updatedRemark.contains(newRemark));
    }

    @Test
    @DisplayName("Execute with overwrite mode replaces existing remark")
    public void testExecuteWithOverwriteMode() {
        String newRemark = "Replacement automated remark";
        Map<String, String> taskParams = new HashMap<>();
        taskParams.put("remark", newRemark);
        taskParams.put("overwrite", "true");

        ITaskProcessor instance = remarkPlugin.newInstance(APPLICATION_LANGUAGE, taskParams);
        TaskProcessorRequest processorRequest = new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH);

        ITaskProcessorResult result = instance.execute(processorRequest, null);

        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        String updatedRemark = result.getRequestData().getRemark();
        assertEquals(newRemark, updatedRemark);
    }

    @Test
    @DisplayName("Execute on empty remark sets new remark")
    public void testExecuteOnEmptyRemark() {
        testRequest.setRemark("");
        String newRemark = "New automated remark";
        Map<String, String> taskParams = new HashMap<>();
        taskParams.put("remark", newRemark);
        taskParams.put("overwrite", "false");

        ITaskProcessor instance = remarkPlugin.newInstance(APPLICATION_LANGUAGE, taskParams);
        TaskProcessorRequest processorRequest = new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH);

        ITaskProcessorResult result = instance.execute(processorRequest, null);

        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        assertEquals(newRemark, result.getRequestData().getRemark());
    }

    @Test
    @DisplayName("Execute on null remark sets new remark")
    public void testExecuteOnNullRemark() {
        testRequest.setRemark(null);
        String newRemark = "New automated remark";
        Map<String, String> taskParams = new HashMap<>();
        taskParams.put("remark", newRemark);
        taskParams.put("overwrite", "false");

        ITaskProcessor instance = remarkPlugin.newInstance(APPLICATION_LANGUAGE, taskParams);
        TaskProcessorRequest processorRequest = new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH);

        ITaskProcessorResult result = instance.execute(processorRequest, null);

        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        assertEquals(newRemark, result.getRequestData().getRemark());
    }

    @Test
    @DisplayName("New instance creates independent copy")
    public void testNewInstanceIndependence() {
        Map<String, String> params1 = new HashMap<>();
        params1.put("remark", "First remark");
        params1.put("overwrite", "false");

        Map<String, String> params2 = new HashMap<>();
        params2.put("remark", "Second remark");
        params2.put("overwrite", "true");

        ITaskProcessor instance1 = remarkPlugin.newInstance(APPLICATION_LANGUAGE, params1);
        ITaskProcessor instance2 = remarkPlugin.newInstance(APPLICATION_LANGUAGE, params2);

        assertNotSame(instance1, instance2);
        assertEquals(PLUGIN_CODE, instance1.getCode());
        assertEquals(PLUGIN_CODE, instance2.getCode());
    }

    @Test
    @DisplayName("Execute with special characters in remark succeeds")
    public void testExecuteWithSpecialCharacters() {
        String specialRemark = "Remarque automatique: äöü éèà ñç 漢字 <>&\"'";
        Map<String, String> taskParams = new HashMap<>();
        taskParams.put("remark", specialRemark);
        taskParams.put("overwrite", "true");

        ITaskProcessor instance = remarkPlugin.newInstance(APPLICATION_LANGUAGE, taskParams);
        TaskProcessorRequest processorRequest = new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH);

        ITaskProcessorResult result = instance.execute(processorRequest, null);

        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        assertEquals(specialRemark, result.getRequestData().getRemark());
    }

    @Test
    @DisplayName("Execute with multiline remark succeeds")
    public void testExecuteWithMultilineRemark() {
        String multilineRemark = "Line 1\nLine 2\nLine 3";
        Map<String, String> taskParams = new HashMap<>();
        taskParams.put("remark", multilineRemark);
        taskParams.put("overwrite", "true");

        ITaskProcessor instance = remarkPlugin.newInstance(APPLICATION_LANGUAGE, taskParams);
        TaskProcessorRequest processorRequest = new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH);

        ITaskProcessorResult result = instance.execute(processorRequest, null);

        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        assertEquals(multilineRemark, result.getRequestData().getRemark());
    }
}
