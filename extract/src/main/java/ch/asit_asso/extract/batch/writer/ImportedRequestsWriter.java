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
package ch.asit_asso.extract.batch.writer;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.email.LocaleUtils;
import ch.asit_asso.extract.persistence.ApplicationRepositories;
import org.apache.commons.lang3.StringUtils;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.RequestHistoryRecord;
import ch.asit_asso.extract.email.EmailSettings;
import ch.asit_asso.extract.email.InvalidProductImportedEmail;
import ch.asit_asso.extract.persistence.RequestHistoryRepository;
import ch.asit_asso.extract.services.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Scope;



/**
 * An object that saves a set of requests that have just been created and updates the related information
 * of the connector instance that they have been imported through.
 *
 * @author Yves Grasset
 */
@Scope("step")
public class ImportedRequestsWriter implements ItemWriter<Request> {

    /**
     * The index of the import step in the request process history.
     */
    private static final int IMPORT_PROCESS_STEP = 0;

    /**
     * The index of the import step in the request full history.
     */
    private static final int IMPORT_HISTORY_STEP = 1;

    /**
     * The connector object that has imported the requests to save.
     */
    private int connectorId;

    /**
     * The objects required to create and send an e-mail message.
     */
    private final EmailSettings emailSettings;

    /**
     * The service for obtaining localized messages.
     */
    private final MessageService messageService;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(ImportedRequestsWriter.class);

    /**
     * The link between the various data objects and their data source.
     */
    private ApplicationRepositories repositories;



    /**
     * Creates a new instance of this writer.
     *
     * @param connectorIdentifier     the number that identifies the instance containing the connector parameters used
     *                                to import the requests
     * @param smtpSettings            the objects required to create and send an e-mail message
     * @param applicationRepositories the he link between the various data objects and their data source
     * @param messageService          the service for obtaining localized messages
     */
    public ImportedRequestsWriter(final int connectorIdentifier, final EmailSettings smtpSettings,
            final ApplicationRepositories applicationRepositories, final MessageService messageService) {

        if (connectorIdentifier < 1) {
            throw new IllegalArgumentException("The connector identifier must be greater than 0.");
        }

        if (smtpSettings == null) {
            throw new IllegalArgumentException("The SMTP settings object cannot be null.");
        }

        if (applicationRepositories == null) {
            throw new IllegalArgumentException("The application repositories object cannot be null.");
        }

        if (messageService == null) {
            throw new IllegalArgumentException("The message service cannot be null.");
        }

        this.connectorId = connectorIdentifier;
        this.emailSettings = smtpSettings;
        this.repositories = applicationRepositories;
        this.messageService = messageService;
    }



    /**
     * Saves the imported requests to the data source.
     *
     * @param requestsList a list that contains the imported requests to save
     */
    @Override
    public final void write(final List<? extends Request> requestsList) {

        try {

            if (requestsList == null) {
                throw new IllegalStateException("The requests list cannot be null.");
            }

            for (Request request : requestsList) {

                if (request.getConnector().getId() != this.connectorId) {
                    this.logger.warn("A request in the collection to persist is not related to the current connector"
                            + " and has been ignored.");
                    continue;
                }

                Request savedRequest = this.repositories.getRequestsRepository().save(request);

                if (savedRequest != null) {
                    this.createHistoryRecord(savedRequest);

                    if (savedRequest.getStatus() == Request.Status.IMPORTFAIL) {
                        this.sendEmailNotification(savedRequest,
                                this.messageService.getMessage(this.getErrorMessageKey(request)), request.getStartDate());
                    }
                }
            }

        } catch (Exception exception) {
            this.logger.error("Could not save the imported requests.", exception);
            throw exception;
        }
    }



