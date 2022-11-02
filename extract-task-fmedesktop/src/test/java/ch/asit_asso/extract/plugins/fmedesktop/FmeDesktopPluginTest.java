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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 *
 * @author Yves Grasset
 */
public class FmeDesktopPluginTest {
    private static final String CONFIG_FILE_PATH = "plugins/fme/properties/configFME.properties";
    private static final String DESCRIPTION_STRING_IDENTIFIER = "plugin.description";
    private static final String EXPECTED_ICON_CLASS = "fa-cogs";
    private static final String EXPECTED_PLUGIN_CODE = "FME2017";
    private static final String FME_PATH_PARAMETER_NAME_PROPERTY = "paramPathFME";
    private static final String HELP_FILE_NAME = "fmeDesktopHelp.html";
    private static final String LABEL_STRING_IDENTIFIER = "plugin.label";
    private static final String PARAMETER_CODE_NAME = "code";
    private static final String PARAMETER_LABEL_NAME = "label";
    private static final String PARAMETER_MAX_LENGTH_NAME = "maxlength";
    private static final String PARAMETER_REQUIRED_NAME = "req";
    private static final String PARAMETER_TYPE_NAME = "type";
    private static final int PARAMETERS_NUMBER = 2;
    private static final String SCRIPT_PATH_PARAMETER_NAME_PROPERTY = "paramPath";
    private static final String TEST_FME_PATH = "";
    private static final String TEST_INSTANCE_LANGUAGE = "fr";
    private static final String TEST_SCRIPT_PATH = "";
    private static final String[] VALID_PARAMETER_TYPES = new String[]{"email", "pass", "multitext", "text"};

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



    @BeforeClass
    public static final void setUpClass() {
    }



    @AfterClass
    public static final void tearDownClass() {
    }



    @Before
    public final void setUp() {
        this.configuration = new PluginConfiguration(FmeDesktopPluginTest.CONFIG_FILE_PATH);
        this.messages = new LocalizedMessages(FmeDesktopPluginTest.TEST_INSTANCE_LANGUAGE);
        this.parameterMapper = new ObjectMapper();

        final String fmePathCode
                = this.configuration.getProperty(FmeDesktopPluginTest.FME_PATH_PARAMETER_NAME_PROPERTY);
        final String scriptPathCode
                = this.configuration.getProperty(FmeDesktopPluginTest.SCRIPT_PATH_PARAMETER_NAME_PROPERTY);

        this.requiredParametersCodes = new String[]{fmePathCode, scriptPathCode};

        this.testParameters = new HashMap<>();
        this.testParameters.put(fmePathCode, FmeDesktopPluginTest.TEST_FME_PATH);
        this.testParameters.put(scriptPathCode, FmeDesktopPluginTest.TEST_SCRIPT_PATH);
    }



    @After
    public final void tearDown() {
    }



    /**
     * Test of newInstance method, of class FmeDesktopPlugin.
     */
    @Test
    public final void testNewInstanceWithoutParameters() {
        System.out.println("newInstance without parameters");
        FmeDesktopPlugin instance = new FmeDesktopPlugin();
        FmeDesktopPlugin result = instance.newInstance(FmeDesktopPluginTest.TEST_INSTANCE_LANGUAGE);
        Assert.assertNotSame(instance, result);
    }



    /**
     * Test of newInstance method, of class FmeDesktopPlugin.
     */
    @Test
    public final void testNewInstanceWithParameters() {
        System.out.println("newInstance with parameters");
        FmeDesktopPlugin instance = new FmeDesktopPlugin();
        FmeDesktopPlugin result = instance.newInstance(FmeDesktopPluginTest.TEST_INSTANCE_LANGUAGE,
                this.testParameters);
        Assert.assertNotSame(instance, result);
    }



    /**
     * Test of getLabel method, of class FmeDesktopPlugin.
     */
    @Test
    public final void testGetLabel() {
        System.out.println("getLabel");
        FmeDesktopPlugin instance = new FmeDesktopPlugin(FmeDesktopPluginTest.TEST_INSTANCE_LANGUAGE);
        String expectedLabel = this.messages.getString(LABEL_STRING_IDENTIFIER);
        String result = instance.getLabel();
        Assert.assertEquals(expectedLabel, result);
    }



    /**
     * Test of getCode method, of class FmeDesktopPlugin.
     */
    @Test
    public final void testGetCode() {
        System.out.println("getCode");
        FmeDesktopPlugin instance = new FmeDesktopPlugin();
        String result = instance.getCode();
        Assert.assertEquals(FmeDesktopPluginTest.EXPECTED_PLUGIN_CODE, result);
    }



    /**
     * Test of getDescription method, of class FmeDesktopPlugin.
     */
    @Test
    public final void testGetDescription() {
        System.out.println("getDescription");
        FmeDesktopPlugin instance = new FmeDesktopPlugin(FmeDesktopPluginTest.TEST_INSTANCE_LANGUAGE);
        String expectedDescription = this.messages.getString(FmeDesktopPluginTest.DESCRIPTION_STRING_IDENTIFIER);
        String result = instance.getDescription();
        Assert.assertEquals(expectedDescription, result);
    }



