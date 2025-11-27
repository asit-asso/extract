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
package ch.asit_asso.extract.connectors.sample;

import java.util.Calendar;
import ch.asit_asso.extract.connectors.common.IExportRequest;



/**
 * The description of an ordered data item that has been processed and whose result must be sent to the
 * originating server.
 *
 * @author Florent Krin
 */
public class ExportRequest implements IExportRequest {

    /**
     * The path of the folder that contains the data necessary to process the request, relative to the
     * folder that contains the data for all requests.
     */
    private String folderIn;

    /**
     * The path of the folder that generating by processing the request, relative to the
     * folder that contains the data for all requests.
     */
    private String folderOut;

    /**
     * The name of the person that ordered the data item.
     */
    private String client;

    /**
     * The indentifying string of the person that ordered the data item.
     */
    private String clientGuid;

    /**
     * The string that identifies the order that this request is part of.
     */
    private String orderGuid;

    /**
     * The description of the order that this request is part of.
     */
    private String orderLabel;

    /**
     * The custom parameters of the order that this request is part of.
     */
    private String parameters;

    /**
     * The geographical area of the data to extract, as a WKT geometry with WGS84 coordinates.
     */
    private String perimeter;

    /**
     * The size of the geographical area in square meters.
     */
    private Double surface;

    /**
     * The string that identifies the ordered data item.
     */
    private String productGuid;

    /**
     * The description of the ordered data item.
     */
    private String productLabel;

    /**
     * The name of the person that this data item was ordered on behalf of, if any.
     */
    private String tiers;

    /**
     * Additional information for the final customer about the request process.
     */
    private String remark;

    /**
     * Whether the request process had to be abandoned.
     */
    private boolean rejected;

    /**
     * The current state of this request. (It should be <code>TOEXPORT</code>.)
     */
    private String status;

    /**
     * When this request was imported for processing.
     */
    private Calendar startDate;

    /**
     * When the process completed successfully. (It should be <code>null</code>.)
     */
    private Calendar endDate;



    @Override
    public final String getOrderGuid() {
        return this.orderGuid;
    }



    /**
     * Defines the identifier of the order that this request is part of.
     *
     * @param guid the string that identifies the order
     */
    public final void setOrderGuid(final String guid) {
        this.orderGuid = guid;
    }



    @Override
    public final String getProductGuid() {
        return this.productGuid;
    }



    /**
     * Defines the identifier of the ordered data item.
     *
     * @param guid the string that identifies the product
     */
    public final void setProductGuid(final String guid) {
        this.productGuid = guid;
    }



    @Override
    public final String getRemark() {
        return this.remark;
    }



    /**
     * Defines additional information for the final customer about the request process.
     *
     * @param remarkString the remark string
     */
    public final void setRemark(final String remarkString) {
        this.remark = remarkString;
    }



    @Override
    public final String getStatus() {
        return this.status;
    }



    /**
     * The processing state of this request.
     *
     * @param currentStatus the status. At this point, it should normally be <code>TOEXPORT</code>
     */
    public final void setStatus(final String currentStatus) {
        this.status = currentStatus;
    }



    @Override
    public final String getFolderOut() {
        return folderOut;
    }



    /**
     * Defines the path of the folder that contains the data produced by processing this request.
     *
     * @param folderOutPath a string with the path of the output folder relative to the folder that holds the data for
     *                      all requests
     */
    public final void setFolderOut(final String folderOutPath) {
        this.folderOut = folderOutPath;
    }



    @Override
    public final String getClient() {
        return this.client;
    }



    /**
     * Defines the person that ordered this data item.
     *
     * @param customerName the name of the customer
     */
    public final void setClient(final String customerName) {
        this.client = customerName;
    }



    @Override
    public final String getClientGuid() {
        return this.clientGuid;
    }



    /**
     * Defines the person that ordered this data item.
     *
     * @param customerName the name of the customer
     */
    public final void setClientGuid(final String customerGuid) {
        this.clientGuid = customerGuid;
    }



    @Override
    public final Calendar getEndDate() {
        return this.endDate;
    }



    /**
     * Defines when the request process successfully completed.
     *
     * @param end the end date. At this stage, it should normally be <code>null</code>
     */
    public final void setEndDate(final Calendar end) {
        this.endDate = end;
    }



    @Override
    public final String getFolderIn() {
        return this.folderIn;
    }



    /**
     * Defines the path of the folder that contains the data necessary to process this request.
     *
     * @param folderInPath a string with the path of the input folder relative to the folder that holds the data for
     *                     all requests
     */
    public final void setFolderIn(final String folderInPath) {
        this.folderIn = folderInPath;
    }



    @Override
    public final String getOrderLabel() {
        return this.orderLabel;
    }



    /**
     * Defines the description of the order that this request is part of.
     *
     * @param label the string that describes the order
     */
    public final void setOrderLabel(final String label) {
        this.orderLabel = label;
    }



    @Override
    public final String getParameters() {
        return this.parameters;
    }



    /**
     * Defines the custom parameters of the order that this request is part of.
     *
     * @param parametersJson a string that contains the parameters and their value in JSON format
     */
    public final void setParameters(final String parametersJson) {
        this.parameters = parametersJson;
    }



    @Override
    public final String getPerimeter() {
        return this.perimeter;
    }



    /**
     * Defines the geographical area of the data to extract.
     *
     * @param wktPerimeter a string that contains the perimeter as a WKT geometry with WGS84 coordinates
     */
    public final void setPerimeter(final String wktPerimeter) {
        this.perimeter = wktPerimeter;
    }



    @Override
    public final Double getSurface() {
        return this.surface;
    }



    /**
     * Defines the size of the data extraction area.
     *
     * @param areaSize the area in square meters
     */
    public final void setSurface(final Double areaSize) {
        this.surface = areaSize;
    }



    @Override
    public final String getProductLabel() {
        return this.productLabel;
    }



    /**
     * Defines the description of the ordered data item.
     *
     * @param label a string that describes the data item
     */
    public final void setProductLabel(final String label) {
        this.productLabel = label;
    }



    @Override
    public final Calendar getStartDate() {
        return this.startDate;
    }



    /**
     * Defines when this request was imported for processing.
     *
     * @param start the import date
     */
    public final void setStartDate(final Calendar start) {
        this.startDate = start;
    }



    @Override
    public final String getTiers() {
        return this.tiers;
    }



    /**
     * Defines the name of the person that this data time was ordered on behalf of.
     *
     * @param thirdPartyName the name of the third party, or <code>null</code> if there is not any
     */
    public final void setTiers(final String thirdPartyName) {
        this.tiers = thirdPartyName;
    }



    @Override
    public final boolean isRejected() {
        return this.rejected;
    }



    /**
     * Defines whether the processing of this request had to be abandoned.
     *
     * @param isRejected <code>true</code> if this request could not be processed
     */
    public final void setRejected(final boolean isRejected) {
        this.rejected = isRejected;
    }

}
