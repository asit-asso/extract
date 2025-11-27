/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.asit_asso.extract.plugins.email;

import java.io.UnsupportedEncodingException;
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
import org.apache.commons.lang3.StringUtils;
import ch.asit_asso.extract.plugins.common.IEmailSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * An electronic message sent by the application.
 *
 * @author Yves Grasset
 */
public class Email {

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
    private final IEmailSettings emailSettings;

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
    public Email(final IEmailSettings settings) {

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
     * Transmits the current message to its recipients.
     *
     * @return <code>true</code> if the message was successfully sent to the SMTP server (which does not necessarily
     *         means that it was then sent to the user)
     */
    public final boolean send() {
        assert emailSettings != null : "The e-mail settings must be set.";

        this.logger.debug("Starting email send process...");
        //this.emailSettings.refresh();

        if (!this.emailSettings.isNotificationEnabled()) {
            this.logger.warn("The e-mail message has not been sent because the e-mail notifications are turned off.");
            return false;
        }

        Session smtpSession = this.getSmtpSession();
        if (smtpSession == null) {
            this.logger.error("Failed to create SMTP session");
            return false;
        }
        
        MimeMessage message = this.createMessage(smtpSession);

        if (message == null) {
            this.logger.error("Could not send an e-mail because an error occured during the message creation.");
            return false;
        }

        try {
            this.logger.debug("Attempting to send email via SMTP...");

            if (this.emailSettings.useAuthentication()) {
                this.logger.debug("Using SMTP authentication with user: {}", this.emailSettings.getSmtpUser());
                Transport.send(message, this.emailSettings.getSmtpUser(), this.emailSettings.getSmtpPassword());

            } else {
                this.logger.debug("Sending without SMTP authentication");
                Transport.send(message);
            }

            this.logger.info("Email sent successfully via SMTP");
            return true;

        } catch (MessagingException exception) {
            this.logger.error("Could not send the e-mail because an error occurred with the SMTP transport. Error: {}", 
                    exception.getMessage(), exception);
            return false;
            
        } catch (Exception exception) {
            this.logger.error("Unexpected error occurred while sending email: {}", 
                    exception.getMessage(), exception);
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
        
        try {
            java.util.Properties props = this.emailSettings.toSystemProperties();
            this.logger.debug("SMTP properties: {}", props.toString());
            
            Session session = Session.getInstance(props);
            this.logger.debug("SMTP session created successfully");
            return session;
        } catch (Exception e) {
            this.logger.error("Failed to create SMTP session: {}", e.getMessage(), e);
            return null;
        }
    }

}
