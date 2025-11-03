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
package ch.asit_asso.extract.batch.reader;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import ch.asit_asso.extract.connectors.common.IConnector;
import ch.asit_asso.extract.connectors.common.IConnectorImportResult;
import ch.asit_asso.extract.connectors.common.IProduct;
import ch.asit_asso.extract.domain.Connector;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.email.ConnectorImportFailedEmail;
import ch.asit_asso.extract.email.EmailSettings;
import ch.asit_asso.extract.email.LocaleUtils;
import ch.asit_asso.extract.persistence.ConnectorsRepository;
import ch.asit_asso.extract.persistence.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.context.annotation.Scope;



/**
 * Batch step that will fetch the orders through a connector.
 *
 * @author Yves Grasset
 */
@Scope("step")
public class ConnectorImportReader implements ItemReader<IProduct> {

    /**
     * The number that identifies the instance containing the connector parameters.
     */
    private final int connectorId;

    /**
     * The plugin to use to fetch the orders.
     */
    private final IConnector connectorPluginInstance;

    /**
     * The Spring Data object that links the connector data objects with the data source.
     */
    private final ConnectorsRepository connectorsRepository;

    /**
     * The objects required to create and send an e-mail message.
     */
    private final EmailSettings emailSettings;

    /**
     * The locale of the language that the application displays messages in.
     */
    private final String language;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(ConnectorImportReader.class);

    /**
     * A FIFO collection of the fetched orders.
     */
    private Queue<IProduct> productsQueue;

    /**
     * The Spring Data object that links the user data objects with the data source.
     */
    private UsersRepository usersRepository;



    /**
     * Creates a new instance of this order reader.
     *
     * @param connectorIdentifier the number that identifies the instance containing the connector parameters
     * @param connectorPlugin     an instance of the connector plugin to use to fetch the orders
     * @param connectorsRepo      the Spring Data object that links the connector data objects with the data source
     * @param usersRepo           the Spring Data object that links the user data objects with the data source
     * @param smtpSettings        an object that assembles the objects required to create and send an e-mail message
     * @param applicationLanguage the locale code of the language used by the application to display messages
     */
    public ConnectorImportReader(final int connectorIdentifier, final IConnector connectorPlugin,
            final ConnectorsRepository connectorsRepo, final UsersRepository usersRepo,
            final EmailSettings smtpSettings, final String applicationLanguage) {

        if (connectorIdentifier < 1) {
            throw new IllegalArgumentException("The connector identifier must be greater than 0.");
        }

        if (connectorPlugin == null) {
            throw new IllegalArgumentException("The connector plugin instance cannot be null.");
        }

        if (connectorsRepo == null) {
            throw new IllegalArgumentException("The connectors repository cannot be null.");
        }

        if (usersRepo == null) {
            throw new IllegalArgumentException("The users repository cannot be null.");
        }

        if (smtpSettings == null) {
            throw new IllegalArgumentException("The e-mail settings object cannot be null.");
        }

        if (applicationLanguage == null) {
            throw new IllegalArgumentException("The application langague code cannot be null.");
        }

        this.connectorId = connectorIdentifier;
        this.connectorPluginInstance = connectorPlugin;
        this.connectorsRepository = connectorsRepo;
        this.usersRepository = usersRepo;
        this.emailSettings = smtpSettings;
        this.language = applicationLanguage;
        this.fetchCommands();
    }



