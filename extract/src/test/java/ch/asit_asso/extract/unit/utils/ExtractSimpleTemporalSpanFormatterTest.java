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

import ch.asit_asso.extract.utils.ExtractSimpleTemporalSpanFormatter;
import ch.asit_asso.extract.utils.SimpleTemporalSpan;
import ch.asit_asso.extract.utils.SimpleTemporalSpan.TemporalField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExtractSimpleTemporalSpanFormatter class.
 *
 * Tests:
 * - Constructor validation
 * - Format method with default locale
 * - Format method with specified locale
 * - Singular/plural field handling
 * - All temporal fields
 *
 * @author Bruno Alves
 */
@DisplayName("ExtractSimpleTemporalSpanFormatter Tests")
class ExtractSimpleTemporalSpanFormatterTest {

    @Mock
    private MessageSource messageSource;

    private ExtractSimpleTemporalSpanFormatter formatter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ==================== 1. CONSTRUCTOR TESTS ====================

    @Nested
    @DisplayName("1. Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("1.1 - Throws IllegalArgumentException when messageSource is null")
        void throwsExceptionWhenMessageSourceIsNull() {
            // When/Then: Should throw IllegalArgumentException
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ExtractSimpleTemporalSpanFormatter(null)
            );

            assertEquals("The localized strings source cannot be null.", exception.getMessage());
        }

