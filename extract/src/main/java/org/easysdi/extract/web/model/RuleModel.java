/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easysdi.extract.web.model;

import org.easysdi.extract.domain.Connector;
import org.easysdi.extract.domain.Rule;



/**
 * Model object representing a rule instance.
 *
 * @author Florent Krin
 */
public class RuleModel {

    /**
     * The string that indicates that this rule is set to be removed.
     */
    public static final String TAG_DELETED = "DELETED";

    /**
     * The string that indicates that this rule is new.
     */
    public static final String TAG_ADDED = "ADDED";

    /**
     * Whether this rule must be considered when matching a request.
     */
    private boolean active = false;

    /**
     * The number that uniquely identifies this rule in the application.
     */
    private int id;

    /**
     * The number that uniquely identifies the process to associate to the requests that match this rule.
     */
    private int processId;

    /**
     * The string that describes the process to associate to the requests that match this rule.
     */
    private String processName;

    /**
     * The number that uniquely identifies the connector that imports the requests to match with this rule.
     */
    private int connectorId;

    /**
     * The expression that request must match.
     */
    private String rule;

    /**
     * The index that tells where this rule will be evaluated in relation to the other rules for
     * the same connector.
     */
    private int position;

    /**
     * The string that provides information about the life cycle of this rule.
     */
    private String tag;



    /**
     * Obtains whether this rule must be considered when matching requests.
     *
     * @return <code>true</code> if this rule is active
     */
    public final boolean isActive() {
        return active;
    }



    /**
     * Defines whether this rule must be considered when matching requests.
     *
     * @param isActive <code>true</code> to activate this rule
     */
    public final void setActive(final boolean isActive) {
        this.active = isActive;
    }



    /**
     * Obtains the identifier for this rule.
     *
     * @return the number that identifies this rule in the application
     */
    public final int getId() {
        return this.id;
    }



    /**
     * Defines the identifier for this rule.
     *
     * @param ruleId the number that identifies this rule in the application
     */
    public final void setId(final int ruleId) {
        this.id = ruleId;
    }



    /**
     * Obtains the identifier for the process to associate with the requests that match this rule.
     *
     * @return the number that identifies the process in the application
     */
    public final int getProcessId() {
        return this.processId;
    }



    /**
     * Defines the identifier for the process to associate with the requests that match this rule.
     *
     * @param processIdentifier the number that identifies the process in the application
     */
    public final void setProcessId(final int processIdentifier) {
        this.processId = processIdentifier;
    }



    /**
     * Obtains the description of the process to associate with the requests that match this rule.
     *
     * @return the string that describes the process
     */
    public final String getProcessName() {
        return this.processName;
    }



    /**
     * Defines the description of the process to associate with the requests that match this rule.
     *
     * @param name the string that describes the process
     */
    public final void setProcessName(final String name) {
        this.processName = name;
    }



    /**
     * Obtains the identifier for the connector that imports the requests to match with this rule.
     *
     * @return the number that identifies the connector in the application
     */
    public final int getConnectorId() {
        return this.connectorId;
    }



    /**
     * Defines the identifier for the connector that imports the requests to match with this rule.
     *
     * @param connectorIdentifier the number that identifies the connector in the application
     */
    public final void setConnectorId(final int connectorIdentifier) {
        this.connectorId = connectorIdentifier;
    }



    /**
     * Obtains the expression that the requests must match.
     *
     * @return the string expression
     */
    public final String getRule() {
        return this.rule;
    }



    /**
     * Defines the expression that the requests must match.
     *
     * @param expression the string expression
     */
    public final void setRule(final String expression) {
        this.rule = expression;
    }



    /**
     * Obtains where the rule will be considered compared to the other rules for the same connector.
     *
     * @return the position index
     */
    public final int getPosition() {
        return position;
    }



    /**
     * Defines where the rule will be considered compared to the other rules for the same connector.
     *
     * @param positionIndex the position index
     */
    public final void setPosition(final int positionIndex) {
        this.position = positionIndex;
    }



    /**
     * Obtains information about the life cycle of this rule.
     *
     * @return the life cycle tag string
     */
    public final String getTag() {
        return this.tag;
    }



    /**
     * Defines the information about the life cycle of this rule.
     *
     * @param lifeCycleTag the life cycle tag string
     */
    public final void setTag(final String lifeCycleTag) {
        this.tag = lifeCycleTag;
    }



    /**
     * Create a new rule model instance.
     */
    public RuleModel() {

    }



    /**
     * Create a new rule model instance.
     *
     * @param connectorIdentifier the number that identifies the connector that imports the request to match with this
     *                            rule
     */
    public RuleModel(final int connectorIdentifier) {
        this.setConnectorId(connectorIdentifier);
    }



    /**
     * Creates a new data object for this rule.
     *
     * @param connector the data object for the connector that imports the request to match with this rule
     * @param process   the data object for the process to associate with the requests that match this rule
     * @return the new rule data object to be persisted
     */
    public final Rule createDomainRule(final Connector connector, final org.easysdi.extract.domain.Process process) {
        Rule domainRule = new Rule();
        domainRule.setConnector(connector);
        domainRule.setActive(this.isActive());
        domainRule.setPosition(this.getPosition());
        domainRule.setRule(this.getRule());
        domainRule.setProcess(process);

        return domainRule;
    }



    /**
     * Creates a new persistence object for this rule.
     *
     * @param connector the data object for the connector that imports the request to match with this rule
     * @return the new rule data object to be persisted
     */
    public final Rule createDomainRule(final Connector connector) {
        return this.createDomainRule(connector, null);
    }



    /**
     * Modifies the properties of the persisted object with those of this model.
     * <p>
     * Note that the properties related to the plugin (type and label) are not updated by this method.</p>
     *
     * @param domainRule      the data object for the rule that this model represents
     * @param domainConnector the data object for the connector that imports the requests to match with this rule
     * @param domainProcess   the data object for the process to associate with the requests that match this rule
     */
    public final void updateDomainRule(final Rule domainRule, final Connector domainConnector,
            final org.easysdi.extract.domain.Process domainProcess) {
        domainRule.setActive(this.isActive());
        domainRule.setConnector(domainConnector);
        domainRule.setPosition(this.getPosition());
        domainRule.setProcess(domainProcess);
        domainRule.setRule(this.getRule());
    }

}
