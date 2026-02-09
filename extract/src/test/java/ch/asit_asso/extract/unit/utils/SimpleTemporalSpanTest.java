/*
 * Copyright (C) 2025 arx iT
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
package ch.asit_asso.extract.unit.utils;

import ch.asit_asso.extract.utils.SimpleTemporalSpan;
import ch.asit_asso.extract.utils.SimpleTemporalSpan.TemporalField;
import ch.asit_asso.extract.utils.SimpleTemporalSpanFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SimpleTemporalSpan class.
 *
 * Tests:
 * - Constructor validation
 * - Getters (getValue, getField)
 * - toString method with and without formatter
 * - TemporalField enum
 *
 * @author Bruno Alves
 */
@DisplayName("SimpleTemporalSpan Tests")
class SimpleTemporalSpanTest {

    // ==================== 1. CONSTRUCTOR TESTS ====================

    @Nested
    @DisplayName("1. Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("1.1 - Throws IllegalArgumentException when temporalField is null")
        void throwsExceptionWhenTemporalFieldIsNull() {
            // When/Then: Should throw IllegalArgumentException
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new SimpleTemporalSpan(5, null)
            );

            assertEquals("The temporal field cannot be null.", exception.getMessage());
        }

        @Test
        @DisplayName("1.2 - Successfully creates span with positive value")
        void createsSpanWithPositiveValue() {
            // When: Creating a span with positive value
            SimpleTemporalSpan span = new SimpleTemporalSpan(10, TemporalField.DAYS);

            // Then: Should create span with correct values
            assertEquals(10, span.getValue());
            assertEquals(TemporalField.DAYS, span.getField());
        }

        @Test
        @DisplayName("1.3 - Successfully creates span with zero value")
        void createsSpanWithZeroValue() {
            // When: Creating a span with zero value
            SimpleTemporalSpan span = new SimpleTemporalSpan(0, TemporalField.HOURS);

            // Then: Should create span with zero value
            assertEquals(0, span.getValue());
            assertEquals(TemporalField.HOURS, span.getField());
        }

        @Test
        @DisplayName("1.4 - Successfully creates span with negative value")
        void createsSpanWithNegativeValue() {
            // When: Creating a span with negative value
            SimpleTemporalSpan span = new SimpleTemporalSpan(-5, TemporalField.MINUTES);

            // Then: Should create span with negative value
            assertEquals(-5, span.getValue());
            assertEquals(TemporalField.MINUTES, span.getField());
        }

        @Test
        @DisplayName("1.5 - Successfully creates span with decimal value")
        void createsSpanWithDecimalValue() {
            // When: Creating a span with decimal value
            SimpleTemporalSpan span = new SimpleTemporalSpan(2.5, TemporalField.HOURS);

            // Then: Should create span with decimal value
            assertEquals(2.5, span.getValue());
            assertEquals(TemporalField.HOURS, span.getField());
        }

