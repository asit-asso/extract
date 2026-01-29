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
package ch.asit_asso.extract.plugins.fmedesktop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * @author Yves Grasset
 */
public class FmeDesktopPluginTest {
    private static final String CONFIG_FILE_PATH = "plugins/fme/properties/configFME.properties";

    private static final String DESCRIPTION_STRING_IDENTIFIER = "plugin.description";

    private static final String EXPECTED_ICON_CLASS = "fa-cogs";

    private static final String EXPECTED_PLUGIN_CODE = "FME2017";

    private static final String FME_PATH_PARAMETER_NAME_PROPERTY = "paramPathFME";

    private static final String HELP_FILE_NAME = "fmeDesktopHelp.html";

    private static final String INSTANCES_PARAMETER_NAME_PROPERTY = "paramInstances";

    private static final String LABEL_STRING_IDENTIFIER = "plugin.label";

    private static final String PARAMETER_CODE_NAME = "code";

    private static final String PARAMETER_LABEL_NAME = "label";

    private static final String PARAMETER_REQUIRED_NAME = "req";

    private static final String PARAMETER_TYPE_NAME = "type";

    private static final int PARAMETERS_NUMBER = 3;

    private static final String SCRIPT_PATH_PARAMETER_NAME_PROPERTY = "paramPath";

    private static final String TEST_FME_PATH = "";

    private static final String TEST_INSTANCE_LANGUAGE = "fr";

    private static final String TEST_INSTANCES = "3";

    private static final String TEST_SCRIPT_PATH = "";

    private static final String[] VALID_PARAMETER_TYPES = new String[] {"email", "pass", "multitext", "text",
                                                                        "numeric"};

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(FmeDesktopPluginTest.class);

    private PluginConfiguration configuration;

    private LocalizedMessages messages;

    private ObjectMapper parameterMapper;

    private String[] requiredParametersCodes;

    private Map<String, String> testParameters;



    public FmeDesktopPluginTest() {

    }


    @BeforeEach
    public final void setUp() {
        this.configuration = new PluginConfiguration(FmeDesktopPluginTest.CONFIG_FILE_PATH);
        this.messages = new LocalizedMessages(FmeDesktopPluginTest.TEST_INSTANCE_LANGUAGE);
        this.parameterMapper = new ObjectMapper();

        final String fmePathCode
                = this.configuration.getProperty(FmeDesktopPluginTest.FME_PATH_PARAMETER_NAME_PROPERTY);
        final String scriptPathCode
                = this.configuration.getProperty(FmeDesktopPluginTest.SCRIPT_PATH_PARAMETER_NAME_PROPERTY);
        final String instancesCode
                = this.configuration.getProperty(FmeDesktopPluginTest.INSTANCES_PARAMETER_NAME_PROPERTY);

        this.requiredParametersCodes = new String[] {fmePathCode, scriptPathCode, instancesCode};

        this.testParameters = new HashMap<>();
        this.testParameters.put(fmePathCode, FmeDesktopPluginTest.TEST_FME_PATH);
        this.testParameters.put(scriptPathCode, FmeDesktopPluginTest.TEST_SCRIPT_PATH);
        this.testParameters.put(instancesCode, FmeDesktopPluginTest.TEST_INSTANCES);
    }



    /**
     * Test of newInstance method, of class FmeDesktopPlugin.
     */
    @Test
    @DisplayName("Create a new instance without parameter values")
    public final void testNewInstanceWithoutParameters() {
        FmeDesktopPlugin instance = new FmeDesktopPlugin();

        FmeDesktopPlugin result = instance.newInstance(FmeDesktopPluginTest.TEST_INSTANCE_LANGUAGE);

        assertNotSame(instance, result);
    }



    /**
     * Test of newInstance method, of class FmeDesktopPlugin.
     */
    @Test
    @DisplayName("Create a new instance with parameter values")
    public final void testNewInstanceWithParameters() {
        FmeDesktopPlugin instance = new FmeDesktopPlugin();

        FmeDesktopPlugin result = instance.newInstance(FmeDesktopPluginTest.TEST_INSTANCE_LANGUAGE,
                                                       this.testParameters);

        assertNotSame(instance, result);
    }



