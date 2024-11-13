package ch.asit_asso.extract.unit.ldap;

import java.time.ZonedDateTime;
import ch.asit_asso.extract.ldap.LdapSettings;
import ch.asit_asso.extract.persistence.SystemParametersRepository;
import ch.asit_asso.extract.unit.MockEnabledTest;
import ch.asit_asso.extract.utils.Secrets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class LdapSettingsTest extends MockEnabledTest {

    public static final int SYNCHRONIZATION_FREQUENCY_HOURS = 2;

    private LdapSettings ldapSettings;

    @Mock
    private SystemParametersRepository repository;

    @Mock
    private Secrets secrets;

    @BeforeEach
    public void setUp() {
        Mockito.when(this.repository.getLdapBaseDn()).thenReturn("");
        Mockito.when(this.repository.getLdapServers()).thenReturn("");
        Mockito.when(this.repository.getLdapSynchronizationFrequency()).thenReturn("12");
        this.ldapSettings = new LdapSettings(this.repository, this.secrets);
    }

    @Test
    @DisplayName("Get next execution when LDAP is disabled")
    void nextExecutionTimeLdapDisabled() {
        this.ldapSettings.setEnabled(false);
        ZonedDateTime nextExecution = this.ldapSettings.getNextScheduledSynchronizationDate();
        assertNull(nextExecution);
    }



    @Test
    @DisplayName("Get next execution when LDAP synchronisation is disabled")
    void nextExecutionTimeLdapSynchroDisabled() {
        this.ldapSettings.setEnabled(true);
        this.ldapSettings.setSynchronizationEnabled(false);
        ZonedDateTime nextExecution = this.ldapSettings.getNextScheduledSynchronizationDate();
        assertNull(nextExecution);
    }



    @Test
    @DisplayName("Get next execution if never executed")
    void nextExecutionTimeIfNull() {
        this.ldapSettings.setEnabled(true);
        this.ldapSettings.setSynchronizationEnabled(true);
        this.ldapSettings.setSynchronizationFrequencyHours(LdapSettingsTest.SYNCHRONIZATION_FREQUENCY_HOURS);
        this.ldapSettings.setLastSynchronizationDate(null);
        ZonedDateTime beforeExercise = ZonedDateTime.now();
        ZonedDateTime nextExecution = this.ldapSettings.getNextScheduledSynchronizationDate();
        ZonedDateTime afterExercise = ZonedDateTime.now();
        assertNotNull(nextExecution);
        assertTrue(nextExecution.isAfter(beforeExercise) && nextExecution.isBefore(afterExercise),
                   "The execution date should be when the function is executed if there was no previous execution.");
    }



    @Test
    @DisplayName("Get next execution")
    void nextExecutionTime() {
        this.ldapSettings.setEnabled(true);
        this.ldapSettings.setSynchronizationEnabled(true);
        this.ldapSettings.setSynchronizationFrequencyHours(LdapSettingsTest.SYNCHRONIZATION_FREQUENCY_HOURS);
        ZonedDateTime lastExecution = ZonedDateTime.now();
        this.ldapSettings.setLastSynchronizationDate(lastExecution);
        ZonedDateTime nextExecution = this.ldapSettings.getNextScheduledSynchronizationDate();
        assertNotNull(nextExecution);
        assertEquals(lastExecution.plusHours(LdapSettingsTest.SYNCHRONIZATION_FREQUENCY_HOURS), nextExecution);
    }

    @Test
    @DisplayName("Should return false when LDAP is disabled")
    public void testLdapDisabled() {
        Mockito.when(this.repository.isLdapEnabled()).thenReturn("false");
        this.ldapSettings = new LdapSettings(this.repository, this.secrets);
        assertFalse(ldapSettings.isValid(), "LDAP should be invalid when disabled");
    }

    @Test
    @DisplayName("Should return false when no LDAP servers are configured")
    public void testServersNotConfigured() {
        Mockito.when(this.repository.isLdapEnabled()).thenReturn("true");
        Mockito.when(this.repository.getLdapServers()).thenReturn("");
        this.ldapSettings = new LdapSettings(this.repository, this.secrets);
        assertFalse(ldapSettings.isValid(), "LDAP should be invalid when no servers are configured");
    }

    @Test
    @DisplayName("Should return false when Base DN is not configured")
    public void testBaseDnNotConfigured() {
        Mockito.when(this.repository.isLdapEnabled()).thenReturn("true");
        Mockito.when(this.repository.getLdapServers()).thenReturn("ldap.example.com");
        Mockito.when(this.repository.getLdapBaseDn()).thenReturn("");
        this.ldapSettings = new LdapSettings(this.repository, this.secrets);
        assertFalse(ldapSettings.isValid(), "LDAP should be invalid when base DN is not configured");
    }

    @Test
    @DisplayName("Should return false when User Object Class is not configured")
    public void testUserObjectClassNotConfigured() {
        Mockito.when(this.repository.isLdapEnabled()).thenReturn("true");
        Mockito.when(this.repository.getLdapServers()).thenReturn("ldap.example.com");
        Mockito.when(this.repository.getLdapBaseDn()).thenReturn("dc=example,dc=com");
        this.ldapSettings = new LdapSettings(this.repository, this.secrets);
        ldapSettings.setUserObjectClass(""); // Empty userObjectClass
        assertFalse(ldapSettings.isValid(), "LDAP should be invalid when user object class is not configured");
    }

    @Test
    @DisplayName("Should return false when Username Attribute is not configured")
    public void testUserNameAttributeNotConfigured() {
        Mockito.when(this.repository.isLdapEnabled()).thenReturn("true");
        Mockito.when(this.repository.getLdapServers()).thenReturn("ldap.example.com");
        Mockito.when(this.repository.getLdapBaseDn()).thenReturn("dc=example,dc=com");
        this.ldapSettings = new LdapSettings(this.repository, this.secrets);
        ldapSettings.setUserObjectClass("inetOrgPerson");
        ldapSettings.setUserNameAttribute(""); // Empty userNameAttribute
        assertFalse(ldapSettings.isValid(), "LDAP should be invalid when username attribute is not configured");
    }

    @Test
    @DisplayName("Should return false when Login Attribute is not configured")
    public void testLoginAttributeNotConfigured() {
        Mockito.when(this.repository.isLdapEnabled()).thenReturn("true");
        Mockito.when(this.repository.getLdapServers()).thenReturn("ldap.example.com");
        Mockito.when(this.repository.getLdapBaseDn()).thenReturn("dc=example,dc=com");
        this.ldapSettings = new LdapSettings(this.repository, this.secrets);
        ldapSettings.setUserObjectClass("inetOrgPerson");
        ldapSettings.setUserNameAttribute("uid");
        ldapSettings.setLoginAttribute(""); // Empty loginAttribute
        assertFalse(ldapSettings.isValid(), "LDAP should be invalid when login attribute is not configured");
    }

    @Test
    @DisplayName("Should return false when Mail Attribute is not configured")
    public void testMailAttributeNotConfigured() {
        Mockito.when(this.repository.isLdapEnabled()).thenReturn("true");
        Mockito.when(this.repository.getLdapServers()).thenReturn("ldap.example.com");
        Mockito.when(this.repository.getLdapBaseDn()).thenReturn("dc=example,dc=com");
        this.ldapSettings = new LdapSettings(this.repository, this.secrets);
        ldapSettings.setUserObjectClass("inetOrgPerson");
        ldapSettings.setUserNameAttribute("uid");
        ldapSettings.setLoginAttribute("uid");
        ldapSettings.setMailAttribute(""); // Empty mailAttribute
        assertFalse(ldapSettings.isValid(), "LDAP should be invalid when mail attribute is not configured");
    }

    @Test
    @DisplayName("Should return false when Admins Group is not configured")
    public void testAdminsGroupNotConfigured() {
        Mockito.when(this.repository.isLdapEnabled()).thenReturn("true");
        Mockito.when(this.repository.getLdapServers()).thenReturn("ldap.example.com");
        Mockito.when(this.repository.getLdapBaseDn()).thenReturn("dc=example,dc=com");
        Mockito.when(this.repository.getLdapAdminsGroup()).thenReturn("");
        this.ldapSettings = new LdapSettings(this.repository, this.secrets);
        ldapSettings.setUserObjectClass("inetOrgPerson");
        ldapSettings.setUserNameAttribute("uid");
        ldapSettings.setLoginAttribute("uid");
        ldapSettings.setMailAttribute("mail");
        assertFalse(ldapSettings.isValid(), "LDAP should be invalid when admins group is not configured");
    }

    @Test
    @DisplayName("Should return false when Operators Group is not configured")
    public void testOperatorsGroupNotConfigured() {
        Mockito.when(this.repository.isLdapEnabled()).thenReturn("true");
        Mockito.when(this.repository.getLdapServers()).thenReturn("ldap.example.com");
        Mockito.when(this.repository.getLdapBaseDn()).thenReturn("dc=example,dc=com");
        Mockito.when(this.repository.getLdapAdminsGroup()).thenReturn("admins");
        Mockito.when(this.repository.getLdapOperatorsGroup()).thenReturn("");
        this.ldapSettings = new LdapSettings(this.repository, this.secrets);
        ldapSettings.setUserObjectClass("inetOrgPerson");
        ldapSettings.setUserNameAttribute("uid");
        ldapSettings.setLoginAttribute("uid");
        ldapSettings.setMailAttribute("mail");
        assertFalse(ldapSettings.isValid(), "LDAP should be invalid when operators group is not configured");
    }

    @Test
    @DisplayName("Should return false when Synchronization User is not configured but Synchronization is enabled")
    public void testSynchronizationUserNotConfigured() {
        Mockito.when(this.repository.isLdapEnabled()).thenReturn("true");
        Mockito.when(this.repository.getLdapServers()).thenReturn("ldap.example.com");
        Mockito.when(this.repository.getLdapBaseDn()).thenReturn("dc=example,dc=com");
        Mockito.when(this.repository.getLdapAdminsGroup()).thenReturn("admins");
        Mockito.when(this.repository.getLdapOperatorsGroup()).thenReturn("operators");
        Mockito.when(this.repository.getLdapSynchronizationUserName()).thenReturn("");
        Mockito.when(this.repository.isLdapSynchronizationEnabled()).thenReturn("true");
        this.ldapSettings = new LdapSettings(this.repository, this.secrets);
        ldapSettings.setUserObjectClass("inetOrgPerson");
        ldapSettings.setUserNameAttribute("uid");
        ldapSettings.setLoginAttribute("uid");
        ldapSettings.setMailAttribute("mail");
        assertFalse(ldapSettings.isValid(), "LDAP should be invalid when synchronization user is not configured");
    }

    @Test
    @DisplayName("Should return false when Encryption Type is not configured")
    public void testEncryptionTypeNotConfigured() {
        Mockito.when(this.repository.isLdapEnabled()).thenReturn("true");
        Mockito.when(this.repository.getLdapServers()).thenReturn("ldap.example.com");
        Mockito.when(this.repository.getLdapBaseDn()).thenReturn("dc=example,dc=com");
        Mockito.when(this.repository.getLdapAdminsGroup()).thenReturn("admins");
        Mockito.when(this.repository.getLdapOperatorsGroup()).thenReturn("operators");
        Mockito.when(this.repository.getLdapSynchronizationUserName()).thenReturn("test");
        Mockito.when(this.repository.isLdapSynchronizationEnabled()).thenReturn("false");
        Mockito.when(this.repository.getLdapEncryptionType()).thenReturn(null);
        this.ldapSettings = new LdapSettings(this.repository, this.secrets);
        ldapSettings.setUserObjectClass("inetOrgPerson");
        ldapSettings.setUserNameAttribute("uid");
        ldapSettings.setLoginAttribute("uid");
        ldapSettings.setMailAttribute("mail");
        assertFalse(ldapSettings.isValid(), "LDAP should be invalid when encryption type is not configured");
    }

    @Test
    @DisplayName("Should return true when all parameters are correctly set when sync is disabled")
    public void testLdapConfigurationSyncDisabledValid() {
        Mockito.when(this.repository.isLdapEnabled()).thenReturn("true");
        Mockito.when(this.repository.getLdapServers()).thenReturn("ldap.example.com");
        Mockito.when(this.repository.getLdapBaseDn()).thenReturn("dc=example,dc=com");
        Mockito.when(this.repository.getLdapAdminsGroup()).thenReturn("admins");
        Mockito.when(this.repository.getLdapOperatorsGroup()).thenReturn("operators");
        Mockito.when(this.repository.getLdapEncryptionType()).thenReturn("LDAPS");
        Mockito.when(this.repository.isLdapSynchronizationEnabled()).thenReturn("false");
        this.ldapSettings = new LdapSettings(this.repository, this.secrets);// not enabled
        ldapSettings.setUserObjectClass("inetOrgPerson");
        ldapSettings.setUserNameAttribute("uid");
        ldapSettings.setLoginAttribute("uid");
        ldapSettings.setMailAttribute("mail");
        assertTrue(ldapSettings.isValid(), "LDAP configuration should be valid when all parameters are set correctly");
    }

    @Test
    @DisplayName("Should return false when sync is enabled and username is not set")
    public void testLdapConfigurationSyncEnabledNoUserName() {
        Mockito.when(this.repository.isLdapEnabled()).thenReturn("true");
        Mockito.when(this.repository.getLdapServers()).thenReturn("ldap.example.com");
        Mockito.when(this.repository.getLdapBaseDn()).thenReturn("dc=example,dc=com");
        Mockito.when(this.repository.getLdapAdminsGroup()).thenReturn("admins");
        Mockito.when(this.repository.getLdapOperatorsGroup()).thenReturn("operators");
        Mockito.when(this.repository.getLdapSynchronizationUserName()).thenReturn("test");
        Mockito.when(this.repository.isLdapSynchronizationEnabled()).thenReturn("true");
        Mockito.when(this.repository.getLdapSynchronizationUserName()).thenReturn("");
        Mockito.when(this.repository.getLdapSynchronizationPassword()).thenReturn("");
        Mockito.when(this.repository.getLdapEncryptionType()).thenReturn("LDAPS");
        this.ldapSettings = new LdapSettings(this.repository, this.secrets);// not enabled
        ldapSettings.setUserObjectClass("inetOrgPerson");
        ldapSettings.setUserNameAttribute("uid");
        ldapSettings.setLoginAttribute("uid");
        ldapSettings.setMailAttribute("mail");
        assertFalse(ldapSettings.isValid(), "LDAP configuration should be valid when all parameters are set correctly");
    }

    @Test
    @DisplayName("Should return false when sync is enabled and password is not set")
    public void testLdapConfigurationSyncEnabledNoUserPassword() {
        Mockito.when(this.repository.isLdapEnabled()).thenReturn("true");
        Mockito.when(this.repository.getLdapServers()).thenReturn("ldap.example.com");
        Mockito.when(this.repository.getLdapBaseDn()).thenReturn("dc=example,dc=com");
        Mockito.when(this.repository.getLdapAdminsGroup()).thenReturn("admins");
        Mockito.when(this.repository.getLdapOperatorsGroup()).thenReturn("operators");
        Mockito.when(this.repository.getLdapSynchronizationUserName()).thenReturn("test");
        Mockito.when(this.repository.isLdapSynchronizationEnabled()).thenReturn("true");
        Mockito.when(this.repository.getLdapSynchronizationUserName()).thenReturn("test");
        Mockito.when(this.repository.getLdapSynchronizationPassword()).thenReturn("");
        Mockito.when(this.repository.getLdapEncryptionType()).thenReturn("LDAPS");
        this.ldapSettings = new LdapSettings(this.repository, this.secrets);// not enabled
        ldapSettings.setUserObjectClass("inetOrgPerson");
        ldapSettings.setUserNameAttribute("uid");
        ldapSettings.setLoginAttribute("uid");
        ldapSettings.setMailAttribute("mail");
        assertFalse(ldapSettings.isValid(), "LDAP configuration should be valid when all parameters are set correctly");
    }

    @Test
    @DisplayName("Should return false when sync is enabled and frequency is negative")
    public void testLdapConfigurationSyncEnabledFrequencyInvalid() {
        Mockito.when(this.repository.isLdapEnabled()).thenReturn("true");
        Mockito.when(this.repository.getLdapServers()).thenReturn("ldap.example.com");
        Mockito.when(this.repository.getLdapBaseDn()).thenReturn("dc=example,dc=com");
        Mockito.when(this.repository.getLdapAdminsGroup()).thenReturn("admins");
        Mockito.when(this.repository.getLdapOperatorsGroup()).thenReturn("operators");
        Mockito.when(this.repository.getLdapSynchronizationUserName()).thenReturn("test");
        Mockito.when(this.repository.isLdapSynchronizationEnabled()).thenReturn("true");
        Mockito.when(this.repository.getLdapSynchronizationUserName()).thenReturn("test");
        Mockito.when(this.repository.getLdapSynchronizationPassword()).thenReturn("");
        Mockito.when(this.repository.getLdapSynchronizationFrequency()).thenReturn("-14");
        Mockito.when(this.repository.getLdapEncryptionType()).thenReturn("LDAPS");
        this.ldapSettings = new LdapSettings(this.repository, this.secrets);// not enabled
        ldapSettings.setUserObjectClass("inetOrgPerson");
        ldapSettings.setUserNameAttribute("uid");
        ldapSettings.setLoginAttribute("uid");
        ldapSettings.setMailAttribute("mail");
        assertFalse(ldapSettings.isValid(), "LDAP configuration should be valid when all parameters are set correctly");
    }

    @Test
    @DisplayName("Should return false when sync is enabled and frequency is not a number")
    public void testLdapConfigurationSyncEnabledFrequencyInvalidText() {
        Mockito.when(this.repository.isLdapEnabled()).thenReturn("true");
        Mockito.when(this.repository.getLdapServers()).thenReturn("ldap.example.com");
        Mockito.when(this.repository.getLdapBaseDn()).thenReturn("dc=example,dc=com");
        Mockito.when(this.repository.getLdapAdminsGroup()).thenReturn("admins");
        Mockito.when(this.repository.getLdapOperatorsGroup()).thenReturn("operators");
        Mockito.when(this.repository.getLdapSynchronizationUserName()).thenReturn("test");
        Mockito.when(this.repository.isLdapSynchronizationEnabled()).thenReturn("true");
        Mockito.when(this.repository.getLdapSynchronizationUserName()).thenReturn("test");
        Mockito.when(this.repository.getLdapSynchronizationPassword()).thenReturn("test");
        Mockito.when(this.repository.getLdapSynchronizationFrequency()).thenReturn("test");
        Mockito.when(this.repository.getLdapEncryptionType()).thenReturn("LDAPS");
        this.ldapSettings = new LdapSettings(this.repository, this.secrets);// not enabled
        ldapSettings.setUserObjectClass("inetOrgPerson");
        ldapSettings.setUserNameAttribute("uid");
        ldapSettings.setLoginAttribute("uid");
        ldapSettings.setMailAttribute("mail");
        assertFalse(ldapSettings.isValid(), "LDAP configuration should be valid when all parameters are set correctly");
    }

    @Test
    @DisplayName("Should return true when all parameters are correctly set when sync is enabled")
    public void testLdapConfigurationSyncEnabledValid() {
        Mockito.when(this.repository.isLdapEnabled()).thenReturn("true");
        Mockito.when(this.repository.getLdapServers()).thenReturn("ldap.example.com");
        Mockito.when(this.repository.getLdapBaseDn()).thenReturn("dc=example,dc=com");
        Mockito.when(this.repository.getLdapAdminsGroup()).thenReturn("admins");
        Mockito.when(this.repository.getLdapOperatorsGroup()).thenReturn("operators");
        Mockito.when(this.repository.getLdapSynchronizationUserName()).thenReturn("test");
        Mockito.when(this.repository.isLdapSynchronizationEnabled()).thenReturn("true");
        Mockito.when(this.repository.getLdapSynchronizationUserName()).thenReturn("test");
        Mockito.when(this.repository.getLdapSynchronizationPassword()).thenReturn("test");
        Mockito.when(this.repository.getLdapEncryptionType()).thenReturn("LDAPS");
        this.ldapSettings = new LdapSettings(this.repository, this.secrets);// not enabled
        ldapSettings.setUserObjectClass("inetOrgPerson");
        ldapSettings.setUserNameAttribute("uid");
        ldapSettings.setLoginAttribute("uid");
        ldapSettings.setMailAttribute("mail");
        assertTrue(ldapSettings.isValid(), "LDAP configuration should be valid when all parameters are set correctly");
    }
}
