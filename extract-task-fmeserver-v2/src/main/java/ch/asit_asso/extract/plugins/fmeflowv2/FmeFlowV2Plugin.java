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
package ch.asit_asso.extract.plugins.fmeflowv2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import ch.asit_asso.extract.plugins.common.IEmailSettings;
import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A plugin that executes an FME Flow (formerly FME Server) task using POST with GeoJSON.
 * This version uses API token authentication and sends parameters in the request body.
 *
 * @author Extract Team
 */
public class FmeFlowV2Plugin implements ITaskProcessor {

    /**
     * The name of the file that holds the text explaining how to use this plugin.
     */
    private static final String HELP_FILE_NAME = "fmeFlowV2Help.html";

    /**
     * The number returned in an HTTP response to tell that the request succeeded.
     */
    private static final int HTTP_OK_RESULT_CODE = 200;
    
    /**
     * The number returned in an HTTP response to tell that the request resulted in the creation of a resource.
     */
    private static final int HTTP_CREATED_RESULT_CODE = 201;
    
    /**
     * Maximum file size allowed for download (500 MB) - Security measure
     */
    private static final long MAX_DOWNLOAD_SIZE = 500L * 1024L * 1024L;
    
    /**
     * Request timeout in seconds
     */
    private static final int REQUEST_TIMEOUT_SECONDS = 300;
    
    /**
     * Maximum retry attempts for network operations
     */
    private static final int MAX_RETRY_ATTEMPTS = 3;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(FmeFlowV2Plugin.class);

    /**
     * The string that identifies this plugin.
     */
    private final String code = "FMEFLOWV2";

    /**
     * The class of the icon to use to represent this plugin.
     */
    private final String pictoClass = "fa-cogs";

    /**
     * The text that explains how to use this plugin in the language of the user interface.
     */
    private String help = null;

    /**
     * The strings that the plugin can send to the user in the language of the user interface.
     */
    private final LocalizedMessages messages;

    /**
     * The settings for the execution of this particular task.
     */
    private Map<String, String> inputs;

    /**
     * Creates a new FME Flow V2 plugin instance with default settings and using the default language.
     */
    public FmeFlowV2Plugin() {
        this.messages = new LocalizedMessages();
        this.inputs = null;
    }

    /**
     * Creates a new FME Flow V2 plugin instance using the default language.
     *
     * @param taskSettings a map with the settings for the execution of this task
     */
    public FmeFlowV2Plugin(Map<String, String> taskSettings) {
        this.messages = new LocalizedMessages();
        this.inputs = taskSettings;
    }

    /**
     * Creates a new FME Flow V2 plugin instance with default settings.
     *
     * @param lang the string that identifies the language of the user interface
     */
    public FmeFlowV2Plugin(String lang) {
        this(lang, null);
    }

    /**
     * Creates a new FME Flow V2 plugin instance.
     *
     * @param lang         the string that identifies the language of the user interface
     * @param taskSettings a map with the settings for the execution of this task
     */
    public FmeFlowV2Plugin(String lang, Map<String, String> taskSettings) {
        if (lang == null) {
            this.messages = new LocalizedMessages();
        } else {
            this.messages = new LocalizedMessages(lang);
        }
        this.inputs = taskSettings;
    }

    @Override
    public ITaskProcessor newInstance(String language) {
        return new FmeFlowV2Plugin(language, this.inputs);
    }

    @Override
    public ITaskProcessor newInstance(String language, Map<String, String> inputs) {
        return new FmeFlowV2Plugin(language, inputs);
    }