    /**
     * Test of getLabel method, of class FmeDesktopPlugin.
     */
    @Test
    @DisplayName("Check the plugin label")
    public final void testGetLabel() {
        FmeDesktopPlugin instance = new FmeDesktopPlugin(FmeDesktopPluginTest.TEST_INSTANCE_LANGUAGE);
        String expectedLabel = this.messages.getString(LABEL_STRING_IDENTIFIER);

        String result = instance.getLabel();

        assertEquals(expectedLabel, result);
    }



    /**
     * Test of getCode method, of class FmeDesktopPlugin.
     */
    @Test
    @DisplayName("Check the plugin identifier")
    public final void testGetCode() {
        FmeDesktopPlugin instance = new FmeDesktopPlugin();

        String result = instance.getCode();

        assertEquals(FmeDesktopPluginTest.EXPECTED_PLUGIN_CODE, result);
    }



    /**
     * Test of getDescription method, of class FmeDesktopPlugin.
     */
    @Test
    @DisplayName("Check the plugin description")
    public final void testGetDescription() {
        FmeDesktopPlugin instance = new FmeDesktopPlugin(FmeDesktopPluginTest.TEST_INSTANCE_LANGUAGE);
        String expectedDescription = this.messages.getString(FmeDesktopPluginTest.DESCRIPTION_STRING_IDENTIFIER);
        String result = instance.getDescription();

        assertEquals(expectedDescription, result);
    }



    /**
     * Test of getHelp method, of class FmeDesktopPlugin.
     */
    @Test
    @DisplayName("Check the help file name")
    public final void testGetHelp() {
        FmeDesktopPlugin instance = new FmeDesktopPlugin(FmeDesktopPluginTest.TEST_INSTANCE_LANGUAGE);
        String expectedHelp = this.messages.getFileContent(FmeDesktopPluginTest.HELP_FILE_NAME);

        String result = instance.getHelp();

        assertEquals(expectedHelp, result);
    }



    /**
     * Test of getPictoClass method, of class FmeDesktopPlugin.
     */
    @Test
    @DisplayName("Check the plugin pictogram")
    public final void testGetPictoClass() {
        FmeDesktopPlugin instance = new FmeDesktopPlugin();

        String result = instance.getPictoClass();

        assertEquals(FmeDesktopPluginTest.EXPECTED_ICON_CLASS, result);
    }



    /**
     * Test of getParams method, of class FmeDesktopPlugin.
     */
    @Test
    @DisplayName("Check the plugin parameters")
    public final void testGetParams() {
        FmeDesktopPlugin instance = new FmeDesktopPlugin();
        ArrayNode parametersArray = null;

        try {
            parametersArray = this.parameterMapper.readValue(instance.getParams(), ArrayNode.class);

        } catch (IOException exception) {
            this.logger.error("An error occurred when the parameters JSON was parsed.", exception);
            fail("Could not parse the parameters JSON string.");
        }

        assertNotNull(parametersArray);
        assertEquals(FmeDesktopPluginTest.PARAMETERS_NUMBER, parametersArray.size());
        List<String> requiredCodes = new ArrayList<>(Arrays.asList(requiredParametersCodes));

        for (int parameterIndex = 0; parameterIndex < parametersArray.size(); parameterIndex++) {
            JsonNode parameterData = parametersArray.get(parameterIndex);
            assertTrue(parameterData.hasNonNull(FmeDesktopPluginTest.PARAMETER_CODE_NAME),
                                  String.format("The parameter #%d does not have a code property", parameterIndex));
            String parameterCode = parameterData.get(FmeDesktopPluginTest.PARAMETER_CODE_NAME).textValue();
            assertTrue(StringUtils.isNotBlank(parameterCode),
                                  String.format("The code for parameter #%d is null or blank", parameterIndex));
            assertTrue(requiredCodes.contains(parameterCode),
                                  String.format("The parameter code %s is not expected or has already been defined.",
                                                parameterCode)
            );
            requiredCodes.remove(parameterCode);

            assertTrue(parameterData.hasNonNull(FmeDesktopPluginTest.PARAMETER_LABEL_NAME),
                                  String.format("The parameter %s does not have a label property", parameterCode));
            String label = parameterData.get(FmeDesktopPluginTest.PARAMETER_LABEL_NAME).textValue();
            assertTrue(StringUtils.isNotBlank(label),
                                  String.format("The label for parameter %s is null or blank.", parameterCode));

            assertTrue(parameterData.hasNonNull(FmeDesktopPluginTest.PARAMETER_TYPE_NAME),
                                  String.format("The parameter %s does not have a type property", parameterCode));
            String parameterType = parameterData.get(FmeDesktopPluginTest.PARAMETER_TYPE_NAME).textValue();
            assertTrue(StringUtils.isNotBlank(label)
                                  && ArrayUtils.contains(FmeDesktopPluginTest.VALID_PARAMETER_TYPES,
                                                         parameterType),
                                  String.format("The type for parameter %s is invalid.", parameterCode));

            assertTrue(parameterData.hasNonNull(FmeDesktopPluginTest.PARAMETER_REQUIRED_NAME),
                                  String.format("The parameter %s does not have a required property", parameterCode));
            assertTrue(parameterData.get(FmeDesktopPluginTest.PARAMETER_REQUIRED_NAME).isBoolean(),
                                  String.format("The required field for the parameter %s is not a boolean",
                                                parameterCode));
        }

        assertTrue(requiredCodes.isEmpty(),
                              String.format("The following parameters are missing: %s",
                                            StringUtils.join(requiredCodes, ", ")));
    }



