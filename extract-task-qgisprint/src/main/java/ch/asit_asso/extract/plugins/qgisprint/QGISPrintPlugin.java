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

import ch.asit_asso.extract.plugins.common.IEmailSettings;
import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * DESCRIBES HERE WHAT THE PLUGIN DO (E.G : A plugin that adds an automated remark to a request)
 * A sample plugin
 *
 * @author Florent Krin
 */
public class QGISPrintPlugin implements ITaskProcessor {

    /**
     * The relative path to the file that holds the general settings for this plugin.
     * this path is placed in resources direcctory
     * CHANGE THE PLUGIN NAME "sample" IN THIS PATH (i.e sample)
     */
    private static final String CONFIG_FILE_PATH = "plugins/qgisprint/properties/config.properties";

    /**
     * The name of the file that holds the text explaining how to use this plugin in the language of
     * the user interface.
     * This file is placed in resources/plugins/(plugin)/lang/fr/
     */
    private static final String HELP_FILE_NAME = "help.html";

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(QGISPrintPlugin.class);

    /**
     * CHANGE THE CODE THAT IDENTIFIES THIS PLUGIN
     * The string that identifies this plugin.
     */
    private final String code = "QGISPRINT";

    /**
     * The text that explains how to use this plugin in the language of the user interface.
     * DO NOT CHANGE THIS VALUE, IT IS CHANGED BY getHelp()
     */
    private String help = null;

    /**
     * The CSS class of the icon to display to represent this plugin.
     * SEARCH AN ICON CLASS in https://fontawesome.com/v4/icons
     */
    private final String pictoClass = "fa-file-pdf-o";

    private static final int CREATED_HTTP_STATUS_CODE = 201;

    private static final int SUCCESS_HTTP_STATUS_CODE = 200;

    /**
     * The strings that this plugin can send to the user in the language of the user interface.
     */
    private LocalizedMessages messages;

    /**
     * The settings for the execution of this task.
     */
    private Map<String, String> inputs;

    /**
     * The general settings for this plugin.
     */
    private PluginConfiguration config;

    private String login;

    private String password;

    /**
     * Creates a new instance of the automated remark plugin with default settings and using the default
     * language.
     * No changes needed in this method
     */
    public QGISPrintPlugin() {
        this.config = new PluginConfiguration(QGISPrintPlugin.CONFIG_FILE_PATH);
        this.messages = new LocalizedMessages();
    }



    /**
     * Creates a new instance of the automated remark plugin with default settings.
     * No changes needed in this method
     * @param language the string that identifies the language of the user interface
     */
    public QGISPrintPlugin(final String language) {
        this.config = new PluginConfiguration(QGISPrintPlugin.CONFIG_FILE_PATH);
        this.messages = new LocalizedMessages(language);
    }



    /**
     * Creates a new instance of the automated remark plugin using the default language.
     * No changes needed in this method
     * @param taskSettings a map that contains the settings for the execution of this task
     */
    public QGISPrintPlugin(final Map<String, String> taskSettings) {
        this();
        this.inputs = taskSettings;
    }

    /**
     * Creates a new instance of the automated remark plugin.
     * No changes needed in this method
     * @param language     the string that identifies the language of the user interface
     * @param taskSettings a map that contains the settings for the execution of this task
     */
    public QGISPrintPlugin(final String language, final Map<String, String> taskSettings) {
        this(language);
        this.inputs = taskSettings;
    }


    /**
     * Returns a new task processor instance with the provided settings.
     *
     * @param language the locale code of the language to display the messages in
     * @return the new task processor instance
     */
    @Override
    public final QGISPrintPlugin newInstance(final String language) {
        return new QGISPrintPlugin(language);
    }


    /**
     * Returns a new task processor instance with the provided settings.
     *
     * @param language the locale code of the language to display the messages in
     * @param inputs   the parameters for this task
     * @return the new task processor instance
     */
    @Override
    public final QGISPrintPlugin newInstance(final String language, final Map<String, String> taskSettings) {
        return new QGISPrintPlugin(language, taskSettings);
    }


    /**
     * Gets the user-friendly name of this task.
     *
     * @return the label
     */
    @Override
    public final String getLabel() {
        return this.messages.getString("plugin.label");
    }


    /**
     * Gets the string that uniquely identifies this task plugin.
     *
     * @return the plugin code
     */
    @Override
    public final String getCode() {
        return this.code;
    }


