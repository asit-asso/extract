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
package ch.asit_asso.extract.web.controllers;

import javax.validation.Valid;
import ch.asit_asso.extract.domain.SystemParameter;
import ch.asit_asso.extract.orchestrator.Orchestrator;
import ch.asit_asso.extract.orchestrator.OrchestratorSettings;
import ch.asit_asso.extract.orchestrator.OrchestratorTimeRange;
import ch.asit_asso.extract.persistence.SystemParametersRepository;
import ch.asit_asso.extract.web.Message.MessageType;
import ch.asit_asso.extract.web.model.SystemParameterModel;
import ch.asit_asso.extract.web.validators.SystemParameterValidator;
import ch.asit_asso.extract.web.validators.TimeRangeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static ch.asit_asso.extract.persistence.SystemParametersRepository.BASE_PATH_KEY;
import static ch.asit_asso.extract.persistence.SystemParametersRepository.DASHBOARD_INTERVAL_KEY;
import static ch.asit_asso.extract.persistence.SystemParametersRepository.DISPLAY_TEMP_FOLDER;
import static ch.asit_asso.extract.persistence.SystemParametersRepository.ENABLE_LDAP_KEY;
import static ch.asit_asso.extract.persistence.SystemParametersRepository.ENABLE_MAIL_NOTIFICATIONS;
import static ch.asit_asso.extract.persistence.SystemParametersRepository.LDAP_ADMINS_GROUP_KEY;
import static ch.asit_asso.extract.persistence.SystemParametersRepository.LDAP_BASE_DN_KEY;
import static ch.asit_asso.extract.persistence.SystemParametersRepository.LDAP_ENABLE_SYNCHRO_KEY;
import static ch.asit_asso.extract.persistence.SystemParametersRepository.LDAP_ENCRYPTION_TYPE_KEY;
import static ch.asit_asso.extract.persistence.SystemParametersRepository.LDAP_OPERATORS_GROUP_KEY;
import static ch.asit_asso.extract.persistence.SystemParametersRepository.LDAP_PASSWORD_KEY;
import static ch.asit_asso.extract.persistence.SystemParametersRepository.LDAP_SERVER_NAMES_KEY;
import static ch.asit_asso.extract.persistence.SystemParametersRepository.LDAP_SYNCHRO_HOURS_FREQUENCY_KEY;
import static ch.asit_asso.extract.persistence.SystemParametersRepository.LDAP_USER_KEY;
import static ch.asit_asso.extract.persistence.SystemParametersRepository.SCHEDULER_FREQUENCY_KEY;
import static ch.asit_asso.extract.persistence.SystemParametersRepository.SCHEDULER_MODE;
import static ch.asit_asso.extract.persistence.SystemParametersRepository.SCHEDULER_RANGES;
import static ch.asit_asso.extract.persistence.SystemParametersRepository.SMTP_FROM_MAIL_KEY;
import static ch.asit_asso.extract.persistence.SystemParametersRepository.SMTP_FROM_NAME_KEY;
import static ch.asit_asso.extract.persistence.SystemParametersRepository.SMTP_PASSWORD_KEY;
import static ch.asit_asso.extract.persistence.SystemParametersRepository.SMTP_PORT_KEY;
import static ch.asit_asso.extract.persistence.SystemParametersRepository.SMTP_SERVER_KEY;
import static ch.asit_asso.extract.persistence.SystemParametersRepository.SMTP_SSL_KEY;
import static ch.asit_asso.extract.persistence.SystemParametersRepository.SMTP_USER_KEY;
import static ch.asit_asso.extract.persistence.SystemParametersRepository.STANDBY_REMINDER_DAYS;
import static ch.asit_asso.extract.persistence.SystemParametersRepository.VALIDATION_FOCUS_PROPERTIES_KEY;


/**
 * Web controller that processes requests related to the management of the application users.
 *
 * @author Florent Krin
 */
@Controller
@Scope("session")
@RequestMapping("/parameters")
public class SystemParametersController extends BaseController {

    /**
     * The string that identifies the part of the website that this controller manages.
     */
    private static final String CURRENT_SECTION_IDENTIFIER = "parameters";