    /**
     * Test of execute method with missing FME script.
     */
    @Test
    @DisplayName("Execute returns error when FME script not found")
    public final void testExecuteWithMissingScript() {
        Map<String, String> invalidParams = new HashMap<>();
        String scriptPathCode = this.configuration.getProperty(FmeDesktopPluginTest.SCRIPT_PATH_PARAMETER_NAME_PROPERTY);
        String fmePathCode = this.configuration.getProperty(FmeDesktopPluginTest.FME_PATH_PARAMETER_NAME_PROPERTY);
        String instancesCode = this.configuration.getProperty(FmeDesktopPluginTest.INSTANCES_PARAMETER_NAME_PROPERTY);

        invalidParams.put(scriptPathCode, "/nonexistent/path/script.fmw");
        invalidParams.put(fmePathCode, "/nonexistent/path/fme.exe");
        invalidParams.put(instancesCode, "1");

        FmeDesktopPlugin instance = new FmeDesktopPlugin(FmeDesktopPluginTest.TEST_INSTANCE_LANGUAGE, invalidParams);

        FmeDesktopRequest request = createTestRequest();
        ch.asit_asso.extract.plugins.common.ITaskProcessorResult result = instance.execute(request, null);

        assertNotNull(result);
        assertEquals(ch.asit_asso.extract.plugins.common.ITaskProcessorResult.Status.ERROR, result.getStatus());
        assertNotNull(result.getMessage());
    }

    /**
     * Test of newInstance method with null language.
     */
    @Test
    @DisplayName("Create a new instance with null language")
    public final void testNewInstanceWithNullLanguage() {
        FmeDesktopPlugin instance = new FmeDesktopPlugin();

        FmeDesktopPlugin result = instance.newInstance(null);

        assertNotNull(result);
        assertNotSame(instance, result);
    }

    /**
     * Test of newInstance method with empty language.
     */
    @Test
    @DisplayName("Create a new instance with empty language")
    public final void testNewInstanceWithEmptyLanguage() {
        FmeDesktopPlugin instance = new FmeDesktopPlugin();

        FmeDesktopPlugin result = instance.newInstance("");

        assertNotNull(result);
        assertNotSame(instance, result);
    }

    /**
     * Test that getParams returns valid JSON multiple times (cached help).
     */
    @Test
    @DisplayName("Check that getHelp caches result")
    public final void testGetHelpCaching() {
        FmeDesktopPlugin instance = new FmeDesktopPlugin(FmeDesktopPluginTest.TEST_INSTANCE_LANGUAGE);

        String help1 = instance.getHelp();
        String help2 = instance.getHelp();

        assertEquals(help1, help2);
        assertNotNull(help1);
    }

    /**
     * Test default constructor.
     */
    @Test
    @DisplayName("Default constructor creates valid instance")
    public final void testDefaultConstructor() {
        FmeDesktopPlugin instance = new FmeDesktopPlugin();

        assertNotNull(instance);
        assertEquals(FmeDesktopPluginTest.EXPECTED_PLUGIN_CODE, instance.getCode());
        assertEquals(FmeDesktopPluginTest.EXPECTED_ICON_CLASS, instance.getPictoClass());
    }

