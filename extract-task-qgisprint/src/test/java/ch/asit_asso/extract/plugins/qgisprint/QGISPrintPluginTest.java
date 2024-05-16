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
package ch.asit_asso.extract.plugins.qgisprint;

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
public class QGISPrintPluginTest {
    private static final String CONFIG_FILE_PATH = "plugins/qgisprint/properties/config.properties";

    private static final String DESCRIPTION_STRING_IDENTIFIER = "plugin.description";

    private static final String EXPECTED_ICON_CLASS = "fa-file-pdf-o";

    private static final String EXPECTED_PLUGIN_CODE = "QGISPRINT";

    private static final String QGIS_URL_PARAMETER_NAME_PROPERTY = "paramUrl";

    private static final String HELP_FILE_NAME = "help.html";

    private static final String TEMPLATE_LAYOUT_PARAMETER_NAME_PROPERTY = "paramTemplateLayout";

    private static final String QGIS_PATH_PARAMETER_NAME_PROPERTY = "paramPathProjectQGIS";

    private static final String LAYERS_PARAMETER_NAME_PROPERTY = "paramLayers";

    private static final String LOGIN_PARAMETER_NAME_PROPERTY = "paramLogin";

    private static final String PASSWORD_PARAMETER_NAME_PROPERTY = "paramPassword";

    private static final String CRS_PARAMETER_NAME_PROPERTY = "paramCRS";

    private static final String LABEL_STRING_IDENTIFIER = "plugin.label";

    private static final String PARAMETER_CODE_NAME = "code";

    private static final String PARAMETER_LABEL_NAME = "label";

    private static final String PARAMETER_REQUIRED_NAME = "req";

    private static final String PARAMETER_TYPE_NAME = "type";

    private static final int PARAMETERS_NUMBER = 7;

    private static final String TEST_QGIS_URL = "http://p22.arxit.lan";

    private static final String TEST_INSTANCE_LANGUAGE = "fr";

    private static final String TEST_QGIS_PATH = "/etc/qgisserver/world.qgs";

    private static final String TEST_LAYERS = "countries";

    private static final String TEST_LOGIN = "fkr";

    private static final String TEST_PASSWORD = "Spirale71";

    private static final String TEST_TEMPLATE_LAYOUT = "myplan";

    private static final String[] VALID_PARAMETER_TYPES = new String[] {"email", "pass", "multitext", "text",
                                                                        "numeric"};

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(QGISPrintPluginTest.class);

    private LocalizedMessages messages;

    private ObjectMapper parameterMapper;

    private String[] expectedParametersCodes;

    private Map<String, String> testParameters;



    public QGISPrintPluginTest() {

    }



    @BeforeEach
    public final void setUp() {
        PluginConfiguration configuration = new PluginConfiguration(QGISPrintPluginTest.CONFIG_FILE_PATH);
        this.messages = new LocalizedMessages(QGISPrintPluginTest.TEST_INSTANCE_LANGUAGE);
        this.parameterMapper = new ObjectMapper();

        final String qgisUrlCode
                = configuration.getProperty(QGISPrintPluginTest.QGIS_URL_PARAMETER_NAME_PROPERTY);
        final String templateLayoutCode
                = configuration.getProperty(QGISPrintPluginTest.TEMPLATE_LAYOUT_PARAMETER_NAME_PROPERTY);
        final String pathQGIS
                = configuration.getProperty(QGISPrintPluginTest.QGIS_PATH_PARAMETER_NAME_PROPERTY);
        final String layers
                = configuration.getProperty(QGISPrintPluginTest.LAYERS_PARAMETER_NAME_PROPERTY);
        final String login
                = configuration.getProperty(QGISPrintPluginTest.LOGIN_PARAMETER_NAME_PROPERTY);
        final String password
                = configuration.getProperty(QGISPrintPluginTest.PASSWORD_PARAMETER_NAME_PROPERTY);
        final String crs
                = configuration.getProperty(QGISPrintPluginTest.CRS_PARAMETER_NAME_PROPERTY);

        this.expectedParametersCodes = new String[] {
                qgisUrlCode, templateLayoutCode, pathQGIS, layers, login, password, crs
        };

        this.testParameters = new HashMap<>();
        this.testParameters.put(qgisUrlCode, QGISPrintPluginTest.TEST_QGIS_URL);
        this.testParameters.put(templateLayoutCode, QGISPrintPluginTest.TEST_TEMPLATE_LAYOUT);
        this.testParameters.put(pathQGIS, QGISPrintPluginTest.TEST_QGIS_PATH);
        this.testParameters.put(layers, QGISPrintPluginTest.TEST_LAYERS);
        this.testParameters.put(login, QGISPrintPluginTest.TEST_LOGIN);
        this.testParameters.put(password, QGISPrintPluginTest.TEST_PASSWORD);
    }



