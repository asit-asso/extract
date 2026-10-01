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
package ch.asit_asso.extract.connectors.easysdiv4;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * @author Yves Grasset
 */
public class Easysdiv4Test {
    private static final String CONFIG_FILE_PATH = "connectors/easysdiv4/properties/config.properties";

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

    private static final String[] VALID_PARAMETER_TYPES = new String[] {"email", "pass", "multitext", "text",
                                                                        "numeric"};

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(Easysdiv4Test.class);

    private ConnectorConfig configuration;

    private LocalizedMessages messages;

    private ObjectMapper parameterMapper;

    private String[] requiredParametersCodes;

    private Map<String, String> testParameters;



    public Easysdiv4Test() {

    }


    @BeforeEach
    public final void setUp() {
        this.configuration = new ConnectorConfig(Easysdiv4Test.CONFIG_FILE_PATH);
        this.messages = new LocalizedMessages(Easysdiv4Test.INSTANCE_LANGUAGE);

        final String loginCode = this.configuration.getProperty(Easysdiv4Test.LOGIN_PARAMETER_NAME_PROPERTY);
        final String passwordCode = this.configuration.getProperty(Easysdiv4Test.PASSWORD_PARAMETER_NAME_PROPERTY);
        final String urlCode = this.configuration.getProperty(Easysdiv4Test.URL_PARAMETER_NAME_PROPERTY);
        final String uploadSizeCode = this.configuration.getProperty(Easysdiv4Test.UPLOAD_SIZE_PARAMETER_NAME_PROPERTY);
        final String detailsUrlPattern
                = this.configuration.getProperty(Easysdiv4Test.DETAILS_URL_PARAMETER_NAME_PROPERTY);

        this.requiredParametersCodes = new String[] {loginCode, passwordCode, urlCode, uploadSizeCode,
                                                     detailsUrlPattern};

        this.testParameters = new HashMap<>();
        this.testParameters.put(loginCode, Easysdiv4Test.TEST_LOGIN);
        this.testParameters.put(passwordCode, Easysdiv4Test.TEST_PASSWORD);
        this.testParameters.put(urlCode, Easysdiv4Test.TEST_URL);
        this.testParameters.put(uploadSizeCode, Easysdiv4Test.TEST_UPLOAD_SIZE);
        this.testParameters.put(detailsUrlPattern, Easysdiv4Test.TEST_DETAILS_URL);

        this.parameterMapper = new ObjectMapper();
    }



    /**
     * Test of newInstance method, of class Easysdiv4.
     */
    @Test
    @DisplayName("Create a new instance without parameter values")
    public final void testNewInstanceWithoutParameters() {
        Easysdiv4 instance = new Easysdiv4();
        
        Easysdiv4 result = instance.newInstance(Easysdiv4Test.INSTANCE_LANGUAGE);
        
        assertNotSame(instance, result);
    }



    /**
     * Test of newInstance method, of class Easysdiv4.
     */
    @Test
    @DisplayName("Create a new instance with parameter values")
    public final void testNewInstanceWithParameters() {
        Easysdiv4 instance = new Easysdiv4();

        Easysdiv4 result = instance.newInstance(Easysdiv4Test.INSTANCE_LANGUAGE, this.testParameters);

        assertNotSame(instance, result);
    }



    /**
     * Test of getLabel method, of class Easysdiv4.
     */
    @Test
    @DisplayName("Check the plugin label")
    public final void testGetLabel() {
        Easysdiv4 instance = new Easysdiv4(Easysdiv4Test.INSTANCE_LANGUAGE);
        String expResult = this.messages.getString(Easysdiv4Test.LABEL_STRING_IDENTIFIER);

        String result = instance.getLabel();

        assertEquals(expResult, result);
    }



    /**
     * Test of getCode method, of class Easysdiv4.
     */
    @Test
    @DisplayName("Check the plugin identifier")
    public final void testGetCode() {
        Easysdiv4 instance = new Easysdiv4();

        String result = instance.getCode();

        assertEquals(Easysdiv4Test.EXPECTED_PLUGIN_CODE, result);
    }