    /**
     * The string that identifies the view to display the information about parameters.
     */
    private static final String VIEW_DETAILS = "parameters/details";

    /**
     * The string that tells this controller to redirect the user to the view that shows all the users.
     */
    private static final String REDIRECT_TO_VIEW = "redirect:/parameters";

    /**
     * The parameter value that indicates that the e-mail notifications are disabled.
     */
    private static final String OFF_STRING = "false";

    /**
     * The parameter value that indicates that the e-mail notifications are enabled.
     */
    private static final String ON_STRING = "true";

    //@Value("#{messageProperties['parameters.ldap.test.success']}")
    private String connectionsSuccessMessage = "OK";

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(SystemParametersController.class);

    /**
     * The Spring Data repository that links the user data objects to the data source.
     */
    private final SystemParametersRepository systemParametersRepository;

    public SystemParametersController(SystemParametersRepository repository) {
        this.systemParametersRepository = repository;
    }

    /**
     * Defines the links between form data and Java objects.
     *
     * @param binder the object that makes the link between web forms data and Java beans
     */
    @InitBinder("parameters")
    public final void initBinder(final WebDataBinder binder) {
        binder.setValidator(new SystemParameterValidator(new TimeRangeValidator()));
    }



    /**
     * Processes a request to add a period when the orchestrator shall run.
     *
     * @param parameterModel the model that contains the modifications of the application settings
     * @param model          The object that contains the data required to add the rule
     * @return The name of the next view
     */
    @PostMapping("addOrchestratorTimeRange")
    public final String addOrchestratorTimeRange(
            @ModelAttribute("parameters") final SystemParameterModel parameterModel, final ModelMap model) {
        this.logger.debug("Processing a request to add an orchestrator time range");

        if (!this.isCurrentUserAdmin()) {
            return REDIRECT_TO_ACCESS_DENIED;
        }

        parameterModel.addTimeRange(new OrchestratorTimeRange());

        return this.prepareModelForDetailsView(model, parameterModel);
    }



    /**
     * Processes a request to remove a period where the orchestrator shall run.
     *
     * @param parameterModel the model that contains the modifications of the application settings
     * @param model          the collection of model objects to communicate to the next view
     * @param rangeIndex     the number that indicates the position of the time range to remove
     * @return the identifier of the next view to display
     */
    @PostMapping("deleteOrchestratorTimeRange/{rangeIndex}")
    public final String deleteOrchestratorTimeRange(@ModelAttribute("parameters") final SystemParameterModel parameterModel,
            final ModelMap model, @PathVariable final int rangeIndex) {
        this.logger.debug("Processing a request to delete the orchestrator time range at index {}.", rangeIndex);

        if (!this.isCurrentUserAdmin()) {
            return REDIRECT_TO_ACCESS_DENIED;
        }

        parameterModel.removeTimeRange(rangeIndex);

        return this.prepareModelForDetailsView(model, parameterModel);
    }



