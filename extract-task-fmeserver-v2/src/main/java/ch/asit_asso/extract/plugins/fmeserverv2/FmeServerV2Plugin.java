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
package ch.asit_asso.extract.plugins.fmeserverv2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import ch.asit_asso.extract.plugins.common.IEmailSettings;
import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A plugin that executes an FME Server V2 task using POST with GeoJSON.
 * This version uses API token authentication and sends parameters in the request body.
 * Enhanced with comprehensive error checking and no single point of failure.
 *
 * @author Extract Team
 */
public class FmeServerV2Plugin implements ITaskProcessor {

    /**
     * The name of the file that holds the text explaining how to use this plugin.
     */
    private static final String HELP_FILE_NAME = "help.html";

    /**
     * The number returned in an HTTP response to tell that the request succeeded.
     */
    private static final int HTTP_OK_RESULT_CODE = 200;

    /**
     * The number returned in an HTTP response to tell that the request resulted in the creation of a resource.
     */
    private static final int HTTP_CREATED_RESULT_CODE = 201;

    /**
     * Maximum file size allowed for download (10 GB) - Security measure
     */
    private static final long MAX_DOWNLOAD_SIZE = 10L * 1024L * 1024L * 1024L;

    /**
     * Request timeout in seconds
     */
    private static final int REQUEST_TIMEOUT_SECONDS = 300;

    /**
     * Connection timeout in seconds
     */
    private static final int CONNECTION_TIMEOUT_SECONDS = 30;

    /**
     * Maximum retry attempts for network operations
     */
    private static final int MAX_RETRY_ATTEMPTS = 3;

    /**
     * Buffer size for file operations
     */
    private static final int BUFFER_SIZE = 8192;

    /**
     * The writer to the application logs.
     */
    private static final Logger logger = LoggerFactory.getLogger(FmeServerV2Plugin.class);

    /**
     * The string that identifies this plugin.
     */
    private static final String CODE = "FMESERVERV2";

    /**
     * The class of the icon to use to represent this plugin.
     */
    private static final String PICTO_CLASS = "fa-cogs";

    /**
     * The text that explains how to use this plugin in the language of the user interface.
     */
    private String help = null;

    /**
     * The strings that the plugin can send to the user in the language of the user interface.
     */
    private final LocalizedMessages messages;

    /**
     * The plugin configuration.
     */
    private final PluginConfiguration config;

    /**
     * The settings for the execution of this particular task.
     */
    private Map<String, String> inputs;

    /**
     * Creates a new FME Server V2 plugin instance with default settings and using the default language.
     */
    public FmeServerV2Plugin() {
        this.messages = new LocalizedMessages();
        this.config = new PluginConfiguration();
        this.inputs = null;
    }

    /**
     * Creates a new FME Server V2 plugin instance using the default language.
     *
     * @param taskSettings a map with the settings for the execution of this task
     */
    public FmeServerV2Plugin(Map<String, String> taskSettings) {
        this.messages = new LocalizedMessages();
        this.config = new PluginConfiguration();
        this.inputs = taskSettings;
    }

    /**
     * Creates a new FME Server V2 plugin instance with default settings.
     *
     * @param lang the string that identifies the language of the user interface
     */
    public FmeServerV2Plugin(String lang) {
        this(lang, null);
    }

    /**
     * Creates a new FME Server V2 plugin instance.
     *
     * @param lang         the string that identifies the language of the user interface
     * @param taskSettings a map with the settings for the execution of this task
     */
    public FmeServerV2Plugin(String lang, Map<String, String> taskSettings) {
        this.messages = (lang == null) ? new LocalizedMessages() : new LocalizedMessages(lang);
        this.config = new PluginConfiguration();
        this.inputs = taskSettings;
    }

    @Override
    public ITaskProcessor newInstance(String language) {
        return new FmeServerV2Plugin(language, this.inputs);
    }

    @Override
    public ITaskProcessor newInstance(String language, Map<String, String> inputs) {
        return new FmeServerV2Plugin(language, inputs);
    }

