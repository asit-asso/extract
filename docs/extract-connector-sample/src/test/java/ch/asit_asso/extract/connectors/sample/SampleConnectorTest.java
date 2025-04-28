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
package ch.asit_asso.extract.connectors.sample;

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
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Assertions;


/**
 *
 * @author Yves Grasset
 */
public class SampleConnectorTest {
    private static final String CONFIG_FILE_PATH = "connectors/sample/properties/config.properties";
    private static final String DESCRIPTION_STRING_IDENTIFIER = "plugin.description";
    private static final String EXPECTED_ICON_CLASS = "";
    private static final String EXPECTED_PLUGIN_CODE = "easysdiv4";
    private static final String DETAILS_URL_PARAMETER_NAME_PROPERTY = "code.detailsUrlPattern";
    private static final String HELP_STRING_IDENTIFIER = "plugin.help";
    private static final String INSTANCE_LANGUAGE = "fr";
    private static final String LABEL_STRING_IDENTIFIER = "plugin.label";
    private static final String LOGIN_PARAMETER_NAME_PROPERTY = "code.login";
    private static final String PARAMETER_CODE_NAME = "code";
    private static final String PARAMETER_LABEL_NAME = "label";
    private static final String PARAMETER_MAX_LENGTH_NAME = "maxlength";
    private static final String PARAMETER_MAX_VALUE_NAME = "max";
    private static final String PARAMETER_MIN_VALUE_NAME = "min";
    private static final String PARAMETER_REQUIRED_NAME = "req";
    private static final String PARAMETER_STEP_NAME = "step";
    private static final String PARAMETER_TYPE_NAME = "type";
    private static final String PASSWORD_PARAMETER_NAME_PROPERTY = "code.password";
    private static final String TEST_LOGIN = "";
    private static final String TEST_PASSWORD = "";
    private static final String TEST_UPLOAD_SIZE = "1024";
    private static final String TEST_URL = "";
    private static final String TEST_DETAILS_URL = null;
    private static final String UPLOAD_SIZE_PARAMETER_NAME_PROPERTY = "code.uploadSize";
    private static final String URL_PARAMETER_NAME_PROPERTY = "code.serviceUrl";
    private static final String[] VALID_PARAMETER_TYPES = new String[]{"email", "pass", "multitext", "text", "numeric"};

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(SampleConnectorTest.class);

    private ConnectorConfig configuration;
    private LocalizedMessages messages;
    private ObjectMapper parameterMapper;
    private String[] requiredParametersCodes;
    private Map<String, String> testParameters;



    public SampleConnectorTest() {
    }



    @BeforeEach
    public final void setUp() {
        this.configuration = new ConnectorConfig(SampleConnectorTest.CONFIG_FILE_PATH);
        this.messages = new LocalizedMessages(SampleConnectorTest.INSTANCE_LANGUAGE);

        final String loginCode = this.configuration.getProperty(SampleConnectorTest.LOGIN_PARAMETER_NAME_PROPERTY);
        final String passwordCode = this.configuration.getProperty(SampleConnectorTest.PASSWORD_PARAMETER_NAME_PROPERTY);
        final String urlCode = this.configuration.getProperty(SampleConnectorTest.URL_PARAMETER_NAME_PROPERTY);
        final String uploadSizeCode = this.configuration.getProperty(SampleConnectorTest.UPLOAD_SIZE_PARAMETER_NAME_PROPERTY);
        final String detailsUrlPattern
                = this.configuration.getProperty(SampleConnectorTest.DETAILS_URL_PARAMETER_NAME_PROPERTY);

        this.requiredParametersCodes = new String[]{loginCode, passwordCode, urlCode, uploadSizeCode, detailsUrlPattern};

        this.testParameters = new HashMap<>();
        this.testParameters.put(loginCode, SampleConnectorTest.TEST_LOGIN);
        this.testParameters.put(passwordCode, SampleConnectorTest.TEST_PASSWORD);
        this.testParameters.put(urlCode, SampleConnectorTest.TEST_URL);
        this.testParameters.put(uploadSizeCode, SampleConnectorTest.TEST_UPLOAD_SIZE);
        this.testParameters.put(detailsUrlPattern, SampleConnectorTest.TEST_DETAILS_URL);

        this.parameterMapper = new ObjectMapper();
    }



