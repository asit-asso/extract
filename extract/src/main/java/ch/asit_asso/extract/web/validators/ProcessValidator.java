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

import org.apache.commons.lang3.StringUtils;
import ch.asit_asso.extract.web.model.ProcessModel;
import ch.asit_asso.extract.web.model.TaskModel;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;



/**
 * An object that ensures that a model representing a process contains valid data.
 *
 * @author Yves Grasset
 */
public class ProcessValidator extends BaseValidator {

    /**
     * An object that can check the conformity of a task model.
     */
    private final TaskValidator taskValidator;



    /**
     * Creates a new instance of this validator.
     *
     * @param processTaskValidator an object that can check the conformity of a task model
     */
    public ProcessValidator(final TaskValidator processTaskValidator) {
        this.checkValidator(processTaskValidator, TaskModel.class);

        this.taskValidator = processTaskValidator;
    }



    /**
     * Determines if objects of a given type can be checked by this validator.
     *
     * @param type the class of the object to validate
     * @return <code>true</code> if objects of the given type can be validated
     */
    @Override
    public final boolean supports(final Class<?> type) {
        return ProcessModel.class.equals(type);
    }



    /**
     * Checks the conformity of a process model.
     *
     * @param target the process to validate
     * @param errors an object assembling the validation errors for an object
     */
    @Override
    public final void validate(final Object target, final Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "processDetails.errors.name.empty");

        final ProcessModel process = (ProcessModel) target;

        int taskIndex = 0;

        if (StringUtils.isEmpty(process.getUsersIds()) && StringUtils.isEmpty(process.getUserGroupsIds())) {
            errors.rejectValue("users", "processDetails.errors.users.empty", "");
        }

        if (process.getTasks().length == 0) {
            errors.rejectValue("tasks", "processDetails.errors.tasks.empty", "");
        } else {
            for (TaskModel task : process.getTasks()) {

                try {
                    errors.pushNestedPath(String.format("tasks[%d]", taskIndex));
                    ValidationUtils.invokeValidator(this.taskValidator, task, errors);
                    taskIndex++;

                } finally {
                    errors.popNestedPath();
                }
            }
        }
    }

}