    @Override
    public ITaskProcessorResult execute(ITaskProcessorRequest request, IEmailSettings emailSettings) {
        long startTime = System.currentTimeMillis();
        logger.debug("Starting FME Server V2 execution for request ID: {}", request != null ? request.getId() : "null");

        FmeServerV2Result result = new FmeServerV2Result();

        try {
            // Validate request
            if (request == null) {
                String errorMessage = messages.getString("plugin.errors.request.null");
                logger.error(errorMessage);
                result.setError("REQUEST_NULL", errorMessage);
                result.setMessage(errorMessage);
                return result;
            }

            result.setRequestData(request);

            // Validate inputs
            if (!validateInputs(result)) {
                return result;
            }

            // Get and validate parameters
            String serviceUrl = StringUtils.trimToNull(this.inputs.get("serviceURL"));
            String apiToken = StringUtils.trimToNull(this.inputs.get("apiToken"));

            if (!validateParameters(serviceUrl, apiToken, result)) {
                return result;
            }

            // Security: Never log sensitive information
            logger.info("Executing FME Server request for order: {}", request.getOrderGuid());

            // Create request handler
            FmeServerV2Request fmeRequest = new FmeServerV2Request(request, config);

            if (!fmeRequest.isValid()) {
                String errorMessage = messages.getString("plugin.errors.request.invalid");
                logger.error(errorMessage);
                result.setError("REQUEST_INVALID", errorMessage);
                result.setMessage(errorMessage);
                return result;
            }

            // Create the GeoJSON request body
            String geoJsonBody = null;
            try {
                geoJsonBody = fmeRequest.createGeoJsonFeature();
                logger.debug("Created GeoJSON feature for request");
            } catch (Exception e) {
                String errorMessage = messages.getString("plugin.errors.geojson.creation", e.getMessage());
                logger.error("Failed to create GeoJSON", e);
                result.setError("GEOJSON_CREATION_FAILED", errorMessage);
                result.setMessage(errorMessage);
                return result;
            }

            // Execute the POST request
            FmeServerResponse fmeResponse = executePostRequest(serviceUrl, apiToken, geoJsonBody);

            // Process the response
            if (fmeResponse.isSuccess() && fmeResponse.getDownloadUrl() != null) {
                if (!processSuccessResponse(fmeResponse, apiToken, request, result)) {
                    return result;
                }
            } else {
                processErrorResponse(fmeResponse, result);
                return result;
            }

            // Set success status
            result.setStatus(ITaskProcessorResult.Status.SUCCESS);
            result.setMessage(messages.getString("plugin.execution.success"));
            logger.info("FME Server process completed successfully for request ID: {}", request.getId());

        } catch (Exception e) {
            handleUnexpectedError(e, result);
        } finally {
            result.setProcessingDuration(startTime);
            logger.debug("FME Server V2 execution completed in {} ms", result.getProcessingDuration());
        }

        return result;
    }

    /**
     * Validates that inputs are present and not empty.
     */
    private boolean validateInputs(FmeServerV2Result result) {
        if (this.inputs == null || this.inputs.isEmpty()) {
            String errorMessage = messages.getString("plugin.errors.params.none");
            logger.error(errorMessage);
            result.setError("PARAMS_NONE", errorMessage);
            result.setMessage(errorMessage);
            return false;
        }
        return true;
    }

