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
package org.easysdi.extract.email;

import java.net.MalformedURLException;
import org.apache.commons.lang3.StringUtils;
import org.easysdi.extract.domain.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;



/**
 * An electronic message notifying the operators that a task processing a request requires an intervention.
 *
 * @author Yves Grasset
 */
public class TaskStandbyEmail extends Email {

    /**
     * The string that identifies the template to use as the body of this message if it is sent as HTML.
     */
    private static final String EMAIL_HTML_TEMPLATE = "html/taskStandby";

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(TaskStandbyEmail.class);



    /**
     * Creates a new instance of this message.
     *
     * @param settings the objects required to create and send an e-mail message
     */
    public TaskStandbyEmail(final EmailSettings settings) {
        super(settings);
    }



    /**
     * Prepares this message so that it is ready to be sent.
     *
     * @param request    the request that was processed by the task that ended in standby mode
     * @param recipients an array that contains the valid e-mail addresses that this message must be sent to
     * @return <code>true</code> if this message has been successfully initialized
     */
    public final boolean initialize(final Request request, final String[] recipients) {
        this.logger.debug("Initializing the task failure message.");

        if (recipients == null || recipients.length == 0) {
            throw new IllegalArgumentException("The recipients array cannot be empty.");
        }

        this.logger.debug("Initializing the message content.");

        if (!this.initializeContent(request)) {
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
     * @param request the request that was processed by the task that ended in standby mode
     * @return <code>true</code> if the message content has been successfully initialized
     */
    public final boolean initializeContent(final Request request) {

        if (request == null) {
            throw new IllegalArgumentException("The request cannot be null.");
        }

        this.logger.debug("Defining the content type to HTML.");
        this.setContentType(ContentType.HTML);

        try {
            this.logger.debug("Defining the message body");
            this.setContentFromTemplate(TaskStandbyEmail.EMAIL_HTML_TEMPLATE, this.getModel(request));

        } catch (EmailTemplateNotFoundException exception) {
            this.logger.error("Could not define the message body.", exception);
            return false;
        }

        this.logger.debug("Defining the subject of the message.");
        this.setSubject(this.getMessageString("email.taskStandby.subject",
                new Object[]{request.getProcess().getName()}));

        this.logger.debug("The task failure message content has been sucessfully initilized.");
        return true;
    }



    /**
     * Creates an object that assembles the data to display in the body of this message.
     *
     * @param request the request that was processed by the task that ended in standby mode
     * @return the context object to feed to the message body template
     */
    private IContext getModel(final Request request) {
        assert request != null : "The request cannot be null";
        assert request.getProcess() != null : "The process attached to the request cannot be null.";

        final Context model = new Context();
        model.setVariable("processName", request.getProcess().getName());
        model.setVariable("productLabel", request.getProductLabel());

        try {
            model.setVariable("dashboardItemUrl", this.getAbsoluteUrl(String.format("/requests/%d",
                    request.getId())));

        } catch (MalformedURLException exception) {
            this.logger.error("Could not get the dashboard item absolute URL.", exception);
        }

        return model;
    }

}
