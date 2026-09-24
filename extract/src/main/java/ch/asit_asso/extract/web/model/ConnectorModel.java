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
package ch.asit_asso.extract.web.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.validation.constraints.Max;
import ch.asit_asso.extract.connectors.common.IConnector;
import ch.asit_asso.extract.domain.Connector;
import ch.asit_asso.extract.domain.Rule;
import ch.asit_asso.extract.persistence.RequestsRepository;
import ch.asit_asso.extract.services.SecretParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 



/**
 * Model object representing a connector instance.
 *
 * @author Yves Grasset
 */
public class ConnectorModel extends PluginItemModel {

    /**
     * The number of seconds to wait before looking again for orders on the server if no other delay has
     * been set.
     */
    private static final int DEFAULT_IMPORT_FREQUENCY = 60;

    /**
     * The number of seconds to wait before looking again for orders on the server if no other delay has
     * been set.
     */
    private static final int DEFAULT_MAXIMUM_RETRIES = 3;

    /**
     * Whether this connector should attempt to retrieve commands.
     */
    private boolean active = false;

    /**
     * Whether this connector had active requests when it was read.
     */
    private boolean hasActiveRequests;

    /**
     * The number that uniquely identifies the connector instance represented by this model.
     */
    private int id;

    /**
     * The delay in seconds between two command retrieval attempts.
     */
    @Max(value = Integer.MAX_VALUE, message = "{connectorDetails.errors.importFrequency.tooLarge}")
    private Integer importFrequency = ConnectorModel.DEFAULT_IMPORT_FREQUENCY;

    /**
     * When the command retrieval was attempted for the last time.
     */
    private Calendar lastImportDate = null;

    /**
     * The string returned by the connector to explain the result state the last time that it tried to
     * fetch orders.
     */
    private String lastImportMessage;

    /**
     * The number of times imports must be attempted before the connector is switched to the error state.
     */
    @Max(value = Integer.MAX_VALUE, message = "{connectorDetails.errors.maxRetries.tooLarge}")
    private Integer maximumRetries = ConnectorModel.DEFAULT_MAXIMUM_RETRIES;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(ConnectorModel.class);

    /**
     * The user-friendly name of this connector instance.
     */
    //@NotEmpty(message = "{connectorDetails.errors.name.empty}")
    private String name;

    /**
     * The string that uniquely identifies the plugin used by this connector instance.
     */
    private String typeCode;

    /**
     * The user-friendly name of the plugin used by this connector instance.
     */
    private String typeLabel;

    /**
     * The items that match the products imported through this instance with a process.
     */
    private final List<RuleModel> rules = new ArrayList<>();



    /**
     * Obtains the status of this connector.
     *
     * @return <code>true</code> if this connector is enabled
     */
    public final boolean isActive() {
        return this.active;
    }



    /**
     * Defines the status of this connector.
     *
     * @param isActive <code>true</code> to enable this connector
     */
    public final void setActive(final boolean isActive) {
        this.active = isActive;
    }



    /**
     * Obtains the number that uniquely identifies the connector instance represented by this model.
     *
     * @return the identifier of this connector
     */
    public final int getId() {
        return this.id;
    }



    /**
     * Defines the number that uniquely identifies the connector instance represented by this model.
     *
     * @param connectorId the identifier
     */
    public final void setId(final int connectorId) {
        this.id = connectorId;
    }



    /**
     * Obtains the frequency at which this connector gets orders.
     *
     * @return the number of seconds between two requests for orders
     */
    public final Integer getImportFrequency() {
        return this.importFrequency;
    }



    /**
     * Defines the frequency at which this connector will get orders.
     *
     * @param delay the number of seconds to wait between two requests for orders
     */
    public final void setImportFrequency(final Integer delay) {
        this.importFrequency = delay;
    }



    /**
     * Obtains when the last request for orders took place.
     *
     * @return the date of the last request for orders
     */
    public final Calendar getLastImportDate() {
        return this.lastImportDate;
    }



