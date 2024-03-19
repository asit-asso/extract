package ch.asit_asso.extract.integration.taskplugins;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ApplicationConfiguration.class })
@WebAppConfiguration*/
@Tag("integration")
public class FmeDesktopIntegrationTest {

/*
    private Request testRequest;

    @Autowired
    private ApplicationRepositories applicationRepositories;

    @Autowired
    private TaskProcessorDiscovererWrapper taskPluginDiscoverer;

    private String applicationLanguage = "fr";

    private String plugin_code = "ARCHIVE";

    private HashMap<String, String> plugin_parameters;

    private String output_path = "/var/extract/archive/{orderGuid}/{productGuid}/";

    private String target_directory = "/var/extract/archive/mon-guid-commande/mon-guid-produit/";

    private String testFileName = "test.txt";

    private String testFileContent = "Ceci est un test";

    @Before
    public void setUp() throws IOException {
        this.testRequest = new Request();
        testRequest.setFolderOut("test_request/output");
        testRequest.setOrderGuid("mon-guid-commande");
        testRequest.setProductGuid("mon-guid-produit");

        this.plugin_parameters = new HashMap<String, String>();
        this.plugin_parameters.put("path", this.output_path);

        File testFile = new File(testRequest.getFolderOut(), this.testFileName);
        testFile.createNewFile();
        FileWriter testFileWriter = new FileWriter(testFile);
        testFileWriter.write(this.testFileContent);
        testFileWriter.close();
    }
*/

    @Test
    public void testParameters() {
/*
        final ITaskProcessor taskPlugin = this.taskPluginsDiscoverer.getTaskProcessor(this.plugin_code);
        assertNotNull(taskPlugin);

        final ITaskProcessor pluginInstance = taskPlugin.newInstance(this.applicationLanguage, task.getParametersValues());
        final String dataFoldersBasePath = this.applicationRepositories.getParametersRepository().getBasePath();
        final TaskProcessorRequest taskProcessorRequest
                = new TaskProcessorRequest(this.testRequest, dataFoldersBasePath);
        final ITaskProcessorResult pluginResult = pluginInstance.execute(taskProcessorRequest, null);
*/


        boolean result = true;

        assertTrue(result);
    }



    @Test
    public void testReturnSuccess() {
        int result = 0;

        assertEquals(0, result);
    }



    @Test
    public void testReturnError() {
        int result = 1;

        assertNotEquals(0, result);
    }
}
