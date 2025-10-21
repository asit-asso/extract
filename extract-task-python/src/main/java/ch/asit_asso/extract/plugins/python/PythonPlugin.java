/*
 * Copyright (C) 2025 SecureMind
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
package ch.asit_asso.extract.plugins.python;

import ch.asit_asso.extract.plugins.common.IEmailSettings;
import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A plugin that executes Python scripts with parameters passed via JSON file
 *
 * @author Bruno Alves
 */
public class PythonPlugin implements ITaskProcessor {

    private static final java.util.regex.Pattern PY_TRACE_FILE_LINE =
            java.util.regex.Pattern.compile("^\\s*File\\s+\"([^\"]+)\",\\s+line\\s+(\\d+)(?:,\\s+in\\s+(.+))?$");


    /**
     * The relative path to the file that holds the general settings for this plugin.
     */
    private static final String CONFIG_FILE_PATH = "plugins/python/properties/config.properties";

    /**
     * The name of the file that holds the text explaining how to use this plugin in the language of
     * the user interface.
     */
    private static final String HELP_FILE_NAME = "help.html";

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(PythonPlugin.class);

    /**
     * The string that identifies this plugin.
     */
    private final String code = "python";

    /**
     * The text that explains how to use this plugin in the language of the user interface.
     */
    private String help = null;

    /**
     * The CSS class of the icon to display to represent this plugin.
     */
    private final String pictoClass = "fa-cogs";

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
    private final PluginConfiguration config;

    /**
     * Creates a new instance of the Python plugin with default settings and using the default language.
     */
    public PythonPlugin() {
        this.config = new PluginConfiguration(PythonPlugin.CONFIG_FILE_PATH);
        this.messages = new LocalizedMessages();
    }

    /**
     * Creates a new instance of the Python plugin with default settings.
     *
     * @param language the string that identifies the language of the user interface
     */
    public PythonPlugin(final String language) {
        this.config = new PluginConfiguration(PythonPlugin.CONFIG_FILE_PATH);
        this.messages = new LocalizedMessages(language);
    }

    /**
     * Creates a new instance of the Python plugin using the default language.
     *
     * @param taskSettings a map that contains the settings for the execution of this task
     */
    public PythonPlugin(final Map<String, String> taskSettings) {
        this();
        this.inputs = taskSettings;
    }