    /**
     * Make the modifications to the application settings permanent.
     *
     * @param parameterModel     the model that contains the modifications of the application settings
     * @param bindingResult      the validation results for the modified settings
     * @param model              the data to display in the view
     * @param redirectAttributes the data to pass to the page that the user may be redirected to
     * @return the string that identifies the view to display
     */
    @PostMapping()
    public final String updateParameters(
            @Valid @ModelAttribute("parameters") final SystemParameterModel parameterModel,
            final BindingResult bindingResult, final ModelMap model, final RedirectAttributes redirectAttributes) {

        this.logger.debug("Processing the data to update parameters.");

        if (!this.isCurrentUserAdmin()) {
            return REDIRECT_TO_ACCESS_DENIED;
        }

        String[] keys = new String[]{
                BASE_PATH_KEY, DASHBOARD_INTERVAL_KEY, SCHEDULER_FREQUENCY_KEY, SCHEDULER_MODE, SCHEDULER_RANGES,
                SMTP_FROM_MAIL_KEY, SMTP_FROM_NAME_KEY, SMTP_PASSWORD_KEY, SMTP_PORT_KEY, SMTP_SERVER_KEY, SMTP_USER_KEY,
                SMTP_SSL_KEY, ENABLE_MAIL_NOTIFICATIONS, VALIDATION_FOCUS_PROPERTIES_KEY, STANDBY_REMINDER_DAYS,
                ENABLE_LDAP_KEY, LDAP_ADMINS_GROUP_KEY, LDAP_BASE_DN_KEY, LDAP_ENABLE_SYNCHRO_KEY,
                LDAP_ENCRYPTION_TYPE_KEY, LDAP_OPERATORS_GROUP_KEY, LDAP_PASSWORD_KEY, LDAP_SERVER_NAMES_KEY,
                LDAP_SYNCHRO_HOURS_FREQUENCY_KEY, LDAP_USER_KEY, DISPLAY_TEMP_FOLDER
        };

        if (bindingResult.hasErrors()) {
            this.logger.info("Updating the system parameters failed because of invalid data.");

            return this.prepareModelForDetailsView(model);
        }

        boolean success = true;
        String currentKey = keys[0];
        try {
            for (String key : keys) {

                currentKey = key;
                this.logger.debug("Fetching the parameter {} to update.", key);
                SystemParameter systemParameter = this.systemParametersRepository.findByKey(key);
                if (!this.systemParametersRepository.existsByKey(key)) {
                    systemParameter = parameterModel.createDomainObject(key);
                }

                switch (key) {

                    case BASE_PATH_KEY:
                        systemParameter.setValue(parameterModel.getBasePath());
                        break;

                    case DISPLAY_TEMP_FOLDER:
                        final String displayFolderValue = (parameterModel.isDisplayTempFolder())
                                ? SystemParametersController.ON_STRING
                                : SystemParametersController.OFF_STRING;
                        systemParameter.setValue(displayFolderValue);
                        break;


                    case DASHBOARD_INTERVAL_KEY:
                        systemParameter.setValue(parameterModel.getDashboardFrequency());
                        break;

                    case ENABLE_LDAP_KEY:
                        final String ldapEnabledValue = (parameterModel.isLdapEnabled())
                                ? SystemParametersController.ON_STRING
                                : SystemParametersController.OFF_STRING;

                        systemParameter.setValue(ldapEnabledValue);
                        break;

                    case LDAP_ADMINS_GROUP_KEY:
                        systemParameter.setValue(parameterModel.getLdapAdminsGroup());
                        break;

                    case LDAP_BASE_DN_KEY:
                        systemParameter.setValue(parameterModel.getLdapBaseDn());
                        break;

                    case LDAP_ENCRYPTION_TYPE_KEY:
                        systemParameter.setValue(parameterModel.getLdapEncryption().name());
                        break;

                    case LDAP_ENABLE_SYNCHRO_KEY:
                        final String ldapSynchroEnabledValue = (parameterModel.isLdapSynchronizationEnabled())
                                ? SystemParametersController.ON_STRING
                                : SystemParametersController.OFF_STRING;

                        systemParameter.setValue(ldapSynchroEnabledValue);
                        break;

                    case LDAP_OPERATORS_GROUP_KEY:
                        systemParameter.setValue(parameterModel.getLdapOperatorsGroup());
                        break;

                    case LDAP_PASSWORD_KEY:
                        systemParameter.setValue(parameterModel.getLdapSynchronizationPassword());
                        break;

                    case LDAP_SERVER_NAMES_KEY:
                        systemParameter.setValue(parameterModel.getLdapServers());
                        break;

                    case LDAP_SYNCHRO_HOURS_FREQUENCY_KEY:
                        systemParameter.setValue(parameterModel.getLdapSynchronizationFrequency());
                        break;

                    case LDAP_USER_KEY:
                        systemParameter.setValue(parameterModel.getLdapSynchronizationUser());
                        break;

                    case SCHEDULER_FREQUENCY_KEY:
                        systemParameter.setValue(parameterModel.getSchedulerFrequency());
                        break;

                    case SCHEDULER_MODE:
                        systemParameter.setValue(parameterModel.getSchedulerMode().name());
                        break;

                    case SCHEDULER_RANGES:
                        systemParameter.setValue(parameterModel.getSchedulerRangesAsJson());
                        break;

                    case SMTP_FROM_MAIL_KEY:
                        systemParameter.setValue(parameterModel.getSmtpFromMail());
                        break;

                    case SMTP_FROM_NAME_KEY:
                        systemParameter.setValue(parameterModel.getSmtpFromName());
                        break;

                    case SMTP_PASSWORD_KEY:

                        if (!parameterModel.isPasswordGenericString()) {
                            systemParameter.setValue(parameterModel.getSmtpPassword());
                        }
                        break;

                    case SMTP_PORT_KEY:
                        systemParameter.setValue(parameterModel.getSmtpPort());
                        break;

                    case SMTP_SERVER_KEY:
                        systemParameter.setValue(parameterModel.getSmtpServer());
                        break;

                    case SMTP_USER_KEY:
                        systemParameter.setValue(parameterModel.getSmtpUser());
                        break;

                    case SMTP_SSL_KEY:
                        systemParameter.setValue(parameterModel.getSslType().name());
                        break;

                    case STANDBY_REMINDER_DAYS:
                        systemParameter.setValue(parameterModel.getStandbyReminderDays());
                        break;

                    case ENABLE_MAIL_NOTIFICATIONS:
                        final String mailEnabledValue = (parameterModel.isMailEnabled())
                                ? SystemParametersController.ON_STRING
                                : SystemParametersController.OFF_STRING;
                        systemParameter.setValue(mailEnabledValue);
                        break;

                    case VALIDATION_FOCUS_PROPERTIES_KEY:
                        systemParameter.setValue(String.join(",", parameterModel.getValidationFocusProperties()));
                        break;

                    default:
                        throw new Exception(String.format("Unsupported application setting : %s", key));
                }

                this.systemParametersRepository.save(systemParameter);
            }

        } catch (Exception exception) {
            this.logger.error("Could not update parameter with key {}.", currentKey, exception);
            success = false;
        }

        if (!success) {
            this.addStatusMessage(model, "parameters.errors.update.failed", MessageType.ERROR);
            return SystemParametersController.REDIRECT_TO_VIEW;

        }

        this.refreshOrchestratorMode();

        this.logger.info("Updating the parameters has succeeded.");
        this.addStatusMessage(redirectAttributes, "parameters.updated", MessageType.SUCCESS);

        return SystemParametersController.REDIRECT_TO_VIEW;
    }



