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
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;



/**
 * An electronic message notifying the operators that a task processing a request failed.
 *
 * @author Yves Grasset
 */
public class TaskFailedEmail extends Email {

    /**
     * The string that identifies the template to use as the body of this message if it is sent as HTML.
     */
    private static final String EMAIL_HTML_TEMPLATE = "html/taskFailed";

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(TaskFailedEmail.class);



    /**
     * Creates a new instance of this message.
     *
     * @param settings the objects required to create and send an e-mail message
     */
    public TaskFailedEmail(final EmailSettings settings) {
        super(settings);
    }



    /**
     * Prepares this message so that it is ready to be sent.
     *
     * @param task         the task that failed
     * @param request      the request that was processed by the task that failed
     * @param errorMessage the string returned by the task plugin to explain why it failed
     * @param failureTime  when the task failed
     * @param recipients   an array that contains the valid e-mail addresses that this message must be sent to
     * @return <code>true</code> if this message has been successfully initialized
     */
    public final boolean initialize(final Task task, final Request request, final String errorMessage,
            final Calendar failureTime, final String[] recipients) {
        this.logger.debug("Initializing the task failure message.");

        if (recipients == null || recipients.length == 0) {
            throw new IllegalArgumentException("The recipients array cannot be empty.");
        }

        this.logger.debug("Initializing the message content.");

        if (!this.initializeContent(task, request, errorMessage, failureTime)) {
            this.logger.error("Could not set the message content.");
            return false;
        }

        this.logger.debug("Adding the recipients : {}", StringUtils.join(recipients, ", "));

        if (!this.addRecipients(recipients)) {
            this.logger.error("Could not add any recipient.");
            return false;
        }

        this.logger.debug("The task failure message has been successfully initialized.");
        return true;
    }



    /**
     * Defines the textual data contained in the message.
     *
     * @param task         the task that failed
     * @param request      the request that was processed by the task that failed
     * @param errorMessage the string returned by the task plugin to explain why it failed
     * @param failureTime  when the task failed
     * @return <code>true</code> if the message content has been successfully initialized
     */
    public final boolean initializeContent(final Task task, final Request request, final String errorMessage,
            final Calendar failureTime) {

        if (task == null) {
            throw new IllegalArgumentException("The task cannot be null.");
        }

        if (request == null) {
            throw new IllegalArgumentException("The request cannot be null.");
        }

        if (errorMessage == null) {
            throw new IllegalArgumentException("The error message cannot be null.");
        }

        if (failureTime == null || new GregorianCalendar().before(failureTime)) {
            throw new IllegalArgumentException("The failure time must be defined and set in the past.");
        }

        this.logger.debug("Defining the content type to HTML.");
        this.setContentType(ContentType.HTML);

        try {
            this.logger.debug("Defining the message body");
            this.setContentFromTemplate(TaskFailedEmail.EMAIL_HTML_TEMPLATE,
                    this.getModel(task, request, errorMessage, failureTime));

        } catch (EmailTemplateNotFoundException exception) {
            this.logger.error("Could not define the message body.", exception);
            return false;
        }

        this.logger.debug("Defining the subject of the message.");
        this.setSubject(this.getMessageString("email.taskFailed.subject", new Object[]{task.getLabel()}));

        this.logger.debug("The task failure message content has been sucessfully initilized.");
        return true;
    }



    /**
     * Creates an object that assembles the data to display in the body of this message.
     *
     * @param task         the task that failed
     * @param request      the request that was processed by the task
     * @param errorMessage the string returned by the task plugin to explain why it failed
     * @param failureTime  when the task failed
     * @return the context object to feed to the message body template
     */
    private IContext getModel(final Task task, final Request request, final String errorMessage,
            final Calendar failureTime) {
        assert task != null : "The task cannot be null.";
        assert request != null : "The request cannot be null";
        assert errorMessage != null : "The error message cannot be null";
        assert failureTime != null : "The time of the failure cannot be null.";
        assert new GregorianCalendar().after(failureTime) : "The failure time must be set in the past.";

        final Context model = new Context();
        
        // Add all standard request variables using the utility class
        RequestModelBuilder.addRequestVariables(model, request);
        
        // Add task-specific variables
        model.setVariable("taskName", task.getLabel());
        model.setVariable("errorMessage", errorMessage);
        model.setVariable("failureTimeString", DateFormat.getDateTimeInstance().format(failureTime.getTime()));

        try {
            model.setVariable("dashboardItemUrl", this.getAbsoluteUrl(String.format("/requests/%d",
                    request.getId())));

        } catch (MalformedURLException exception) {
            this.logger.error("Could not get the dashboard item absolute URL.", exception);
        }

        return model;
    }

}