    /**
     * Instantiates a new entry in the history of the given request.
     *
     * @param request the request that is currently exported
     * @return the created request history record, or <code>null</code> if the operation failed
     */
    private RequestHistoryRecord createHistoryRecord(final Request request) {
        assert request != null : "The request cannot be null";

        RequestHistoryRepository repository = this.repositories.getRequestHistoryRepository();

        RequestHistoryRecord exportRecord = new RequestHistoryRecord();
        exportRecord.setRequest(request);
        exportRecord.setStartDate(request.getStartDate());
        exportRecord.setStep(ImportedRequestsWriter.IMPORT_HISTORY_STEP);
        exportRecord.setProcessStep(ImportedRequestsWriter.IMPORT_PROCESS_STEP);
        exportRecord.setTaskLabel(this.messageService.getMessage("requestHistory.tasks.import.label"));
        exportRecord.setUser(this.repositories.getUsersRepository().getSystemUser());

        String messageKey;
        RequestHistoryRecord.Status status;

        if (request.getStatus() == Request.Status.IMPORTED) {
            status = RequestHistoryRecord.Status.FINISHED;
            messageKey = "importTask.message.ok";

        } else {
            status = RequestHistoryRecord.Status.ERROR;
            messageKey = this.getErrorMessageKey(request);
        }

        exportRecord.setStatus(status);
        exportRecord.setMessage(this.messageService.getMessage(messageKey));
        exportRecord.setEndDate(new GregorianCalendar());

        return repository.save(exportRecord);
    }



    /**
     * Obtains the string identifying the message that describes why the request import failed.
     *
     * @param request the request whose import failed
     * @return the error message key
     */
    private String getErrorMessageKey(final Request request) {
        assert request.getStatus() == Request.Status.IMPORTFAIL : "The request must be in a failed import state.";

        if (request.getPerimeter() == null) {
            return "importTask.message.error.noGeometry";
        }

        return "importTask.message.error.generic";
    }






    /**
     * Notifies the administrator by e-mail that the export failed.
     *
     * @param request       the request that could not be exported
     * @param resultMessage the string that explain why the export failed
     * @param errorDate     when the export failure occurred
     */
    private void sendEmailNotification(final Request request, final String resultMessage, final Calendar errorDate) {
        assert request != null : "The request cannot be null.";
        assert request.getConnector() != null : "The request connector cannot be null.";
        assert resultMessage != null : "The result error message cannot be null.";

        try {
            this.logger.debug("Sending e-mail notifications to administrators.");

            // Retrieve administrators as User objects
            final User[] administrators = this.repositories.getUsersRepository()
                .findByProfileAndActiveTrue(User.Profile.ADMIN);

            if (administrators == null || administrators.length == 0) {
                this.logger.warn("No administrators found for invalid product import notification.");
                return;
            }

            // Get available locales from email settings (configured from extract.i18n.language)
            final List<java.util.Locale> availableLocales = this.emailSettings.getAvailableLocales();
            boolean atLeastOneEmailSent = false;

            // Send individual email to each administrator with their preferred locale
            for (User administrator : administrators) {
                try {
                    final InvalidProductImportedEmail message = new InvalidProductImportedEmail(this.emailSettings);

                    // Get validated locale for this administrator
                    java.util.Locale userLocale = LocaleUtils.getValidatedUserLocale(administrator, availableLocales);

                    if (!message.initializeContent(request, resultMessage, errorDate, userLocale)) {
                        this.logger.error("Could not create the message for user {}.", administrator.getLogin());
                        continue;
                    }

                    try {
                        message.addRecipient(administrator.getEmail());
                    } catch (javax.mail.internet.AddressException e) {
                        this.logger.error("Invalid email address for user {}: {}",
                            administrator.getLogin(), administrator.getEmail());
                        continue;
                    }

                    if (message.send()) {
                        this.logger.debug("Invalid product import notification sent successfully to {} with locale {}.",
                                        administrator.getEmail(), userLocale.toLanguageTag());
                        atLeastOneEmailSent = true;
                    } else {
                        this.logger.warn("Failed to send invalid product import notification to {}.",
                            administrator.getEmail());
                    }

                } catch (Exception exception) {
                    this.logger.warn("Error sending notification to user {}: {}",
                        administrator.getLogin(), exception.getMessage());
                }
            }

            if (atLeastOneEmailSent) {
                this.logger.info("The invalid imported product notification was sent to at least one administrator.");
            } else {
                this.logger.warn("The invalid imported product notification was not sent to any administrator.");
            }

        } catch (Exception exception) {
            this.logger.warn("An error prevented notifying the administrators by e-mail.", exception);
        }
    }

}
