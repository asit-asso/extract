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

import ch.asit_asso.extract.connectors.common.IProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;



/**
 * A data item request that has been imported.
 *
 * @author Yves Grasset
 */
public class Product implements IProduct {

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(ExportRequest.class);

    /**
     * The description of the order that this request is part of.
     */
    private String orderLabel;

    /**
     * The identifier of the order that this request is part of.
     */
    private String orderGuid;

    /**
     * The identifier of the requested data item.
     */
    private String productGuid;

    /**
     * The description of the requested data item.
     */
    private String productLabel;

    /**
     * The name of the organization that ordered this data item.
     */
    private String organism;

    /**
     * The identifying string of the organization that ordered this data item.
     */
    private String organismGuid;

    /**
     * The name of the person that ordered this data item.
     */
    private String client;

    /**
     * The identifying string of the person that ordered this data item.
     */
    private String clientGuid;

    /**
     * Additional information (usually contact information) about the person that ordered this data item.
     */
    private String clientDetails;

    /**
     * The name of the person that this data item was requested on behalf of, if any.
     */
    private String tiers;

    /**
     * The string that uniquely identifies the person that this data item was requested on behalf of, if any.
     */
    private String tiersGuid;

    /**
     * Additional information (usually contact information) about the person that this data item was
     * requested on behalf of, if any.
     */
    private String tiersDetails;

    /**
     * The geographical area of the data to extract, as a WKT geometry with WGS84 coordinates.
     */
    private String perimeter;

    /**
     * The size of the extract area in square meters.
     */
    private Double surface;

    /**
     * Additional settings for the processing of this request.
     */
    private String othersParameters;

    /**
     * The address that provides an access to the details of this order on the originating server.
     */
    private String externalUrl;



    @Override
    public final String getOrderLabel() {
        return this.orderLabel;
    }



    /**
     * Defines the description of the order that this product request is part of.
     *
     * @param label the order label
     */
    public final void setOrderLabel(final String label) {
        this.orderLabel = label;
    }



    @Override
    public final String getOrderGuid() {
        return this.orderGuid;
    }



    /**
     * Defines the identifier of the order that this product request is part of.
     *
     * @param guid the order identifier
     */
    public final void setOrderGuid(final String guid) {
        this.orderGuid = guid;
    }



    @Override
    public final String getProductGuid() {
        return this.productGuid;
    }



    /**
     * Defines the identifier of the requested data item.
     *
     * @param guid the product identifier
     */
    public final void setProductGuid(final String guid) {
        this.productGuid = guid;
    }



    @Override
    public final String getProductLabel() {
        return this.productLabel;
    }



    /**
     * Defines the description of the requested data item.
     *
     * @param label the product label
     */
    public final void setProductLabel(final String label) {
        this.productLabel = label;
    }



    @Override
    public final String getOrganism() {
        return this.organism;
    }



    /**
     * Defines the organization that requested this product.
     *
     * @param organismName the name of the organization
     */
    public final void setOrganism(final String organismName) {
        this.organism = organismName;
    }



    @Override
    public final String getOrganismGuid() {
        return this.organismGuid;
    }



    /**
     * Defines the organization that requested this product.
     *
     * @param guid the string that uniquely identifies the organization
     */
    public final void setOrganismGuid(final String guid) {
        this.organismGuid = guid;
    }



    @Override
    public final String getClient() {
        return this.client;
    }



    /**
     * Defines the person who requested this data item.
     *
     * @param name the customer's name
     */
    public final void setClient(final String name) {
        this.client = name;
    }



    @Override
    public final String getClientGuid() {
        return this.clientGuid;
    }



    /**
     * Defines the person who requested this data item.
     *
     * @param guid the string that uniquely identifies the customer
     */
    public final void setClientGuid(final String guid) {
        this.clientGuid = guid;
    }



    @Override
    public final String getClientDetails() {
        return this.clientDetails;
    }



    /**
     * Defines additional information (usually contact information) about the person who requested
     * this product.
     *
     * @param details a string with information about the customer
     */
    public final void setClientDetails(final String details) {
        this.clientDetails = details;
    }



    @Override
    public final String getTiers() {
        return this.tiers;
    }



    /**
     * Defines the person that this product was requested on behalf of.
     *
     * @param name the name of the third party, or <code>null</code> if there is not any
     */
    public final void setTiers(final String name) {
        this.tiers = name;
    }


    @Override
    public final String getTiersGuid() { return this.tiersGuid; }



    /**
     * Defines the string that uniquely identifies the person that this product was requested on behalf of.
     *
     * @param tiersGuid the identifier of the third party, or <code>null</code> if there is not any
     */
    public final void setTiersGuid(final String tiersGuid) {
        this.tiersGuid = tiersGuid;
    }



    @Override
    public final String getTiersDetails() {
        return this.tiersDetails;
    }



    /**
     * Defines additional information (usually contact information) about the person that this product
     * was requested on behalf of, if there is any.
     *
     * @param details a string with additional information about the third party, or <code>null</code> if there is
     *                not any
     */
    public final void setTiersDetails(final String details) {
        this.tiersDetails = details;
    }



    @Override
    public final String getPerimeter() {
        return this.perimeter;
    }



    /**
     * Defines the geographical area of the data to extract.
     *
     * @param wktGeometry the extract area as a WKT geometry with WGS84 coordinates
     */
    public final void setPerimeter(final String wktGeometry) {
        this.perimeter = wktGeometry;
    }



    @Override
    public final Double getSurface() {
        return this.surface;
    }



    /**
     * Defines the size of the extract area.
     *
     * @param areaSize the area size in square meters
     */
    public final void setSurface(final Double areaSize) {
        this.surface = areaSize;
    }



    @Override
    public final String getOthersParameters() {
        return this.othersParameters;
    }



    /**
     * Defines additional settings to process this product request.
     *
     * @param parametersJson a string with the parameters and their value in JSON format
     */
    public final void setOthersParameters(final String parametersJson) {
        this.othersParameters = parametersJson;
    }



    @Override
    public final String getExternalUrl() {
        return this.externalUrl;
    }



    /**
     * Defines the address that provides an access to the details of this order on the originating server.
     *
     * @param url the address of the order on the source server, or <code>null</code> if there is no such URL
     */
    public final void setExternalUrl(final String url) {

        if (url == null) {
            this.externalUrl = null;
            return;
        }

        try {
            URL inputAsUrl = new URL(url);

            if (!inputAsUrl.toURI().isAbsolute()) {
                this.logger.error("The external address for the order details must be absolute.");
                this.externalUrl = null;
            }

            this.externalUrl = inputAsUrl.toString();

        } catch (MalformedURLException | URISyntaxException exception) {
            this.logger.error("The given external address is not a valid URL.", exception);
            this.externalUrl = null;
        }
    }

}