    /**
     * Test of getHelp method, of class FmeDesktopPlugin.
     */
    @Test
    public final void testGetHelp() {
        System.out.println("getHelp");
        FmeDesktopPlugin instance = new FmeDesktopPlugin(FmeDesktopPluginTest.TEST_INSTANCE_LANGUAGE);
        String expectedHelp = this.messages.getFileContent(FmeDesktopPluginTest.HELP_FILE_NAME);
        String result = instance.getHelp();
        Assert.assertEquals(expectedHelp, result);
    }



    /**
     * Test of getPictoClass method, of class FmeDesktopPlugin.
     */
    @Test
    public final void testGetPictoClass() {
        System.out.println("getPictoClass");
        FmeDesktopPlugin instance = new FmeDesktopPlugin();
        String result = instance.getPictoClass();
        Assert.assertEquals(FmeDesktopPluginTest.EXPECTED_ICON_CLASS, result);
    }



    /**
     * Test of getParams method, of class FmeDesktopPlugin.
     */
    @Test
    public final void testGetParams() {
        System.out.println("getParams");
        FmeDesktopPlugin instance = new FmeDesktopPlugin();
        ArrayNode parametersArray = null;

        try {
            parametersArray = this.parameterMapper.readValue(instance.getParams(), ArrayNode.class);

        } catch (IOException exception) {
            this.logger.error("An error occurred when the parameters JSON was parsed.", exception);
            Assert.fail("Could not parse the parameters JSON string.");
        }

        Assert.assertNotNull(parametersArray);
        Assert.assertEquals(FmeDesktopPluginTest.PARAMETERS_NUMBER, parametersArray.size());
        List<String> requiredCodes = new ArrayList<>(Arrays.asList(requiredParametersCodes));

        for (int parameterIndex = 0; parameterIndex < parametersArray.size(); parameterIndex++) {
            JsonNode parameterData = parametersArray.get(parameterIndex);
            Assert.assertTrue(String.format("The parameter #%d does not have a code property", parameterIndex),
                    parameterData.hasNonNull(FmeDesktopPluginTest.PARAMETER_CODE_NAME));
            String parameterCode = parameterData.get(FmeDesktopPluginTest.PARAMETER_CODE_NAME).textValue();
            Assert.assertTrue(String.format("The code for parameter #%d is null or blank", parameterIndex),
                    StringUtils.isNotBlank(parameterCode));
            Assert.assertTrue(
                    String.format("The parameter code %s is not expected or has already been defined.", parameterCode),
                    requiredCodes.indexOf(parameterCode) >= 0
            );
            requiredCodes.remove(parameterCode);

            Assert.assertTrue(String.format("The parameter %s does not have a label property", parameterCode),
                    parameterData.hasNonNull(FmeDesktopPluginTest.PARAMETER_LABEL_NAME));
            String label = parameterData.get(FmeDesktopPluginTest.PARAMETER_LABEL_NAME).textValue();
            Assert.assertTrue(String.format("The label for parameter %s is null or blank.", parameterCode),
                    StringUtils.isNotBlank(label));

            Assert.assertTrue(String.format("The parameter %s does not have a type property", parameterCode),
                    parameterData.hasNonNull(FmeDesktopPluginTest.PARAMETER_TYPE_NAME));
            String parameterType = parameterData.get(FmeDesktopPluginTest.PARAMETER_TYPE_NAME).textValue();
            Assert.assertTrue(String.format("The type for parameter %s is invalid.", parameterCode),
                    StringUtils.isNotBlank(label)
                    && ArrayUtils.contains(FmeDesktopPluginTest.VALID_PARAMETER_TYPES, parameterType));

            Assert.assertTrue(String.format("The parameter %s does not have a required property", parameterCode),
                    parameterData.hasNonNull(FmeDesktopPluginTest.PARAMETER_REQUIRED_NAME));
            Assert.assertTrue(String.format("The required field for the parameter %s is not a boolean", parameterCode),
                    parameterData.get(FmeDesktopPluginTest.PARAMETER_REQUIRED_NAME).isBoolean());

            Assert.assertTrue(String.format("The parameter %s does not have a maximum length property", parameterCode),
                    parameterData.hasNonNull(FmeDesktopPluginTest.PARAMETER_MAX_LENGTH_NAME));
            Assert.assertTrue(String.format("The maximum length for parameter %s is not an integer", parameterCode),
                    parameterData.get(FmeDesktopPluginTest.PARAMETER_MAX_LENGTH_NAME).isInt());
            int maxLength = parameterData.get(FmeDesktopPluginTest.PARAMETER_MAX_LENGTH_NAME).intValue();
            Assert.assertTrue(
                    String.format("The maximum length of parameter %s is not strictly positive", parameterCode),
                    maxLength > 0
            );
        }

        Assert.assertTrue(
                String.format("The following parameters are missing: %s", StringUtils.join(requiredCodes, ", ")),
                requiredCodes.isEmpty()
        );
    }



    /**
     * Test of execute method, of class FmeDesktopPlugin.
     */
    @Test
    public final void testExecute() {
        // TODO Test avec bouchon
    }

}
