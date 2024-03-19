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
package ch.asit_asso.extract.web.model.json;

import ch.asit_asso.extract.domain.Connector;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Value;


/**
 * A reprsentation of information about a data order that must be formatted as JSON.
 *
 * @author Yves Grasset
 */
public class OrderInfo {

    /**
     * The name of the connector instance that was used to import this order.
     */
    @JsonView(PublicField.class)
    private String connectorName;

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

    @Value("#{messageProperties['requestDetails.connector.deleted']}")
    private String deletedConnectorText = "(Supprimé)";



    /**
     * Obtains the name of the connector instance that was used to import this order.
     *
     * @return the name of the connector instance
     */
    public final String getConnectorName() {
        return this.connectorName;
    }



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
     * @param order     the string that describes the placed order
     * @param product   the string that describes the ordered data item
     * @param connector the connector instance used to import this order
     */
    public OrderInfo(final String order, final String product, final Connector connector) {

//        if (connector == null) {
//            throw new IllegalArgumentException("The connector cannot be null.");
//        }

        this.connectorName = (connector != null) ? connector.getName() : this.deletedConnectorText;
        this.orderLabel = order;
        this.productLabel = product;
    }

}
