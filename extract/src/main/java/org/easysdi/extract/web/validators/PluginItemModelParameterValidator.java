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

import org.apache.commons.lang3.math.NumberUtils;
import org.easysdi.extract.web.model.PluginItemModelParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;



/**
 * An object ensuring that a parameter for the model of an object that uses a plugin contains valid
 * information.
 *
 * @author Yves Grasset
 */
public class PluginItemModelParameterValidator extends BaseValidator {

    /**
     * The writer to the application logs.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(PluginItemModelParameterValidator.class);



    /**
     * Determines if a given class can be checked by this validator.
     *
     * @param type the class of the object to validate
     * @return <code>true</code> if the type is supported
     */
    @Override
    public final boolean supports(final Class<?> type) {
        return PluginItemModelParameter.class.equals(type);
    }



    /**
     * Checks the conformity of the parameter information.
     *
     * @param target the parameter to validate
     * @param errors the object that assembles the validation errors for the parameter
     */
    @Override
    public final void validate(final Object target, final Errors errors) {
        final PluginItemModelParameter parameter = (PluginItemModelParameter) target;
        PluginItemModelParameterValidator.LOGGER.debug("Validating value for parameter \"{}\"", parameter.getName());
        final String parameterLabel = parameter.getLabel();
        final String parameterType = parameter.getType();
        final Object[] nameParams = new Object[]{
            parameter.getName()
        };

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "label", "parameter.errors.label.empty",
                nameParams);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "type", "parameter.errors.type.empty",
                nameParams);

        if (!parameterType.equals("boolean") && !parameterType.equals("numeric") && parameter.getMaxLength() < 1) {
            errors.rejectValue("maxLength", "parameter.errors.maxLength.negative", nameParams,
                    "parameter.errors.generic");
            return;
        }

        final Object[] labelParams = new Object[]{
            parameterLabel
        };

        final Object value = parameter.getValue();

        if (value == null) {
            PluginItemModelParameterValidator.LOGGER.debug("The value for parameter \"{}\" is null",
                    parameter.getName());

            if (parameter.isRequired()) {
                errors.rejectValue("value", "parameter.errors.required", labelParams,
                        "parameter.errors.generic");
            }

            PluginItemModelParameterValidator.LOGGER.debug("The parameter \"{}\" is not mandatory, so everything is OK.",
                    parameter.getName());
            return;
        }

        if (!(value instanceof String) || parameterType.equals("boolean")) {
            PluginItemModelParameterValidator.LOGGER.debug("The value for parameter \"{}\" is not a string or the parameter is boolean.",
                    parameter.getName());
            return;
        }

        final String stringValue = (String) value;

        if (!StringUtils.hasText(stringValue)) {
            PluginItemModelParameterValidator.LOGGER.debug("The value for parameter \"{}\" is empty.",
                    parameter.getValue(), parameter.getName());

            if (parameter.isRequired()) {
                errors.rejectValue("value", "parameter.errors.required", labelParams,
                        "parameter.errors.generic");
            }

            PluginItemModelParameterValidator.LOGGER.debug("The parameter \"{}\" is not mandatory, so everything is OK.",
                    parameter.getName());
            return;
        }

        if (parameterType.equals("numeric")) {
            PluginItemModelParameterValidator.LOGGER.debug("The parameter \"{}\" is numeric.",
                    parameter.getName());

            if (!NumberUtils.isParsable(stringValue)) {
                errors.rejectValue("value", "parameter.errors.number.invalid", labelParams,
                        "parameter.errors.generic");
                return;
            }

            PluginItemModelParameterValidator.LOGGER.debug("The value for parameter \"{}\" is parseable.",
                    parameter.getName());

            final int intValue = NumberUtils.toInt(stringValue);
            final Integer maxValue = parameter.getMaxValue();
            final Integer minValue = parameter.getMinValue();
            final Integer step = parameter.getStep();

            if (maxValue != null && intValue > maxValue) {
                errors.rejectValue("value", "parameter.errors.number.tooLarge", new Object[]{
                    parameterLabel,
                    maxValue
                }, "parameter.errors.generic");

                return;
            }

            PluginItemModelParameterValidator.LOGGER.debug("The value {} for parameter \"{}\" is OK with the max (if set).",
                    intValue, parameter.getName());

            if (minValue != null && intValue < minValue) {
                errors.rejectValue("value", "parameter.errors.number.tooSmall", new Object[]{
                    parameterLabel,
                    minValue
                }, "parameter.errors.generic");

                return;
            }

            PluginItemModelParameterValidator.LOGGER.debug("The value {} for parameter \"{}\" is OK with the min (if set).",
                    intValue, parameter.getName());

            if (step != null) {
                final int relativeValue = (minValue != null) ? intValue - minValue : intValue;

                if (relativeValue % step != 0) {
                    errors.rejectValue("value", "parameter.errors.number.invalidStep", new Object[]{
                        parameterLabel,
                        step,
                        minValue
                    }, "parameter.errors.generic");
                }

                return;
            }

            PluginItemModelParameterValidator.LOGGER.debug("The value {} for parameter \"{}\" is OK with all the numeric criteria.",
                    intValue, parameter.getName());

            return;
        }

        final int maxLength = parameter.getMaxLength();

        if (stringValue.length() > maxLength) {
            errors.rejectValue("value", "parameter.errors.tooLong", new Object[]{
                parameterLabel,
                maxLength
            }, "parameter.errors.generic");

            return;
        }

        PluginItemModelParameterValidator.LOGGER.debug("The value for parameter \"{}\" is OK with the max length ({}).",
                parameter.getName(), maxLength);

        if (parameter.getType().equals("email") && !parameter.validateUpdatedValue(stringValue)) {
            errors.rejectValue("value", "parameter.errors.invalidEmailString", labelParams, "parameter.errors.generic");
            return;
        }

        PluginItemModelParameterValidator.LOGGER.debug("The value for parameter \"{}\" is OK with all criteria.",
                parameter.getName());
    }

}
