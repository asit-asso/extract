package ch.asit_asso.extract.orchestrator.schedulers;

import ch.asit_asso.extract.ldap.LdapSettings;
import ch.asit_asso.extract.orchestrator.OrchestratorSettings;
import ch.asit_asso.extract.orchestrator.runners.LdapSynchronizationJobRunner;
import ch.asit_asso.extract.orchestrator.triggers.LdapSynchronizationTrigger;
import ch.asit_asso.extract.persistence.ApplicationRepositories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.config.TriggerTask;

public class ManagementTasksScheduler extends JobScheduler {

    private ScheduledTask ldapSynchronizationTask;

    private final ApplicationRepositories applicationRepositories;

    private final LdapSettings ldapSettings;

    private final Logger logger = LoggerFactory.getLogger(ManagementTasksScheduler.class);

    /**
     * Creates a new instance of this scheduler.
     *
     * @param registrar the object that allows to repeatedly execute tasks at a given interval
     */
    public ManagementTasksScheduler(ScheduledTaskRegistrar registrar, ApplicationRepositories applicationRepositories,
                                    LdapSettings ldapSettings, OrchestratorSettings orchestratorSettings) {

        super(registrar);

        this.applicationRepositories = applicationRepositories;
        this.ldapSettings = ldapSettings;
        this.setSchedulingStep(orchestratorSettings.getFrequency());
    }



    @Override
    public void scheduleJobs() {
        this.ldapSettings.refresh();

        if (!this.ldapSettings.isEnabled() || !this.ldapSettings.isSynchronizationEnabled()) {
            this.logger.info("The LDAP synchronization is disabled, so the job won't be scheduled.");
            return;
        }

        this.scheduleLdapSynchronization();
    }



    @Override
    public void unscheduleJobs() {
        this.unscheduleLdapSynchronization();
    }



    private void scheduleLdapSynchronization() {
        this.logger.debug("Scheduling the LDAP users synchronization.");
        final var synchronizationRunner = new LdapSynchronizationJobRunner(this.applicationRepositories, this.ldapSettings);
        final var synchronizationTrigger = new LdapSynchronizationTrigger(this.ldapSettings);
        final var triggerTask = new TriggerTask(synchronizationRunner, synchronizationTrigger);
        this.ldapSynchronizationTask = this.getTaskRegistrar().scheduleTriggerTask(triggerTask);
        this.logger.debug("The LDAP synchronization has been scheduled with a trigger.");
    }



    private void unscheduleLdapSynchronization() {
        this.logger.debug("Unscheduling the LDAP users synchronization job.");

        if (this.ldapSynchronizationTask == null) {
            this.logger.debug("The LDAP synchronization job is not scheduled, so nothing done.");
            return;
        }

        this.ldapSynchronizationTask.cancel();
        this.logger.debug("The LDAP synchronization job has been unscheduled.");
    }
}
