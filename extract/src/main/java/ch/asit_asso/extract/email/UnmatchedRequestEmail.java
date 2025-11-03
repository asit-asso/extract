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
import java.util.Locale;
import ch.asit_asso.extract.domain.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;



/**
 * An electronic message that notifies the administrators that a request didn't match any of the rules
 * defined by its connector.
 *
 * @author Yves Grasset
 */
public class UnmatchedRequestEmail extends Email {

    /**
     * The string that identifies the template to use to define the content of this message.
     */
    private static final String EMAIL_TEMPLATE = "html/unmatchedRequest";

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(UnmatchedRequestEmail.class);



    /**
     * Creates a new instance of this message.
     *
     * @param settings the object that assembles all the configuration objects required to create and send an e-mail
     *                 message
     */
    public UnmatchedRequestEmail(final EmailSettings settings) {
        super(settings);
    }



    /**
     * Configures the message so it is ready to be sent.
     *
     * @param request    the request that didn't match any rule
     * @param recipients an array containing the addresses of the active managers
     * @return <code>true</code> if the message has been successfully initialized
     */
    public final boolean initialize(final Request request, final String[] recipients) {

        if (request == null) {
            throw new IllegalArgumentException("The request cannot be null.");
        }

        if (recipients == null || recipients.length == 0) {
            throw new IllegalArgumentException("The recipients array cannot be empty.");
        }

        if (!this.addRecipients(recipients)) {
            this.logger.error("No recipient could not be defined.");
            return false;
        }

        this.setContentType(ContentType.HTML);

        try {
            this.setContentFromTemplate(UnmatchedRequestEmail.EMAIL_TEMPLATE, this.getModel(request));

        } catch (EmailTemplateNotFoundException exception) {
            this.logger.error("Could not define the body of the message.", exception);
            return false;
        }

        this.setSubject(this.getMessageString("email.unmatchedRequest.subject"));

        return true;
    }



    /**
     * Defines the textual data contained in the message.
     *
     * @param request the request that didn't match any rule
     * @return <code>true</code> if the message content has been successfully initialized
     */
    public final boolean initializeContent(final Request request) {
        return this.initializeContent(request, null);
    }

    /**
     * Defines the textual data contained in the message for a specific locale.
     *
     * @param request the request that didn't match any rule
     * @param locale  the locale to use for the message content, or null to use default
     * @return <code>true</code> if the message content has been successfully initialized
     */
    public final boolean initializeContent(final Request request, final Locale locale) {

        if (request == null) {
            throw new IllegalArgumentException("The request cannot be null.");
        }

        this.setContentType(ContentType.HTML);

        try {
            this.setContentFromTemplate(UnmatchedRequestEmail.EMAIL_TEMPLATE, this.getModel(request, locale));

        } catch (EmailTemplateNotFoundException exception) {
            this.logger.error("Could not define the body of the message.", exception);
            return false;
        }

        this.setSubject(this.getMessageString("email.unmatchedRequest.subject", null, locale));

        return true;
    }



    /**
     * Creates an object that assembles the data to display in the message.
     *
     * @param request the request that didn't match any rule
     * @return the template context object
     */
    private IContext getModel(final Request request) {
        return this.getModel(request, null);
    }

    /**
     * Creates an object that assembles the data to display in the body of this message for a specific locale.
     *
     * @param request the request that didn't match any rule
     * @param locale  the locale to use for the template context, or null to use default
     * @return the context object to feed to the message body template
     */
    private IContext getModel(final Request request, final Locale locale) {
        assert request != null : "The request must be set.";
        assert request.getConnector() != null : "The request connector must be set";

        final Context model = new Context(locale);

        // Add all standard request variables using the utility class
        RequestModelBuilder.addRequestVariables(model, request);

        // Add email-specific variables
        model.setVariable("connectorName", request.getConnector().getName());

        try {
            model.setVariable("dashboardItemUrl", this.getAbsoluteUrl(String.format("/requests/%d",
                    request.getId())));

        } catch (MalformedURLException exception) {
            this.logger.error("Could not get the dashboard item absolute URL.", exception);
        }

        return model;
    }

}
