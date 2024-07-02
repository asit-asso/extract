/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.asit_asso.extract.plugins.fmedesktop;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ch.asit_asso.extract.plugins.common.IEmailSettings;
import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A plugin that executes an FME Desktop task.
 *
 * @author Florent Krin
 */
public class FmeDesktopPlugin implements ITaskProcessor {

    /**
     * The path to the file that holds the general settings of the plugin.
     */
    private static final String CONFIG_FILE_PATH = "plugins/fme/properties/configFME.properties";

    /**
     * The name of the file that holds the text explaining how to use this plugin in the language of
     * the user interface.
     */
    private static final String HELP_FILE_NAME = "fmeDesktopHelp.html";

    /**
     * Object that ensures that the test of available FME Desktop instances and the (possible) start of the extraction
     * process are atomic.
     */
    private static final Lock LOCK = new ReentrantLock(true);

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(FmeDesktopPlugin.class);

    /**
     * The string that identifies this plugin.
     */
    private final String code = "FME2017";

    /**
     * The class of the icon to use to represent this plugin.
     */
    private final String pictoClass = "fa-cogs";

    /**
     * The text that explains how to use this plugin in the language of the user interface.
     */
    private String help = null;

    /**
     * The stings that the plugin can send to the user in the language of the user interface.
     */
    private final LocalizedMessages messages;

    /**
     * The settings for the execution of this particular task.
     */
    private Map<String, String> inputs;

    /**
     * The access to the general settings of the plugin.
     */
    private final PluginConfiguration config;


    /**
     * Creates a new FME Desktop plugin instance with default settings and using the default language.
     */
    public FmeDesktopPlugin() {
        this.config = new PluginConfiguration(FmeDesktopPlugin.CONFIG_FILE_PATH);
        this.messages = new LocalizedMessages();
    }


    /**
     * Creates a new FME Desktop plugin instance with default settings.
     *
     * @param language the string that identifies the language to use to send messages to the user
     */
    public FmeDesktopPlugin(final String language) {
        this.config = new PluginConfiguration(FmeDesktopPlugin.CONFIG_FILE_PATH);
        this.messages = new LocalizedMessages(language);
    }


    /**
     * Creates a new FME Desktop plugin instance using the default language.
     *
     * @param taskSettings the settings for the execution of this particular task
     */
    public FmeDesktopPlugin(final Map<String, String> taskSettings) {
        this();
        this.inputs = taskSettings;
    }


    /**
     * Creates a new FME Desktop plugin instance.
     *
     * @param language     the string that identifies the language to use to send messages to the user
     * @param taskSettings the settings for the execution of this particular task
     */
    public FmeDesktopPlugin(final String language, final Map<String, String> taskSettings) {
        this(language);
        this.inputs = taskSettings;
    }


    @Override
    public final FmeDesktopPlugin newInstance(final String language) {
        return new FmeDesktopPlugin(language);
    }


    @Override
    public final FmeDesktopPlugin newInstance(final String language, final Map<String, String> taskSettings) {
        return new FmeDesktopPlugin(language, taskSettings);
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
            this.help = this.messages.getFileContent(FmeDesktopPlugin.HELP_FILE_NAME);
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

        ObjectNode scriptPathNode = parametersNode.addObject();
        scriptPathNode.put("code", this.config.getProperty("paramPath"));
        scriptPathNode.put("label", this.messages.getString("paramPath.label"));
        scriptPathNode.put("type", "text");
        scriptPathNode.put("req", true);
        scriptPathNode.put("maxlength", 255);

        ObjectNode fmePathNode = parametersNode.addObject();
        fmePathNode.put("code", this.config.getProperty("paramPathFME"));
        fmePathNode.put("label", this.messages.getString("paramPathFME.label"));
        fmePathNode.put("type", "text");
        fmePathNode.put("req", true);
        fmePathNode.put("maxlength", 255);

        ObjectNode fmeInstances = parametersNode.addObject();
        fmeInstances.put("code", this.config.getProperty("paramInstances"));
        fmeInstances.put("label", this.messages.getString("paramInstances.label")
                .replace("{maxInstances}", this.getMaxFmeInstances().toString()));
        fmeInstances.put("type", "numeric");
        fmeInstances.put("req", true);
        fmeInstances.put("min", 1);
        fmeInstances.put("step", 1);
        fmeInstances.put("max", this.getMaxFmeInstances());

        try {
            return mapper.writeValueAsString(parametersNode);

        } catch (JsonProcessingException exception) {
            logger.error("An error occurred when the parameters were converted to JSON.", exception);
            return null;
        }
    }