    /**
     * Creates a new instance of the Python plugin.
     *
     * @param language     the string that identifies the language of the user interface
     * @param taskSettings a map that contains the settings for the execution of this task
     */
    public PythonPlugin(final String language, final Map<String, String> taskSettings) {
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
    public ITaskProcessor newInstance(final String language) {
        return new PythonPlugin(language);
    }

    /**
     * Returns a new task processor instance with the provided settings.
     *
     * @param language the locale code of the language to display the messages in
     * @param inputs   a map that contains the settings for the execution of this task
     * @return the new task processor instance
     */
    @Override
    public ITaskProcessor newInstance(final String language, final Map<String, String> inputs) {
        return new PythonPlugin(language, inputs);
    }

    /**
     * Returns the string that uniquely identifies this plugin.
     *
     * @return the plugin unique identifier string
     */
    @Override
    public String getCode() {
        return this.code;
    }

    /**
     * Returns the text that describes this plugin.
     *
     * @return the string that describes this plugin
     */
    @Override
    public String getLabel() {
        return this.messages.getString("plugin.label");
    }

    /**
     * Returns the description of this plugin.
     *
     * @return the string that describes this plugin
     */
    @Override
    public String getDescription() {
        return this.messages.getString("plugin.description");
    }

    /**
     * Returns the text that explains how to use this plugin.
     *
     * @return the string that explains this plugin
     */
    @Override
    public String getHelp() {
        final String helpFilePath = String.format("%s/lang/%s/%s",
                CONFIG_FILE_PATH.replace("/properties/config.properties", ""),
                this.messages.getLocale().getLanguage(),
                HELP_FILE_NAME
        );

        if (this.help == null) {
            this.help = this.messages.getHelp(helpFilePath);
        }
        return this.help;
    }

    /**
     * Returns the class name of the icon for this plugin.
     *
     * @return the CSS class that identifies the icon
     */
    @Override
    public String getPictoClass() {
        return this.pictoClass;
    }

    /**
     * Returns the parameters definition for this plugin.
     *
     * @return a JSON array containing the plugin parameters
     */
    @Override
    public String getParams() {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode parametersNode = mapper.createArrayNode();

        // Python interpreter path parameter
        ObjectNode pythonInterpreterParam = mapper.createObjectNode();
        pythonInterpreterParam.put("code", "pythonInterpreter");
        pythonInterpreterParam.put("label", this.messages.getString("plugin.params.pythonInterpreter.label"));
        pythonInterpreterParam.put("type", "text");
        pythonInterpreterParam.put("req", true);
        pythonInterpreterParam.put("maxlength", 255);
        pythonInterpreterParam.put("help", this.messages.getString("plugin.params.pythonInterpreter.help"));
        parametersNode.add(pythonInterpreterParam);

        // Python script path parameter
        ObjectNode pythonScriptParam = mapper.createObjectNode();
        pythonScriptParam.put("code", "pythonScript");
        pythonScriptParam.put("label", this.messages.getString("plugin.params.pythonScript.label"));
        pythonScriptParam.put("type", "text");
        pythonScriptParam.put("req", true);
        pythonScriptParam.put("maxlength", 500);
        pythonScriptParam.put("help", this.messages.getString("plugin.params.pythonScript.help"));
        parametersNode.add(pythonScriptParam);

        try {
            return mapper.writeValueAsString(parametersNode);
        } catch (JsonProcessingException e) {
            logger.error("Could not create parameters JSON", e);
            return "[]";
        }
    }

    /**
     * Executes a Python plugin task defined by the given request and email settings.
     * The method performs necessary validations, prepares input directories,
     * generates a parameters file, and invokes a Python script for execution.
     *
     * @param request The task processor request containing information to compute,
     *                including input/output directories, parameters, and task details.
     * @param emailSettings The email settings required for notifying about the task progress
     *                      or errors if needed.
     * @return The result of the task execution, containing success status,
     *         output directory path, and error messages or descriptive information in case of failure.
     */
    @Override
    public ITaskProcessorResult execute(ITaskProcessorRequest request, IEmailSettings emailSettings) {
        this.logger.debug("Starting Python plugin execution");

        PythonResult result = new PythonResult();
        result.setRequestData(request);

        try {
            // Check if inputs are initialized
            if (this.inputs == null || this.inputs.isEmpty()) {
                String errorMessage = this.messages.getString("plugin.errors.interpreter.config");
                this.logger.error(errorMessage);
                result.setSuccess(false);
                result.setMessage(errorMessage);  // Use setMessage for consistency
                return result;
            }

            // Validate required parameters
            String pythonInterpreter = this.inputs.get("pythonInterpreter");
            String pythonScript = this.inputs.get("pythonScript");

            if (pythonInterpreter == null || pythonInterpreter.trim().isEmpty()) {
                String errorMessage = this.messages.getString("plugin.errors.interpreter.missing");
                this.logger.error(errorMessage);
                result.setSuccess(false);
                result.setMessage(errorMessage);  // Use setMessage for consistency
                return result;
            }

            if (pythonScript == null || pythonScript.trim().isEmpty()) {
                String errorMessage = this.messages.getString("plugin.errors.script.missing");
                this.logger.error(errorMessage);
                result.setSuccess(false);
                result.setMessage(errorMessage);  // Use setMessage for consistency
                return result;
            }

            // Trim paths to remove extra spaces
            pythonInterpreter = pythonInterpreter.trim();
            pythonScript = pythonScript.trim();

            // Check if Python interpreter exists and is executable
            File pythonInterpreterFile = new File(pythonInterpreter);
            if (!pythonInterpreterFile.exists()) {
                String errorMessage = String.format(
                        this.messages.getString("plugin.errors.interpreter.not.found"),
                        pythonInterpreter
                );
                this.logger.error(errorMessage);
                result.setSuccess(false);
                result.setMessage(errorMessage);
                return result;
            }

            if (!pythonInterpreterFile.canExecute()) {
                String errorMessage = String.format(
                        this.messages.getString("plugin.errors.interpreter.not.executable"),
                        pythonInterpreter
                );
                this.logger.error(errorMessage);
                result.setSuccess(false);
                result.setMessage(errorMessage);
                return result;
            }

            // Check if Python script exists and is readable
            File pythonScriptFile = new File(pythonScript);
            if (!pythonScriptFile.exists()) {
                String errorMessage = String.format(
                        this.messages.getString("plugin.errors.script.not.found"),
                        pythonScript
                );
                this.logger.error(errorMessage);
                result.setSuccess(false);
                result.setMessage(errorMessage);
                return result;
            }

            if (!pythonScriptFile.canRead()) {
                String errorMessage = String.format(
                        this.messages.getString("plugin.errors.script.not.readable"),
                        pythonScript
                );
                this.logger.error(errorMessage);
                result.setSuccess(false);
                result.setMessage(errorMessage);
                return result;
            }

            // Validate and create input directory for parameters file
            String folderIn = request.getFolderIn();
            if (folderIn == null || folderIn.trim().isEmpty()) {
                String errorMessage = this.messages.getString("plugin.errors.folderin.undefined");
                this.logger.error(errorMessage);
                result.setSuccess(false);
                result.setMessage(errorMessage);
                return result;
            }

            File inputDir = new File(folderIn);
            if (!inputDir.exists()) {
                if (!inputDir.mkdirs()) {
                    String errorMessage = String.format(
                            this.messages.getString("plugin.errors.folderin.creation.failed"),
                            folderIn
                    );
                    this.logger.error(errorMessage);
                    result.setSuccess(false);
                    result.setMessage(errorMessage);
                    return result;
                }
            }

            if (!inputDir.canWrite()) {
                String errorMessage = String.format(
                        this.messages.getString("plugin.errors.folderin.not.writable"),
                        folderIn
                );
                this.logger.error(errorMessage);
                result.setSuccess(false);
                result.setMessage(errorMessage);
                return result;
            }

            // Validate output directory exists
            String folderOut = request.getFolderOut();
            if (folderOut == null || folderOut.trim().isEmpty()) {
                String errorMessage = this.messages.getString("plugin.errors.folderout.undefined");
                this.logger.error(errorMessage);
                result.setSuccess(false);
                result.setMessage(errorMessage);
                return result;
            }

            File outputDir = new File(folderOut);
            if (!outputDir.exists()) {
                if (!outputDir.mkdirs()) {
                    String errorMessage = String.format(
                            this.messages.getString("plugin.errors.folderout.creation.failed"),
                            folderOut
                    );
                    this.logger.error(errorMessage);
                    result.setSuccess(false);
                    result.setMessage(errorMessage);
                    return result;
                }
            }

            // Create parameters JSON file in FolderIn
            File parametersFile = new File(inputDir, "parameters.json");
            try {
                createParametersFile(request, parametersFile);
                this.logger.info("Parameters file created successfully: {}", parametersFile.getAbsolutePath());
            } catch (IOException e) {
                String errorMessage = String.format(
                        this.messages.getString("plugin.errors.parameters.file.creation.io"),
                        e.getMessage()
                );
                this.logger.error(errorMessage, e);
                result.setSuccess(false);
                result.setMessage(errorMessage);
                return result;
            } catch (Exception e) {
                String errorMessage = String.format(
                        this.messages.getString("plugin.errors.parameters.file.creation.unexpected"),
                        e.getMessage()
                );
                this.logger.error(errorMessage, e);
                result.setSuccess(false);
                result.setMessage(errorMessage);
                return result;
            }

            // Verify parameters file was created
            if (!parametersFile.exists() || !parametersFile.canRead()) {
                String errorMessage = this.messages.getString("plugin.errors.parameters.file.not.readable");
                this.logger.error(errorMessage);
                result.setSuccess(false);
                result.setMessage(errorMessage);
                return result;
            }

            // Execute Python script
            String errorMessage = executePythonScript(pythonInterpreter, pythonScript,
                                                     parametersFile, request);

            if (errorMessage == null) {
                this.logger.info("Python script executed successfully");
                result.setSuccess(true);
                result.setMessage(this.messages.getString("plugin.messages.script.executed"));
                result.setResultFilePath(folderOut);
            } else {
                // Error occurred during execution - put error in message like FMEDesktop
                this.logger.error("Python script execution failed: {}", errorMessage);
                result.setSuccess(false);
                result.setMessage(errorMessage);  // Use setMessage instead of setErrorMessage
            }

        } catch (SecurityException e) {
            String errorMessage = String.format(
                    this.messages.getString("plugin.errors.security.exception"),
                    e.getMessage()
            );
            this.logger.error(errorMessage, e);
            result.setSuccess(false);
            result.setMessage(errorMessage);  // Use setMessage instead of setErrorMessage
        } catch (Exception e) {
            String errorMessage = String.format(
                    this.messages.getString("plugin.errors.unexpected"),
                    e.getClass().getSimpleName(),
                    e.getMessage() != null ? e.getMessage() : ""
            );
            this.logger.error("Unexpected error during Python plugin execution", e);
            result.setSuccess(false);
            result.setMessage(errorMessage);  // Use setMessage instead of setErrorMessage
        }

        return result;
    }

    /**
     * Executes the Python script with the given parameters.
     *
     * @param pythonExecutable the path to Python interpreter
     * @param scriptPath the path to the Python script
     * @param parametersFile the parameters JSON file
     * @param request the task processor request
     * @return null if successful, error message if failed
     */
    private String executePythonScript(String pythonExecutable, String scriptPath,
                                       File parametersFile, ITaskProcessorRequest request) {
        this.logger.debug("Executing Python script: {} with parameters file: {}", scriptPath, parametersFile);

        try {
            // Build command
            List<String> command = new ArrayList<>();
            command.add(pythonExecutable);
            command.add(scriptPath);
            command.add(parametersFile.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder(command);

            // Set working directory to the script's directory
            File scriptFile = new File(scriptPath);
            File workingDir = scriptFile.getParentFile();
            if (workingDir == null || !workingDir.exists() || !workingDir.isDirectory()) {
                return String.format(this.messages.getString("plugin.errors.script.directory.invalid"),
                        workingDir != null ? workingDir.getAbsolutePath() : "null");
            }
            processBuilder.directory(workingDir);

            // Capture both stdout and stderr in a single stream
            processBuilder.redirectErrorStream(true);

            this.logger.info("Executing command: {} in directory: {}",
                    String.join(" ", command), workingDir);

            Process process;
            try {
                process = processBuilder.start();
            } catch (IOException e) {
                String errorDetail = e.getMessage();
                return String.format(this.messages.getString("plugin.errors.script.launch.failed"), errorDetail);
            }

            // Capture output with timeout handling
            StringBuilder mergedOutput = new StringBuilder();      // Combined stdout/stderr
            StringBuilder tracebackBuffer = new StringBuilder();   // Only traceback/error lines
            boolean hasError = false;
            boolean inTraceback = false;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    mergedOutput.append(line).append("\n");

                    // Detect start of Python traceback
                    if (line.contains("Traceback (most recent call last):")) {
                        inTraceback = true;
                        hasError = true;
                        tracebackBuffer.append(line).append("\n");
                        this.logger.error("Python Traceback started: {}", line);
                        continue;
                    }

                    // If in traceback, capture all lines including file/line info
                    if (inTraceback) {
                        tracebackBuffer.append(line).append("\n");
                        this.logger.error("Python Traceback: {}", line);

                        // Check if this is a line with file and line number info
                        if (line.trim().startsWith("File \"") && line.contains(", line ")) {
                            this.logger.error("  -> Error location: {}", line.trim());
                        }

                        // Check if traceback is ending (error type line)
                        if (line.matches("^[A-Z][a-zA-Z]*(?:Error|Exception):.*")) {
                            inTraceback = false;
                        }
                    }

                    // Detect Python-specific errors outside formal traceback
                    if (line.matches(".*\\b(SyntaxError|IndentationError|TabError|NameError|ImportError|ModuleNotFoundError|FileNotFoundError|PermissionError|ValueError|TypeError|KeyError|AttributeError|IndexError)\\b.*")) {
                        hasError = true;
                        if (!inTraceback) {
                            tracebackBuffer.append(line).append("\n");
                            this.logger.error("Python Error: {}", line);
                        }
                    } else if (!inTraceback) {
                        this.logger.info("Python: {}", line);
                    }
                }
            } catch (IOException e) {
                return String.format(this.messages.getString("plugin.errors.output.read.failed"), e.getMessage());
            }

            // Wait for completion with timeout (5 minutes)
            boolean completed;
            try {
                completed = process.waitFor(300, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                process.destroyForcibly();
                return this.messages.getString("plugin.errors.execution.interrupted");
            }

            if (!completed) {
                process.destroyForcibly();
                return this.messages.getString("plugin.errors.execution.timeout");
            }

            int exitCode = process.exitValue();
            this.logger.info("Python script finished with exit code: {}", exitCode);

            if (exitCode != 0) {
                String scriptOutput = mergedOutput.toString().trim();
                String tracebackText = tracebackBuffer.toString().trim();

                // Always log full error context for debugging
                String fullErrorForLog = tracebackText.isEmpty() ? scriptOutput : tracebackText + "\n\n--- Merged Output ---\n" + scriptOutput;
                this.logger.error("Full Python error (traceback and output):\n{}", fullErrorForLog);

                // Build a concise, user-facing error with file/line if available
                String detailed = buildDetailedPythonError(
                        tracebackText.isEmpty() ? scriptOutput : tracebackText,
                        scriptOutput,
                        scriptPath,
                        exitCode
                );

                // Keep existing exit-code specific handling for non-1 codes
                if (exitCode == 1) {
                    return String.format(this.messages.getString("plugin.errors.detected"), detailed);
                }

                switch (exitCode) {
                    case 2:
                        return String.format(this.messages.getString("plugin.errors.bad.usage"),
                                scriptOutput.isEmpty() ? "" :
                                        this.messages.getString("plugin.errors.details.prefix") + "\n" + scriptOutput);
                    case 126:
                        return String.format(this.messages.getString("plugin.errors.script.not.executable"), scriptPath);
                    case 127:
                        return String.format(this.messages.getString("plugin.errors.command.not.found"), pythonExecutable);
                    case -1:
                    case 255:
                        String base = this.messages.getString("plugin.errors.terminated.abnormally");
                        if (!scriptOutput.isEmpty()) {
                            base += "\n" + this.messages.getString("plugin.errors.details.prefix") + "\n" + scriptOutput;
                        }
                        return base;
                    default:
                        if (!scriptOutput.isEmpty()) {
                            return String.format(this.messages.getString("plugin.errors.exit.code.with.output"),
                                    exitCode, detailed.isEmpty() ? scriptOutput : detailed);
                        } else {
                            return String.format(this.messages.getString("plugin.errors.exit.code"), exitCode);
                        }
                }
            }

            this.logger.info("Python script executed successfully");
            return null; // Success

        } catch (SecurityException e) {
            return String.format(this.messages.getString("plugin.errors.security"), e.getMessage());
        } catch (IllegalArgumentException e) {
            return String.format(this.messages.getString("plugin.errors.configuration"), e.getMessage());
        } catch (Exception e) {
            return String.format(this.messages.getString("plugin.errors.unexpected"),
                    e.getClass().getSimpleName(),
                    e.getMessage() != null ? e.getMessage() :
                            this.messages.getString("plugin.errors.no.details"));
        }
    }

    /**
     * Builds a detailed error message based on Python script error outputs, traceback locations,
     * and exception details, along with the script execution exit code.
     *
     * @param primaryErrorText the primary error message text from the Python script, typically the standard error output
     * @param fallbackOutputText the fallback output text, such as the script standard output, in case the primary error text is absent
     * @param scriptPath the path to the Python script, used to prioritize traceback location extraction
     * @param exitCode the exit code from the Python script execution
     * @return a formatted string that combines traceback location, exception information, and the script exit code
     */
    private String buildDetailedPythonError(String primaryErrorText,
                                            String fallbackOutputText,
                                            String scriptPath,
                                            int exitCode) {
        String sourceText = (primaryErrorText != null && !primaryErrorText.isEmpty())
                ? primaryErrorText
                : (fallbackOutputText != null ? fallbackOutputText : "");

        // Extract first traceback location (file, line, function)
        String location = extractFirstTracebackLocation(sourceText, scriptPath);

        // Extract the last exception line (e.g., ValueError: message)
        String exceptionLine = null;
        String[] lines = sourceText.split("\\R");
        for (int i = lines.length - 1; i >= 0; i--) {
            String l = lines[i].trim();
            if (l.matches("^[A-Z][A-Za-z0-9_.]*(?:Error|Exception):.*")) {
                exceptionLine = l;
                break;
            }
        }

        StringBuilder sb = new StringBuilder();
        if (location != null) {
            sb.append(location);
        }
        if (exceptionLine != null) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(exceptionLine);
        }

        // Fallback if we couldn't parse anything meaningful
        if (sb.length() == 0) {
            // Limit to avoid flooding UI
            String truncated = sourceText.length() > 4000 ? sourceText.substring(0, 4000) + "..." : sourceText;
            sb.append(truncated);
        }

        // Include exit code for context
        sb.append("\n(exit code: ").append(exitCode).append(")");
        return sb.toString();
    }


