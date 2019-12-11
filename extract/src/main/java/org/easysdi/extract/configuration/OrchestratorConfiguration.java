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
package org.easysdi.extract.configuration;

import org.easysdi.extract.connectors.ConnectorDiscovererWrapper;
import org.easysdi.extract.email.EmailSettings;
import org.easysdi.extract.initializers.ApplicationInitializer;
import org.easysdi.extract.orchestrator.Orchestrator;
import org.easysdi.extract.orchestrator.OrchestratorSettings;
import org.easysdi.extract.persistence.ApplicationRepositories;
import org.easysdi.extract.persistence.SystemParametersRepository;
import org.easysdi.extract.plugins.TaskProcessorDiscovererWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
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
public class OrchestratorConfiguration implements SchedulingConfigurer {

    /**
     * An object that ensure that this application is ready to be run.
     */
    @Autowired
    private ApplicationInitializer applicationInitializer;

    /**
     * The ISO code of the language to use for the user interface.
     */
    @Value("${extract.i18n.language}")
    private String applicationLanguage;

    /**
     * An ensemble of objects that link the data objects to the data source.
     */
    @Autowired
    private ApplicationRepositories applicationRepositories;

    /**
     * The access to the available connector plugins.
     */
    @Autowired
    private ConnectorDiscovererWrapper connectorsDiscoverer;

    /**
     * The object that assembles the configuration objects required to create and send e-mail messages.
     */
    @Autowired
    private EmailSettings emailSettings;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(OrchestratorConfiguration.class);

    /**
     * The object that allows to run batch processes at a given interval.
     */
    private ScheduledTaskRegistrar scheduledTaskRegistrar;

    /**
     * The access to the available task plugins.
     */
    @Autowired
    private TaskProcessorDiscovererWrapper taskPluginDiscoverer;

    /**
     * The Spring Data object that links the application parameters with the data source.
     */
    @Autowired
    private SystemParametersRepository systemParametersRepository;



//    @Bean
//    public OrchestratorSettings orchestratorSettings() {
//        return new OrchestratorSettings(this.systemParametersRepository);
//    }
    /**
     * Instantiates and starts the various scheduler that are responsible for running the background
     * processes.
     *
     * @param taskRegistrar the object used to keep track of the batch processes.
     */
    @Override
    public final void configureTasks(final ScheduledTaskRegistrar taskRegistrar) {
        this.applicationInitializer.ensureInitialized();
        this.scheduledTaskRegistrar = taskRegistrar;
        this.logger.debug("Getting an instance of the orchestrator.");
        Orchestrator orchestrator = Orchestrator.getInstance();

        if (!orchestrator.initializeComponents(this.scheduledTaskRegistrar, this.applicationLanguage,
                this.applicationRepositories, this.connectorsDiscoverer, this.taskPluginDiscoverer,
                this.emailSettings, new OrchestratorSettings(this.systemParametersRepository))) {
            this.logger.error("The background tasks are not scheduled because it was impossible to properly initialize"
                    + " the orchestrator.");
            return;
        }

        orchestrator.scheduleMonitoringByWorkingState();
    }

}
