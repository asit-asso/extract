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
package ch.asit_asso.extract.email;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;

import ch.asit_asso.extract.plugins.common.IEmailSettings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import ch.asit_asso.extract.persistence.SystemParametersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.thymeleaf.TemplateEngine;



/**
 * The parameters to use to send electronic messages.
 *
 * @author Yves Grasset
 */
public class EmailSettings implements IEmailSettings {

    /**
     * The smallest number that can legally be used as an HTTP port number.
     */
    private static final int FIRST_VALID_HTTP_PORT = 0;

    /**
     * The largest number that can legally be used as an HTTP port number.
     */
    private static final int LAST_VALID_HTTP_PORT = 65535;

    /**
     * The base URL to access the application from outside.
     */
    private final URL applicationExternalRootUrl;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(EmailSettings.class);

    /**
     * Whether the application must send e-mail notifications.
     */
    private boolean notificationEnabled;

    /**
     * The object that gives an access to the localized strings.
     */
    private final MessageSource messageSource;

    /**
     * The e-mail address to use as the e-mail sender.
     */
    private String senderAddress;

    /**
     * The string to use as the sender name.
     */
    private String senderName;

    /**
     * The name of the mail server.
     */
    private String smtpHost;

    /**
     * The password to use to connect to the mail server.
     */
    private String smtpPassword;

    /**
     * The user name to use to connect to the mail server.
     */
    private String smtpUser;

    /**
     * The port used to communicate with the mail server.
     */
    private int smtpPort;

    /**
     * Whether messages should sent through a secure connection.
     */
    private SslType sslType;

    /**
     * The Spring Data object that links the general parameters of the application with the data source.
     */
    private SystemParametersRepository systemParametersRepository;

    /**
     * The Thymeleaf object that allows to process the e-mail templates.
     */
    private final TemplateEngine templateEngine;



    /**
     * How the connection to the SMTP is secured.
     */
    public enum SslType {
        /**
         * The connection to the SMTP server is not secure.
         */
        NONE,
        /**
         * The connection to the SMTP server forces the use of a secured connection and fails if the server
         * does not support it.
         */
        EXPLICIT,
        /**
         * The connection to the SMTP server uses a secured connection if the server supports it or a
         * non-secure one otherwise.
         */
        IMPLICIT
    }



    /**
     * Creates a new instance of the e-mail settings.
     *
     * @param repository      the Spring Data object that links the application parameters with the data source
     * @param engine          the object that allows to process the e-mail templates
     * @param messages        the object that gives an access to the application strings
     * @param externalRootUrl a string that contains the absolute URL of the application
     */
    public EmailSettings(final SystemParametersRepository repository, final TemplateEngine engine,
            final MessageSource messages, final String externalRootUrl) {

        if (repository == null) {
            throw new IllegalArgumentException("The system parameters repository cannot be null.");
        }

        if (engine == null) {
            throw new IllegalArgumentException("The template engine cannot be null.");
        }

        if (messages == null) {
            throw new IllegalArgumentException("The message source cannot be null.");
        }

        if (StringUtils.isBlank(externalRootUrl)) {
            throw new IllegalArgumentException("The application root URL cannot be empty.");
        }

        URL rootUrl;

        try {
            rootUrl = new URL(externalRootUrl);

        } catch (MalformedURLException exception) {
            throw new IllegalArgumentException("The application root URL is invalid.", exception);
        }

        this.systemParametersRepository = repository;
        this.templateEngine = engine;
        this.messageSource = messages;
        this.applicationExternalRootUrl = rootUrl;
        this.setSettingsFromDataSource();
    }



    /**
     * Obtains the application string that matches the given key.
     *
     * @param messageKey the key that identifies the desired message
     * @return the message
     */
    public final String getMessageString(final String messageKey) {
        return this.getMessageString(messageKey, null);
    }



    /**
     * Obtains the application string that matches the given key.
     *
     * @param messageKey the key that identifies the desired message
     * @param arguments  an array of object that will replace the placeholders in the message string, or
     *                   <code>null</code> if no substitution is needed
     * @return the message
     */
    public final String getMessageString(final String messageKey, final Object[] arguments) {

        if (StringUtils.isBlank(messageKey)) {
            throw new IllegalArgumentException("The message key cannot be null.");
        }

        return this.messageSource.getMessage(messageKey, arguments, Locale.getDefault());
    }