    /**
     * Test constructor with only parameters.
     */
    @Test
    @DisplayName("Constructor with parameters creates valid instance")
    public final void testConstructorWithParameters() {
        FmeDesktopPlugin instance = new FmeDesktopPlugin(this.testParameters);

        assertNotNull(instance);
        assertEquals(FmeDesktopPluginTest.EXPECTED_PLUGIN_CODE, instance.getCode());
    }

    /**
     * Test that parameters JSON is well-formed.
     */
    @Test
    @DisplayName("Parameters JSON contains numeric type for instances")
    public final void testGetParamsNumericType() {
        FmeDesktopPlugin instance = new FmeDesktopPlugin();
        ArrayNode parametersArray = null;

        try {
            parametersArray = this.parameterMapper.readValue(instance.getParams(), ArrayNode.class);
        } catch (IOException exception) {
            fail("Could not parse the parameters JSON string.");
        }

        assertNotNull(parametersArray);

        boolean hasNumericType = false;
        for (int i = 0; i < parametersArray.size(); i++) {
            JsonNode param = parametersArray.get(i);
            if ("numeric".equals(param.get("type").textValue())) {
                hasNumericType = true;
                assertTrue(param.has("min"), "Numeric parameter should have min");
                assertTrue(param.has("max"), "Numeric parameter should have max");
                assertTrue(param.has("step"), "Numeric parameter should have step");
                break;
            }
        }
        assertTrue(hasNumericType, "Should have at least one numeric parameter");
    }

    /**
     * Test execute with null request data.
     */
    @Test
    @DisplayName("Execute with missing FME executable path returns error")
    public final void testExecuteWithMissingExecutable() {
        // Create params with valid script but invalid FME exe
        Map<String, String> params = new HashMap<>();
        String scriptPathCode = this.configuration.getProperty(FmeDesktopPluginTest.SCRIPT_PATH_PARAMETER_NAME_PROPERTY);
        String fmePathCode = this.configuration.getProperty(FmeDesktopPluginTest.FME_PATH_PARAMETER_NAME_PROPERTY);
        String instancesCode = this.configuration.getProperty(FmeDesktopPluginTest.INSTANCES_PARAMETER_NAME_PROPERTY);

        // Use a path that doesn't exist
        params.put(scriptPathCode, "/nonexistent/script.fmw");
        params.put(fmePathCode, "/nonexistent/fme.exe");
        params.put(instancesCode, "1");

        FmeDesktopPlugin instance = new FmeDesktopPlugin(FmeDesktopPluginTest.TEST_INSTANCE_LANGUAGE, params);
        FmeDesktopRequest request = createTestRequest();

        ch.asit_asso.extract.plugins.common.ITaskProcessorResult result = instance.execute(request, null);

        assertNotNull(result);
        assertEquals(ch.asit_asso.extract.plugins.common.ITaskProcessorResult.Status.ERROR, result.getStatus());
    }

    /**
     * Test that newInstance with params creates independent copy.
     */
    @Test
    @DisplayName("newInstance with params creates independent copy")
    public final void testNewInstanceIndependence() {
        FmeDesktopPlugin original = new FmeDesktopPlugin(FmeDesktopPluginTest.TEST_INSTANCE_LANGUAGE);

        Map<String, String> params1 = new HashMap<>();
        params1.put("key", "value1");

        Map<String, String> params2 = new HashMap<>();
        params2.put("key", "value2");

        FmeDesktopPlugin instance1 = original.newInstance(FmeDesktopPluginTest.TEST_INSTANCE_LANGUAGE, params1);
        FmeDesktopPlugin instance2 = original.newInstance(FmeDesktopPluginTest.TEST_INSTANCE_LANGUAGE, params2);

        assertNotSame(instance1, instance2);
        assertNotSame(original, instance1);
        assertNotSame(original, instance2);
    }

    /**
     * Helper method to create a test request.
     */
    private FmeDesktopRequest createTestRequest() {
        FmeDesktopRequest request = new FmeDesktopRequest();
        request.setId(1);
        request.setFolderIn("/tmp/input");
        request.setFolderOut("/tmp/output");
        request.setProductGuid("test-product-guid");
        request.setPerimeter("POLYGON((6.5 46.5, 6.6 46.5, 6.6 46.6, 6.5 46.6, 6.5 46.5))");
        request.setParameters("{}");
        request.setOrderLabel("Test Order");
        request.setClientGuid("test-client-guid");
        request.setOrganismGuid("test-organism-guid");
        return request;
    }

}
