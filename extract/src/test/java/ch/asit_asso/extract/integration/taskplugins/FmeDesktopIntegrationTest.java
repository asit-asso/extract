package ch.asit_asso.extract.integration.taskplugins;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.plugins.TaskProcessorsDiscoverer;
import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import ch.asit_asso.extract.plugins.implementation.TaskProcessorRequest;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Tag("integration")
public class FmeDesktopIntegrationTest {

    public static final String EXPECTED_SUCCESS_MESSAGE = "OK";

    public static final String EXPECTED_SUCCESS_ERROR_CODE = "";

    public static final ITaskProcessorResult.Status EXPECTED_SUCCESS_STATUS = ITaskProcessorResult.Status.SUCCESS;

    public static final String EXPECTED_FR_NO_FILES_MESSAGE = "Le répertoire sortie est vide ou n'existe pas.";

    public static final ITaskProcessorResult.Status EXPECTED_ERROR_STATUS = ITaskProcessorResult.Status.ERROR;

    private Request testRequest;

    private static ITaskProcessor fmeDesktopPlugin;

    private static final String APPLICATION_LANGUAGE = "fr";

    private static final String PLUGIN_CODE = "FME2017";

    private HashMap<String, String> plugin_parameters;

    private static final String DATA_FOLDERS_BASE_PATH = "/var/extract/orders";

    private static final String SUCCESS_WORKSPACE = "src/test/java/ch/asit_asso/extract/integration/taskplugins/my_workspace.fmw";

    private static final String FAILING_WORKSPACE = "src/test/java/ch/asit_asso/extract/integration/taskplugins/my_workspace_fails.fmw";

    private static final String NO_FILES_WORKSPACE = "src/test/java/ch/asit_asso/extract/integration/taskplugins/my_workspace_nofiles.fmw";

    private static final String FME_MOCK_LINUX = "src/test/java/ch/asit_asso/extract/integration/taskplugins/FmeDesktopTest";

    private static final String FME_MOCK_WINDOWS = "src/test/java/ch/asit_asso/extract/integration/taskplugins/FmeDesktopTestWindows";

    private static final String INPUT_FOLDER = "5f258673-e743-475f-93a4-f466f6be3031/input";

    private static final String NUM_INSTANCES = "1";

    private static final String OUTPUT_FOLDER = "5f258673-e743-475f-93a4-f466f6be3031/output";

    private static final String CLIENT_GUID = "4b01553d-9766-4014-9166-3f00f58adfc7";

    private static final String ORDER_LABEL = "443530";

    private static final String ORGANISM_GUID = "a35f0327-bceb-43a1-b366-96c3a94bc47b";

    private static final String PARAMETERS_JSON
            = "{\"FORMAT\":\"DXF\",\"PROJECTION\":\"SWITZERLAND95\",\"RAISON\":\"LOCALISATION\""
            + ",\"RAISON_LABEL\":\"Localisation en vue de projets\""
            + ",\"REMARK\":\"Ceci est un test\\nAvec retour à la ligne\"}";

    private static final String PERIMETER_POLYGON
            = "POLYGON((7.008802763251656 46.245519329293245,7.008977478638646 46.24596978223839,7.010099318044382"
            + " 46.24634512591109,7.011161356635566 46.24649533820254,7.011851394695592"
            + " 46.24654742881326,7.012123110524144 46.24662042289713,7.012329750692657"
            + " 46.246724655380014,7.012417623228246 46.24668000889588,7.012559036117633"
            + " 46.24642191589558,7.012535717792058 46.246088985456616,7.012514122624683"
            + " 46.245949469899564,7.012472496413521 46.245884093468234,7.012185407319924"
            + " 46.24570534214322,7.01217302489515 46.24563108046702,7.011217983680352"
            + " 46.24547903436611,7.009977076726536 46.244995300279086,7.009187734983265"
            + " 46.24479663917551,7.008860662659381 46.24516646719812,7.008784739864421"
            + " 46.24533934577381,7.008802763251656 46.245519329293245))";

    public static final String PLUGIN_FILE_NAME_FILTER = "extract-task-fmedesktop-*.jar";

    public static final String PRODUCT_GUID = "a8405d50-f712-4e3e-96b2-a5452cf4e03e";

    public static final int REQUEST_ID = 1;

    public static final Request.Status REQUEST_STATUS = Request.Status.ONGOING;

    public static final String TASK_PLUGINS_FOLDER_PATH = "src/main/resources/task_processors";


    @BeforeAll
    public static void initialize() {
        FmeDesktopIntegrationTest.configurePlugin();
    }


    @BeforeEach
    public final void setUp() {
        this.configureRequest();
        this.configurePluginParameters();
    }