    /**
     * Gets a description of what this task does.
     *
     * @return the description text
     */
    @Override
    public final String getDescription() {
        return this.messages.getString("plugin.description");
    }


    /**
     * Gets a text explaining how to use this task.
     *
     * @return the help text for this task.
     */
    @Override
    public final String getHelp() {

        if (this.help == null) {
            this.help = this.messages.getFileContent(QGISPrintPlugin.HELP_FILE_NAME);
        }

        return this.help;
    }


    /**
     * Gets the path of the icon for this task plugin.
     *
     * @return the path of the image file
     */
    @Override
    public final String getPictoClass() {
        return this.pictoClass;
    }


    /**
     * This methods returns plugin parameters as an array in JSON format
     * Important :
     * "type" can be multitext / text / boolean / pass / numeric
     * "code" is the parameter name défined in the config file
     * "label" is the parameter label defined in messages.properties
     * AN SAMPLE IS WRITTEN IN THIS METHOD
     * @return a JSON string containing the definition of the parameters
     */
    @Override
    public final String getParams() {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode parametersNode = mapper.createArrayNode();

        ObjectNode urlNode = parametersNode.addObject();
        urlNode.put("code", this.config.getProperty("paramUrl"));
        urlNode.put("label", this.messages.getString("paramUrl.label"));
        urlNode.put("type", "text");
        urlNode.put("req", true);
        urlNode.put("maxlength", 255);

        ObjectNode templateLayoutNode = parametersNode.addObject();
        templateLayoutNode.put("code", this.config.getProperty("paramTemplateLayout"));
        templateLayoutNode.put("label", this.messages.getString("paramTemplateLayout.label"));
        templateLayoutNode.put("type", "text");
        templateLayoutNode.put("req", true);
        templateLayoutNode.put("maxlength", 50);

        ObjectNode pathProjectNode = parametersNode.addObject();
        pathProjectNode.put("code", this.config.getProperty("paramPathProjectQGIS"));
        pathProjectNode.put("label", this.messages.getString("paramPathProjectQGIS.label"));
        pathProjectNode.put("type", "text");
        pathProjectNode.put("req", false);
        pathProjectNode.put("maxlength", 255);

        ObjectNode loginNode = parametersNode.addObject();
        loginNode.put("code", this.config.getProperty("paramLogin"));
        loginNode.put("label", this.messages.getString("paramLogin.label"));
        loginNode.put("type", "text");
        loginNode.put("req", false);
        loginNode.put("maxlength", 50);

        ObjectNode passwordNode = parametersNode.addObject();
        passwordNode.put("code", this.config.getProperty("paramPassword"));
        passwordNode.put("label", this.messages.getString("paramPassword.label"));
        passwordNode.put("type", "pass");
        passwordNode.put("req", false);
        passwordNode.put("maxlength", 50);

        ObjectNode layersNode = parametersNode.addObject();
        layersNode.put("code", this.config.getProperty("paramLayers"));
        layersNode.put("label", this.messages.getString("paramLayers.label"));
        layersNode.put("type", "text");
        layersNode.put("req", false);
        layersNode.put("maxlength", 50);

        ObjectNode crsNode = parametersNode.addObject();
        crsNode.put("code", this.config.getProperty("paramCRS"));
        crsNode.put("label", this.messages.getString("paramCRS.label"));
        crsNode.put("type", "text");
        crsNode.put("req", false);
        crsNode.put("maxlength", 50);

        try {
            return mapper.writeValueAsString(parametersNode);

        } catch (JsonProcessingException exception) {
            logger.error("An error occurred when the parameters were converted to JSON.", exception);
            return null;
        }
    }


