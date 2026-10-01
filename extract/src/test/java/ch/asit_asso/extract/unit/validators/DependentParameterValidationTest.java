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

import ch.asit_asso.extract.utils.PluginUtils;
import ch.asit_asso.extract.web.model.PluginItemModelParameter;
import ch.asit_asso.extract.web.model.TaskModel;
import ch.asit_asso.extract.web.validators.PluginItemModelParameterValidator;
import ch.asit_asso.extract.web.validators.TaskValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for task parameters that only apply while a boolean parameter is enabled (issue #368).
 *
 * @author Bruno Alves
 */
@DisplayName("Switch-dependent task parameters (issue #368)")
class DependentParameterValidationTest {

    private static final String PARAMETERS_JSON = "["
            + "{\"code\":\"cancelOnNoData\",\"label\":\"Cancel\",\"type\":\"boolean\"},"
            + "{\"code\":\"cancellationRemark\",\"label\":\"Remark\",\"type\":\"text\",\"req\":true,"
            + "\"maxlength\":4000,\"dependsOn\":\"cancelOnNoData\"}"
            + "]";

    private static final String REMARK_VALUE_FIELD = "parameters[1].value";

    private TaskValidator validator;

    private TaskModel task;



    @BeforeEach
    void setUp() {
        this.validator = new TaskValidator(new PluginItemModelParameterValidator());
        this.task = new TaskModel();
        this.task.setPluginCode("python");
        this.task.setPluginLabel("Extraction Python");
        this.task.setParameters(PluginUtils.parseParametersJson(DependentParameterValidationTest.PARAMETERS_JSON));
    }



    @Test
    @DisplayName("The plugin definition links the remark to its switch")
    void definitionDeclaresTheDependency() {
        final PluginItemModelParameter remark = this.task.getParameterByName("cancellationRemark");

        assertEquals("cancelOnNoData", remark.getDependsOn());
        assertTrue(this.task.hasDependentParameters("cancelOnNoData"));
        assertFalse(this.task.hasDependentParameters("cancellationRemark"));
    }



    @Test
    @DisplayName("A required remark may stay empty while its switch is off")
    void emptyRemarkIsAcceptedWhileTheSwitchIsOff() {
        this.setValues("false", "");

        assertFalse(this.task.isParameterActive(this.task.getParameterByName("cancellationRemark")));
        assertNull(this.remarkError());
    }



    @Test
    @DisplayName("A required remark must be filled once its switch is on")
    void emptyRemarkIsRejectedOnceTheSwitchIsOn() {
        this.setValues("true", "");

        final FieldError error = this.remarkError();

        assertTrue(this.task.isParameterActive(this.task.getParameterByName("cancellationRemark")));
        assertNotNull(error);
        assertEquals("parameter.errors.required", error.getCode());
    }



    @Test
    @DisplayName("A filled remark is accepted once its switch is on")
    void filledRemarkIsAcceptedOnceTheSwitchIsOn() {
        this.setValues("true", "Aucune donnée pour ce périmètre");

        assertNull(this.remarkError());
    }



    @Test
    @DisplayName("A task saved with the switch off loads its empty remark without being considered invalid")
    void emptyStoredRemarkDoesNotFailToLoad() {
        final PluginItemModelParameter remark = this.task.getParameterByName("cancellationRemark");

        assertDoesNotThrow(() -> remark.updateValue(""));
        assertDoesNotThrow(() -> remark.updateValue((Object) null));
    }



    private void setValues(final String switchValue, final String remarkValue) {
        this.task.getParameterByName("cancelOnNoData").setValue(switchValue);
        this.task.getParameterByName("cancellationRemark").setValue(remarkValue);
    }



    private FieldError remarkError() {
        final BeanPropertyBindingResult errors = new BeanPropertyBindingResult(this.task, "task");

        this.validator.validate(this.task, errors);

        return errors.getFieldError(DependentParameterValidationTest.REMARK_VALUE_FIELD);
    }
}