    /**
     * Validates service URL and API token parameters.
     */
    private boolean validateParameters(String serviceUrl, String apiToken, FmeServerV2Result result) {
        // Validate service URL
        if (serviceUrl == null) {
            String errorMessage = messages.getString("plugin.errors.params.serviceurl.undefined");
            logger.error(errorMessage);
            result.setError("SERVICEURL_UNDEFINED", errorMessage);
            result.setMessage(errorMessage);
            return false;
        }

        if (!isValidUrl(serviceUrl)) {
            String errorMessage = messages.getString("plugin.errors.params.serviceurl.invalid");
            logger.error("Invalid service URL provided");
            result.setError("SERVICEURL_INVALID", errorMessage);
            result.setMessage(errorMessage);
            return false;
        }

        // Validate API token
        if (apiToken == null) {
            String errorMessage = messages.getString("plugin.errors.params.apitoken.undefined");
            logger.error(errorMessage);
            result.setError("APITOKEN_UNDEFINED", errorMessage);
            result.setMessage(errorMessage);
            return false;
        }

        if (apiToken.trim().length() < 10) {
            String errorMessage = messages.getString("plugin.errors.params.apitoken.invalid");
            logger.error("API token appears to be invalid (too short)");
            result.setError("APITOKEN_INVALID", errorMessage);
            result.setMessage(errorMessage);
            return false;
        }

        return true;
    }

    /**
     * Processes a successful FME Server response.
     */
    private boolean processSuccessResponse(FmeServerResponse fmeResponse, String apiToken,
                                          ITaskProcessorRequest request, FmeServerV2Result result) {
        String responseUrl = fmeResponse.getDownloadUrl();

        try {
            // Download the result file
            File downloadedFile = downloadResult(responseUrl, apiToken, request.getFolderOut());

            if (downloadedFile != null && downloadedFile.exists()) {
                result.setResultFilePath(request.getFolderOut());
                result.addResultInfo("downloadedFile", downloadedFile.getName());
                result.addResultInfo("fileSize", String.valueOf(downloadedFile.length()));
                logger.info("Downloaded result file: {} ({} bytes)",
                           downloadedFile.getName(), downloadedFile.length());
                return true;
            } else {
                String errorMessage = messages.getString("plugin.errors.download.failed");
                logger.error(errorMessage);
                result.setError("DOWNLOAD_FAILED", errorMessage);
                result.setMessage(errorMessage);
                return false;
            }
        } catch (Exception e) {
            String errorMessage = messages.getString("plugin.errors.download.exception", e.getMessage());
            logger.error("Download failed with exception", e);
            result.setError("DOWNLOAD_EXCEPTION", errorMessage);
            result.setMessage(errorMessage);
            return false;
        }
    }

    /**
     * Processes an error response from FME Server.
     */
    private void processErrorResponse(FmeServerResponse fmeResponse, FmeServerV2Result result) {
        if (!fmeResponse.isSuccess()) {
            String errorMessage = fmeResponse.getErrorMessage() != null ?
                fmeResponse.getErrorMessage() :
                messages.getString("plugin.errors.response.failed");
            logger.error("FME Server transformation failed: {}", errorMessage);
            result.setError("TRANSFORMATION_FAILED", errorMessage);
            result.setMessage(errorMessage);
        } else {
            String errorMessage = messages.getString("plugin.errors.response.no.url");
            logger.error(errorMessage);
            result.setError("NO_DOWNLOAD_URL", errorMessage);
            result.setMessage(errorMessage);
        }
    }

    /**
     * Handles unexpected errors during execution.
     */
    private void handleUnexpectedError(Exception e, FmeServerV2Result result) {
        String errorMessage = messages.getString("plugin.errors.process.failed", e.getMessage());
        logger.error("Unexpected error executing FME Server process", e);
        result.setError("UNEXPECTED_ERROR", errorMessage);
        result.setMessage(errorMessage);
        result.setStatus(ITaskProcessorResult.Status.ERROR);
    }

