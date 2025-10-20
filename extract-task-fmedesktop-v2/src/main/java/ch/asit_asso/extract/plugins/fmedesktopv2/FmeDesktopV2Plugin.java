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
package ch.asit_asso.extract.plugins.fmedesktopv2;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ch.asit_asso.extract.plugins.common.IEmailSettings;
import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A plugin that executes an FME Desktop task with parameters passed via JSON file.
 * This version overcomes command line length limitations by using a GeoJSON file.
 *
 * @author Extract Team
 */
public class FmeDesktopV2Plugin implements ITaskProcessor {

    /**
     * The path to the file that holds the general settings of the plugin.
     */
    private static final String CONFIG_FILE_PATH = "plugins/fmedesktopv2/properties/config.properties";

    /**
     * The name of the file that holds the text explaining how to use this plugin in the language of
     * the user interface.
     */
    private static final String HELP_FILE_NAME = "fmeDesktopV2Help.html";

    /**
     * Object that ensures that the test of available FME Desktop instances and the (possible) start of the extraction
     * process are atomic.
     */
    private static final Lock LOCK = new ReentrantLock(true);
    private static final long PROCESS_TIMEOUT_SECONDS = 10;
    private static final long PROCESS_TIMEOUT_HOURS = 72;  // 3 days timeout for FME processes

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(FmeDesktopV2Plugin.class);

    /**
     * The string that identifies this plugin.
     */
    private final String code = "FME2017V2";

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
     * Creates a new FME Desktop V2 plugin instance with default settings and using the default language.
     */
    public FmeDesktopV2Plugin() {
        this.messages = new LocalizedMessages();
        this.config = new PluginConfiguration(FmeDesktopV2Plugin.CONFIG_FILE_PATH);
        this.inputs = null;
    }

    /**
     * Creates a new FME Desktop V2 plugin instance using the default language.
     *
     * @param taskSettings a map with the settings for the execution of this task
     */
    public FmeDesktopV2Plugin(Map<String, String> taskSettings) {
        this.messages = new LocalizedMessages();
        this.config = new PluginConfiguration(FmeDesktopV2Plugin.CONFIG_FILE_PATH);
        this.inputs = taskSettings;
    }

    /**
     * Creates a new FME Desktop V2 plugin instance with default settings.
     *
     * @param lang the string that identifies the language of the user interface
     */
    public FmeDesktopV2Plugin(String lang) {
        this(lang, null);
    }

    /**
     * Creates a new FME Desktop V2 plugin instance.
     *
     * @param lang    the string that identifies the language of the user interface
     * @param taskSettings a map with the settings for the execution of this task
     */
    public FmeDesktopV2Plugin(String lang, Map<String, String> taskSettings) {
        if (lang == null) {
            this.messages = new LocalizedMessages();
        } else {
            this.messages = new LocalizedMessages(lang);
        }
        this.config = new PluginConfiguration(FmeDesktopV2Plugin.CONFIG_FILE_PATH);
        this.inputs = taskSettings;
    }

    @Override
    public ITaskProcessor newInstance(String language) {
        return new FmeDesktopV2Plugin(language, this.inputs);
    }

    @Override
    public ITaskProcessor newInstance(String language, Map<String, String> inputs) {
        return new FmeDesktopV2Plugin(language, inputs);
    }

