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
package ch.asit_asso.extract.batch.processor;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.email.LocaleUtils;
import ch.asit_asso.extract.email.UnmatchedRequestEmail;
import ch.asit_asso.extract.persistence.RulesRepository;
import ch.asit_asso.extract.persistence.UsersRepository;
import ch.asit_asso.extract.requestmatching.RequestMatcher;
import ch.asit_asso.extract.domain.Connector;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.Request.Status;
import ch.asit_asso.extract.domain.Rule;
import ch.asit_asso.extract.email.EmailSettings;
import ch.asit_asso.extract.persistence.SystemParametersRepository;
import ch.asit_asso.extract.utils.FileSystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;



/**
 * An object that will attempt to match an imported request with a process through the rules defined for
 * its connector.
 *
 * @author Yves Grasset
 */
public class RequestMatchingProcessor implements ItemProcessor<Request, Request> {

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(RequestMatchingProcessor.class);

    /**
     * The object that assembles the configuration objects required to send an e-mail message.
     */
    private final EmailSettings emailSettings;

    /**
     * The link between the rules data objects and the data source.
     */
    private final RulesRepository rulesRepository;

    /**
     * The link to fetch the application general parameters in the data source.
     */
    private final SystemParametersRepository systemParametersRepository;

    /**
     * The link between the users data objects and the data source.
     */
    private final UsersRepository usersRepository;



    /**
     * Creates a new instance of the processor.
     *
     * @param rulesRepo      the link between the rules data objects and the data source
     * @param parametersRepo the access to the application general parameters
     * @param usersRepo      the link between the users data obejcts and the data source
     * @param smtpSettings   the object that asssembles the configuration objects required to create and send
     *                       e-mail messages
     */
    public RequestMatchingProcessor(final RulesRepository rulesRepo, final SystemParametersRepository parametersRepo,
            final UsersRepository usersRepo, final EmailSettings smtpSettings) {

        if (rulesRepo == null) {
            throw new IllegalArgumentException("The rules repository cannot be null.");
        }

        if (parametersRepo == null) {
            throw new IllegalArgumentException("The system parameters repository cannot be null.");
        }

        if (usersRepo == null) {
            throw new IllegalArgumentException("The users repository cannot be null.");
        }

        if (smtpSettings == null) {
            throw new IllegalArgumentException("The e-mail settings object cannot be null.");
        }

        this.rulesRepository = rulesRepo;
        this.systemParametersRepository = parametersRepo;
        this.usersRepository = usersRepo;
        this.emailSettings = smtpSettings;
    }



    /**
     * Matches a request with a process.
     *
     * @param request the request to link to a process
     * @return the updated request
     */
    @Override
    public final Request process(final Request request) {
        this.logger.debug("Attempting to match request {} with a rule.", request.getId());
        final Rule matchingRule = this.matchRequestWithRules(request);

        if (matchingRule == null) {
            this.logger.warn("Request {} did not match any rule from connector {}.", request.getId(),
                    request.getConnector().getName());
            this.sendEmailToAdmins(request);
            return this.setRequestToUnmatched(request);
        }

        assert matchingRule.getProcess() != null : "The process defined for the matching must be set.";

        return this.setRequestToMatched(request, matchingRule.getProcess());
    }



    /**
     * Checks the rules defined for the connector that imported the request to see if one matches.
     *
     * @param request the request to match against the connector rules
     * @return the rule that matches the request, or <code>null</code> if none matched
     */
    private Rule matchRequestWithRules(final Request request) {
        assert request != null : "The request must not be null.";

        Connector requestConnector = request.getConnector();

        if (requestConnector == null) {
            throw new IllegalStateException("The request must be linked to the connector that created it.");
        }

        this.logger.debug("Attempting to match with rules of connector {}.", requestConnector.getName());

        RequestMatcher requestMatchingRuleWrapper = new RequestMatcher(request);
        List<Rule> rules = this.rulesRepository.findByConnectorAndActiveTrueOrderByPosition(requestConnector);
        this.logger.info("Check matching for Request {}", request.getId());
        Rule matchRule = requestMatchingRuleWrapper.matchRequestWithRules(rules);

        return matchRule;
    }



    /**
     * Carries the actions to prepare a request for the process that matches it.
     *
     * @param request        the request to prepare
     * @param matchedProcess the process that matched the request
     * @return the updated request
     */
    private Request setRequestToMatched(final Request request,
            final Process matchedProcess) {
        assert request != null : "The request must not be null.";
        assert matchedProcess != null : "The process must not be null.";

        if (!this.defineDataFolders(request)) {
            this.logger.error("The definition of the data folders for request {} failed.", request.getId());
            return request;
        }

        this.logger.debug("Updating request fields with process info.");
        request.setProcess(matchedProcess);
        request.setStatus(Status.ONGOING);
        request.setTasknum(1);

        return request;
    }



