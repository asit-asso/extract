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
package org.easysdi.extract.plugins;

import java.io.File;
import java.util.Calendar;
import org.easysdi.extract.domain.Request;
import org.easysdi.extract.plugins.common.ITaskProcessorRequest;



/**
 * Information about a request that needs to be processed by a plugin.
 *
 * @author Yves Grasset
 */
public final class TaskProcessorRequest implements ITaskProcessorRequest {

    /**
     * The name of the person who requested the data.
     */
    private String client;

    /**
     * The identifying string of the person who requested the data.
     */
    private String clientGuid;

    /**
     * The date when this request was exported back to its originating server (should be <code>null</code>).
     */
    private Calendar endDate;

    /**
     * The path of the folder that contains the data required to process the request, relative to the
     * folder that contains the data for all requests as set in the application settings.
     */
    private String folderIn;

    /**
     * The path of the folder that contains the data produced by the request processing, relative to the
     * folder that contains the data for all requests as set in the application settings.
     */
    private String folderOut;

    private int id;

    /**
     * The string that uniquely identifies the order that this request is a part of on its originating
     * server.
     */
    private String orderGuid;

    /**
     * The description of the order that this request is a part of.
     */
    private String orderLabel;

    /**
     * Additional settings about this request.
     */
    private String parameters;

    /**
     * The geographical extent of the ordered data as a WKT polygon with WGS84 coordinates.
     */
    private String perimeter;

    /**
     * The organization that the person who made this request belongs to.
     */
    private String organism;

    /**
     * The identifying string of the organization that the person who made this request belongs to.
     */
    private String organismGuid;

    /**
     * The string that uniquely identifies the requested data item on its originating server.
     */
    private String productGuid;

    /**
     * The description of the requested data item.
     */
    private String productLabel;

    /**
     * Whether the processing of this request must be abandoned.
     */
    private boolean rejected;

    /**
     * Additional information about the processing of this request to send back to the person who made
     * the order.
     */
    private String remark;

    /**
     * When this request was imported.
     */
    private Calendar startDate;

    /**
     * The current state of this request.
     */
    private String status;

    /**
     * The person or organism that this request was made on behalf of, if any.
     */
    private String tiers;



    /**
     * Creates a new instance of this request object.
     *
     * @param domainRequest       the information about the request from the data source
     * @param dataFoldersBasePath the absolute path of the folder that contains the data for all the requests
     */
    public TaskProcessorRequest(final Request domainRequest, final String dataFoldersBasePath) {

        if (domainRequest == null) {
            throw new IllegalArgumentException("The request from the data source cannot be null.");
        }

        this.setId(domainRequest.getId());
        this.setClient(domainRequest.getClient());
        this.setClientGuid(domainRequest.getClientGuid());
        this.setEndDate(domainRequest.getEndDate());
        this.setFolderIn(new File(dataFoldersBasePath, domainRequest.getFolderIn()).getAbsolutePath());
        this.setFolderOut(new File(dataFoldersBasePath, domainRequest.getFolderOut()).getAbsolutePath());
        this.setOrderGuid(domainRequest.getOrderGuid());
        this.setOrderLabel(domainRequest.getOrderLabel());
        this.setOrganism(domainRequest.getOrganism());
        this.setOrganismGuid(domainRequest.getOrganismGuid());
        this.setParameters(domainRequest.getParameters());
        this.setPerimeter(domainRequest.getPerimeter());
        this.setProductGuid(domainRequest.getProductGuid());
        this.setProductLabel(domainRequest.getProductLabel());
        this.setRejected(domainRequest.isRejected());
        this.setRemark(domainRequest.getRemark());
        this.setStartDate(domainRequest.getStartDate());
        this.setStatus(domainRequest.getStatus().toString());
        this.setTiers(domainRequest.getTiers());
    }



    @Override
    public int getId() {
        return this.id;
    }



    public void setId(final int id) {
        this.id = id;
    }



    @Override
    public String getOrderGuid() {
        return this.orderGuid;
    }



    /**
     * Defines the unique identifier of the order that this request is a part of.
     *
     * @param guid the string that uniquely identifies the order on its originating server
     */
    public void setOrderGuid(final String guid) {
        this.orderGuid = guid;
    }



    @Override
    public String getProductGuid() {
        return this.productGuid;
    }



    /**
     * Defines the unique identifier of the requested data item.
     *
     * @param guid the string that uniquely identifies the product on its originating server
     */
    public void setProductGuid(final String guid) {
        this.productGuid = guid;
    }



    @Override
    public String getRemark() {
        return this.remark;
    }



