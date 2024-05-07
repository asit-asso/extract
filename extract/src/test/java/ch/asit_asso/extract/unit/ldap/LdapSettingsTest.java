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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
