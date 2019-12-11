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
package org.easysdi.extract.plugins.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import javax.mail.internet.AddressException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.easysdi.extract.plugins.common.IEmailSettings;
import org.easysdi.extract.plugins.common.ITaskProcessor;
import org.easysdi.extract.plugins.common.ITaskProcessorRequest;
import org.easysdi.extract.plugins.common.ITaskProcessorResult;
import org.easysdi.extract.plugins.email.Email.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * A plugin that adds an automated remark to a request.
 *
 * @author Yves Grasset
 */
public class EmailPlugin implements ITaskProcessor {

    /**
     * The path to the file that holds the general settings for this plugin.
     */
    private static final String CONFIG_FILE_PATH = "plugins/email/properties/configEmail.properties";

    /**
     * The name of the file that holds the text explaining how to use this plugin in the language of
     * the user interface.
     */
    private static final String HELP_FILE_NAME = "emailHelp.html";

    /**
     * The name of the file to use to generate the HTML content of the message sent by this plugin
     * in the language of the user interface.
     */
    private static final String TEMPLATE_FILE_NAME = "emailTemplate.html";

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(EmailPlugin.class);

    /**
     * The string that identifies this plugin.
     */
    private final String code = "EMAIL";

    /**
     * The model used to generate the HTML content of the sent e-mail message.
     */
    private String emailTemplate = null;

    /**
     * The text that explains how to use this plugin in the language of the user interface.
     */
    private String help = null;

    /**
     * The CSS class of the icon to display to represent this plugin.
     */
    private final String pictoClass = "fa-envelope-o";

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
    public EmailPlugin() {
        this.config = new PluginConfiguration(EmailPlugin.CONFIG_FILE_PATH);
        this.messages = new LocalizedMessages();
    }



    /**
     * Creates a new instance of the automated remark plugin with default settings.
     *
     * @param language the string that identifies the language of the user interface
     */
    public EmailPlugin(final String language) {
        this.config = new PluginConfiguration(EmailPlugin.CONFIG_FILE_PATH);
        this.messages = new LocalizedMessages(language);
    }



    /**
     * Creates a new instance of the automated remark plugin using the default language.
     *
     * @param taskSettings a map that contains the settings for the execution of this task
     */
    public EmailPlugin(final Map<String, String> taskSettings) {
        this();
        this.inputs = taskSettings;
    }



    /**
     * Creates a new instance of the automated remark plugin.
     *
     * @param language     the string that identifies the language of the user interface
     * @param taskSettings a map that contains the settings for the execution of this task
     */
    public EmailPlugin(final String language, final Map<String, String> taskSettings) {
        this(language);
        this.inputs = taskSettings;
    }



    @Override
    public final EmailPlugin newInstance(final String language) {
        return new EmailPlugin(language);
    }



    @Override
    public final EmailPlugin newInstance(final String language, final Map<String, String> taskSettings) {
        return new EmailPlugin(language, taskSettings);
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
            this.help = this.messages.getFileContent(EmailPlugin.HELP_FILE_NAME);
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

        ObjectNode toNode = parametersNode.addObject();
        toNode.put("code", this.config.getProperty("param.to"));
        toNode.put("label", this.messages.getString("param.to.label"));
        toNode.put("type", "email");
        toNode.put("req", true);
        toNode.put("maxlength", 5000);

        ObjectNode subjectNode = parametersNode.addObject();
        subjectNode.put("code", this.config.getProperty("param.subject"));
        subjectNode.put("label", this.messages.getString("param.subject.label"));
        subjectNode.put("type", "text");
        subjectNode.put("req", true);
        subjectNode.put("maxlength", 1000);

        ObjectNode bodyNode = parametersNode.addObject();
        bodyNode.put("code", this.config.getProperty("param.body"));
        bodyNode.put("label", this.messages.getString("param.body.label"));
        bodyNode.put("type", "multitext");
        bodyNode.put("req", true);
        bodyNode.put("maxlength", 5000);

        try {
            return mapper.writeValueAsString(parametersNode);

        } catch (JsonProcessingException exception) {
            logger.error("An error occurred when the parameters were converted to JSON.", exception);
            return null;
        }
    }



    @Override
    public final ITaskProcessorResult execute(final ITaskProcessorRequest request, final IEmailSettings emailSettings) {

        final EmailResult pluginResult = new EmailResult();
        EmailResult.Status resultStatus = EmailResult.Status.ERROR;
        String resultMessage;
        String resultErrorCode = "-1";

        try {
            this.logger.debug("Start Email Plugin");

            final String toAsString = this.inputs.get(this.config.getProperty("param.to"));
            final String rawSubject = this.inputs.get(this.config.getProperty("param.subject"));
            final String rawBody = this.inputs.get(this.config.getProperty("param.body"));

            if (emailSettings.isNotificationEnabled()) {

                final String[] toAddressesArray = this.parseToAddressesString(toAsString);

                if (!ArrayUtils.isEmpty(toAddressesArray)) {
                    final String subject = this.replaceRequestVariables(rawSubject, request);
                    final String body = this.replaceRequestVariables(rawBody, request);

                    if (this.sendNotification(toAddressesArray, subject, body, emailSettings)) {
                        resultMessage = this.messages.getString("email.executing.success");
                        resultStatus = EmailResult.Status.SUCCESS;
                        resultErrorCode = "";
                    } else {
                        resultMessage = this.messages.getString("email.executing.failed");
                    }

                } else {
                    resultMessage = this.messages.getString("email.error.noAddressee");
                }

            } else {
                resultMessage = this.messages.getString("email.notifications.off");
                resultStatus = EmailResult.Status.SUCCESS;
                resultErrorCode = "";
            }

        } catch (Exception e) {
            this.logger.error("The Plugin Email has failed", e.getMessage());
            resultMessage = String.format(this.messages.getString("email.executing.failedWithMessage"), e.getMessage());
        }

        pluginResult.setStatus(resultStatus);
        pluginResult.setErrorCode(resultErrorCode);
        pluginResult.setMessage(resultMessage);
        pluginResult.setRequestData(request);

        this.logger.debug("Plugin Email : status is " + resultStatus.name());

        return pluginResult;
    }



