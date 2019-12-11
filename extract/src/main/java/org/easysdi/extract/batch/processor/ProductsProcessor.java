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
package org.easysdi.extract.batch.processor;

import java.util.GregorianCalendar;
import org.apache.commons.lang3.StringUtils;
import org.easysdi.extract.connectors.common.IProduct;
import org.easysdi.extract.domain.Connector;
import org.easysdi.extract.domain.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Scope;



/**
 * An object that converts ordered products obtained through a connector into requests.
 *
 * @author Yves Grasset
 */
@Scope("step")
public class ProductsProcessor implements ItemProcessor<IProduct, Request> {

    /**
     * The instance of the connector through which the orders have been fetched.
     */
    private final Connector connectorInstance;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(ProductsProcessor.class);



    /**
     * Creates a new instance of the orders processor.
     *
     * @param instance the instance of the connector through which the orders have been fetched
     */
    public ProductsProcessor(final Connector instance) {

        if (instance == null) {
            throw new IllegalArgumentException("The connector instance cannot be null.");
        }

        this.connectorInstance = instance;
    }



    @Override
    public final Request process(final IProduct product) throws Exception {

        try {
            this.logger.debug("Processing product {} from order {}.", product.getProductLabel(),
                    product.getOrderLabel());
            Request request = new Request();
            request.setConnector(this.connectorInstance);
            request.setClient(product.getClient());
            request.setClientGuid(product.getClientGuid());
            request.setClientDetails(product.getClientDetails());
            request.setOrderGuid(product.getOrderGuid());
            request.setOrderLabel(product.getOrderLabel());
            request.setOrganism(product.getOrganism());
            request.setOrganismGuid(product.getOrganismGuid());
            request.setParameters(product.getOthersParameters());
            request.setPerimeter(product.getPerimeter());
            request.setProductGuid(product.getProductGuid());
            request.setProductLabel(product.getProductLabel());
            request.setTiers(product.getTiers());
            request.setTiersDetails(product.getTiersDetails());
            request.setSurface(product.getSurface());
            request.setStartDate(new GregorianCalendar());
            Request.Status importStatus = Request.Status.IMPORTED;

            if (StringUtils.isBlank(product.getPerimeter())) {
                importStatus = Request.Status.IMPORTFAIL;
            }

            request.setStatus(importStatus);

            return request;

        } catch (Exception exception) {
            this.logger.error("Could not convert the product in a request object.", exception);
            throw exception;
        }
    }

}