    /**
     * Creates the required data folder to process the request and updates the corresponding request fields.
     * <p>
     * <b>Note:</b> The passed {@link Request} object will only be updated if <i>all</i> the required folders have
     * been created.
     *
     * @param request the request
     * @return <code>true</code> if all the necessary folders have been created.
     */
    private boolean defineDataFolders(final Request request) {

        this.logger.debug("Creating the folders for request {}.", request.getId());
        final File baseFolder = FileSystemUtils.createFolder(new File(this.systemParametersRepository.getBasePath()),
                false);

        if (baseFolder == null) {
            this.logger.error("The base folder for requests data {} does not exist or is not accessible and could not"
                    + " be created.", request.getId());
            return false;
        }

        final File requestDataRootFolder = this.createDataRootFolder(baseFolder);

        if (requestDataRootFolder == null) {
            this.logger.error("The data folders for request {} could not be created.", request.getId());
            return false;
        }

        final File inputFolder = this.createInputFolder(requestDataRootFolder);

        if (inputFolder == null) {
            this.logger.error("The input data folder for request {} could not be created.", request.getId());
            return false;
        }

        final File outputFolder = this.createOutputFolder(requestDataRootFolder);

        if (outputFolder == null) {
            this.logger.error("The output data folder for request {} could not be created.", request.getId());
            return false;
        }

        final Path baseFolderPath = Paths.get(baseFolder.getAbsolutePath());
        request.setFolderIn(baseFolderPath.relativize(inputFolder.toPath()).toString());
        request.setFolderOut(baseFolderPath.relativize(outputFolder.toPath()).toString());

        return true;
    }



    /**
     * Notifies the active administrators by electronic message that a request did not match any rule.
     *
     * @param request the request that did not match
     */
    private void sendEmailToAdmins(final Request request) {
        assert request != null : "The request must be set.";

        try {
            this.logger.debug("Sending e-mail notifications to administrators.");

            // Retrieve administrators as User objects
            final User[] administrators = this.usersRepository.findByProfileAndActiveTrue(User.Profile.ADMIN);

            if (administrators == null || administrators.length == 0) {
                this.logger.warn("No administrators found for unmatched request notification.");
                return;
            }

            // Get available locales from email settings (configured from extract.i18n.language)
            final List<java.util.Locale> availableLocales = this.emailSettings.getAvailableLocales();
            boolean atLeastOneEmailSent = false;

            // Send individual email to each administrator with their preferred locale
            for (User administrator : administrators) {
                try {
                    final UnmatchedRequestEmail message = new UnmatchedRequestEmail(this.emailSettings);

                    // Get validated locale for this administrator
                    java.util.Locale userLocale = LocaleUtils.getValidatedUserLocale(administrator, availableLocales);

                    if (!message.initializeContent(request, userLocale)) {
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
                        this.logger.debug("Unmatched request notification sent successfully to {} with locale {}.",
                                        administrator.getEmail(), userLocale.toLanguageTag());
                        atLeastOneEmailSent = true;
                    } else {
                        this.logger.warn("Failed to send unmatched request notification to {}.",
                            administrator.getEmail());
                    }

                } catch (Exception exception) {
                    this.logger.warn("Error sending notification to user {}: {}",
                        administrator.getLogin(), exception.getMessage());
                }
            }

            if (atLeastOneEmailSent) {
                this.logger.info("The unmatched request e-mail notification was sent to at least one administrator.");
            } else {
                this.logger.warn("The unmatched request e-mail notification was not sent to any administrator.");
            }

        } catch (Exception exception) {
            this.logger.warn("An error prevented notifying the administrators by e-mail.", exception);
        }
    }
//
//
//
//    /**
//     * Obtains the e-mail addresses of all the currently active application administrators.
//     *
//     * @return an array containing the addresses
//     */
//    private String[] getActiveAdministratorsAddresses() {
//        List<String> addresses = new ArrayList<>();
//
//        for (User admin : this.usersRepository.findByProfileAndActiveTrue(Profile.ADMIN)) {
//            addresses.add(admin.getEmail());
//        }
//
//        return addresses.toArray(new String[]{});
//    }



    /**
     * Carries the actions to signal that a request has not matched any process.
     *
     * @param request the request that did not match any process
     * @return the updated request
     */
    private Request setRequestToUnmatched(final Request request) {
        assert request != null : "The request must not be null.";

        this.logger.debug("Setting request status to unmatched,");
        request.setStatus(Status.UNMATCHED);

        return request;
    }



    /**
     * Creates the folder that will contain the data generated and consumed by the process.
     *
     * @param baseFolder the folder that contains the data for all requests
     * @return the request data folder, or <code>null</code> if it could not be created
     */
    private File createDataRootFolder(final File baseFolder) {
        File rootFolder;

        try {

            do {
                rootFolder = new File(baseFolder, UUID.randomUUID().toString());

            } while (rootFolder.exists());

            return FileSystemUtils.createFolder(rootFolder, true);

        } catch (SecurityException exception) {
            this.logger.error("A security error occurred while creating a data folder name.", exception);
            return null;
        }
    }



    /**
     * Creates the folder that will contain the data consumed by the request process.
     *
     * @param rootFolder the request data folder
     * @return the input data folder, or <code>null</code> if it could not be created
     */
    private File createInputFolder(final File rootFolder) {

        return FileSystemUtils.createFolder(new File(rootFolder, "input"));

    }



    /**
     * Creates the folder that will contain the data generated by the request process.
     *
     * @param rootFolder the request data folder
     * @return the output data folder, or <code>null</code> if it could not be created
     */
    private File createOutputFolder(final File rootFolder) {

        return FileSystemUtils.createFolder(new File(rootFolder, "output"));

    }

}