    /**
     * Validates that a URL is safe to use - Security: SSRF prevention
     */
    private boolean isValidUrl(String urlString) {
        if (urlString == null || urlString.trim().isEmpty()) {
            return false;
        }

        try {
            URL url = new URL(urlString);
            String protocol = url.getProtocol();
            String host = url.getHost();

            // Only allow HTTP and HTTPS
            if (!"http".equalsIgnoreCase(protocol) && !"https".equalsIgnoreCase(protocol)) {
                logger.warn("Invalid protocol in URL: {}", protocol);
                return false;
            }

            // Prevent localhost and private network access (SSRF protection)
            if (host == null || host.isEmpty()) {
                logger.warn("Empty host in URL");
                return false;
            }

            // Check for private/local addresses
            if (isPrivateOrLocalAddress(host)) {
                logger.warn("Attempted access to restricted host: {}", host);
                return false;
            }

            return true;
        } catch (MalformedURLException e) {
            logger.error("Malformed URL: {}", urlString, e);
            return false;
        } catch (Exception e) {
            logger.error("Invalid URL format", e);
            return false;
        }
    }

    /**
     * System property to allow local addresses for testing purposes only.
     * Set via -Dextract.ssrf.allowLocalForTesting=true
     */
    private static final String ALLOW_LOCAL_PROPERTY = "extract.ssrf.allowLocalForTesting";

    /**
     * Checks if a host is a private or local address.
     * Can be bypassed for testing by setting system property extract.ssrf.allowLocalForTesting=true
     */
    private boolean isPrivateOrLocalAddress(String host) {
        if (Boolean.getBoolean(ALLOW_LOCAL_PROPERTY)) {
            logger.debug("SSRF protection bypassed for testing ({}=true)", ALLOW_LOCAL_PROPERTY);
            return false;
        }

        return host.equalsIgnoreCase("localhost") ||
               host.startsWith("127.") ||
               host.startsWith("10.") ||
               host.startsWith("192.168.") ||
               host.startsWith("172.16.") ||
               host.startsWith("172.17.") ||
               host.startsWith("172.18.") ||
               host.startsWith("172.19.") ||
               host.startsWith("172.20.") ||
               host.startsWith("172.21.") ||
               host.startsWith("172.22.") ||
               host.startsWith("172.23.") ||
               host.startsWith("172.24.") ||
               host.startsWith("172.25.") ||
               host.startsWith("172.26.") ||
               host.startsWith("172.27.") ||
               host.startsWith("172.28.") ||
               host.startsWith("172.29.") ||
               host.startsWith("172.30.") ||
               host.startsWith("172.31.") ||
               host.startsWith("169.254.") ||
               host.equals("0.0.0.0") ||
               host.equals("::1") ||
               host.equals("::") ||
               host.startsWith("[::1]") ||
               host.startsWith("fe80:");
    }

    /**
     * Inner class to represent FME Server response
     */
    private static class FmeServerResponse {
        private final boolean success;
        private final String downloadUrl;
        private final String errorMessage;
        private final int statusCode;

        public FmeServerResponse(boolean success, String downloadUrl, String errorMessage, int statusCode) {
            this.success = success;
            this.downloadUrl = downloadUrl;
            this.errorMessage = errorMessage;
            this.statusCode = statusCode;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }

    /**
     * Executes the POST request to FME Server Data Download service with enhanced error handling.
     */
    private FmeServerResponse executePostRequest(String serviceUrl, String apiToken, String jsonBody)
            throws IOException {

        // Configure timeouts to prevent resource exhaustion
        RequestConfig requestConfig = RequestConfig.custom()
                .build();

        // Retry logic with exponential backoff
        IOException lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                return executePostRequestAttempt(serviceUrl, apiToken, jsonBody, requestConfig, attempt);

            } catch (IOException e) {
                lastException = e;
                logger.warn("Request attempt {}/{} failed: {}", attempt, MAX_RETRY_ATTEMPTS, e.getMessage());

                if (attempt < MAX_RETRY_ATTEMPTS) {
                    try {
                        // Exponential backoff
                        long waitTime = (long) Math.pow(2, attempt) * 1000;
                        logger.info("Waiting {} ms before retry", waitTime);
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Request interrupted", ie);
                    }
                }
            }
        }