    private static void configurePlugin() {
        TaskProcessorsDiscoverer taskPluginDiscoverer = TaskProcessorsDiscoverer.getInstance();
        taskPluginDiscoverer.setApplicationLanguage(FmeDesktopIntegrationTest.APPLICATION_LANGUAGE);

        File pluginDir = new File(FmeDesktopIntegrationTest.TASK_PLUGINS_FOLDER_PATH);
        FileFilter fileFilter = new WildcardFileFilter(FmeDesktopIntegrationTest.PLUGIN_FILE_NAME_FILTER);
        File[] foundPluginFiles = pluginDir.listFiles(fileFilter);

        if (ArrayUtils.isEmpty(foundPluginFiles)) {
            throw new RuntimeException("FME Desktop plugin JAR not found.");
        }

        URL pluginUrl;

        try {
            pluginUrl = new URL(String.format("jar:file:%s!/", foundPluginFiles[0].getAbsolutePath()));

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        taskPluginDiscoverer.setJarUrls(new URL[] { pluginUrl });
        FmeDesktopIntegrationTest.fmeDesktopPlugin =
                taskPluginDiscoverer.getTaskProcessor(FmeDesktopIntegrationTest.PLUGIN_CODE);
    }



    private void configurePluginParameters() {
        this.plugin_parameters = new HashMap<>();

        String mockExecutableFilePath;
        if (SystemUtils.IS_OS_WINDOWS) {
            mockExecutableFilePath = FmeDesktopIntegrationTest.FME_MOCK_WINDOWS;

        } else {
            mockExecutableFilePath = FmeDesktopIntegrationTest.FME_MOCK_LINUX;
        }

        this.plugin_parameters.put("pathFME", new File(mockExecutableFilePath).getAbsolutePath());
        this.plugin_parameters.put("instances", FmeDesktopIntegrationTest.NUM_INSTANCES);
    }



    private void configureRequest() {
        this.testRequest = new Request();
        this.testRequest.setFolderIn(FmeDesktopIntegrationTest.INPUT_FOLDER);
        this.testRequest.setFolderOut(FmeDesktopIntegrationTest.OUTPUT_FOLDER);
        this.testRequest.setClientGuid(FmeDesktopIntegrationTest.CLIENT_GUID);
        this.testRequest.setOrderLabel(FmeDesktopIntegrationTest.ORDER_LABEL);
        this.testRequest.setOrganismGuid(FmeDesktopIntegrationTest.ORGANISM_GUID);
        this.testRequest.setParameters(FmeDesktopIntegrationTest.PARAMETERS_JSON);
        this.testRequest.setPerimeter(FmeDesktopIntegrationTest.PERIMETER_POLYGON);
        this.testRequest.setProductGuid(FmeDesktopIntegrationTest.PRODUCT_GUID);
        this.testRequest.setStatus(FmeDesktopIntegrationTest.REQUEST_STATUS);
        this.testRequest.setId(FmeDesktopIntegrationTest.REQUEST_ID);
    }



    @Test
    public final void testParameters() {
        this.plugin_parameters.put("path", new File(FmeDesktopIntegrationTest.SUCCESS_WORKSPACE).getAbsolutePath());

        final ITaskProcessor pluginInstance
                = FmeDesktopIntegrationTest.fmeDesktopPlugin.newInstance(FmeDesktopIntegrationTest.APPLICATION_LANGUAGE,
                                                                         this.plugin_parameters);

        final TaskProcessorRequest taskProcessorRequest
                = new TaskProcessorRequest(this.testRequest, FmeDesktopIntegrationTest.DATA_FOLDERS_BASE_PATH);
        final ITaskProcessorResult pluginResult = pluginInstance.execute(taskProcessorRequest, null);

        assertEquals(FmeDesktopIntegrationTest.EXPECTED_SUCCESS_MESSAGE, pluginResult.getMessage());
        assertEquals(FmeDesktopIntegrationTest.EXPECTED_SUCCESS_ERROR_CODE, pluginResult.getErrorCode());
        assertEquals(FmeDesktopIntegrationTest.EXPECTED_SUCCESS_STATUS, pluginResult.getStatus());
    }



    @Test
    public final void testReturnNoFiles() {
        this.plugin_parameters.put("path", new File(FmeDesktopIntegrationTest.NO_FILES_WORKSPACE).getAbsolutePath());

        final ITaskProcessor pluginInstance
                = FmeDesktopIntegrationTest.fmeDesktopPlugin.newInstance(FmeDesktopIntegrationTest.APPLICATION_LANGUAGE,
                                                                         this.plugin_parameters);

        final TaskProcessorRequest taskProcessorRequest
                = new TaskProcessorRequest(this.testRequest, FmeDesktopIntegrationTest.DATA_FOLDERS_BASE_PATH);
        final ITaskProcessorResult pluginResult = pluginInstance.execute(taskProcessorRequest, null);

        assertEquals(FmeDesktopIntegrationTest.EXPECTED_FR_NO_FILES_MESSAGE, pluginResult.getMessage());
        assertNotEquals(FmeDesktopIntegrationTest.EXPECTED_SUCCESS_ERROR_CODE, pluginResult.getErrorCode());
        assertEquals(FmeDesktopIntegrationTest.EXPECTED_ERROR_STATUS, pluginResult.getStatus());
    }



    @Test
    public final void testReturnError() {
        this.plugin_parameters.put("path", new File(FmeDesktopIntegrationTest.FAILING_WORKSPACE).getAbsolutePath());

        final ITaskProcessor pluginInstance
                = FmeDesktopIntegrationTest.fmeDesktopPlugin.newInstance(FmeDesktopIntegrationTest.APPLICATION_LANGUAGE,
                                                                         this.plugin_parameters);

        final TaskProcessorRequest taskProcessorRequest
                = new TaskProcessorRequest(this.testRequest, FmeDesktopIntegrationTest.DATA_FOLDERS_BASE_PATH);
        final ITaskProcessorResult pluginResult = pluginInstance.execute(taskProcessorRequest, null);

        assertNotEquals("", pluginResult.getMessage());
        assertNotEquals(FmeDesktopIntegrationTest.EXPECTED_SUCCESS_ERROR_CODE, pluginResult.getErrorCode());
        assertEquals(FmeDesktopIntegrationTest.EXPECTED_ERROR_STATUS, pluginResult.getStatus());
    }
}
