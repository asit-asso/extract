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

import ch.asit_asso.extract.connectors.common.IConnector;
import ch.asit_asso.extract.connectors.common.IConnectorImportResult;
import ch.asit_asso.extract.connectors.common.IExportRequest;
import ch.asit_asso.extract.connectors.sample.utils.RequestUtils;
import ch.asit_asso.extract.connectors.sample.utils.ZipUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;



/**
 * DESCRIBES HERE WHAT THE CONNECTOR DO
 * E.G : A plugin that imports orders from a  server and exports their result.
 *
 * @author Florent Krin
 */
public class SampleConnector implements IConnector {

    /**
     * CHANGE THE PLUGIN NAME "sample" IN THIS PATH (i.e sample)
     * The path to the configuration of this plugin.
     */
    private static final String CONFIG_FILE_PATH = "connectors/sample/properties/config.properties";

    /**
     * The status code returned to tell that an HTTP request resulted in the creation of a resource.
     */
    private static final int CREATED_HTTP_STATUS_CODE = 201;

    /**
     * The port that is used by default for HTTP requests.
     */
    private static final int DEFAULT_HTTP_PORT = 80;

    /**
     * The port that is used by default for secure HTTP requests.
     */
    private static final int DEFAULT_HTTPS_PORT = 443;

    /**
     * The ASCII code of the last character that does not represent a symbol.
     */
    private static final int LAST_CONTROL_CHARACTER_CODE = 32;

    /**
     * The ASCII code of the last character in the standard character set.
     */
    private static final int LAST_STANDARD_ASCII_CHARACTER_CODE = 128;

    /**
     * The status code returned to tell that an HTTP request succeeded.
     */
    private static final int SUCCESS_HTTP_STATUS_CODE = 200;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(SampleConnector.class);

    /**
     * CHANGE THE CODE THAT IDENTIFIES THIS CONNECTOR
     * The code that uniquely identifies this connector.
     */
    private final String code = "sample";

    /**
     * The parameters values to communicate with a particular easySDI v4 server.
     */
    private Map<String, String> inputs;

    /**
     * The plugin configuration.
     */
    private ConnectorConfig config;

    /**
     * The messages to the user in the language used by the user interface.
     */
    private LocalizedMessages messages;



    /**
     * Creates a new connector plugin instance with default parameters.
     * No changes needed in this method
     */
    public SampleConnector() {
        this.config = new ConnectorConfig(SampleConnector.CONFIG_FILE_PATH);
        this.messages = new LocalizedMessages();
    }



    /**
     * Creates a new connector plugin instance with default connection parameters.
     * No changes needed in this method
     * @param language the string that identifies the language used by the user interface
     */
    public SampleConnector(final String language) {
        this.config = new ConnectorConfig(SampleConnector.CONFIG_FILE_PATH);
        this.messages = new LocalizedMessages(language);
    }



    /**
     * Creates a new connector plugin instance with the default user interface langauge.
     * No changes needed in this method
     * @param parametersValues the parameters values to connect to the easySDI v4 server
     */
    public SampleConnector(final Map<String, String> parametersValues) {
        this();
        this.inputs = parametersValues;
    }



    /**
     * Creates a new connector plugin instance.
     * No changes needed in this method
     * @param language         the string that identifies the language used by the user interface
     * @param parametersValues the parameters values to connect to the  server
     */
    public SampleConnector(final String language, final Map<String, String> parametersValues) {
        this(language);
        this.inputs = parametersValues;
    }



    @Override
    public final SampleConnector newInstance(final String language) {
        return new SampleConnector(language);
    }



    @Override
    public final SampleConnector newInstance(final String language, final Map<String, String> parametersValues) {
        return new SampleConnector(language, parametersValues);
    }


    /**
     * Gets the user-friendly name of this connector plugin.
     *
     * @return the label
     */
    @Override
    public final String getLabel() {
        return this.messages.getString("plugin.label");
    }


    /**
     * Returns the string that uniquely identify this connector plugin.
     *
     * @return the connector code
     */
    @Override
    public final String getCode() {
        return this.code;
    }


    /**
     * Returns a text explaining what this connector plugin does.
     *
     * @return the description text
     */
    @Override
    public final String getDescription() {
        return this.messages.getString("plugin.description");
    }


    /**
     * Returns a text explaining how to use this connector plugin.
     *
     * @return the help text
     */
    @Override
    public final String getHelp() {
        return this.messages.getString("plugin.help");
    }


    /**
     * Gets the path of the icon for this connector plugin.
     *
     * @return the path of the image file
     */
    @Override
    public final String getPicto() {
        return "";
    }


    /**
     * Gets the description of the parameters that must be provided to use this connector.
     * Important :
     * "type" can be multitext / text / boolean / pass / numeric
     * "code" is the parameter name défined in the config file
     * "label" is the parameter label defined in messages.properties
     * AN SAMPLE IS WRITED IN THIS METHOD
     * @return a JSON string containing the definition of the parameters
     */
    @Override
    public final String getParams() {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode parametersNode = mapper.createArrayNode();

        /*
         * THE FOLLOWING SAMPLE ADDS TWO PARAMETERS IN ARRAY
         * IF CONNECTOR ACCEPTS NO PARAMETERS THEN RETURN AN EMPTY ARRAY
         */
         /*
        ObjectNode serviceUrlNode = parametersNode.addObject();
        serviceUrlNode.put("code", this.config.getProperty("code.serviceUrl"));
        serviceUrlNode.put("label", this.messages.getString("label.serviceUrl"));
        serviceUrlNode.put("type", "text");
        serviceUrlNode.put("req", true);
        serviceUrlNode.put("maxlength", 255);

        ObjectNode loginNode = parametersNode.addObject();
        loginNode.put("code", this.config.getProperty("code.login"));
        loginNode.put("label", this.messages.getString("label.login"));
        loginNode.put("type", "text");
        loginNode.put("req", true);
        loginNode.put("maxlength", 50);

        ObjectNode passwordNode = parametersNode.addObject();
        passwordNode.put("code", this.config.getProperty("code.password"));
        passwordNode.put("label", this.messages.getString("label.password"));
        passwordNode.put("type", "pass");
        passwordNode.put("req", true);
        passwordNode.put("maxlength", 50);

        ObjectNode uploadSizeNode = parametersNode.addObject();
        uploadSizeNode.put("code", this.config.getProperty("code.uploadSize"));
        uploadSizeNode.put("label", this.messages.getString("label.uploadSize"));
        uploadSizeNode.put("type", "numeric");
        uploadSizeNode.put("req", false);
        uploadSizeNode.put("min", 1);
        uploadSizeNode.put("step", 1);

        ObjectNode externalUrlPatternNode = parametersNode.addObject();
        externalUrlPatternNode.put("code", this.config.getProperty("code.detailsUrlPattern"));
        externalUrlPatternNode.put("label", this.messages.getString("label.detailsUrlPattern"));
        externalUrlPatternNode.put("type", "text");
        externalUrlPatternNode.put("req", false);
        externalUrlPatternNode.put("maxlength", 255);
        */

        try {
            return mapper.writeValueAsString(parametersNode);

        } catch (JsonProcessingException exception) {
            logger.error("An error occurred when the parameters description array was converted to JSON.", exception);
            return null;
        }
    }

