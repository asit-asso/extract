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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.asit_asso.extract.unit.web;

import ch.asit_asso.extract.web.editors.ScrollPositionEditor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the conversion of the scroll position submitted by the browser.
 *
 * @author Bruno Alves
 */
@DisplayName("Scroll position editor")
class ScrollPositionEditorTest {

    @ParameterizedTest(name = "\"{0}\" gives {1}")
    @CsvSource({
            "3583.75, 3584",
            "12.4, 12",
            "250, 250",
            "' 42 ', 42",
            "0, 0",
            "-5, 0",
            "-0.4, 0",
            "1e20, 2147483647",
            "99999, 99999"
    })
    @DisplayName("A numeric position is rounded and limited to the integer range")
    void numericValuesAreRoundedAndClamped(final String text, final int expected) {
        assertEquals(expected, this.convert(text));
    }



    @ParameterizedTest(name = "\"{0}\" gives 0")
    @NullSource
    @ValueSource(strings = {"", "   ", "abc", "NaN", "Infinity", "-Infinity", "12px"})
    @DisplayName("A missing or invalid position gives 0")
    void invalidValuesGiveZero(final String text) {
        assertEquals(0, this.convert(text));
    }



    private int convert(final String text) {
        final ScrollPositionEditor editor = new ScrollPositionEditor();
        editor.setAsText(text);

        return (int) editor.getValue();
    }
}