    /**
     * Test of getDescription method, of class Easysdiv4.
     */
    @Test
    @DisplayName("Check the plugin description")
    public final void testGetDescription() {
        Easysdiv4 instance = new Easysdiv4(Easysdiv4Test.INSTANCE_LANGUAGE);
        String expResult = this.messages.getString(Easysdiv4Test.DESCRIPTION_STRING_IDENTIFIER);

        String result = instance.getDescription();

        assertEquals(expResult, result);
    }



    /**
     * Test of getHelp method, of class Easysdiv4.
     */
    @Test
    @DisplayName("Check the help file name")
    public final void testGetHelp() {
        Easysdiv4 instance = new Easysdiv4(Easysdiv4Test.INSTANCE_LANGUAGE);
        String expResult = this.messages.getFileContent("help.html");

        String result = instance.getHelp();

        assertEquals(expResult, result);
    }



    /**
     * Test of getPicto method, of class Easysdiv4.
     */
    @Test
    @DisplayName("Check the plugin pictogram")
    public final void testGetPicto() {
        Easysdiv4 instance = new Easysdiv4();

        String result = instance.getPicto();

        assertEquals(Easysdiv4Test.EXPECTED_ICON_CLASS, result);
    }

    @Test
    @DisplayName("Check the default timeout is 5000")
    public final void testGetDefaultTimeout() {
        Easysdiv4 instance = new Easysdiv4();
        var config = instance.createRequestConfigWithTimeout();
        assertEquals(5000, config.getConnectTimeout());
        assertEquals(5000, config.getConnectionRequestTimeout());
        assertEquals(5000, config.getSocketTimeout());
    }