    /**
     * Test of newInstance method, of class FmeDesktopPlugin.
     */
    @Test
    @DisplayName("Create a new instance without parameter values")
    public final void testNewInstanceWithoutParameters() {
        QGISPrintPlugin instance = new QGISPrintPlugin();

        QGISPrintPlugin result = instance.newInstance(QGISPrintPluginTest.TEST_INSTANCE_LANGUAGE);

        assertNotSame(instance, result);
    }



    /**
     * Test of newInstance method, of class FmeDesktopPlugin.
     */
    @Test
    @DisplayName("Create a new instance with parameter values")
    public final void testNewInstanceWithParameters() {
        QGISPrintPlugin instance = new QGISPrintPlugin();

        QGISPrintPlugin result = instance.newInstance(QGISPrintPluginTest.TEST_INSTANCE_LANGUAGE,
                                                      this.testParameters);

        assertNotSame(instance, result);
    }



    /**
     * Test of getLabel method, of class FmeDesktopPlugin.
     */
    @Test
    @DisplayName("Check the plugin label")
    public final void testGetLabel() {
        QGISPrintPlugin instance = new QGISPrintPlugin(QGISPrintPluginTest.TEST_INSTANCE_LANGUAGE);
        String expectedLabel = this.messages.getString(QGISPrintPluginTest.LABEL_STRING_IDENTIFIER);

        String result = instance.getLabel();

        assertEquals(expectedLabel, result);
    }



    /**
     * Test of getCode method, of class FmeDesktopPlugin.
     */
    @Test
    @DisplayName("Check the plugin identifier")
    public final void testGetCode() {
        QGISPrintPlugin instance = new QGISPrintPlugin();

        String result = instance.getCode();

        assertEquals(QGISPrintPluginTest.EXPECTED_PLUGIN_CODE, result);
    }



    /**
     * Test of getDescription method, of class FmeDesktopPlugin.
     */
    @Test
    @DisplayName("Check the plugin description")
    public final void testGetDescription() {
        QGISPrintPlugin instance = new QGISPrintPlugin(QGISPrintPluginTest.TEST_INSTANCE_LANGUAGE);
        String expectedDescription = this.messages.getString(QGISPrintPluginTest.DESCRIPTION_STRING_IDENTIFIER);

        String result = instance.getDescription();

        assertEquals(expectedDescription, result);
    }



    /**
     * Test of getHelp method, of class FmeDesktopPlugin.
     */
    @Test
    @DisplayName("Check the help file name")
    public final void testGetHelp() {
        QGISPrintPlugin instance = new QGISPrintPlugin(QGISPrintPluginTest.TEST_INSTANCE_LANGUAGE);
        String expectedHelp = this.messages.getFileContent(QGISPrintPluginTest.HELP_FILE_NAME);

        String result = instance.getHelp();

        assertEquals(expectedHelp, result);
    }



    /**
     * Test of getPictoClass method, of class FmeDesktopPlugin.
     */
    @Test
    @DisplayName("Check the plugin pictogram")
    public final void testGetPictoClass() {
        QGISPrintPlugin instance = new QGISPrintPlugin();

        String result = instance.getPictoClass();

        assertEquals(QGISPrintPluginTest.EXPECTED_ICON_CLASS, result);
    }



