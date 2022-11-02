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
package ch.asit_asso.extract.domain;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlRootElement;



/**
 * Condition for matching a data item request to the process that will generate the data.
 *
 * @author Florent Krin
 */
@Entity
@Table(name = "Rules", indexes = {
    @Index(columnList = "id_process", name = "IDX_RULE_PROCESS"),
    @Index(columnList = "id_connector", name = "IDX_RULE_CONNECTOR")
})
@XmlRootElement
public class Rule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The number that uniquely identifies this rule in the application.
     */
    @Id
    @GeneratedValue
    @Basic(optional = false)
    @NotNull
    @Column(name = "id_rule")
    private Integer id;

    /**
     * The criteria that the data item request must satisfy.
     */
    @Column(name = "rule", columnDefinition = "text")
    private String rule;

    /**
     * Whether this rule must be considered when a new data item request is imported.
     */
    @Column(name = "active")
    private Boolean active;

    /**
     * The number that determines when this rule must be considered compared to the other rules of the
     * parent connector.
     */
    @Column(name = "position")
    private Integer position;

    /**
     * The ensemble of task to execute to generate the data for the requests that match the criteria of
     * this rule.
     */
    @JoinColumn(name = "id_process", referencedColumnName = "id_process",
            foreignKey = @ForeignKey(name = "FK_RULE_PROCESS")
    )
    @ManyToOne
    private Process process;

    /**
     * The connector that imports the requests that can be matched against this rule.
     */
    @JoinColumn(name = "id_connector", referencedColumnName = "id_connector",
            foreignKey = @ForeignKey(name = "FK_RULE_CONNECTOR")
    )
    @ManyToOne
    private Connector connector;



    /**
     * Creates a new rule instance.
     */
    public Rule() {
    }



    /**
     * Creates a new rule instance.
     *
     * @param identifier the number that identifies this rule in the application
     */
    public Rule(final Integer identifier) {
        this.id = identifier;
    }



    /**
     * Obtains the number that identifies this rule in the application.
     *
     * @return the rule identifier
     */
    public Integer getId() {
        return this.id;
    }



    /**
     * Defines the number that identifies this rule in the application.
     *
     * @param identifier the rule identifier
     */
    public void setId(final Integer identifier) {
        this.id = identifier;
    }



    /**
     * Obtains the criteria that the data item request must satisfy.
     *
     * @return a string with the logical operations to check
     */
    public String getRule() {
        return this.rule;
    }



    /**
     * Defines the criteria that the data item request must satisfy.
     *
     * @param criteria a string with the logical operations to check
     */
    public void setRule(final String criteria) {
        this.rule = criteria;
    }



    /**
     * Obtains whether this rule must be considered when a new data item request is imported.
     *
     * @return <code>true</code> if the rule must be taken into account
     */
    public Boolean isActive() {
        return active;
    }



    /**
     * Defines whether this rule must be considered when a new data item request is imported.
     *
     * @param isActive <code>true</code> if the rule must be taken into account
     */
    public void setActive(final Boolean isActive) {
        this.active = isActive;
    }



    /**
     * Obtains where this rule must be considered compared to the other rules defined for the parent
     * connector.
     *
     * @return the position index
     */
    public Integer getPosition() {
        return position;
    }



    /**
     * Defines where this rule must be considered compared to the other rules defined for the parent
     * connector.
     *
     * @param positionIndex the position index
     */
    public void setPosition(final Integer positionIndex) {
        this.position = positionIndex;
    }



    /**
     * Obtains the ensemble of tasks to execute in order to generate the data for the matching data item
     * requests.
     *
     * @return the associated process
     */
    public Process getProcess() {
        return process;
    }



    /**
     * Defines the ensemble of tasks to execute in order to generate the data for the matching data item
     * requests.
     *
     * @param generationProcess the process to associate to this rule
     */
    public void setProcess(final Process generationProcess) {
        this.process = generationProcess;
    }



    /**
     * Obtains the instance that imports data item requests to match against this rule.
     *
     * @return the parent connector
     */
    public Connector getConnector() {
        return connector;
    }



    /**
     * Defines the instance that imports data item requests to match against this rule.
     *
     * @param associatedConnector the parent connector
     */
    public void setConnector(final Connector associatedConnector) {
        this.connector = associatedConnector;
    }



    @Override
    public final int hashCode() {
        int hash = 0;
        hash += this.id.hashCode();

        return hash;
    }



    @Override
    public final boolean equals(final Object object) {

        if (object == null || !(object instanceof Rule)) {
            return false;
        }

        Rule other = (Rule) object;

        return this.id.equals(other.id);
    }



    @Override
    public final String toString() {
        return String.format("ch.asit_asso.extract.Rule[ idRule=%d ]", this.id);
    }

}
