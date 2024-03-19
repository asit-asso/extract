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
package ch.asit_asso.extract.plugins.fmeserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import ch.asit_asso.extract.plugins.common.IEmailSettings;
import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * A plugin that executes an FME Server task.
 *
 * @author Florent Krin
 */
public class FmeServerPlugin implements ITaskProcessor {

    /**
     * The path to the file that holds the general settings for this plugin.
     */
    private static final String CONFIG_FILE_PATH = "plugins/fmeserver/properties/config.properties";

    /**
     * The default value of the port to use for HTTP requests.
     */
    private static final int DEFAULT_HTTP_PORT = 80;

    /**
     * The default value of the port to use for secure HTTP requests.
     */
    private static final int DEFAULT_HTTPS_PORT = 443;

    /**
     * The name of the file that holds the text explaining how to use this plugin in the language of
     * the user interface.
     */
    private static final String HELP_FILE_NAME = "fmeServerHelp.html";

    /**
     * The number returned in an HTTP response to tell that the request resulted in the creation of
     * a resource.
     */
    private static final int HTTP_CREATED_RESULT_CODE = 201;

    /**
     * The number returned in an HTTP response to tell that the request succeeded.
     */
    private static final int HTTP_OK_RESULT_CODE = 200;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(FmeServerPlugin.class);

    /**
     * The string that identifies this plugin.
     */
    private final String code = "FMESERVER";

    /**
     * The class of the icon to display to represent this plugin.
     */
    private final String pictoClass = "fa-cogs";

    /**
     * The text that explains how to use this plugin in the language of the user interface.
     */
    private String help = null;

    /**
     * The strings that this plugin can send to the user in the language of the user interface.
     */
    private LocalizedMessages messages;

    /**
     * The settings for the execution of this particular task.
     */
    private Map<String, String> inputs;

    /**
     * The general settings for this plugin.
     */
    private PluginConfiguration config;



    /**
     * Creates a new FME Server plugin instance with default settings and using the default language.
     */
    public FmeServerPlugin() {
        this.config = new PluginConfiguration(FmeServerPlugin.CONFIG_FILE_PATH);
        this.messages = new LocalizedMessages();
    }



    /**
     * Creates a new FME Server plugin instance with default settings.
     *
     * @param language the string that identifies the language of the user interface
     */
    public FmeServerPlugin(final String language) {
        this.config = new PluginConfiguration(FmeServerPlugin.CONFIG_FILE_PATH);
        this.messages = new LocalizedMessages(language);
    }



    /**
     * Creates a new FME Server plugin instance using the default language.
     *
     * @param taskSettings a map with the settings for the execution of this particular task
     */
    public FmeServerPlugin(final Map<String, String> taskSettings) {
        this();
        this.inputs = taskSettings;
    }



    /**
     * Creates a new FME Server plugin instance.
     *
     * @param language     the string that identifies the language of the user interface
     * @param taskSettings a map with the settings for the execution of this particular task
     */
    public FmeServerPlugin(final String language, final Map<String, String> taskSettings) {
        this(language);
        this.inputs = taskSettings;
    }



    @Override
    public final FmeServerPlugin newInstance(final String language) {
        return new FmeServerPlugin(language);
    }



    @Override
    public final FmeServerPlugin newInstance(final String language, final Map<String, String> taskSettings) {
        return new FmeServerPlugin(language, taskSettings);
    }



    @Override
    public final String getLabel() {
        return this.messages.getString("plugin.label");
    }



    @Override
    public final String getCode() {
        return this.code;
    }



    @Override
    public final String getDescription() {
        return this.messages.getString("plugin.description");
    }



    @Override
    public final String getHelp() {

        if (this.help == null) {
            this.help = this.messages.getFileContent(FmeServerPlugin.HELP_FILE_NAME);
        }

        return this.help;
    }



    @Override
    public final String getPictoClass() {
        return this.pictoClass;
    }



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

        try {
            return mapper.writeValueAsString(parametersNode);

        } catch (JsonProcessingException exception) {
            logger.error("An error occurred when the parameters were converted to JSON.", exception);
            return null;
        }
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