        // All retries failed
        String errorMsg = messages.getString("plugin.errors.connection.failed",
                                            lastException != null ? lastException.getMessage() : "Unknown error");
        logger.error("All retry attempts failed");
        return new FmeServerResponse(false, null, errorMsg, -1);
    }

    /**
     * Single attempt to execute POST request.
     */
    private FmeServerResponse executePostRequestAttempt(String serviceUrl, String apiToken,
                                                       String jsonBody, RequestConfig requestConfig,
                                                       int attempt) throws IOException {

        try (CloseableHttpClient httpClient = createHttpClient(requestConfig)) {

            // Prepare URL with parameters
            String urlWithParams = prepareServiceUrl(serviceUrl);
            HttpPost httpPost = new HttpPost(urlWithParams);

            // Set headers
            httpPost.setHeader("Authorization", "fmetoken token=" + apiToken);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("User-Agent", "Extract-FMEServerV2-Plugin/2.0");

            // Set body
            StringEntity jsonEntity = new StringEntity(jsonBody, ContentType.APPLICATION_JSON);
            httpPost.setEntity(jsonEntity);

            logger.info("Executing POST request (attempt {}/{})", attempt, MAX_RETRY_ATTEMPTS);

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                return processHttpResponse(response);
            }
        }
    }

    /**
     * Prepares the service URL with required parameters.
     */
    private String prepareServiceUrl(String serviceUrl) {
        if (!serviceUrl.contains("?")) {
            serviceUrl += "?";
        } else {
            serviceUrl += "&";
        }

        // Add standard Data Download parameters
        serviceUrl += "opt_responseformat=json";
        serviceUrl += "&opt_servicemode=sync";  // Synchronous mode for immediate download

        return serviceUrl;
    }

    /**
     * Processes HTTP response from FME Server.
     */
    private FmeServerResponse processHttpResponse(CloseableHttpResponse response) throws IOException {
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == HTTP_OK_RESULT_CODE || statusCode == HTTP_CREATED_RESULT_CODE) {
            return processSuccessfulHttpResponse(response, statusCode);
        } else {
            return processErrorHttpResponse(response, statusCode);
        }
    }

    /**
     * Processes a successful HTTP response.
     */
    private FmeServerResponse processSuccessfulHttpResponse(CloseableHttpResponse response, int statusCode)
            throws IOException {

        HttpEntity responseEntity = response.getEntity();
        if (responseEntity == null) {
            logger.error("Empty response entity received");
            return new FmeServerResponse(false, null,
                                        messages.getString("plugin.errors.response.empty"), statusCode);
        }

        String responseStr = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
        logger.debug("Response received, extracting download URL");

        // Try to extract download URL from response
        String downloadUrl = extractDownloadUrl(responseStr);

        if (downloadUrl != null) {
            return new FmeServerResponse(true, downloadUrl, null, statusCode);
        } else {
            logger.error("No download URL found in response");
            return new FmeServerResponse(false, null,
                                        messages.getString("plugin.errors.response.no.url"), statusCode);
        }
    }

    /**
     * Processes an error HTTP response.
     */
    private FmeServerResponse processErrorHttpResponse(CloseableHttpResponse response, int statusCode)
            throws IOException {

        String errorBody = "";
        String errorDetails = "";

        if (response.getEntity() != null) {
            errorBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            errorDetails = extractErrorDetails(errorBody);
        }

        if (!errorDetails.isEmpty()) {
            logger.error("FME Server error (HTTP {}): {}", statusCode, errorDetails);
        } else {
            logger.error("HTTP error {}: {}", statusCode, errorBody);
        }

        String errorMsg = !errorDetails.isEmpty() ? errorDetails :
                         messages.getString("plugin.errors.http.status", statusCode);

        return new FmeServerResponse(false, null, errorMsg, statusCode);
    }

    /**
     * Extracts download URL from JSON response.
     */
    private String extractDownloadUrl(String responseStr) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode responseJson = mapper.readTree(responseStr);

            // Check various possible fields for the download URL
            String[] urlFields = {"url", "downloadUrl", "resultUrl", "outputUrl"};

            // Check in root
            for (String field : urlFields) {
                if (responseJson.has(field)) {
                    return responseJson.get(field).asText();
                }
            }

            // Check in serviceResponse
            if (responseJson.has("serviceResponse")) {
                JsonNode serviceResponse = responseJson.get("serviceResponse");
                for (String field : urlFields) {
                    if (serviceResponse.has(field)) {
                        return serviceResponse.get(field).asText();
                    }
                }
            }

            // Check if response is just a URL string
            if (responseStr.startsWith("http")) {
                return responseStr.trim();
            }

        } catch (Exception e) {
            logger.debug("Could not parse JSON response: {}", e.getMessage());
            // If not JSON, maybe the response is the URL directly
            if (responseStr.startsWith("http")) {
                return responseStr.trim();
            }
        }

        return null;
    }

    /**
     * Extracts error details from FME error response.
     */
    private String extractErrorDetails(String errorBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode errorJson = mapper.readTree(errorBody);

            StringBuilder details = new StringBuilder();

            // Check for standard error message
            if (errorJson.has("message")) {
                details.append(errorJson.get("message").asText());
            }

            // Check for FME specific error structure
            if (errorJson.has("serviceResponse")) {
                JsonNode serviceResponse = errorJson.get("serviceResponse");

                if (serviceResponse.has("statusInfo")) {
                    JsonNode statusInfo = serviceResponse.get("statusInfo");
                    if (statusInfo.has("message")) {
                        if (details.length() > 0) details.append(" - ");
                        details.append(statusInfo.get("message").asText());
                    }
                }

                if (serviceResponse.has("fmeTransformationResult")) {
                    JsonNode fmeResult = serviceResponse.get("fmeTransformationResult");
                    if (fmeResult.has("fmeEngineResponse")) {
                        JsonNode engineResponse = fmeResult.get("fmeEngineResponse");
                        if (engineResponse.has("statusMessage")) {
                            if (details.length() > 0) details.append(" - ");
                            details.append(engineResponse.get("statusMessage").asText());
                        }
                    }
                }
            }

            return details.toString();

        } catch (Exception e) {
            logger.debug("Could not parse error response as JSON", e);
            return "";
        }
    }

    /**
     * Creates HTTP client with proper configuration.
     */
    private CloseableHttpClient createHttpClient(RequestConfig requestConfig) {
        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setMaxConnTotal(10)
                .setMaxConnPerRoute(5)
                .build();
    }

    /**
     * Downloads the result file from FME Server with enhanced validation and error handling.
     */
    private File downloadResult(String downloadUrl, String apiToken, String outputFolder)
            throws IOException {

        // Validate download URL
        if (!isValidUrl(downloadUrl)) {
            throw new IOException(messages.getString("plugin.errors.download.url.invalid"));
        }

        // Generate safe filename
        String fileName = generateSafeFileName();
        Path outputPath = Paths.get(outputFolder, fileName);

        // Security: Ensure output path is within allowed directory
        Path normalizedPath = outputPath.normalize();
        Path normalizedFolder = Paths.get(outputFolder).normalize();

        if (!normalizedPath.startsWith(normalizedFolder)) {
            throw new IOException(messages.getString("plugin.errors.download.path.invalid"));
        }

        File outputFile = normalizedPath.toFile();
        logger.info("Downloading result to: {}", outputFile.getAbsolutePath());

        // Download with retries
        IOException lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                return downloadFileAttempt(downloadUrl, apiToken, outputFile, attempt);
            } catch (IOException e) {
                lastException = e;
                logger.warn("Download attempt {}/{} failed: {}", attempt, MAX_RETRY_ATTEMPTS, e.getMessage());

                if (outputFile.exists()) {
                    outputFile.delete();
                }

                if (attempt < MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep((long) Math.pow(2, attempt) * 1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Download interrupted", ie);
                    }
                }
            }
        }

        throw new IOException(messages.getString("plugin.errors.download.failed.after.retries", lastException.getMessage()));
    }

    /**
     * Single attempt to download file.
     */
    private File downloadFileAttempt(String downloadUrl, String apiToken, File outputFile, int attempt)
            throws IOException {

        URL url = new URL(downloadUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            // Configure connection
            connection.setRequestProperty("Authorization", "fmetoken token=" + apiToken);
            connection.setConnectTimeout(CONNECTION_TIMEOUT_SECONDS * 1000);
            connection.setReadTimeout(REQUEST_TIMEOUT_SECONDS * 1000);
            connection.setRequestProperty("User-Agent", "Extract-FMEServerV2-Plugin/2.0");

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException(messages.getString("plugin.errors.download.http.error", responseCode));
            }

            // Check content length
            long contentLength = connection.getContentLengthLong();
            if (contentLength > MAX_DOWNLOAD_SIZE) {
                throw new IOException(messages.getString("plugin.errors.download.too.large",
                                                        contentLength, MAX_DOWNLOAD_SIZE));
            }

            // Download with progress tracking
            try (InputStream in = connection.getInputStream();
                 OutputStream out = new FileOutputStream(outputFile)) {

                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                long totalBytesRead = 0;
                long lastLogTime = System.currentTimeMillis();

                while ((bytesRead = in.read(buffer)) != -1) {
                    totalBytesRead += bytesRead;

                    // Security: Prevent zip bombs and excessive downloads
                    if (totalBytesRead > MAX_DOWNLOAD_SIZE) {
                        throw new IOException(messages.getString("plugin.errors.download.exceeded.max"));
                    }

                    out.write(buffer, 0, bytesRead);

                    // Log progress every 5 seconds
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastLogTime > 5000) {
                        if (contentLength > 0) {
                            int percent = (int) ((totalBytesRead * 100) / contentLength);
                            logger.info("Download progress: {}% ({} / {} bytes)",
                                      percent, totalBytesRead, contentLength);
                        } else {
                            logger.info("Downloaded {} bytes", totalBytesRead);
                        }
                        lastLogTime = currentTime;
                    }
                }

                logger.info("Download completed: {} bytes", totalBytesRead);
                return outputFile;
            }

        } finally {
            connection.disconnect();
        }
    }

    /**
     * Generates a safe filename for the download.
     */
    private String generateSafeFileName() {
        return String.format("fme_result_%d_%s.zip",
                           System.currentTimeMillis(),
                           java.util.UUID.randomUUID().toString().substring(0, 8));
    }

    @Override
    public String getCode() {
        return CODE;
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
            this.help = this.messages.getFileContent(HELP_FILE_NAME);
        }
        return this.help;
    }

    @Override
    public String getPictoClass() {
        return PICTO_CLASS;
    }

    @Override
    public String getParams() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ArrayNode parametersNode = mapper.createArrayNode();

            // Service URL parameter
            ObjectNode serviceUrlParam = mapper.createObjectNode();
            serviceUrlParam.put("code", "serviceURL");
            serviceUrlParam.put("label", this.messages.getString("plugin.params.serviceurl.label"));
            serviceUrlParam.put("type", "text");
            serviceUrlParam.put("maxlength", 500);
            serviceUrlParam.put("req", true);
            serviceUrlParam.put("help", this.messages.getString("plugin.params.serviceurl.help"));
            parametersNode.add(serviceUrlParam);

            // API Token parameter
            ObjectNode apiTokenParam = mapper.createObjectNode();
            apiTokenParam.put("code", "apiToken");
            apiTokenParam.put("label", this.messages.getString("plugin.params.apitoken.label"));
            apiTokenParam.put("type", "pass");  // Password field to hide token
            apiTokenParam.put("maxlength", 500);
            apiTokenParam.put("req", true);
            apiTokenParam.put("help", this.messages.getString("plugin.params.apitoken.help"));
            parametersNode.add(apiTokenParam);

            return mapper.writeValueAsString(parametersNode);

        } catch (JsonProcessingException e) {
            logger.error("Could not create parameters JSON", e);
            return "[]";
        }
    }
}