    /**
     * Test of newInstance method, of class .
     */
    @Test
    public final void testNewInstanceWithoutParameters() {
        System.out.println("newInstance without parameters");
        SampleConnector instance = new SampleConnector();
        SampleConnector result = instance.newInstance(SampleConnectorTest.INSTANCE_LANGUAGE);
        Assertions.assertNotSame(instance, result);
    }



    /**
     * Test of newInstance method, of class .
     */
    @Test
    public final void testNewInstanceWithParameters() {
        System.out.println("newInstance with parameters");
        SampleConnector instance = new SampleConnector();
        SampleConnector result = instance.newInstance(SampleConnectorTest.INSTANCE_LANGUAGE, this.testParameters);
        Assertions.assertNotSame(instance, result);
    }



    /**
     * Test of getLabel method, of class .
     */
    @Test
    public final void testGetLabel() {
        System.out.println("getLabel");
        SampleConnector instance = new SampleConnector(SampleConnectorTest.INSTANCE_LANGUAGE);
        String expResult = this.messages.getString(SampleConnectorTest.LABEL_STRING_IDENTIFIER);
        String result = instance.getLabel();
        Assertions.assertEquals(expResult, result);
    }



    /**
     * Test of getCode method, of class .
     */
    @Test
    public final void testGetCode() {
        System.out.println("getCode");
        SampleConnector instance = new SampleConnector();
        String result = instance.getCode();
        Assertions.assertEquals(SampleConnectorTest.EXPECTED_PLUGIN_CODE, result);
    }



    /**
     * Test of getDescription method, of class .
     */
    @Test
    public final void testGetDescription() {
        System.out.println("getDescription");
        SampleConnector instance = new SampleConnector(SampleConnectorTest.INSTANCE_LANGUAGE);
        String expResult = this.messages.getString(SampleConnectorTest.DESCRIPTION_STRING_IDENTIFIER);
        String result = instance.getDescription();
        Assertions.assertEquals(expResult, result);
    }



    /**
     * Test of getHelp method, of class .
     */
    @Test
    public final void testGetHelp() {
        System.out.println("getHelp");
        SampleConnector instance = new SampleConnector(SampleConnectorTest.INSTANCE_LANGUAGE);
        String expResult = this.messages.getString(SampleConnectorTest.HELP_STRING_IDENTIFIER);
        String result = instance.getHelp();
        Assertions.assertEquals(expResult, result);
    }



    /**
     * Test of getPicto method, of class .
     */
    @Test
    public final void testGetPicto() {
        System.out.println("getPicto");
        SampleConnector instance = new SampleConnector();
        String result = instance.getPicto();
        Assertions.assertEquals(SampleConnectorTest.EXPECTED_ICON_CLASS, result);
    }



