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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;
import org.thymeleaf.exceptions.TemplateInputException;
import org.thymeleaf.util.StringUtils;



/**
 * An electronic message sent by the application.
 *
 * @author Yves Grasset
 */
@Component
public abstract class Email {

    /**
     * The character set to use to encode the content of this message.
     */
    private static final String TEXT_ENCODING = "UTF-8";

    /**
     * The type of data used by the body of this message.
     */
    private ContentType contentType;

    /**
     * The title of this message.
     */
    private String subject;

    /**
     * The body of this message.
     */
    private String content;

    /**
     * A list of addresses that this message must be sent to.
     */
    private final List<InternetAddress> recipients;

    /**
     * The parameters to use by the application to send electronic messages.
     */
    private final EmailSettings emailSettings;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(Email.class);



    /**
     * The possible types of data that can be used to define the body of an e-mail message.
     */
    public enum ContentType {
        /**
         * The body of the e-mail is formatted as HTML.
         */
        HTML,
        /**
         * The body of the e-mail is sent as plain text, with no formatting.
         */
        TEXT
    }



    /**
     * Creates a new instance of this message.
     *
     * @param settings the object that contains the data required to create and send an e-mail message
     */
    public Email(final EmailSettings settings) {

        if (settings == null) {
            throw new IllegalArgumentException("The e-mail settings object cannot be null.");
        }

        this.emailSettings = settings;
        this.contentType = ContentType.TEXT;
        this.recipients = new ArrayList<>();
    }



    /**
     * Obtains the body of this message.
     *
     * @return the string representing the body of the message
     */
    public final String getContent() {
        return this.content;
    }



    /**
     * Defines the body of this message.
     *
     * @param text the string representing the body of the message
     */
    public final void setContent(final String text) {

        if (StringUtils.isEmpty(text)) {
            throw new IllegalArgumentException("The message content cannot be empty.");
        }

        this.content = text;
    }



    /**
     * Defines the body of this message by processing a message template.
     *
     * @param templateName the name of the template to process to define the message body
     * @param model        the data to display in the template
     * @throws EmailTemplateNotFoundException if the template name could not be resolved by the template engine
     */
    public final void setContentFromTemplate(final String templateName, final IContext model)
            throws EmailTemplateNotFoundException {

        if (StringUtils.isEmpty(templateName)) {
            throw new IllegalArgumentException("The template name cannot be null");
        }

        if (model == null) {
            throw new IllegalArgumentException("The model cannot be null.");
        }

        String messageContent;

        try {
            messageContent = this.getTemplateEngine().process(templateName, model);

        } catch (TemplateInputException exception) {
            throw new EmailTemplateNotFoundException(templateName, exception);
        }

        this.logger.debug("Parsed content:\n{}", messageContent);

        if (StringUtils.isEmpty(messageContent)) {
            throw new EmailTemplateNotFoundException(templateName);
        }

        this.setContent(messageContent);
    }



    /**
     * Obtains the type of data used for the body of this message.
     *
     * @return the content type
     */
    public final ContentType getContentType() {
        return this.contentType;
    }



    /**
     * Defines the type of data used for the body of this message.
     *
     * @param messageContentType the content type
     */
    public final void setContentType(final ContentType messageContentType) {

        if (messageContentType == null) {
            throw new IllegalArgumentException("The message content type cannot be null.");
        }

        this.contentType = messageContentType;
    }



    /**
     * Obtains the application string that matches the given key.
     *
     * @param messageKey the key that identifies the desired message
     * @return the message
     */
    protected final String getMessageString(final String messageKey) {
        return this.emailSettings.getMessageString(messageKey);
    }



    /**
     * Obtains the application string that matches the given key.
     *
     * @param messageKey the key that identifies the desired message
     * @param arguments  an array of object that will replace the placeholders in the message string, or
     *                   <code>null</code> if no substitution is needed
     * @return the message
     */
    protected final String getMessageString(final String messageKey, final Object[] arguments) {
        return this.emailSettings.getMessageString(messageKey, arguments);
    }



    /**
     * Returns the full address to access a given ressource from the outside.
     *
     * @param relativeUrl the address of the ressource relative to the application root
     * @return the absolute URL
     * @throws MalformedURLException if the relative URL is not valid
     */
    protected final String getAbsoluteUrl(final String relativeUrl) throws MalformedURLException {
        return this.emailSettings.getAbsoluteUrl(relativeUrl);
    }



    /**
     * Adds a collection of addresses to the recipients of this message. Invalid e-mail addresses will throw an
     * exception. If you want these to be silently ignored, please use the {@link #addRecipients(java.lang.String[])}
     * method instead.
     *
     * @param recipientsArray an array that contains the addresses that this message must be sent to
     * @throws AddressException if a string from the array is an invalid e-mail address
     */
    public final void addAllRecipients(final String[] recipientsArray) throws AddressException {

        if (recipientsArray == null || recipientsArray.length == 0) {
            throw new IllegalArgumentException("The recipients array cannot be empty.");
        }

        for (String recipientAddress : recipientsArray) {
            this.addRecipient(recipientAddress);
        }
    }