    /**
     * Defines the additional information about the processing of this request for the person who
     * ordered it.
     *
     * @param processRemark the string containing the additional information
     */
    public void setRemark(final String processRemark) {
        this.remark = processRemark;
    }



    @Override
    public String getStatus() {
        return this.status;
    }



    /**
     * Defines the current state of this request.
     *
     * @param statusString the state of this request
     */
    public void setStatus(final String statusString) {
        this.status = statusString;
    }



    @Override
    public String getFolderOut() {
        return this.folderOut;
    }



    /**
     * Defines the path of the folder that contains the data produced by the processing of this request.
     *
     * @param outFolderPath the path relative to the folder that contains the data for all requests as set
     *                      in the application settings
     */
    public void setFolderOut(final String outFolderPath) {
        this.folderOut = outFolderPath;
    }



    @Override
    public String getClient() {
        return this.client;
    }



    /**
     * Defines the name of the person who placed the order that this request is a part of.
     *
     * @param clientName the customer's name
     */
    public void setClient(final String clientName) {
        this.client = clientName;
    }



    @Override
    public String getClientGuid() {
        return this.clientGuid;
    }



    /**
     * Defines the name of the person who placed the order that this request is a part of.
     *
     * @param clientName the customer's name
     */
    public void setClientGuid(final String clientGuid) {
        this.clientGuid = clientGuid;
    }



    @Override
    public Calendar getEndDate() {
        return this.endDate;
    }



    /**
     * Defines when this request was exported back to its originating server.
     *
     * @param end the date and time of the export (should be <code>null</code>)
     */
    public void setEndDate(final Calendar end) {
        this.endDate = end;
    }



    @Override
    public String getFolderIn() {
        return this.folderIn;
    }



    /**
     * Defines the path of the folder that contains the data required to process this request.
     *
     * @param inFolderPath the path relative to the folder that contains the data for all requests as set
     *                     in the application settings
     */
    public void setFolderIn(final String inFolderPath) {
        this.folderIn = inFolderPath;
    }



    @Override
    public String getOrderLabel() {
        return this.orderLabel;
    }



    /**
     * Defines the description of the order that this request is a part of.
     *
     * @param label the string that describes the order
     */
    public void setOrderLabel(final String label) {
        this.orderLabel = label;
    }



    @Override
    public String getParameters() {
        return this.parameters;
    }



    /**
     * Defines the additional settings of this request.
     *
     * @param additionalParameters the additional settings as a JSON string
     */
    public void setParameters(final String additionalParameters) {
        this.parameters = additionalParameters;
    }



    @Override
    public String getPerimeter() {
        return this.perimeter;
    }



    /**
     * Defines the extent of the requested data.
     *
     * @param wktPerimeter the geometry of the data to extract as a WKT polygon with WGS84 coordinates
     */
    public void setPerimeter(final String wktPerimeter) {
        this.perimeter = wktPerimeter;
    }



    @Override
    public String getOrganism() {
        return this.organism;
    }



    /**
     * Defines the organization that the person who made this request is part of.
     *
     * @param organismName the name of the organization
     */
    public void setOrganism(final String organismName) {
        this.organism = organismName;
    }



    @Override
    public String getOrganismGuid() {
        return this.organismGuid;
    }



    /**
     * Defines the organization that the person who made this request is part of.
     *
     * @param organismName the name of the organization
     */
    public void setOrganismGuid(final String organismGuid) {
        this.organismGuid = organismGuid;
    }



    @Override
    public String getProductLabel() {
        return this.productLabel;
    }



    /**
     * Defines the description of the requested data item.
     *
     * @param label the string that describes the product
     */
    public void setProductLabel(final String label) {
        this.productLabel = label;
    }



    @Override
    public boolean isRejected() {
        return this.rejected;
    }



    /**
     * Defines whether the processing of this request must be abandoned.
     *
     * @param isRejected <code>true</code> if the process cannot be carried on
     */
    public void setRejected(final boolean isRejected) {
        this.rejected = isRejected;
    }



    @Override
    public Calendar getStartDate() {
        return this.startDate;
    }



    /**
     * Defines when this request was imported from its originating server.
     *
     * @param start the date and time of the import
     */
    public void setStartDate(final Calendar start) {
        this.startDate = start;
    }



    @Override
    public String getTiers() {
        return this.tiers;
    }



    /**
     * Defines the person or organism that this request was ordered on behalf of.
     *
     * @param thirdParty the name of the third party, or <code>null</code> if there is not any
     */
    public void setTiers(final String thirdParty) {
        this.tiers = thirdParty;
    }

}
