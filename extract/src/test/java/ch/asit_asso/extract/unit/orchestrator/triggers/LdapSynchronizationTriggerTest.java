package ch.asit_asso.extract.unit.orchestrator.triggers;

import java.time.ZonedDateTime;
import java.util.Date;
import ch.asit_asso.extract.ldap.LdapSettings;
import ch.asit_asso.extract.orchestrator.triggers.LdapSynchronizationTrigger;
import ch.asit_asso.extract.unit.MockEnabledTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.scheduling.support.SimpleTriggerContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

class LdapSynchronizationTriggerTest extends MockEnabledTest {

    public static final int SYNCHRONIZATION_FREQUENCY_HOURS = 2;

    @Mock
    private LdapSettings ldapSettings;


    @BeforeEach
    void setUp() {
        Mockito.doCallRealMethod().when(this.ldapSettings).getLastSynchronizationDate();
        Mockito.doCallRealMethod().when(this.ldapSettings).getNextScheduledSynchronizationDate();
        Mockito.doCallRealMethod().when(this.ldapSettings).getSynchronizationFrequencyHours();
        Mockito.doCallRealMethod().when(this.ldapSettings).isEnabled();
        Mockito.doCallRealMethod().when(this.ldapSettings).isSynchronizationEnabled();
        Mockito.doCallRealMethod().when(this.ldapSettings).setEnabled(anyBoolean());
        Mockito.doCallRealMethod().when(this.ldapSettings).setLastSynchronizationDate(any(ZonedDateTime.class));
        Mockito.doCallRealMethod().when(this.ldapSettings).setSynchronizationEnabled(anyBoolean());
        Mockito.doCallRealMethod().when(this.ldapSettings).setSynchronizationFrequencyHours(anyInt());
    }



    @Test
    @DisplayName("Get next execution when LDAP is disabled")
    void nextExecutionTimeLdapDisabled() {
        this.ldapSettings.setEnabled(false);
        LdapSynchronizationTrigger ldapSynchronizationTrigger = new LdapSynchronizationTrigger(this.ldapSettings);
        Date nextExecution = ldapSynchronizationTrigger.nextExecutionTime(new SimpleTriggerContext());
        verify(this.ldapSettings, atLeastOnce()).refresh();
        assertNull(nextExecution);
    }



    @Test
    @DisplayName("Get next execution when LDAP synchronisation is disabled")
    void nextExecutionTimeLdapSynchroDisabled() {
        this.ldapSettings.setEnabled(true);
        this.ldapSettings.setSynchronizationEnabled(false);
        LdapSynchronizationTrigger ldapSynchronizationTrigger = new LdapSynchronizationTrigger(this.ldapSettings);
        Date nextExecution = ldapSynchronizationTrigger.nextExecutionTime(new SimpleTriggerContext());
        verify(this.ldapSettings, atLeastOnce()).refresh();
        assertNull(nextExecution);
    }



    @Test
    @DisplayName("Get next execution")
    void nextExecutionTime() {
        this.ldapSettings.setEnabled(true);
        this.ldapSettings.setSynchronizationEnabled(true);
        this.ldapSettings.setSynchronizationFrequencyHours(
                LdapSynchronizationTriggerTest.SYNCHRONIZATION_FREQUENCY_HOURS);
        ZonedDateTime lastExecution = ZonedDateTime.now();
        this.ldapSettings.setLastSynchronizationDate(lastExecution);
        Date expectedDate =
                Date.from(lastExecution.plusHours(LdapSynchronizationTriggerTest.SYNCHRONIZATION_FREQUENCY_HOURS)
                                       .toInstant());
        LdapSynchronizationTrigger ldapSynchronizationTrigger = new LdapSynchronizationTrigger(this.ldapSettings);
        Date nextExecution = ldapSynchronizationTrigger.nextExecutionTime(new SimpleTriggerContext());

        verify(this.ldapSettings, atLeastOnce()).refresh();
        assertNotNull(nextExecution);
        assertEquals(expectedDate, nextExecution);
    }
}
