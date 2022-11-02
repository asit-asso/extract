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

import ch.asit_asso.extract.web.model.PluginItemModelParameter;
import ch.asit_asso.extract.web.model.PluginItemModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;



/**
 * An object ensuring that a model representing an object that implements a plugin contains valid
 * information.
 *
 * @author Yves Grasset
 */
public abstract class PluginItemValidator extends BaseValidator {

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(PluginItemValidator.class);

    /**
     * An object that can check the conformity of a non-standard plugin parameter.
     */
    private final PluginItemModelParameterValidator parameterValidator;



    /**
     * Creates a new instance of this validator.
     *
     * @param modelParameterValidator an object that can check the conformity of a non-standard parameter.
     */
    protected PluginItemValidator(final PluginItemModelParameterValidator modelParameterValidator) {
        this.checkValidator(modelParameterValidator, PluginItemModelParameter.class);

        this.parameterValidator = modelParameterValidator;
    }



    /**
     * Checks that the plugin parameters contain valid information.
     *
     * @param target the model whose parameters must be validated
     * @param errors an object that assembles the validation errors for the object to validate
     */
    protected final void validateParameters(final PluginItemModel target, final Errors errors) {
        int parameterIndex = 0;

        for (PluginItemModelParameter parameter : target.getParameters()) {

            try {
                errors.pushNestedPath(String.format("parameters[%d]", parameterIndex));

                if (!StringUtils.hasText(parameter.getName())) {
                    this.logger.warn("Parameter number {} for object {} has an empty name.", parameterIndex, target);
                    errors.rejectValue("name", "parameter.errors.name.empty", new Object[]{parameterIndex},
                            "parameter.errors.generic");
                    continue;
                }

                ValidationUtils.invokeValidator(this.parameterValidator, parameter, errors);
                parameterIndex++;

            } finally {
                errors.popNestedPath();
            }
        }

    }

}
