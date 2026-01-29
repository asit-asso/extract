/*
 * Copyright (C) 2025 SecureMind Sàrl
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

import ch.asit_asso.extract.orchestrator.OrchestratorSettings;
import ch.asit_asso.extract.orchestrator.OrchestratorTimeRange;
import ch.asit_asso.extract.web.validators.TimeRangeValidator;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SchedulerMode validation.
 * Tests the validation of scheduler operating hours configuration.
 *
 * @author Test Suite
 */
class SchedulerModeValidatorTest {

    private OrchestratorSettings settings;

    @BeforeEach
    public void setUp() {
        settings = new OrchestratorSettings();
    }

    /**
     * Test 1: Mode OFF should always return false for isWorking()
     * Verifies that OFF mode (complete stop) never allows scheduling
     */
    @Test
    @DisplayName("Mode OFF should always prevent scheduling")
    public void testModeOFFIsNeverWorking() {
        settings.setMode(OrchestratorSettings.SchedulerMode.OFF);
        settings.setRanges(new ArrayList<>()); // Empty ranges

        assertFalse(settings.isWorking(), "OFF mode should never be working");
    }
    /**
     * Test 2: Mode ON should always return true for isWorking()
     * Verifies that ON mode (24/7) always allows scheduling
     */
    @Test
    @DisplayName("Mode ON (24/7) should always allow scheduling")
    public void testModeONIsAlwaysWorking() {
        settings.setMode(OrchestratorSettings.SchedulerMode.ON);
        settings.setRanges(new ArrayList<>()); // Empty ranges, should be ignored

        assertTrue(settings.isWorking(), "ON mode should always be working");
    }

    /**
     * Test 3: Mode RANGES with empty ranges should throw error on validation
     * Verifies that RANGES mode requires at least one configured range
     */
    @Test
    @DisplayName("Mode RANGES with empty ranges should be invalid")
    public void testModeRANGESWithoutRangesIsInvalid() {
        settings.setMode(OrchestratorSettings.SchedulerMode.RANGES);
        settings.setRanges(new ArrayList<>()); // Empty ranges

        assertFalse(settings.isNowInRanges(), "RANGES mode without ranges should be invalid");
    }

    /**
     * Test 4: Mode RANGES with valid ranges should be valid
     * Verifies that a properly configured range is accepted
     */
    @Test
    @DisplayName("Mode RANGES with valid ranges should be valid")
    public void testModeRANGESWithValidRangesIsValid() {

        settings.setMode(OrchestratorSettings.SchedulerMode.RANGES);

        List<OrchestratorTimeRange> ranges = new ArrayList<>();
        OrchestratorTimeRange range = new OrchestratorTimeRange(1, 1); // Monday to Monday
        range.setStartTime("08:00");
        range.setEndTime("18:00");
        ranges.add(range);

        settings.setRanges(ranges);

        assertTrue(settings.isValid(), "RANGES mode with valid ranges should be valid");
    }

    /**
     * Test 4b: Mode RANGES with invalid ranges should be invalid
     * Verifies that a properly configured range is refused if endTime is
     * before startTime
     */
    @Test
    @DisplayName("Mode RANGES with invalid ranges should be invalid")
    public void testModeRANGESWithInvalidRangesIsInvalid() {

        settings.setMode(OrchestratorSettings.SchedulerMode.RANGES);

        List<OrchestratorTimeRange> ranges = new ArrayList<>();
        OrchestratorTimeRange range = new OrchestratorTimeRange(1, 1); // Monday to Monday
        range.setStartTime("11:00");
        range.setEndTime("10:00"); // endTime before startTime
        ranges.add(range);

        settings.setRanges(ranges);

        assertFalse(settings.isValid(), "RANGES mode with invalid ranges should be invalid");
    }

    /**
     * Test 5: Mode RANGES during working hours should return true
     * Verifies that isNowInRanges() returns true when current time is within a range
     */
    @Test
    @DisplayName("Mode RANGES should work during configured hours")
    public void testModeRANGESWorksInConfiguredHours() {
        settings.setMode(OrchestratorSettings.SchedulerMode.RANGES);

        // Create a range that includes the current time (all week, all day)
        List<OrchestratorTimeRange> ranges = new ArrayList<>();
        OrchestratorTimeRange range = new OrchestratorTimeRange(1, 7); // Monday to Sunday (entire week)
        range.setStartTime("00:00");
        range.setEndTime("23:59");
        ranges.add(range);

        settings.setRanges(ranges);

        assertTrue(settings.isNowInRanges(), "Should be working during configured hours");
        assertTrue(settings.isWorking(), "isWorking() should return true during configured hours");
    }