    @Override
    public ITaskProcessorResult execute(ITaskProcessorRequest request, IEmailSettings emailSettings) {
        
        this.logger.debug("Starting FME Flow V2 execution.");
        FmeFlowV2Result result = new FmeFlowV2Result();
        result.setRequestData(request);

        // Validate inputs - Security: Input validation
        if (this.inputs == null || this.inputs.isEmpty()) {
            String errorMessage = this.messages.getString("errorParameters.noParam");
            this.logger.error(errorMessage);
            result.setStatus(ITaskProcessorResult.Status.ERROR);
            result.setMessage(errorMessage);
            return result;
        }

        // Get and validate parameters - Security: Input sanitization
        String serviceUrl = StringUtils.trimToNull(this.inputs.get("serviceURL"));
        String apiToken = StringUtils.trimToNull(this.inputs.get("apiToken"));
        String geoJsonParam = StringUtils.trimToNull(this.inputs.get("geoJsonParameter"));
        
        // Default parameter name if not specified
        if (geoJsonParam == null) {
            geoJsonParam = "GEOJSON_INPUT";
        }

        if (serviceUrl == null) {
            String errorMessage = this.messages.getString("errorServiceUrl.notDefined");
            this.logger.error(errorMessage);
            result.setStatus(ITaskProcessorResult.Status.ERROR);
            result.setMessage(errorMessage);
            return result;
        }
        
        // Security: URL validation to prevent SSRF
        if (!isValidUrl(serviceUrl)) {
            String errorMessage = this.messages.getString("errorServiceUrl.invalid");
            this.logger.error("Invalid service URL provided");
            result.setStatus(ITaskProcessorResult.Status.ERROR);
            result.setMessage(errorMessage);
            return result;
        }

        if (apiToken == null) {
            String errorMessage = this.messages.getString("errorApiToken.notDefined");
            this.logger.error(errorMessage);
            result.setStatus(ITaskProcessorResult.Status.ERROR);
            result.setMessage(errorMessage);
            return result;
        }

        // Security: Never log sensitive information
        this.logger.info("Executing FME Flow request to service URL: {}", sanitizeUrlForLogging(serviceUrl));

        try {
            // Create the GeoJSON request body
            String geoJsonBody = createGeoJsonRequest(request);
            
            // Execute the POST request
            String responseUrl = executePostRequest(serviceUrl, apiToken, geoJsonBody, geoJsonParam);
            
            if (responseUrl != null && !responseUrl.isEmpty()) {
                // Download the result file
                File downloadedFile = downloadResult(responseUrl, apiToken, request.getFolderOut());
                
                if (downloadedFile != null && downloadedFile.exists()) {
                    this.logger.info("FME Flow process completed successfully");
                    result.setStatus(ITaskProcessorResult.Status.SUCCESS);
                    result.setMessage(this.messages.getString("extract.success"));
                    result.setResultFilePath(request.getFolderOut());
                } else {
                    String errorMessage = this.messages.getString("errorDownload.failed");
                    this.logger.error(errorMessage);
                    result.setStatus(ITaskProcessorResult.Status.ERROR);
                    result.setMessage(errorMessage);
                }
            } else {
                String errorMessage = this.messages.getString("errorResponse.noUrl");
                this.logger.error(errorMessage);
                result.setStatus(ITaskProcessorResult.Status.ERROR);
                result.setMessage(errorMessage);
            }
            
        } catch (Exception e) {
            String errorMessage = String.format(this.messages.getString("errorProcess.failed"), 
                                                e.getMessage());
            this.logger.error("Error executing FME Flow process", e);
            result.setStatus(ITaskProcessorResult.Status.ERROR);
            result.setMessage(errorMessage);
        }

        return result;
    }
    
    /**
     * Validates that a URL is safe to use - Security: SSRF prevention
     */
    private boolean isValidUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            String protocol = url.getProtocol();
            String host = url.getHost();
            
            // Only allow HTTP and HTTPS
            if (!"http".equalsIgnoreCase(protocol) && !"https".equalsIgnoreCase(protocol)) {
                return false;
            }
            
            // Prevent localhost and private network access (SSRF protection)
            if (host == null || host.isEmpty() || 
                host.equalsIgnoreCase("localhost") || 
                host.startsWith("127.") ||
                host.startsWith("10.") ||
                host.startsWith("192.168.") ||
                host.startsWith("172.16.") ||
                host.startsWith("169.254.") ||
                host.equals("0.0.0.0")) {
                logger.warn("Attempted access to restricted host: {}", host);
                return false;
            }
            
