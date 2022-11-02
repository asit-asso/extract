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
package ch.asit_asso.extract.plugins.archive;

import ch.asit_asso.extract.plugins.common.IEmailSettings;
import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * A plugin that will copy data about a data item request for archiving.
 *
 * @author Florent Krin
 */
public class ArchivePlugin implements ITaskProcessor {

    /**
     * The relative path to the file that holds the plugin configuration.
     */
    private static final String CONFIG_FILE_PATH = "plugins/archivage/properties/configArchivage.properties";

    /**
     * The name of the file that contains the help text about how to use this plugin.
     */
    private static final String HELP_FILE_NAME = "archivageHelp.html";

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(ArchivePlugin.class);

    /**
     * The string that identifies this plugin.
     */
    private final String code = "ARCHIVE";

    /**
     * The text that explains how to use this plugin in the language used by the application.
     */
    private String help = null;

    /**
     * The class of the pictogram to use to represent this plugin.
     */
    private final String pictoClass = "fa-folder-open-o";

    /**
     * The messages for the user in the language used by the application.
     */
    private LocalizedMessages messages;

    /**
     * The parameters passed to this plugin by the application.
     */
    private Map<String, String> inputs;

    /**
     * The general settings of the archiving plugin.
     */
    private PluginConfiguration config;



    /**
     * Creates a new archiving plugin instance with default settings and using the default language.
     */
    public ArchivePlugin() {
        this.config = new PluginConfiguration(ArchivePlugin.CONFIG_FILE_PATH);
        this.messages = new LocalizedMessages();
    }



    /**
     * Creates a new archiving plugin instance with default settings.
     *
     * @param language the string that identifies the language to use for the messages to the user
     */
    public ArchivePlugin(final String language) {
        this.config = new PluginConfiguration(ArchivePlugin.CONFIG_FILE_PATH);
        this.messages = new LocalizedMessages(language);
    }



    /**
     * Creates a new archiving plugin instance using the default language.
     *
     * @param settings the archiving settings for this instance
     */
    public ArchivePlugin(final Map<String, String> settings) {
        this();
        this.inputs = settings;
    }



    /**
     * Creates a new archiving plugin instance.
     *
     * @param language the string that identifies the language to use for the messages to the user
     * @param settings the archiving settings for this instance
     */
    public ArchivePlugin(final String language, final Map<String, String> settings) {
        this(language);
        this.inputs = settings;
    }



    @Override
    public final ArchivePlugin newInstance(final String language) {
        return new ArchivePlugin(language);
    }



    @Override
    public final ArchivePlugin newInstance(final String language, final Map<String, String> settings) {
        return new ArchivePlugin(language, settings);
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
            this.help = this.messages.getFileContent(ArchivePlugin.HELP_FILE_NAME);
        }

