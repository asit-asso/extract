/*
 * Copyright (C) 2019 arx iT
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
import org.apache.commons.lang3.StringUtils;
import ch.asit_asso.extract.utils.DateTimeUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;



/**
 *
 * @author Yves Grasset
 */
public class TimeRangeValidator extends BaseValidator {

    @Override
    public final boolean supports(final Class<?> type) {
        return OrchestratorTimeRange.class.equals(type);
    }



    @Override
    public void validate(final Object target, final Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "startDayIndex",
                "parameters.errors.schedulerRange.startDayIndex.requiredy");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "endDayIndex",
                "parameters.errors.schedulerRange.endDayIndex.required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "startTime",
                "parameters.errors.schedulerRange.startTime.required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "endTime",
                "parameters.errors.schedulerRange.endTime.required");

        OrchestratorTimeRange range = (OrchestratorTimeRange) target;

        this.validateDayIndex(range.getStartDayIndex(), "startDayIndex",
                "parameters.errors.schedulerRange.startDayIndex.invalid", errors);
        this.validateDayIndex(range.getEndDayIndex(), "endDayIndex",
                "parameters.errors.schedulerRange.endDayIndex.invalid", errors);
        this.validateTimeString(range.getStartTime(), "startTime", "parameters.errors.schedulerRange.startTime.invalid",
                errors);
        this.validateTimeString(range.getEndTime(), "endTime", "parameters.errors.schedulerRange.endTime.invalid",
                errors);

        if (DateTimeUtils.compareTimeStrings(range.getStartTime(), range.getEndTime()) >= 0) {
            errors.rejectValue("endTime", "parameters.errors.schedulerRange.endTime.tooSmall");
        }
    }



    private void validateDayIndex(final int value, final String fieldName, final String errorMessageKey,
            final Errors errors) {
        assert !StringUtils.isBlank(fieldName) : "The field name to validate cannot be empty.";
        assert !StringUtils.isBlank(errorMessageKey) : "The key of the validation error message cannot be empty.";
        assert errors != null : "The object that holds the validation errors cannot be null.";

        if (value < OrchestratorTimeRange.MINIMUM_DAY_INDEX || value > OrchestratorTimeRange.MAXIMUM_DAY_INDEX) {
            errors.rejectValue(fieldName, errorMessageKey);
        }
    }



    private void validateTimeString(final String value, final String fieldName, final String errorMessageKey,
            final Errors errors) {
        assert !StringUtils.isBlank(fieldName) : "The field name to validate cannot be empty.";
        assert !StringUtils.isBlank(errorMessageKey) : "The key of the validation error message cannot be empty.";
        assert errors != null : "The object that holds the validation errors cannot be null.";

        if (!DateTimeUtils.isTimeStringValid(value, true)) {
            errors.rejectValue(fieldName, errorMessageKey);
        }
    }

}
