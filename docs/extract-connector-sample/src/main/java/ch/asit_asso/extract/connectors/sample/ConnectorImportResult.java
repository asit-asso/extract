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

import java.util.ArrayList;
import java.util.List;
import ch.asit_asso.extract.connectors.common.IConnectorImportResult;
import ch.asit_asso.extract.connectors.common.IProduct;



/**
 * The result of an attempt to fetch orders from a remote server.
 *
 * @author Florent Krin
 */
public class ConnectorImportResult implements IConnectorImportResult {

    /**
     * Whether the import succeeded.
     */
    private boolean status;

    /**
     * A text explaining which error occurred, if any.
     */
    private String errorMessage;

    /**
     * The ordered data items fetched from the server.
     */
    private List<IProduct> productList;



    /**
     * Creates a new import result instance.
     */
    public ConnectorImportResult() {
        this.productList = new ArrayList<>();
    }



    @Override
    public final boolean getStatus() {
        return status;
    }



    /**
     * Defines the final state of the import.
     *
     * @param importStatus the import status
     */
    public final void setStatus(final boolean importStatus) {
        this.status = importStatus;
    }



    @Override
    public final String getErrorMessage() {
        return errorMessage;
    }



    /**
     * Defines why the import failed, if it did.
     *
     * @param message a string the describes the error that occurred, or <code>null</code> if no error occurred
     */
    public final void setErrorMessage(final String message) {
        this.errorMessage = message;
    }



    @Override
    public final List<IProduct> getProductList() {
        return productList;
    }



    /**
     * Defines the products that have been imported, if any.
     *
     * @param products a list of ordered products fetched from the remote server
     */
    public final void setProductList(final List<IProduct> products) {
        this.productList = products;
    }



    /**
     * Adds a data item request to those that have been imported from the remote server.
     *
     * @param product the data item to add
     */
    public final void addProduct(final Product product) {
        if (this.productList == null) {
            this.productList = new ArrayList<>();
        }
        this.productList.add(product);
    }



    @Override
    public final String toString() {

        return "[\r\n"
                + "success : " + status + "\r\n"
                + "errorMessage : " + errorMessage + "\r\n"
                + "]";
    }

}