    /**
     * Fetches the commands based on the provided parameters.
     * AN SAMPLE IS WRITTEN IN THIS METHOD BUT MUST BE REWRITTEN ACCORDING THE CONNECTOR
     * THIS CODE SUPPOSES THAT ORDERS ARE TRANSMITTED IN XML FORMAT
     * @return the commands
     */
    @Override
    public final IConnectorImportResult importCommands() {
        this.logger.debug("Importing commands");

        ConnectorImportResult result;

        try {
            //call getOrder service
            this.logger.debug("Fetching order XML from service");
            result = this.callGetOrderService(String.format("%s.%s", inputs.get(config.getProperty("code.serviceUrl")),
                            config.getProperty("getOrders.method")), inputs.get(config.getProperty("code.login")),
                    inputs.get(config.getProperty("code.password")));

        } catch (Exception exception) {
            this.logger.error("The import commands has failed", exception);
            result = new ConnectorImportResult();
            result.setStatus(false);
            result.setErrorMessage(String.format("%s : %s", this.messages.getString("importorder.exception"),
                    exception.getMessage()));
        }

        this.logger.info("output result : " + result.toString());
        return result;
    }


    /**
     * Sends the result obtained by processing a command to the server that produced it.
     * AN SAMPLE IS WRITTEN IN THIS METHOD BUT MUST BE REWRITTEN ACCORDING THE CONNECTOR
     * THIS CODE SUPPOSES THAT REMOTE SERVER ACCEPTS RESULT IN XML FORMAT
     * @param request the result to export
     * @return the object describing the export result (code, message, error details)
     */
    @Override
    public final ExportResult exportResult(final IExportRequest request) {

        this.logger.debug("Exporting result orders (setProduct method)");

        ExportResult exportResult = null;
        String templatePath = null;

        if (request.isRejected()) {
            templatePath = config.getProperty("setProduct.rejection.filepath");

        } else if (request.getStatus().equals("FINISHED")) {
            templatePath = config.getProperty("setProduct.success.filepath");
        }

        this.logger.debug("use template xml {} for sending with setProduct", templatePath);

        final InputStream templateXMLStream = this.getClass().getClassLoader().getResourceAsStream(templatePath);
        File outputFile = null;

        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document document = builder.parse(new InputSource(templateXMLStream));
            final Node firstChild = document.getFirstChild();

            final XPathFactory xPathfactory = XPathFactory.newInstance();
            final XPath xpath = xPathfactory.newXPath();

            //Read Order list
            final XPathExpression exprProduct = xpath.compile(config.getProperty("setProduct.xpath.product"));
            final XPathExpression exprOrder = xpath.compile(config.getProperty("setProduct.xpath.order"));
            final XPathExpression exprRemark = xpath.compile(config.getProperty("setProduct.xpath.remark"));

            final NodeList productNodeList = (NodeList) exprProduct.evaluate(document, XPathConstants.NODESET);
            final NodeList orderNodeList = (NodeList) exprOrder.evaluate(document, XPathConstants.NODESET);
            NodeList remarkNodeList = (NodeList) exprRemark.evaluate(document, XPathConstants.NODESET);

            if (productNodeList != null && productNodeList.getLength() > 0) {
                this.logger.debug("set product guid {}", request.getProductGuid());
                ((Element) productNodeList.item(0)).setAttribute(config.getProperty("setProduct.attribute.guid"),
                        request.getProductGuid());
            }
            if (orderNodeList != null && orderNodeList.getLength() > 0) {
                this.logger.debug("set order guid {}", request.getOrderGuid());
                orderNodeList.item(0).setNodeValue(request.getOrderGuid());
                ((Element) firstChild).getElementsByTagName("sdi:order").item(0).setTextContent(request.getOrderGuid());
            }
            if (remarkNodeList != null && remarkNodeList.getLength() > 0 && request.getRemark() != null) {
                this.logger.debug("set remark {}", request.getRemark());
                final String escapedRemark = this.escapeExtendedCharactersForXml(request.getRemark(), true);
                final CDATASection remarkSection = document.createCDATASection(escapedRemark);
                ((Element) firstChild).getElementsByTagName("sdi:remark").item(0).appendChild(remarkSection);
            }

            if (!request.isRejected()) {
                outputFile = this.prepareOutputFileForRequest(request);

                if (outputFile == null) {
                    exportResult = new ExportResult();
                    exportResult.setSuccess(false);
                    exportResult.setResultCode("-1");
                    exportResult.setResultMessage(this.messages.getString("exportresult.prerequisite.error"));
                    exportResult.setErrorDetails(this.messages.getString("exportresult.prerequisite.nofile"));

                    return exportResult;
                }

                final int uploadLimit = NumberUtils.toInt(inputs.get(config.getProperty("code.uploadSize")));
                final long fileSizeInMB = FileUtils.sizeOf(outputFile) / FileUtils.ONE_MB;

                if (uploadLimit > 0 && fileSizeInMB > uploadLimit) {
                    final String detailsMessage = String.format(this.messages.getString("exportresult.upload.tooLarge"),
                            fileSizeInMB, uploadLimit);
                    exportResult = new ExportResult();
                    exportResult.setSuccess(false);
                    exportResult.setResultCode("-2");
                    exportResult.setResultMessage(this.messages.getString("exportresult.prerequisite.error"));
                    exportResult.setErrorDetails(detailsMessage);

                    return exportResult;
                }

                final String outputFileName = outputFile.getName();
                this.logger.debug("set filename {}", outputFileName);
                final String escapedFileName = this.escapeExtendedCharactersForXml(outputFileName);
                ((Element) firstChild).getElementsByTagName("sdi:filename").item(0).setTextContent(escapedFileName);
            }

            this.logger.debug("call setProduct");
            final String exportUrl = String.format("%s.%s", inputs.get(config.getProperty("code.serviceUrl")),
                    config.getProperty("setProduct.method"));
            exportResult = this.callSetProductService(document, exportUrl, inputs.get(config.getProperty("code.login")),
                    inputs.get(config.getProperty("code.password")), outputFile);

        } catch (Exception exception) {
            this.logger.error("The order export has failed.", exception);

            exportResult = new ExportResult();
            exportResult.setSuccess(false);
            exportResult.setResultCode("-1");
            exportResult.setResultMessage(String.format("%s: %s",
                    this.messages.getString("exportresult.executing.failed"), exception.getMessage()));
            exportResult.setErrorDetails(exception.getMessage());

        } finally {

            if (outputFile != null && outputFile.exists()) {
                this.logger.debug("Deleting output file…");

                if (!outputFile.delete()) {
                    this.logger.debug("Could not delete output file {}.", outputFile.getAbsolutePath());
                }
            }
        }

