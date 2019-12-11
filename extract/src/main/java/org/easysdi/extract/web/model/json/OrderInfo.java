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
package org.easysdi.extract.web.model.json;

import com.fasterxml.jackson.annotation.JsonView;



/**
 * A reprsentation of information about a data order that must be formatted as JSON.
 *
 * @author Yves Grasset
 */
public class OrderInfo {

    /**
     * The description of the data order.
     */
    @JsonView(PublicField.class)
    private final String orderLabel;

    /**
     * The description of the ordered data item.
     */
    @JsonView(PublicField.class)
    private final String productLabel;



    /**
     * Obtains the description of the placed order.
     *
     * @return the order label
     */
    public final String getOrderLabel() {
        return this.orderLabel;
    }



    /**
     * Obtains the description of the ordered data item.
     *
     * @return the product label
     */
    public final String getProductLabel() {
        return this.productLabel;
    }



    /**
     * Create a new JSON order information instance.
     *
     * @param order   the string that describes the placed order
     * @param product the string that describes the ordered data item
     */
    public OrderInfo(final String order, final String product) {
        this.orderLabel = order;
        this.productLabel = product;
    }

}