    /**
     * Processes a request to display all the application users.
     *
     * @param model the data to display in the next view
     * @return the string that identifies the next view to display
     */
    @GetMapping
    public final String viewPage(final ModelMap model) {

        if (!this.isCurrentUserAdmin()) {
            return REDIRECT_TO_ACCESS_DENIED;
        }

        SystemParameterModel systemParameterModel = new SystemParameterModel();
        systemParameterModel.setBasePath(systemParametersRepository.getBasePath());
        final String displayTempFolderValue = systemParametersRepository.isTempFolderDisplayed();
        systemParameterModel.setDisplayTempFolder(SystemParametersController.ON_STRING.equals(displayTempFolderValue));
        systemParameterModel.setDashboardFrequency(systemParametersRepository.getDashboardRefreshInterval());
        systemParameterModel.setLdapAdminsGroup(systemParametersRepository.getLdapAdminsGroup());
        systemParameterModel.setLdapBaseDn(systemParametersRepository.getLdapBaseDn());
        final String ldapEnabledValue = systemParametersRepository.isLdapEnabled();
        systemParameterModel.setLdapEnabled(SystemParametersController.ON_STRING.equals(ldapEnabledValue));
        systemParameterModel.setLdapEncryption(systemParametersRepository.getLdapEncryptionType());
        systemParameterModel.setLdapOperatorsGroup(systemParametersRepository.getLdapOperatorsGroup());
        systemParameterModel.setLdapServers(systemParametersRepository.getLdapServers());
        final String ldapSynchroEnabledValue = systemParametersRepository.isLdapSynchronizationEnabled();
        systemParameterModel.setLdapSynchronizationEnabled(
                SystemParametersController.ON_STRING.equals(ldapSynchroEnabledValue));
        systemParameterModel.setLdapSynchronizationFrequency(systemParametersRepository.getLdapSynchronizationFrequency());
        systemParameterModel.setLdapSynchronizationPassword(systemParametersRepository.getLdapSynchronizationPassword());
        systemParameterModel.setLdapSynchronizationUser(systemParametersRepository.getLdapSynchronizationUserName());
        systemParameterModel.setSchedulerFrequency(systemParametersRepository.getSchedulerFrequency());
        final OrchestratorSettings.SchedulerMode schedulerMode = OrchestratorSettings.SchedulerMode.valueOf(systemParametersRepository.getSchedulerMode());
        systemParameterModel.setSchedulerMode(schedulerMode);
        final String rangesString = systemParametersRepository.getSchedulerRanges();
        systemParameterModel.setSchedulerRanges(OrchestratorTimeRange.fromCollectionJson(rangesString));
        systemParameterModel.setSmtpFromMail(systemParametersRepository.getSmtpFromMail());
        systemParameterModel.setSmtpFromName(systemParametersRepository.getSmtpFromName());
        systemParameterModel.setSmtpPassword(SystemParameterModel.PASSWORD_GENERIC_STRING);
        systemParameterModel.setSmtpPort(systemParametersRepository.getSmtpPort());
        systemParameterModel.setSmtpServer(systemParametersRepository.getSmtpServer());
        systemParameterModel.setSmtpUser(systemParametersRepository.getSmtpUser());
        systemParameterModel.setSslType(systemParametersRepository.getSmtpSSL());
        systemParameterModel.setStandbyReminderDays(systemParametersRepository.getStandbyReminderDays());
        final String mailEnabledValue = systemParametersRepository.isEmailNotificationEnabled();
        systemParameterModel.setMailEnabled(SystemParametersController.ON_STRING.equals(mailEnabledValue));
        systemParameterModel.setValidationFocusProperties(systemParametersRepository.getValidationFocusProperties());

        return this.prepareModelForDetailsView(model, systemParameterModel);
    }


//    @PostMapping("testLdap")
//    @ResponseBody
//    public String testLdapConnection(@RequestParam("serverUrls") String serverUrlsString,
//                                     @RequestParam("bases") String basesString,
//                                     @RequestParam("encryption") String encryptionString,
//                                     @RequestParam("username") String username, @RequestParam("password") String password) {
//
//        LdapPool serversPool = new LdapPool(serverUrlsString.split(";"), basesString.split(";"),
//                                            EncryptionType.valueOf(encryptionString), username, password);
//        Optional<String> testResult = serversPool.testConnections();
//
//        if (testResult.isPresent()) {
//            return testResult.get();
//        }
//
//        return this.connectionsSuccessMessage;
//    }



