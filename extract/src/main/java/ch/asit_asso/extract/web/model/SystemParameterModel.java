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
package ch.asit_asso.extract.web.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ch.asit_asso.extract.domain.SystemParameter;
import ch.asit_asso.extract.email.EmailSettings.SslType;
import ch.asit_asso.extract.ldap.LdapSettings;
import ch.asit_asso.extract.orchestrator.OrchestratorSettings;
import ch.asit_asso.extract.orchestrator.OrchestratorTimeRange;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * The representation of the application settings.
 *
 * @author Yves Grasset
 */
public class SystemParameterModel extends PluginItemModel {

    /**
     * The string used as a placeholder for the existing password.
     */
    public static final String PASSWORD_GENERIC_STRING = "*****";

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(SystemParameterModel.class);

    /**
     * The path of the folder that contains the data for all the requests.
     */
    private String basePath;

    /**
     * The delay to wait before refreshing the dashboard.data.
     */
    private String dashboardFrequency;

    private boolean displayTempFolder;

    private String ldapAdminsGroup;

    private String ldapBaseDn;

    private boolean ldapEnabled;

    private LdapSettings.EncryptionType ldapEncryption;

    private String ldapOperatorsGroup;

    private String ldapServers;

    private boolean ldapSynchronizationEnabled;

    private String ldapSynchronizationFrequency;

    private String ldapSynchronizationPassword;

    private String ldapSynchronizationUser;

    /**
     * Whether the application must send e-mail notifications.
     */
    private boolean mailEnabled;

    /**
     * The delay to wait before a new execution of an orchestrator job.
     */
    private String schedulerFrequency;

    /**
     * The run mode of the orchestrator.
     */
    private OrchestratorSettings.SchedulerMode schedulerMode;

    /**
     * The delay to wait before a new execution of an orchestrator job.
     */
    private List<OrchestratorTimeRange> schedulerRanges = new ArrayList<>();

    /**
     * The e-mail address of the sender for the messages sent by the application.
     */
    private String smtpFromMail;

    /**
     * The name of the sender for the messages sent by the application.
     */
    private String smtpFromName;

    /**
     * The password to use to authenticate with the mail server.
     */
    private String smtpPassword;

    /**
     * The port to use to communicate with the mail server.
     */
    private String smtpPort;

    /**
     * The name of the mail server.
     */
    private String smtpServer;

    /**
     * The user that allows to authenticate with the mail server, or <code>null</code> if the server
     * does not require authentication.
     */
    private String smtpUser;

    /**
     * The type of secure connection to establish with the mail server.
     */
    private SslType sslType;


    private String standbyReminderDays;


    private String validationFocusProperties;


    /**
     * Obtains the path of the folder that contains the data for all the requests.
     *
     * @return the absolute path of the requests data folder
     */
    public final String getBasePath() {
        return this.basePath;
    }



    /**
     * Defines the path of the folder that contains the data for all the requests.
     *
     * @param baseDataFolderPath the absolute path of the folder that contains the date for all the requests.
     */
    public final void setBasePath(final String baseDataFolderPath) {
        this.basePath = baseDataFolderPath;
    }



    /**
     * Obtains the delay before refreshing the dashboard data.
     *
     * @return the refresh delay in seconds
     */
    public final String getDashboardFrequency() {
        return this.dashboardFrequency;
    }



    /**
     * Defines the delay before refreshing the dashboard data.
     *
     * @param frequency a string with the refresh delay in seconds
     */
    public final void setDashboardFrequency(final String frequency) {
        this.dashboardFrequency = frequency;
    }


    public final boolean isDisplayTempFolder() { return this.displayTempFolder; }


    public final void setDisplayTempFolder(final boolean display) { this.displayTempFolder = display; }



    public final boolean isLdapEnabled() { return this.ldapEnabled; }



    public final void setLdapEnabled(final boolean enabled) { this.ldapEnabled = enabled; }



    public final String getLdapAdminsGroup() { return this.ldapAdminsGroup; }



    public final void setLdapAdminsGroup(final String query) { this.ldapAdminsGroup = query; }



    public final String getLdapBaseDn() { return this.ldapBaseDn; }



    public final void setLdapBaseDn(final String baseDn ) { this.ldapBaseDn = baseDn; }



    public final LdapSettings.EncryptionType getLdapEncryption() { return this.ldapEncryption; }



    public final void setLdapEncryption(final LdapSettings.EncryptionType encryption) { this.ldapEncryption = encryption; }



