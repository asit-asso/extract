/*
 * Copyright (C) 2024 arx iT
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
package ch.asit_asso.extract.unit.validators;

import ch.asit_asso.extract.orchestrator.OrchestratorTimeRange;
import ch.asit_asso.extract.web.validators.TimeRangeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TimeRangeValidator.
 * Tests all validation logic for OrchestratorTimeRange objects.
 *
 * Note: Since OrchestratorTimeRange setters throw IllegalArgumentException for invalid values,
 * we use ReflectionTestUtils to set invalid values directly for testing the validator.
 *
 * IMPORTANT LIMITATION: The validator calls DateTimeUtils.compareTimeStrings() at line 62
 * without checking if there are previous validation errors. This means that if time strings
 * are null or invalid, compareTimeStrings() will throw an exception before the validator
 * can properly report field errors. Therefore, this test suite focuses on testing valid
 * time strings with invalid day indices, and testing the end time comparison logic.
 *
 * @author Bruno Alves
 */
@DisplayName("TimeRangeValidator")
class TimeRangeValidatorTest {

    private TimeRangeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TimeRangeValidator();
    }

    /**
     * Helper method to create an OrchestratorTimeRange with potentially invalid values
     * using reflection to bypass setter validation.
     */
    private OrchestratorTimeRange createRange(int startDay, int endDay, String startTime, String endTime) {
        OrchestratorTimeRange range = new OrchestratorTimeRange();
        ReflectionTestUtils.setField(range, "startDayIndex", startDay);
        ReflectionTestUtils.setField(range, "endDayIndex", endDay);
        ReflectionTestUtils.setField(range, "startTime", startTime);
        ReflectionTestUtils.setField(range, "endTime", endTime);
        return range;
    }

    // ========================================================================
    // TESTS FOR supports() METHOD
    // ========================================================================

    @Nested
    @DisplayName("supports() method")
    class SupportsMethod {

        @Test
        @DisplayName("should support OrchestratorTimeRange class")
        void shouldSupportOrchestratorTimeRangeClass() {
            assertTrue(validator.supports(OrchestratorTimeRange.class));
        }

        @Test
        @DisplayName("should not support null class")
        void shouldNotSupportNullClass() {
            assertFalse(validator.supports(null));
        }

        @Test
        @DisplayName("should not support Object class")
        void shouldNotSupportObjectClass() {
            assertFalse(validator.supports(Object.class));
        }

        @Test
        @DisplayName("should not support String class")
        void shouldNotSupportStringClass() {
            assertFalse(validator.supports(String.class));
        }

        @Test
        @DisplayName("should not support subclass of OrchestratorTimeRange")
        void shouldNotSupportSubclass() {
            class SubTimeRange extends OrchestratorTimeRange {
                SubTimeRange() {
                    super();
                }
            }
            assertFalse(validator.supports(SubTimeRange.class));
        }
    }

    // ========================================================================
    // TESTS FOR validate() METHOD - VALID RANGES
    // ========================================================================

    @Nested
    @DisplayName("validate() - Valid Ranges")
    class ValidRanges {

        @Test
        @DisplayName("should accept valid range with all fields correct")
        void shouldAcceptValidRange() {
            OrchestratorTimeRange range = new OrchestratorTimeRange(1, 5, "08:00", "18:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            validator.validate(range, errors);

            assertFalse(errors.hasErrors(), "Valid range should not have errors");
        }

        @Test
        @DisplayName("should accept range with 24:00 as end time")
        void shouldAcceptRangeWith2400EndTime() {
            OrchestratorTimeRange range = new OrchestratorTimeRange(1, 7, "00:00", "24:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            validator.validate(range, errors);

            assertFalse(errors.hasErrors(), "Range with 24:00 should be valid");
        }

        @Test
        @DisplayName("should accept range spanning full week")
        void shouldAcceptFullWeekRange() {
            OrchestratorTimeRange range = new OrchestratorTimeRange(1, 7, "00:00", "24:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            validator.validate(range, errors);

            assertFalse(errors.hasErrors(), "Full week range should be valid");
        }

        @Test
        @DisplayName("should accept single day range")
        void shouldAcceptSingleDayRange() {
            OrchestratorTimeRange range = new OrchestratorTimeRange(3, 3, "09:00", "17:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            validator.validate(range, errors);

            assertFalse(errors.hasErrors(), "Single day range should be valid");
        }

        @Test
        @DisplayName("should accept wrap-around week range (Friday to Monday)")
        void shouldAcceptWrapAroundWeekRange() {
            OrchestratorTimeRange range = new OrchestratorTimeRange(5, 1, "08:00", "18:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            validator.validate(range, errors);

            assertFalse(errors.hasErrors(), "Wrap-around week range should be valid");
        }

        @Test
        @DisplayName("should accept range with minimum valid day indices (1-1)")
        void shouldAcceptMinimumDayIndices() {
            OrchestratorTimeRange range = new OrchestratorTimeRange(1, 1, "00:00", "24:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            validator.validate(range, errors);

            assertFalse(errors.hasErrors());
        }

        @Test
        @DisplayName("should accept range with maximum valid day indices (7-7)")
        void shouldAcceptMaximumDayIndices() {
            OrchestratorTimeRange range = new OrchestratorTimeRange(7, 7, "00:00", "24:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            validator.validate(range, errors);

            assertFalse(errors.hasErrors());
        }

        @Test
        @DisplayName("should accept range at boundary of valid day indices (1-7)")
        void shouldAcceptBoundaryDayIndices() {
            OrchestratorTimeRange range = new OrchestratorTimeRange(1, 7, "00:00", "24:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            validator.validate(range, errors);

            assertFalse(errors.hasErrors());
        }

        @Test
        @DisplayName("should accept range with midnight as start (00:00)")
        void shouldAcceptMidnightStart() {
            OrchestratorTimeRange range = new OrchestratorTimeRange(1, 7, "00:00", "12:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            validator.validate(range, errors);

            assertFalse(errors.hasErrors());
        }

        @Test
        @DisplayName("should accept range with midnight as end (24:00)")
        void shouldAcceptMidnightEnd() {
            OrchestratorTimeRange range = new OrchestratorTimeRange(1, 7, "12:00", "24:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            validator.validate(range, errors);

            assertFalse(errors.hasErrors());
        }

        @Test
        @DisplayName("should accept range spanning entire day (00:00 to 24:00)")
        void shouldAcceptEntireDay() {
            OrchestratorTimeRange range = new OrchestratorTimeRange(1, 1, "00:00", "24:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            validator.validate(range, errors);

            assertFalse(errors.hasErrors());
        }

        @Test
        @DisplayName("should accept range with times at minute 59")
        void shouldAcceptTimesAtMinute59() {
            OrchestratorTimeRange range = new OrchestratorTimeRange(1, 7, "08:59", "17:59");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            validator.validate(range, errors);

            assertFalse(errors.hasErrors());
        }

        @Test
        @DisplayName("should accept range with times at hour 23")
        void shouldAcceptTimesAtHour23() {
            OrchestratorTimeRange range = new OrchestratorTimeRange(1, 7, "23:00", "23:59");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            validator.validate(range, errors);

            assertFalse(errors.hasErrors());
        }
    }

    // ========================================================================
    // TESTS FOR validate() METHOD - INVALID DAY INDICES
    // ========================================================================

    @Nested
    @DisplayName("validate() - Invalid Day Indices (with valid times)")
    class InvalidDayIndices {

        @Test
        @DisplayName("should reject startDayIndex below minimum (0)")
        void shouldRejectStartDayIndexBelowMinimum() {
            OrchestratorTimeRange range = createRange(0, 7, "08:00", "18:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            validator.validate(range, errors);

            assertTrue(errors.hasFieldErrors("startDayIndex"));
            assertEquals("parameters.errors.schedulerRange.startDayIndex.invalid",
                    errors.getFieldError("startDayIndex").getCode());
        }

        @Test
        @DisplayName("should reject startDayIndex above maximum (8)")
        void shouldRejectStartDayIndexAboveMaximum() {
            OrchestratorTimeRange range = createRange(8, 7, "08:00", "18:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            validator.validate(range, errors);

            assertTrue(errors.hasFieldErrors("startDayIndex"));
            assertEquals("parameters.errors.schedulerRange.startDayIndex.invalid",
                    errors.getFieldError("startDayIndex").getCode());
        }

        @Test
        @DisplayName("should reject endDayIndex below minimum (0)")
        void shouldRejectEndDayIndexBelowMinimum() {
            OrchestratorTimeRange range = createRange(1, 0, "08:00", "18:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            validator.validate(range, errors);

            assertTrue(errors.hasFieldErrors("endDayIndex"));
            assertEquals("parameters.errors.schedulerRange.endDayIndex.invalid",
                    errors.getFieldError("endDayIndex").getCode());
        }

        @Test
        @DisplayName("should reject endDayIndex above maximum (8)")
        void shouldRejectEndDayIndexAboveMaximum() {
            OrchestratorTimeRange range = createRange(1, 8, "08:00", "18:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            validator.validate(range, errors);

            assertTrue(errors.hasFieldErrors("endDayIndex"));
            assertEquals("parameters.errors.schedulerRange.endDayIndex.invalid",
                    errors.getFieldError("endDayIndex").getCode());
        }

        @Test
        @DisplayName("should reject negative startDayIndex (-1)")
        void shouldRejectNegativeStartDayIndex() {
            OrchestratorTimeRange range = createRange(-1, 7, "08:00", "18:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            validator.validate(range, errors);

            assertTrue(errors.hasFieldErrors("startDayIndex"));
            assertEquals("parameters.errors.schedulerRange.startDayIndex.invalid",
                    errors.getFieldError("startDayIndex").getCode());
        }

        @Test
        @DisplayName("should reject negative endDayIndex (-1)")
        void shouldRejectNegativeEndDayIndex() {
            OrchestratorTimeRange range = createRange(1, -1, "08:00", "18:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            validator.validate(range, errors);

            assertTrue(errors.hasFieldErrors("endDayIndex"));
            assertEquals("parameters.errors.schedulerRange.endDayIndex.invalid",
                    errors.getFieldError("endDayIndex").getCode());
        }

        @Test
        @DisplayName("should reject very large startDayIndex (100)")
        void shouldRejectVeryLargeStartDayIndex() {
            OrchestratorTimeRange range = createRange(100, 7, "08:00", "18:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            validator.validate(range, errors);

            assertTrue(errors.hasFieldErrors("startDayIndex"));
            assertEquals("parameters.errors.schedulerRange.startDayIndex.invalid",
                    errors.getFieldError("startDayIndex").getCode());
        }

        @Test
        @DisplayName("should reject very large endDayIndex (100)")
        void shouldRejectVeryLargeEndDayIndex() {
            OrchestratorTimeRange range = createRange(1, 100, "08:00", "18:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            validator.validate(range, errors);

            assertTrue(errors.hasFieldErrors("endDayIndex"));
            assertEquals("parameters.errors.schedulerRange.endDayIndex.invalid",
                    errors.getFieldError("endDayIndex").getCode());
        }

        @Test
        @DisplayName("should reject both invalid startDayIndex and endDayIndex simultaneously")
        void shouldRejectBothInvalidDayIndices() {
            OrchestratorTimeRange range = createRange(0, 8, "08:00", "18:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            validator.validate(range, errors);

            assertTrue(errors.hasFieldErrors("startDayIndex"));
            assertTrue(errors.hasFieldErrors("endDayIndex"));
            assertEquals(2, errors.getFieldErrorCount());
        }
    }

    // ========================================================================
    // TESTS FOR validate() METHOD - END TIME COMPARISON
    // ========================================================================

    @Nested
    @DisplayName("validate() - End Time Must Be Greater Than Start Time")
    class EndTimeComparison {

        @Test
        @DisplayName("should reject when endTime equals startTime (compareTimeStrings >= 0)")
        void shouldRejectEndTimeEqualsStartTime() {
            OrchestratorTimeRange range = createRange(1, 7, "12:00", "12:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            validator.validate(range, errors);

            assertTrue(errors.hasFieldErrors("endTime"));
            assertEquals("parameters.errors.schedulerRange.endTime.tooSmall",
                    errors.getFieldError("endTime").getCode());
        }

        @Test
        @DisplayName("should reject when endTime is before startTime")
        void shouldRejectEndTimeBeforeStartTime() {
            OrchestratorTimeRange range = createRange(1, 7, "18:00", "08:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            validator.validate(range, errors);

            assertTrue(errors.hasFieldErrors("endTime"));
            assertEquals("parameters.errors.schedulerRange.endTime.tooSmall",
                    errors.getFieldError("endTime").getCode());
        }

        @Test
        @DisplayName("should reject when endTime is 1 minute before startTime")
        void shouldRejectEndTimeOneMinuteBeforeStartTime() {
            OrchestratorTimeRange range = createRange(1, 7, "12:01", "12:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            validator.validate(range, errors);

            assertTrue(errors.hasFieldErrors("endTime"));
            assertEquals("parameters.errors.schedulerRange.endTime.tooSmall",
                    errors.getFieldError("endTime").getCode());
        }

        @Test
        @DisplayName("should reject when endTime is 00:00 and startTime is 24:00")
        void shouldRejectEndTime0000AndStartTime2400() {
            OrchestratorTimeRange range = createRange(1, 7, "24:00", "00:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            validator.validate(range, errors);

            assertTrue(errors.hasFieldErrors("endTime"));
            assertEquals("parameters.errors.schedulerRange.endTime.tooSmall",
                    errors.getFieldError("endTime").getCode());
        }

        @Test
        @DisplayName("should accept when endTime is 1 minute after startTime")
        void shouldAcceptEndTimeOneMinuteAfterStartTime() {
            OrchestratorTimeRange range = new OrchestratorTimeRange(1, 7, "12:00", "12:01");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            validator.validate(range, errors);

            assertFalse(errors.hasFieldErrors("endTime"));
        }

        @Test
        @DisplayName("should accept when endTime is significantly after startTime")
        void shouldAcceptEndTimeSignificantlyAfterStartTime() {
            OrchestratorTimeRange range = new OrchestratorTimeRange(1, 7, "08:00", "18:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            validator.validate(range, errors);

            assertFalse(errors.hasFieldErrors("endTime"));
        }
    }

    // ========================================================================
    // TESTS FOR validate() METHOD - COMBINED ERRORS
    // ========================================================================

    @Nested
    @DisplayName("validate() - Combined Errors")
    class CombinedErrors {

        @Test
        @DisplayName("should report both invalid day indices and endTime too small")
        void shouldReportInvalidDaysAndEndTimeTooSmall() {
            OrchestratorTimeRange range = createRange(0, 8, "18:00", "08:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            validator.validate(range, errors);

            assertTrue(errors.hasFieldErrors("startDayIndex"));
            assertTrue(errors.hasFieldErrors("endDayIndex"));
            assertTrue(errors.hasFieldErrors("endTime"));
            assertEquals(3, errors.getFieldErrorCount());
        }

        @Test
        @DisplayName("should report invalid startDayIndex and endTime too small")
        void shouldReportInvalidStartDayAndEndTimeTooSmall() {
            OrchestratorTimeRange range = createRange(0, 7, "18:00", "08:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            validator.validate(range, errors);

            assertTrue(errors.hasFieldErrors("startDayIndex"));
            assertTrue(errors.hasFieldErrors("endTime"));
            assertEquals(2, errors.getFieldErrorCount());
        }

        @Test
        @DisplayName("should report invalid endDayIndex and endTime too small")
        void shouldReportInvalidEndDayAndEndTimeTooSmall() {
            OrchestratorTimeRange range = createRange(1, 8, "18:00", "08:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            validator.validate(range, errors);

            assertTrue(errors.hasFieldErrors("endDayIndex"));
            assertTrue(errors.hasFieldErrors("endTime"));
            assertEquals(2, errors.getFieldErrorCount());
        }
    }

    // ========================================================================
    // TESTS DOCUMENTING KNOWN LIMITATIONS
    // ========================================================================

    @Nested
    @DisplayName("Known Limitations - Throws Exception Instead of Validating")
    class KnownLimitations {

        @Test
        @DisplayName("throws NullPointerException when startTime is null")
        void throwsNPEWithNullStartTime() {
            OrchestratorTimeRange range = createRange(1, 7, null, "18:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            assertThrows(NullPointerException.class, () -> validator.validate(range, errors));
        }

        @Test
        @DisplayName("throws NullPointerException when endTime is null")
        void throwsNPEWithNullEndTime() {
            OrchestratorTimeRange range = createRange(1, 7, "08:00", null);
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            assertThrows(NullPointerException.class, () -> validator.validate(range, errors));
        }

        @Test
        @DisplayName("throws IllegalArgumentException when startTime is empty")
        void throwsExceptionWithEmptyStartTime() {
            OrchestratorTimeRange range = createRange(1, 7, "", "18:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            assertThrows(IllegalArgumentException.class, () -> validator.validate(range, errors));
        }

        @Test
        @DisplayName("throws IllegalArgumentException when endTime is empty")
        void throwsExceptionWithEmptyEndTime() {
            OrchestratorTimeRange range = createRange(1, 7, "08:00", "");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            assertThrows(IllegalArgumentException.class, () -> validator.validate(range, errors));
        }

        @Test
        @DisplayName("throws IllegalArgumentException when startTime has invalid format")
        void throwsExceptionWithInvalidStartTimeFormat() {
            OrchestratorTimeRange range = createRange(1, 7, "invalid", "18:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            assertThrows(IllegalArgumentException.class, () -> validator.validate(range, errors));
        }

        @Test
        @DisplayName("throws IllegalArgumentException when endTime has invalid format")
        void throwsExceptionWithInvalidEndTimeFormat() {
            OrchestratorTimeRange range = createRange(1, 7, "08:00", "invalid");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            assertThrows(IllegalArgumentException.class, () -> validator.validate(range, errors));
        }

        @Test
        @DisplayName("throws IllegalArgumentException when startTime hour is beyond 24")
        void throwsExceptionWithStartTimeHourBeyond24() {
            OrchestratorTimeRange range = createRange(1, 7, "25:00", "18:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            assertThrows(IllegalArgumentException.class, () -> validator.validate(range, errors));
        }

        @Test
        @DisplayName("throws IllegalArgumentException when endTime hour is beyond 24")
        void throwsExceptionWithEndTimeHourBeyond24() {
            OrchestratorTimeRange range = createRange(1, 7, "08:00", "25:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            assertThrows(IllegalArgumentException.class, () -> validator.validate(range, errors));
        }

        @Test
        @DisplayName("throws IllegalArgumentException when startTime minutes are beyond 59")
        void throwsExceptionWithStartTimeMinutesBeyond59() {
            OrchestratorTimeRange range = createRange(1, 7, "08:60", "18:00");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            assertThrows(IllegalArgumentException.class, () -> validator.validate(range, errors));
        }

        @Test
        @DisplayName("throws IllegalArgumentException when endTime minutes are beyond 59")
        void throwsExceptionWithEndTimeMinutesBeyond59() {
            OrchestratorTimeRange range = createRange(1, 7, "08:00", "18:60");
            Errors errors = new BeanPropertyBindingResult(range, "timeRange");

            assertThrows(IllegalArgumentException.class, () -> validator.validate(range, errors));
        }
    }
}
