package ch.asit_asso.extract.ldap;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.validation.constraints.NotNull;
import ch.asit_asso.extract.persistence.SystemParametersRepository;
import ch.asit_asso.extract.utils.Secrets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdapSettings {

    public static final String DEFAULT_STARTTLS_PORT = "389";
    public static final String DEFAULT_LDAPS_PORT = "636";
    private String adminsGroup;

    private String[] baseDn;

    private boolean enabled;

    private EncryptionType encryptionType;

    private ZonedDateTime lastSynchronizationDate;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(LdapSettings.class);

    private String loginAttribute;

    private String mailAttribute;

    private String operatorsGroup;

    private final Secrets secrets;

    private String[] servers;

    private boolean synchronizationEnabled;

    private int synchronizationFrequencyHours;

    private String synchronizationPassword;

    private String synchronizationUserName;

    private String userNameAttribute;

    private String userObjectClass;

    /**
     * The Spring Data object that links the general parameters of the application with the data source.
     */
    private final SystemParametersRepository systemParametersRepository;

    public enum EncryptionType {
        LDAPS,
        STARTTLS
    }

    public String getAdminsGroup() {
        return this.adminsGroup;
    }

    public void setAdminsGroup(String adminsGroup) {
        this.adminsGroup = adminsGroup;
    }

    public String[] getBaseDn() {
        return this.baseDn;
    }

    public void setBaseDn(@NotNull String baseDnString) {
        List<String> baseDnList = new ArrayList<>();

        for (String baseDn : baseDnString.split(";")) {
            baseDnList.add(baseDn.trim());
        }

        this.baseDn = baseDnList.toArray(String[]::new);
    }

    public void setBaseDn(String[] baseDn) {
        this.baseDn = baseDn;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public EncryptionType getEncryptionType() {
        return this.encryptionType;
    }

    public void setEncryptionType(EncryptionType encryptionType) {
        this.encryptionType = encryptionType;
    }

    public ZonedDateTime getLastSynchronizationDate() {
        return this.lastSynchronizationDate;
    }

    public void setLastSynchronizationDate(ZonedDateTime lastSynchronizationDate) {
        this.lastSynchronizationDate = lastSynchronizationDate;
    }

    public String getLoginAttribute() {
        return this.loginAttribute;
    }

    public void setLoginAttribute(String loginAttribute) {
        this.loginAttribute = loginAttribute;
    }

    public String getMailAttribute() {
        return this.mailAttribute;
    }

    public void setMailAttribute(String mailAttribute) {
        this.mailAttribute = mailAttribute;
    }

    public ZonedDateTime getNextScheduledSynchronizationDate() {

        if (!this.isEnabled() || !this.isSynchronizationEnabled()) {
            return null;
        }

        if (this.getLastSynchronizationDate() == null) {
            return ZonedDateTime.now();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        ZonedDateTime nextExecution = this.getLastSynchronizationDate().plusHours(this.getSynchronizationFrequencyHours());
        return nextExecution;
    }

    public String getUserNameAttribute() {
        return this.userNameAttribute;
    }

    public void setUserNameAttribute(String userNameAttribute) {
        this.userNameAttribute = userNameAttribute;
    }

    public String getOperatorsGroup() {
        return this.operatorsGroup;
    }

    public void setOperatorsGroup(String operatorsGroup) {
        this.operatorsGroup = operatorsGroup;
    }

    public String[] getServers() {
        return this.servers;
    }


    public void setServers(@NotNull String serversString) {
        List<String> serversList = new ArrayList<>();

        for (String baseDn : serversString.split(";")) {
            serversList.add(baseDn.trim());
        }

        this.servers = serversList.toArray(String[]::new);
    }

    public void setServers(String[] servers) {
        this.servers = servers;
    }

    public boolean isSynchronizationEnabled() {
        return this.synchronizationEnabled;
    }

    public void setSynchronizationEnabled(boolean synchronizationEnabled) {
        this.synchronizationEnabled = synchronizationEnabled;
    }

    public int getSynchronizationFrequencyHours() {
        return this.synchronizationFrequencyHours;
    }

    public void setSynchronizationFrequencyHours(int synchronizationFrequencyHours) {
        this.synchronizationFrequencyHours = synchronizationFrequencyHours;
    }

    public String getSynchronizationPassword() {
        return this.secrets.decrypt(this.synchronizationPassword);
    }

    public void setSynchronizationPassword(String synchronizationPassword) {
        this.synchronizationPassword = synchronizationPassword;
    }

    public String getSynchronizationUserName() {
        return this.synchronizationUserName;
    }

    public void setSynchronizationUserName(String synchronizationUserName) {
        this.synchronizationUserName = synchronizationUserName;
    }

    public String getUserObjectClass() {
        return this.userObjectClass;
    }

    public void setUserObjectClass(String objectClass) {
        this.userObjectClass = objectClass;
    }


    public LdapSettings(final SystemParametersRepository repository, final Secrets secrets) {

        if (repository == null) {
            throw new IllegalArgumentException("The system parameters repository cannot be null.");
        }

        if (secrets == null) {
            throw new IllegalArgumentException("The password utility bean cannot be null.");
        }

        this.systemParametersRepository = repository;
        this.secrets = secrets;
        this.setSettingsFromDataSource();
    }


    /**
     * Determines if the current LDAP configuration data is correctly defined.
     *
     * @return <code>true</code> if the configuration is valid
     */
    public final boolean isValid() {
        if (!this.isEnabled()) {
            this.logger.warn("LDAP is disabled");
            return false;
        }

        if (this.servers == null || this.servers.length == 0) {
            this.logger.error("No LDAP servers configured.");
            return false;
        }

        if (this.baseDn == null || this.baseDn.length == 0) {
            this.logger.error("No LDAP base DN configured.");
            return false;
        }

        if (this.userObjectClass == null || this.userObjectClass.isEmpty()) {
            this.logger.error("No LDAP user object class configured.");
            return false;
        }

        if (this.userNameAttribute == null || this.userNameAttribute.isEmpty()) {
            this.logger.error("No LDAP user name attribute configured.");
            return false;
        }

        if (this.loginAttribute == null || this.loginAttribute.isEmpty()) {
            this.logger.error("No LDAP login attribute configured.");
            return false;
        }

        if (this.mailAttribute == null || this.mailAttribute.isEmpty()) {
            this.logger.error("No LDAP mail attribute configured.");
            return false;
        }

        if (this.adminsGroup == null || this.adminsGroup.isEmpty()) {
            this.logger.error("No LDAP admins group configured.");
            return false;
        }

        if (this.synchronizationEnabled) {
            if (this.synchronizationUserName == null || this.synchronizationUserName.isEmpty()) {
                this.logger.error("No LDAP synchronization username configured.");
                return false;
            }

            if (this.synchronizationPassword == null || this.synchronizationPassword.isEmpty()) {
                this.logger.error("No LDAP synchronization password configured.");
                return false;
            }

            if (this.synchronizationFrequencyHours <= 0) {
                this.logger.error("No LDAP synchronization frequency hours configured.");
                return false;
            }
        }

        if (this.encryptionType == null) {
            this.logger.error("No LDAP encryption type configured.");
            return false;
        }

        logger.info("LDAP configuration is valid.");

        return true;
    }



    public void refresh() {
        this.logger.debug("Reloading the LDAP settings from the data source.");
        this.setSettingsFromDataSource();
    }


    /**
     * Defines the e-mail parameters from what is currently set in the data source.
     */
    private void setSettingsFromDataSource() {
        this.logger.debug("Defining the LDAP configuration from the data source.");
        this.setAdminsGroup(this.systemParametersRepository.getLdapAdminsGroup());
        this.setBaseDn(this.systemParametersRepository.getLdapBaseDn());
        final String rawEnabledValue = this.systemParametersRepository.isLdapEnabled();
        this.setEnabled(Boolean.parseBoolean(rawEnabledValue));
        this.setServers(this.systemParametersRepository.getLdapServers());
        final String rawEncryptionValue = this.systemParametersRepository.getLdapEncryptionType();

        if (rawEncryptionValue != null) {
            this.setEncryptionType(LdapSettings.EncryptionType.valueOf(rawEncryptionValue));
        }

        final String rawLastSynchronizationValue = this.systemParametersRepository.getLdapLastSynchronizationDate();

        if (rawLastSynchronizationValue != null) {

            try {
                this.setLastSynchronizationDate(ZonedDateTime.parse(rawLastSynchronizationValue));

            } catch (DateTimeParseException synchronizationDateParseException) {
                this.logger.error("The last synchronization date in the data source is not a valid ISO date string.");
            }
        }

        this.setOperatorsGroup(this.systemParametersRepository.getLdapOperatorsGroup());

        final String rawSynchronizationEnabledValue = this.systemParametersRepository.isLdapSynchronizationEnabled();

        if (rawSynchronizationEnabledValue != null) {
            this.setSynchronizationEnabled(Boolean.parseBoolean(rawSynchronizationEnabledValue));
        }

        final String rawSynchronizationFrequencyValue = this.systemParametersRepository.getLdapSynchronizationFrequency();

        try {
            this.setSynchronizationFrequencyHours(Integer.parseInt(rawSynchronizationFrequencyValue));

        } catch (NumberFormatException exception) {
            this.logger.error("The LDAP synchronization frequency in the data source is not a valid integer.");
        }

        this.setSynchronizationPassword(this.systemParametersRepository.getLdapSynchronizationPassword());
        this.setSynchronizationUserName(this.systemParametersRepository.getLdapSynchronizationUserName());

        if (!this.isValid()) {
            this.logger.warn("The LDAP parameters in the data source are not valid. Please check the configuration.");
        }
    }
}
