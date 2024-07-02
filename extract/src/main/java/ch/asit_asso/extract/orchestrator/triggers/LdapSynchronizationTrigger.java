package ch.asit_asso.extract.orchestrator.triggers;

import java.util.Date;
import javax.validation.constraints.NotNull;
import ch.asit_asso.extract.ldap.LdapSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;

public class LdapSynchronizationTrigger implements Trigger {

    private final Logger logger = LoggerFactory.getLogger(LdapSynchronizationTrigger.class);

    private final LdapSettings settings;


    public LdapSynchronizationTrigger(@NotNull LdapSettings ldapSettings) {
        this.settings = ldapSettings;
    }


    @Override
    public Date nextExecutionTime(@NotNull TriggerContext triggerContext) {
        this.logger.debug("Getting the next execution time for the LDAP synchronization.");
        this.settings.refresh();

        if (!this.settings.isEnabled()) {
            this.logger.warn("The orchestrator tried to obtain the next synchronization time, but LDAP is turned off.");
            return null;
        }

        if (!this.settings.isSynchronizationEnabled()) {
            this.logger.warn("The orchestrator tried to obtain the next synchronization time, but LDAP synchronization"
                             + " is turned off.");
            return null;
        }

        return Date.from(this.settings.getNextScheduledSynchronizationDate().toInstant());
    }
}