        @ParameterizedTest(name = "1.6.{index} - Creates span with {0} field")
        @EnumSource(TemporalField.class)
        @DisplayName("1.6 - Successfully creates span with all temporal fields")
        void createsSpanWithAllTemporalFields(TemporalField field) {
            // When: Creating a span with the given field
            SimpleTemporalSpan span = new SimpleTemporalSpan(1, field);

            // Then: Should create span with correct field
            assertEquals(field, span.getField());
        }
    }

    // ==================== 2. GETTER TESTS ====================

    @Nested
    @DisplayName("2. Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("2.1 - getValue returns correct value")
        void getValueReturnsCorrectValue() {
            // Given: A span with a specific value
            SimpleTemporalSpan span = new SimpleTemporalSpan(42, TemporalField.SECONDS);

            // When/Then: getValue should return the correct value
            assertEquals(42, span.getValue());
        }

        @Test
        @DisplayName("2.2 - getField returns correct field")
        void getFieldReturnsCorrectField() {
            // Given: A span with a specific field
            SimpleTemporalSpan span = new SimpleTemporalSpan(1, TemporalField.WEEKS);

            // When/Then: getField should return the correct field
            assertEquals(TemporalField.WEEKS, span.getField());
        }

        @ParameterizedTest
        @ValueSource(doubles = {0.0, 0.5, 1.0, 1.5, 100.0, -50.0, Double.MAX_VALUE, Double.MIN_VALUE})
        @DisplayName("2.3 - getValue works with various numeric values")
        void getValueWorksWithVariousValues(double value) {
            // Given: A span with various values
            SimpleTemporalSpan span = new SimpleTemporalSpan(value, TemporalField.MILLISECONDS);

            // When/Then: getValue should return the correct value
            assertEquals(value, span.getValue());
        }
    }

    // ==================== 3. TO STRING TESTS ====================

    @Nested
    @DisplayName("3. toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("3.1 - toString with null formatter returns default toString")
        void toStringWithNullFormatterReturnsDefaultToString() {
            // Given: A span
            SimpleTemporalSpan span = new SimpleTemporalSpan(5, TemporalField.DAYS);

            // When: Calling toString with null formatter
            String result = span.toString(null);

            // Then: Should return default toString (from Object)
            assertNotNull(result);
            // Default toString returns something like "ch.asit_asso.extract.utils.SimpleTemporalSpan@hashcode"
            assertTrue(result.contains("SimpleTemporalSpan"));
        }

        @Test
        @DisplayName("3.2 - toString with formatter uses formatter")
        void toStringWithFormatterUsesFormatter() {
            // Given: A span and a mock formatter
            SimpleTemporalSpan span = new SimpleTemporalSpan(3, TemporalField.HOURS);
            SimpleTemporalSpanFormatter mockFormatter = mock(SimpleTemporalSpanFormatter.class);
            when(mockFormatter.format(span)).thenReturn("3 hours");

            // When: Calling toString with formatter
            String result = span.toString(mockFormatter);

            // Then: Should use the formatter
            assertEquals("3 hours", result);
            verify(mockFormatter).format(span);
        }

        @Test
        @DisplayName("3.3 - toString with formatter passes correct span")
        void toStringWithFormatterPassesCorrectSpan() {
            // Given: A span and a mock formatter
            SimpleTemporalSpan span = new SimpleTemporalSpan(7, TemporalField.WEEKS);
            SimpleTemporalSpanFormatter mockFormatter = mock(SimpleTemporalSpanFormatter.class);
            when(mockFormatter.format(any(SimpleTemporalSpan.class))).thenReturn("formatted");

            // When: Calling toString with formatter
            span.toString(mockFormatter);

            // Then: Formatter should receive the correct span
            verify(mockFormatter).format(span);
        }
    }

    // ==================== 4. TEMPORAL FIELD ENUM TESTS ====================

    @Nested
    @DisplayName("4. TemporalField Enum Tests")
    class TemporalFieldEnumTests {

        @Test
        @DisplayName("4.1 - Enum contains YEARS value")
        void enumContainsYears() {
            assertNotNull(TemporalField.valueOf("YEARS"));
        }

        @Test
        @DisplayName("4.2 - Enum contains MONTHS value")
        void enumContainsMonths() {
            assertNotNull(TemporalField.valueOf("MONTHS"));
        }

        @Test
        @DisplayName("4.3 - Enum contains WEEKS value")
        void enumContainsWeeks() {
            assertNotNull(TemporalField.valueOf("WEEKS"));
        }

        @Test
        @DisplayName("4.4 - Enum contains DAYS value")
        void enumContainsDays() {
            assertNotNull(TemporalField.valueOf("DAYS"));
        }

        @Test
        @DisplayName("4.5 - Enum contains HOURS value")
        void enumContainsHours() {
            assertNotNull(TemporalField.valueOf("HOURS"));
        }

        @Test
        @DisplayName("4.6 - Enum contains MINUTES value")
        void enumContainsMinutes() {
            assertNotNull(TemporalField.valueOf("MINUTES"));
        }

        @Test
        @DisplayName("4.7 - Enum contains SECONDS value")
        void enumContainsSeconds() {
            assertNotNull(TemporalField.valueOf("SECONDS"));
        }

        @Test
        @DisplayName("4.8 - Enum contains MILLISECONDS value")
        void enumContainsMilliseconds() {
            assertNotNull(TemporalField.valueOf("MILLISECONDS"));
        }

        @Test
        @DisplayName("4.9 - Enum has exactly 8 values")
        void enumHasExactlyEightValues() {
            assertEquals(8, TemporalField.values().length);
        }

        @Test
        @DisplayName("4.10 - Enum values are in correct order")
        void enumValuesInCorrectOrder() {
            TemporalField[] values = TemporalField.values();
            assertEquals(TemporalField.YEARS, values[0]);
            assertEquals(TemporalField.MONTHS, values[1]);
            assertEquals(TemporalField.WEEKS, values[2]);
            assertEquals(TemporalField.DAYS, values[3]);
            assertEquals(TemporalField.HOURS, values[4]);
            assertEquals(TemporalField.MINUTES, values[5]);
            assertEquals(TemporalField.SECONDS, values[6]);
            assertEquals(TemporalField.MILLISECONDS, values[7]);
        }
    }

    // ==================== 5. EDGE CASES ====================

    @Nested
    @DisplayName("5. Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("5.1 - Handles very large positive value")
        void handlesVeryLargePositiveValue() {
            // When: Creating a span with very large value
            SimpleTemporalSpan span = new SimpleTemporalSpan(Double.MAX_VALUE, TemporalField.MILLISECONDS);

            // Then: Should handle correctly
            assertEquals(Double.MAX_VALUE, span.getValue());
        }

        @Test
        @DisplayName("5.2 - Handles very small positive value")
        void handlesVerySmallPositiveValue() {
            // When: Creating a span with very small value
            SimpleTemporalSpan span = new SimpleTemporalSpan(Double.MIN_VALUE, TemporalField.SECONDS);

            // Then: Should handle correctly
            assertEquals(Double.MIN_VALUE, span.getValue());
        }

        @Test
        @DisplayName("5.3 - Handles infinity values")
        void handlesInfinityValues() {
            // When: Creating spans with infinity
            SimpleTemporalSpan positiveInfinity = new SimpleTemporalSpan(Double.POSITIVE_INFINITY, TemporalField.YEARS);
            SimpleTemporalSpan negativeInfinity = new SimpleTemporalSpan(Double.NEGATIVE_INFINITY, TemporalField.YEARS);

            // Then: Should handle correctly
            assertEquals(Double.POSITIVE_INFINITY, positiveInfinity.getValue());
            assertEquals(Double.NEGATIVE_INFINITY, negativeInfinity.getValue());
        }

        @Test
        @DisplayName("5.4 - Handles NaN value")
        void handlesNaNValue() {
            // When: Creating a span with NaN
            SimpleTemporalSpan span = new SimpleTemporalSpan(Double.NaN, TemporalField.DAYS);

            // Then: Should handle correctly (value is NaN)
            assertTrue(Double.isNaN(span.getValue()));
        }

        @Test
        @DisplayName("5.5 - Multiple spans with same values are independent")
        void multipleSpansAreIndependent() {
            // When: Creating two spans with same values
            SimpleTemporalSpan span1 = new SimpleTemporalSpan(5, TemporalField.DAYS);
            SimpleTemporalSpan span2 = new SimpleTemporalSpan(5, TemporalField.DAYS);

            // Then: They should be independent objects
            assertNotSame(span1, span2);
            assertEquals(span1.getValue(), span2.getValue());
            assertEquals(span1.getField(), span2.getField());
        }
    }
}