    /**
     * Creates the content of the message to be sent based on a template in the language used by the
     * application.
     *
     * @param title the subject of the message
     * @param body  the text of the message
     * @return the HTML code to define as the content of the message, or <code>null</code> if it could not be
     *         generated (usually because the template could not be found)
     */
    private String generateMessageContent(final String title, final String body) {

        final String template = this.getEmailTemplate();

        if (template == null) {
            return null;
        }

        return template.replace("##title##", title).replace("##body##", body.replaceAll("\n(\r)?|\r(\n)?", "<br />"));
    }



    /**
     * Obtains the file to use to generate the HTML content of the message sent by this plugin.
     *
     * @return the message template, or <code>null</code> if it could not be found
     */
    private String getEmailTemplate() {

        if (this.emailTemplate == null) {
            this.emailTemplate = this.messages.getFileContent(EmailPlugin.TEMPLATE_FILE_NAME);
        }

        return this.emailTemplate;
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

                if (calendarFieldInstance.getTime() != null) {
                    fieldValue = DateFormat.getDateTimeInstance().format(calendarFieldInstance.getTime());
                }
            }

            return fieldValue;

        } catch (NoSuchFieldException e) {
            this.logger.error("Could not find the field \"" + fieldName + "\" in the request object.", e);

        } catch (IllegalAccessException exc) {
            this.logger.error("Could not access the field value for \"" + fieldName + "\".", exc);
        }

        return null;
    }



    /**
     * Splits a string that contains e-mail addresses separated by commas or semicolons and validates them.
     *
     * @param addressesString the string that lists the e-mail addresses
     * @return an array that contains each valid e-mail address contained in the string, or <code>null</code> if the
     *         input string could not be parsed
     */
    private String[] parseToAddressesString(final String addressesString) {

        if (addressesString == null) {
            return null;
        }

        List<String> addressesList = new ArrayList<>();
        EmailValidator validator = EmailValidator.getInstance();

        for (String address : addressesString.split("[,;]")) {
            address = address.trim();

            if (validator.isValid(address)) {
                addressesList.add(address);
            }
        }

        return addressesList.toArray(new String[addressesList.size()]);
    }



    /**
     * Substitutes the placeholders for request variables in a string with their value.
     *
     * @param stringToProcess the string that may contain request variables
     * @param request         the request whose properties must be used to replace the placeholders
     * @return the string with the placeholders replaced (if any)
     */
    private String replaceRequestVariables(final String stringToProcess, final ITaskProcessorRequest request) {

        if (StringUtils.isBlank(stringToProcess)) {
            return stringToProcess;
        }

        final String[] authorizedFields = this.config.getProperty("content.properties.authorized").split(",");
        String formattedString = stringToProcess;

        for (String fieldName : authorizedFields) {
            final String fieldValue = this.getRequestFieldValue(request, fieldName);

            if (fieldValue == null) {
                continue;
            }

            final String fieldSearchPattern = String.format("(?i)\\{%s\\}", fieldName);
            formattedString = formattedString.replaceAll(fieldSearchPattern, fieldValue);
        }

        return formattedString;
    }



    private boolean sendNotification(final String[] toAddressesArray, final String subject, final String body,
            final IEmailSettings emailSettings) {
        assert ArrayUtils.isNotEmpty(toAddressesArray) :
                "There must be at least one address to send the notification to";

        boolean hasSentMail = false;
        final String content = this.generateMessageContent(subject, body);

        if (content == null) {
            this.logger.warn("The content of the message could not be generated. The usual cause is that the e-mail"
                    + " template could not be found.");
            return false;
        }

        for (String address : toAddressesArray) {
            Email email = new Email(emailSettings);

            try {
                email.addRecipient(address);
            } catch (AddressException exception) {
                this.logger.warn("The address {} could not be added as recipient. The error message is : {}.", address,
                        exception.getMessage());
                continue;
            }

            email.setSubject(subject);
            email.setContentType(ContentType.HTML);
            email.setContent(content);

            if (!email.send()) {
                this.logger.warn("An error occurred when the notification was sent to {}.", address);
                continue;
            }

            hasSentMail = true;
        }

        return hasSentMail;
    }

}
