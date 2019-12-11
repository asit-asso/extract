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
package org.easysdi.extract.plugins.remark;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import org.easysdi.extract.plugins.common.IEmailSettings;
import org.easysdi.extract.plugins.common.ITaskProcessor;
import org.easysdi.extract.plugins.common.ITaskProcessorRequest;
import org.easysdi.extract.plugins.common.ITaskProcessorResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * A plugin that adds an automated remark to a request.
 *
 * @author Florent Krin
 */
public class RemarkPlugin implements ITaskProcessor {

    /**
     * The path to the file that holds the general settings for this plugin.
     */
    private static final String CONFIG_FILE_PATH = "plugins/remark/properties/configRemark.properties";

    /**
     * The name of the file that holds the text explaining how to use this plugin in the language of
     * the user interface.
     */
    private static final String HELP_FILE_NAME = "remarkHelp.html";

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(RemarkPlugin.class);

    /**
     * The string that identifies this plugin.
     */
    private final String code = "REMARK";

    /**
     * The text that explains how to use this plugin in the language of the user interface.
     */
    private String help = null;

    /**
     * The CSS class of the icon to display to represent this plugin.
     */
    private final String pictoClass = "fa-comment-o";

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
     * Creates a new instance of the automated remark plugin with default settings and using the default
     * language.
     */
    public RemarkPlugin() {
        this.config = new PluginConfiguration(RemarkPlugin.CONFIG_FILE_PATH);
        this.messages = new LocalizedMessages();
    }



    /**
     * Creates a new instance of the automated remark plugin with default settings.
     *
     * @param language the string that identifies the language of the user interface
     */
    public RemarkPlugin(final String language) {
        this.config = new PluginConfiguration(RemarkPlugin.CONFIG_FILE_PATH);
        this.messages = new LocalizedMessages(language);
    }



    /**
     * Creates a new instance of the automated remark plugin using the default language.
     *
     * @param taskSettings a map that contains the settings for the execution of this task
     */
    public RemarkPlugin(final Map<String, String> taskSettings) {
        this();
        this.inputs = taskSettings;
    }



    /**
     * Creates a new instance of the automated remark plugin.
     *
     * @param language     the string that identifies the language of the user interface
     * @param taskSettings a map that contains the settings for the execution of this task
     */
    public RemarkPlugin(final String language, final Map<String, String> taskSettings) {
        this(language);
        this.inputs = taskSettings;
    }



    @Override
    public final RemarkPlugin newInstance(final String language) {
        return new RemarkPlugin(language);
    }



    @Override
    public final RemarkPlugin newInstance(final String language, final Map<String, String> taskSettings) {
        return new RemarkPlugin(language, taskSettings);
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
            this.help = this.messages.getFileContent(RemarkPlugin.HELP_FILE_NAME);
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

        ObjectNode remarkNode = parametersNode.addObject();
        remarkNode.put("code", this.config.getProperty("paramRemark"));
        remarkNode.put("label", this.messages.getString("paramRemark.label"));
        remarkNode.put("type", "multitext");
        remarkNode.put("req", true);
        remarkNode.put("maxlength", 5000);

        ObjectNode overwriteNode = parametersNode.addObject();
        overwriteNode.put("code", this.config.getProperty("paramOverwrite"));
        overwriteNode.put("label", this.messages.getString("paramOverwrite.label"));
        overwriteNode.put("type", "boolean");

        try {
            return mapper.writeValueAsString(parametersNode);

        } catch (JsonProcessingException exception) {
            logger.error("An error occurred when the parameters were converted to JSON.", exception);
            return null;
        }
    }



    @Override
    public final ITaskProcessorResult execute(final ITaskProcessorRequest request, final IEmailSettings emailSettings) {

        final RemarkResult pluginResult = new RemarkResult();
        RemarkResult.Status resultStatus = RemarkResult.Status.ERROR;
        String resultMessage;
        String resultErrorCode = "-1";
        final RemarkRequest updatedRequest = new RemarkRequest(request);

        try {
            this.logger.debug("Start Remark Plugin");

            final boolean overwriteRemark = (this.inputs.get(this.config.getProperty("paramOverwrite")).equals("true"));
            final String newRemark = this.inputs.get(this.config.getProperty("paramRemark"));
            final String currentRemark = request.getRemark();

            if (overwriteRemark || currentRemark == null || "".equals(currentRemark)) {
                updatedRequest.setRemark(newRemark);
            } else {
                updatedRequest.setRemark(String.format("%s\r\n%s", currentRemark, newRemark));
            }

            resultMessage = this.messages.getString("remark.executing.success");
            resultStatus = RemarkResult.Status.SUCCESS;
            resultErrorCode = "";

        } catch (Exception e) {
            this.logger.error("The Plugin Remark has failed", e.getMessage());
            resultMessage = this.messages.getString("remark.executing.failed");
        }

        pluginResult.setStatus(resultStatus);
        pluginResult.setErrorCode(resultErrorCode);
        pluginResult.setMessage(resultMessage);
        pluginResult.setRequestData(updatedRequest);

        this.logger.debug("Plugin Remark : status is " + resultStatus.name());

        return pluginResult;
    }

}
