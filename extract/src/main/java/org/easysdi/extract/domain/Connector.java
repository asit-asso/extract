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
package org.easysdi.extract.domain;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang3.StringUtils;
import org.easysdi.extract.domain.converters.JsonToParametersValuesConverter;
import org.easysdi.extract.persistence.RequestsRepository;
import org.hibernate.annotations.SortNatural;



/**
 * An instance of a connector as represented in the data source.
 *
 * @author Florent Krin
 */
@Entity
@Table(name = "Connectors")
@XmlRootElement
public class Connector implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The unique string that identifies the plugin used by this connector.
     */
    @Size(max = 50)
    @Column(name = "connector_code")
    private String connectorCode;

    /**
     * The user-friendly name of the connector.
     */
    @Size(max = 255)
    @Column(name = "connector_label")
    private String connectorLabel;

    /**
     * A JSON string containing the values for the plugin parameters.
     */
    @Size(max = 4000)
    @Column(name = "connector_params", length = 4000)
    @Convert(converter = JsonToParametersValuesConverter.class)
    private HashMap<String, String> connectorParametersValues;

    /**
     * The name of this instance.
     */
    @Size(max = 50)
    @Column(name = "name")
    private String name;

    /**
     * The number of seconds to wait between two command retrievals.
     */
    @Column(name = "import_freq")
    private Integer importFrequency;

    /**
     * Whether this instance should attempt to retrieve commands.
     */
    @Column(name = "active")
    private Boolean active;

    /**
     * The date and time that the commands retrieval was last attempted.
     */
    @Column(name = "last_import_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar lastImportDate;

    /**
     * The message returned by the plugin after the last command retrieval attempt.
     */
    @Column(name = "last_import_msg", length = 4000)
    @Size(max = 4000)
    private String lastImportMessage;

    /**
     * The number that uniquely identifies this connector instance.
     */
    @Id
    @Basic(optional = false)
    @GeneratedValue
    @NotNull
    @Column(name = "id_connector")
    private Integer id;

    /**
     * The requests created from commands retrieved by this instance.
     */
    @OneToMany(mappedBy = "connector", cascade = CascadeType.DETACH)
    private Collection<Request> requestsCollection;

    /**
     * The items matching the requests retrieved by this instance with a process.
     */
    @OneToMany(mappedBy = "connector", cascade = CascadeType.REMOVE)
    @OrderBy("position ASC")
    @SortNatural
    private Collection<Rule> rulesCollection;



    /**
     * Creates a new connector instance.
     */
    public Connector() {
    }



    /**
     * Creates a new connector instance.
     *
     * @param connectorIdentifier the number that will uniquely identify the new instance
     */
    public Connector(final Integer connectorIdentifier) {
        this.id = connectorIdentifier;
    }



    /**
     * Obtains the string that uniquely identifies the connector plugin to use.
     *
     * @return this.the plugin identifier
     */
    public String getConnectorCode() {
        return this.connectorCode;
    }



    /**
     * Defines the string that uniquely identifies the connector plugin to use.
     *
     * @param code the plugin identifier
     */
    public void setConnectorCode(final String code) {
        this.connectorCode = code;
    }



    /**
     * Obtains the user-friendly name of the connector plugin to use.
     *
     * @return this.the plugin name
     */
    public String getConnectorLabel() {
        return this.connectorLabel;
    }



    /**
     * Defines the user-friendly name of the connector plugin to use.
     *
     * @param label the plugin name
     */
    public void setConnectorLabel(final String label) {
        this.connectorLabel = label;
    }



    /**
     * Obtains the collection of values for the parameters of the connector plugin.
     *
     * @return this.the parameters values collection
     */
    public HashMap<String, String> getConnectorParametersValues() {
        return this.connectorParametersValues;
    }



    /**
     * Defines the collection of values for the parameters of the connector plugin.
     *
     * @param parametersValues the parameters values collection
     */
    public void setConnectorParametersValues(final HashMap<String, String> parametersValues) {

        if (parametersValues == null) {
            throw new IllegalArgumentException("The parameters map cannot be null.");
        }

        if (parametersValues.isEmpty()) {
            return;
        }

        if (this.connectorParametersValues == null) {
            this.connectorParametersValues = new HashMap<>();
        }

        for (Entry<String, String> parameterData : parametersValues.entrySet()) {
            this.connectorParametersValues.put(parameterData.getKey(), parameterData.getValue());
        }
    }



    /**
     * Obtains the user-friendly name of this instance.
     *
     * @return this.the name of this connector instance
     */
    public String getName() {
        return this.name;
    }



    /**
     * Defines the user-friendly name of this instance.
     *
     * @param connectorName the name of this connector instance
     */
    public void setName(final String connectorName) {
        this.name = connectorName;
    }



    /**
     * Obtains the delay between two command retrieval attempts.
     *
     * @return this.the delay in seconds
     */
    public Integer getImportFrequency() {
        return this.importFrequency;
    }



    /**
     * Defines the delay between two command retrieval attempts.
     *
     * @param frequency the delay in seconds
     */
    public void setImportFrequency(final Integer frequency) {
        this.importFrequency = frequency;
    }



    /**
     * Obtains whether this instance attempts to retrieve commands.
     *
     * @return this.<code>true</code> if this instance retrieves commands
     */
    public Boolean isActive() {
        return this.active;
    }



    /**
     * Defines whether this instance should attempt to retrieve commands.
     *
     * @param isActive <code>true</code> to retrieve commands with this instance
     */
    public void setActive(final Boolean isActive) {
        this.active = isActive;
    }



    /**
     * Obtains when the commands retrieval was last attempted.
     *
     * @return this.the date and time of the last successful command import
     */
    public Calendar getLastImportDate() {
        return this.lastImportDate;
    }



    /**
     * Defines when the commands retrieval was last attempted.
     *
     * @param lastImport the date and time of the last successful command import
     */
    public void setLastImportDate(final Calendar lastImport) {
        this.lastImportDate = lastImport;
    }



    /**
     * Obtains the text returned by the plugin the last time that a command retrieval was attempted.
     *
     * @return this.the message produced by the last import attempt
     */
    public String getLastImportMessage() {
        return this.lastImportMessage;
    }



    /**
     * Defines the text returned by the plugin the last time that a command retrieval was attempted.
     *
     * @param message the message produced by the last import attempt
     */
    public void setLastImportMessage(final String message) {
        this.lastImportMessage = message;
    }



    /**
     * Obtains the number that uniquely identifies this instance.
     *
     * @return this.the identifier for this connector instance
     */
    public Integer getId() {
        return this.id;
    }



    /**
     * Defines the number that uniquely identifies this instance.
     *
     * @param identifier the identifier for this connector instance
     */
    public void setId(final Integer identifier) {
        this.id = identifier;
    }



    /**
     * Obtains the requests created from commands retrieved by this instance.
     *
     * @return this.the requests collection
     */
    @XmlTransient
    public Collection<Request> getRequestsCollection() {
        return this.requestsCollection;
    }



    /**
     * Defines the requests created from commands retrieved by this instance.
     *
     * @param requests a collection that contains the requests to bind to this connector.
     */
    public void setRequestsCollection(final Collection<Request> requests) {
        this.requestsCollection = requests;
    }



    /**
     * Obtains the items that match the requests of this instance with a process.
     *
     * @return this.the rules collection
     */
    @XmlTransient
    public Collection<Rule> getRulesCollection() {
        return this.rulesCollection;
    }



    /**
     * Defines the items that match the requests of this instance with a process.
     *
     * @param rules a collection that contains the rules to apply to the incoming requests
     */
    public void setRulesCollection(final Collection<Rule> rules) {
        this.rulesCollection = rules;
    }



    /**
     * Determines if the last import by this instance resulted in an error.
     *
     * @return <code>true</code> if the last import failed
     */
    public final boolean isInError() {
        return StringUtils.isNotEmpty(this.lastImportMessage);
    }



    @Override
    public final int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }



    @Override
    public final boolean equals(final Object object) {

        if (object == null || !(object instanceof Connector)) {
            return false;
        }

        final Connector other = (Connector) object;
        return this.id.equals(other.id);
    }



    @Override
    public final String toString() {
        return String.format("org.easysdi.extract.Connector[idConnector=%d]", this.id);
    }



    /**
     * Determines if a command imported by this instance is currently being processed.
     * <p>
     * <b>Note:</b> For this method, a request is considered as being processed if its {@link Request.Status} is set
     * to anything else than FINISHED. That includes requests that are in error or waiting for an action by an
     * operator.
     * <p>
     * <i><b>IMPORTANT:</b> This method parses the requests collection. It can then be very (very!) slow if there are
     * a lot of finished requests. In this case, it is advised to use the
     * {@link #hasActiveRequests(org.easysdi.extract.persistence.RequestsRepository)} method.</i>
     *
     * @return this.<code>true</code> if at least one request bound to this instance is not finished
     */
    public final boolean hasActiveRequests() {

        for (Request request : this.getRequestsCollection()) {

            if (request.isActive()) {
                return true;
            }
        }

        return false;
    }



    /**
     * Determines if a command imported by this instance is currently being processed.
     * <p>
     * <b>Note:</b> For this method, a request is considered as being processed if its {@link Request.Status} is set
     * to anything else than FINISHED. That includes requests that are in error or waiting for an action by an
     * operator.
     *
     * @param requestsRepository the Spring Data object that links the request data objects with the data source
     * @return this.<code>true</code> if at least one request bound to this instance is not finished
     */
    public final boolean hasActiveRequests(final RequestsRepository requestsRepository) {

        if (requestsRepository == null) {
            throw new IllegalArgumentException("The requests repository cannot be null.");
        }

        return !requestsRepository.findByConnectorAndStatusNot(this, Request.Status.FINISHED).isEmpty();
    }

}
