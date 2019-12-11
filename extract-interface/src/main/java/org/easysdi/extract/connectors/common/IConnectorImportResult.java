/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easysdi.extract.connectors.common;

import java.util.List;



/**
 * Common interface for the result of orders import by a connector.
 *
 * @author Florent Krin
 */
public interface IConnectorImportResult {

    /**
     * Obtains the final state of the import.
     *
     * @return the import status
     */
    boolean getStatus();



    /**
     * Obtains the products that have been imported, if any.
     *
     * @return a collection of product that have been ordered on the server
     */
    List<IProduct> getProductList();



    /**
     * The string that explains why the import failed, if it did.
     *
     * @return the error message, or <code>null</code> if the import succeeded
     */
    String getErrorMessage();

}