    /**
     * Test of getParams method, of class Easysdiv4.
     */
    @Test
    @DisplayName("Check the plugin parameters")
    public final void testGetParams() {
        Easysdiv4 instance = new Easysdiv4();
        ArrayNode parametersArray = null;

        try {
            parametersArray = this.parameterMapper.readValue(instance.getParams(), ArrayNode.class);

        } catch (IOException exception) {
            this.logger.error("An error occurred when the parameters JSON was parsed.", exception);
            fail("Could not parse the parameters JSON string.");
        }

        assertNotNull(parametersArray);
        assertEquals(this.testParameters.size(), parametersArray.size());
        List<String> requiredCodes = new ArrayList<>(Arrays.asList(requiredParametersCodes));

        for (int parameterIndex = 0; parameterIndex < parametersArray.size(); parameterIndex++) {
            JsonNode parameterData = parametersArray.get(parameterIndex);
            assertTrue(parameterData.hasNonNull(Easysdiv4Test.PARAMETER_CODE_NAME),
                                  String.format("The parameter #%d does not have a code property", parameterIndex));
            String parameterCode = parameterData.get(Easysdiv4Test.PARAMETER_CODE_NAME).textValue();
            assertTrue(StringUtils.isNotBlank(parameterCode),
                                  String.format("The code for parameter #%d is null or blank", parameterIndex));
            assertTrue(requiredCodes.contains(parameterCode),
                                  String.format("The parameter code %s is not expected or has already been defined.",
                                                parameterCode)
            );
            requiredCodes.remove(parameterCode);

            assertTrue(parameterData.hasNonNull(Easysdiv4Test.PARAMETER_LABEL_NAME),
                                  String.format("The parameter %s does not have a label property", parameterCode));
            String label = parameterData.get(Easysdiv4Test.PARAMETER_LABEL_NAME).textValue();
            assertTrue(StringUtils.isNotBlank(label),
                                  String.format("The label for parameter %s is null or blank.", parameterCode));

            assertTrue(parameterData.hasNonNull(Easysdiv4Test.PARAMETER_TYPE_NAME),
                                  String.format("The parameter %s does not have a type property", parameterCode));
            String parameterType = parameterData.get(Easysdiv4Test.PARAMETER_TYPE_NAME).textValue();
            assertTrue(StringUtils.isNotBlank(label)
                                  && ArrayUtils.contains(Easysdiv4Test.VALID_PARAMETER_TYPES, parameterType),
                                  String.format("The type for parameter %s is invalid.", parameterCode));

            assertTrue(parameterData.hasNonNull(Easysdiv4Test.PARAMETER_REQUIRED_NAME),
                                  String.format("The parameter %s does not have a required property", parameterCode));
            assertTrue(parameterData.get(Easysdiv4Test.PARAMETER_REQUIRED_NAME).isBoolean(),
                                  String.format("The required field for the parameter %s is not a boolean",
                                                parameterCode));

            if (parameterType.equals("numeric")) {
                Integer maxValue = null;

                if (parameterData.hasNonNull(Easysdiv4Test.PARAMETER_MAX_VALUE_NAME)) {
                    assertTrue(parameterData.get(Easysdiv4Test.PARAMETER_MAX_VALUE_NAME).isNumber(),
                                          String.format("The maximum value for parameter %s is not a number",
                                                        parameterCode));
                    maxValue = parameterData.get(Easysdiv4Test.PARAMETER_MAX_VALUE_NAME).intValue();
                }

                if (parameterData.hasNonNull(Easysdiv4Test.PARAMETER_MIN_VALUE_NAME)) {
                    assertTrue(parameterData.get(Easysdiv4Test.PARAMETER_MIN_VALUE_NAME).isInt(),
                                          String.format("The minimum value for parameter %s is not an integer",
                                                        parameterCode));
                    int minValue = parameterData.get(Easysdiv4Test.PARAMETER_MIN_VALUE_NAME).intValue();

                    if (maxValue != null) {
                        assertTrue(minValue < maxValue,
                                              String.format(
                                                      "The minimum value for parameter %s must be less than the maximum value",
                                                      parameterCode));
                    }
                }

                if (parameterData.hasNonNull(Easysdiv4Test.PARAMETER_STEP_NAME)) {
                    assertTrue(parameterData.get(Easysdiv4Test.PARAMETER_STEP_NAME).isInt(),
                                          String.format("The step value for parameter %s is not an integer",
                                                        parameterCode));
                }

            } else {
                assertTrue(parameterData.hasNonNull(Easysdiv4Test.PARAMETER_MAX_LENGTH_NAME),
                                      String.format("The parameter %s does not have a maximum length property",
                                                    parameterCode));
                assertTrue(parameterData.get(Easysdiv4Test.PARAMETER_MAX_LENGTH_NAME).isInt(),
                                      String.format("The maximum length for parameter %s is not an integer",
                                                    parameterCode));
                int maxLength = parameterData.get(Easysdiv4Test.PARAMETER_MAX_LENGTH_NAME).intValue();
                assertTrue(maxLength > 0,
                                      String.format("The maximum length of parameter %s is not strictly positive",
                                                    parameterCode)
                );
            }
        }

        assertTrue(requiredCodes.isEmpty(),
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



    /**
     * Builds an in-memory XML document from a string, mirroring the order XML structure returned by an
     * easySDI v4 server: a client contact address containing the address, phone and e-mail sub-elements
     * read by {@link Easysdiv4#buildContactDetailsFromXpath}.
     */
    private Document buildOrderDocument(final String email) throws Exception {
        return this.buildOrderDocument(email, "");
    }



    /**
     * Builds an in-memory XML document mirroring the order XML structure returned by an easySDI v4 server, with
     * every element name carrying the given prefix. The connector parses the server response with a
     * non-namespace-aware parser, so <code>sdi:</code> is a literal part of the element names it looks up.
     *
     * @param email  the content of the e-mail element, or <code>null</code> to omit the element
     * @param prefix the prefix of the structural element names, such as <code>sdi:</code>, or an empty string
     */
    private Document buildOrderDocument(final String email, final String prefix) throws Exception {
        String xml = "<" + prefix + "orders xmlns:sdi=\"http://www.easysdi.org/2011/sdi\">"
                + "<" + prefix + "order guid=\"ORDER-1\">"
                + "<" + prefix + "client guid=\"CLIENT-1\">"
                + "<" + prefix + "name>Jean Dupont</" + prefix + "name>"
                + "<" + prefix + "contact><" + prefix + "address>"
                + "<sdi:addressstreet1>Rue de la Gare 1</sdi:addressstreet1>"
                + "<sdi:zip>1880</sdi:zip>"
                + "<sdi:locality>Bex</sdi:locality>"
                + (email != null ? "<sdi:email>" + email + "</sdi:email>" : "")
                + "</" + prefix + "address></" + prefix + "contact>"
                + "</" + prefix + "client>"
                + "</" + prefix + "order>"
                + "</" + prefix + "orders>";

        return DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }



    private String extractClientEmail(final Document document) {
        return new Easysdiv4().buildContactDetailsFromXpath(document, "//order[@guid='ORDER-1']/client/contact/address")
                .getEmail();
    }



    /**
     * The client's e-mail address is read from the order's contact address and exposed on its own, next to the
     * legacy free-text details (issue #366).
     */
    @Test
    @DisplayName("The client's e-mail address is extracted from the order's contact address")
    public final void testClientEmailExtraction() throws Exception {
        Document document = this.buildOrderDocument("jean.dupont@example.com");

        assertEquals("jean.dupont@example.com", this.extractClientEmail(document));
    }



    /**
     * An order without an e-mail element yields no client e-mail address rather than an error (issue #366).
     */
    @Test
    @DisplayName("A missing client e-mail address resolves to null, not an error")
    public final void testClientEmailExtractionWithoutEmail() throws Exception {
        Document document = this.buildOrderDocument(null);

        assertNull(this.extractClientEmail(document));
    }



    /**
     * The real easySDI v4 response prefixes every element with <code>sdi:</code>, which the connector's
     * namespace-unaware XPath evaluation matches by local name (issue #366).
     */
    @Test
    @DisplayName("The client's e-mail address is extracted from a fully sdi-prefixed order XML")
    public final void testClientEmailExtractionWithPrefixedElements() throws Exception {
        Document document = this.buildOrderDocument("jean.dupont@example.com", "sdi:");

        assertEquals("jean.dupont@example.com", this.extractClientEmail(document));
    }



    /**
     * Surrounding whitespace in the e-mail element must not end up in the recipient address (issue #366).
     */
    @Test
    @DisplayName("The client's e-mail address is trimmed")
    public final void testClientEmailExtractionTrimsValue() throws Exception {
        Document document = this.buildOrderDocument("\n  jean.dupont@example.com \n");

        assertEquals("jean.dupont@example.com", this.extractClientEmail(document));
    }



    /**
     * A value that is not an e-mail address (such as the client's name) must not be propagated as the client's
     * address, where it would later be rejected as a malformed recipient (issue #366).
     */
    @Test
    @DisplayName("A non-address value in the e-mail element yields no client e-mail address")
    public final void testClientEmailExtractionRejectsNonAddressValue() throws Exception {
        Document document = this.buildOrderDocument("Jean Dupont");

        assertNull(this.extractClientEmail(document));
    }



    @Test
    @DisplayName("The e-mail address shape check accepts addresses and rejects other texts")
    public final void testLooksLikeEmailAddress() {
        assertTrue(Easysdiv4.looksLikeEmailAddress("jean.dupont@example.com"));
        assertTrue(Easysdiv4.looksLikeEmailAddress("j@x"));
        assertFalse(Easysdiv4.looksLikeEmailAddress(null));
        assertFalse(Easysdiv4.looksLikeEmailAddress(""));
        assertFalse(Easysdiv4.looksLikeEmailAddress("Jean Dupont"));
        assertFalse(Easysdiv4.looksLikeEmailAddress("jean.dupont"));
        assertFalse(Easysdiv4.looksLikeEmailAddress("@example.com"));
        assertFalse(Easysdiv4.looksLikeEmailAddress("jean@"));
        assertFalse(Easysdiv4.looksLikeEmailAddress("jean@@example.com"));
        assertFalse(Easysdiv4.looksLikeEmailAddress("jean dupont@example.com"));
    }

}