        return this.help;
    }



    @Override
    public final String getPictoClass() {
        return pictoClass;
    }



    @Override
    public final String getParams() {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode parametersNode = mapper.createArrayNode();

        ObjectNode pathNode = parametersNode.addObject();
        pathNode.put("code", this.config.getProperty("paramPath"));
        pathNode.put("label", this.messages.getString("paramPath.label"));
        pathNode.put("type", "text");
        pathNode.put("req", true);
        pathNode.put("maxlength", 255);

        try {
            return mapper.writeValueAsString(parametersNode);

        } catch (JsonProcessingException exception) {
            logger.error("An error occurred when the parameters were converted to JSON.", exception);
            return null;
        }
    }



    /**
     * Copies the request data to the archive folder.
     *
     * @param sourceFolder  the directory that holds the request data to archive
     * @param archiveFolder the directory where the request data must be copied
     * @throws IOException a file system error prevented the copy operation from completing
     */
    private void copyToFolder(final File sourceFolder, final File archiveFolder) throws IOException {

        if (!archiveFolder.exists()) {
            FileUtils.forceMkdir(archiveFolder);
        }

        for (java.io.File srcFile : sourceFolder.listFiles()) {

            if (srcFile.isDirectory()) {
                FileUtils.copyDirectoryToDirectory(srcFile, archiveFolder);

            } else {
                FileUtils.copyFileToDirectory(srcFile, archiveFolder);
            }
        }

    }



    @Override
    public final ITaskProcessorResult execute(final ITaskProcessorRequest request, final IEmailSettings emailSettings) {

        String destPath = this.inputs.get(this.config.getProperty("paramPath"));
        destPath = this.buildPathWithPropertyValues(destPath, request);
        final File srcDir = new File(request.getFolderOut());
        final File destDir = new File(destPath);

        final ArchiveResult pluginResult = new ArchiveResult();
        ArchiveResult.Status resultStatus = ArchiveResult.Status.ERROR;
        String resultMessage;
        String resultErrorCode = "-1";

        try {

            this.logger.debug("Start Archivage Plugin");

            if (!srcDir.exists()) {
                resultMessage = this.messages.getString("archivage.path.sourcedir.notexists");

            } else {
                this.logger.debug("Total files in the folder : " + srcDir.list().length);

                this.logger.debug("Copy files from " + request.getFolderOut() + " to " + destPath);
                this.copyToFolder(srcDir, destDir);

                resultStatus = ArchiveResult.Status.SUCCESS;
                resultErrorCode = "";
                resultMessage = this.messages.getString("archivage.executing.success").replace("{archivePath}",
                        destPath);
            }

        } catch (Exception e) {
            this.logger.error("Archivage has failed", e.getMessage());
            resultMessage = this.messages.getString("archivage.executing.failed") + e.getMessage();
        }

        pluginResult.setStatus(resultStatus);
        pluginResult.setErrorCode(resultErrorCode);
        pluginResult.setMessage(resultMessage);
        pluginResult.setRequestData(request);

        this.logger.debug("Plugin Archivage : status is " + resultStatus.name());

        return pluginResult;
    }



    /**
     * Replaces request properties placeholders in the path of the archive folder with their value for the
     * current request.
     *
     * @param path    the archive folder path string with placeholders
     * @param request the request that is currently archived
     * @return the archive folder path for the current request
     */
    public final String buildPathWithPropertyValues(final String path, final ITaskProcessorRequest request) {
        final String[] authorizedFields = this.config.getProperty("path.properties.authorized").split(",");
        String formattedPath = path;

        for (String fieldName : authorizedFields) {
            String fieldValue = this.getRequestFieldValue(request, fieldName);

            if (fieldValue == null) {
                continue;
            }

            final String fieldPlaceholder = String.format("{%s}", fieldName.toUpperCase());
            int searchFieldIndex = formattedPath.toUpperCase().indexOf(fieldPlaceholder);

            while (searchFieldIndex >= 0) {
                formattedPath = formattedPath.substring(0, searchFieldIndex) + fieldValue
                        + formattedPath.substring(searchFieldIndex + fieldPlaceholder.length());
                searchFieldIndex = formattedPath.toUpperCase().indexOf(fieldPlaceholder);
            }
        }

        return formattedPath;
    }



    /**
     * Obtains the value of a property in the request that is currently being archived.
     *
     * @param request   the current request
     * @param fieldName the name of the property
     * @return the value of the property, or <code>null</code> if the value could not be obtained
     */
    private String getRequestFieldValue(final ITaskProcessorRequest request, final String fieldName) {

        try {
            final Field field = request.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            final Object fieldInstance = field.get(request);
            field.setAccessible(false);

            if (fieldInstance == null) {
                return "";
            }

            String fieldValue = fieldInstance.toString();

            if (field.getType().isAssignableFrom(Calendar.class)) {
                Calendar calendarFieldInstance = (Calendar) fieldInstance;
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

                if (calendarFieldInstance.getTime() != null) {
                    fieldValue = formatter.format(calendarFieldInstance.getTime());
                }
            }

            return this.sanitizeStringForFileSystem(fieldValue);

        } catch (NoSuchFieldException e) {
            this.logger.error("Could not be find the field \"" + fieldName + "\" in the request object.", e);

        } catch (IllegalAccessException exc) {
            this.logger.error("Could not be access to the field value for \"" + fieldName + "\".", exc);
        }

        return null;
    }



    /**
     * Replaces the characters that are not allowed in a file system string.
     *
     * @param rawString the file system string to sanitize
     * @return the safe file system string
     */
    private String sanitizeStringForFileSystem(final String rawString) {
        assert rawString != null : "The string to sanitize cannot be null";

        return StringUtils.stripAccents(rawString.replaceAll("[\\s<>*\"/\\\\\\[\\]:;|=,]", "_"));
    }

}