    @Override
    public ITaskProcessorResult execute(ITaskProcessorRequest request, IEmailSettings emailSettings) {

        this.logger.debug("Starting FME Desktop V2 execution.");
        FmeDesktopV2Result result = new FmeDesktopV2Result();
        FmeDesktopV2Result.Status resultStatus = FmeDesktopV2Result.Status.ERROR;
        String resultMessage = "";
        String resultErrorCode = "-1";

        try {
            if (this.inputs == null || this.inputs.isEmpty()) {
                result.setStatus(FmeDesktopV2Result.Status.ERROR);
                result.setErrorCode("-1");
                result.setMessage(this.messages.getString("errorParameters.noParam"));
                result.setRequestData(request);
                return result;
            }

            String workspaceParam = StringUtils.trimToNull(this.inputs.get("workbench"));
            String applicationParam = StringUtils.trimToNull(this.inputs.get("application"));

            if (workspaceParam == null) {
                result.setStatus(FmeDesktopV2Result.Status.ERROR);
                result.setErrorCode("-1");
                result.setMessage(this.messages.getString("errorWorkspace.notDefined"));
                result.setRequestData(request);
                return result;
            }

            if (applicationParam == null) {
                result.setStatus(FmeDesktopV2Result.Status.ERROR);
                result.setErrorCode("-1");
                result.setMessage(this.messages.getString("errorApplication.notDefined"));
                result.setRequestData(request);
                return result;
            }

            File workspaceFile = new File(workspaceParam);
            if (!workspaceFile.exists() || !workspaceFile.isFile()) {
                result.setStatus(FmeDesktopV2Result.Status.ERROR);
                result.setErrorCode("-1");
                result.setMessage(this.messages.getString("errorWorkspace.notFile"));
                result.setRequestData(request);
                return result;
            }

            File applicationFile = new File(applicationParam);
            if (!applicationFile.exists() || !applicationFile.isFile()) {
                result.setStatus(FmeDesktopV2Result.Status.ERROR);
                result.setErrorCode("-1");
                result.setMessage(this.messages.getString("errorApplication.notFile"));
                result.setRequestData(request);
                return result;
            }

            // Create the parameters JSON file
            File parametersFile = new File(request.getFolderIn(), "parameters.json");
            try {
                createParametersFile(request, parametersFile);
                this.logger.info("Created parameters file: {}", parametersFile.getAbsolutePath());
            } catch (IOException e) {
                result.setStatus(FmeDesktopV2Result.Status.ERROR);
                result.setErrorCode("-1");
                result.setMessage(String.format("Failed to create parameters file: %s", e.getMessage()));
                result.setRequestData(request);
                return result;
            }

            // Launch FME process with instance management
            final Process fmeTaskProcess = this.launchFmeTaskProcess(request, workspaceParam,
                    applicationParam, parametersFile);

            if (fmeTaskProcess == null) {
                this.logger.warn("There wasn't enough licences to run the FME extraction. Task execution will be retried later.");
                result.setStatus(FmeDesktopV2Result.Status.NOT_RUN);
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
                    resultStatus = FmeDesktopV2Result.Status.SUCCESS;
                    resultErrorCode = "";
                    resultMessage = this.messages.getString("extract.success");
                    result.setResultFilePath(request.getFolderOut());

                } else {
                    this.logger.debug("Result folder is empty or not exists");
                    resultMessage = this.messages.getString("errorFolderOut.empty");
                }
            }

            this.logger.debug("End of FME extraction");

        } catch (Exception exception) {
            final String exceptionMessage = exception.getMessage();
            this.logger.error("The FME workspace has failed", exception);
            resultMessage = String.format(this.messages.getString("errorFme.executionFailed"), exceptionMessage);
        }

        result.setStatus(resultStatus);
        result.setErrorCode(resultErrorCode);
        result.setMessage(resultMessage);
        result.setRequestData(request);

