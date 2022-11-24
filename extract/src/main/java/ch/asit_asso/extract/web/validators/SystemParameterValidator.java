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
package ch.asit_asso.extract.web.validators;

import ch.asit_asso.extract.orchestrator.OrchestratorTimeRange;
import ch.asit_asso.extract.utils.EmailUtils;
import ch.asit_asso.extract.web.model.SystemParameterModel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;



/**
 * An object that ensure that a model representing a user contains valid information.
 *
 * @author Yves Grasset
 */
public class SystemParameterValidator extends BaseValidator {

    /**
     * The smallest number that is acceptable as an HTTP port.
     */
    private static final int MAXIMUM_HTTP_PORT = 65536;

    /**
     * The largest number of seconds to wait before refreshing the dashboard data.
     */
    private static final int MAXIMUM_DASHBOARD_FREQUENCY = 2147483;

    /**
     * The smallest number of seconds to wait before refreshing the dashboard data.
     */
    private static final int MINIMUM_DASHBOARD_FREQUENCY = 10;

    /**
     * The largest number of seconds to wait between two executions of a background task.
     */
    private static final int MAXIMUM_SCHEDULER_FREQUENCY = Integer.MAX_VALUE;

    /**
     * The smallest number of seconds to wait between two executions of a background task.
     */
    private static final int MINIMUM_SCHEDULER_FREQUENCY = 1;

    /**
     * The smallest number that is acceptable as an HTTP port.
     */
    private static final int MINIMUM_HTTP_PORT = 0;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(UserValidator.class);

    private final TimeRangeValidator timeRangeValidator;



    /**
     * Creates a new instance of this validator.
     *
     * @param rangeValidator the object that checks if a time range is valid
     */
    public SystemParameterValidator(final TimeRangeValidator rangeValidator) {
        this.checkValidator(rangeValidator, OrchestratorTimeRange.class);
        this.timeRangeValidator = rangeValidator;
    }



    /**
     * Determines if objects of a given type can be checked with this validator.
     *
     * @param type the class of the objects to validate
     * @return <code>true</code> if the type is supported by this validator
     */
    @Override
    public final boolean supports(final Class<?> type) {
        return SystemParameterModel.class.equals(type);
    }



    /**
     * Checks the conformity of the user model information.
     *
     * @param target the object to validate
     * @param errors an object that assembles the validation errors for the object
     */
    @Override
    @Transactional(readOnly = true)
    public void validate(final Object target, final Errors errors) {
        this.logger.debug("Validating the user model {}.", target);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "basePath", "parameters.errors.basepath.required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "smtpFromMail", "parameters.errors.smtpfrommail.required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "smtpFromName", "parameters.errors.smtpfromname.required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "smtpServer", "parameters.errors.smtpserver.required");

        final SystemParameterModel systemParameterModel = (SystemParameterModel) target;
        this.validateDashboardFrequency(systemParameterModel.getDashboardFrequency(), errors);
        this.validateSchedulerFrequency(systemParameterModel.getSchedulerFrequency(), errors);
        this.validateSmtpPort(systemParameterModel.getSmtpPort(), errors);

        if (!this.validateEmail(systemParameterModel.getSmtpFromMail())) {
            errors.rejectValue("smtpFromMail", "parameters.errors.smtpfrommail.invalid");
        }

        if (systemParameterModel.getSslType() == null) {
            errors.rejectValue("sslType", "parameters.errors.ssltype.required");
        }

        int orchestratorTimeRangeIndex = 0;

        for (OrchestratorTimeRange timeRange : systemParameterModel.getSchedulerRanges()) {

            try {
                errors.pushNestedPath(String.format("schedulerRanges[%d]", orchestratorTimeRangeIndex));
                ValidationUtils.invokeValidator(this.timeRangeValidator, timeRange, errors);
                orchestratorTimeRangeIndex++;

            } finally {
                errors.popNestedPath();
            }
        }
    }



