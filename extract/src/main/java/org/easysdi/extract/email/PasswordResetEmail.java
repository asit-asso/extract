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

import javax.mail.internet.AddressException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;



/**
 * An electronic message to send a code to the user that request to redefine her password.
 *
 * @author Yves Grasset
 */
public class PasswordResetEmail extends Email {

    /**
     * The name of the template to use to define the message body.
     */
    private static final String TEMPLATE_NAME = "html/passwordReset";

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(PasswordResetEmail.class);



    /**
     * Creates a new instance of this message.
     *
     * @param emailSettings the object that contains the parameters required to create and send e-mail messages
     */
    public PasswordResetEmail(final EmailSettings emailSettings) {
        super(emailSettings);
    }



    /**
     * Configures the message so it is ready to be sent.
     *
     * @param token     the code that allows the user to reset his password
     * @param recipient the user's e-mail address
     * @return <code>true</code> if the message has been successfully initialized
     */
    public final boolean initialize(final String token, final String recipient) {

        if (StringUtils.isEmpty(token)) {
            throw new IllegalArgumentException("The token cannot be empty.");
        }

        try {
            this.addRecipient(recipient);

        } catch (AddressException exception) {
            this.logger.error("Could not set the recipient.", exception);
            return false;
        }

        this.setContentType(ContentType.HTML);

        try {
            this.setContentFromTemplate(PasswordResetEmail.TEMPLATE_NAME, this.getModel(token));

        } catch (EmailTemplateNotFoundException exception) {
            this.logger.error("Could not define the body of the e-mail message.", exception);
            return false;
        }

        this.setSubject(this.getMessageString("email.passwordReset.subject"));

        return true;
    }



    /**
     * Creates an object that assembles the data to display in the message.
     *
     * @param token the code that allows the user to change her password
     * @return the template context object
     */
    private IContext getModel(final String token) {
        assert token != null : "The token must be set.";

        Context model = new Context();
        model.setVariable("token", token);

        return model;
    }

}