    /**
     * Test of getParams method, of class FmeDesktopPlugin.
     */
    @Test
    @DisplayName("Check the plugin parameters")
    public final void testGetParams() {
        QGISPrintPlugin instance = new QGISPrintPlugin();
        ArrayNode parametersArray = null;

        try {
            parametersArray = this.parameterMapper.readValue(instance.getParams(), ArrayNode.class);

        } catch (IOException exception) {
            this.logger.error("An error occurred when the parameters JSON was parsed.", exception);
            fail("Could not parse the parameters JSON string.");
        }

        assertNotNull(parametersArray);
        assertEquals(QGISPrintPluginTest.PARAMETERS_NUMBER, parametersArray.size());
        List<String> requiredCodes = new ArrayList<>(Arrays.asList(this.expectedParametersCodes));

        for (int parameterIndex = 0; parameterIndex < parametersArray.size(); parameterIndex++) {
            JsonNode parameterData = parametersArray.get(parameterIndex);
            assertTrue(parameterData.hasNonNull(QGISPrintPluginTest.PARAMETER_CODE_NAME),
                                  String.format("The parameter #%d does not have a code property", parameterIndex));
            String parameterCode = parameterData.get(QGISPrintPluginTest.PARAMETER_CODE_NAME).textValue();
            assertTrue(StringUtils.isNotBlank(parameterCode),
                                  String.format("The code for parameter #%d is null or blank", parameterIndex));
            assertTrue(requiredCodes.contains(parameterCode),
                                  String.format("The parameter code %s is not expected or has already been defined.",
                                                parameterCode)
            );
            requiredCodes.remove(parameterCode);

            assertTrue(parameterData.hasNonNull(QGISPrintPluginTest.PARAMETER_LABEL_NAME),
                                  String.format("The parameter %s does not have a label property", parameterCode));
            String label = parameterData.get(QGISPrintPluginTest.PARAMETER_LABEL_NAME).textValue();
            assertTrue(StringUtils.isNotBlank(label),
                                  String.format("The label for parameter %s is null or blank.", parameterCode));

            assertTrue(parameterData.hasNonNull(QGISPrintPluginTest.PARAMETER_TYPE_NAME),
                                  String.format("The parameter %s does not have a type property", parameterCode));
            String parameterType = parameterData.get(QGISPrintPluginTest.PARAMETER_TYPE_NAME).textValue();
            assertTrue(StringUtils.isNotBlank(label)
                                  && ArrayUtils.contains(QGISPrintPluginTest.VALID_PARAMETER_TYPES,
                                                         parameterType),
                                  String.format("The type for parameter %s is invalid.", parameterCode));

            assertTrue(parameterData.hasNonNull(QGISPrintPluginTest.PARAMETER_REQUIRED_NAME),
                                  String.format("The parameter %s does not have a required property", parameterCode));
            assertTrue(parameterData.get(QGISPrintPluginTest.PARAMETER_REQUIRED_NAME).isBoolean(),
                                  String.format("The required field for the parameter %s is not a boolean",
                                                parameterCode));
        }

        assertTrue(requiredCodes.isEmpty(),
                              String.format("The following parameters are missing: %s",
                                            StringUtils.join(requiredCodes, ", ")));
    }



//    /**
//     * Test of execute method, of class FmeDesktopPlugin.
//     */
//    @Test
//    public final void testExecute() {
//        QGISPrintRequest pluginRequest = new QGISPrintRequest();
//        pluginRequest.setFolderOut("/var/extract/orders");
//        pluginRequest.setProductGuid("cf419a79-13d5");
//        pluginRequest.setPerimeter(
//                "POLYGON((6.448008826017048 46.55990536924183,6.920106602414769 46.56124431272321,6.917946995512395 46.379519066609355,6.468224626928717 46.37835458395662,6.448008826017048 46.55990536924183))");
//        QGISPrintPlugin plugin = new QGISPrintPlugin(QGISPrintPluginTest.TEST_INSTANCE_LANGUAGE,
//                                                     this.testParameters);
//
//        QGISPrintResult result = (QGISPrintResult) plugin.execute(pluginRequest, null);
//
//    }

}