    /**
     * Checks that the value entered as the dashboard refresh frequency is valid.
     *
     * @param valueString the string entered as the frequency
     * @param errors      the object that assembles the validation errors
     * @return <code>true</code> if the dashboard refresh frequency is valid
     */
    private boolean validateDashboardFrequency(final String valueString, final Errors errors) {

        if (!this.validateInteger(valueString)) {
            errors.rejectValue("dashboardFrequency", "parameters.errors.dashboardfrequency.invalid");
            return false;
        }

        final int frequency = Integer.valueOf(valueString);

        if (frequency < SystemParameterValidator.MINIMUM_DASHBOARD_FREQUENCY) {
            errors.rejectValue("dashboardFrequency", "parameters.errors.dashboardfrequency.tooSmall",
                    new Object[]{SystemParameterValidator.MINIMUM_DASHBOARD_FREQUENCY},
                    "parameters.errors.dashboardfrequency.invalid");
            return false;
        }

        if (frequency > SystemParameterValidator.MAXIMUM_DASHBOARD_FREQUENCY) {
            errors.rejectValue("dashboardFrequency", "parameters.errors.dashboardfrequency.tooLarge",
                    new Object[]{SystemParameterValidator.MAXIMUM_DASHBOARD_FREQUENCY},
                    "parameters.errors.dashboardfrequency.invalid");
            return false;
        }

        return true;
    }



    /**
     * Checks that the value entered as the orchestrator task frequency is valid.
     *
     * @param valueString the string entered as the frequency
     * @param errors      the object that assembles the validation errors
     * @return <code>true</code> if the scheduler frequency is valid
     */
    private boolean validateSchedulerFrequency(final String valueString, final Errors errors) {

        if (!this.validateInteger(valueString)) {
            errors.rejectValue("schedulerFrequency", "parameters.errors.schedulerfrequency.outOfRange",
                    new Object[]{SystemParameterValidator.MINIMUM_SCHEDULER_FREQUENCY,
                        SystemParameterValidator.MAXIMUM_SCHEDULER_FREQUENCY},
                    "parameters.errors.schedulerfrequency.invalid");
            return false;
        }

        final int frequency = Integer.valueOf(valueString);

        if (frequency < SystemParameterValidator.MINIMUM_SCHEDULER_FREQUENCY) {
            errors.rejectValue("schedulerFrequency", "parameters.errors.schedulerfrequency.notpositive");
            return false;
        }

        if (frequency > SystemParameterValidator.MAXIMUM_SCHEDULER_FREQUENCY) {
            errors.rejectValue("schedulerFrequency", "parameters.errors.schedulerfrequency.tooLarge",
                    new Object[]{SystemParameterValidator.MAXIMUM_SCHEDULER_FREQUENCY},
                    "parameters.errors.schedulerfrequency.invalid");
            return false;
        }

        return true;
    }



    /**
     * Checks that the value entered as the SMTP port is a valid HTTP port.
     *
     * @param valueString the string entered as the SMTP port
     * @param errors      the object that assembles the validation errors
     * @return <code>true</code> if the SMTP port is valid
     */
    private boolean validateSmtpPort(final String valueString, final Errors errors) {

        if (!this.validateInteger(valueString)) {
            errors.rejectValue("smtpPort", "parameters.errors.smtpport.invalid");
            return false;
        }

        final int port = Integer.valueOf(valueString);

        if (port < SystemParameterValidator.MINIMUM_HTTP_PORT || port > SystemParameterValidator.MAXIMUM_HTTP_PORT) {
            errors.rejectValue("smtpPort", "parameters.errors.smtpport.outofrange", new Object[]{
                SystemParameterValidator.MINIMUM_HTTP_PORT, SystemParameterValidator.MAXIMUM_HTTP_PORT
            }, "parameters.errors.smtpport.invalid");
            return false;
        }

        return true;
    }



    /**
     * Ensures that an e-mail address is correctly formed.
     * <p>
     * <b>Note:</b> Does not check if the address is a real one.
     *
     * @param email the string containing the e-mail address
     * @return the key of the validation error message, or <code>null</code> if the address is correctly formatted
     */
    private boolean validateEmail(final String email) {

        if (StringUtils.isBlank(email)) {
            return false;
        }

        return EmailUtils.isAddressValid(email);
    }



    /**
     * Ensures that an value is an integer.
     *
     * @param valueString the string containing the value to check
     * @return the key of the validation error message, or <code>null</code> if the value is a correct integer
     */
    private boolean validateInteger(final String valueString) {

        try {
            Integer.parseInt(valueString);

        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

}
