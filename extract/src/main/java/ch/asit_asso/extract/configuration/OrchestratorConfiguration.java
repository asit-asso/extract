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
package ch.asit_asso.extract.configuration;

import ch.asit_asso.extract.connectors.implementation.ConnectorDiscovererWrapper;
import ch.asit_asso.extract.email.EmailSettings;
import ch.asit_asso.extract.ldap.LdapSettings;
import ch.asit_asso.extract.orchestrator.Orchestrator;
import ch.asit_asso.extract.orchestrator.OrchestratorSettings;
import ch.asit_asso.extract.persistence.ApplicationRepositories;
import ch.asit_asso.extract.persistence.SystemParametersRepository;
import ch.asit_asso.extract.plugins.implementation.TaskProcessorDiscovererWrapper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;



/**
 * The background work of the application.
 *
 * @author Yves Grasset
 */
@Configuration
@EnableBatchProcessing
@EnableScheduling
@DependsOn("applicationInitializer")
public class OrchestratorConfiguration implements SchedulingConfigurer {

    /**
     * The ISO code of the language to use for the user interface.
     */
    @Value("${extract.i18n.language}")
    private String applicationLanguage;

    /**
     * An ensemble of objects that link the data objects to the data source.
     */
    private final ApplicationRepositories applicationRepositories;

    /**
     * The access to the available connector plugins.
     */
    private final ConnectorDiscovererWrapper connectorsDiscoverer;

    /**
     * The object that assembles the configuration objects required to create and send e-mail messages.
     */
    private final EmailSettings emailSettings;

    private final LdapSettings ldapSettings;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(OrchestratorConfiguration.class);

    /**
     * The access to the available task plugins.
     */
    private final TaskProcessorDiscovererWrapper taskPluginDiscoverer;

    /**
     * The Spring Data object that links the application parameters with the data source.
     */
    private final SystemParametersRepository systemParametersRepository;



    public OrchestratorConfiguration(ApplicationRepositories repositories,
                                     ConnectorDiscovererWrapper connectorsDiscoverer,
                                     TaskProcessorDiscovererWrapper taskPluginDiscoverer, EmailSettings emailSettings,
                                     LdapSettings ldapSettings, SystemParametersRepository parametersRepository) {
        this.applicationRepositories = repositories;
        this.connectorsDiscoverer = connectorsDiscoverer;
        this.emailSettings = emailSettings;
        this.ldapSettings = ldapSettings;
        this.taskPluginDiscoverer = taskPluginDiscoverer;
        this.systemParametersRepository = parametersRepository;
    }


    /**
     * Instantiates and starts the various scheduler that are responsible for running the background
     * processes.
     *
     * @param taskRegistrar the object used to keep track of the batch processes.
     */
    @Override
    public final void configureTasks(final @NotNull ScheduledTaskRegistrar taskRegistrar) {
        this.logger.debug("Getting an instance of the orchestrator.");
        Orchestrator orchestrator = Orchestrator.getInstance();

        if (!orchestrator.initializeComponents(taskRegistrar, this.applicationLanguage, this.applicationRepositories,
                                               this.connectorsDiscoverer, this.taskPluginDiscoverer, this.emailSettings,
                                               this.ldapSettings, new OrchestratorSettings(this.systemParametersRepository))) {
            this.logger.error("The background tasks are not scheduled because it was impossible to properly initialize"
                    + " the orchestrator.");
            return;
        }

        orchestrator.scheduleMonitoringByWorkingState();
    }

}
