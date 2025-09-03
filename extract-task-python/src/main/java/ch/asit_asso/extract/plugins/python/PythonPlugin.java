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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A plugin that executes Python scripts with parameters passed via JSON file
 *
 * @author Extract Team
 */
public class PythonPlugin implements ITaskProcessor {

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
    private PluginConfiguration config;

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
        pythonInterpreterParam.put("label", this.messages.getString("param.pythonInterpreter.label"));
        pythonInterpreterParam.put("type", "text");
        pythonInterpreterParam.put("req", true);
        pythonInterpreterParam.put("maxlength", 255);
        pythonInterpreterParam.put("help", this.messages.getString("param.pythonInterpreter.help"));
        parametersNode.add(pythonInterpreterParam);

        // Python script path parameter
        ObjectNode pythonScriptParam = mapper.createObjectNode();
        pythonScriptParam.put("code", "pythonScript");
        pythonScriptParam.put("label", this.messages.getString("param.pythonScript.label"));
        pythonScriptParam.put("type", "text");
        pythonScriptParam.put("req", true);
        pythonScriptParam.put("maxlength", 500);
        pythonScriptParam.put("help", this.messages.getString("param.pythonScript.help"));
        parametersNode.add(pythonScriptParam);

        // Additional arguments parameter (optional)
        ObjectNode additionalArgsParam = mapper.createObjectNode();
        additionalArgsParam.put("code", "additionalArgs");
        additionalArgsParam.put("label", this.messages.getString("param.additionalArgs.label"));
        additionalArgsParam.put("type", "text");
        additionalArgsParam.put("req", false);
        additionalArgsParam.put("maxlength", 500);
        additionalArgsParam.put("help", this.messages.getString("param.additionalArgs.help"));
        parametersNode.add(additionalArgsParam);