    /**
     * Returns the full address to access a given ressource from the outside.
     *
     * @param relativeUrl the address of the ressource relative to the application root
     * @return the absolute URL
     * @throws MalformedURLException if the relative URL is not valid
     */
    public final String getAbsoluteUrl(final String relativeUrl) throws MalformedURLException {

        if (relativeUrl == null) {
            throw new IllegalArgumentException("The relative URL cannot be null.");
        }

        final String applicationRelativeUrl = (relativeUrl.startsWith("/")) ? relativeUrl.substring(1) : relativeUrl;

        return new URL(this.applicationExternalRootUrl, applicationRelativeUrl).toString();
    }



    /**
     * Obtains the e-mail address to use as the sender of application messages.
     *
     * @return the e-mail address of the sender
     */
    @Override
    public final String getSenderAddress() {
        return this.senderAddress;
    }



    /**
     * Obtains the human-friendly name to use for the sender.
     *
     * @return the name of the sender
     */
    @Override
    public final String getSenderName() {
        return this.senderName;
    }



    /**
     * Obtains the name of the server to use to send electronic messages.
     *
     * @return the SMTP server name
     */
    @Override
    public final String getSmtpHost() {
        return this.smtpHost;
    }



    /**
     * Obtains the string to use to authenticate with the mail server.
     *
     * @return the password, or <code>null</code> if no authentication is necessary
     */
    @Override
    public final String getSmtpPassword() {
        return this.smtpPassword;
    }



    /**
     * Obtains the TCP port to use to communicate with the mail server.
     *
     * @return the TCP port number
     */
    @Override
    public final int getSmtpPort() {
        return this.smtpPort;
    }



    /**
     * Obtains the user name to use to authenticate with the mail server.
     *
     * @return the SMTP user name
     */
    @Override
    public final String getSmtpUser() {
        return this.smtpUser;
    }



    /**
     * Obtains the type of secure connection to establish when connecting to the mail server.
     *
     * @return the type of SSL connection to use, such as implicit or explicit
     */
    public final SslType getSslType() {
        return this.sslType;
    }



    /**
     * Obtains the type of secure connection to establish when connecting to the mail server.
     *
     * @return a string identifying the type of SSL connection to use. The possible values are NONE, IMPLICIT and
     *         EXPLICIT
     */
    @Override
    public final String getSslTypeAsString() {
        return this.sslType.name();
    }



    /**
     * Obtains the e-mail template processor.
     *
     * @return the template engine
     */
    public final TemplateEngine getTemplateEngine() {
        return this.templateEngine;
    }



    /**
     * Obtains the protocol to use to communicate with the server.
     *
     * @return the string that identifies the protocol to use
     */
    @Override
    public final String getTransport() {
        return "smtp";
    }



    /**
     * Obtains whether the application must send e-mail notifications.
     *
     * @return <code>true</code> if the e-mail notifications must be sent
     */
    @Override
    public final boolean isNotificationEnabled() {
        return this.notificationEnabled;
    }



    /**
     * Obtains whether the connection with the mail server should authenticated with a user and password.
     *
     * @return <code>true</code> if the connection must be authenticated
     */
    @Override
    public final boolean useAuthentication() {
        return (this.smtpPassword != null);
    }



    /**
     * Reads the e-mail settings from the data source.
     */
    public final void refresh() {
        this.setSettingsFromDataSource();
    }



    /**
     * Creates a {@link java.util.Properties} object with the current e-mail settings that are necessary to
     * start a session.
     *
     * @return the SMTP configuration properties
     */
    @Override
    public final Properties toSystemProperties() {
        Properties properties = new Properties();
        String portString = Integer.toString(this.getSmtpPort());
        properties.put("mail.smtp.port", portString);
        String transport = this.getTransport();
        properties.put(String.format("mail.%s.host", transport), this.getSmtpHost());
        properties.put(String.format("mail.%s.auth", transport), Boolean.toString(this.useAuthentication()));

        if (this.getSslType() != SslType.NONE) {
            properties.put("mail.smtp.ssl.checkserveridentity", "true");

            if (this.getSslType() == SslType.EXPLICIT) {
                properties.put("mail.smtp.starttls.enable", "true");

            } else if (this.getSslType() == SslType.IMPLICIT) {
                properties.put("mail.smtp.ssl.enable", "true");
            }
        }
        return properties;
    }