        return exportResult;
    }


    /**
     *
     * FOLLOWINGS METHODS ARE OPTIONALS AND MUST BE CHANGED USELESS THERE ARE USEFUL FOR THE NEW CONNECTOR
     * THIS METHODS ARE MOSTLY USED TO PROCESS OR TRANSFORM DATA IN XML FORMAT     *
     */

    /**
     * Obtains the message that explains the HTTP code return by an operation through this plugin.
     *
     * @param httpCode the returned HTTP code
     * @return the string that describe the HTTP status
     */
    private String getMessageFromHttpCode(final int httpCode) {
        final String genericErrorMessage = this.messages.getString("error.message.generic");
        final String httpErrorMessage = this.messages.getString(String.format("httperror.message.%d", httpCode));

        if (httpErrorMessage == null) {
            return genericErrorMessage;
        }

        return String.format("%s - %s", genericErrorMessage, httpErrorMessage);
    }



    /**
     * Obtains all the XML items that match an XPath expression.
     *
     * @param document the XML document to parse
     * @param xmlPath  the XPath expression
     * @return a node list that contains the found items
     */
    private NodeList getXMLNodeListFromXPath(final Document document, final String xmlPath) {

        NodeList nodeList = null;
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();

        try {
            XPathExpression expr = xpath.compile(xmlPath);
            nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            this.logger.error("Unable to retrieve xml node from xpath.", e);
        }

        return nodeList;
    }



    /**
     * Gets the content of the first XML element that matches an XPath expression.
     *
     * @param document    the XML document to parse
     * @param xpathString the XPath expression
     * @return the text content of the first matching item, or an empty string if no element matches
     */
    private String getXMLNodeLabelFromXpath(final Document document, final String xpathString) {

        try {
            final XPathFactory xPathfactory = XPathFactory.newInstance();
            final XPath xpath = xPathfactory.newXPath();
            final XPathExpression expr = xpath.compile(xpathString);
            final NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);

            if (nodeList != null && nodeList.getLength() > 0) {
                return nodeList.item(0).getTextContent();
            }

        } catch (XPathExpressionException exc) {
            this.logger.error("The attribute {} could not be retrieved", xpathString);
        }

        return "";
    }



    /**
     * Creates an address string from the content of an XML element.
     *
     * @param document    the XML document to parse
     * @param xpathString the XPath expression that locates the element containing the address information
     * @return the address string
     */
    private String buildAddressDetailsFromXpath(final Document document, final String xpathString) {

        List<String> details = new ArrayList<>();

        try {
            final XPathFactory xPathfactory = XPathFactory.newInstance();
            final XPath xpath = xPathfactory.newXPath();
            final XPathExpression expr = xpath.compile(xpathString);
            final NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);

            if (nodeList != null && nodeList.getLength() > 0) {
                final Element addressNode = (Element) nodeList.item(0);
                final NodeList address1Node = addressNode.getElementsByTagName("sdi:addressstreet1");
                final NodeList address2Node = addressNode.getElementsByTagName("sdi:addressstreet2");
                final NodeList zipCodeNode = addressNode.getElementsByTagName("sdi:zip");
                final NodeList localityNode = addressNode.getElementsByTagName("sdi:locality");
                final NodeList emailNode = addressNode.getElementsByTagName("sdi:email");
                final NodeList phoneNode = addressNode.getElementsByTagName("sdi:phone");

                if (address1Node != null && address1Node.getLength() > 0) {
                    final String address1Text = address1Node.item(0).getTextContent();

                    if (StringUtils.isNotEmpty(address1Text)) {
                        details.add(address1Text);
                    }
                }

                if (address2Node != null && address2Node.getLength() > 0) {
                    final String address2Text = address2Node.item(0).getTextContent();

                    if (StringUtils.isNotEmpty(address2Text)) {
                        details.add(address2Text);
                    }
                }

                String zipCodeText = null;
                String localityText = null;

                if (zipCodeNode != null && zipCodeNode.getLength() > 0) {
                    zipCodeText = zipCodeNode.item(0).getTextContent();
                }

                if (localityNode != null && localityNode.getLength() > 0) {
                    localityText = localityNode.item(0).getTextContent();
                }

                if (StringUtils.isEmpty(zipCodeText)) {

                    if (StringUtils.isNotEmpty(localityText)) {
                        details.add(localityText);
                    }

                } else if (StringUtils.isEmpty(localityText)) {
                    details.add(zipCodeText);

                } else {
                    details.add(String.format("%s %s", zipCodeText, localityText));
                }

                if (phoneNode != null && phoneNode.getLength() > 0) {
                    final String phoneText = phoneNode.item(0).getTextContent();
                    this.logger.debug("Phone node content is {}", phoneText);

                    if (StringUtils.isNotEmpty(phoneText)) {
                        details.add(phoneText);
                    }
                }

                if (emailNode != null && emailNode.getLength() > 0) {
                    final String emailText = emailNode.item(0).getTextContent();
                    this.logger.debug("E-mail node content is {}", emailText);

                    if (StringUtils.isNotEmpty(emailText)) {
                        details.add(emailText);
                    }
                }
            }

        } catch (XPathExpressionException exc) {
            this.logger.error("The address details could not be retrieved", exc);
        }

        this.logger.debug("Address details are:\n{}", StringUtils.join(details, "\r\n"));
        return StringUtils.join(details, "\r\n");
    }



    /**
     * Sends the document describing the result to export to the server.
     *
     * @param xmlDocument the XML document that contains the information about the processing result
     * @param url         the address where the document must be sent
     * @param login       the user name to authenticate with the server
     * @param password    the password to authenticate with the server
     * @param resultFile  the file generated by the processing
     * @return an export result object describing whether the export succeeded
     */
    private ExportResult callSetProductService(final Document xmlDocument, final String url, final String login,
            final String password, final File resultFile) {

        try {
            final String xmlString = this.createExportXmlString(xmlDocument).replaceAll("\\r", StringUtils.EMPTY);
            this.logger.debug("Sent XML:\n{}", xmlString);

            final URI targetUri = new URI(url);
            final HttpHost targetServer = this.getHostFromUri(targetUri);

            return this.sendExportRequest(targetServer, targetUri, login, password, xmlString,
                    resultFile);

        } catch (Exception exception) {
            ExportResult exportResult = new ExportResult();
            exportResult.setSuccess(false);
            exportResult.setResultCode("-1");
            exportResult.setResultMessage(this.messages.getString("exportresult.executing.failed"));
            exportResult.setErrorDetails(exception.getMessage());
            this.logger.warn("The order export has failed.", exception);

            return exportResult;
        }
    }



    /**
     * Converts an XML export document to a string.
     *
     * @param xmlDocument the XML document to convert
     * @return the XML string
     * @throws TransformerException the document could be converted from XML to string
     * @throws IOException          the XML string could not be written
     */
    private String createExportXmlString(final Document xmlDocument) throws TransformerException, IOException {
        DOMSource domSource = new DOMSource(xmlDocument);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        try (StringWriter writer = new StringWriter()) {
            StreamResult transformResult = new StreamResult(writer);
            transformer.transform(domSource, transformResult);

            return writer.toString();
        }
    }



    /**
     * Sends the export data to the server.
     *
     * @param targetServer the host to send the data to
     * @param targetUri    the URL to send export data
     * @param login        the user name to authenticate with the server
     * @param password     the password to authenticate with the server
     * @param exportXml    the XML string that describes the result to export
     * @param resultFile   the file generated by the request processing
     * @return the file generated by the processing
     * @throws IOException                  the plugin could not communicate with the server
     * @throws SAXException                 the response from the server could not be parsed
     * @throws ParserConfigurationException the response parser could not be instantiated
     */
    private ExportResult sendExportRequest(final HttpHost targetServer, final URI targetUri, final String login,
            final String password, final String exportXml, final File resultFile)
            throws IOException, SAXException, ParserConfigurationException {

        try (final CloseableHttpClient client = this.getHttpClient(targetServer, login, password)) {
            final HttpPost httpPost = this.createPostRequest(targetUri);
            httpPost.setEntity(this.buildExportEntity(exportXml, resultFile));
            final HttpClientContext clientContext = this.getBasicAuthenticationContext(targetServer);

            try (final CloseableHttpResponse response = client.execute(httpPost, clientContext)) {
                final int statusCode = response.getStatusLine().getStatusCode();
                this.logger.info("The export request returned with the HTTP status {}.", statusCode);
                System.out.println("response = " + response);

                return this.parseExportResponse(response);
            }
        }

    }



    /**
     * Creates the content of the HTTP export request with the description document and the result file.
     *
     * @param exportXml  the XML document that describes the result of the request processing
     * @param resultFile the file that contains the generated data for the request
     * @return the HTTP entity to export
     */
    private HttpEntity buildExportEntity(final String exportXml, final File resultFile) {
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        entityBuilder.setCharset(StandardCharsets.UTF_8);
        entityBuilder.addTextBody("xml", exportXml, ContentType.TEXT_XML);

        if (resultFile != null) {
            entityBuilder.addBinaryBody("file", resultFile);
        }

        entityBuilder.setContentType(ContentType.MULTIPART_FORM_DATA);
        return entityBuilder.build();
    }



    /**
     * Processes what the server sent back as a response to an export request.
     *
     * @param response the response from the server
     * @return the parsed result of the export
     * @throws IOException                  the response from the server could not be read
     * @throws SAXException                 the response from the server could not be parsed
     * @throws ParserConfigurationException the response parser could not be instantiated
     */
    private ExportResult parseExportResponse(final HttpResponse response)
            throws IOException, SAXException, ParserConfigurationException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(response.getEntity().getContent()));
        Node firstChild = document.getFirstChild();

        NodeList codeNodeList = document.getElementsByTagName(config.getProperty("setProductResult.node.code"));
        NodeList messageNodeList = document.getElementsByTagName(config.getProperty("setProductResult.node.message"));
        NodeList detailsNodeList = document.getElementsByTagName(config.getProperty("setProductResult.node.details"));

        ExportResult exportResult = new ExportResult();

        if (codeNodeList != null && codeNodeList.getLength() > 0) {
            exportResult.setResultCode(codeNodeList.item(0).getTextContent());
        }

        if (messageNodeList != null && messageNodeList.getLength() > 0) {
            exportResult.setResultMessage(messageNodeList.item(0).getTextContent());
        }

        if (firstChild.getNodeName().equals("sdi:success")) {
            exportResult.setSuccess(true);

        } else if (firstChild.getNodeName().equals("sdi:exception")) {
            exportResult.setSuccess(false);

            if (detailsNodeList != null && detailsNodeList.getLength() > 0) {
                exportResult.setErrorDetails(detailsNodeList.item(0).getTextContent());
            }
        }

        System.out.println("success = " + exportResult.toString());
        return exportResult;
    }



    /**
     * Builds an HTTP request to be sent with the GET method, adding proxy information if it is defined.
     *
     * @param url the address that the GET request must be sent to
     * @return the HTTP GET request object
     */
    private HttpGet createGetRequest(final URI url) {
        assert url != null : "The target url cannot be null.";

        this.logger.debug("Creating HTTP GET request for URL {}.", url);

        return (HttpGet) this.addProxyInfoToRequest(new HttpGet(url));
    }



    /**
     * Builds an HTTP request to be sent with the POST method, with proxy information if it is defined.
     *
     * @param url the address that the POST request must be sent to
     * @return the HTTP POST request object
     */
    private HttpPost createPostRequest(final URI url) {
        assert url != null : "The target url cannot be null.";

        this.logger.debug("Creating HTTP GET request for URL {}.", url);

        return (HttpPost) this.addProxyInfoToRequest(new HttpPost(url));
    }



    /**
     * Adds information about the proxy server to use to communicate with the easySDI v4 server,
     * if appropriate.
     *
     * @param request the request to send to the easySDI v4 server
     * @return the request object with proxy information if appropriate
     */
    private HttpRequestBase addProxyInfoToRequest(final HttpRequestBase request) {
        assert request != null : "The request cannot be null.";

        final RequestConfig proxyConfig = this.getProxyConfiguration();

        if (proxyConfig != null) {
            this.logger.debug("Using the proxy server set in the system properties.");
            request.setConfig(proxyConfig);
        }

        return request;
    }



    /**
     * Obtains an object that contains authentication for the easySDI v4 server and, if appropriate, for
     * the proxy server.
     *
     * @param targetHost     the easySDI v4 server
     * @param targetLogin    the user name to authenticate with the easySDI v4 server
     * @param targetPassword the password to authenticate with the easySDI v4 server
     * @return the credentials provider object
     */
    private CredentialsProvider getCredentialsProvider(final HttpHost targetHost, final String targetLogin,
            final String targetPassword) {

        assert targetHost != null : "The target host cannot be null.";

        final CredentialsProvider credentials = new BasicCredentialsProvider();
        this.logger.debug("Setting credentials for the target server.");
        this.addCredentialsToProvider(credentials, targetHost, targetLogin, targetPassword);

        final HttpHost proxyHost = this.getProxyHost();

        if (proxyHost != null) {
            this.logger.debug("Setting credentials for the proxy server.");

            try {
                PropertiesConfiguration configuration = this.getApplicationConfiguration();
                final String proxyLogin = configuration.getString("http.proxyUser");
                final String proxyPassword = configuration.getString("http.proxyPassword");
                this.addCredentialsToProvider(credentials, proxyHost, proxyLogin, proxyPassword);

            } catch (ConfigurationException exception) {
                this.logger.error("Cannot read the application configuration. No proxy credentials set.", exception);
            }
        }

        return credentials;
    }



    /**
     * Obtains an object that holds the configuration of the easySDI v4 connector plugin.
     *
     * @return the configuration object
     * @throws ConfigurationException the configuration could not be parsed
     */
    private PropertiesConfiguration getApplicationConfiguration() throws ConfigurationException {
        return new PropertiesConfiguration("application.properties");
    }



    /**
     * Adds authentication information for a server to an existing credentials provider.
     *
     * @param provider the credentials provider to add the credentials to
     * @param host     the server to autenticate with
     * @param login    the user name to authenticate with the server
     * @param password the password to authenticate with the server
     * @return the credential provider with the added credentials
     */
    private CredentialsProvider addCredentialsToProvider(final CredentialsProvider provider, final HttpHost host,
            final String login, final String password) {
        assert host != null : "The host cannot be null.";

        if (!StringUtils.isEmpty(login) && password != null) {
            provider.setCredentials(new AuthScope(host), new UsernamePasswordCredentials(login, password));
            this.logger.debug("Credentials added for host {}:{}.", host.getHostName(), host.getPort());

        } else {
            this.logger.debug("No credentials set for host {}.", host.toHostString());
        }

        return provider;
    }



    /**
     * Obtains a client object to make authenticated HTTP requests.
     *
     * @param targetHost     the server to send the requests to
     * @param targetLogin    the user name to authenticate with the server
     * @param targetPassword the password to authenticate with the server
     * @return the HTTP client
     */
    private CloseableHttpClient getHttpClient(final HttpHost targetHost, final String targetLogin,
            final String targetPassword) {
        assert targetHost != null : "The target host cannot be null";

        final CredentialsProvider credentials = this.getCredentialsProvider(targetHost, targetLogin, targetPassword);

        return HttpClients.custom().setDefaultCredentialsProvider(credentials).build();
    }



    /**
     * Obtains the configuration of the proxy server to use to connect to the easySDI v4 server.
     *
     * @return the request configuration that uses the defined proxy, or <code>null</code> if no proxy is defined
     */
    private RequestConfig getProxyConfiguration() {
        final HttpHost proxy = this.getProxyHost();

        if (proxy == null) {
            return null;
        }

        return RequestConfig.custom().setProxy(proxy).build();
    }



    /**
     * Obtains the proxy server to use to connect to the easySDI v4 server, if any.
     *
     * @return the proxy server host, or <code>null</code> if no proxy is defined
     */
    private HttpHost getProxyHost() {

        try {
            PropertiesConfiguration applicationConfiguration = this.getApplicationConfiguration();
            final String proxyHostName = applicationConfiguration.getString("http.proxyHost");
            this.logger.debug("The proxy host set in the system properties is {}.", proxyHostName);

            if (proxyHostName == null) {
                this.logger.debug("No proxy set in the system properties.");
                return null;
            }

            final int proxyPort = applicationConfiguration.getInteger("http.proxyPort", -1);
            this.logger.debug("The proxy port in the system properties is {}.", proxyPort);

            return (proxyPort < 0) ? new HttpHost(proxyHostName) : new HttpHost(proxyHostName, proxyPort);

        } catch (ConfigurationException exception) {
            this.logger.error("Cannot read the application configuration. Proxy configuration (if any) ignored.",
                    exception);
            return null;
        }
    }



    /**
     * Sends the document requesting orders to process.
     *
     * @param url      the address to send the import request to
     * @param login    the user name to authenticate with the server
     * @param password the password to authenticate with the server
     * @return the result of the import
     * @throws Exception an error prevented the import to be completed
     */
    private ConnectorImportResult callGetOrderService(final String url, final String login, final String password)
            throws Exception {
        this.logger.debug("Getting orders from service {}.", url);
        URI targetUri = new URI(url);
        HttpHost targetServer = this.getHostFromUri(targetUri);

        return this.sendImportRequest(targetServer, targetUri, login, password);
    }



    /**
     * Obtains a host object based on an address.
     *
     * @param uri the address that contains the host information
     * @return the HTTP host for the server that the URL points to
     */
    private HttpHost getHostFromUri(final URI uri) {
        final String hostName = uri.getHost();
        int port = uri.getPort();
        final String scheme = uri.getScheme();

        if (port < 0) {

            switch (scheme.toLowerCase()) {

                case "http":
                    this.logger.debug("No port in URL for host {}. Using HTTP default 80.", hostName);
                    port = SampleConnector.DEFAULT_HTTP_PORT;
                    break;

                case "https":
                    this.logger.debug("No port in URL for host {}. Using HTTPS default 443.", hostName);
                    port = SampleConnector.DEFAULT_HTTPS_PORT;
                    break;

                default:
                    this.logger.error("Unsupported protocol {}.", scheme);
                    return null;
            }
        }

        return new HttpHost(hostName, port, scheme);
    }



    /**
     * Sends the import request data to the easySDI v4 server.
     *
     * @param targetServer the HTTP host that represents the easySDI v4 server
     * @param targetUri    the address to send the import request data to
     * @param login        the user name to authenticate with the server
     * @param password     the password to authenticate with the server
     * @return the result of the import
     * @throws IOException                  the plugin could not communicate with the server
     * @throws SAXException                 the server response could not be parsed
     * @throws ParserConfigurationException the response parser could not be configured
     */
    private ConnectorImportResult sendImportRequest(final HttpHost targetServer, final URI targetUri,
            final String login, final String password)
            throws IOException, SAXException, ParserConfigurationException {

        try (final CloseableHttpClient client = this.getHttpClient(targetServer, login, password)) {
            final HttpGet httpGet = this.createGetRequest(targetUri);
            final HttpClientContext clientContext = this.getBasicAuthenticationContext(targetServer);

            this.logger.debug("Executing order HTTP request.");

            try (final CloseableHttpResponse response = client.execute(httpGet, clientContext)) {
                return this.parseImportResponse(response);
            }
        }
    }



    /**
     * Obtains an HTTP context object that allows basic authentication with the easySDI v4 server.
     *
     * @param targetServer the HTTP host that represents the easySDI v4 server
     * @return the HTTP client context
     */
    private HttpClientContext getBasicAuthenticationContext(final HttpHost targetServer) {
        final AuthCache authenticationCache = new BasicAuthCache();
        final BasicScheme basicAuthentication = new BasicScheme();
        authenticationCache.put(targetServer, basicAuthentication);
        final HttpClientContext clientContext = HttpClientContext.create();
        clientContext.setAuthCache(authenticationCache);

        return clientContext;
    }



    /**
     * Processes what the server returned as a response to an import request.
     *
     * @param response the response sent by the easySDI v4 server
     * @return the parsed import result
     * @throws IOException                  the response could not be read
     * @throws SAXException                 the response could not be parsed
     * @throws ParserConfigurationException the response parser could not be instantiated
     */
    private ConnectorImportResult parseImportResponse(final HttpResponse response)
            throws IOException, SAXException, ParserConfigurationException {
        //verify the valid error code first
        final ConnectorImportResult result = new ConnectorImportResult();
        final int httpCode = response.getStatusLine().getStatusCode();
        final String httpMessage = this.getMessageFromHttpCode(httpCode);
        this.logger.debug("Order HTTP request completed with status code {}.", httpCode);

        if (httpCode != SampleConnector.CREATED_HTTP_STATUS_CODE && httpCode != SampleConnector.SUCCESS_HTTP_STATUS_CODE) {
            this.logger.debug("getOrder has failed with HTTP code {} => return directly output", httpCode);
            result.setStatus(false);
            result.setErrorMessage(httpMessage);

            return result;
        }

        this.logger.debug("HTTP request was successful. Response was {}.", response);
        final String responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

        if (!"".equals(responseString)) {
            result.setStatus(true);

        } else {
            result.setStatus(false);
            result.setErrorMessage(this.messages.getString("importorders.result.xmlempty"));
        }

        this.logger.debug("Response content is:\n{}.", responseString);
        this.addImportedProductsToResult(responseString, result);

        return result;
    }



    /**
     * Adds a data item request to the collection of imported products.
     *
     * @param responseString the string that contains the import response from the server
     * @param result         the object that holds the processed result of the import request.
     * @return the import result object with the added product
     * @throws SAXException                 the response could not be parsed
     * @throws IOException                  the response could not be read
     * @throws ParserConfigurationException the response parser could not be instantiated
     */
    private ConnectorImportResult addImportedProductsToResult(final String responseString,
            final ConnectorImportResult result) throws SAXException, IOException, ParserConfigurationException {

        final String detailsUrlPattern = this.inputs.get(this.config.getProperty("code.detailsUrlPattern"));
        this.logger.debug("Building document");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(responseString)));

        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();

        this.logger.debug("Parsing orders");
        final NodeList orderList = getXMLNodeListFromXPath(document, config.getProperty("getOrders.xpath.orderlist"));

        //Loop order list
        for (int orderIndex = 0; orderIndex < orderList.getLength(); orderIndex++) {
            this.logger.debug("Processing order index {}.", orderIndex);
            final Node orderNode = orderList.item(orderIndex);
            final String guid = orderNode.getAttributes().getNamedItem("guid").getTextContent();
            final String orderId = orderNode.getAttributes().getNamedItem("id").getTextContent();
            this.logger.debug("Order GUID is {}.", guid);
            this.logger.debug("Parsing order properties.");
            final NodeList productList = this.getXMLNodeListFromXPath(document,
                    config.getProperty("getOrders.xpath.productlist").replace("<guid>", guid));
            final String orderGuid = orderNode.getAttributes().getNamedItem("guid").getTextContent();
            final String orderLabel = orderId;
            final String organism = this.getXMLNodeLabelFromXpath(document,
                    config.getProperty("getOrders.xpath.organism").replace("<guid>", guid));
            final String organismGuid = this.getXMLNodeLabelFromXpath(document,
                    config.getProperty("getOrders.xpath.organismGuid").replace("<guid>", guid));
            final String client = this.getXMLNodeLabelFromXpath(document,
                    config.getProperty("getOrders.xpath.client").replace("<guid>", guid));
            final String clientGuid = this.getXMLNodeLabelFromXpath(document,
                    config.getProperty("getOrders.xpath.clientGuid").replace("<guid>", guid));
            final String clientDetails = this.buildAddressDetailsFromXpath(document,
                    config.getProperty("getOrders.xpath.clientDetails").replace("<guid>", guid));
            this.logger.debug("Client details are : {}", clientDetails);
            final String tiers = this.getXMLNodeLabelFromXpath(document,
                    config.getProperty("getOrders.xpath.tiers").replace("<guid>", guid));
            final String tiersGuid = this.getXMLNodeLabelFromXpath(document,
                    config.getProperty("getOrders.xpath.tiersGuid").replace("<guid>", guid));
            final String tiersDetails = this.buildAddressDetailsFromXpath(document,
                    config.getProperty("getOrders.xpath.tiersdetails").replace("<guid>", guid));
            final String perimeter = this.getOrderPerimeterFromDocument(document, guid);
            double surface = 0d;

            try {
                surface = Double.parseDouble(this.getXMLNodeLabelFromXpath(document,
                        config.getProperty("getOrders.xpath.surface").replace("<guid>", guid)));

            } catch (Exception e) {
                this.logger.error("Surface could not be parsed to Double.");
            }

            this.logger.debug("Parsing products.");
            String detailsUrl;

            for (int productIndex = 0; productIndex < productList.getLength(); productIndex++) {
                this.logger.debug("Processing product index {}.", productIndex);
                final Element productNode = (Element) productList.item(productIndex);
                final Product product = new Product();

                final String productGuid = productNode.getAttribute(config.getProperty("getOrders.attribute.guid"));
                this.logger.debug("Product GUID is {}.", productGuid);
                final NodeList productLabelNodeList = productNode.getElementsByTagName("sdi:name");
                final NodeList propertiesNodeList = productNode.getElementsByTagName("sdi:property");

                this.logger.debug("Building product object.");
                product.setOrderGuid(orderGuid);
                product.setOrderLabel(orderLabel);
                product.setOrganism(organism);
                product.setOrganismGuid(organismGuid);
                product.setClient(client);
                product.setClientGuid(clientGuid);
                product.setClientDetails(clientDetails);
                product.setTiers(tiers);
                product.setTiersGuid(tiersGuid);
                product.setTiersDetails(tiersDetails);
                product.setPerimeter(perimeter);
                product.setSurface(surface);
                product.setProductGuid(productGuid);

                if (productLabelNodeList.getLength() > 0) {
                    product.setProductLabel(productLabelNodeList.item(0).getTextContent());
                }

                this.logger.debug("Parsing product properties.");
                product.setOthersParameters(this.parseOtherParameters(propertiesNodeList));
                this.logger.debug("Product parameters JSON is {}.", product.getOthersParameters());

                this.logger.debug("Creating order details URL.");
                detailsUrl = null;

                if (detailsUrlPattern != null && detailsUrlPattern.length() > 0) {
                    this.logger.debug("Order details URL pattern is {}", detailsUrlPattern);
                    detailsUrl = RequestUtils.interpolateVariables(detailsUrlPattern, product, this.config);

                } else {
                    this.logger.debug("No order details URL pattern defined.");
                }

                product.setExternalUrl(detailsUrl);
                this.logger.debug("Details URL set to {}", detailsUrl);

                this.logger.debug("Adding product {} to result.", productGuid);
                result.addProduct(product);
            }

        }

        return result;
    }



    private boolean checkOrderPerimeterType(Document document, String orderGuid) {
        String perimeterType = this.getXMLNodeLabelFromXpath(document,
                config.getProperty("getOrders.xpath.perimeterType").replace("<guid>", orderGuid));
        this.logger.debug("The order perimeter type is \"{}\".", perimeterType);

        return perimeterType != null
                && perimeterType.equalsIgnoreCase(config.getProperty("getOrders.perimeterType.valid"));
    }



    private String getOrderPerimeterFromDocument(Document document, String orderGuid) {

        if (!this.checkOrderPerimeterType(document, orderGuid)) {
            this.logger.warn("The order perimeter type is not supported.");
            return null;
        }

        return this.getXMLNodeLabelFromXpath(document, config.getProperty("getOrders.xpath.perimeter").replace("<guid>",
                orderGuid));
    }



    /**
     * Processes the custom settings for a data item request.
     *
     * @param propertiesNodeList the list of XML elements that contain the custom settings
     * @return the custom settings as a JSON string
     */
    private String parseOtherParameters(final NodeList propertiesNodeList) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();

        for (int propertyIndex = 0; propertyIndex < propertiesNodeList.getLength(); propertyIndex++) {
            this.logger.debug("Parsing product property index {}.", propertyIndex);
            final Element propertyNode = (Element) propertiesNodeList.item(propertyIndex);
            final NodeList propertyValues = propertyNode.getElementsByTagName("sdi:value");

            final String alias = propertyNode.getAttribute("alias");
            this.logger.debug("Property alias is {}.", alias);
            final int valuesNumber = propertyValues.getLength();
            this.logger.debug("The property has {} values.", valuesNumber);

            if (valuesNumber == 0) {
                continue;
            }

            if (valuesNumber == 1) {
                rootNode.put(alias, propertyValues.item(0).getTextContent());
                continue;
            }

            final ArrayNode valuesArray = rootNode.putArray(alias);

            for (int valueIndex = 0; valueIndex < valuesNumber; valueIndex++) {
                valuesArray.add(propertyValues.item(valueIndex).getTextContent());
            }
        }

        try {
            return mapper.writeValueAsString(rootNode);
        } catch (JsonProcessingException exception) {
            logger.error("An error occurred when the dynamic paremeters were converted to JSON.", exception);
            return null;
        }
    }



    /**
     * Converts the extended characters (such as é or ð) and the control characters to XML entities.
     *
     * @param originalString the string to escape
     * @return the escaped string
     */
    private String escapeExtendedCharactersForXml(final String originalString) {
        return this.escapeExtendedCharactersForXml(originalString, false);
    }



    /**
     * Converts the extended characters (such as é or ð) and optionally the control characters to XML
     * entities.
     *
     * @param originalString     the string to escape
     * @param ignoreControlChars <code>true</code> to leave the control characters (ASSCI code 32 or lower) as
     *                           they are
     * @return the escaped string
     */
    private String escapeExtendedCharactersForXml(final String originalString, final boolean ignoreControlChars) {
        final int length = originalString.length();

        StringBuilder escapedStringBuilder = new StringBuilder();

        for (int offset = 0; offset < length;) {
            final int codepoint = originalString.codePointAt(offset);

            if (codepoint < SampleConnector.LAST_STANDARD_ASCII_CHARACTER_CODE
                    && (codepoint > SampleConnector.LAST_CONTROL_CHARACTER_CODE || ignoreControlChars)) {
                escapedStringBuilder.append(Character.toChars(codepoint));

            } else {
                escapedStringBuilder.append(String.format("&#%d;", codepoint));
            }

            offset += Character.charCount(codepoint);
        }

        return escapedStringBuilder.toString();
    }



    /**
     * Provides a name for a file to export as the result of an order process. The non-existence of a file with
     * this name in the folder is done when this method is executed, but it is of course not guaranteed that it will
     * still be available by the time the file is created.
     *
     * @param request        the order whose result must be exported
     * @param avoidCollision <code>true</code> to generate a name that will not match an existing file
     * @return the name for the file to export
     */
    private String getArchiveNameForRequest(final IExportRequest request, final boolean avoidCollision) {
        return this.getFileNameForRequest(request, "zip", avoidCollision);
    }



    /**
     * Provides a name for a file to export as the result of an order process. The non-existence of a file with
     * this name in the folder is done when this method is executed, but it is of course not guaranteed that it will
     * still be available by the time the file is created.
     *
     * @param request        the order whose result must be exported
     * @param extension      the string describing the type of the file, such as <code>pdf</code> or <code>zip</code>
     * @param avoidCollision <code>true</code> to generate a name that will not match an existing file
     * @return the name for the file to export
     */
    private String getFileNameForRequest(final IExportRequest request, final String extension,
            final boolean avoidCollision) {
        assert request != null : "The exported request cannot be null.";
        assert request.getOrderLabel() != null : "The label of the exported order cannot be null.";
        assert request.getProductLabel() != null : "The label of the exported product cannot be null.";

        final String baseFileName = String.format("%s_%s", request.getOrderLabel(), request.getProductLabel())
                .replaceAll("[\\s<>*\"/\\\\\\[\\]:;|=,]", "_");
        this.logger.debug("The raw base file name is {}", baseFileName);
        this.logger.debug("The bytes of the raw base file name is {}.", baseFileName.getBytes(StandardCharsets.UTF_8));
        final String sanitizedBaseFileName = StringUtils.stripAccents(baseFileName);
        final String fileName = String.format("%s.%s", sanitizedBaseFileName, extension);

        if (!avoidCollision) {
            return fileName;
        }

        int index = 1;
        final String folderOutPath = request.getFolderOut();
        File outputFile = new File(folderOutPath, fileName);

        while (outputFile.exists()) {
            outputFile = new File(folderOutPath, String.format("%s_%d.%s", sanitizedBaseFileName, index++, extension));
        }

        return outputFile.getName();
    }



    /**
     * Provides a file to export as the result of an order process.
     *
     * @param request the order whose result must be exported
     * @return the output file to export
     * @throws IOException if a file system error prevented the creation of the output file
     */
    private File prepareOutputFileForRequest(final IExportRequest request) throws IOException {
        assert request != null : "The request cannot be null.";
        assert StringUtils.isNotBlank(request.getFolderOut()) : "The request output folder path cannot be empty.";

        this.logger.debug("Getting result file");
        final String outputFolderPath = request.getFolderOut();
        final File outputFolder = new File(outputFolderPath);

        if (!outputFolder.exists() || !outputFolder.isDirectory()) {
            this.logger.error("Invalid or inaccessible output folder {}.", outputFolder.getCanonicalPath());
            return null;
        }

        Collection<File> outputFilesList = FileUtils.listFiles(outputFolder, null, true);

        if (outputFilesList.isEmpty()) {
            return null;
        }

        if (outputFilesList.size() == 1) {
            final File outputFolderFile = (File) outputFilesList.toArray()[0];
            final String outputFolderFileName = outputFolderFile.getName();
            final String extension = FilenameUtils.getExtension(outputFolderFileName);

            if (outputFolderFileName.equals(this.getFileNameForRequest(request, extension, false))) {
                this.logger.debug("Output folder only contains one file \"{}\" and it matches the desired"
                        + " result file name, so it will be sent as is.", outputFolderFileName);
                return outputFolderFile;
            }

            final String resultFileName = this.getFileNameForRequest(request, extension, true);

            this.logger.debug("Output folder only contains one file \"{}\", so this will be sent as the"
                    + " output file with the name \"{}\"", outputFolderFileName, resultFileName);
            return Files.copy(outputFolderFile.toPath(), Paths.get(outputFolderPath, resultFileName)).toFile();
        }

        return ZipUtils.zipFolderContentToFile(outputFolder, this.getArchiveNameForRequest(request, true));
    }

}