    @Override
    public final ITaskProcessorResult execute(final ITaskProcessorRequest request, final IEmailSettings emailSettings) {

        final FmeDesktopResult result = new FmeDesktopResult();
        FmeDesktopResult.Status resultStatus = FmeDesktopResult.Status.ERROR;
        String resultMessage = "";
        String resultErrorCode = "-1";

        try {

            final String fmeScriptPath = this.getFmeScriptPath();

            if (fmeScriptPath == null) {
                result.setStatus(FmeDesktopResult.Status.ERROR);
                result.setErrorCode("-1");
                result.setMessage(this.messages.getString("fme.script.notfound"));
                result.setRequestData(request);

                return result;
            }

            final String fmeExecutablePath = this.getFmeExecutablePath();

            if (fmeExecutablePath == null) {
                result.setStatus(FmeDesktopResult.Status.ERROR);
                result.setErrorCode("-1");
                result.setMessage(this.messages.getString("fme.executable.notfound"));
                result.setRequestData(request);

                return result;
            }

            final Process fmeTaskProcess = this.launchFmeTaskProcess(request, fmeScriptPath, fmeExecutablePath);

            if (fmeTaskProcess == null) {
                this.logger.warn("There wasn't enough licences to run the FME extraction. Task execution will be retried later.");
                result.setStatus(FmeDesktopResult.Status.NOT_RUN);
                result.setRequestData(request);
                return result;
            }

            fmeTaskProcess.waitFor();

            int retValue = fmeTaskProcess.exitValue();

            if (retValue != 0) {
                resultMessage = this.readInputStream(fmeTaskProcess.getErrorStream());

            } else {
                final File dirFolderOut = new File(request.getFolderOut());
                final File[] resultFiles = dirFolderOut.listFiles((dir, name) -> (name != null));
                final int resultFilesNumber = (resultFiles != null) ? resultFiles.length : 0;
                this.logger.debug("folder out {} contains {} file(s)", dirFolderOut.getPath(), resultFilesNumber);

                if (resultFilesNumber > 0) {
                    this.logger.debug("FME task succeeded");
                    resultStatus = FmeDesktopResult.Status.SUCCESS;
                    resultErrorCode = "";
                    resultMessage = this.messages.getString("fmeresult.message.success");

                } else {
                    this.logger.debug("Result folder is empty or not exists");
                    resultMessage = this.messages.getString("fmeresult.error.folderout.empty");
                }
            }

            this.logger.debug("End of FME extraction");

        } catch (Exception exception) {
            final String exceptionMessage = exception.getMessage();
            this.logger.error("The FME workspace has failed", exception);
            resultMessage = String.format(this.messages.getString("fme.executing.failed"), exceptionMessage);
        }

        result.setStatus(resultStatus);
        result.setErrorCode(resultErrorCode);
        result.setMessage(resultMessage);
        result.setRequestData(request);

        return result;

    }

    private String readInputStream(InputStream inputStream) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;
        List<String> messageLines = new ArrayList<>();

        while ((line = reader.readLine()) != null) {
            messageLines.add(line);
        }