    public final void setLdapEncryption(final String encryptionValue) {
        this.ldapEncryption = null;

        if (encryptionValue != null) {
            this.ldapEncryption = LdapSettings.EncryptionType.valueOf(encryptionValue);
        }
    }



    public final String getLdapOperatorsGroup() { return this.ldapOperatorsGroup; }



    public final void setLdapOperatorsGroup(final String query) { this.ldapOperatorsGroup = query; }



    public final String getLdapServers() { return this.ldapServers; }



    public final void setLdapServers(final String servers) { this.ldapServers = servers; }



    public final boolean isLdapSynchronizationEnabled() { return this.ldapSynchronizationEnabled; }



    public void setLdapSynchronizationEnabled(boolean ldapSynchronizationEnabled) {
        this.ldapSynchronizationEnabled = ldapSynchronizationEnabled;
    }



    public final String getLdapSynchronizationFrequency() { return this.ldapSynchronizationFrequency; }



    public final void setLdapSynchronizationFrequency(final String hours) { this.ldapSynchronizationFrequency = hours; }


    public String getLdapSynchronizationPassword() { return this.ldapSynchronizationPassword; }


    public void setLdapSynchronizationPassword(String password) { this.ldapSynchronizationPassword = password; }


    public String getLdapSynchronizationUser() { return this.ldapSynchronizationUser; }


    public void setLdapSynchronizationUser(String userName) { this.ldapSynchronizationUser = userName; };

    /**
     * Gets the delay to wait before a new execution of an orchestrator job.
     *
     * @return the delay in seconds
     */
    public final String getSchedulerFrequency() {
        return this.schedulerFrequency;
    }



    /**
     * Defines the delay to wait before a new execution of an orchestrator job.
     *
     * @param frequency a string with the number to use as a delay to wait before a new execution of an
     *                  orchestrator job
     */
    public final void setSchedulerFrequency(final String frequency) {
        this.schedulerFrequency = frequency;
    }



    /**
     * Gets the delay to wait before a new execution of an orchestrator job.
     *
     * @return the delay in seconds
     */
    public final OrchestratorSettings.SchedulerMode getSchedulerMode() {
        return this.schedulerMode;
    }



    /**
     * Defines the delay to wait before a new execution of an orchestrator job.
     *
     * @param mode
     */
    public final void setSchedulerMode(final OrchestratorSettings.SchedulerMode mode) {

        if (mode == null) {
            throw new IllegalArgumentException("The orchestrator running mode cannot be null.");
        }

        this.schedulerMode = mode;
    }



    /**
     * Gets the delay to wait before a new execution of an orchestrator job.
     *
     * @return the delay in seconds
     */
    public final OrchestratorTimeRange[] getSchedulerRanges() {
        return this.schedulerRanges.toArray(new OrchestratorTimeRange[]{});
    }



