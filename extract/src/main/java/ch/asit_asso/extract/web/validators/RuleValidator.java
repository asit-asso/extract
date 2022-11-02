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

import ch.asit_asso.extract.web.model.RuleModel;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;



/**
 * An object that ensures that a model representing a rule contains valid data.
 *
 * @author Yves Grasset
 */
public class RuleValidator extends BaseValidator {

    /**
     * Determines if objects of a given class can be checked by this validator.
     *
     * @param type the class of the object to validate
     * @return <code>true</code> if objects of the given type are supported by this validator
     */
    @Override
    public final boolean supports(final Class<?> type) {
        return RuleModel.class.equals(type);
    }



    /**
     * Checks the conformity of a rule model.
     *
     * @param target the rule model to validate
     * @param errors an object that assembles the validation errors for an object
     */
    @Override
    public final void validate(final Object target, final Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "rule", "connectorDetails.errors.rule.text.empty");

        RuleModel rule = (RuleModel) target;

        if (rule.getProcessId() <= 0) {
            errors.rejectValue("idProcess", "connectorDetails.errors.rule.process.undefined");
        }
    }

}