    /**
     * Defines when the last request for orders took place.
     *
     * @param lastImport the date of the last request for orders
     */
    public final void setLastImportDate(final Calendar lastImport) {
        this.lastImportDate = lastImport;
    }



    /**
     * Obtains the string returned by the connector to explain the result state the last time that it
     * attempted to fetch orders.
     *
     * @return the message
     */
    public final String getLastImportMessage() {
        return this.lastImportMessage;
    }



    /**
     * Defines the string returned by the connector to explain the result state the last time that it
     * attempted to fetch orders.
     *
     * @param message the message
     */
    public final void setLastImportMessage(final String message) {
        this.lastImportMessage = message;
    }



    /**
     * Obtains the name of this connector instance.
     *
     * @return the user-friendly string identifying this connector
     */
    public final String getName() {
        return this.name;
    }



    /**
     * Defines the name of this connector instance.
     *
     * @param connectorName a user-friendly string to identify this connector
     */
    public final void setName(final String connectorName) {
        this.name = connectorName;
    }



    /**
     * Obtains the number of times imports must be attempted before the connector is switched to the error
     * state.
     *
     * @return the number of tries to attempt
     */
    public Integer getMaximumRetries() {
        return this.maximumRetries;
    }



    /**
     * Defines the number of times imports must be attempted before the connector is switched to the error
     * state.
     *
     * @param number the number of tries to attempt
     */
    public void setMaximumRetries(final Integer number) {
        this.maximumRetries = number;
    }



    /**
     * Obtains the code that identifies the type of connector to use.
     *
     * @return the string identifying the type of connector to use to get the orders
     */
    public final String getTypeCode() {
        return this.typeCode;
    }



    /**
     * Defines the code that identifies the type of connector to use.
     * <p>
     * Note that the value of this property will only be persisted if this connector instance is a new one.</p>
     *
     * @param pluginCode the connector type code
     */
    public final void setTypeCode(final String pluginCode) {
        this.typeCode = pluginCode;
    }



    /**
     * Obtains the label of the connector type .
     *
     * @return the user-friendly name of the type of connector
     */
    public final String getTypeLabel() {
        return this.typeLabel;
    }



    /**
     * Defines the label of the connector type.
     * <p>
     * Note that the value of this property will only be persisted if this connector instance is a new one.</p>
     *
     * @param pluginLabel the user-friendly name of the type of connector to use
     */
    public final void setTypeLabel(final String pluginLabel) {
        this.typeLabel = pluginLabel;
    }



    /**
     * Obtains the rules of the connector .
     *
     * @return the user-friendly name of the type of connector
     */
    public final RuleModel[] getRules() {
        return this.rules.toArray(new RuleModel[]{});
    }



    /**
     * Obtains a request-matching rule from the number that identifies it.
     *
     * @param ruleId the identifier of the rule
     * @return the rule, or <code>null</code> if none matches the given identifier
     */
    public final RuleModel getRuleById(final int ruleId) {

        for (RuleModel connectorRule : this.getRules()) {

            if (connectorRule.getId() == ruleId) {
                return connectorRule;
            }
        }

        return null;
    }



    /**
     * Defines the rules of the connector.
     * <p>
     * Note that the value of this property will only be persisted if this connector instance is a new one.</p>
     *
     * @param rulesArray the user-friendly rules of connector to use
     */
    public final void setRules(final RuleModel[] rulesArray) {
        this.rules.clear();
        this.rules.addAll(Arrays.asList(rulesArray));
    }



    /**
     * Obtains whether there were unfinished requests bound to this connector when it was read.
     *
     * @return <code>true</code> if the connector has active requests
     */
    public final boolean hasActiveRequests() {
        return this.hasActiveRequests;
    }



    /**
     * Creates a new connector instance model.
     */
    public ConnectorModel() {
    }



    /**
     * Creates a new connector instance model.
     *
     * @param connectorPlugin an instance of the plugin that will be used by this new connector instance
     */
    public ConnectorModel(final IConnector connectorPlugin) {
        this(connectorPlugin, null);
    }