    /**
     * Executes the task.
     * CHANGE THIS METHOD BODY, AN SAMPLE IS WRITTEN IN THIS METHOD
     * @param request       the request that requires the execution of this task
     * @param emailSettings the parameters required to send an e-mail notification
     * @return an object  containing status, code, message and updated request
     */
    @Override
    public final ITaskProcessorResult execute(final ITaskProcessorRequest request, final IEmailSettings emailSettings) {

        final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        final Date now = new Date();

        try {

            this.logger.debug("Start QGIS Server extraction");

            final String baseurl = this.inputs.get(this.config.getProperty("paramUrl"));
            final String templateLayout = this.inputs.get(this.config.getProperty("paramTemplateLayout"));
            final String pathProject = this.inputs.get(this.config.getProperty("paramPathProjectQGIS"));
            final String login = this.inputs.get(this.config.getProperty("paramLogin"));
            this.login = login;
            this.logger.debug("login is" + login + " / " + this.login);
            final String password = this.inputs.get(this.config.getProperty("paramPassword"));
            this.password = password;
            this.logger.debug("password is : " + password + " / " + this.password);
            final String layers = this.inputs.get(this.config.getProperty("paramLayers"));
            final String productId = request.getProductGuid();
            String crs = this.inputs.get(this.config.getProperty("paramCRS"));
            if(crs == null || crs.isEmpty())
                crs = this.config.getProperty("defaultCRS");
            String folderOut = request.getFolderOut();
            final String subFolderName = String.format("%s_%s", dateTimeFormat.format(now), productId);

            if (!StringUtils.isEmpty(folderOut)) {
                folderOut = Paths.get(folderOut, subFolderName).toString();
            }
            this.logger.debug("Request perimeter is {}.", request.getPerimeter());
            this.logger.debug("calling WMS GetProjectSettings from url {}.", baseurl);

            /*File qgisProject = new File(pathProject);
            if(!qgisProject.exists())
                throw new Exception(String.format(this.messages.getString("plugin.error.project.notexists"), pathProject));
            */

            //get coverage layer
            String coverageLayer = getCoverageLayer(baseurl, templateLayout, pathProject );
            if(coverageLayer == null || coverageLayer.isEmpty())
                throw new Exception(this.messages.getString("plugin.error.coveragelayer"));

            //get feature Ids
            ArrayList<String> listFeatureIds = this.getFeatureIds(baseurl, pathProject, coverageLayer, request.getPerimeter());
            if(listFeatureIds.isEmpty())
                throw new Exception(this.messages.getString("plugin.error.getFeature.noids"));

            final QGISPrintResult result = executePrint(folderOut, baseurl, pathProject, crs, templateLayout, layers, listFeatureIds);
            result.setRequestData(request);

            return result;

        } catch (Exception exception) {
            this.logger.error("The QGIS extraction service has failed", exception);

            final QGISPrintResult result = new QGISPrintResult();
            result.setMessage(String.format(this.messages.getString("plugin.executing.failed"), exception.getMessage()));
            result.setErrorCode("-1");
            result.setRequestData(request);
            result.setStatus(ITaskProcessorResult.Status.ERROR);

            return result;
        }
    }

    /**
     * Execute the query GetProjectSettings for the qgis service passed in parameter.
     * This query allows to retrieve the coverage layer.
     * The url params for this request are : SERVICE=WMS&VERSION=1.3.0&REQUEST=GetProjectSettings&MAP=XXX
     * where MAP is the qgis project path
     * @param baseUrl QGIS Server url
     * @param templateLayout template layout (e.g. myplan)
     * @param pathQGS path for the qgis project
     * @return
     * @throws Exception
     */
    private String getCoverageLayer(String baseUrl, String templateLayout, String pathQGS)
            throws Exception {

        final String paramsUrl = String.format(this.config.getProperty("GetProjectSettingsParamUrl"), pathQGS);
        final String requestUrl = baseUrl + "?" + paramsUrl;
        this.logger.debug("Execute request " + requestUrl);

        final URI targetUri = new URI(requestUrl);
        final HttpHost targetServer = this.getHostFromUri(targetUri);

        try (CloseableHttpClient httpclient = this.getHttpClient(targetServer, this.login, this.password)) {
            HttpGet httpGet = new HttpGet(targetUri);
            final HttpClientContext clientContext = this.getBasicAuthenticationContext(targetServer);

            this.logger.debug("Executing QGIS request GetProjectSettings.");

            try (CloseableHttpResponse response = httpclient.execute(httpGet, clientContext)) {
                String coverageLayer = this.parseCoverageLayerInResponse(response, templateLayout);
                this.logger.debug("Coverage layer has found " + coverageLayer);
                return coverageLayer;
            }
        }

    }

