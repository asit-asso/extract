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
 * Functional tests for FME Desktop V1 plugin.
 * Tests full plugin execution with mock FME executable that validates command-line parameters.
 *
 * @author Extract Test Team
 */
@Tag("functional")
public class FmeDesktopV1PluginFunctionalTest {

    private static final String APPLICATION_LANGUAGE = "fr";
    private static final String PLUGIN_CODE = "FME2017";
    private static final String DATA_FOLDERS_BASE_PATH = "/tmp/extract-test-fmedesktopv1-functional";
    private static final String TASK_PLUGINS_FOLDER_PATH = "src/main/resources/task_processors";
    private static final String PLUGIN_FILE_NAME_FILTER = "extract-task-fmedesktop-*.jar";

    private static final String SUCCESS_WORKSPACE = "src/test/java/ch/asit_asso/extract/functional/taskplugins/fme_scripts/workspace_v1.fmw";
    private static final String FAILURE_WORKSPACE = "src/test/java/ch/asit_asso/extract/functional/taskplugins/fme_scripts/workspace_v1_fails.fmw";
    private static final String NO_FILES_WORKSPACE = "src/test/java/ch/asit_asso/extract/functional/taskplugins/fme_scripts/workspace_v1_nofiles.fmw";
    private static final String MOCK_EXECUTABLE = "src/test/java/ch/asit_asso/extract/functional/taskplugins/fme_scripts/fme_desktop_v1_mock.sh";

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

    private static final String PARAMETERS_JSON =
            "{\"FORMAT\":\"DXF\",\"PROJECTION\":\"SWITZERLAND95\",\"RAISON\":\"LOCALISATION\"," +
            "\"RAISON_LABEL\":\"Localisation en vue de projets\"," +
            "\"REMARK\":\"Ceci est un test\\nAvec retour à la ligne\"}";

    private static ITaskProcessor fmeDesktopV1Plugin;
    private Request testRequest;
    private Map<String, String> pluginParameters;
    private String folderIn;
    private String folderOut;

    @BeforeAll
    public static void initialize() {
        configurePlugin();
    }

    @BeforeEach
    public void setUp() throws IOException {
        String orderFolderName = "ORDER-FME-V1-TEST";
        // Relative paths for the request (TaskProcessorRequest combines with base path)
        folderIn = Paths.get(orderFolderName, "input").toString();
        folderOut = Paths.get(orderFolderName, "output").toString();

        // Create actual directories on filesystem using absolute paths
        Files.createDirectories(Paths.get(DATA_FOLDERS_BASE_PATH, folderIn));
        Files.createDirectories(Paths.get(DATA_FOLDERS_BASE_PATH, folderOut));

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
            throw new RuntimeException("FME Desktop V1 plugin JAR not found.");
        }