    /**
     * Defines the generic model attributes that ensure a proper display of the system parameters details
     * view. <b>Important:</b> This method does not set the attribute for the parameters themselves. This must be set
     * separately, if necessary.
     *
     * @param model the data to display in the view
     * @return the string that identifies the details view
     */
    private String prepareModelForDetailsView(final ModelMap model) {
        return this.prepareModelForDetailsView(model, null);
    }



    /**
     * Defines the generic model attributes that ensure a proper display of the system parameters details
     * view.
     *
     * @param model          the data to display in the view
     * @param parameterModel the model that represents the system parameters to display in the details view, or
     *                       <code>null</code> not to define any parameters specifically (because it is set elsewhere,
     *                       for example)
     * @return the string that identifies the details view
     */
    private String prepareModelForDetailsView(final ModelMap model, final SystemParameterModel parameterModel) {
        assert model != null : "The model must be set.";

        this.addJavascriptMessagesAttribute(model);
        this.addCurrentSectionToModel(SystemParametersController.CURRENT_SECTION_IDENTIFIER, model);

        if (parameterModel != null) {
            model.addAttribute("parameters", parameterModel);
        }

        return SystemParametersController.VIEW_DETAILS;
    }



    /**
     * Informs the background tasks orchestrator that system parameters may have been updated.
     */
    private void refreshOrchestratorMode() {
        Orchestrator orchestrator = Orchestrator.getInstance();

        if (!orchestrator.isInitialized()) {
            this.logger.warn("The orchestrator is not initialized. The run mode will not be updated. Please check if"
                    + " there were errors when the application was started.");
            return;
        }

        orchestrator.updateSettingsFromDataSource(true);
    }

}