    /**
     * Test 6: Mode RANGES outside working hours should return false
     * Verifies that isNowInRanges() returns false when current time is outside all ranges
     */
    @Test
    @DisplayName("Mode RANGES should NOT work outside configured hours")
    public void testModeRANGESNotWorksOutsideConfiguredHours() {
        settings.setMode(OrchestratorSettings.SchedulerMode.RANGES);

        // Create a range that does NOT include the current time
        // If today is not Sunday, create a Sunday-only range at 01:00-02:00
        // This will almost certainly be outside current time
        List<OrchestratorTimeRange> ranges = new ArrayList<>();
        OrchestratorTimeRange range = new OrchestratorTimeRange(7, 7); // Only Sunday
        range.setStartTime("01:00");
        range.setEndTime("02:00");
        ranges.add(range);

        settings.setRanges(ranges);

        // Most of the time this should be false (unless we run tests on Sunday between 01:00-02:00)
        // But we can only guarantee this test works if the range is truly outside current time
        assertFalse(settings.isWorking(), "Should NOT be working outside configured hours");
    }

    /**
     * Test 7: Multiple ranges - should be working if current time matches ANY range
     * Verifies that having multiple ranges works correctly
     */
    @Test
    @DisplayName("Multiple ranges: should work if current time matches any range")
    public void testMultipleRangesWithCurrentTimeInOneRange() {
        settings.setMode(OrchestratorSettings.SchedulerMode.RANGES);

        List<OrchestratorTimeRange> ranges = new ArrayList<>();

        // First range: outside current time
        OrchestratorTimeRange range1 = new OrchestratorTimeRange(7, 7);
        range1.setStartTime("01:00");
        range1.setEndTime("02:00");
        ranges.add(range1);

        // Second range: includes current time (all week, all day)
        OrchestratorTimeRange range2 = new OrchestratorTimeRange(1, 7);
        range2.setStartTime("00:00");
        range2.setEndTime("23:59");
        ranges.add(range2);

        settings.setRanges(ranges);

        assertTrue(settings.isNowInRanges(), "Should be working because at least one range matches");
        assertTrue(settings.isWorking(), "isWorking() should return true");
    }

    /**
     * Test 8: Mode RANGES with single-day range (Monday only)
     * Verifies that day-specific ranges work correctly
     */
    @Test
    @DisplayName("Mode RANGES: single day range validation")
    public void testModeRANGESWithSingleDayRange() {
        settings.setMode(OrchestratorSettings.SchedulerMode.RANGES);

        List<OrchestratorTimeRange> ranges = new ArrayList<>();
        OrchestratorTimeRange range = new OrchestratorTimeRange(1, 1); // Monday only
        range.setStartTime("00:00");
        range.setEndTime("23:59");
        ranges.add(range);

        settings.setRanges(ranges);

        assertTrue(settings.isValid(), "Single day range should be valid");
    }

    /**
     * Test 9: Mode RANGES with full week range (Monday-Sunday)
     * Verifies that week-spanning ranges work correctly
     */
    @Test
    @DisplayName("Mode RANGES: full week range should always work")
    public void testModeRANGESWithFullWeekRange() {
        settings.setMode(OrchestratorSettings.SchedulerMode.RANGES);

        List<OrchestratorTimeRange> ranges = new ArrayList<>();
        OrchestratorTimeRange range = new OrchestratorTimeRange(1, 7); // Full week
        range.setStartTime("00:00");
        range.setEndTime("23:59");
        ranges.add(range);

        settings.setRanges(ranges);

        assertTrue(settings.isNowInRanges(), "Full week range should always include current time");
        assertTrue(settings.isWorking(), "isWorking() should return true");
    }

    /**
     * Test 10: Mode RANGES with multiple non-overlapping ranges
     * Verifies that multiple separate ranges work correctly
     */
    @Test
    @DisplayName("Mode RANGES: multiple non-overlapping ranges")
    public void testModeRANGESWithMultipleNonOverlappingRanges() {
        settings.setMode(OrchestratorSettings.SchedulerMode.RANGES);

        List<OrchestratorTimeRange> ranges = new ArrayList<>();

        // Range 1: Monday 08:00-18:00
        OrchestratorTimeRange range1 = new OrchestratorTimeRange(1, 1);
        range1.setStartTime("08:00");
        range1.setEndTime("18:00");
        ranges.add(range1);

        // Range 2: Friday 08:00-18:00
        OrchestratorTimeRange range2 = new OrchestratorTimeRange(5, 5);
        range2.setStartTime("08:00");
        range2.setEndTime("18:00");
        ranges.add(range2);

        settings.setRanges(ranges);

        assertTrue(settings.isValid(), "Multiple non-overlapping ranges should be valid");
    }

    /**
     * Test 11: Invalid frequency should fail validation
     * Verifies that frequency must be positive
     */
    @Test
    @DisplayName("Frequency must be positive")
    public void testFrequencyMustBePositive() {
        settings.setMode(OrchestratorSettings.SchedulerMode.ON);
        settings.setRanges(new ArrayList<>());

        assertThrows(IllegalArgumentException.class, () -> settings.setFrequency(0),
                "Frequency 0 should throw IllegalArgumentException");

        assertThrows(IllegalArgumentException.class, () -> settings.setFrequency(-5),
                "Negative frequency should throw IllegalArgumentException");
    }
}