        return result;
    }

    /**
     * Reads the content from an input stream.
     *
     * @param inputStream the input stream to read
     * @return the content as a string
     * @throws IOException if an error occurs while reading
     */
    private String readInputStream(java.io.InputStream inputStream) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;
        List<String> messageLines = new ArrayList<>();

        while ((line = reader.readLine()) != null) {
            messageLines.add(line);
        }

        return StringUtils.join(messageLines, System.lineSeparator());
    }

    /**
     * Launches the FME task process with instance management.
     *
     * @param request           the request to process
     * @param workspacePath     the path to the FME workspace file
     * @param applicationPath   the path to the FME application executable
     * @param parametersFile    the JSON parameters file
     * @return the Process object, or null if not enough instances available
     * @throws IOException if an error occurs while launching the process
     */
    private Process launchFmeTaskProcess(final ITaskProcessorRequest request, final String workspacePath,
                                          final String applicationPath, final File parametersFile) throws IOException {

        try {
            FmeDesktopV2Plugin.LOCK.lock();
            this.logger.debug("Checking license availability…");

            if (!this.hasEnoughInstances()) {
                return null;
            }

            this.logger.debug("Start FME extraction");
            final Process fmeTaskProcess;
            final File dirWorkspace = new File(FilenameUtils.getFullPathNoEndSeparator(workspacePath));
            this.logger.debug("Current working directory is {}", dirWorkspace);
            this.logger.debug("Current user is {}", System.getProperty("user.name"));

            List<String> command = new ArrayList<>();
            command.add(applicationPath);
            command.add(workspacePath);
            command.add("--parametersFile");
            command.add(parametersFile.getAbsolutePath());

            this.logger.debug("Executed command line is : {}", StringUtils.join(command, " "));

            ProcessBuilder processBuilder = new ProcessBuilder(command);
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
            FmeDesktopV2Plugin.LOCK.unlock();
        }
    }

    /**
     * Creates the parameters JSON file in GeoJSON format.
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
        
        // Convert WKT to GeoJSON geometry if available
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
        
        // Client info
        properties.put("ClientGuid", request.getClientGuid());
        properties.put("ClientName", request.getClient());
        
        // Organism info
        properties.put("OrganismGuid", request.getOrganismGuid());
        properties.put("OrganismName", request.getOrganism());
        
        // Product info
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
            this.help = this.messages.getFileContent(FmeDesktopV2Plugin.HELP_FILE_NAME);
        }

        return this.help;
    }

    @Override
    public String getPictoClass() {
        return this.pictoClass;
    }

    /**
     * Gets the maximum number of FME instances allowed according to configuration.
     *
     * @return the maximum number of FME instances
     */
    private Integer getMaxFmeInstances() {
        return NumberUtils.toInt(this.config.getProperty("maxFmeInstances"), 8);
    }

    /**
     * Checks if there are enough FME instances available to run the task.
     *
     * @return true if enough instances are available, false otherwise
     */
    private boolean hasEnoughInstances() {
        int requiredInstances = NumberUtils.toInt(this.inputs.get("nbInstances"), 1);
        int currentInstances = this.getCurrentFmeInstances();
        int maximumInstances = this.getMaxFmeInstances();

        this.logger.debug("Task requires {} instances, {} instances are already running from a maximum of {}",
                requiredInstances, currentInstances, maximumInstances);
        return (maximumInstances - currentInstances) >= requiredInstances;
    }

    /**
     * Gets the validated path to the tasklist.exe command on Windows.
     *
     * @return the path to tasklist.exe
     * @throws SecurityException if the tasklist.exe file is not found or not executable
     */
    private String getValidatedTaskListPath() {
        String windowsDir = System.getenv("windir");

        if (windowsDir == null) {
            logger.warn("The 'windir' environment variable is not set. Falling back to C:\\Windows.");
            windowsDir = "C:\\Windows";
        }

        File taskListFile = new File(windowsDir + "\\System32\\tasklist.exe");

        if (!taskListFile.exists() || !taskListFile.canExecute()) {
            logger.error("The tasklist.exe file does not exist or is not executable.");
            throw new SecurityException("The tasklist.exe file does not exist or is not executable.");
        }

        return taskListFile.getAbsolutePath();
    }

    /**
     * Gets the current number of FME instances running on the system.
     *
     * @return the number of running FME instances
     */
    private int getCurrentFmeInstances() {
        ProcessBuilder processBuilder;
        Process process;
        BufferedReader input = null;

        try {
            this.logger.debug("Current process user is {}.", System.getProperty("user.name"));

            if (SystemUtils.IS_OS_WINDOWS) {
                String command = getValidatedTaskListPath() + " /fo csv /nh /FI \"IMAGENAME eq fme.exe\"";
                processBuilder = new ProcessBuilder("cmd.exe", "/c", command);

            } else if (SystemUtils.IS_OS_LINUX) {
                String command ="pgrep -l ^fme$";
                processBuilder = new ProcessBuilder("bash", "-c", command);

            } else {
                this.logger.error("This operating system is not supported by Extract.");
                throw new UnsupportedOperationException("Unsupported operating system.");
            }

            processBuilder.directory(null);
            process = processBuilder.start();

            if (!process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                logger.error("Process took too long to execute and was terminated");
                process.destroy();
                throw new RuntimeException("Process execution timed out.");
            }

            input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String processItem;
            int instances = 0;

            this.logger.debug("Fetching current FME processes:");
            while ((processItem = input.readLine()) != null) {
                this.logger.debug(processItem);

                if (processItem.isEmpty() || processItem.startsWith("INFO:")) {
                    continue;
                }

                instances++;
            }
            input.close();

            return instances;
        } catch (IOException ioException) {
            this.logger.error("Unable to get the running FME processes.", ioException);
            throw new RuntimeException("Could not get FME instances.", ioException);
        } catch (InterruptedException interruptedException) {
            this.logger.error("Process was interrupted.", interruptedException);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Process was interrupted.", interruptedException);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ioException) {
                    this.logger.warn("Unable to close the input stream.", ioException);
                }
            }
        }
    }

    @Override
    public String getParams() {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode parametersNode = mapper.createArrayNode();

        // Workspace parameter
        ObjectNode workspaceParam = mapper.createObjectNode();
        workspaceParam.put("code", "workbench");
        workspaceParam.put("label", this.messages.getString("param.workbench.label"));
        workspaceParam.put("type", "text");
        workspaceParam.put("maxlength", 500);
        workspaceParam.put("req", true);
        workspaceParam.put("help", this.messages.getString("param.workbench.help"));
        parametersNode.add(workspaceParam);

        // FME Application parameter
        ObjectNode applicationParam = mapper.createObjectNode();
        applicationParam.put("code", "application");
        applicationParam.put("label", this.messages.getString("param.application.label"));
        applicationParam.put("type", "text");
        applicationParam.put("maxlength", 500);
        applicationParam.put("req", true);
        applicationParam.put("help", this.messages.getString("param.application.help"));
        parametersNode.add(applicationParam);

        // Number of instances parameter
        ObjectNode instancesParam = mapper.createObjectNode();
        instancesParam.put("code", "nbInstances");
        instancesParam.put("label", this.messages.getString("param.nbInstances.label")
                .replace("{maxInstances}", this.getMaxFmeInstances().toString()));
        instancesParam.put("type", "numeric");
        instancesParam.put("min", 1);
        instancesParam.put("max", this.getMaxFmeInstances());
        instancesParam.put("req", true);
        instancesParam.put("step", 1);
        instancesParam.put("help", this.messages.getString("param.nbInstances.help"));
        parametersNode.add(instancesParam);

        try {
            return mapper.writeValueAsString(parametersNode);
        } catch (JsonProcessingException e) {
            logger.error("Could not create parameters JSON", e);
            return "[]";
        }
    }
}