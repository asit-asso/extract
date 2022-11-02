/*
 * Copyright (C) 2018 arx iT
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
package ch.asit_asso.extract.email;

import java.net.MalformedURLException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.apache.commons.lang3.StringUtils;
import ch.asit_asso.extract.domain.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;



/**
 * An electronic message notifying the administrators that a product could not be imported as a request.
 *
 * @author Yves Grasset
 */
public class InvalidProductImportedEmail extends Email {

    /**
     * The string that identifies the template to use as the body of this message if it is sent as HTML.
     */
    private static final String EMAIL_HTML_TEMPLATE = "html/invalidProductImported";

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(InvalidProductImportedEmail.class);



    /**
     * Creates a new instance of this message.
     *
     * @param settings the objects that are required to create and send an e-mail message.
     */
    public InvalidProductImportedEmail(final EmailSettings settings) {
        super(settings);
    }



    /**
     * Prepares this message so that it is ready to be sent.
     *
     * @param request      the request that could not be imported
     * @param errorMessage the string returned by the connector to explain why the import failed
     * @param importTime   when the import failed
     * @param recipients   an array containing the valid e-mail addresses that this message must be sent to
     * @return <code>true</code> if this message has been successfully initialized
     */
    public final boolean initialize(final Request request, final String errorMessage, final Calendar importTime,
            final String[] recipients) {

        this.logger.debug("Initializing the request import failure e-mail message.");

        if (request == null) {
            throw new IllegalArgumentException("The request cannot be null.");
        }

        if (request.getConnector() == null) {
            throw new IllegalStateException("The request connector must be set.");
        }

        if (errorMessage == null) {
            throw new IllegalArgumentException("The error message cannot be null.");
        }

        if (importTime == null || new GregorianCalendar().before(importTime)) {
            throw new IllegalArgumentException("The import time must be defined and set in the past.");
        }

        if (recipients == null || recipients.length == 0) {
            throw new IllegalArgumentException("The recipients array cannot be empty.");
        }

        this.logger.debug("Adding the recipients : {}", StringUtils.join(recipients, ", "));

        if (!this.addRecipients(recipients)) {
            this.logger.error("Could not add any recipient.");
            return false;
        }

        this.logger.debug("Setting the message content type as HTML.");
        this.setContentType(Email.ContentType.HTML);

        try {
            this.logger.debug("Defining the body of the message.");
            this.setContentFromTemplate(InvalidProductImportedEmail.EMAIL_HTML_TEMPLATE,
                    this.getModel(request, errorMessage, importTime));

        } catch (EmailTemplateNotFoundException exception) {
            this.logger.error("Could not define the body of the request import failure message.", exception);
            return false;
        }

        this.logger.debug("Defining the subject of the message.");
        this.setSubject(this.getMessageString("email.invalidProductImported.subject"));

        this.logger.debug("The request import failure message has been successfully initialized.");
        return true;
    }



    /**
     * Creates an object that assembles the data to display in this message.
     *
     * @param request      the request that could not be imported
     * @param errorMessage the string that explains why the import failed
     * @param importTime   when the import failed
     * @return the context object to feed to the message body template
     */
    private IContext getModel(final Request request, final String errorMessage, final Calendar importTime) {
        assert request != null : "The request cannot be null.";
        assert request.getConnector() != null : "The request connector cannot be null.";
        assert errorMessage != null : "The import message cannot be null.";
        assert importTime != null : "The time of the failed import cannot be null.";
        assert new GregorianCalendar().after(importTime) : "The time of the failed import must be set in the past.";

        Context model = new Context();
        model.setVariable("productLabel", request.getProductLabel());
        model.setVariable("connectorName", request.getConnector().getName());
        model.setVariable("errorMessage", errorMessage);
        model.setVariable("failureTimeString", DateFormat.getDateTimeInstance().format(importTime.getTime()));

        try {
            model.setVariable("dashboardItemUrl", this.getAbsoluteUrl(String.format("/requests/%d",
                    request.getId())));

        } catch (MalformedURLException exception) {
            this.logger.error("Could not get the dashboard item absolute URL.", exception);
        }

        return model;
    }

}
