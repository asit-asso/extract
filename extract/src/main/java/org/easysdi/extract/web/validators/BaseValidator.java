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

import org.springframework.validation.Validator;



/**
 * An abstract that provides generic methods for model validators.
 *
 * @author Yves Grasset
 */
public abstract class BaseValidator implements Validator {

    /**
     * Ensures that a given validator is fit to check the conformity of the objects that it is supposed
     * to validate.
     *
     * @param validator      the validator to check
     * @param typeToValidate the type of the object that the validator will deal with
     * @throws IllegalArgumentException the validator is null or not suitable for the desired type
     */
    protected final void checkValidator(final Validator validator, final Class<?> typeToValidate) {

        if (validator == null) {
            throw new IllegalArgumentException("The validator cannot be null.");
        }

        if (!validator.supports(typeToValidate)) {
            throw new IllegalArgumentException(String.format("The validator %s is not suitable to validate %s objects.",
                    validator.getClass().getCanonicalName(), typeToValidate.getCanonicalName()));
        }
    }

}
