package ch.asit_asso.extract.batch.processor;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import javax.validation.constraints.NotNull;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.email.EmailSettings;
import ch.asit_asso.extract.email.StandbyReminderEmail;
import ch.asit_asso.extract.persistence.ApplicationRepositories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;

public class StandbyRequestsReminderProcessor  implements ItemProcessor<Request, Request> {

    private final String applicationLanguage;

    private final int daysBeforeReminder;

    private final EmailSettings emailSettings;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(StandbyRequestsReminderProcessor.class);

    private final ApplicationRepositories repositories;


    public StandbyRequestsReminderProcessor(@NotNull final ApplicationRepositories applicationRepositories,
                                            @NotNull final EmailSettings smtpSettings,
                                            @NotNull final String applicationLanguage) {

        //this.logger.debug("Instantiating the standby request reminder processor.");
        this.applicationLanguage = applicationLanguage;
        this.emailSettings = smtpSettings;
        this.repositories = applicationRepositories;
        this.daysBeforeReminder = Integer.valueOf(this.repositories.getParametersRepository().getStandbyReminderDays());
        //this.logger.debug("Standby request reminder processor instantiated.");
    }

    public final Request process(@NonNull Request request) {
        //this.logger.debug("Processing request {}. Days before reminder is set to {}", request.getId(), this.daysBeforeReminder);

        if (this.daysBeforeReminder == 0) {
            //this.logger.debug("Request standby notifications is disabled. Ignoring.");
            return request;
        }

        //this.logger.debug("Checking if last reminder is before the limit.");
        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        final Calendar now = GregorianCalendar.getInstance();
        //this.logger.debug("Current date is {}", dateFormat.format(now.getTime()));
        final Calendar limit = GregorianCalendar.getInstance();
        limit.add(Calendar.DAY_OF_MONTH, this.daysBeforeReminder * -1);
        //this.logger.debug("Notification will be sent if the last reminder is before {}", dateFormat.format(limit.getTime()));
        //this.logger.debug("Request {} last reminder is from {}", request.getId(), dateFormat.format(request.getLastReminder().getTime()));

        if (request.getLastReminder() == null || limit.after(request.getLastReminder())) {
            final boolean notificationSuccess = this.sendEmailNotification(request);

            if (notificationSuccess) {
                this.logger.info("A notification message for standby request {} has been set.", request.getId());
                request.setLastReminder(now);
                this.logger.debug("Last reminder set to {}", dateFormat.format(request.getLastReminder().getTime()));

            } else {
                this.logger.info("Notification for standby request {} should have been sent but failed.", request.getId());
            }
        }

        return request;
    }




    /**
     * Notifies the administrator by e-mail that the export failed.
     *
     * @param request   the request that could not be exported
     */
    private boolean sendEmailNotification(final Request request) {
        assert request != null : "The request cannot be null.";
        assert request.getConnector() != null : "The request connector cannot be null.";

        try {
            this.logger.debug("Sending an e-mail reminder to the operators.");
            final StandbyReminderEmail message = new StandbyReminderEmail(this.emailSettings);
            final String[] operatorsAddresses
                    = this.repositories.getProcessesRepository().getProcessOperatorsAddresses(request.getProcess().getId());
            final Set<String> recipientsAddresses
                    = new HashSet<>(Arrays.asList(operatorsAddresses));

            for (String adminAddress : this.repositories.getUsersRepository().getActiveAdministratorsAddresses()) {

                if (adminAddress == null || recipientsAddresses.contains(adminAddress)) {
                    continue;
                }

                recipientsAddresses.add(adminAddress);
            }

            if (!message.initialize(request, recipientsAddresses.toArray(new String[]{}))) {
                this.logger.error("Could not create the request export failure message.");
                return false;
            }

            final boolean messageSent = message.send();

            if (!messageSent) {
                this.logger.warn("The request export failure notification was not sent.");
                return false;
            }

            this.logger.info("The request export failure notification was successfully sent to the operators.");
            return true;

        } catch (Exception exception) {
            this.logger.warn("An error prevented notifying the operators by e-mail.", exception);
            return false;
        }
    }

}
