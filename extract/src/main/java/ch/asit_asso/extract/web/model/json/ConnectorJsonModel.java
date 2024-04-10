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
package ch.asit_asso.extract.web.model.json;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import ch.asit_asso.extract.domain.Connector;
import com.fasterxml.jackson.annotation.JsonView;
import org.joda.time.DateTime;
import org.springframework.context.MessageSource;



/**
 * A model for a connector that must be represented in JSON.
 *
 * @author Yves Grasset
 */
public class ConnectorJsonModel implements JsonModel {

    /**
     * The string to use to generate the relative URL to view the details of a connector.
     */
    private static final String CONNECTOR_URL_FORMAT = "connectors/%d";

    /**
     * The string that identifies the localized string indicating that the last import resulted in
     * an error.
     */
    private static final String IMPORT_ERROR_MESSAGE_KEY = "requestsList.connectors.importError";

    /**
     * The string that identifies the localized string indicating that the last import succeeded.
     */
    private static final String IMPORT_SUCCESS_MESSAGE_KEY = "requestsList.connectors.importSuccess";

    /**
     * The string that identifies the localized string indicating that no import has been run yet.
     */
    private static final String NO_IMPORT_MESSAGE_KEY = "requestsList.connectors.noImport";

    /**
     * The number that identifies this connector in the application.
     */
    @JsonView(PublicField.class)
    private int id;

    /**
     * Whether the last import for this connector failed.
     */
    @JsonView(PublicField.class)
    private boolean inError;

    /**
     * The human-friendy identifier for this connector.
     */
    @JsonView(PublicField.class)
    private String name;

    /**
     * The localized string that describes the status of this connector (through the result of its
     * latest import).
     */
    @JsonView(PublicField.class)
    private String stateMessage;

    /**
     * The relative address to access the details of this connector, if allowed by the current user's
     * privileges.
     */
    @JsonView(PublicField.class)
    private String url;



    /**
     * Creates a new JSON representation for a connector.
     *
     * @param connector      the connector to model
     * @param messageSource  the access to the localized application strings
     * @param canViewDetails <code>true</code> if the current user can view the details of the connector
     */
    public ConnectorJsonModel(final Connector connector, final MessageSource messageSource,
            final boolean canViewDetails) {

        if (connector == null) {
            throw new IllegalArgumentException("The connector to model cannot be null.");
        }

        if (messageSource == null) {
            throw new IllegalArgumentException("The message source cannot be null.");
        }

        this.id = connector.getId();
        this.inError = connector.isInError();
        this.name = connector.getName();
        this.stateMessage = this.getConnectorStateMessage(connector, messageSource);
        this.url = (canViewDetails) ? String.format(ConnectorJsonModel.CONNECTOR_URL_FORMAT, this.id) : null;
    }



    /**
     * Obtains the identifier for this connector.
     *
     * @return the number that identifies this connector in the application
     */
    public final int getId() {
        return this.id;
    }



    /**
     * Obtains whether the last import for this connector failed.
     *
     * @return <code>true</code> if this connector is in an error state
     */
    public final boolean isInError() {
        return this.inError;
    }



    /**
     * Obtains the localized string that describes the current status of this connector (through the result
     * of its latest import).
     *
     * @return the localized connector state description
     */
    public final String getStateMessage() {
        return this.stateMessage;
    }



    /**
     * Obtains the human-friendly identifier for this connector.
     *
     * @return the connector name
     */
    public final String getName() {
        return this.name;
    }



    /**
     * Obtains the relative URL to view the details of this connector, if the current user is allowed
     * to do so.
     *
     * @return the relative URL to the details page, or <code>null</code> if the current user has not been granted
     *         the necessary privileges
     */
    public final String getUrl() {
        return this.url;
    }



    /**
     * Generates JSON models for a collection of connectors.
     *
     * @param connectorsArray          an array that contains the connectors to be exported to JSON
     * @param messageSource            the access to the localized application strings
     * @param canViewConnectorsDetails <code>true</code> if the current user can view the details of the connectors
     * @return an array that contains the generated JSON models
     */
    public static ConnectorJsonModel[] fromConnectorsArray(final Connector[] connectorsArray,
            final MessageSource messageSource, final boolean canViewConnectorsDetails) {

        if (connectorsArray == null) {
            throw new IllegalArgumentException("The connectors array cannot be null.");
        }

        if (messageSource == null) {
            throw new IllegalArgumentException("The message source cannot be null.");
        }

        List<ConnectorJsonModel> modelsList = new ArrayList<>();

        for (Connector connector : connectorsArray) {

            if (!canViewConnectorsDetails && !connector.isInError()) {
                continue;
            }

            modelsList.add(new ConnectorJsonModel(connector, messageSource, canViewConnectorsDetails));
        }

        return modelsList.toArray(new ConnectorJsonModel[]{});
    }



    /**
     * Generates a localized string to describe the current status of a connector.
     *
     * @param connector     the connector
     * @param messageSource the access to the localized application strings
     * @return the localized connector status message
     */
    private String getConnectorStateMessage(final Connector connector, final MessageSource messageSource) {
        assert connector != null : "The connector cannot be null.";
        assert messageSource != null : "The message source cannot be null.";

        Locale defaultLocale = Locale.getDefault();
        Calendar lastImportDate = connector.getLastImportDate();

        if (lastImportDate == null) {
            return messageSource.getMessage(ConnectorJsonModel.NO_IMPORT_MESSAGE_KEY, null, defaultLocale);
        }

        String importTimeString = new DateTime(lastImportDate).toString("HH:mm");

        if (connector.isInError()) {
            return messageSource.getMessage(ConnectorJsonModel.IMPORT_ERROR_MESSAGE_KEY, new Object[]{
                importTimeString, connector.getLastImportMessage()
            }, defaultLocale);
        }

        return messageSource.getMessage(ConnectorJsonModel.IMPORT_SUCCESS_MESSAGE_KEY,
                new Object[]{importTimeString}, defaultLocale);
    }

}
