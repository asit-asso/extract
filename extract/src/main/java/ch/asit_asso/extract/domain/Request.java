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
import java.util.Calendar;
import javax.persistence.Basic;
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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang3.StringUtils;



/**
 * A order for a data item imported through a connector.
 *
 * @author Florent Krin
 */
@Entity
@Table(name = "Requests", indexes = {
    @Index(columnList = "status", name = "IDX_REQUEST_STATUS"),
    @Index(columnList = "id_connector", name = "IDX_REQUEST_CONNECTOR"),
    @Index(columnList = "id_process", name = "IDX_REQUEST_PROCESS")
})
@XmlRootElement
public class Request implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The number that uniquely identifies this order in the application.
     */
    @Id
    @Basic(optional = false)
    @GeneratedValue
    @NotNull
    @Column(name = "id_request")
    private Integer id;

    /**
     * The description of this order.
     */
    @Size(max = 255)
    @Column(name = "p_orderlabel")
    private String orderLabel;

    /**
     * The GUID that uniquely identifies this order on the originating server.
     */
    @Size(max = 255)
    @Column(name = "p_orderguid")
    private String orderGuid;

    /**
     * The GUID that uniquely identifies the ordered data item on the originating server.
     */
    @Size(max = 255)
    @Column(name = "p_productguid")
    private String productGuid;

    /**
     * The description of the ordered data item.
     */
    @Size(max = 255)
    @Column(name = "p_productlabel")
    private String productLabel;

    /**
     * The organization that this data item was requested by.
     */
    @Size(max = 255)
    @Column(name = "p_organism")
    private String organism;

    @Size(max = 255)
    @Column(name = "p_organismguid")
    private String organismGuid;

    /**
     * The name of the person who ordered this data item.
     */
    @Size(max = 255)
    @Column(name = "p_client")
    private String client;

    @Size(max = 255)
    @Column(name = "p_clientguid")
    private String clientGuid;

    /**
     * Additional information about the person who ordered this data item.
     */
    @Size(max = 4000)
    @Column(name = "p_clientdetails")
    private String clientDetails;

    /**
     * The name of the third-party (if any) that this data item was ordered on behalf of.
     */
    @Size(max = 255)
    @Column(name = "p_tiers")
    private String tiers;

    /**
     * Additional information about the third-party (if any) that this data item was ordered on behalf of.
     */
    @Size(max = 4000)
    @Column(name = "p_tiersdetails")
    private String tiersDetails;


    /**
     * Additional information about the third-party (if any) that this data item was ordered on behalf of.
     */
    @Size(max = 255)
    @Column(name = "p_tiersguid")
    private String tiersGuid;

    /**
     * The WKT geometry of the geographical area for this order.
     */
    @Column(name = "p_perimeter", columnDefinition = "text")
    private String perimeter;

    /**
     * The area of the order perimeter in square meters.
     */
    @Column(name = "p_surface")
    private Double surface;

    /**
     * Additional information about the data item order as a JSON string.
     */
    @Column(name = "p_parameters", columnDefinition = "text")
    private String parameters;

    /**
     * Additional information about the process of this data item to return to the customer.
     */
    @Size(max = 4000)
    @Column(name = "remark", length = 4000)
    private String remark;

    /**
     * The path of the folder that contains the input data to process this data item. It is relative to the
     * folder that contains the data for all orders, as set in the application settings.
     */
    @Size(max = 255)
    @Column(name = "folder_in")
    private String folderIn;

    /**
     * The path of the folder that contains the data generated by processing this data item. It is relative
     * to the folder that contains the data for all orders, as set in the application settings.
     */
    @Size(max = 255)
    @Column(name = "folder_out")
    private String folderOut;

    /**
     * When this order was imported.
     */
    @Column(name = "start_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar startDate;

    /**
     * When the process of this data item finished.
     */
    @Column(name = "end_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Calendar endDate;

    /**
     * The index of the current process (or next) task.
     */
    @Column(name = "tasknum")
    private Integer tasknum;

    /**
     * The current state of the data item processing.
     */
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

    /**
     * Whether processing this data item must be abandoned.
     */
    @Column(name = "rejected")
    private boolean rejected;

    /**
     * The address that provides an access to the details of this order.
     */
    @Size(max = 255)
    @Column(name = "p_external_url")
    private String externalUrl;

    /**
     * The set of tasks attached to this data item order to produce the requested data.
     */
    @JoinColumn(name = "id_process", referencedColumnName = "id_process",
            foreignKey = @ForeignKey(name = "FK_REQUEST_PROCESS")
    )
    @ManyToOne
    private Process process;

    /**
     * The connector that imported this data item order.
     */
    @JoinColumn(name = "id_connector", referencedColumnName = "id_connector", nullable = true, updatable = true,
            foreignKey = @ForeignKey(name = "FK_REQUEST_CONNECTOR")
    )
    @ManyToOne
    private Connector connector;



    /**
     * The possible states of a data item order processing.
     */
    public enum Status {

        /**
         * The order from the originated server cannot be processed as a request.
         */
        IMPORTFAIL,
        /**
         * The order has been fetched from its originating server.
         */
        IMPORTED,
        /**
         * The processing of the data item is running normally.
         */
        ONGOING,
        /**
         * No process could be attached to this data item order based on the connector rules.
         */
        UNMATCHED,
        /**
         * The last process task that run failed.
         */
        ERROR,
        /**
         * An operator must decide if the process can proceed.
         */
        STANDBY,
        /**
         * The result of the data item processing is ready to be sent back to the originating server.
         */
        TOEXPORT,
        /**
         * The result of the data item processing could not be sent back to the originating server.
         */
        EXPORTFAIL,
        /**
         * The result was successfully sent back to the originating server and the process is now over.
         */
        FINISHED
    }



    /**
     * Creates a new instance of a data item order.
     */
    public Request() {
    }



    /**
     * Creates a new instance of a data item order.
     *
     * @param idRequest the number that uniquely identifies the request
     */
    public Request(final Integer idRequest) {
        this.id = idRequest;
    }



    /**
     * Obtains the number that uniquely identifies this data item order in the application.
     *
     * @return the request identifier
     */
    public Integer getId() {
        return this.id;
    }



    /**
     * Defines the number that uniquely identifies this data item order in the application.
     *
     * @param identifier the request identifier
     */
    public void setId(final Integer identifier) {
        this.id = identifier;
    }



    /**
     * Obtains the description of the order that this data item request is a part of.
     *
     * @return the order label
     */
    public String getOrderLabel() {
        return this.orderLabel;
    }



    /**
     * Defines the description of the order that this data item request is a part of.
     *
     * @param label the order label
     */
    public void setOrderLabel(final String label) {
        this.orderLabel = label;
    }



    /**
     * Obtains the string that uniquely identifies the order that this data item request is a part of on
     * its originating server.
     *
     * @return the order UUID
     */
    public String getOrderGuid() {
        return this.orderGuid;
    }



    /**
     * Defines the string that uniquely identifies the order that this data item request is a part of on
     * its originating server.
     *
     * @param uuid the order UUID
     */
    public void setOrderGuid(final String uuid) {
        this.orderGuid = uuid;
    }



    /**
     * Obtains the string that uniquely identifies the requested data item on the server where the order
     * was made.
     *
     * @return the product UUID
     */
    public String getProductGuid() {
        return this.productGuid;
    }



    /**
     * Defines the string that uniquely identifies the requested data item on the server where the order
     * was made.
     *
     * @param uuid the product UUID
     */
    public void setProductGuid(final String uuid) {
        this.productGuid = uuid;
    }



    /**
     * Obtains the description of the ordered data item.
     *
     * @return the product label
     */
    public String getProductLabel() {
        return this.productLabel;
    }



    /**
     * Defines the description of the ordered data item.
     *
     * @param label the product label
     */
    public void setProductLabel(final String label) {
        this.productLabel = label;
    }



    /**
     * Obtains the organization that the person who placed this data item order is part of.
     *
     * @return the name of the organization
     */
    public String getOrganism() {
        return this.organism;
    }



    /**
     * Defines the organization that the person who placed this data item order is part of.
     *
     * @param organismName the name of the organization
     */
    public void setOrganism(final String organismName) {
        this.organism = organismName;
    }



    /**
     * Obtains the string that uniquely identifies the organization that the person who placed this data item order is
     * part of.
     *
     * @return the identifier of the organization
     */
    public String getOrganismGuid() {
        return this.organismGuid;
    }



    /**
     * Defines the string that uniquely identifies the organization that the person who placed this data item order is part of.
     *
     * @param organismGuid the identifier of the organization
     */
    public void setOrganismGuid(final String organismGuid) {
        this.organismGuid = organismGuid;
    }



    /**
     * Obtains the name of the customer who ordered this data item.
     *
     * @return the customer name
     */
    public String getClient() {
        return this.client;
    }



    /**
     * Defines the name of the customer who ordered this data item.
     *
     * @param customer the customer name
     */
    public void setClient(final String customer) {
        this.client = customer;
    }



    /**
     * Obtains the string that uniquely identifies the customer who ordered this data item.
     *
     * @return the identifier of the customer
     */
    public String getClientGuid() {
        return this.clientGuid;
    }



    /**
     * Defines the string that uniquely identifies the customer who ordered this data item.
     *
     * @param customerGuid the identifier of the customer
     */
    public void setClientGuid(final String customerGuid) {
        this.clientGuid = customerGuid;
    }



    /**
     * Obtains additional information about the customer who ordered this data item.
     *
     * @return a string that contains the customer's details
     */
    public String getClientDetails() {
        return this.clientDetails;
    }



    /**
     * Defines additional information about the customer who ordered this data item.
     *
     * @param details a string that contains the customer's details
     */
    public void setClientDetails(final String details) {
        this.clientDetails = details;
    }



    /**
     * Obtains the name of the third party (if any) on behalf of which this data item was ordered.
     *
     * @return the name of the third party, or <code>null</code> if there is none
     */
    public String getTiers() {
        return this.tiers;
    }



    /**
     * Defines the name of the third party (if any) on behalf of which this data item was ordered.
     *
     * @param name the name of the third party, or <code>null</code> if there is none
     */
    public void setTiers(final String name) {
        this.tiers = name;
    }



    /**
     * Obtains the string that uniquely identifies the third party (if any) on behalf of which this data item
     * was ordered.
     *
     * @return the identifier of the third party, or <code>null</code> if there is none
     */
    public String getTiersGuid() {
        return this.tiersGuid;
    }



    /**
     * Defines the string that uniquely identifies the third party (if any) on behalf of which this data item
     * was ordered.
     *
     * @param tiersGuid the identifier of the third party, or <code>null</code> if there is none
     */
    public void setTiersGuid(final String tiersGuid) {
        this.tiersGuid = tiersGuid;
    }



    /**
     * Obtains additional information about the third party (if any) on behalf of which this data item was
     * ordered.
     *
     * @return a string that contains the third party's details, or <code>null</code> if there is none
     */
    public String getTiersDetails() {
        return this.tiersDetails;
    }



    /**
     * Defines additional information about the third party (if any) on behalf of which this data item was
     * ordered.
     *
     * @param details a string that contains the third party's details, or <code>null</code> if there is none
     */
    public void setTiersDetails(final String details) {
        this.tiersDetails = details;
    }



    /**
     * Obtains the geographical area of the ordered data.
     *
     * @return a string that contains the area as a WKT polygon with WGS84 coordinates
     */
    public String getPerimeter() {
        return this.perimeter;
    }



    /**
     * Defines the geographical area of the ordered data.
     *
     * @param wktPolygon a string that contains the area as a WKT polygon with WGS84 coordinates
     */
    public void setPerimeter(final String wktPolygon) {
        this.perimeter = wktPolygon;
    }



    /**
     * Obtains the size of the ordered data area.
     *
     * @return the area in square meters
     */
    public Double getSurface() {
        return this.surface;
    }



    /**
     * Defines the size of the ordered data area.
     *
     * @param areaSize the area in square meters
     */
    public void setSurface(final Double areaSize) {
        this.surface = areaSize;
    }



    /**
     * Obtains additional information about this data item order.
     *
     * @return a string that contains the additional parameters in JSON format
     */
    public String getParameters() {
        return this.parameters;
    }



    /**
     * Defines additional information about this data item order.
     *
     * @param parametersJson a string that contains the additional parameters in JSON format
     */
    public void setParameters(final String parametersJson) {
        this.parameters = parametersJson;
    }



    /**
     * Obtains the text to return to the customer to provide additional information about processing this
     * data item order.
     *
     * @return the remark
     */
    public String getRemark() {
        return this.remark;
    }



    /**
     * Defines the text to return to the customer to provide additional information about processing this
     * data item order.
     *
     * @param newRemark the remark
     */
    public void setRemark(final String newRemark) {
        this.remark = newRemark;
    }



    /**
     * Obtains the path of the folder that contains the data required to process this order.
     *
     * @return the path of the input folder relative to the orders data folder as set in the application settings
     */
    public String getFolderIn() {
        return folderIn;
    }



    /**
     * Defines the path of the folder that contains the data required to process this order.
     *
     * @param path the path of the input folder relative to the orders data folder as set in the application settings
     */
    public void setFolderIn(final String path) {
        this.folderIn = path;
    }



    /**
     * Obtains the path of the folder that contains the data produced by processing this order.
     *
     * @return the path of the output folder relative to the orders data folder as set in the application settings
     */
    public String getFolderOut() {
        return folderOut;
    }



    /**
     * Defines the path of the folder that contains the data produced by processing this order.
     *
     * @param path the path of the output folder relative to the orders data folder as set in the application settings
     */
    public void setFolderOut(final String path) {
        this.folderOut = path;
    }



    /**
     * Obtains when this order was imported.
     *
     * @return the date and time of the import
     */
    public Calendar getStartDate() {
        return this.startDate;
    }



    /**
     * Defines when this order was imported.
     *
     * @param start the date and time of the import
     */
    public void setStartDate(final Calendar start) {
        this.startDate = start;
    }



    /**
     * Obtains when the processing of this order successfully completed.
     *
     * @return the date and time of the process end, or <code>null</code> if the process has not completed yet
     */
    public Calendar getEndDate() {
        return this.endDate;
    }



    /**
     * Defines when the processing of this order successfully completed.
     *
     * @param end the date and time of the process end
     */
    public void setEndDate(final Calendar end) {
        this.endDate = end;
    }



    /**
     * Obtains the current processing step for this order.
     *
     * @return the index of the current task in the process
     */
    public Integer getTasknum() {
        return this.tasknum;
    }



    /**
     * Defines the current processing step for this order.
     *
     * @param taskIndex the index of the current task in the process
     */
    public void setTasknum(final Integer taskIndex) {
        this.tasknum = taskIndex;
    }



    /**
     * Obtains the current state of this order.
     *
     * @return the order status
     */
    public Status getStatus() {
        return this.status;
    }



    /**
     * Defines the current state of this order.
     *
     * @param currentStatus the order status
     */
    public void setStatus(final Status currentStatus) {
        this.status = currentStatus;
    }



    /**
     * Obtains the set of tasks that is associated with this order to produce the requested data.
     *
     * @return the process object
     */
    public Process getProcess() {
        return this.process;
    }



    /**
     * Defines the set of tasks that is associated with this order to produce the requested data.
     *
     * @param associatedProcess the process object
     */
    public void setProcess(final Process associatedProcess) {
        this.process = associatedProcess;
    }



    /**
     * Obtains the connector instance that imported this order.
     *
     * @return the connector instance
     */
    public Connector getConnector() {
        return this.connector;
    }



    /**
     * Defines the connector instance that imported this order.
     *
     * @param originatingConnector the connector instance
     */
    public void setConnector(final Connector originatingConnector) {
        this.connector = originatingConnector;
    }



    @Override
    public final int hashCode() {
        int hash = 0;
        hash += this.id.hashCode();

        return hash;
    }



    @Override
    public final boolean equals(final Object object) {

        if (object == null || !(object instanceof Request)) {
            return false;
        }

        Request other = (Request) object;

        return this.id.equals(other.id);
    }



    @Override
    public final String toString() {
        return String.format("ch.asit_asso.extract.Request[ idRequest=%d ]", this.id);
    }



    /**
     * Obtains whether the processing of this data item order has been abandoned.
     *
     * @return <code>true</code> if this order cannot be processed
     */
    public boolean isRejected() {
        return rejected;
    }



    /**
     * Defines whether the processing of this data item order has been abandoned.
     *
     * @param isRejected <code>true</code> if this order cannot be processed
     */
    public void setRejected(final boolean isRejected) {
        this.rejected = isRejected;
    }



    /**
     * Obtains the address that provides an access to the details of this order on the originating server.
     *
     * @return the URL of the order on the source server, or <code>null</code> if there is no such URL
     */
    public String getExternalUrl() {
        return this.externalUrl;
    }



    /**
     * Defines the address that provides an access to the details of this order on the originating server.
     *
     * @param url the address of the order on the source server, or <code>null</code> if there is no such URL
     */
    public void setExternalUrl(final String url) {
        this.externalUrl = url;
    }



    /**
     * Obtains whether this data item order has yet to be completed.
     *
     * @return <code>true</code> if this order is not completed yet, including if it currently is in error or in
     *         standby
     */
    public final boolean isActive() {
        return this.getStatus() != Status.FINISHED;
    }



    /**
     * Obtains whether this data item order is currently being processed.
     *
     * @return <code>true</code> if this order is being processed, NOT including if it currently is in error or in
     *         standby
     */
    public final boolean isOngoing() {
        return this.getStatus() == Status.ONGOING;
    }



    /**
     * Updates the properties of this request to indicate that it cannot be processed.
     *
     * @param rejectionRemark the string that explains why this request is rejected
     */
    public final void reject(final String rejectionRemark) {

        if (StringUtils.isBlank(rejectionRemark)) {
            throw new IllegalArgumentException("The rejection remark cannot be empty.");
        }

        if (this.getProcess() != null) {
            this.setTasknum(this.getProcess().getTasksCollection().size() + 1);

        } else {
            this.setTasknum(1);
        }

        this.setStatus(Request.Status.TOEXPORT);
        this.setRemark(rejectionRemark);
        this.setRejected(true);
    }

}