    /**
     * Extracts the first traceback location from a Python script error text. Traceback locations
     * are identified using a specific regex pattern. If a preferred script path is provided, the method
     * prioritizes matching traceback locations associated with that script.
     *
     * @param text the error text from which traceback locations need to be extracted; can contain
     *             multiple traceback entries
     * @param preferredScriptPath the path of the preferred Python script to prioritize in traceback
     *                             extraction; can be null or empty if no preference is required
     * @return a formatted string representing the first extracted traceback location, or null if no
     *         traceback location is found in the error text
     */
    private String extractFirstTracebackLocation(String text, String preferredScriptPath) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        String bestMatch = null;
        java.util.regex.Matcher m = PY_TRACE_FILE_LINE.matcher(text);
        while (m.find()) {
            String file = m.group(1);
            String line = m.group(2);
            String func = m.groupCount() >= 3 ? m.group(3) : null;

            // Prefer frames from the executed script if present
            boolean isPreferred = (preferredScriptPath != null && !preferredScriptPath.isEmpty())
                    && file.replace('\\', '/').endsWith(new File(preferredScriptPath).getName());

            String formatted = "File: " + file + ", line " + line + (func != null ? ", in " + func : "");
            if (isPreferred) {
                return formatted;
            }
            if (bestMatch == null) {
                bestMatch = formatted;
            }
        }
        return bestMatch;
    }



    /**
     * Creates the parameters JSON file in GeoJSON format as per issue #346.
     * The file is a GeoJSON with the perimeter as a Feature and all other parameters as properties.
     *
     * @param request the request containing the parameters
     * @param outputFile the file to write the GeoJSON to
     * @throws IOException if there's an error writing the file
     */
    private void createParametersFile(ITaskProcessorRequest request, File outputFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Create single GeoJSON Feature (not FeatureCollection)
        ObjectNode root = mapper.createObjectNode();
        root.put("type", "Feature");

        // Convert WKT to GeoJSON geometry if needed
        String perimeter = request.getPerimeter();
        if (perimeter != null && !perimeter.isEmpty()) {
            try {
                // Check if it's WKT format
                if (perimeter.trim().matches("^(MULTI)?(POLYGON|POINT|LINESTRING).*")) {
                    // Convert WKT to GeoJSON
                    ObjectNode geometryNode = convertWKTToGeoJSON(perimeter, mapper);
                    root.set("geometry", geometryNode);
                    this.logger.debug("Converted WKT to GeoJSON geometry");
                } else {
                    // Try to parse as already GeoJSON
                    ObjectNode geometryNode = (ObjectNode) mapper.readTree(perimeter);
                    root.set("geometry", geometryNode);
                    this.logger.debug("Using existing GeoJSON geometry");
                }
            } catch (Exception e) {
                this.logger.error("Error processing geometry: {}", e.getMessage());
                // Create null geometry if conversion fails
                root.putNull("geometry");
            }
        } else {
            root.putNull("geometry");
        }

        // Add all parameters as properties of the Feature
        ObjectNode properties = mapper.createObjectNode();

        // Basic info
        properties.put("Request", request.getId());
        properties.put("FolderOut", request.getFolderOut());
        properties.put("FolderIn", request.getFolderIn());
        properties.put("OrderGuid", request.getOrderGuid());
        properties.put("OrderLabel", request.getOrderLabel());
        properties.put("ClientGuid", request.getClientGuid());
        properties.put("ClientName", request.getClient());
        properties.put("OrganismGuid", request.getOrganismGuid());
        properties.put("OrganismName", request.getOrganism());
        properties.put("ProductGuid", request.getProductGuid());
        properties.put("ProductLabel", request.getProductLabel());

        // Add custom parameters as a nested object
        String parametersJson = request.getParameters();
        if (parametersJson != null && !parametersJson.isEmpty()) {
            try {
                ObjectNode parametersNode = (ObjectNode) mapper.readTree(parametersJson);
                // Add parameters as a nested object in properties
                properties.set("Parameters", parametersNode);
            } catch (Exception e) {
                this.logger.warn("Could not parse custom parameters as JSON: {}", e.getMessage());
                // If not valid JSON, add as string
                properties.put("Parameters", parametersJson);
            }
        } else {
            // Add empty parameters object if no parameters
            properties.set("Parameters", mapper.createObjectNode());
        }

        root.set("properties", properties);

        // Write GeoJSON to file
        mapper.writeValue(outputFile, root);
        this.logger.debug("GeoJSON parameters file created: {}", outputFile.getAbsolutePath());
    }

    /**
     * Converts WKT geometry string to GeoJSON geometry object.
     *
     * @param wkt the WKT string
     * @param mapper the Jackson ObjectMapper
     * @return GeoJSON geometry as ObjectNode
     */
    private ObjectNode convertWKTToGeoJSON(String wkt, ObjectMapper mapper) throws Exception {
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

        } else if (geometry instanceof MultiPolygon multiPolygon) {
            geoJsonGeometry.put("type", "MultiPolygon");
            ArrayNode multiCoordinates = mapper.createArrayNode();
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
     * Handles exterior ring and holes (interior rings).
     *
     * @param polygon the JTS Polygon
     * @param mapper the Jackson ObjectMapper
     * @return coordinates as ArrayNode
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

        // Add interior rings (holes)
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
}