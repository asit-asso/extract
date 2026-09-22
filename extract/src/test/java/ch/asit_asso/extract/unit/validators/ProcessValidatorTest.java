/*
 * Copyright (C) 2026 asit-asso
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ch.asit_asso.extract.unit.validators;

import ch.asit_asso.extract.web.model.ProcessModel;
import ch.asit_asso.extract.web.validators.PluginItemModelParameterValidator;
import ch.asit_asso.extract.web.validators.ProcessValidator;
import ch.asit_asso.extract.web.validators.TaskValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the process description validation (issue #374).
 *
 * @author Bruno Alves
 */
@DisplayName("Process Description Validator Tests (issue #374)")
class ProcessValidatorTest {

    private static final int DESCRIPTION_MAX_LENGTH = 4000;

    private ProcessValidator validator;



    @BeforeEach
    void setUp() {
        this.validator = new ProcessValidator(new TaskValidator(new PluginItemModelParameterValidator()));
    }



    @Test
    @DisplayName("A description is optional and accepts exactly 4000 characters")
    void optionalDescriptionAcceptsTheMaximumLength() {
        assertNull(this.descriptionError(null));
        assertNull(this.descriptionError("a".repeat(ProcessValidatorTest.DESCRIPTION_MAX_LENGTH)));
    }



    @Test
    @DisplayName("A description longer than 4000 characters is rejected")
    void descriptionLongerThanMaximumLengthIsRejected() {
        final FieldError error
                = this.descriptionError("a".repeat(ProcessValidatorTest.DESCRIPTION_MAX_LENGTH + 1));

        assertNotNull(error);
        assertEquals("processDetails.errors.description.tooLong", error.getCode());
    }



    private FieldError descriptionError(final String description) {
        final ProcessModel process = new ProcessModel();
        process.setDescription(description);
        final BeanPropertyBindingResult errors = new BeanPropertyBindingResult(process, "process");

        this.validator.validate(process, errors);

        return errors.getFieldError("description");
    }
}