    public ConnectorModel(final IConnector connectorPlugin, final SecretParameters secretParameters) {

        if (connectorPlugin == null) {
            throw new IllegalArgumentException("The connector plugin cannot be null.");
        }

        this.definePropertiesFromPlugin(connectorPlugin);
    }



    /**
     * Creates a new connector instance model.
     *
     * @param connectorPlugin    an instance of the plugin that will be used by this new connector instance.
     * @param domainConnector    the persisted connector instance that this model will represent
     * @param requestsRepository the Spring Data object that links the request data objects with the data source, or
     *                           <code>null</code> to use the requests collection to look for active requests. This
     *                           will be VERY slow if there are a large number of finished requests. You have been
     *                           warned.
     */

    public ConnectorModel(final IConnector connectorPlugin, final Connector domainConnector,
            final RequestsRepository requestsRepository, final SecretParameters secretParameters) {

        this(connectorPlugin, secretParameters);

        if (secretParameters == null) {
            throw new IllegalArgumentException("The secret parameters service cannot be null.");
        }

        if (domainConnector == null) {
            throw new IllegalArgumentException("The connector data object that this model represents cannot be null.");
        }

        if (!Objects.equals(connectorPlugin.getCode(), domainConnector.getConnectorCode())) {
            throw new IllegalArgumentException("The connector plugin is not the one used by the connector instance.");
        }

        this.definePropertiesFromDomainConnector(domainConnector, requestsRepository, secretParameters);
    }



    /**
     * Sets the properties of this connector instance that are related to its plugin.
     *
     * @param connectorPlugin an instance of the plugin that is used by this connector instance.
     */
    private void definePropertiesFromPlugin(final IConnector connectorPlugin) {
        this.setTypeCode(connectorPlugin.getCode());
        this.setTypeLabel(connectorPlugin.getLabel());
        this.defineParametersFromPlugin(connectorPlugin);
    }



    /**
     * Sets the properties of this connector instance that are related to its persisted object.
     *
     * @param domainConnector    the persisted connector instance that this model represents
     * @param requestsRepository the Spring Data object that links the request data objects with the data source, or
     *                           <code>null</code> to use the requests collection to look for active requests. This
     *                           will be VERY slow if there are a large number of finished requests. You have been
     *                           warned.
     */
    private void definePropertiesFromDomainConnector(final Connector domainConnector,
            final RequestsRepository requestsRepository, final SecretParameters secretParameters) {
        assert domainConnector != null : "The connector data object cannot be null.";

        if (requestsRepository == null) {
            this.logger.warn("The requests repository that was passed is null. The state of the requests bound to this"
                    + " connector will be computed from the full collection, which can be VERY long if there is a large"
                    + " number of requests in the data source. See if you can pass the requests repository for better"
                    + " performance.");
        }

        this.setActive(domainConnector.isActive());
        this.setId(domainConnector.getId());
        this.setImportFrequency(domainConnector.getImportFrequency());
        this.setLastImportDate(domainConnector.getLastImportDate());
        this.setLastImportMessage(domainConnector.getLastImportMessage());
        this.setName(domainConnector.getName());
        this.setMaximumRetries(domainConnector.getMaximumRetries());
        HashMap<String, String> values = domainConnector.getConnectorParametersValues();
        if (secretParameters != null) {
            values = secretParameters.decrypt(values, this.getSecretParameterNames());
        }
        this.setParametersValuesFromMap(values);
        this.setRulesFromRulesDomain(domainConnector.getRulesCollection());
        this.hasActiveRequests = (requestsRepository != null) ? domainConnector.hasActiveRequests(requestsRepository)
                : domainConnector.hasActiveRequests();
    }



