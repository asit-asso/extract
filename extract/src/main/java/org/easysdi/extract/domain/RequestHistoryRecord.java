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
import java.util.GregorianCalendar;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import org.thymeleaf.util.StringUtils;



/**
 * An item tracing the state of a task in the processing of a request.
 *
 * @author Yves Grasset
 */
@Entity
@Table(name = "Request_History", indexes = {
    @Index(columnList = "id_request", name = "IDX_REQUEST_HISTORY_REQUEST"),
    @Index(columnList = "id_user", name = "IDX_REQUEST_HISTORY_USER")
})
@XmlRootElement
public class RequestHistoryRecord implements Serializable {

    /**
     * The identifier of the history entry.
     */
    @Id
    @GeneratedValue
    @Column(name = "id_record")
    //@NotNull
    private Integer id;

    /**
     * The user-friendly name of the plugin used to execute the task.
     */
    @Column(name = "task_label")
    @Size(max = 255)
    private String taskLabel;

    /**
     * The state of the task execution.
     */
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

    /**
     * The position of the task in the processing of the request. This value is incremented each time a task is
     * executed. This value is thus unique among the records related to a given request.
     */
    @Column(name = "step")
    private Integer step;

    /**
     * The position of the task in the process that was related to the request. This value will be identical
     * if the same process task is executed several times for the same request.
     */
    @Column(name = "process_step")
    private Integer processStep;

    /**
     * The string produced by the task plugin to explain the result.
     */
    @Column(name = "last_msg", length = 4000)
    @Size(max = 4000)
    private String message;

    /**
     * When the task execution began.
     */
    @Column(name = "start_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar startDate;

    /**
     * When the task execution completed.
     */
    @Column(name = "end_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar endDate;

    /**
     * The request related to this entry.
     */
    @JoinColumn(name = "id_request", referencedColumnName = "id_request",
            foreignKey = @ForeignKey(name = "FK_REQUEST_HISTORY_REQUEST")
    )
    @ManyToOne
    private Request request;

    /**
     * The user that executed the task.
     */
    @JoinColumn(name = "id_user", referencedColumnName = "id_user",
            foreignKey = @ForeignKey(name = "FK_REQUEST_HISTORY_USER")
    )
    @ManyToOne
    private User user;



    /**
     * Obtains the unique identifier of this entry.
     *
     * @return the identifier
     */
    public Integer getId() {
        return this.id;
    }



    /**
     * Defines the unique identifier for this entry.
     *
     * @param identifier the unique identifier
     */
    public void setId(final Integer identifier) {
        this.id = identifier;
    }



    /**
     * Obtains the user-friendly name of the plugin that executed the task.
     *
     * @return the task plugin name
     */
    public String getTaskLabel() {
        return this.taskLabel;
    }



    /**
     * Defines the user-friendly name of the plugin that executed the task.
     *
     * @param label the task plugin name
     */
    public void setTaskLabel(final String label) {
        this.taskLabel = label;
    }



    /**
     * Obtains the state of the task execution.
     *
     * @return the status
     */
    public Status getStatus() {
        return this.status;
    }



    /**
     * Defines the state of the task execution.
     *
     * @param currentStatus the status
     */
    public void setStatus(final Status currentStatus) {
        this.status = currentStatus;
    }



    /**
     * Obtains the position of this entry in the processing of the request, including reruns.
     *
     * @return the general position
     */
    public Integer getStep() {
        return this.step;
    }



    /**
     * Defines the position of this entry in the processing of the request, including reruns.
     *
     * @param historyStep the general position
     */
    public void setStep(final Integer historyStep) {
        this.step = historyStep;
    }



    /**
     * Obtains the position of the task related to this entry in the request process.
     *
     * @return the task position in the process
     */
    public Integer getProcessStep() {
        return this.processStep;
    }



    /**
     * Defines the position of the task related to this entry in the request process.
     *
     * @param processTaskStep the task position in the process
     */
    public void setProcessStep(final Integer processTaskStep) {
        this.processStep = processTaskStep;
    }



    /**
     * Obtains when the task execution began.
     *
     * @return the start date
     */
    public Calendar getStartDate() {
        return this.startDate;
    }



    /**
     * Defines when the task execution began.
     *
     * @param start the start date
     */
    public void setStartDate(final Calendar start) {
        this.startDate = start;
    }



    /**
     * Obtains when the task execution completed.
     *
     * @return the end date
     */
    public Calendar getEndDate() {
        return this.endDate;
    }



    /**
     * Defines when the task execution completed.
     *
     * @param end the end date
     */
    public void setEndDate(final Calendar end) {
        this.endDate = end;
    }



    /**
     * Obtains the request whose processing is traced by this entry.
     *
     * @return the request
     */
    public Request getRequest() {
        return this.request;
    }



    /**
     * Defines the request whose processing is traced by this entry.
     *
     * @param parentRequest the request
     */
    public void setRequest(final Request parentRequest) {
        this.request = parentRequest;
    }



    /**
     * Obtains the user that executed the task.
     *
     * @return the user
     */
    public User getUser() {
        return this.user;
    }



    /**
     * Defines the user that executed the task.
     *
     * @param taskUser the user
     */
    public void setUser(final User taskUser) {
        this.user = taskUser;
    }



    /**
     * Obtains the string produced by the task plugin to explain its result.
     *
     * @return the message
     */
    public String getMessage() {
        return this.message;
    }



    /**
     * Defines the string produced by the task plugin to explain its result.
     *
     * @param resultMessage the message
     */
    public void setMessage(final String resultMessage) {
        this.message = resultMessage;
    }



    /**
     * The possible task execution states.
     */
    public enum Status {
        /**
         * The task is currently running.
         */
        ONGOING,
        /**
         * The task requires an operator to decide if the process can go on.
         */
        STANDBY,
        /**
         * The task failed.
         */
        ERROR,
        /**
         * The task completed successfully.
         */
        FINISHED,
        /**
         * The task was not run.
         */
        SKIPPED
    }



    /**
     * Creates a new history record.
     */
    public RequestHistoryRecord() {

    }



    /**
     * Updates this entry to show that the task ended with an error.
     *
     * @param errorMessage the text explaining the error
     */
    public final void setToError(final String errorMessage) {
        this.setToError(errorMessage, new GregorianCalendar());
    }



    /**
     * Updates this entry to show that the task ended with an error.
     *
     * @param errorMessage the text explaining the error
     * @param errorDate    when the failure occurred
     */
    public final void setToError(final String errorMessage, final Calendar errorDate) {

        if (StringUtils.isEmptyOrWhitespace(errorMessage)) {
            throw new IllegalArgumentException("The error message cannot be empty.");
        }

        if (errorDate == null) {
            throw new IllegalArgumentException("The error date cannot be null.");
        }

        this.setStatus(Status.ERROR);
        this.setEndDate(errorDate);
        this.setMessage(errorMessage);
    }



    @Override
    public final int hashCode() {
        int hash = 0;
        hash += this.id.hashCode();

        return hash;
    }



    @Override
    public final boolean equals(final Object object) {

        if (object == null || !(object instanceof RequestHistoryRecord)) {
            return false;
        }

        RequestHistoryRecord other = (RequestHistoryRecord) object;

        return this.id.equals(other.id);
    }



    @Override
    public final String toString() {
        return "org.easysdi.extract.RequestHistoryEntry[ idEntry=" + id + " ]";
    }

}