    /**
     * Determines if the current SMTP configuration data is correctly defined.
     *
     * @return <code>true</code> if the configuration is valid
     */
    @Override
    public final boolean isValid() {

        return EmailValidator.getInstance().isValid(this.senderAddress) && !StringUtils.isBlank(this.senderName)
                && !StringUtils.isBlank(this.smtpHost) && this.smtpPort >= EmailSettings.FIRST_VALID_HTTP_PORT
                && this.smtpPort <= EmailSettings.LAST_VALID_HTTP_PORT && this.sslType != null;
    }



    /**
     * Defines whether the application must send e-mail notifications.
     *
     * @param enabled <code>true</code> to turn the notifications on
     */
    private void setNotificationsEnabled(final boolean enabled) {
        this.notificationEnabled = enabled;
    }



    /**
     * Defines the e-mail address to use as the sender of the messages sent by the application.
     *
     * @param emailAddress a valid e-mail address
     */
    private void setSenderAddress(final String emailAddress) {
        this.senderAddress = emailAddress;
    }



    /**
     * Defines the human-friendly name for the sender of the application messages.
     *
     * @param name the name of the sender
     */
    private void setSenderName(final String name) {
        this.senderName = name;
    }



    /**
     * Defines the name of the server to use to send electronic messages.
     *
     * @param serverName the name of the SMTP server
     */
    private void setSmtpHost(final String serverName) {
        this.smtpHost = serverName;
    }



    /**
     * Defines the string to use to authenticate with the mail server.
     *
     * @param password the password, or either <code>null</code> or an empty string if no authentication is necessary
     */
    private void setSmtpPassword(final String password) {

        if (StringUtils.isEmpty(password)) {
            this.smtpPassword = null;

        } else {
            this.smtpPassword = password;
        }
    }



    /**
     * Defines the user name to use to authenticate with the mail server.
     *
     * @param userName the string to use as the identifier of the user that allows to authenticate on the SMTP server,
     *                 or <code>null</code> if the server does not require an authentication
     */
    private void setSmtpUser(final String userName) {

        if (StringUtils.isEmpty(userName)) {
            this.smtpUser = null;

        } else {
            this.smtpUser = userName;
        }
    }



    /**
     * Defines the TCP port to use to communicate with the mail server.
     *
     * @param port the TCP port number
     */
    private void setSmtpPort(final int port) {
        this.smtpPort = port;
    }



    /**
     * Defines the type of secure connection to establish when connecting to the mail server.
     *
     * @param secureConnectionType the type of SSL connection to use
     */
    private void setSsl(final SslType secureConnectionType) {
        this.sslType = secureConnectionType;
    }



    /**
     * Defines the e-mail parameters from what is currently set in the data source.
     */
    private void setSettingsFromDataSource() {
        this.logger.debug("Defining the SMTP configuration from the data source.");
        this.setSenderAddress(this.systemParametersRepository.getSmtpFromMail());
        this.setSenderName(this.systemParametersRepository.getSmtpFromName());
        this.setSmtpHost(this.systemParametersRepository.getSmtpServer());
        this.setSmtpUser(this.systemParametersRepository.getSmtpUser());
        this.setSmtpPassword(this.systemParametersRepository.getSmtpPassword());
        final String rawNotificationParameterValue = this.systemParametersRepository.isEmailNotificationEnabled();
        this.setNotificationsEnabled(Boolean.parseBoolean(rawNotificationParameterValue));

        try {
            this.setSmtpPort(Integer.valueOf(this.systemParametersRepository.getSmtpPort()));

        } catch (NumberFormatException exception) {
            this.logger.error("The SMTP port in the data source is not a valid integer.");
        }

        String sslTypeConfigString = this.systemParametersRepository.getSmtpSSL();

        if (sslTypeConfigString != null) {
            this.setSsl(SslType.valueOf(sslTypeConfigString));
        }

        if (!this.isValid()) {
            this.logger.warn("The SMTP parameters in the data source are not valid. Please check the configuration.");
        }
    }

}