        URL pluginUrl;
        try {
            pluginUrl = new URL(String.format("jar:file:%s!/", foundPluginFiles[0].getAbsolutePath()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        taskPluginDiscoverer.setJarUrls(new URL[] { pluginUrl });
        fmeDesktopV1Plugin = taskPluginDiscoverer.getTaskProcessor(PLUGIN_CODE);
        assertNotNull(fmeDesktopV1Plugin, "FME Desktop V1 plugin should be discovered");
    }

    private void configureRequest() {
        testRequest = new Request();
        testRequest.setId(1);
        testRequest.setOrderLabel(ORDER_LABEL);
        testRequest.setOrderGuid("order-guid-fme-v1");
        testRequest.setProductLabel("Test Product FME V1");
        testRequest.setProductGuid(PRODUCT_GUID);
        testRequest.setClient("Test Client FME V1");
        testRequest.setClientGuid(CLIENT_GUID);
        testRequest.setOrganism("Test Organism FME V1");
        testRequest.setOrganismGuid(ORGANISM_GUID);
        testRequest.setFolderIn(folderIn);
        testRequest.setFolderOut(folderOut);
        testRequest.setPerimeter(PERIMETER_POLYGON);
        testRequest.setParameters(PARAMETERS_JSON);
        testRequest.setStatus(Request.Status.ONGOING);
    }

    @Test
    @DisplayName("FME Desktop V1 plugin is correctly discovered")
    public void testPluginDiscovery() {
        assertNotNull(fmeDesktopV1Plugin, "Plugin should be discovered");
        assertEquals(PLUGIN_CODE, fmeDesktopV1Plugin.getCode(), "Plugin code should match");
    }

    @Test
    @DisplayName("FME Desktop V1 basic execution succeeds with all parameters")
    public void testFmeDesktopV1BasicExecution() throws IOException {
        // Given: Plugin configured with mock executable
        pluginParameters.put("path", new File(SUCCESS_WORKSPACE).getAbsolutePath());
        pluginParameters.put("pathFME", new File(MOCK_EXECUTABLE).getAbsolutePath());
        pluginParameters.put("instances", "1");

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeDesktopV1Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Result should be success
        assertNotNull(result);
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus(),
            "Status should be SUCCESS. Message: " + result.getMessage());

        // Verify output file was created
        File outputDir = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderOut).toString());
        File[] outputFiles = outputDir.listFiles();
        assertNotNull(outputFiles, "Output directory should exist");
        assertTrue(outputFiles.length > 0, "Output directory should contain files");
    }

    @Test
    @DisplayName("FME Desktop V1 passes Perimeter parameter correctly")
    public void testPerimeterParameterPassed() throws IOException {
        // Given: Request with specific perimeter
        testRequest.setPerimeter(PERIMETER_POLYGON);
        pluginParameters.put("path", new File(SUCCESS_WORKSPACE).getAbsolutePath());
        pluginParameters.put("pathFME", new File(MOCK_EXECUTABLE).getAbsolutePath());
        pluginParameters.put("instances", "1");

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeDesktopV1Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Result should be success (mock validates parameters)
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus(),
            "Should succeed when perimeter is provided");

        // Verify output file contains perimeter info
        File outputFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderOut, "result_v1.txt").toString());
        assertTrue(outputFile.exists(), "Result file should exist");
        String content = new String(Files.readAllBytes(outputFile.toPath()));
        assertTrue(content.contains("Perimeter:"), "Result should contain perimeter info");
    }

    @Test
    @DisplayName("FME Desktop V1 passes Product parameter correctly")
    public void testProductParameterPassed() throws IOException {
        // Given: Request with specific product
        pluginParameters.put("path", new File(SUCCESS_WORKSPACE).getAbsolutePath());
        pluginParameters.put("pathFME", new File(MOCK_EXECUTABLE).getAbsolutePath());
        pluginParameters.put("instances", "1");

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeDesktopV1Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Check output file contains product
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        File outputFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderOut, "result_v1.txt").toString());
        String content = new String(Files.readAllBytes(outputFile.toPath()));
        assertTrue(content.contains("Product: " + PRODUCT_GUID), "Result should contain product GUID");
    }

    @Test
    @DisplayName("FME Desktop V1 passes OrderLabel parameter correctly")
    public void testOrderLabelParameterPassed() throws IOException {
        // Given: Request with order label
        pluginParameters.put("path", new File(SUCCESS_WORKSPACE).getAbsolutePath());
        pluginParameters.put("pathFME", new File(MOCK_EXECUTABLE).getAbsolutePath());
        pluginParameters.put("instances", "1");

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeDesktopV1Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Check output file contains order label
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        File outputFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderOut, "result_v1.txt").toString());
        String content = new String(Files.readAllBytes(outputFile.toPath()));
        assertTrue(content.contains("OrderLabel: " + ORDER_LABEL), "Result should contain order label");
    }

    @Test
    @DisplayName("FME Desktop V1 passes Client GUID parameter correctly")
    public void testClientGuidParameterPassed() throws IOException {
        // Given: Request with client GUID
        pluginParameters.put("path", new File(SUCCESS_WORKSPACE).getAbsolutePath());
        pluginParameters.put("pathFME", new File(MOCK_EXECUTABLE).getAbsolutePath());
        pluginParameters.put("instances", "1");

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeDesktopV1Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Check output file contains client GUID
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        File outputFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderOut, "result_v1.txt").toString());
        String content = new String(Files.readAllBytes(outputFile.toPath()));
        assertTrue(content.contains("ClientGuid: " + CLIENT_GUID), "Result should contain client GUID");
    }

    @Test
    @DisplayName("FME Desktop V1 passes Organism GUID parameter correctly")
    public void testOrganismGuidParameterPassed() throws IOException {
        // Given: Request with organism GUID
        pluginParameters.put("path", new File(SUCCESS_WORKSPACE).getAbsolutePath());
        pluginParameters.put("pathFME", new File(MOCK_EXECUTABLE).getAbsolutePath());
        pluginParameters.put("instances", "1");

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeDesktopV1Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Check output file contains organism GUID
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus());
        File outputFile = new File(Paths.get(DATA_FOLDERS_BASE_PATH, folderOut, "result_v1.txt").toString());
        String content = new String(Files.readAllBytes(outputFile.toPath()));
        assertTrue(content.contains("OrganismGuid: " + ORGANISM_GUID), "Result should contain organism GUID");
    }

    @Test
    @DisplayName("FME Desktop V1 handles workspace error gracefully")
    public void testWorkspaceError() {
        // Given: Workspace configured to fail
        pluginParameters.put("path", new File(FAILURE_WORKSPACE).getAbsolutePath());
        pluginParameters.put("pathFME", new File(MOCK_EXECUTABLE).getAbsolutePath());
        pluginParameters.put("instances", "1");

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeDesktopV1Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Result should be error
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus(),
            "Status should be ERROR when workspace fails");
    }

    @Test
    @DisplayName("FME Desktop V1 returns error when no output files generated")
    public void testNoOutputFiles() {
        // Given: Workspace configured to produce no files
        pluginParameters.put("path", new File(NO_FILES_WORKSPACE).getAbsolutePath());
        pluginParameters.put("pathFME", new File(MOCK_EXECUTABLE).getAbsolutePath());
        pluginParameters.put("instances", "1");

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeDesktopV1Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Result should be error (no files in output)
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus(),
            "Status should be ERROR when no output files are generated");
    }

    @Test
    @DisplayName("FME Desktop V1 returns error when workspace file not found")
    public void testWorkspaceNotFound() {
        // Given: Non-existent workspace path
        pluginParameters.put("path", "/non/existent/workspace.fmw");
        pluginParameters.put("pathFME", new File(MOCK_EXECUTABLE).getAbsolutePath());
        pluginParameters.put("instances", "1");

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeDesktopV1Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Result should be error
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus(),
            "Status should be ERROR when workspace not found");
    }

    @Test
    @DisplayName("FME Desktop V1 returns error when executable not found")
    public void testExecutableNotFound() {
        // Given: Non-existent executable path
        pluginParameters.put("path", new File(SUCCESS_WORKSPACE).getAbsolutePath());
        pluginParameters.put("pathFME", "/non/existent/fme.exe");
        pluginParameters.put("instances", "1");

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeDesktopV1Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Result should be error
        assertEquals(ITaskProcessorResult.Status.ERROR, result.getStatus(),
            "Status should be ERROR when FME executable not found");
    }

    @Test
    @DisplayName("FME Desktop V1 handles null perimeter")
    public void testNullPerimeter() throws IOException {
        // Given: Request without perimeter
        testRequest.setPerimeter(null);
        pluginParameters.put("path", new File(SUCCESS_WORKSPACE).getAbsolutePath());
        pluginParameters.put("pathFME", new File(MOCK_EXECUTABLE).getAbsolutePath());
        pluginParameters.put("instances", "1");

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeDesktopV1Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should handle gracefully (mock accepts null perimeter)
        // Note: The actual behavior depends on the mock implementation
        assertNotNull(result);
    }

    @Test
    @DisplayName("FME Desktop V1 handles JSON parameters correctly")
    public void testJsonParameters() throws IOException {
        // Given: Request with JSON parameters
        String jsonParams = "{\"FORMAT\":\"PDF\",\"QUALITY\":\"HIGH\",\"SCALE\":10000}";
        testRequest.setParameters(jsonParams);
        pluginParameters.put("path", new File(SUCCESS_WORKSPACE).getAbsolutePath());
        pluginParameters.put("pathFME", new File(MOCK_EXECUTABLE).getAbsolutePath());
        pluginParameters.put("instances", "1");

        // When: Executing the plugin
        ITaskProcessor pluginInstance = fmeDesktopV1Plugin.newInstance(APPLICATION_LANGUAGE, pluginParameters);
        ITaskProcessorResult result = pluginInstance.execute(
            new TaskProcessorRequest(testRequest, DATA_FOLDERS_BASE_PATH),
            null
        );

        // Then: Should succeed
        assertEquals(ITaskProcessorResult.Status.SUCCESS, result.getStatus(),
            "Should handle JSON parameters correctly");
    }

    @Test
    @DisplayName("Plugin has correct parameter structure")
    public void testPluginParameterStructure() {
        String params = fmeDesktopV1Plugin.getParams();
        assertNotNull(params, "Params should not be null");
        assertTrue(params.contains("path"), "Should have 'path' parameter");
        assertTrue(params.contains("pathFME"), "Should have 'pathFME' parameter");
        assertTrue(params.contains("instances"), "Should have 'instances' parameter");
    }

    @Test
    @DisplayName("Plugin has correct label and description")
    public void testPluginLabelAndDescription() {
        assertNotNull(fmeDesktopV1Plugin.getLabel(), "Label should not be null");
        assertNotNull(fmeDesktopV1Plugin.getDescription(), "Description should not be null");
        assertFalse(fmeDesktopV1Plugin.getLabel().isEmpty(), "Label should not be empty");
        assertFalse(fmeDesktopV1Plugin.getDescription().isEmpty(), "Description should not be empty");
    }
}