        @Test
        @DisplayName("1.2 - Successfully creates formatter with valid messageSource")
        void createsFormatterWithValidMessageSource() {
            // When/Then: Should create formatter without exception
            assertDoesNotThrow(() -> new ExtractSimpleTemporalSpanFormatter(messageSource));
        }
    }

    // ==================== 2. FORMAT WITH DEFAULT LOCALE ====================

    @Nested
    @DisplayName("2. Format with Default Locale")
    class FormatWithDefaultLocaleTests {

        @BeforeEach
        void setUp() {
            formatter = new ExtractSimpleTemporalSpanFormatter(messageSource);
        }

        @Test
        @DisplayName("2.1 - Throws IllegalArgumentException when span is null")
        void throwsExceptionWhenSpanIsNull() {
            // When/Then: Should throw IllegalArgumentException
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> formatter.format(null)
            );

            assertEquals("The span cannot be null.", exception.getMessage());
        }

        @Test
        @DisplayName("2.2 - Formats span with default locale")
        void formatsSpanWithDefaultLocale() {
            // Given: A span and mocked message source
            SimpleTemporalSpan span = new SimpleTemporalSpan(5, TemporalField.DAYS);

            when(messageSource.getMessage(eq("temporalField.plural.DAYS"), isNull(), any(Locale.class)))
                .thenReturn("jours");
            when(messageSource.getMessage(eq("temporalSpan.string"), any(Object[].class), any(Locale.class)))
                .thenReturn("5 jours");

            // When: Formatting the span
            String result = formatter.format(span);

            // Then: Should return formatted string
            assertEquals("5 jours", result);
            verify(messageSource).getMessage(eq("temporalField.plural.DAYS"), isNull(), any(Locale.class));
            verify(messageSource).getMessage(eq("temporalSpan.string"), any(Object[].class), any(Locale.class));
        }
    }

    // ==================== 3. FORMAT WITH SPECIFIED LOCALE ====================

    @Nested
    @DisplayName("3. Format with Specified Locale")
    class FormatWithSpecifiedLocaleTests {

        @BeforeEach
        void setUp() {
            formatter = new ExtractSimpleTemporalSpanFormatter(messageSource);
        }

        @Test
        @DisplayName("3.1 - Throws IllegalArgumentException when span is null")
        void throwsExceptionWhenSpanIsNull() {
            // When/Then: Should throw IllegalArgumentException
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> formatter.format(null, Locale.FRENCH)
            );

            assertEquals("The span cannot be null.", exception.getMessage());
        }

        @Test
        @DisplayName("3.2 - Throws IllegalArgumentException when locale is null")
        void throwsExceptionWhenLocaleIsNull() {
            // Given: A valid span
            SimpleTemporalSpan span = new SimpleTemporalSpan(5, TemporalField.DAYS);

            // When/Then: Should throw IllegalArgumentException
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> formatter.format(span, null)
            );

            assertEquals("The locale cannot be null.", exception.getMessage());
        }

        @Test
        @DisplayName("3.3 - Formats span with French locale")
        void formatsSpanWithFrenchLocale() {
            // Given: A span and mocked message source
            SimpleTemporalSpan span = new SimpleTemporalSpan(3, TemporalField.HOURS);

            when(messageSource.getMessage(eq("temporalField.plural.HOURS"), isNull(), eq(Locale.FRENCH)))
                .thenReturn("heures");
            when(messageSource.getMessage(eq("temporalSpan.string"), any(Object[].class), eq(Locale.FRENCH)))
                .thenReturn("3 heures");

            // When: Formatting the span with French locale
            String result = formatter.format(span, Locale.FRENCH);

            // Then: Should return French formatted string
            assertEquals("3 heures", result);
            verify(messageSource).getMessage(eq("temporalField.plural.HOURS"), isNull(), eq(Locale.FRENCH));
        }

        @Test
        @DisplayName("3.4 - Formats span with English locale")
        void formatsSpanWithEnglishLocale() {
            // Given: A span and mocked message source
            SimpleTemporalSpan span = new SimpleTemporalSpan(2, TemporalField.WEEKS);

            when(messageSource.getMessage(eq("temporalField.plural.WEEKS"), isNull(), eq(Locale.ENGLISH)))
                .thenReturn("weeks");
            when(messageSource.getMessage(eq("temporalSpan.string"), any(Object[].class), eq(Locale.ENGLISH)))
                .thenReturn("2 weeks");

            // When: Formatting the span with English locale
            String result = formatter.format(span, Locale.ENGLISH);

            // Then: Should return English formatted string
            assertEquals("2 weeks", result);
        }
    }

    // ==================== 4. SINGULAR/PLURAL HANDLING ====================

    @Nested
    @DisplayName("4. Singular/Plural Field Handling")
    class SingularPluralHandlingTests {

        @BeforeEach
        void setUp() {
            formatter = new ExtractSimpleTemporalSpanFormatter(messageSource);
        }

        @Test
        @DisplayName("4.1 - Uses singular key when value is 1")
        void usesSingularKeyWhenValueIsOne() {
            // Given: A span with value 1
            SimpleTemporalSpan span = new SimpleTemporalSpan(1, TemporalField.DAYS);

            when(messageSource.getMessage(eq("temporalField.singular.DAYS"), isNull(), any(Locale.class)))
                .thenReturn("jour");
            when(messageSource.getMessage(eq("temporalSpan.string"), any(Object[].class), any(Locale.class)))
                .thenReturn("1 jour");

            // When: Formatting the span
            String result = formatter.format(span, Locale.FRENCH);

            // Then: Should use singular key
            verify(messageSource).getMessage(eq("temporalField.singular.DAYS"), isNull(), eq(Locale.FRENCH));
            assertEquals("1 jour", result);
        }

        @Test
        @DisplayName("4.2 - Uses singular key when value is 0")
        void usesSingularKeyWhenValueIsZero() {
            // Given: A span with value 0
            SimpleTemporalSpan span = new SimpleTemporalSpan(0, TemporalField.MINUTES);

            when(messageSource.getMessage(eq("temporalField.singular.MINUTES"), isNull(), any(Locale.class)))
                .thenReturn("minute");
            when(messageSource.getMessage(eq("temporalSpan.string"), any(Object[].class), any(Locale.class)))
                .thenReturn("0 minute");

            // When: Formatting the span
            formatter.format(span, Locale.FRENCH);

            // Then: Should use singular key (0 <= 1)
            verify(messageSource).getMessage(eq("temporalField.singular.MINUTES"), isNull(), eq(Locale.FRENCH));
        }

        @Test
        @DisplayName("4.3 - Uses plural key when value is greater than 1")
        void usesPluralKeyWhenValueGreaterThanOne() {
            // Given: A span with value > 1
            SimpleTemporalSpan span = new SimpleTemporalSpan(5, TemporalField.SECONDS);

            when(messageSource.getMessage(eq("temporalField.plural.SECONDS"), isNull(), any(Locale.class)))
                .thenReturn("secondes");
            when(messageSource.getMessage(eq("temporalSpan.string"), any(Object[].class), any(Locale.class)))
                .thenReturn("5 secondes");

            // When: Formatting the span
            formatter.format(span, Locale.FRENCH);

            // Then: Should use plural key
            verify(messageSource).getMessage(eq("temporalField.plural.SECONDS"), isNull(), eq(Locale.FRENCH));
        }

        @Test
        @DisplayName("4.4 - Uses singular key when value is negative")
        void usesSingularKeyWhenValueIsNegative() {
            // Given: A span with negative value (edge case)
            SimpleTemporalSpan span = new SimpleTemporalSpan(-1, TemporalField.HOURS);

            when(messageSource.getMessage(eq("temporalField.singular.HOURS"), isNull(), any(Locale.class)))
                .thenReturn("heure");
            when(messageSource.getMessage(eq("temporalSpan.string"), any(Object[].class), any(Locale.class)))
                .thenReturn("-1 heure");

            // When: Formatting the span
            formatter.format(span, Locale.FRENCH);

            // Then: Should use singular key (-1 <= 1)
            verify(messageSource).getMessage(eq("temporalField.singular.HOURS"), isNull(), eq(Locale.FRENCH));
        }

        @Test
        @DisplayName("4.5 - Uses plural key when value is 1.5")
        void usesPluralKeyWhenValueIsOnePointFive() {
            // Given: A span with decimal value > 1
            SimpleTemporalSpan span = new SimpleTemporalSpan(1.5, TemporalField.HOURS);

            when(messageSource.getMessage(eq("temporalField.plural.HOURS"), isNull(), any(Locale.class)))
                .thenReturn("heures");
            when(messageSource.getMessage(eq("temporalSpan.string"), any(Object[].class), any(Locale.class)))
                .thenReturn("1.5 heures");

            // When: Formatting the span
            formatter.format(span, Locale.FRENCH);

            // Then: Should use plural key (1.5 > 1)
            verify(messageSource).getMessage(eq("temporalField.plural.HOURS"), isNull(), eq(Locale.FRENCH));
        }
    }

    // ==================== 5. ALL TEMPORAL FIELDS ====================

    @Nested
    @DisplayName("5. All Temporal Fields")
    class AllTemporalFieldsTests {

        @BeforeEach
        void setUp() {
            formatter = new ExtractSimpleTemporalSpanFormatter(messageSource);
        }

        @ParameterizedTest(name = "5.{index} - Formats {0} field correctly")
        @EnumSource(TemporalField.class)
        @DisplayName("Formats all temporal fields")
        void formatsAllTemporalFields(TemporalField field) {
            // Given: A span with the given field
            SimpleTemporalSpan span = new SimpleTemporalSpan(2, field);
            String expectedFieldKey = "temporalField.plural." + field.name();

            when(messageSource.getMessage(eq(expectedFieldKey), isNull(), any(Locale.class)))
                .thenReturn(field.name().toLowerCase());
            when(messageSource.getMessage(eq("temporalSpan.string"), any(Object[].class), any(Locale.class)))
                .thenReturn("2 " + field.name().toLowerCase());

            // When: Formatting the span
            String result = formatter.format(span, Locale.ENGLISH);

            // Then: Should format correctly
            assertNotNull(result);
            verify(messageSource).getMessage(eq(expectedFieldKey), isNull(), eq(Locale.ENGLISH));
        }

        @Test
        @DisplayName("5.9 - Formats YEARS field")
        void formatsYearsField() {
            // Given
            SimpleTemporalSpan span = new SimpleTemporalSpan(10, TemporalField.YEARS);
            when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("10 years");

            // When
            String result = formatter.format(span, Locale.ENGLISH);

            // Then
            assertNotNull(result);
        }

        @Test
        @DisplayName("5.10 - Formats MONTHS field")
        void formatsMonthsField() {
            // Given
            SimpleTemporalSpan span = new SimpleTemporalSpan(6, TemporalField.MONTHS);
            when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("6 months");

            // When
            String result = formatter.format(span, Locale.ENGLISH);

            // Then
            assertNotNull(result);
        }

        @Test
        @DisplayName("5.11 - Formats MILLISECONDS field")
        void formatsMillisecondsField() {
            // Given
            SimpleTemporalSpan span = new SimpleTemporalSpan(500, TemporalField.MILLISECONDS);
            when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("500 milliseconds");

            // When
            String result = formatter.format(span, Locale.ENGLISH);

            // Then
            assertNotNull(result);
        }
    }

    // ==================== 6. EDGE CASES ====================

    @Nested
    @DisplayName("6. Edge Cases")
    class EdgeCasesTests {

        @BeforeEach
        void setUp() {
            formatter = new ExtractSimpleTemporalSpanFormatter(messageSource);
        }

        @Test
        @DisplayName("6.1 - Handles very large values")
        void handlesVeryLargeValues() {
            // Given
            SimpleTemporalSpan span = new SimpleTemporalSpan(Integer.MAX_VALUE, TemporalField.MILLISECONDS);
            when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn(Integer.MAX_VALUE + " milliseconds");

            // When
            String result = formatter.format(span, Locale.ENGLISH);

            // Then
            assertNotNull(result);
        }

        @Test
        @DisplayName("6.2 - Handles decimal values")
        void handlesDecimalValues() {
            // Given
            SimpleTemporalSpan span = new SimpleTemporalSpan(2.5, TemporalField.HOURS);
            when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("2.5 hours");

            // When
            String result = formatter.format(span, Locale.ENGLISH);

            // Then
            assertNotNull(result);
        }
    }
}
