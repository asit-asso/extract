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
package org.easysdi.extract.web.validators;

import org.easysdi.extract.web.model.ConnectorModel;
import org.easysdi.extract.web.model.RuleModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;



/**
 * An object ensuring that the model representing a connector contains valid information.
 *
 * @author Yves Grasset
 */
public class ConnectorValidator extends PluginItemValidator {

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(ConnectorValidator.class);

    /**
     * The object that checks that the values defined for a rule are valid.
     */
    private final RuleValidator ruleValidator;



    /**
     * Creates a new instance of this validator.
     *
     * @param connectorParameterValidator an object that can check the conformity of the connector non-standard
     *                                    parameters
     * @param connectorRuleValidator      an object that can check the conformity of the connector processing rules
     */
    public ConnectorValidator(final PluginItemModelParameterValidator connectorParameterValidator,
            final RuleValidator connectorRuleValidator) {
        super(connectorParameterValidator);
        this.checkValidator(connectorRuleValidator, RuleModel.class);

        this.ruleValidator = connectorRuleValidator;
    }



    /**
     * Determines whether a given class can be checked with this validator.
     *
     * @param type the class of the object to validate
     * @return <code>true</code> if the given class can be validated
     */
    @Override
    public final boolean supports(final Class<?> type) {
        return ConnectorModel.class.equals(type);
    }



    /**
     * Checks the conformity of the connector model information.
     *
     * @param object the object to validate
     * @param errors the object that assembles the validation errors for the object
     */
    @Override
    public final void validate(final Object object, final Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "connectorDetails.errors.name.empty");
        ConnectorModel connector = (ConnectorModel) object;
        this.logger.debug("Checking the validity of connector %s", connector.getName());

        if (connector.getImportFrequency() == null) {
            errors.rejectValue("importFrequency", "connectorDetails.errors.importFrequency.required");
        } else if (connector.getImportFrequency() < 1) {
            errors.rejectValue("importFrequency", "connectorDetails.errors.importFrequency.negative");
        }

        if (connector.getMaximumRetries() == null) {
            errors.rejectValue("maximumRetries", "connectorDetails.errors.maxRetries.required");
        } else if (connector.getMaximumRetries() < 0) {
            errors.rejectValue("maximumRetries", "connectorDetails.errors.maxRetries.negative");
        }

        this.validateParameters(connector, errors);

        int ruleIndex = 0;

        for (RuleModel rule : connector.getRules()) {

            try {
                errors.pushNestedPath(String.format("rules[%d]", ruleIndex));
                ValidationUtils.invokeValidator(this.ruleValidator, rule, errors);
                ruleIndex++;

            } finally {
                errors.popNestedPath();
            }
        }
    }

}
