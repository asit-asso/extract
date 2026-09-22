package ch.asit_asso.extract.services;

import java.util.HashMap;
import ch.asit_asso.extract.connectors.common.IConnector;
import ch.asit_asso.extract.connectors.implementation.ConnectorDiscovererWrapper;
import ch.asit_asso.extract.domain.Connector;
import ch.asit_asso.extract.domain.SystemParameter;
import ch.asit_asso.extract.domain.Task;
import ch.asit_asso.extract.persistence.ApplicationRepositories;
import ch.asit_asso.extract.persistence.SystemParametersRepository;
import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import ch.asit_asso.extract.plugins.implementation.TaskProcessorDiscovererWrapper;
import ch.asit_asso.extract.utils.Secrets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Upgrades legacy clear-text secrets once the application has started.
 */
@Service
public class SecretMigrationService {

    private static final String[] SYSTEM_SECRET_KEYS = {
        SystemParametersRepository.LDAP_PASSWORD_KEY,
        SystemParametersRepository.SMTP_PASSWORD_KEY
    };

    private final ApplicationRepositories repositories;

    private final ConnectorDiscovererWrapper connectorDiscoverer;

    private final Logger logger = LoggerFactory.getLogger(SecretMigrationService.class);

    private final SecretParameters secretParameters;

    private final Secrets secrets;

    private final TaskProcessorDiscovererWrapper taskProcessorDiscoverer;

    public SecretMigrationService(final ApplicationRepositories applicationRepositories,
                                  final ConnectorDiscovererWrapper connectors,
                                  final SecretParameters parameters,
                                  final Secrets secretsUtility,
                                  final TaskProcessorDiscovererWrapper taskProcessors) {
        this.repositories = applicationRepositories;
        this.connectorDiscoverer = connectors;
        this.secretParameters = parameters;
        this.secrets = secretsUtility;
        this.taskProcessorDiscoverer = taskProcessors;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void migrate() {
        this.migrateSystemParameters();
        this.migrateTasks();
        this.migrateConnectors();
    }

    private void migrateSystemParameters() {
        for (String key : SYSTEM_SECRET_KEYS) {
            SystemParameter parameter = this.repositories.getParametersRepository().findByKey(key);
            if (parameter == null || Secrets.isBlank(parameter.getValue())) {
                continue;
            }

            String encryptedValue = this.secrets.encryptIfNeeded(parameter.getValue());
            if (!parameter.getValue().equals(encryptedValue)) {
                parameter.setValue(encryptedValue);
                this.repositories.getParametersRepository().save(parameter);
                this.logger.info("Migrated the {} application secret to encrypted storage.", key);
            }
        }
    }

    private void migrateTasks() {
        for (Task task : this.repositories.getTasksRepository().findAll()) {
            ITaskProcessor plugin = this.taskProcessorDiscoverer.getTaskProcessor(task.getCode());
            if (plugin == null) {
                continue;
            }
            HashMap<String, String> current = task.getParametersValues();
            if (current == null) {
                continue;
            }
            HashMap<String, String> encrypted = this.secretParameters.encrypt(current, plugin.getParams());
            if (!current.equals(encrypted)) {
                task.setParametersValues(encrypted);
                this.repositories.getTasksRepository().save(task);
            }
        }
    }

    private void migrateConnectors() {
        for (Connector connector : this.repositories.getConnectorsRepository().findAll()) {
            IConnector plugin = this.connectorDiscoverer.getConnector(connector.getConnectorCode());
            if (plugin == null) {
                continue;
            }
            HashMap<String, String> current = connector.getConnectorParametersValues();
            if (current == null) {
                continue;
            }
            HashMap<String, String> encrypted = this.secretParameters.encrypt(current, plugin.getParams());
            if (!current.equals(encrypted)) {
                connector.setConnectorParametersValues(encrypted);
                this.repositories.getConnectorsRepository().save(connector);
            }
        }
    }
}