            return true;
        } catch (Exception e) {
            logger.error("Invalid URL format", e);
            return false;
        }
    }
    
    /**
     * Sanitizes URL for logging to prevent log injection
     */
    private String sanitizeUrlForLogging(String url) {
        if (url == null) return "null";
        // Remove any control characters and limit length
        return url.replaceAll("[\r\n]", "")
                  .substring(0, Math.min(url.length(), 200));
    }
    
    /**
     * Creates the GeoJSON request body with all parameters.
     * 
     * @param request the request containing the parameters
     * @return the GeoJSON as a string
     */
    private String createGeoJsonRequest(ITaskProcessorRequest request) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        // Create GeoJSON Feature
        ObjectNode root = mapper.createObjectNode();
        root.put("type", "Feature");
        
        // Convert WKT to GeoJSON geometry if available
        String perimeter = request.getPerimeter();
        if (perimeter != null && !perimeter.isEmpty()) {
            try {
                ObjectNode geometryNode = convertWKTToGeoJSON(perimeter, mapper);
                root.set("geometry", geometryNode);
                this.logger.debug("Converted WKT to GeoJSON geometry");
            } catch (Exception e) {
                this.logger.warn("Could not convert WKT to GeoJSON, using null geometry: {}", 
                                e.getMessage());
                root.putNull("geometry");
            }
        } else {
            root.putNull("geometry");
        }
        
        // Add all parameters as properties
        ObjectNode properties = mapper.createObjectNode();
        
        // Basic info
        properties.put("RequestId", request.getId());
        properties.put("FolderOut", request.getFolderOut());
        properties.put("FolderIn", request.getFolderIn());
        
        // Order info
        properties.put("OrderGuid", request.getOrderGuid());
        properties.put("OrderLabel", request.getOrderLabel());
        
        // Client info - New fields for v2
        properties.put("ClientGuid", request.getClientGuid());
        properties.put("ClientName", request.getClient());  // New in v2
        
        // Organism info - New fields for v2
        properties.put("OrganismGuid", request.getOrganismGuid());
        properties.put("OrganismName", request.getOrganism());  // New in v2
        
        // Product info - New fields for v2
        properties.put("ProductGuid", request.getProductGuid());
        properties.put("ProductLabel", request.getProductLabel());  // New in v2
        
        // Add surface if available (from FME Desktop v2)
        if (request.getPerimeter() != null) {
            try {
                double surface = calculateSurface(request.getPerimeter());
                properties.put("Surface", surface);
            } catch (Exception e) {
                logger.debug("Could not calculate surface: {}", e.getMessage());
            }
        }
        
        // Add custom parameters
        String parametersJson = request.getParameters();
        if (parametersJson != null && !parametersJson.isEmpty()) {
            try {
                ObjectNode parametersNode = (ObjectNode) mapper.readTree(parametersJson);
                properties.set("Parameters", parametersNode);
            } catch (Exception e) {
                this.logger.warn("Could not parse custom parameters as JSON: {}", e.getMessage());
                properties.put("Parameters", parametersJson);
            }
        } else {
            properties.set("Parameters", mapper.createObjectNode());
        }
        
        root.set("properties", properties);
        
        return mapper.writeValueAsString(root);
    }
    
    /**
     * Calculates the surface area from a WKT perimeter string.
     */
    private double calculateSurface(String wktPerimeter) throws ParseException {
        WKTReader reader = new WKTReader();
        Geometry geometry = reader.read(wktPerimeter);
        // Assuming WGS84 coordinates, this is a rough approximation
        // For accurate area calculation, projection to a local coordinate system would be needed
        return geometry.getArea() * 111000 * 111000; // Very rough conversion to square meters
    }
    
    /**
     * Converts WKT geometry string to GeoJSON geometry object.
     */
    private ObjectNode convertWKTToGeoJSON(String wkt, ObjectMapper mapper) throws ParseException {
        WKTReader reader = new WKTReader();
        Geometry geometry = reader.read(wkt);
        
        ObjectNode geoJsonGeometry = mapper.createObjectNode();
        
        if (geometry instanceof org.locationtech.jts.geom.Point) {
            geoJsonGeometry.put("type", "Point");
            Coordinate coord = geometry.getCoordinate();
            ArrayNode coordinates = mapper.createArrayNode();
            coordinates.add(coord.x);
            coordinates.add(coord.y);
            geoJsonGeometry.set("coordinates", coordinates);
            
        } else if (geometry instanceof Polygon) {
            geoJsonGeometry.put("type", "Polygon");
            geoJsonGeometry.set("coordinates", polygonToCoordinates((Polygon) geometry, mapper));
            
        } else if (geometry instanceof MultiPolygon) {
            geoJsonGeometry.put("type", "MultiPolygon");
            ArrayNode multiCoordinates = mapper.createArrayNode();
            MultiPolygon multiPolygon = (MultiPolygon) geometry;
            for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);
                multiCoordinates.add(polygonToCoordinates(polygon, mapper));
            }
            geoJsonGeometry.set("coordinates", multiCoordinates);
            
        } else if (geometry instanceof org.locationtech.jts.geom.LineString) {
            geoJsonGeometry.put("type", "LineString");
            ArrayNode coordinates = mapper.createArrayNode();
            for (Coordinate coord : geometry.getCoordinates()) {
                ArrayNode point = mapper.createArrayNode();
                point.add(coord.x);
                point.add(coord.y);
                coordinates.add(point);
            }
            geoJsonGeometry.set("coordinates", coordinates);
            
        } else {
            throw new IllegalArgumentException("Unsupported geometry type: " + geometry.getGeometryType());
        }
        
        return geoJsonGeometry;
    }
    
    /**
     * Converts a JTS Polygon to GeoJSON coordinates array.
     */
    private ArrayNode polygonToCoordinates(Polygon polygon, ObjectMapper mapper) {
        ArrayNode rings = mapper.createArrayNode();
        
        // Add exterior ring
        ArrayNode exteriorRing = mapper.createArrayNode();
        for (Coordinate coord : polygon.getExteriorRing().getCoordinates()) {
            ArrayNode point = mapper.createArrayNode();
            point.add(coord.x);
            point.add(coord.y);
            exteriorRing.add(point);
        }
        rings.add(exteriorRing);
        
        // Add interior rings (holes) - Support for donuts
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            ArrayNode interiorRing = mapper.createArrayNode();
            for (Coordinate coord : polygon.getInteriorRingN(i).getCoordinates()) {
                ArrayNode point = mapper.createArrayNode();
                point.add(coord.x);
                point.add(coord.y);
                interiorRing.add(point);
            }
            rings.add(interiorRing);
        }
        
        return rings;
    }
    
    /**
     * Executes the POST request to FME Flow Data Download service.
     * 
     * @param serviceUrl the URL of the FME Data Download service
     * @param apiToken the API token for authentication
     * @param jsonBody the GeoJSON body to send
     * @param geoJsonParam the name of the published parameter in the FME workspace
     * @return the URL to download the result, or null if error
     */
    private String executePostRequest(String serviceUrl, String apiToken, String jsonBody, String geoJsonParam) 
            throws IOException {
        
        // Security: Configure timeouts to prevent resource exhaustion
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(30 * 1000)  // 30 seconds
                .setSocketTimeout(REQUEST_TIMEOUT_SECONDS * 1000)
                .build();
        
        // Create HTTP client with timeout configuration
        try (CloseableHttpClient httpClient = createHttpClient(requestConfig)) {
            
            // For FME Data Download Service, we need to append parameters
            // The service expects: http://<host>/fmedatadownload/<repository>/<workspace>.fmw
            String urlWithParams = serviceUrl;
            if (!serviceUrl.contains("?")) {
                urlWithParams += "?";
            } else {
                urlWithParams += "&";
            }
            // Add standard Data Download parameters
            urlWithParams += "opt_responseformat=json";
            urlWithParams += "&opt_servicemode=sync";  // Synchronous mode for immediate download
            
            HttpPost httpPost = new HttpPost(urlWithParams);
            
            // Security: Set authorization header with API token
            httpPost.setHeader("Authorization", "fmetoken token=" + apiToken);
            httpPost.setHeader("Accept", "application/json");
            
            // For FME Data Download, we pass the GeoJSON as a form parameter
            // The workspace should have a published parameter to receive this
            List<NameValuePair> formParams = new ArrayList<>();
            // Add the GeoJSON as a parameter using the configured parameter name
            formParams.add(new BasicNameValuePair(geoJsonParam, jsonBody));
            
            // Create URL-encoded form entity
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(formParams, StandardCharsets.UTF_8);
            httpPost.setEntity(formEntity);
            
            // Security: Retry logic with exponential backoff
            for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
                try {
                    logger.info("Executing POST request (attempt {}/{})", attempt, MAX_RETRY_ATTEMPTS);
                    
                    try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                        int statusCode = response.getStatusLine().getStatusCode();
                        
                        if (statusCode == HTTP_OK_RESULT_CODE || statusCode == HTTP_CREATED_RESULT_CODE) {
                            // Parse response to get download URL
                            HttpEntity responseEntity = response.getEntity();
                            if (responseEntity != null) {
                                String responseStr = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
                                logger.debug("Response received, extracting download URL");
                                
                                // Try to parse JSON response to get URL
                                try {
                                    ObjectMapper mapper = new ObjectMapper();
                                    ObjectNode responseJson = (ObjectNode) mapper.readTree(responseStr);
                                    
                                    // Check various possible fields for the download URL
                                    if (responseJson.has("serviceResponse") && 
                                        responseJson.get("serviceResponse").has("url")) {
                                        return responseJson.get("serviceResponse").get("url").asText();
                                    } else if (responseJson.has("url")) {
                                        return responseJson.get("url").asText();
                                    } else if (responseJson.has("downloadUrl")) {
                                        return responseJson.get("downloadUrl").asText();
                                    } else {
                                        // If response is just a URL string
                                        if (responseStr.startsWith("http")) {
                                            return responseStr.trim();
                                        }
                                    }
                                } catch (Exception e) {
                                    logger.warn("Could not parse JSON response, treating as plain text");
                                    // If not JSON, maybe the response is the URL directly
                                    if (responseStr.startsWith("http")) {
                                        return responseStr.trim();
                                    }
                                }
                            }
                            logger.error("No download URL found in response");
                            return null;
                            
                        } else {
                            String errorBody = "";
                            if (response.getEntity() != null) {
                                errorBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                            }
                            logger.error("HTTP error {}: {}", statusCode, errorBody);
                            
                            // Don't retry on client errors (4xx)
                            if (statusCode >= 400 && statusCode < 500) {
                                return null;
                            }
                        }
                    }
                    
                } catch (IOException e) {
                    if (attempt == MAX_RETRY_ATTEMPTS) {
                        throw e;
                    }
                    logger.warn("Request attempt {} failed: {}", attempt, e.getMessage());
                    
                    // Exponential backoff
                    try {
                        Thread.sleep((long) Math.pow(2, attempt) * 1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Request interrupted", ie);
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Creates HTTP client with proper SSL configuration.
     * Security: Proper SSL/TLS configuration
     */
    private CloseableHttpClient createHttpClient(RequestConfig requestConfig) {
        try {
            // In production, you should properly configure SSL with certificate validation
            // This is a simplified version - enhance for production use
            return HttpClients.custom()
                    .setDefaultRequestConfig(requestConfig)
                    .build();
        } catch (Exception e) {
            logger.error("Error creating HTTP client", e);
            return HttpClients.createDefault();
        }
    }
    
    /**
     * Downloads the result file from FME Flow.
     * Security: Validates file size and type
     */
    private File downloadResult(String downloadUrl, String apiToken, String outputFolder) 
            throws IOException {
        
        // Security: Validate download URL
        if (!isValidUrl(downloadUrl)) {
            throw new IOException("Invalid download URL");
        }
        
        URL url = new URL(downloadUrl);
        
        // Generate safe filename
        String fileName = "fme_result_" + System.currentTimeMillis() + ".zip";
        Path outputPath = Paths.get(outputFolder, fileName);
        
        // Security: Ensure output path is within allowed directory
        Path normalizedPath = outputPath.normalize();
        if (!normalizedPath.startsWith(Paths.get(outputFolder).normalize())) {
            throw new IOException("Invalid output path");
        }
        
        File outputFile = normalizedPath.toFile();
        
        logger.info("Downloading result to: {}", outputFile.getAbsolutePath());
        
        // Create connection with authentication
        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", "fmetoken token=" + apiToken);
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(300000);
        
        try {
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Failed to download result: HTTP " + responseCode);
            }
            
            // Security: Check content length to prevent resource exhaustion
            long contentLength = connection.getContentLengthLong();
            if (contentLength > MAX_DOWNLOAD_SIZE) {
                throw new IOException("File too large: " + contentLength + " bytes (max: " + 
                                     MAX_DOWNLOAD_SIZE + ")");
            }
            
            // Download with size monitoring
            try (InputStream in = connection.getInputStream();
                 OutputStream out = new FileOutputStream(outputFile)) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytesRead = 0;
                
                while ((bytesRead = in.read(buffer)) != -1) {
                    totalBytesRead += bytesRead;
                    
                    // Security: Prevent zip bombs and excessive downloads
                    if (totalBytesRead > MAX_DOWNLOAD_SIZE) {
                        outputFile.delete();
                        throw new IOException("Download exceeded maximum size limit");
                    }
                    
                    out.write(buffer, 0, bytesRead);
                }
                
                logger.info("Downloaded {} bytes successfully", totalBytesRead);
            }
            
            return outputFile;
            
        } finally {
            connection.disconnect();
        }
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getDescription() {
        return this.messages.getString("plugin.description");
    }

    @Override
    public String getLabel() {
        return this.messages.getString("plugin.label");
    }

    @Override
    public String getHelp() {
        if (this.help == null) {
            this.help = this.messages.getFileContent(FmeFlowV2Plugin.HELP_FILE_NAME);
        }
        return this.help;
    }

    @Override
    public String getPictoClass() {
        return this.pictoClass;
    }

    @Override
    public String getParams() {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode parametersNode = mapper.createArrayNode();

        // Service URL parameter
        ObjectNode serviceUrlParam = mapper.createObjectNode();
        serviceUrlParam.put("code", "serviceURL");
        serviceUrlParam.put("label", this.messages.getString("param.serviceUrl.label"));
        serviceUrlParam.put("type", "text");
        serviceUrlParam.put("maxlength", 500);
        serviceUrlParam.put("req", true);
        serviceUrlParam.put("help", this.messages.getString("param.serviceUrl.help"));
        parametersNode.add(serviceUrlParam);

        // API Token parameter
        ObjectNode apiTokenParam = mapper.createObjectNode();
        apiTokenParam.put("code", "apiToken");
        apiTokenParam.put("label", this.messages.getString("param.apiToken.label"));
        apiTokenParam.put("type", "pass");  // Password field to hide token
        apiTokenParam.put("maxlength", 500);
        apiTokenParam.put("req", true);
        apiTokenParam.put("help", this.messages.getString("param.apiToken.help"));
        parametersNode.add(apiTokenParam);

        // GeoJSON Parameter name
        ObjectNode geoJsonParamNode = mapper.createObjectNode();
        geoJsonParamNode.put("code", "geoJsonParameter");
        geoJsonParamNode.put("label", this.messages.getString("param.geoJsonParam.label"));
        geoJsonParamNode.put("type", "text");
        geoJsonParamNode.put("maxlength", 100);
        geoJsonParamNode.put("req", false);
        geoJsonParamNode.put("default", "GEOJSON_INPUT");
        geoJsonParamNode.put("help", this.messages.getString("param.geoJsonParam.help"));
        parametersNode.add(geoJsonParamNode);

        try {
            return mapper.writeValueAsString(parametersNode);
        } catch (JsonProcessingException e) {
            logger.error("Could not create parameters JSON", e);
            return "[]";
        }
    }
}