    /**
     * Defines one more address that this message must be sent to.
     *
     * @param address a valid e-mail address to sent this message to
     * @throws AddressException the e-mail address is incorrectly formatted
     */
    public final void addRecipient(final String address) throws AddressException {
        this.recipients.add(new InternetAddress(address));
    }



    /**
     * Adds a collection of addresses to the recipients of this message. Invalid e-mail addresses will be
     * silently ignored. If you want these to throw an exception, please use the
     * {@link #addAllRecipients(java.lang.String[])} method instead.
     *
     * @param recipientsArray an array that contains the addresses that this message must be sent to
     * @return <code>true</code> if at least one recipient could be defined
     */
    public final boolean addRecipients(final String[] recipientsArray) {
        assert recipientsArray != null && recipientsArray.length > 0 : "The recipients array cannot be empty.";

        for (String recipientAddress : recipientsArray) {

            try {
                this.addRecipient(recipientAddress);

            } catch (AddressException exception) {
                this.logger.error("Could not define address {} as a recipient.", exception);
            }
        }

        return (this.getRecipients().length > 0);
    }



    /**
     * Obtains the addresses that this message must be sent to.
     *
     * @return an array that contains the internet addresses of the recipients
     */
    public final InternetAddress[] getRecipients() {
        return this.recipients.toArray(new InternetAddress[]{});
    }



    /**
     * Obtains the title of this message.
     *
     * @return the message title
     */
    public final String getSubject() {
        return this.subject;
    }



    /**
     * Defines the title of this message.
     *
     * @param messageSubject the message title
     */
    public final void setSubject(final String messageSubject) {
        this.subject = messageSubject;
    }



    /**
     * Obtains the e-mail template processor.
     *
     * @return the template engine
     */
    private TemplateEngine getTemplateEngine() {
        return this.emailSettings.getTemplateEngine();
    }



    /**
     * Transmits the current message to its recipients.
     *
     * @return <code>true</code> if the message was successfully sent to the SMTP server (which does not necessarily
     *         means that it was then sent to the user)
     */
    public final boolean send() {
        assert emailSettings != null : "The e-mail settings must be set.";

        this.logger.debug("Getting the SMTP settings from the database.");
        this.emailSettings.refresh();

        if (!this.emailSettings.isNotificationEnabled()) {
            this.logger.info("The e-mail message has not been sent because the e-mail notifications are turned off.");
            return false;
        }

        Session smtpSession = this.getSmtpSession();
        MimeMessage message = this.createMessage(smtpSession);

        if (message == null) {
            this.logger.info("Could not send an e-mail because an error occured during the message creation.");
            return false;
        }

        try {

            if (this.emailSettings.useAuthentication()) {
                Transport.send(message, this.emailSettings.getSmtpUser(), this.emailSettings.getSmtpPassword());

            } else {
                Transport.send(message);
            }

            return true;

        } catch (MessagingException exception) {
            this.logger.error("Could not send the e-mail because an error occurred with the SMTP transport", exception);

            return false;
        }
    }



    /**
     * Builds a MIME message to send the current e-mail.
     *
     * @param session the current SMTP session
     * @return the MIME message, or <code>null</code> if an error occurred
     */
    private MimeMessage createMessage(final Session session) {
        this.logger.debug("Creating the e-mail message.");
        assert emailSettings != null : "The e-mail settings must be set.";

        if (!this.emailSettings.isValid()) {
            this.logger.error("Could not send the message. The SMTP configuration is not valid.");
            return null;
        }

        MimeMessage message = new MimeMessage(session);

        try {
            this.logger.debug("Defining the message sender.");
            message.setFrom(new InternetAddress(this.emailSettings.getSenderAddress(),
                    this.emailSettings.getSenderName()));
            this.logger.debug("Defining the message recipients.");
            message.setRecipients(RecipientType.TO, this.getRecipients());
            this.logger.debug("Defining the message subject : {}", this.getSubject());
            message.setSubject(this.getSubject(), Email.TEXT_ENCODING);

            switch (this.getContentType()) {

                case HTML:
                    this.logger.debug("Defining the message HTML content:\n{}", this.getContent());
                    message.setContent(this.getContent(), "text/html; charset=" + Email.TEXT_ENCODING);
                    break;

                case TEXT:
                    this.logger.debug("Defining the message text content:\n{}", this.getContent());
                    message.setText(this.getContent(), Email.TEXT_ENCODING);
                    break;

                default:
                    throw new UnsupportedOperationException(String.format("The content type %s is not supported.",
                            this.getContentType().name()));
            }

            this.logger.debug("Defining the message send date.");
            message.setSentDate(new Date());

        } catch (MessagingException | UnsupportedEncodingException exception) {
            this.logger.error("Could not create the e-mail message.", exception);
            return null;
        }

        this.logger.debug("The message is created.");
        return message;
    }



    /**
     * Create an SMTP session with the current e-mail settings.
     *
     * @return an SMTP session
     */
    private Session getSmtpSession() {
        this.logger.debug("Creating the SMTP session.");

        return Session.getInstance(this.emailSettings.toSystemProperties());
    }

}
