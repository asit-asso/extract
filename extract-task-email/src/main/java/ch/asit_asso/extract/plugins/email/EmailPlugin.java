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
package ch.asit_asso.extract.plugins.email;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.mail.internet.AddressException;
import ch.asit_asso.extract.plugins.common.IEmailSettings;
import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
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
    private final LocalizedMessages messages;

    /**
     * The settings for the execution of this task.
     */
    private Map<String, String> inputs;

    /**
     * The general settings for this plugin.
     */
    private final PluginConfiguration config;



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
            this.logger.error("An error occurred when the parameters were converted to JSON.", exception);
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
            this.logger.debug("Email settings enabled: {}", emailSettings != null ? emailSettings.isNotificationEnabled() : "null");

            final String toAsString = this.inputs.get(this.config.getProperty("param.to"));
            final String rawSubject = this.inputs.get(this.config.getProperty("param.subject"));
            final String rawBody = this.inputs.get(this.config.getProperty("param.body"));
            
            this.logger.debug("Recipients: {}", toAsString);
            this.logger.debug("Subject: {}", rawSubject);
            this.logger.debug("Body: {}", rawBody);
            this.logger.debug("Email settings: {}", request.getParameters());
            if (emailSettings.isNotificationEnabled()) {

                final String[] toAddressesArray = this.parseToAddressesString(toAsString);
                this.logger.debug("Parsed {} email addresses", toAddressesArray != null ? toAddressesArray.length : 0);

                if (!ArrayUtils.isEmpty(toAddressesArray)) {
                    final String subject = this.replaceRequestVariables(rawSubject, request);
                    final String body = this.replaceRequestVariables(rawBody, request);
                    
                    this.logger.debug("Processed subject: {}", subject);
                    this.logger.debug("Processed body (first 200 chars): {}", body != null && body.length() > 200 ? body.substring(0, 200) + "..." : body);

                    this.logger.debug("Attempting to send email notification...");
                    if (this.sendNotification(toAddressesArray, subject, body, emailSettings)) {
                        resultMessage = this.messages.getString("email.executing.success");
                        resultStatus = EmailResult.Status.SUCCESS;
                        resultErrorCode = "";
                        this.logger.info("Email sent successfully to {} recipients", toAddressesArray.length);
                    } else {
                        resultMessage = this.messages.getString("email.executing.failed");
                        this.logger.error("Failed to send email - sendNotification returned false");
                    }

                } else {
                    resultMessage = this.messages.getString("email.error.noAddressee");
                    this.logger.error("No valid email addresses found in: {}", toAsString);
                }

            } else {
                resultMessage = this.messages.getString("email.notifications.off");
                resultStatus = EmailResult.Status.SUCCESS;
                resultErrorCode = "";
                this.logger.warn("Email notifications are disabled in settings");
            }

        } catch (Exception e) {
            this.logger.error("The Plugin Email has failed with exception: " + e.getClass().getName(), e);
            String errorMsg = (e.getMessage() != null) ? e.getMessage() : "Unknown error";
            resultMessage = String.format(this.messages.getString("email.executing.failedWithMessage"), errorMsg);
            
            // Ensure we always return a valid result even on unexpected errors
            resultStatus = EmailResult.Status.ERROR;
            resultErrorCode = "-1";
        }

        // Ensure we always have valid values
        if (resultStatus == null) {
            resultStatus = EmailResult.Status.ERROR;
            this.logger.error("resultStatus was null, setting to ERROR");
        }
        if (resultMessage == null) {
            resultMessage = this.messages.getString("email.executing.failed");
            this.logger.error("resultMessage was null, setting default error message");
        }
        if (resultErrorCode == null) {
            resultErrorCode = "-1";
            this.logger.error("resultErrorCode was null, setting to -1");
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
            try {
                this.emailTemplate = this.messages.getFileContent(EmailPlugin.TEMPLATE_FILE_NAME);
                if (this.emailTemplate == null) {
                    this.logger.error("Email template file {} could not be loaded", TEMPLATE_FILE_NAME);
                    // Provide a basic fallback template
                    this.emailTemplate = "<!DOCTYPE html><html><head><title>##title##</title></head><body>##body##</body></html>";
                }
            } catch (Exception e) {
                this.logger.error("Error loading email template {}: {}", TEMPLATE_FILE_NAME, e.getMessage());
                this.emailTemplate = "<!DOCTYPE html><html><head><title>##title##</title></head><body>##body##</body></html>";
            }
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
        // Handle special alias fields
        String actualFieldName = fieldName;
        boolean isISOFormat = false;

        // Map alias fields to their actual field names
        if (fieldName.equals("clientName")) {
            actualFieldName = "client";
        } else if (fieldName.equals("organisationName")) {
            actualFieldName = "organism";
        } else if (fieldName.equals("startDateISO")) {
            actualFieldName = "startDate";
            isISOFormat = true;
        } else if (fieldName.equals("endDateISO")) {
            actualFieldName = "endDate";
            isISOFormat = true;
        }

        // First, try to use the getter method (preferred approach)
        try {
            // Build getter method name
            String getterName = "get" + actualFieldName.substring(0, 1).toUpperCase() + actualFieldName.substring(1);

            // Special case for boolean fields that might use "is" prefix
            if (actualFieldName.equals("rejected")) {
                getterName = "isRejected";
            }

            this.logger.trace("Trying to invoke getter '{}' for field '{}'", getterName, fieldName);

            // Try to find and invoke the getter method
            java.lang.reflect.Method getter = request.getClass().getMethod(getterName);
            Object result = getter.invoke(request);

            if (result == null) {
                this.logger.trace("Getter '{}' returned null, returning empty string", getterName);
                return "";
            }

            // Handle Calendar type specially
            if (result instanceof Calendar calendarResult) {
                if (isISOFormat) {
                    // Format as ISO 8601
                    String isoDateStr = calendarResult.getTime().toInstant().toString();
                    this.logger.trace("Getter '{}' returned Calendar, formatted as ISO 8601: {}", getterName, isoDateStr);
                    return isoDateStr;
                } else {
                    // Format as localized date/time
                    String dateStr = DateFormat.getDateTimeInstance().format(calendarResult.getTime());
                    this.logger.trace("Getter '{}' returned Calendar, formatted as: {}", getterName, dateStr);
                    return dateStr;
                }
            }

            String stringValue = result.toString();
            this.logger.trace("Getter '{}' returned: {}", getterName, stringValue);
            return stringValue;

        } catch (Exception e) {
            this.logger.debug("Could not find or invoke getter method '{}' for field '{}': {}",
                "get" + actualFieldName.substring(0, 1).toUpperCase() + actualFieldName.substring(1),
                fieldName, e.getMessage());
        }

        // Fallback to direct field access (for fields not exposed via getters)
        try {
            final Field field = request.getClass().getDeclaredField(actualFieldName);
            field.setAccessible(true);
            final Object fieldInstance = field.get(request);
            field.setAccessible(false);

            if (fieldInstance == null) {
                return "";
            }

            String fieldValue = fieldInstance.toString();

            if (field.getType().isAssignableFrom(Calendar.class)) {
                Calendar calendarFieldInstance = (Calendar) fieldInstance;
                if (isISOFormat) {
                    fieldValue = calendarFieldInstance.getTime().toInstant().toString();
                } else {
                    fieldValue = DateFormat.getDateTimeInstance().format(calendarFieldInstance.getTime());
                }
            }

            return fieldValue;

        } catch (NoSuchFieldException e) {
            this.logger.warn("Could not find the field \"{}\" in the request object.", fieldName);

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
        
        this.logger.debug("Parsing email addresses from: {}", addressesString);

        if (addressesString == null) {
            this.logger.warn("Email addresses string is null");
            return null;
        }
        
        if (addressesString.trim().isEmpty()) {
            this.logger.warn("Email addresses string is empty");
            return null;
        }

        List<String> addressesList = new ArrayList<>();

        for (String address : addressesString.split("[,;]")) {
            address = address.trim();
            
            this.logger.debug("Checking email address: '{}'", address);

            if (this.isEmailAddressValid(address)) {
                addressesList.add(address);
                this.logger.debug("Valid email address added: {}", address);
            } else {
                this.logger.warn("Invalid email address rejected: '{}'", address);
            }
        }
        
        this.logger.debug("Parsed {} valid email addresses from input", addressesList.size());

        return addressesList.toArray(String[]::new);
    }



    private boolean isEmailAddressValid(final String address) {

        return EmailValidator.getInstance().isValid(address);
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
        
        this.logger.debug("replaceRequestVariables called with string: {}", stringToProcess);
        this.logger.debug("Request class: {}", request.getClass().getName());

        // Try to get from authorizedFields first, fallback to content.properties.authorized
        String authorizedFieldsProperty = this.config.getProperty("authorizedFields");
        if (authorizedFieldsProperty == null || authorizedFieldsProperty.isEmpty()) {
            authorizedFieldsProperty = this.config.getProperty("content.properties.authorized");
        }
        final String[] authorizedFields = authorizedFieldsProperty.split(",");
        String formattedString = stringToProcess;

        // First, replace standard fields
        for (String fieldName : authorizedFields) {
            final String fieldValue = this.getRequestFieldValue(request, fieldName);
            
            this.logger.trace("Processing field '{}': value = '{}'", fieldName, fieldValue);

            if (fieldValue == null) {
                this.logger.trace("Field '{}' returned null, skipping", fieldName);
                continue;
            }

            final String fieldSearchPattern = String.format("(?i)\\{%s\\}", fieldName);
            formattedString = formattedString.replaceAll(fieldSearchPattern, fieldValue);
            this.logger.trace("Replaced pattern '{}' with value '{}'", fieldSearchPattern, fieldValue);
        }

        // Then, handle dynamic parameters from JSON
        formattedString = this.replaceDynamicParameters(formattedString, request);

        return formattedString;
    }

    /**
     * Replaces dynamic parameter placeholders from the request's parameters JSON field.
     * Supports both {parameters.xxx} and {param_xxx} formats.
     *
     * @param stringToProcess the string that may contain parameter placeholders
     * @param request         the request containing the parameters JSON
     * @return the string with parameter placeholders replaced
     */
    private String replaceDynamicParameters(final String stringToProcess, final ITaskProcessorRequest request) {
        if (StringUtils.isBlank(stringToProcess)) {
            return stringToProcess;
        }

        String formattedString = stringToProcess;

        try {
            // Get the parameters field value
            final String parametersJson = this.getRequestFieldValue(request, "parameters");

            if (parametersJson != null && !parametersJson.trim().isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> parametersMap = mapper.readValue(parametersJson,
                    new TypeReference<Map<String, Object>>() {});

                this.logger.debug("Parsed {} dynamic parameters from request", parametersMap.size());

                // Replace placeholders for each parameter
                for (Map.Entry<String, Object> entry : parametersMap.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue() != null ? entry.getValue().toString() : "";

                    // Support multiple placeholder formats
                    // 1. {parameters.KEY} (original case)
                    String pattern1 = String.format("(?i)\\{parameters\\.%s\\}", key);
                    formattedString = formattedString.replaceAll(pattern1, value);

                    // 2. {parameters.key} (lowercase)
                    String pattern2 = String.format("(?i)\\{parameters\\.%s\\}", key.toLowerCase());
                    formattedString = formattedString.replaceAll(pattern2, value);

                    // 3. {param_KEY} (original case)
                    String pattern3 = String.format("(?i)\\{param_%s\\}", key);
                    formattedString = formattedString.replaceAll(pattern3, value);

                    // 4. {param_key} (lowercase)
                    String pattern4 = String.format("(?i)\\{param_%s\\}", key.toLowerCase());
                    formattedString = formattedString.replaceAll(pattern4, value);

                    this.logger.trace("Replaced parameter {} with value {}", key, value);
                }
            }
        } catch (Exception e) {
            this.logger.error("Failed to parse and replace dynamic parameters: {}", e.getMessage());
        }

        // Replace any remaining unreplaced {parameters.XXX} or {param_XXX} placeholders with "null"
        // This handles cases where the parameter key doesn't exist in the JSON
        formattedString = formattedString.replaceAll("(?i)\\{parameters\\.[^}]+\\}", "null");
        formattedString = formattedString.replaceAll("(?i)\\{param_[^}]+\\}", "null");

        return formattedString;
    }



    private boolean sendNotification(final String[] toAddressesArray, final String subject, final String body,
            final IEmailSettings emailSettings) {
        assert ArrayUtils.isNotEmpty(toAddressesArray) :
                "There must be at least one address to send the notification to";

        this.logger.debug("sendNotification called with {} addresses", toAddressesArray.length);
        
        boolean hasSentMail = false;
        final String content = this.generateMessageContent(subject, body);

        if (content == null) {
            this.logger.error("The content of the message could not be generated. The usual cause is that the e-mail"
                    + " template could not be found. Subject: {}, Body (first 100 chars): {}", 
                    subject, body != null && body.length() > 100 ? body.substring(0, 100) + "..." : body);
            return false;
        }
        
        this.logger.debug("Email content generated successfully, length: {} chars", content.length());

        for (String address : toAddressesArray) {
            this.logger.debug("Processing email for address: {}", address);
            Email email = new Email(emailSettings);

            try {
                email.addRecipient(address);
                this.logger.debug("Recipient added successfully: {}", address);
            } catch (AddressException exception) {
                this.logger.error("The address {} could not be added as recipient. The error message is: {}.", 
                        address, exception.getMessage());
                this.logger.debug("Full exception details for invalid address {}: ", address, exception);
                continue;
            } catch (Exception exception) {
                this.logger.error("Unexpected error when adding recipient {}: {}", 
                        address, exception.getMessage());
                this.logger.debug("Full exception details: ", exception);
                continue;
            }

            email.setSubject(subject);
            email.setContentType(Email.ContentType.HTML);
            email.setContent(content);
            
            this.logger.debug("Sending email with subject: '{}' to: {}", subject, address);

            if (!email.send()) {
                this.logger.error("Failed to send email to {}. The email.send() method returned false. This could be due to SMTP configuration issues.", address);
                continue;
            }

            this.logger.info("Email sent successfully to: {}", address);
            hasSentMail = true;
        }

        this.logger.debug("sendNotification completed. Success: {}", hasSentMail);
        return hasSentMail;
    }

}