        return String.format("%s - %s", genericErrorMessage, httpErrorMessage);
    }



    @Override
    public final ITaskProcessorResult execute(final ITaskProcessorRequest request, final IEmailSettings emailSettings) {
        final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        final Date now = new Date();

        try {

            this.logger.debug("Start FME extraction");

            final String url = this.inputs.get(this.config.getProperty("paramUrl"));
            final String login = this.inputs.get(this.config.getProperty("paramLogin"));
            final String password = this.inputs.get(this.config.getProperty("paramPassword"));
            final String productId = request.getProductGuid();
            final String perimeter = request.getPerimeter();
            final String parameters = request.getParameters();
            String folderOut = request.getFolderOut();
            final String subFolderName = String.format("%s_%s", dateTimeFormat.format(now), productId);

            if (!StringUtils.isEmpty(folderOut)) {
                folderOut = Paths.get(folderOut, subFolderName).toString();
            }

            this.logger.debug("calling fme task from url {}.", url);

            final URIBuilder uriBuilder = new URIBuilder(url);
            uriBuilder.setParameter(config.getProperty("paramRequestResponseFormat"), "json");
            uriBuilder.setParameter(config.getProperty("paramRequestProduct"), productId);
            uriBuilder.setParameter(config.getProperty("paramRequestPerimeter"), perimeter);
            uriBuilder.setParameter(config.getProperty("paramRequestParameters"), parameters);
            uriBuilder.setParameter(config.getProperty("paramRequestFolderOut"), folderOut);
            uriBuilder.setParameter(config.getProperty("paramRequestOrderLabel"), request.getOrderLabel());
            uriBuilder.setParameter(config.getProperty("paramRequestInternalId"), String.format("%d", request.getId()));
            uriBuilder.setParameter(config.getProperty("paramRequestClientGuid"), request.getClientGuid());
            uriBuilder.setParameter(config.getProperty("paramRequestOrganismGuid"), request.getOrganismGuid());

            final FmeServerResult result = this.sendServerRequest(uriBuilder.build(), login, password, folderOut);
            result.setRequestData(request);

            return result;

        } catch (Exception exception) {
            this.logger.error("The FME extraction service has failed", exception);

            final FmeServerResult result = new FmeServerResult();
            result.setMessage(String.format(this.messages.getString("fme.executing.failed"), exception.getMessage()));
            result.setErrorCode("-1");
            result.setRequestData(request);
            result.setStatus(ITaskProcessorResult.Status.ERROR);

            return result;
        }
    }



    /**
     * Submits a task to the FME server.
     *
     * @param url       the URI of the task to execute
     * @param login     the user name to use to authenticate with the FME server
     * @param password  the password to use to authenticate with the FME server
     * @param folderOut a string that contains the path of the folder where the result of the task must be written
     * @return a result object that describes the outcome of the task
     * @throws IOException an error occurred when communicating with the FME Server
     */
    private FmeServerResult sendServerRequest(final URI url, final String login, final String password,
            final String folderOut) throws IOException {
        HttpHost targetHost = this.getHostFromUri(url);

        try (CloseableHttpClient httpclient = this.getHttpClient(targetHost, login, password)) {
            HttpGet httpGet = new HttpGet(url);

            this.logger.debug("Executing FME request.");

            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                return this.parseServerResponse(response, folderOut);
            }
        }

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

        if (port < 0) {

            switch (scheme.toLowerCase()) {

                case "http":
                    this.logger.debug("No port in URL for host {}. Using HTTP default 80.", hostName);
                    port = FmeServerPlugin.DEFAULT_HTTP_PORT;
                    break;

                case "https":
                    this.logger.debug("No port in URL for host {}. Using HTTPS default 443.", hostName);
                    port = FmeServerPlugin.DEFAULT_HTTPS_PORT;
                    break;

                default:
                    this.logger.error("The protocol {} is not supported", scheme);
                    return null;
            }
        }

        return new HttpHost(hostName, port, scheme);
    }



    /**
     * Builds a client object to communicate with an HTTP server.
     *
     * @param targetHost     the HTTP host that the client must communicate with
     * @param targetLogin    the user name to use to authenticate with the server
     * @param targetPassword the password to user to authenticate with the server
     * @return the HTTP client
     */
    private CloseableHttpClient getHttpClient(final HttpHost targetHost, final String targetLogin,
            final String targetPassword) {
        assert targetHost != null : "The target host cannot be null";

        final CredentialsProvider credentials = this.getCredentialsProvider(targetHost, targetLogin, targetPassword);

        return HttpClients.custom().setDefaultCredentialsProvider(credentials).build();
    }



    /**
     * Builds an object that will hold authentication information for an HTTP request.
     *
     * @param targetHost     the server to authenticate with
     * @param targetLogin    the user name to use to authenticate with the server
     * @param targetPassword the password to use to authenticate with the server
     * @return the credentials provider
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
     * Adds a set of authentication information to an existing credentials provider object.
     *
     * @param provider the credentials provider to add the authentication information to
     * @param host     the server to authenticate with
     * @param login    the user name to use to authenticate with the server
     * @param password the password to use to authenticate with the server
     * @return the credentials provider with the new credentials added
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
     * Processes the HTTP response returned by the FME Server task.
     *
     * @param response  the HTTP response returned by the task
     * @param folderOut a string that contains the path to the folder where the result of the task must be written
     * @return a result object that describes the outcome of the task
     * @throws IOException an error occurred when the response was read
     */
    private FmeServerResult parseServerResponse(final HttpResponse response, final String folderOut)
            throws IOException {

        final int httpCode = response.getStatusLine().getStatusCode();
        this.logger.debug("HTTP FME request completed with status code {}.", httpCode);

        if (httpCode != FmeServerPlugin.HTTP_CREATED_RESULT_CODE && httpCode != FmeServerPlugin.HTTP_OK_RESULT_CODE) {
            this.logger.error("HTTP FME request failed.");
            FmeServerResult result = new FmeServerResult();
            result.setErrorCode(String.valueOf(httpCode));
            result.setMessage(this.getMessageFromHttpCode(httpCode));
            result.setStatus(FmeServerResult.Status.ERROR);

            return result;
        }

        this.logger.debug("HTTP request was successful. Response was {}.", response);
        final JSONObject json = this.readResponseContentAsJson(response);

        return this.buildResultFromServiceResponse(json.getJSONObject("serviceResponse"), folderOut);
    }



    /**
     * Parses the JSON returned by the FME Server task to get its outcome.
     *
     * @param jsonServiceResponse the JSON object built from the response returned by the task
     * @param folderOut           a string that contains the path to the folder where the file generated by the task
     *                            must be written
     * @return a result object that describes the outcome of the task
     */
    private FmeServerResult buildResultFromServiceResponse(final JSONObject jsonServiceResponse,
            final String folderOut) {
        final String status = jsonServiceResponse.getJSONObject("statusInfo").getString("status");
        this.logger.debug("Status is:\n{}.", status);

        if (!"success".equals(status)) {
            this.logger.debug("error Message is:\n{}.", status);
            final FmeServerResult result = new FmeServerResult();
            result.setErrorCode("-1");
            result.setStatus(ITaskProcessorResult.Status.ERROR);
            result.setMessage(status);

            return result;
        }

        if (!jsonServiceResponse.has("url") || jsonServiceResponse.getString("url").equals("")
                || jsonServiceResponse.getString("url") == null) {
            this.logger.debug("Result url is not exists or empty");
            final FmeServerResult result = new FmeServerResult();
            result.setMessage(this.messages.getString("fmeresult.error.url.notfound"));
            result.setErrorCode("-1");
            result.setStatus(FmeServerResult.Status.ERROR);

            return result;
        }

        return this.downloadResultFile(jsonServiceResponse.getString("url"), folderOut);
    }



    /**
     * Obtains the file generated by the FME Server task from the server.
     *
     * @param resultUrl a string that contains the address where the result file can be obtained
     * @param folderOut a string that contains the path to the folder where the generated file must be written
     * @return a result object that describes the outcome of the task
     */
    private FmeServerResult downloadResultFile(final String resultUrl, final String folderOut) {
        final FmeServerResult result = new FmeServerResult();
        final File folderOutFile = new File(folderOut);

        if (!this.downloadFromUrl(resultUrl, folderOut, folderOutFile.getName())) {
            this.logger.debug("Could not download the result file.");
            result.setMessage(this.messages.getString("fmeresult.error.download.failed"));
            result.setErrorCode("-1");
            result.setStatus(FmeServerResult.Status.ERROR);

            return result;
        }

        result.setErrorCode("");
        result.setMessage(this.messages.getString("fmeresult.message.success"));
        result.setStatus(FmeServerResult.Status.SUCCESS);

        return result;
    }



    /**
     * Gets the content of the HTTP response returned by the FME Server task.
     *
     * @param response the HTTP response returned by the task
     * @return the content of the response as a string
     * @throws IOException an error occurred when the response was read
     */
    private String readResponseContent(final HttpResponse response) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();

        try (BufferedReader breader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            String line;

            while ((line = breader.readLine()) != null) {
                contentBuilder.append(line);
            }
        }

        this.logger.debug("Response content is:\n{}.", contentBuilder.toString());
        return contentBuilder.toString();
    }



    /**
     * Processes the content of the HTTP response returned by the FME Server task as JSON.
     *
     * @param response the HTTP response returned by the FME Server task
     * @return the content of the response as a JSON object
     * @throws IOException an error occurred when the response was read
     */
    private JSONObject readResponseContentAsJson(final HttpResponse response) throws IOException {
        return new JSONObject(this.readResponseContent(response));
    }



    /**
     * Obtains an archive file from a URL.
     *
     * @param urlString a string that contains the address where the file can be obtained
     * @param folderOut a string that contains the path to the folder where the file must be written
     * @param zipName   the name to give the local ZIP file
     * @return <code>true</code> if the ZIP file was successfully downloaded
     */
    private boolean downloadFromUrl(final String urlString, final String folderOut, final String zipName) {

        try {
            final URL url = new URL(urlString);
            final ReadableByteChannel remoteFileByteChannel = Channels.newChannel(url.openStream());
            final File destinationFolder = new File(folderOut);

            if (!destinationFolder.exists()) {
                destinationFolder.mkdirs();
            }

            final File outputFile = new File(folderOut, StringUtils.appendIfMissingIgnoreCase(zipName, ".zip"));
            final FileOutputStream localFileOutputStream = new FileOutputStream(outputFile);
            localFileOutputStream.getChannel().transferFrom(remoteFileByteChannel, 0, Long.MAX_VALUE);

            return true;

        } catch (Exception exception) {
            this.logger.error("Result zip could not be downloaded.", exception);
            return false;
        }
    }

}