    /**
     * Parse the response for the query GetProjectSettings. The coverage layer is read in the xml returned.
     * @param response response for the query GetProjectSettings
     * @param templateLayout template layout (e.g. myplan)
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws Exception
     */
    private String parseCoverageLayerInResponse(final HttpResponse response, final String templateLayout)
            throws IOException, SAXException, ParserConfigurationException, Exception {

        final int httpCode = response.getStatusLine().getStatusCode();
        String httpMessage = this.getMessageFromHttpCode(httpCode);
        this.logger.debug("HTTP GetProjectSettings completed with status code {}.", httpCode);


        if (httpCode != QGISPrintPlugin.CREATED_HTTP_STATUS_CODE && httpCode != QGISPrintPlugin.SUCCESS_HTTP_STATUS_CODE) {
            this.logger.error("GetProjectSettings has failed with HTTP code {} => return directly output", httpCode);
            String responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            this.logger.error("Response error is : {}", responseString);
            if(!responseString.isEmpty()) {
                String exceptionError = responseString.replaceAll("<.*?>", "");
                //String exceptionError = Jsoup.parse(responseString).text();
                httpMessage = httpMessage + " - " + exceptionError;
            }
            throw new Exception(httpMessage);
            //return null;
        }


        this.logger.debug("HTTP GetProjectSettings was successful. Response was {}.", response);
        final String responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

        if(responseString.isEmpty())
            return null;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(responseString)));

        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();

        return this.getXMLNodeLabelFromXpath(document,
                String.format(this.config.getProperty("getProjectSettings.xpath.atlasCoverageLayer"), templateLayout));

    }


    /**
     * Generate an GML for a WKT passed by parameter, this GML is used by GetFeature request
     * @param coverageLayer the coverage layer retrieved in WSProjectSettings qyery
     * @param wkt the perimeter for the request, this perimeter is in WKT format
     * @return A GML string
     * @throws IOException
     * @throws ParseException
     * @throws org.locationtech.jts.io.ParseException
     */
    private String getGMLPerimeter(String coverageLayer, String wkt)
            throws IOException, ParseException, org.locationtech.jts.io.ParseException {

        WKTReader wktReader = new WKTReader();
        Geometry geom = wktReader.read(wkt);
        String gml = "";
        String xmlFilePath = switch (geom.getGeometryType()) {
            case Geometry.TYPENAME_POINT -> this.config.getProperty("getFeature.body.point");
            case Geometry.TYPENAME_POLYGON -> this.config.getProperty("getFeature.body.polygon");
            case Geometry.TYPENAME_LINESTRING -> this.config.getProperty("getFeature.body.polyline");
            default -> "";
        };

        try (InputStream fileStream = this.getClass().getClassLoader().getResourceAsStream(xmlFilePath)) {

            assert fileStream != null;
            gml = IOUtils.toString(fileStream, StandardCharsets.UTF_8);
        }

        gml = gml.replace(this.config.getProperty("template.coveragelayer.key"), coverageLayer);
        StringBuilder listCoords = new StringBuilder();
        for(Coordinate coord : geom.getCoordinates()) {
            listCoords.append(coord.getX()).append(" ").append(coord.getY()).append(" ");
        }
        gml = gml.replace(this.config.getProperty("template.coordinates.key"), listCoords.toString());

        //GMLWriter gmlWriter = new GMLWriter(true);
        //String gml = gmlWriter.write(geom);

        return gml;
    }

    /**
     * Creates the content of the HTTP request with the perimeter request on body
     *
     * @param exportXml  the XML document that describes the result of the request processing
     * @param resultFile the file that contains the generated data for the request
     * @return the HTTP entity to export
     */
    private HttpEntity createHttpEntity(final String xml, final File resultFile) {
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        entityBuilder.setCharset(StandardCharsets.UTF_8);
        entityBuilder.addTextBody("xml", xml, ContentType.TEXT_XML);

        if (resultFile != null) {
            entityBuilder.addBinaryBody("file", resultFile);
        }

        entityBuilder.setContentType(ContentType.MULTIPART_FORM_DATA);
        return entityBuilder.build();
    }

    /**
     * Execute the query GetFeature for the qgis service passed in parameter
     * @param baseUrl the qgis server url
     * @param pathQGS path for the qgis project
     * @param coverageLayer the coverage layer retrieved in WSProjectSettings qyery
     * @return
     * @throws Exception
     */
    private ArrayList<String>  getFeatureIds(String baseUrl, String pathQGS, String coverageLayer, String wktPerimeter)
            throws Exception {

        final String paramsUrl = String.format(this.config.getProperty("GetFeatureParamUrl"), coverageLayer, pathQGS);
        final String requestUrl = baseUrl + "?" + paramsUrl;
        this.logger.debug("Execute request " + requestUrl);

        final URI targetUri = new URI(requestUrl);
        final HttpHost targetServer = this.getHostFromUri(targetUri);
        String gmlPerimeter = "";

        if(wktPerimeter != null && !wktPerimeter.isEmpty()) {
            gmlPerimeter = getGMLPerimeter(coverageLayer, wktPerimeter);
            this.logger.debug("GML Perimeter is : " + gmlPerimeter);
        }
        try (CloseableHttpClient httpclient = this.getHttpClient(targetServer, this.login, this.password)) {
            HttpPost httpPost = new HttpPost(targetUri);
            StringEntity xmlEntity = new StringEntity(gmlPerimeter);
            httpPost.setEntity(xmlEntity );
            //httpPost.setEntity
            final HttpClientContext clientContext = this.getBasicAuthenticationContext(targetServer);

            this.logger.debug("Executing QGIS request GetFeature.");

            try (CloseableHttpResponse response = httpclient.execute(httpPost, clientContext)) {
                ArrayList<String> listIds = this.parseFeatureIdsInResponse(response, coverageLayer);
                this.logger.debug("Coverage layer has found " + StringUtils.join(listIds));
                return listIds;
            }
        }


    }

    /**
     * Parse the response for the query GetFeature. feature ids are read in the xml returned.
     * @param response response for the query GetFeature
     * @param coverageLayer the coverage layer
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws Exception
     */
    private ArrayList<String>  parseFeatureIdsInResponse(final HttpResponse response, final String coverageLayer)
            throws IOException, SAXException, ParserConfigurationException, Exception {

        final int httpCode = response.getStatusLine().getStatusCode();
        final String httpMessage = this.getMessageFromHttpCode(httpCode);
        this.logger.debug("HTTP GetFeature completed with status code {}.", httpCode);

        if (httpCode != QGISPrintPlugin.CREATED_HTTP_STATUS_CODE && httpCode != QGISPrintPlugin.SUCCESS_HTTP_STATUS_CODE) {
            this.logger.error("GetFeature has failed with HTTP code {} => return directly output", httpCode);
            throw new Exception(httpMessage);
        }

        this.logger.debug("HTTP GetFeature was successful. Response was {}.", response);
        final String responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

        if(responseString.isEmpty())
            throw new Exception(this.messages.getString("plugin.error.getFeature.responseempty"));

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(responseString)));

        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();

        final NodeList idsList = this.getXMLNodeListFromXPath(document,
                String.format(this.config.getProperty("getFeature.xpath.gmlId"), coverageLayer));
        ArrayList<String> ids = new ArrayList<String>();

        for (int i = 0; i < idsList.getLength(); i++) {
            ids.add(idsList.item(i).getTextContent());
        }

        return ids;

    }

    /**
     * Execute the GetPrint query. A pdf file is returned by the query
     * @param folderOut folder where is saved thedf output
     * @param baseUrl the qgis seve url
     * @param pathQGS path for the qgis project
     * @param crs spatial reference EPSG
     * @param template template layout (e.g myplan)
     * @param layers layers comma-separated (this is an input plugin property)
     * @param ids feature ids retrieved by the GetFeature query
     * @return
     * @throws Exception
     */
    private QGISPrintResult executePrint(String folderOut, String baseUrl, String pathQGS, String crs, String template,  String layers, ArrayList<String> ids)
            throws Exception {

        final QGISPrintResult result = new QGISPrintResult();
        final String paramsUrl = String.format(this.config.getProperty("getPrintParamUrl"), crs, template, pathQGS, layers, String.join(",", ids));
        final String requestUrl = baseUrl + "?" + paramsUrl;
        this.logger.debug("Execute request " + requestUrl);

        final File destinationFolder = new File(folderOut);
        if (!destinationFolder.exists()) {
            destinationFolder.mkdirs();
        }

        final URI targetUri = new URI(requestUrl);
        final HttpHost targetServer = this.getHostFromUri(targetUri);

        try (CloseableHttpClient httpclient = this.getHttpClient(targetServer, this.login, this.password)) {
            HttpGet httpGet = new HttpGet(targetUri);
            final HttpClientContext clientContext = this.getBasicAuthenticationContext(targetServer);
            this.logger.debug("Executing QGIS request GetPrint.");

            try (CloseableHttpResponse response = httpclient.execute(httpGet, clientContext)) {

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                XPathFactory xPathfactory = XPathFactory.newInstance();
                XPath xpath = xPathfactory.newXPath();

                int httpCode = response.getStatusLine().getStatusCode();
                String httpMessage = this.getMessageFromHttpCode(httpCode);
                String responseString = null;
                this.logger.debug("HTTP GetPrint completed with status code {}.", httpCode);

                if ((httpCode != QGISPrintPlugin.CREATED_HTTP_STATUS_CODE) && (httpCode != QGISPrintPlugin.SUCCESS_HTTP_STATUS_CODE)) {
                    this.logger.error("GetPrint has failed with HTTP code {}", httpCode);
                    if(httpCode == 400) {
                        responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                        if(!responseString.isEmpty()) {
                            Document document = builder.parse(new InputSource(new StringReader(responseString)));
                            String exception = this.getXMLNodeLabelFromXpath(document, this.config.getProperty("getprint.xpath.exception"));
                            httpMessage = String.format(this.messages.getString("plugin.error.getPrint.failed"), exception);
                        }
                    }
                    result.setErrorCode("-1");
                    result.setStatus(QGISPrintResult.Status.ERROR);
                    result.setMessage(httpMessage);
                    return result;
                }

                this.logger.debug("HTTP GetPrint was successful. Response was {}.", response);

                //responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                try (InputStream is = response.getEntity().getContent()) {
                    final File outputFile = new File(destinationFolder, destinationFolder.getName() + ".pdf");
                    FileOutputStream fos = new FileOutputStream(outputFile);
                    int inByte;
                    while ((inByte = is.read()) != -1)
                        fos.write(inByte);
                    is.close();
                    fos.close();

                }
                result.setErrorCode("");
                result.setMessage(this.messages.getString("plugin.executing.success"));
                result.setStatus(QGISPrintResult.Status.SUCCESS);
                return result;
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


        return credentials;
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
     * Builds a host object for the server mentioned in a URI.
     *
     * @param uri the URI
     * @return the HTTP host object for the server
     */
    private HttpHost getHostFromUri(final URI uri) {
        final String hostName = uri.getHost();
        int port = uri.getPort();
        final String scheme = uri.getScheme();

        return new HttpHost(hostName, port, scheme);

        /*if (port < 0) {

            switch (scheme.toLowerCase()) {

                case "http":
                    this.logger.debug("No port in URL for host {}. Using HTTP default 80.", hostName);
                    port = QGISPrintPlugin.DEFAULT_HTTP_PORT;
                    break;

                case "https":
                    this.logger.debug("No port in URL for host {}. Using HTTPS default 443.", hostName);
                    port = QGISPrintPlugin.DEFAULT_HTTPS_PORT;
                    break;

                default:
                    this.logger.error("The protocol {} is not supported", scheme);
                    return null;
            }
        }

        return new HttpHost(hostName, port, scheme);
        */
    }


    /**
     * Obtains a client object to make authenticated HTTP requests.
     *
     * @param targetHost     the server to send the requests to
     * @return the HTTP client
     */
    private CloseableHttpClient getHttpClient(final HttpHost targetHost, String targetLogin, String targetPassword) {
        assert targetHost != null : "The target host cannot be null";

        final CredentialsProvider credentials = this.getCredentialsProvider(targetHost, targetLogin, targetPassword);

        return HttpClients.custom().setDefaultCredentialsProvider(credentials).build();
    }


    /**
     * Obtains the message that informs the user of the result of the task based on the returned HTTP code.
     *
     * @param httpCode the HTTP code returned by the FME Server task
     * @return the message string in the language of the user interface
     */
    private String getMessageFromHttpCode(final int httpCode) {

        String genericErrorMessage = this.messages.getString("error.message.generic");
        String httpErrorMessage = this.messages.getString(String.format("httperror.message.%d", httpCode));

        if (httpErrorMessage == null) {
            return genericErrorMessage;
        }

        return String.format("%s - %s", genericErrorMessage, httpErrorMessage);
    }

}
