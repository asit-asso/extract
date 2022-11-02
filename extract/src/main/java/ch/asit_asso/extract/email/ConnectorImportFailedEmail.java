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
package ch.asit_asso.extract.email;

import java.net.MalformedURLException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.apache.commons.lang3.StringUtils;
import ch.asit_asso.extract.domain.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;



/**
 * An electronic message that notifies the administrators that an orders import operation failed.
 *
 * @author Yves Grasset
 */
public class ConnectorImportFailedEmail extends Email {

    /**
     * The string that identifies the template to use to display the body of the message.
     */
    private static final String EMAIL_TEMPLATE = "html/importFailed";

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(ConnectorImportFailedEmail.class);



    /**
     * Creates a new instance of this message.
     *
     * @param settings the objects required to create and send e-mail messages
     */
    public ConnectorImportFailedEmail(final EmailSettings settings) {
        super(settings);
    }



    /**
     * Prepares this message so that it is ready to be sent.
     *
     * @param connector        the connector instance used by the failed import
     * @param errorMessage     the string returned by the connector to explain why the import failed
     * @param failedImportTime when the import failed
     * @param recipients       an array containing the valid adresses that this message must be sent to
     * @return <code>true</code> if the message has been successfully initialized
     */
    public final boolean initialize(final Connector connector, final String errorMessage,
            final Calendar failedImportTime, final String[] recipients) {
        this.logger.debug("Initializing the import fail e-mail message.");

        if (connector == null) {
            throw new IllegalArgumentException("The connector cannot be null.");
        }

        if (errorMessage == null) {
            throw new IllegalArgumentException("The error message cannot be null.");
        }

        if (failedImportTime == null || new GregorianCalendar().before(failedImportTime)) {
            throw new IllegalArgumentException("The failure time is invalid.");
        }

        if (recipients == null || recipients.length == 0) {
            throw new IllegalArgumentException("The recipients array cannot be empty.");
        }

        this.logger.debug("Defining the recipients : {}", StringUtils.join(recipients, ", "));

        if (!this.addRecipients(recipients)) {
            this.logger.error("No recipient could be added to the message.");
            return false;
        }

        this.logger.debug("Defining the content type to HTML.");
        this.setContentType(ContentType.HTML);

        try {
            this.logger.debug("Merging the data with the template as the e-mail body.");
            this.setContentFromTemplate(ConnectorImportFailedEmail.EMAIL_TEMPLATE,
                    this.getModel(connector, errorMessage, failedImportTime));

        } catch (EmailTemplateNotFoundException exception) {
            this.logger.error("Could not define the body of the message.", exception);
            return false;
        }

        this.logger.debug("Defining the subject of the message.");
        this.setSubject(this.getMessageString("email.connectorImportFailed.subject"));

        this.logger.debug("The import failure message has been successfully initialized.");
        return true;
    }



    /**
     * Creates an object that assembles the data to display in the message body.
     *
     * @param connector        the connector instance used by the failed import
     * @param errorMessage     the string returned by the connector to explain why the import failed
     * @param failedImportTime when the import failed
     * @return the context object to feed to the body template
     */
    private IContext getModel(final Connector connector, final String errorMessage, final Calendar failedImportTime) {
        assert connector != null : "The connector cannot be null.";
        assert errorMessage != null : "The error message cannot be null.";
        assert failedImportTime != null : "The import failure time cannot be null.";
        assert new GregorianCalendar().after(failedImportTime) : "The import failure time must be set in the past.";

        this.logger.debug("Defining the data model to merge with the template.");
        final Context model = new Context();
        model.setVariable("connectorName", connector.getName());
        model.setVariable("errorMessage", errorMessage);
        model.setVariable("failureTimeString", DateFormat.getDateTimeInstance().format(failedImportTime.getTime()));

        try {
            model.setVariable("dashboardUrl", this.getAbsoluteUrl("/"));

        } catch (MalformedURLException exception) {
            this.logger.error("Could not get the dashboard absolute URL.", exception);
        }

        return model;
    }

}