    /**
     * Defines the request-matching rules for this model based on those defined in the data source.
     *
     * @param rulesCollection the rules defined for this connector in the data source
     */
    private void setRulesFromRulesDomain(final Collection<Rule> rulesCollection) {

        for (Rule rule : rulesCollection) {
            RuleModel model = new RuleModel();
            model.setActive(rule.isActive());
            model.setId(rule.getId());
            model.setConnectorId(rule.getConnector().getId());
            model.setPosition(rule.getPosition());
            model.setRule(rule.getRule());

            if (rule.getProcess() != null) {
                model.setProcessId(rule.getProcess().getId());
                model.setProcessName(rule.getProcess().getName());
            }

            this.rules.add(model);
        }
    }



    /**
     * The parameter definition provided by the selected connector plugin.
     */
    private String pluginParametersDefinition;
    /**
     * Sets the parameters definition (but not their values).
     *
     * @param connectorPlugin an instance of the plugin used by this connector instance
     */
    private void defineParametersFromPlugin(final IConnector connectorPlugin) {
        assert connectorPlugin != null : "The connector plugin must not be null.";

        this.pluginParametersDefinition = connectorPlugin.getParams();
        this.defineParametersFromJson(this.pluginParametersDefinition);
    }
    /**
     * Defines the authoritative parameter definition used when persisting this connector.
     *
     * @param parametersDefinition the definition returned by the connector plugin
     */
    public final void definePluginParametersDefinition(final String parametersDefinition) {
        if (parametersDefinition == null) {
            throw new IllegalArgumentException("The connector plugin parameters definition cannot be null.");
        }
        this.pluginParametersDefinition = parametersDefinition;
    }



    /**
     * Creates a new persistence object for this connector instance.
     *
     * @return the new connector object to be persisted
     */
    public final Connector createDomainConnector(final SecretParameters secretParameters) {
        if (secretParameters == null) {
            throw new IllegalArgumentException("The secret parameters service cannot be null.");
        }

        Connector domainConnector = new Connector();
        domainConnector.setConnectorCode(this.getTypeCode());
        domainConnector.setConnectorLabel(this.getTypeLabel());
        this.updateDomainConnector(domainConnector, secretParameters);
        return domainConnector;
    }



    /**
     * Modifies the properties of the persisted object with those of this model.
     * <p>
     * Note that the properties related to the plugin (type and label) are not updated by this method.</p>
     *
     * @param domainConnector the persisted object that this connector instance model represents
     */
    public final void updateDomainConnector(final Connector domainConnector,
                                            final SecretParameters secretParameters) {
        if (domainConnector == null) {
            throw new IllegalArgumentException("The domain connector cannot be null.");
        }

        if (secretParameters == null) {
            throw new IllegalArgumentException("The secret parameters service cannot be null.");
        }

        if (this.pluginParametersDefinition == null) {
            throw new IllegalStateException("The connector plugin parameters definition is not set.");
        }

        domainConnector.setActive(this.isActive());
        domainConnector.setImportFrequency(this.getImportFrequency());
        domainConnector.setName(this.getName());
        domainConnector.setMaximumRetries(this.getMaximumRetries());
        HashMap<String, String> values = secretParameters.encrypt(this.getParametersValues(),
                this.pluginParametersDefinition);
        domainConnector.updateConnectorParametersValues(values);
    }

    private Set<String> getSecretParameterNames() {
        Set<String> secretNames = new HashSet<>();
        for (PluginItemModelParameter parameter : this.getParameters()) {
            if (SecretParameters.isSecretType(parameter.getType())) {
                secretNames.add(parameter.getName());
            }
        }
        return secretNames;
    }



    /**
     * Add a request-matching rule to this connector.
     *
     * @param ruleModel the rule to add
     */
    public final void addRule(final RuleModel ruleModel) {
        ruleModel.setConnectorId(this.id);
        ruleModel.setPosition(this.rules.size() + 1);

        if (RuleModel.TAG_ADDED.equals(ruleModel.getTag())) {
            ruleModel.setId(this.getTemporaryRuleId());
        }

        this.logger.debug("Adding a rule (ID: {}) at position {}.", ruleModel.getId(), ruleModel.getPosition());
        this.rules.add(ruleModel);
    }



