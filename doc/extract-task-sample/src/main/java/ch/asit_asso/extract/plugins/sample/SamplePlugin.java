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
package ch.asit_asso.extract.plugins.sample;

import ch.asit_asso.extract.plugins.common.IEmailSettings;
import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * DESCRIBES HERE WHAT THE PLUGIN DO (E.G : A plugin that adds an automated remark to a request)
 * A sample plugin
 *
 * @author Florent Krin
 */
public class SamplePlugin implements ITaskProcessor {

    /**
     * The relative path to the file that holds the general settings for this plugin.
     * this path is placed in resources direcctory
     * CHANGE THE PLUGIN NAME "sample" IN THIS PATH (i.e sample)
     */
    private static final String CONFIG_FILE_PATH = "plugins/sample/properties/config.properties";

    /**
     * The name of the file that holds the text explaining how to use this plugin in the language of
     * the user interface.
     * This file is placed in resources/plugins/(plugin)/lang/fr/
     */
    private static final String HELP_FILE_NAME = "help.html";

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(SamplePlugin.class);

    /**
     * CHANGE THE CODE THAT IDENTIFIES THIS PLUGIN
     * The string that identifies this plugin.
     */
    private final String code = "Sample";

    /**
     * The text that explains how to use this plugin in the language of the user interface.
     * DO NOT CHANGE THIS VALUE, IT IS CHANGED BY getHelp()
     */
    private String help = null;

    /**
     * The CSS class of the icon to display to represent this plugin.
     * SEARCH AN ICON CLASS in https://fontawesome.com/v4/icons
     */
    private final String pictoClass = "fa-house";

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
     * No changes needed in this method
     */
    public SamplePlugin() {
        this.config = new PluginConfiguration(SamplePlugin.CONFIG_FILE_PATH);
        this.messages = new LocalizedMessages();
    }



    /**
     * Creates a new instance of the automated remark plugin with default settings.
     * No changes needed in this method
     * @param language the string that identifies the language of the user interface
     */
    public SamplePlugin(final String language) {
        this.config = new PluginConfiguration(SamplePlugin.CONFIG_FILE_PATH);
        this.messages = new LocalizedMessages(language);
    }



    /**
     * Creates a new instance of the automated remark plugin using the default language.
     * No changes needed in this method
     * @param taskSettings a map that contains the settings for the execution of this task
     */
    public SamplePlugin(final Map<String, String> taskSettings) {
        this();
        this.inputs = taskSettings;
    }



    /**
     * Creates a new instance of the automated remark plugin.
     * No changes needed in this method
     * @param language     the string that identifies the language of the user interface
     * @param taskSettings a map that contains the settings for the execution of this task
     */
    public SamplePlugin(final String language, final Map<String, String> taskSettings) {
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
    public final SamplePlugin newInstance(final String language) {
        return new SamplePlugin(language);
    }


    /**
     * Returns a new task processor instance with the provided settings.
     *
     * @param language the locale code of the language to display the messages in
     * @param inputs   the parameters for this task
     * @return the new task processor instance
     */
    @Override
    public final SamplePlugin newInstance(final String language, final Map<String, String> taskSettings) {
        return new SamplePlugin(language, taskSettings);
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
            this.help = this.messages.getFileContent(SamplePlugin.HELP_FILE_NAME);
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
     * AN SAMPLE IS WRITED IN THIS METHOD
     * @return a JSON string containing the definition of the parameters
     */
    @Override
    public final String getParams() {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode parametersNode = mapper.createArrayNode();

        /**
         * THE FOLLOWING SAMPLE ADDS TWO PARAMETERS IN ARRAY
         * IF PLUGIN ACCEPTS NO PARAMETERS THEN RETURN AN EMPTY ARRAY
         */
         /**
        ObjectNode remarkNode = parametersNode.addObject();
        remarkNode.put("code", this.config.getProperty("paramRemark"));
        remarkNode.put("label", this.messages.getString("paramRemark.label"));
        remarkNode.put("type", "multitext");
        remarkNode.put("req", true);
        remarkNode.put("maxlength", 5000);
         //if type = numeric then adds this optional parameters
         //remarkNode.put("min", 1);
         //remarkNode.put("step", 1);

        ObjectNode overwriteNode = parametersNode.addObject();
        overwriteNode.put("code", this.config.getProperty("param2"));
        overwriteNode.put("label", this.messages.getString("param2.label"));
        overwriteNode.put("type", "boolean");
        **/

        try {
            return mapper.writeValueAsString(parametersNode);

        } catch (JsonProcessingException exception) {
            logger.error("An error occurred when the parameters were converted to JSON.", exception);
            return null;
        }
    }


    /**
     * Executes the task.
     * CHANGE THIS METHOD BODY, AN SAMPLE IS WRITED IN THIS METHOD
     * @param request       the request that requires the execution of this task
     * @param emailSettings the parameters required to send an e-mail notification
     * @return an object  containing status, code, message and updated request
     */
    @Override
    public final ITaskProcessorResult execute(final ITaskProcessorRequest request, final IEmailSettings emailSettings) {

        final SampleResult pluginResult = new SampleResult();
        SampleResult.Status resultStatus = SampleResult.Status.ERROR;
        String resultMessage;
        String resultErrorCode = "-1";
        final SampleRequest updatedRequest = new SampleRequest(request);

        try {
            this.logger.debug("Start Sample Plugin");

            /**
             * THE FOLLOWING CODE IS A SAMPLE FROM THE PLUGIN REMARK
             * IF PLUGIN ACCEPTS NO PARAMETERS THEN RETURN AN EMPTY ARRAY
             */
            /**
            final boolean overwriteRemark = (this.inputs.get(this.config.getProperty("param2")).equals("true"));
            final String newRemark = this.inputs.get(this.config.getProperty("param1"));
            final String currentRemark = request.getRemark();

            if (overwriteRemark || currentRemark == null || "".equals(currentRemark)) {
                updatedRequest.setRemark(newRemark);
            } else {
                updatedRequest.setRemark(String.format("%s\r\n%s", currentRemark, newRemark));
            }
            **/

            resultMessage = this.messages.getString("sample.executing.success");
            resultStatus = SampleResult.Status.SUCCESS;
            resultErrorCode = "";

        } catch (Exception e) {
            this.logger.error("The Plugin has failed", e.getMessage());
            resultMessage = this.messages.getString("sample.executing.failed");
        }

        pluginResult.setStatus(resultStatus);
        pluginResult.setErrorCode(resultErrorCode);
        pluginResult.setMessage(resultMessage);
        pluginResult.setRequestData(updatedRequest);

        this.logger.debug("Plugin Sample : status is " + resultStatus.name());

        return pluginResult;
    }

}
