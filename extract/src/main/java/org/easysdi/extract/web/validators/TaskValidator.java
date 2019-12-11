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

import org.easysdi.extract.web.model.TaskModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;



/**
 * An object ensuring that the model representing a task contains valid information.
 *
 * @author Yves Grasset
 */
public class TaskValidator extends PluginItemValidator {

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(TaskValidator.class);



    /**
     * Creates a new instance of this validator.
     *
     * @param taskParameterValidator an object that checks that a task parameter contains valid information
     */
    public TaskValidator(final PluginItemModelParameterValidator taskParameterValidator) {
        super(taskParameterValidator);
    }



    /**
     * Determines if a given class can be checked with this validator.
     *
     * @param type the class of the object to validate
     * @return <code>true</code> if the class can be validated
     */
    @Override
    public final boolean supports(final Class<?> type) {
        return TaskModel.class.equals(type);
    }



    /**
     * Checks the conformity of a model representing a task.
     *
     * @param target the task model to check
     * @param errors an object that assembles the validation errors for the task model
     */
    @Override
    public final void validate(final Object target, final Errors errors) {
        final TaskModel task = (TaskModel) target;
        final Object[] taskIdParams = new Object[]{
            task.getId()
        };

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "pluginCode", "processDetails.errors.task.pluginCode.empty",
                taskIdParams);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "pluginLabel", "processDetails.errors.task.pluginLabel.empty",
                taskIdParams);

        if (task.getPosition() < 0) {
            errors.rejectValue("position", "processDetails.errors.task.position.negative", taskIdParams, "");
        }

        this.validateParameters(task, errors);
    }

}