        try {
            return mapper.writeValueAsString(parametersNode);
        } catch (JsonProcessingException e) {
            logger.error("Could not create parameters JSON", e);
            return "[]";
        }
    }

    /**
     * Executes the Python script with the request parameters.
     *
     * @param request       the request to process
     * @param emailSettings the email settings
     * @return the result of the task execution
     */
    @Override
    public ITaskProcessorResult execute(ITaskProcessorRequest request, IEmailSettings emailSettings) {
        this.logger.debug("Starting Python plugin execution");
        
        PythonResult result = new PythonResult();
        result.setRequestData(request);
        
        try {
            // Check if inputs are initialized
            if (this.inputs == null || this.inputs.isEmpty()) {
                String errorMessage = "Configuration error: No input parameters provided";
                this.logger.error(errorMessage);
                result.setSuccess(false);
                result.setMessage(errorMessage);  // Use setMessage for consistency
                return result;
            }
            
            // Validate required parameters
            String pythonInterpreter = this.inputs.get("pythonInterpreter");
            String pythonScript = this.inputs.get("pythonScript");
            
            if (pythonInterpreter == null || pythonInterpreter.trim().isEmpty()) {
                String errorMessage = this.messages.getString("error.pythonInterpreter.missing");
                this.logger.error(errorMessage);
                result.setSuccess(false);
                result.setMessage(errorMessage);  // Use setMessage for consistency
                return result;
            }
            
            if (pythonScript == null || pythonScript.trim().isEmpty()) {
                String errorMessage = this.messages.getString("error.pythonScript.missing");
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
                String errorMessage = String.format("L'interpréteur Python n'existe pas: %s", pythonInterpreter);
                this.logger.error(errorMessage);
                result.setSuccess(false);
                result.setMessage(errorMessage);
                return result;
            }
            
            if (!pythonInterpreterFile.canExecute()) {
                String errorMessage = String.format("L'interpréteur Python n'est pas exécutable: %s", pythonInterpreter);
                this.logger.error(errorMessage);
                result.setSuccess(false);
                result.setMessage(errorMessage);
                return result;
            }
            
            // Check if Python script exists and is readable
            File pythonScriptFile = new File(pythonScript);
            if (!pythonScriptFile.exists()) {
                String errorMessage = String.format("Le script Python n'existe pas: %s", pythonScript);
                this.logger.error(errorMessage);
                result.setSuccess(false);
                result.setMessage(errorMessage);
                return result;
            }
            
            if (!pythonScriptFile.canRead()) {
                String errorMessage = String.format("Le script Python n'est pas lisible: %s", pythonScript);
                this.logger.error(errorMessage);
                result.setSuccess(false);
                result.setMessage(errorMessage);
                return result;
            }
            
            // Validate and create output directory
            String folderOut = request.getFolderOut();
            if (folderOut == null || folderOut.trim().isEmpty()) {
                String errorMessage = "Erreur: Le dossier de sortie n'est pas défini";
                this.logger.error(errorMessage);
                result.setSuccess(false);
                result.setMessage(errorMessage);
                return result;
            }
            
            File outputDir = new File(folderOut);
            if (!outputDir.exists()) {
                if (!outputDir.mkdirs()) {
                    String errorMessage = String.format("Impossible de créer le dossier de sortie: %s", folderOut);
                    this.logger.error(errorMessage);
                    result.setSuccess(false);
                    result.setMessage(errorMessage);
                    return result;
                }
            }
            
            if (!outputDir.canWrite()) {
                String errorMessage = String.format("Impossible d'écrire dans le dossier de sortie: %s", folderOut);
                this.logger.error(errorMessage);
                result.setSuccess(false);
                result.setMessage(errorMessage);
                return result;
            }
            
            // Create parameters JSON file
            File parametersFile = new File(outputDir, "parameters.json");
            try {
                createParametersFile(request, parametersFile);
                this.logger.info("Parameters file created successfully: {}", parametersFile.getAbsolutePath());
            } catch (IOException e) {
                String errorMessage = String.format("Erreur lors de la création du fichier de paramètres: %s", e.getMessage());
                this.logger.error(errorMessage, e);
                result.setSuccess(false);
                result.setMessage(errorMessage);
                return result;
            } catch (Exception e) {
                String errorMessage = String.format("Erreur inattendue lors de la création du fichier de paramètres: %s", e.getMessage());
                this.logger.error(errorMessage, e);
                result.setSuccess(false);
                result.setMessage(errorMessage);
                return result;
            }
            
            // Verify parameters file was created
            if (!parametersFile.exists() || !parametersFile.canRead()) {
                String errorMessage = "Le fichier de paramètres n'a pas pu être créé ou n'est pas lisible";
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
                result.setMessage(this.messages.getString("execute.success"));
                result.setResultFilePath(folderOut);
            } else {
                // Error occurred during execution - put error in message like FMEDesktop
                this.logger.error("Python script execution failed: {}", errorMessage);
                result.setSuccess(false);
                result.setMessage(errorMessage);  // Use setMessage instead of setErrorMessage
            }
            
        } catch (SecurityException e) {
            String errorMessage = String.format("Erreur de sécurité: %s", e.getMessage());
            this.logger.error(errorMessage, e);
            result.setSuccess(false);
            result.setMessage(errorMessage);  // Use setMessage instead of setErrorMessage
        } catch (Exception e) {
            String errorMessage = String.format("Erreur inattendue: %s", e.getClass().getSimpleName());
            if (e.getMessage() != null) {
                errorMessage += " - " + e.getMessage();
            }
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
            
            // Add additional arguments if provided
            String additionalArgs = this.inputs.get("additionalArgs");
            if (additionalArgs != null && !additionalArgs.trim().isEmpty()) {
                String[] args = additionalArgs.trim().split("\\s+");
                for (String arg : args) {
                    if (!arg.isEmpty()) {
                        command.add(arg);
                    }
                }
            }
            
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            
            // Set working directory
            File workingDir = new File(request.getFolderOut());
            if (!workingDir.exists() || !workingDir.isDirectory()) {
                return String.format("Erreur: Le répertoire de travail n'existe pas ou n'est pas un dossier: %s", 
                                   request.getFolderOut());
            }
            processBuilder.directory(workingDir);
            
            // Capture both stdout and stderr
            processBuilder.redirectErrorStream(true);
            
            this.logger.info("Executing command: {} in directory: {}", 
                           String.join(" ", command), workingDir);
            
            Process process;
            try {
                process = processBuilder.start();
            } catch (IOException e) {
                // Handle specific error cases
                String errorDetail = e.getMessage();
                if (errorDetail != null) {
                    if (errorDetail.contains("No such file or directory") || 
                        errorDetail.contains("Le fichier spécifié est introuvable")) {
                        // Python interpreter not found
                        return String.format("ERREUR: L'interpréteur Python '%s' n'a pas été trouvé.\n" +
                                           "Vérifiez que Python est installé et que le chemin est correct.", 
                                           pythonExecutable);
                    } else if (errorDetail.contains("Permission denied") || 
                               errorDetail.contains("Accès refusé")) {
                        return String.format("ERREUR: Permission refusée pour exécuter '%s'.\n" +
                                           "Vérifiez les permissions du fichier.", 
                                           pythonExecutable);
                    }
                }
                return String.format("ERREUR lors du lancement du script: %s", errorDetail);
            }
            
            // Capture output with timeout handling
            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();
            boolean hasError = false;
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    
                    // Detect Python-specific errors
                    String lowerLine = line.toLowerCase();
                    if (line.contains("Traceback") || line.contains("Error:") || 
                        line.contains("SyntaxError") || line.contains("IndentationError") ||
                        line.contains("TabError") || line.contains("NameError") ||
                        line.contains("ImportError") || line.contains("ModuleNotFoundError") ||
                        line.contains("FileNotFoundError") || line.contains("PermissionError")) {
                        hasError = true;
                        errorOutput.append(line).append("\n");
                        this.logger.error("Python Error: {}", line);
                    } else {
                        this.logger.info("Python: {}", line);
                    }
                }
            } catch (IOException e) {
                return String.format("ERREUR lors de la lecture de la sortie du script: %s", e.getMessage());
            }
            
            // Wait for completion with timeout (5 minutes)
            boolean completed;
            try {
                completed = process.waitFor(300, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                process.destroyForcibly();
                return "ERREUR: L'exécution du script a été interrompue";
            }
            
            if (!completed) {
                process.destroyForcibly();
                return "ERREUR: Timeout - Le script Python n'a pas terminé après 5 minutes.\n" +
                       "Le script prend trop de temps ou est peut-être bloqué.";
            }
            
            int exitCode = process.exitValue();
            this.logger.info("Python script finished with exit code: {}", exitCode);
            
            // Check exit code and format appropriate error message
            if (exitCode != 0) {
                String scriptOutput = output.toString().trim();
                String errorMsg;
                
                // Special handling for common Python exit codes
                switch (exitCode) {
                    case 1:
                        // General error - check if we have Python error details
                        if (hasError && !errorOutput.toString().isEmpty()) {
                            errorMsg = String.format("ERREUR Python détectée:\n%s", errorOutput.toString());
                        } else if (!scriptOutput.isEmpty()) {
                            // Check for specific Python errors in output
                            if (scriptOutput.contains("SyntaxError")) {
                                errorMsg = String.format("ERREUR de syntaxe dans le script Python:\n%s", scriptOutput);
                            } else if (scriptOutput.contains("IndentationError") || scriptOutput.contains("TabError")) {
                                errorMsg = String.format("ERREUR d'indentation dans le script Python:\n%s", scriptOutput);
                            } else if (scriptOutput.contains("ImportError") || scriptOutput.contains("ModuleNotFoundError")) {
                                errorMsg = String.format("ERREUR d'import - Module Python manquant:\n%s", scriptOutput);
                            } else if (scriptOutput.contains("FileNotFoundError")) {
                                errorMsg = String.format("ERREUR - Fichier introuvable dans le script:\n%s", scriptOutput);
                            } else if (scriptOutput.contains("PermissionError")) {
                                errorMsg = String.format("ERREUR de permissions dans le script:\n%s", scriptOutput);
                            } else if (scriptOutput.contains("NameError")) {
                                errorMsg = String.format("ERREUR - Variable ou fonction non définie:\n%s", scriptOutput);
                            } else {
                                errorMsg = String.format("Le script Python a échoué (code %d):\n%s", exitCode, scriptOutput);
                            }
                        } else {
                            errorMsg = String.format("Le script Python a échoué avec le code %d (aucun détail disponible)", exitCode);
                        }
                        break;
                    case 2:
                        errorMsg = String.format("ERREUR: Mauvaise utilisation du script (code 2).\n" +
                                               "Vérifiez les paramètres du script.\n%s", 
                                               scriptOutput.isEmpty() ? "" : "Détails:\n" + scriptOutput);
                        break;
                    case 126:
                        errorMsg = String.format("ERREUR: Le script '%s' n'est pas exécutable.\n" +
                                               "Vérifiez les permissions du fichier (chmod +x).", scriptPath);
                        break;
                    case 127:
                        errorMsg = String.format("ERREUR: Commande introuvable.\n" +
                                               "L'interpréteur Python '%s' n'a pas été trouvé dans le PATH.", 
                                               pythonExecutable);
                        break;
                    case -1:
                    case 255:
                        errorMsg = "ERREUR: Le script a été terminé de manière anormale";
                        if (!scriptOutput.isEmpty()) {
                            errorMsg += "\nDétails:\n" + scriptOutput;
                        }
                        break;
                    default:
                        if (!scriptOutput.isEmpty()) {
                            errorMsg = String.format("Le script Python a retourné le code d'erreur %d:\n%s", 
                                                   exitCode, scriptOutput);
                        } else {
                            errorMsg = String.format("Le script Python a retourné le code d'erreur %d", exitCode);
                        }
                }
                
                this.logger.error(errorMsg);
                return errorMsg;
            }
            
            this.logger.info("Python script executed successfully");
            return null; // Success
            
        } catch (SecurityException e) {
            String errorMsg = String.format("ERREUR de sécurité: %s\nVérifiez les permissions.", e.getMessage());
            this.logger.error(errorMsg, e);
            return errorMsg;
        } catch (IllegalArgumentException e) {
            String errorMsg = String.format("ERREUR de configuration: %s", e.getMessage());
            this.logger.error(errorMsg, e);
            return errorMsg;
        } catch (Exception e) {
            String errorMsg = String.format("ERREUR inattendue (%s): %s", 
                                          e.getClass().getSimpleName(), 
                                          e.getMessage() != null ? e.getMessage() : "Pas de détails");
            this.logger.error(errorMsg, e);
            return errorMsg;
        }
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
        properties.put("RequestId", request.getId());
        properties.put("FolderOut", request.getFolderOut());
        properties.put("FolderIn", request.getFolderIn());
        
        // Order info
        properties.put("OrderGuid", request.getOrderGuid());
        properties.put("OrderLabel", request.getOrderLabel());
        
        // Client info (renamed as per spec)
        properties.put("ClientGuid", request.getClientGuid());
        properties.put("ClientName", request.getClient());
        
        // Organism info (renamed as per spec)
        properties.put("OrganismGuid", request.getOrganismGuid());
        properties.put("OrganismName", request.getOrganism());
        
        // Product info (renamed as per spec)
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