    @Override
    public final IProduct read()
            throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        return this.productsQueue.poll();
    }



    /**
     * Connects to the server specified by the connector to look for orders.
     */
    private void fetchCommands() {

        if (this.connectorPluginInstance == null) {
            throw new IllegalStateException("The connector plugin is not defined.");
        }

        Connector connector = this.connectorsRepository.findById(this.connectorId)
                                .orElseThrow(() -> new IllegalStateException("Could not fetch the connector instance."));

        if (!this.connectorPluginInstance.getCode().equals(connector.getConnectorCode())) {
            throw new IllegalStateException("The connector plugin does not match the type required by"
                    + " the connector instance");
        }

        String connectorName = connector.getName();

        this.logger.debug("Importing commands for connector {}.", connectorName);
        this.productsQueue = new ArrayDeque<>();

        IConnectorImportResult result;

        try {
            IConnector parameteredPluginInstance
                    = this.connectorPluginInstance.newInstance(this.language, connector.getConnectorParametersValues());
            this.logger.debug("Connector plugin instantiated with the parameters values from connector {}.",
                    connectorName);
            result = parameteredPluginInstance.importCommands();

        } catch (Exception exception) {
            this.logger.error("An error occurred when the connector was fetching commands for connector {} from the"
                    + " server.", connectorName, exception);
            this.updateConnectorLastImportInfo(false, exception.getMessage());
            return;
        }

        if (result == null) {
            this.logger.warn("The commands import for connector {} returned a null result.", connectorName);
            this.updateConnectorLastImportInfo(false, "Null result.");
            return;
        }

        if (!result.getStatus()) {
            this.logger.warn("The commands import for connector {} failed.", connectorName);
            this.updateConnectorLastImportInfo(false, result.getErrorMessage());
            return;
        }

        this.logger.debug("The commands import for connector {} succeeded.", connectorName);
        this.productsQueue.addAll(result.getProductList());
        int queueSize = this.productsQueue.size();
        this.logger.debug("{} product{} imported and added to the queue.", queueSize, (queueSize > 1) ? "s" : "");
        this.updateConnectorLastImportInfo(true, "");
    }



    /**
     * Sets the last import date and message for the connector instance.
     *
     * @param success <code>true</code> if the last import completed without an error
     * @param message the string that explains the result of the last import
     */
    private void updateConnectorLastImportInfo(final boolean success, final String message) {
        this.logger.debug("Updating the connector last import info with message \"{}\".", message);
        Connector connector = null;

        try {
            connector = this.connectorsRepository.findById(this.connectorId)
                .orElseThrow(() -> {
                    return new UnsupportedOperationException("Impossible to update a connector that does not exist.");
                });

            final Calendar importTime = new GregorianCalendar();

            if (!success) {
                int errorCount = connector.getErrorCount() + 1;

                if (errorCount > connector.getMaximumRetries()) {

                    if (!connector.isInError() || !Objects.equals(message, connector.getLastImportMessage())) {
                        this.sendNotificationEmail(connector, message, importTime);
                        connector.setLastImportMessage(message);
                    }
                } else {
                    connector.setErrorCount(errorCount);
                }

            } else {
                connector.setErrorCount(0);
                connector.setLastImportMessage(message);
            }

            connector.setLastImportDate(importTime);

            connector = this.connectorsRepository.save(connector);

            if (connector == null) {
                this.logger.error("The connector last import update failed when saving.");
                return;
            }

            this.logger.debug("Connector saved with last import date {} and message \"{}\"",
                    connector.getLastImportDate().getTime(), connector.getLastImportMessage());

        } catch (Exception exception) {
            String connectorName = (connector != null)
                    ? connector.getName() : String.format("(ID: %d)", this.connectorId);
            this.logger.error("Could not update the last import information for the connector {}.", connectorName,
                    exception);
        }
    }



    /**
     * Notifies the active administrators that the last import failed.
     *
     * @param connector    the connector used for the import that failed
     * @param errorMessage the string returned to explain why the import failed
     * @param importTime   when the import failed
     */
    private void sendNotificationEmail(final Connector connector, final String errorMessage,
            final Calendar importTime) {
        assert connector != null : "The connector cannot be null.";
        assert errorMessage != null : "The error message cannot be null.";
        assert importTime != null : "The import time cannot be null.";

        try {
            this.logger.debug("Sending e-mail notifications to administrators.");

            // Retrieve administrators as User objects
            final User[] administrators = this.usersRepository.findByProfileAndActiveTrue(User.Profile.ADMIN);

            if (administrators == null || administrators.length == 0) {
                this.logger.warn("No administrators found for connector import failure notification.");
                return;
            }

            // Parse available locales from configuration
            final List<java.util.Locale> availableLocales = LocaleUtils.parseAvailableLocales(this.language);
            boolean atLeastOneEmailSent = false;

            // Send individual email to each administrator with their preferred locale
            for (User administrator : administrators) {
                try {
                    final ConnectorImportFailedEmail message = new ConnectorImportFailedEmail(this.emailSettings);

                    // Get validated locale for this administrator
                    java.util.Locale userLocale = LocaleUtils.getValidatedUserLocale(administrator, availableLocales);

                    if (!message.initializeContent(connector, errorMessage, importTime, userLocale)) {
                        this.logger.error("Could not create the message for user {}.", administrator.getLogin());
                        continue;
                    }

                    try {
                        message.addRecipient(administrator.getEmail());
                    } catch (javax.mail.internet.AddressException e) {
                        this.logger.error("Invalid email address for user {}: {}",
                            administrator.getLogin(), administrator.getEmail());
                        continue;
                    }

                    if (message.send()) {
                        this.logger.debug("Connector import failure notification sent successfully to {} with locale {}.",
                                        administrator.getEmail(), userLocale.toLanguageTag());
                        atLeastOneEmailSent = true;
                    } else {
                        this.logger.warn("Failed to send connector import failure notification to {}.",
                            administrator.getEmail());
                    }

                } catch (Exception exception) {
                    this.logger.warn("Error sending notification to user {}: {}",
                        administrator.getLogin(), exception.getMessage());
                }
            }

            if (atLeastOneEmailSent) {
                this.logger.info("The connector error import e-mail notification was sent to at least one administrator.");
            } else {
                this.logger.warn("The connector error import e-mail notification was not sent to any administrator.");
            }

        } catch (Exception exception) {
            this.logger.warn("An error prevented notifying the administrators by e-mail.", exception);
        }
    }

}