    /**
     * Provides a temporary identifier for a rule that has just been added.
     *
     * This only tells the new rule apart from the other rules of the form until it is saved. It is not the
     * identifier of a row of the data source, and it may well be the one of a rule of another connector: what
     * makes a rule new is its {@link RuleModel#TAG_ADDED} tag, never its identifier.
     *
     * @return an identifier that is unique among the rules of this connector
     */
    private int getTemporaryRuleId() {
        int maxRuleId = 0;

        for (RuleModel rule : this.rules) {
            int ruleId = rule.getId();

            if (ruleId > maxRuleId) {
                maxRuleId = ruleId;
            }
        }

        return maxRuleId + 1;
    }



    /**
     * Obtains the identifiers of the submitted rules that are not rules of this connector.
     *
     * A rule that has just been added carries a temporary identifier, which is not looked at here. Any other
     * identifier must be one of the rules that this connector holds in the data source. When it is not, the
     * submitted data cannot be trusted: it has either been tampered with, or the browser has filled the form with
     * a value belonging to another connector (issue #428).
     *
     * @param domainConnector the data object for this connector
     * @return the identifiers that do not belong to this connector, empty if the submitted rules are consistent
     */
    public final Integer[] getForeignRuleIds(final Connector domainConnector) {

        if (domainConnector == null) {
            throw new IllegalArgumentException("The connector data object cannot be null.");
        }

        return this.getForeignRuleIds(domainConnector.getRulesCollection());
    }



    /**
     * Obtains the identifiers of the submitted rules that cannot be, this connector not existing yet.
     *
     * A connector that is being created holds no rule, so no submitted rule can claim an identifier.
     *
     * @return the identifiers that no rule can carry here, empty if the submitted rules are consistent
     */
    public final Integer[] getForeignRuleIds() {
        return this.getForeignRuleIds((Collection<Rule>) null);
    }



    /**
     * Obtains the identifiers of the submitted rules that are none of the rules the connector holds.
     *
     * @param rulesInDataSource the rules that the connector holds, which may be null
     * @return the identifiers that do not belong to the connector
     */
    private Integer[] getForeignRuleIds(final Collection<Rule> rulesInDataSource) {
        final List<Integer> foreignRuleIds = new ArrayList<>();

        for (RuleModel ruleModel : this.rules) {

            if (ruleModel.isNew()) {
                continue;
            }

            if (!ConnectorModel.containsRule(rulesInDataSource, ruleModel.getId())) {
                foreignRuleIds.add(ruleModel.getId());
            }
        }

        return foreignRuleIds.toArray(Integer[]::new);
    }



    /**
     * Obtains whether a collection of rule data objects holds the one with a given identifier.
     *
     * @param domainRules the rule data objects to look into, which may be null
     * @param ruleId      the identifier to look for
     * @return <code>true</code> if one of those rules carries that identifier
     */
    private static boolean containsRule(final Collection<Rule> domainRules, final int ruleId) {

        if (domainRules == null) {
            return false;
        }

        for (Rule domainRule : domainRules) {

            if (domainRule.getId() != null && domainRule.getId() == ruleId) {
                return true;
            }
        }

        return false;
    }



    /**
     * Removes the first request-matching rule that matches the provided identifier. (There should only be one
     * anyway.)
     *
     * @param ruleId the number that identifies the rule to remove
     */
    public final void removeRule(final int ruleId) {
        RuleModel ruleToRemove = this.getRuleById(ruleId);

        if (ruleToRemove == null) {
            return;
        }

        this.removeRule(ruleToRemove);
    }



    /**
     * Removes the given request-matching rule from the collection.
     *
     * @param rule the rule to remove
     */
    public final void removeRule(final RuleModel rule) {

        if (rule == null) {
            throw new IllegalArgumentException("The rule to remove cannot be null.");
        }

        this.rules.remove(rule);
    }

}