    public final String getSchedulerRangesAsJson() {
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.writeValueAsString(this.getSchedulerRanges());

        } catch (JsonProcessingException exception) {
            this.logger.error("Could not serialize the operation time ranges of the orchestrator to JSON.", exception);
            return null;
        }
    }



    /**
     * Defines the delay to wait before a new execution of an orchestrator job.
     *
     * @param ranges
     */
    public final void setSchedulerRanges(final OrchestratorTimeRange[] ranges) {
        this.schedulerRanges.clear();
        this.schedulerRanges.addAll(Arrays.asList(ranges));
    }



    /**
     * Defines the delay to wait before a new execution of an orchestrator job.
     *
     * @param ranges
     */
    public final void setSchedulerRanges(final List<OrchestratorTimeRange> ranges) {
        this.schedulerRanges = ranges;
    }



    /**
     * Gets the e-mail address of the sender for the messages sent by the application.
     *
     * @return the sender address
     */
    public final String getSmtpFromMail() {
        return this.smtpFromMail;
    }



    /**
     * Defines the e-mail address of the sender for the messages sent by the application.
     *
     * @param fromAddress the e-mail to use as an e-mail address of the sender for the messages sent by the
     *                    application
     */
    public final void setSmtpFromMail(final String fromAddress) {
        this.smtpFromMail = fromAddress;
    }



    /**
     * Gets the name of the sender for the messages sent by the application.
     *
     * @return the sender name
     */
    public final String getSmtpFromName() {
        return this.smtpFromName;
    }



    /**
     * Defines the name of the sender for the messages sent by the application.
     *
     * @param fromName the string to use as a name of the sender for the message sent by application
     */
    public final void setSmtpFromName(final String fromName) {
        this.smtpFromName = fromName;
    }



    /**
     * Gets the password to use to use to authenticate with the mail server.
     *
     * @return the password
     */
    public final String getSmtpPassword() {
        return this.smtpPassword;
    }



    /**
     * Defines the password to use to authenticate with the mail server.
     *
     * @param password the password
     */
    public final void setSmtpPassword(final String password) {
        this.smtpPassword = password;
    }



    /**
     * Gets the port to use to communicate with the mail server.
     *
     * @return a string with the port number
     */
    public final String getSmtpPort() {
        return this.smtpPort;
    }



    /**
     * Defines the port to use to communicate with the mail server.
     *
     * @param port a string with the port number
     */
    public final void setSmtpPort(final String port) {
        this.smtpPort = port;
    }



    /**
     * Gets the name of the mail server.
     *
     * @return the server name
     */
    public final String getSmtpServer() {
        return this.smtpServer;
    }



    /**
     * Defines the name of the mail server.
     *
     * @param serverName the mail server name
     */
    public final void setSmtpServer(final String serverName) {
        this.smtpServer = serverName;
    }



    /**
     * Gets the user name that allows to authenticate with the mail server.
     *
     * @return the login, or <code>null</code> if the server does not require authentication
     */
    public final String getSmtpUser() {
        return this.smtpUser;
    }



    /**
     * Defines the user name that allows to authenticate with the mail server.
     *
     * @param userName the login, or <code>null</code> if the server does not require authentication
     */
    public final void setSmtpUser(final String userName) {
        this.smtpUser = userName;
    }



    /**
     * Obtains the type of secure connection to establish with the mail server.
     *
     * @return the secure connection type
     */
    public final SslType getSslType() {
        return this.sslType;
    }



    /**
     * Defines the type of secure connection to establish with the mail server.
     *
     * @param connectionType the secure connection type
     */
    public final void setSslType(final SslType connectionType) {
        this.sslType = connectionType;
    }



    /**
     * Defines the type of secure connection to establish with the mail server.
     *
     * @param connectionTypeString a string with the secure connection type
     */
    public final void setSslType(final String connectionTypeString) {
        this.sslType = null;

        if (connectionTypeString != null) {
            this.sslType = SslType.valueOf(connectionTypeString);
        }
    }



    public final String getStandbyReminderDays() { return this.standbyReminderDays; }


    public final void setStandbyReminderDays(final String days) { this.standbyReminderDays = days; }



    /**
     * Obtains whether the application must send e-mail notifications.
     *
     * @return <code>true</code> if the notifications are enabled
     */
    public final boolean isMailEnabled() {
        return this.mailEnabled;
    }



    /**
     * Defines whether the application must send e-mail notifications.
     *
     * @param sendNotifications <code>true</code> to enable the notifications
     */
    public final void setMailEnabled(final boolean sendNotifications) {
        this.mailEnabled = sendNotifications;
    }



    public final String getValidationFocusProperties() { return this.validationFocusProperties; }



    public final void setValidationFocusProperties(String propertiesString) { this.validationFocusProperties = propertiesString; }



    /**
     * Creates a new instance of this model.
     */
    public SystemParameterModel() {
        this.logger.debug("Instantiating a model for a new user.");
    }



    public final void addTimeRange(final OrchestratorTimeRange newRange) {

        if (newRange == null) {
            throw new IllegalArgumentException("The time range to add cannot be null.");
        }

        this.schedulerRanges.add(newRange);
    }



    public final void removeTimeRange(final int rangeIndex) {
        this.schedulerRanges.remove(rangeIndex);
    }



    /**
     * Obtains whether the password of this user as defined in the model is the generic placeholder,
     * meaning that it has not been modified.
     *
     * @return <code>true</code> if the password has not been modified
     */
    public final boolean isPasswordGenericString() {
        return SystemParameterModel.PASSWORD_GENERIC_STRING.equals(this.smtpPassword);
    }



    /**
     * Create a domain object for a given application setting.
     *
     * @param key the string that identifies the application setting
     * @return the new data object
     */
    public final SystemParameter createDomainObject(final String key) {

        if (key == null) {
            throw new IllegalArgumentException("The key cannot be null.");
        }

        return new SystemParameter(key);
    }

}
