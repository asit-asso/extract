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
import org.apache.commons.lang3.StringUtils;
import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;



/**
 * An electronic message notifying the watchers of a process that a new request has been imported for it.
 *
 * @author Extract Team
 */
public class NewRequestWatcherEmail extends Email {

    /**
     * The string that identifies the template to use as the body of this message if it is sent as HTML.
     */
    private static final String EMAIL_HTML_TEMPLATE = "html/newRequestWatcher";

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(NewRequestWatcherEmail.class);



    /**
     * Creates a new instance of this message.
     *
     * @param settings the objects required to create and send an e-mail message
     */
    public NewRequestWatcherEmail(final EmailSettings settings) {
        super(settings);
    }



    /**
     * Prepares this message so that it is ready to be sent.
     *
     * @param request    the request that was imported for the watched process
     * @param process    the process that the request has been attached to
     * @param recipients an array that contains the valid e-mail addresses that this message must be sent to
     * @return <code>true</code> if this message has been successfully initialized
     */
    public final boolean initialize(final Request request, final Process process, final String[] recipients) {
        this.logger.debug("Initializing the new request watcher notification message.");

        if (recipients == null || recipients.length == 0) {
            throw new IllegalArgumentException("The recipients array cannot be empty.");
        }

        this.logger.debug("Initializing the message content.");

        if (!this.initializeContent(request, process, null)) {
            this.logger.error("Could not set the message content.");
            return false;
        }

        this.logger.debug("Adding the recipients : {}", StringUtils.join(recipients, ", "));

        if (!this.addRecipients(recipients)) {
            this.logger.error("Could not add any recipient.");
            return false;
        }

        this.logger.debug("The new request watcher notification message has been successfully initialized.");
        return true;
    }



    /**
     * Defines the textual data contained in the message for a specific locale.
     *
     * @param request the request that was imported for the watched process
     * @param process the process that the request has been attached to
     * @param locale  the locale to use for the message content, or <code>null</code> to use the default locale
     * @return <code>true</code> if the message content has been successfully initialized
     */
    public final boolean initializeContent(final Request request, final Process process, final Locale locale) {

        if (request == null) {
            throw new IllegalArgumentException("The request cannot be null.");
        }

        if (process == null) {
            throw new IllegalArgumentException("The process cannot be null.");
        }

        this.logger.debug("Defining the content type to HTML.");
        this.setContentType(ContentType.HTML);

        try {
            this.logger.debug("Defining the message body");
            this.setContentFromTemplate(NewRequestWatcherEmail.EMAIL_HTML_TEMPLATE, this.getModel(request, process,
                    locale));

        } catch (EmailTemplateNotFoundException exception) {
            this.logger.error("Could not define the message body.", exception);
            return false;
        }

        this.logger.debug("Defining the subject of the message.");
        this.setSubject(this.getMessageString("email.newRequestWatcher.subject",
                new Object[]{process.getName()}, locale));

        this.logger.debug("The new request watcher notification message content has been successfully initialized.");
        return true;
    }



    /**
     * Creates an object that assembles the data to display in the body of this message for a specific locale.
     *
     * @param request the request that was imported for the watched process
     * @param process the process that the request has been attached to
     * @param locale  the locale to use for the template context, or <code>null</code> to use the default locale
     * @return the context object to feed to the message body template
     */
    private IContext getModel(final Request request, final Process process, final Locale locale) {
        assert request != null : "The request cannot be null";
        assert process != null : "The process cannot be null.";

        final Context model = new Context(locale);

        // Add all standard request variables using the utility class
        RequestModelBuilder.addRequestVariables(model, request);

        // Add email-specific variables
        model.setVariable("processName", process.getName());

        try {
            model.setVariable("dashboardItemUrl", this.getAbsoluteUrl(String.format("/requests/%d",
                    request.getId())));

        } catch (MalformedURLException exception) {
            this.logger.error("Could not get the dashboard item absolute URL.", exception);
        }

        return model;
    }

}