    /**
     * Test of getParams method, of class .
     */
    @Test
    public final void testGetParams() {
        System.out.println("getParams");
        SampleConnector instance = new SampleConnector();
        ArrayNode parametersArray = null;

        try {
            parametersArray = this.parameterMapper.readValue(instance.getParams(), ArrayNode.class);

        } catch (IOException exception) {
            this.logger.error("An error occurred when the parameters JSON was parsed.", exception);
            Assertions.fail("Could not parse the parameters JSON string.");
        }

        Assertions.assertNotNull(parametersArray);
        Assertions.assertEquals(this.testParameters.size(), parametersArray.size());
        List<String> requiredCodes = new ArrayList<>(Arrays.asList(requiredParametersCodes));

        for (int parameterIndex = 0; parameterIndex < parametersArray.size(); parameterIndex++) {
            JsonNode parameterData = parametersArray.get(parameterIndex);
            Assertions.assertTrue(parameterData.hasNonNull(SampleConnectorTest.PARAMETER_CODE_NAME),
                                  String.format("The parameter #%d does not have a code property", parameterIndex));
            String parameterCode = parameterData.get(SampleConnectorTest.PARAMETER_CODE_NAME).textValue();
            Assertions.assertTrue(StringUtils.isNotBlank(parameterCode),
                                  String.format("The code for parameter #%d is null or blank", parameterIndex));
            Assertions.assertTrue(requiredCodes.contains(parameterCode),
                                  String.format("The parameter code %s is not expected or has already been defined.",
                                                parameterCode)
            );
            requiredCodes.remove(parameterCode);

            Assertions.assertTrue(parameterData.hasNonNull(SampleConnectorTest.PARAMETER_LABEL_NAME),
                                  String.format("The parameter %s does not have a label property", parameterCode));
            String label = parameterData.get(SampleConnectorTest.PARAMETER_LABEL_NAME).textValue();
            Assertions.assertTrue(StringUtils.isNotBlank(label),
                                  String.format("The label for parameter %s is null or blank.", parameterCode));

            Assertions.assertTrue(parameterData.hasNonNull(SampleConnectorTest.PARAMETER_TYPE_NAME),
                                  String.format("The parameter %s does not have a type property", parameterCode));
            String parameterType = parameterData.get(SampleConnectorTest.PARAMETER_TYPE_NAME).textValue();
            Assertions.assertTrue(StringUtils.isNotBlank(label)
                                          && ArrayUtils.contains(SampleConnectorTest.VALID_PARAMETER_TYPES, parameterType),
                                  String.format("The type for parameter %s is invalid.", parameterCode));

            Assertions.assertTrue(parameterData.hasNonNull(SampleConnectorTest.PARAMETER_REQUIRED_NAME),
                                  String.format("The parameter %s does not have a required property", parameterCode));
            Assertions.assertTrue(parameterData.get(SampleConnectorTest.PARAMETER_REQUIRED_NAME).isBoolean(),
                                  String.format("The required field for the parameter %s is not a boolean",
                                                parameterCode));

            if (parameterType.equals("numeric")) {
                Integer maxValue = null;

                if (parameterData.hasNonNull(SampleConnectorTest.PARAMETER_MAX_VALUE_NAME)) {
                    Assertions.assertTrue(parameterData.get(SampleConnectorTest.PARAMETER_MAX_VALUE_NAME).isNumber(),
                                          String.format("The maximum value for parameter %s is not a number",
                                                        parameterCode));
                    maxValue = parameterData.get(SampleConnectorTest.PARAMETER_MAX_VALUE_NAME).intValue();
                }

                if (parameterData.hasNonNull(SampleConnectorTest.PARAMETER_MIN_VALUE_NAME)) {
                    Assertions.assertTrue(parameterData.get(SampleConnectorTest.PARAMETER_MIN_VALUE_NAME).isInt(),
                                          String.format("The minimum value for parameter %s is not an integer",
                                                        parameterCode));
                    int minValue = parameterData.get(SampleConnectorTest.PARAMETER_MIN_VALUE_NAME).intValue();

                    if (maxValue != null) {
                        Assertions.assertTrue(minValue < maxValue,
                              String.format("The minimum value for parameter %s must be less than the maximum value",
                                            parameterCode));
                    }
                }

                if (parameterData.hasNonNull(SampleConnectorTest.PARAMETER_STEP_NAME)) {
                    Assertions.assertTrue(parameterData.get(SampleConnectorTest.PARAMETER_STEP_NAME).isInt(),
                                          String.format("The step value for parameter %s is not an integer",
                                                        parameterCode));
                }

            } else {
                Assertions.assertTrue(parameterData.hasNonNull(SampleConnectorTest.PARAMETER_MAX_LENGTH_NAME),
                                      String.format("The parameter %s does not have a maximum length property",
                                                    parameterCode));
                Assertions.assertTrue(parameterData.get(SampleConnectorTest.PARAMETER_MAX_LENGTH_NAME).isInt(),
                                      String.format("The maximum length for parameter %s is not an integer",
                                                    parameterCode));
                int maxLength = parameterData.get(SampleConnectorTest.PARAMETER_MAX_LENGTH_NAME).intValue();
                Assertions.assertTrue(maxLength > 0,
                                      String.format("The maximum length of parameter %s is not strictly positive",
                                                    parameterCode)
                );
            }
        }

        Assertions.assertTrue(requiredCodes.isEmpty(),
                              String.format("The following parameters are missing: %s",
                                            StringUtils.join(requiredCodes, ", "))
        );
    }



    /**
     * Test of importCommands method, of class Easysdiv4.
     */
    @Test
    public final void testImportCommands() {
        // TODO Test avec bouchon
    }



    /**
     * Test of exportResult method, of class Easysdiv4.
     */
    @Test
    public final void testExportResult() {
        // TODO Test avec bouchon
    }

}