        return StringUtils.join(messageLines, System.lineSeparator());
    }



    private Integer getMaxFmeInstances() {
        return NumberUtils.toInt(this.config.getProperty("maxFmeInstances"));
    }


    /**
     * Ensures that the quotes in a JSON parameters string are correctly formatted to be passed as a
     * parameter to FME Desktop.
     *
     * @param json the JSON parameter string
     * @return a properly quoted JSON string
     */
    private String formatJsonParametersQuotes(final String json) {

        if (StringUtils.isEmpty(json)) {
            return json;
        }

        return String.format("\"%s\"", json.replaceAll("\\\\\\\\", "\\\\u005c")
                                           .replaceAll("\\\\n", "\\\\u000d\\\\u000a")
                                           .replaceAll("\\\\\"", "\\\\u0022")
                                           .replaceAll("\"", "\"\"")
                                           .replaceAll("/", "\\\\u002f"));
    }



    /**
     * Obtains the command line that will launch the processing of the current request by FNE Desktop.
     *
     * @param request           the request to process
     * @param fmeScriptPath     the location of the FME script that will process the request
     * @param fmeExecutablePath the location of the FME Desktop executable file (fme.exe)
     * @return the FME command line
     */
    private String getFmeCommandForRequest(final ITaskProcessorRequest request, final String fmeScriptPath,
                                           final String fmeExecutablePath) {
        final String productId = request.getProductGuid();
        final String perimeter = request.getPerimeter();
        final String parameters = request.getParameters();

        return String.format(
                "\"%s\" \"%s\" --%s \"%s\" --%s \"%s\" --%s \"%s\" --%s %s --%s \"%s\" --%s %s --%s \"%s\" --%s \"%s\"",
                fmeExecutablePath, Paths.get(fmeScriptPath).getFileName().toString(),
                this.config.getProperty("paramRequestPerimeter"), perimeter,
                this.config.getProperty("paramRequestProduct"), productId,
                this.config.getProperty("paramRequestFolderOut"), request.getFolderOut(),
                this.config.getProperty("paramRequestParameters"), this.formatJsonParametersQuotes(parameters),
                this.config.getProperty("paramRequestOrderLabel"), request.getOrderLabel(),
                this.config.getProperty("paramRequestInternalId"), request.getId(),
                this.config.getProperty("paramRequestClientGuid"), request.getClientGuid(),
                this.config.getProperty("paramRequestOrganismGuid"), request.getOrganismGuid());
    }



    private String[] getFmeCommandForRequestAsArray(final ITaskProcessorRequest request, final String fmeScriptPath,
                                                    final String fmeExecutablePath) {
        final String productId = request.getProductGuid();
        final String perimeter = request.getPerimeter();
        final String parameters = request.getParameters();

        return new String[]{
            fmeExecutablePath, Paths.get(fmeScriptPath).getFileName().toString(),
            this.formatParameterName("paramRequestPerimeter"), perimeter,
            this.formatParameterName("paramRequestProduct"), productId,
            this.formatParameterName("paramRequestFolderOut"), request.getFolderOut(),
            this.formatParameterName("paramRequestParameters"), parameters,
            this.formatParameterName("paramRequestOrderLabel"), request.getOrderLabel(),
            this.formatParameterName("paramRequestInternalId"), Integer.toString(request.getId()),
            this.formatParameterName("paramRequestClientGuid"), request.getClientGuid(),
            this.formatParameterName("paramRequestOrganismGuid"), request.getOrganismGuid()
        };
    }


    private String formatParameterName(String parameterNamePropertyKey) {
        return String.format("--%s", this.config.getProperty(parameterNamePropertyKey));
    }



    /**
     * Obtains the location of the FME Desktop executable file in the file system.
     *
     * @return the FME Desktop executable file path or <code>null</code> if it does not exist or cannot be accessed
     */
    private String getFmeExecutablePath() {
        final String fmeExecutablePath = this.inputs.get(this.config.getProperty("paramPathFME"));
        final File fmeExecutable = new File(fmeExecutablePath);

        this.logger.debug("The FME executable path is {}. It {}, {} readable and {} a file.",
                fmeExecutablePath, (fmeExecutable.exists()) ? "exists" : "does not exist",
                (fmeExecutable.canRead()) ? "is" : "is not", (fmeExecutable.isFile()) ? "is" : "is not");

        if (!fmeExecutable.exists() || !fmeExecutable.canRead() || !fmeExecutable.isFile()) {
            return null;
        }

        return fmeExecutablePath;
    }


    /**
     * Obtains the location of the FME Desktop script to execute to process the current request.
     *
     * @return the FME Desktop script path or <code>null</code> if it does not exist or cannot be accessed
     */
    private String getFmeScriptPath() {
        final String fmwPath = this.inputs.get(this.config.getProperty("paramPath"));

        final File fmwScript = new File(fmwPath);

        this.logger.debug("The FME script path is {}. It {}, {} readable and {} a file.",
                fmwPath, (fmwScript.exists()) ? "exists" : "does not exist",
                (fmwScript.canRead()) ? "is" : "is not", (fmwScript.isFile()) ? "is" : "is not");

        if (!fmwScript.exists() || !fmwScript.canRead() || !fmwScript.isFile()) {
            return null;
        }

        return fmwPath;
    }


    private Process launchFmeTaskProcess(final ITaskProcessorRequest request, final String fmeScriptPath,
                                         final String fmeExecutablePath) throws IOException {

        try {
            FmeDesktopPlugin.LOCK.lock();
            this.logger.debug("Checking license availability…");

            if (!this.hasEnoughInstances()) {
                return null;
            }

            this.logger.debug("Start FME extraction");
            final Process fmeTaskProcess;
            final File dirWorkspace = new File(FilenameUtils.getFullPathNoEndSeparator(fmeScriptPath));
            this.logger.debug("Current working directory is {}", dirWorkspace);
            this.logger.debug("Current user is {}", System.getProperty("user.name"));
            ProcessBuilder processBuilder;

            if (SystemUtils.IS_OS_WINDOWS) {
                final String command = this.getFmeCommandForRequest(request, fmeScriptPath, fmeExecutablePath);
                this.logger.debug("Executed command line is : {}", command);
                //fmeTaskProcess = Runtime.getRuntime().exec(command, null, dirWorkspace);
                processBuilder = new ProcessBuilder(command);

            } else {
                final String[] commandArray = this.getFmeCommandForRequestAsArray(request, fmeScriptPath, fmeExecutablePath);
                this.logger.debug("Executed command line tokens are : {}", StringUtils.join(commandArray, " "));
                //fmeTaskProcess = Runtime.getRuntime().exec(commandArray, null, dirWorkspace);
                processBuilder = new ProcessBuilder(commandArray);
            }

            fmeTaskProcess = processBuilder.directory(dirWorkspace)
                                           .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                                           .start();

            try {
                // Gives the FME process some time to start before checking the number of available instances again
                Thread.sleep(200);

            } catch (InterruptedException interruptedException) {
                this.logger.warn("The wait timeout to let the FME extraction start has been interrupted.",
                        interruptedException);
            }

            return fmeTaskProcess;

        } finally {
            FmeDesktopPlugin.LOCK.unlock();
        }
    }


    private boolean hasEnoughInstances() {
        int requiredInstances = NumberUtils.toInt(this.inputs.get(this.config.getProperty("paramInstances")));
        int currentInstances = this.getCurrentFmeInstances();
        int maximumInstances = this.getMaxFmeInstances();

        this.logger.debug("Task requires {} instances, {} instances are already running from a maximum of {}",
                requiredInstances, currentInstances, maximumInstances);
        return (maximumInstances - currentInstances) >= requiredInstances;
    }


    private int getCurrentFmeInstances() {
        Process process;
        Runtime runtime = Runtime.getRuntime();

        try {
            this.logger.debug("Current process user is {}.", System.getProperty("user.name"));

            if (SystemUtils.IS_OS_WINDOWS) {
                process = runtime.exec(System.getenv("windir") + "\\system32\\tasklist.exe /fo csv /nh /FI \"IMAGENAME eq fme.exe\"");

            } else if (SystemUtils.IS_OS_LINUX) {
                process = runtime.exec("pgrep -l ^fme$");

            } else {
                this.logger.error("This operating system is not supported by Extract.");
                throw new UnsupportedOperationException("Unsupported operating system.");
            }

            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String processItem;
            int instances = 0;
            this.logger.debug("Fetching current FME processes:");
            while ((processItem = input.readLine()) != null) {
                this.logger.debug(processItem);

                if (processItem.startsWith("INFO:")) {
                    continue;
                }

                instances++;
            }
            input.close();

            return instances;

        } catch (IOException ioException) {
            this.logger.error("Unable to get the running FME processes.", ioException);
            throw new RuntimeException("Could not get FME instances.", ioException);
        }

    }

}
