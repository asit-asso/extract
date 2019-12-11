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
package org.easysdi.extract.connectors.common;



/**
 * An interface to communicate information about a data item that has been ordered.
 *
 * @author Yves Grasset
 */
public interface IProduct {

    /**
     * Obtains the description of the order that this product is a part of.
     *
     * @return the string that describes the order
     */
    String getOrderLabel();



    /**
     * Obtains the identifier of the order that this product is a part of.
     *
     * @return the string that identifies the order on the originating server
     */
    String getOrderGuid();



    /**
     * Obtains the identifier of this product.
     *
     * @return the string that identifies this product on the server where it was ordered
     */
    String getProductGuid();



    /**
     * Obtains the description of this production.
     *
     * @return the string that describes this product
     */
    String getProductLabel();



    /**
     * Obtains the organization that ordered this product.
     *
     * @return the name of the organization
     */
    String getOrganism();



    /**
     * Obtains the string that identifies the organization that ordered this product.
     *
     * @return the GUID of the organization
     */
    String getOrganismGuid();



    /**
     * Obtains the name of the person who ordered this product.
     *
     * @return the name of the customer
     */
    String getClient();



    /**
     * Obtains the string that identifies the person who ordered this product.
     *
     * @return the GUID of the customer
     */
    String getClientGuid();



    /**
     * Obtains additional information about the person who ordered this product.
     *
     * @return a string with details about the customer (usually contact information)
     */
    String getClientDetails();



    /**
     * Obtains the name of the person that this product was ordered on behalf of, if any.
     *
     * @return the name of the third party, or <code>null</code> if there is not any
     */
    String getTiers();



    /**
     * Obtains additional information about the person that this product was ordered on behalf of, if any.
     *
     * @return a string with details about the third party (usually contact information), or <code>null</code> if
     *         there is not any
     */
    String getTiersDetails();



    /**
     * Obtains the geographical area of the ordered data.
     *
     * @return a string that contains the area as a WKT geometry with WGS84 coordinates
     */
    String getPerimeter();



    /**
     * Obtains the size of the geographical area for the ordered data.
     *
     * @return the area in square meters
     */
    Double getSurface();



    /**
     * Obtains custom settings about the order.
     *
     * @return the additional parameters as a JSON string
     */
    String getOthersParameters();

}
