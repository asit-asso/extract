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
 * Integration tests for the Validation plugin.
 * Tests the plugin behavior when loaded through the plugin discovery mechanism.
 */
@Tag("integration")
public class ValidationPluginIntegrationTest {

    private static final String APPLICATION_LANGUAGE = "fr";
    private static final String PLUGIN_CODE = "VALIDATION";
    private static final String DATA_FOLDERS_BASE_PATH = "/tmp/extract-test-validation-integration";
    private static final String TASK_PLUGINS_FOLDER_PATH = "src/main/resources/task_processors";
    private static final String PLUGIN_FILE_NAME_FILTER = "extract-task-validation-*.jar";

    private static ITaskProcessor validationPlugin;
    private Request testRequest;
    private String folderIn;
    private String folderOut;

    @BeforeAll
    public static void initialize() {
        configurePlugin();
    }

    @BeforeEach
    public void setUp() throws IOException {
        String orderFolderName = "ORDER-VALIDATION-TEST";
        folderIn = Paths.get(orderFolderName, "input").toString();
        folderOut = Paths.get(orderFolderName, "output").toString();

        Path basePath = Paths.get(DATA_FOLDERS_BASE_PATH, orderFolderName);
        Files.createDirectories(basePath.resolve("input"));
        Files.createDirectories(basePath.resolve("output"));

        testRequest = new Request();
        testRequest.setId(1);
        testRequest.setOrderLabel("ORDER-VALIDATION-001");
        testRequest.setOrderGuid("order-guid-validation-test");
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
            throw new RuntimeException("Validation plugin JAR not found. Build the project first.");
        }

        URL pluginUrl;
        try {
            assert foundPluginFiles != null;
            pluginUrl = new URL(String.format("jar:file:%s!/", foundPluginFiles[0].getAbsolutePath()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        taskPluginDiscoverer.setJarUrls(new URL[] { pluginUrl });
        validationPlugin = taskPluginDiscoverer.getTaskProcessor(PLUGIN_CODE);
        assertNotNull(validationPlugin, "Validation plugin should be discovered");
    }

    @Test
    @DisplayName("Plugin is correctly discovered with expected code")
    public void testPluginDiscovery() {
        assertNotNull(validationPlugin);
        assertEquals(PLUGIN_CODE, validationPlugin.getCode());
    }

    @Test
    @DisplayName("Plugin has valid label and description")
    public void testPluginMetadata() {
        assertNotNull(validationPlugin.getLabel());
        assertFalse(validationPlugin.getLabel().isEmpty());

        assertNotNull(validationPlugin.getDescription());
        assertFalse(validationPlugin.getDescription().isEmpty());
    }

    @Test
    @DisplayName("Plugin has valid help content")
    public void testPluginHelp() {
        String help = validationPlugin.getHelp();
        assertNotNull(help);
        assertFalse(help.isEmpty());
    }

    @Test
    @DisplayName("Plugin returns valid JSON parameters")
    public void testPluginParameters() {
        String params = validationPlugin.getParams();
        assertNotNull(params);
        assertTrue(params.contains("validMessages") || params.contains("rejectMessages") || params.contains("list_msgs"));
    }

    @Test
    @DisplayName("Execute always returns STANDBY status")
    public void testExecuteReturnsStandby() {
        Map<String, String> taskParams = new HashMap<>();
        taskParams.put("validMessages", "Data validated|Quality check passed");
        taskParams.put("rejectMessages", "Data invalid|Missing fields");

        ITaskProcessor instance = validationPlugin.newInstance(APPLICATION_LANGUAGE, taskParams);
        TaskProcessorRequest processorRequest = new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH);

        ITaskProcessorResult result = instance.execute(processorRequest, null);

        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.STANDBY, result.getStatus());
    }

    @Test
    @DisplayName("Execute preserves original request unchanged")
    public void testExecutePreservesRequest() {
        Map<String, String> taskParams = new HashMap<>();

        ITaskProcessor instance = validationPlugin.newInstance(APPLICATION_LANGUAGE, taskParams);
        TaskProcessorRequest processorRequest = new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH);

        ITaskProcessorResult result = instance.execute(processorRequest, null);

        assertNotNull(result);
        assertNotNull(result.getRequestData());
        assertSame(processorRequest, result.getRequestData());
    }

    @Test
    @DisplayName("Execute without parameters returns STANDBY")
    public void testExecuteWithoutParameters() {
        ITaskProcessor instance = validationPlugin.newInstance(APPLICATION_LANGUAGE);
        TaskProcessorRequest processorRequest = new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH);

        ITaskProcessorResult result = instance.execute(processorRequest, null);

        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.STANDBY, result.getStatus());
    }

    @Test
    @DisplayName("Execute with empty parameters returns STANDBY")
    public void testExecuteWithEmptyParameters() {
        Map<String, String> taskParams = new HashMap<>();
        taskParams.put("validMessages", "");
        taskParams.put("rejectMessages", "");

        ITaskProcessor instance = validationPlugin.newInstance(APPLICATION_LANGUAGE, taskParams);
        TaskProcessorRequest processorRequest = new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH);

        ITaskProcessorResult result = instance.execute(processorRequest, null);

        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.STANDBY, result.getStatus());
    }

    @Test
    @DisplayName("New instance creates independent copy")
    public void testNewInstanceIndependence() {
        Map<String, String> params1 = new HashMap<>();
        params1.put("validMessages", "First validation");

        Map<String, String> params2 = new HashMap<>();
        params2.put("validMessages", "Second validation");

        ITaskProcessor instance1 = validationPlugin.newInstance(APPLICATION_LANGUAGE, params1);
        ITaskProcessor instance2 = validationPlugin.newInstance(APPLICATION_LANGUAGE, params2);

        assertNotSame(instance1, instance2);
        assertEquals(PLUGIN_CODE, instance1.getCode());
        assertEquals(PLUGIN_CODE, instance2.getCode());
    }

    @Test
    @DisplayName("Execute is stateless and repeatable")
    public void testExecuteStatelessness() {
        Map<String, String> taskParams = new HashMap<>();
        taskParams.put("validMessages", "Validated");

        ITaskProcessor instance = validationPlugin.newInstance(APPLICATION_LANGUAGE, taskParams);
        TaskProcessorRequest processorRequest = new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH);

        ITaskProcessorResult result1 = instance.execute(processorRequest, null);
        ITaskProcessorResult result2 = instance.execute(processorRequest, null);
        ITaskProcessorResult result3 = instance.execute(processorRequest, null);

        assertEquals(result1.getStatus(), result2.getStatus());
        assertEquals(result1.getStatus(), result3.getStatus());
        assertEquals(ITaskProcessorResult.Status.STANDBY, result1.getStatus());
    }

    @Test
    @DisplayName("Execute result has proper message")
    public void testExecuteResultMessage() {
        Map<String, String> taskParams = new HashMap<>();

        ITaskProcessor instance = validationPlugin.newInstance(APPLICATION_LANGUAGE, taskParams);
        TaskProcessorRequest processorRequest = new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH);

        ITaskProcessorResult result = instance.execute(processorRequest, null);

        assertNotNull(result.getMessage());
        assertFalse(result.getMessage().isEmpty());
    }

    @Test
    @DisplayName("Execute with null request handles gracefully")
    public void testExecuteWithNullRequest() {
        Map<String, String> taskParams = new HashMap<>();

        ITaskProcessor instance = validationPlugin.newInstance(APPLICATION_LANGUAGE, taskParams);

        ITaskProcessorResult result = instance.execute(null, null);

        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.STANDBY, result.getStatus());
    }
}
