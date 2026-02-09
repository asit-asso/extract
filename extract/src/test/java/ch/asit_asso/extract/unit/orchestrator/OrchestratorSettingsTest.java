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
package ch.asit_asso.extract.unit.orchestrator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.asit_asso.extract.orchestrator.OrchestratorSettings;
import ch.asit_asso.extract.orchestrator.OrchestratorSettings.SchedulerMode;
import ch.asit_asso.extract.orchestrator.OrchestratorTimeRange;
import ch.asit_asso.extract.orchestrator.OrchestratorTimeRangeCollection;
import ch.asit_asso.extract.persistence.SystemParametersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrchestratorSettings class.
 * Tests all branches including validation, mode switching, and time range handling.
 */
@ExtendWith(MockitoExtension.class)
public class OrchestratorSettingsTest {

    @Mock
    private SystemParametersRepository systemParametersRepository;

    @Nested
    @DisplayName("Default constructor tests")
    class DefaultConstructorTests {

        @Test
        @DisplayName("Default constructor sets default frequency to 20")
        void testDefaultConstructorSetsFrequency() {
            OrchestratorSettings settings = new OrchestratorSettings();

            assertEquals(20, settings.getFrequency());
        }

        @Test
        @DisplayName("Default constructor sets mode to ON")
        void testDefaultConstructorSetsMode() {
            OrchestratorSettings settings = new OrchestratorSettings();

            assertEquals(SchedulerMode.ON, settings.getMode());
        }

        @Test
        @DisplayName("Default constructor creates empty ranges collection")
        void testDefaultConstructorCreatesEmptyRanges() {
            OrchestratorSettings settings = new OrchestratorSettings();

            assertNotNull(settings.getRanges());
            assertEquals(0, settings.getRanges().getRanges().length);
        }
    }

    @Nested
    @DisplayName("Parameterized constructor tests")
    class ParameterizedConstructorTests {

        @Test
        @DisplayName("Constructor with valid parameters")
        void testConstructorWithValidParameters() {
            List<OrchestratorTimeRange> rangesList = new ArrayList<>();

            OrchestratorSettings settings = new OrchestratorSettings(30, SchedulerMode.RANGES, rangesList);

            assertEquals(30, settings.getFrequency());
            assertEquals(SchedulerMode.RANGES, settings.getMode());
            assertNotNull(settings.getRanges());
        }

        @Test
        @DisplayName("Constructor throws on negative frequency")
        void testConstructorThrowsOnNegativeFrequency() {
            List<OrchestratorTimeRange> rangesList = new ArrayList<>();

            assertThrows(IllegalArgumentException.class, () -> {
                new OrchestratorSettings(-1, SchedulerMode.ON, rangesList);
            });
        }

        @Test
        @DisplayName("Constructor throws on zero frequency")
        void testConstructorThrowsOnZeroFrequency() {
            List<OrchestratorTimeRange> rangesList = new ArrayList<>();

            assertThrows(IllegalArgumentException.class, () -> {
                new OrchestratorSettings(0, SchedulerMode.ON, rangesList);
            });
        }

        @Test
        @DisplayName("Constructor throws on null mode")
        void testConstructorThrowsOnNullMode() {
            List<OrchestratorTimeRange> rangesList = new ArrayList<>();

            assertThrows(IllegalArgumentException.class, () -> {
                new OrchestratorSettings(20, null, rangesList);
            });
        }

        @Test
        @DisplayName("Constructor throws on null ranges list")
        void testConstructorThrowsOnNullRangesList() {
            assertThrows(IllegalArgumentException.class, () -> {
                new OrchestratorSettings(20, SchedulerMode.ON, null);
            });
        }
    }

    @Nested
    @DisplayName("Repository constructor tests")
    class RepositoryConstructorTests {

        @Test
        @DisplayName("Constructor from repository loads values correctly")
        void testConstructorFromRepository() {
            when(systemParametersRepository.getSchedulerFrequency()).thenReturn("30");
            when(systemParametersRepository.getSchedulerMode()).thenReturn("ON");
            when(systemParametersRepository.getSchedulerRanges()).thenReturn("[]");

            OrchestratorSettings settings = new OrchestratorSettings(systemParametersRepository);

            assertEquals(30, settings.getFrequency());
            assertEquals(SchedulerMode.ON, settings.getMode());
            verify(systemParametersRepository).getSchedulerFrequency();
            verify(systemParametersRepository).getSchedulerMode();
            verify(systemParametersRepository).getSchedulerRanges();
        }

        @Test
        @DisplayName("Constructor throws on null repository")
        void testConstructorThrowsOnNullRepository() {
            assertThrows(IllegalArgumentException.class, () -> {
                new OrchestratorSettings((SystemParametersRepository) null);
            });
        }
    }

    @Nested
    @DisplayName("setFrequency tests")
    class SetFrequencyTests {

        @Test
        @DisplayName("setFrequency with positive value succeeds")
        void testSetFrequencyPositive() {
            OrchestratorSettings settings = new OrchestratorSettings();

            settings.setFrequency(60);

            assertEquals(60, settings.getFrequency());
        }

        @Test
        @DisplayName("setFrequency with 1 succeeds")
        void testSetFrequencyOne() {
            OrchestratorSettings settings = new OrchestratorSettings();

            settings.setFrequency(1);

            assertEquals(1, settings.getFrequency());
        }

        @Test
        @DisplayName("setFrequency with 0 throws")
        void testSetFrequencyZeroThrows() {
            OrchestratorSettings settings = new OrchestratorSettings();

            assertThrows(IllegalArgumentException.class, () -> {
                settings.setFrequency(0);
            });
        }

        @Test
        @DisplayName("setFrequency with negative value throws")
        void testSetFrequencyNegativeThrows() {
            OrchestratorSettings settings = new OrchestratorSettings();

            assertThrows(IllegalArgumentException.class, () -> {
                settings.setFrequency(-5);
            });
        }
    }

    @Nested
    @DisplayName("setMode tests")
    class SetModeTests {

        @Test
        @DisplayName("setMode ON succeeds")
        void testSetModeOn() {
            OrchestratorSettings settings = new OrchestratorSettings();
            settings.setMode(SchedulerMode.OFF);

            settings.setMode(SchedulerMode.ON);

            assertEquals(SchedulerMode.ON, settings.getMode());
        }

        @Test
        @DisplayName("setMode OFF succeeds")
        void testSetModeOff() {
            OrchestratorSettings settings = new OrchestratorSettings();

            settings.setMode(SchedulerMode.OFF);

            assertEquals(SchedulerMode.OFF, settings.getMode());
        }

        @Test
        @DisplayName("setMode RANGES succeeds")
        void testSetModeRanges() {
            OrchestratorSettings settings = new OrchestratorSettings();

            settings.setMode(SchedulerMode.RANGES);

            assertEquals(SchedulerMode.RANGES, settings.getMode());
        }

        @Test
        @DisplayName("setMode null throws")
        void testSetModeNullThrows() {
            OrchestratorSettings settings = new OrchestratorSettings();

            assertThrows(IllegalArgumentException.class, () -> {
                settings.setMode(null);
            });
        }
    }

    @Nested
    @DisplayName("setRanges tests")
    class SetRangesTests {

        @Test
        @DisplayName("setRanges with collection succeeds")
        void testSetRangesCollection() {
            OrchestratorSettings settings = new OrchestratorSettings();
            OrchestratorTimeRangeCollection ranges = new OrchestratorTimeRangeCollection();

            settings.setRanges(ranges);

            assertSame(ranges, settings.getRanges());
        }

        @Test
        @DisplayName("setRanges with null collection throws")
        void testSetRangesNullCollectionThrows() {
            OrchestratorSettings settings = new OrchestratorSettings();

            assertThrows(IllegalArgumentException.class, () -> {
                settings.setRanges((OrchestratorTimeRangeCollection) null);
            });
        }

        @Test
        @DisplayName("setRanges with list succeeds")
        void testSetRangesList() {
            OrchestratorSettings settings = new OrchestratorSettings();
            List<OrchestratorTimeRange> rangesList = new ArrayList<>();

            settings.setRanges(rangesList);

            assertNotNull(settings.getRanges());
            assertEquals(0, settings.getRanges().getRanges().length);
        }

        @Test
        @DisplayName("setRanges with null list throws")
        void testSetRangesNullListThrows() {
            OrchestratorSettings settings = new OrchestratorSettings();

            assertThrows(IllegalArgumentException.class, () -> {
                settings.setRanges((List<OrchestratorTimeRange>) null);
            });
        }
    }

    @Nested
    @DisplayName("isValid tests")
    class IsValidTests {

        @Test
        @DisplayName("isValid returns true for default settings")
        void testIsValidDefault() {
            OrchestratorSettings settings = new OrchestratorSettings();

            assertTrue(settings.isValid());
        }

        @Test
        @DisplayName("isValid returns true for ON mode with any frequency")
        void testIsValidOnMode() {
            OrchestratorSettings settings = new OrchestratorSettings();
            settings.setFrequency(100);
            settings.setMode(SchedulerMode.ON);

            assertTrue(settings.isValid());
        }

        @Test
        @DisplayName("isValid returns true for OFF mode")
        void testIsValidOffMode() {
            OrchestratorSettings settings = new OrchestratorSettings();
            settings.setMode(SchedulerMode.OFF);

            assertTrue(settings.isValid());
        }

        @Test
        @DisplayName("isValid returns true for RANGES mode with empty ranges (empty collection is valid)")
        void testIsValidRangesModeEmptyRanges() {
            OrchestratorSettings settings = new OrchestratorSettings();
            settings.setMode(SchedulerMode.RANGES);
            settings.setRanges(new OrchestratorTimeRangeCollection());

            // An empty OrchestratorTimeRangeCollection is considered valid
            // (no invalid ranges means valid collection)
            assertTrue(settings.isValid());
        }
    }

    @Nested
    @DisplayName("isWorking tests")
    class IsWorkingTests {

        @Test
        @DisplayName("isWorking returns true for ON mode")
        void testIsWorkingOnMode() {
            OrchestratorSettings settings = new OrchestratorSettings();
            settings.setMode(SchedulerMode.ON);

            assertTrue(settings.isWorking());
        }

        @Test
        @DisplayName("isWorking returns false for OFF mode")
        void testIsWorkingOffMode() {
            OrchestratorSettings settings = new OrchestratorSettings();
            settings.setMode(SchedulerMode.OFF);

            assertFalse(settings.isWorking());
        }

        @Test
        @DisplayName("isWorking for RANGES mode delegates to isNowInRanges")
        void testIsWorkingRangesModeEmptyRanges() {
            OrchestratorSettings settings = new OrchestratorSettings();
            settings.setMode(SchedulerMode.RANGES);
            settings.setRanges(new OrchestratorTimeRangeCollection());

            // Empty ranges means not in any range
            assertFalse(settings.isWorking());
        }
    }

    @Nested
    @DisplayName("getStateString tests")
    class GetStateStringTests {

        @Test
        @DisplayName("getStateString returns RUNNING when working")
        void testGetStateStringRunning() {
            OrchestratorSettings settings = new OrchestratorSettings();
            settings.setMode(SchedulerMode.ON);

            assertEquals("RUNNING", settings.getStateString());
        }

        @Test
        @DisplayName("getStateString returns STOPPED for OFF mode")
        void testGetStateStringStopped() {
            OrchestratorSettings settings = new OrchestratorSettings();
            settings.setMode(SchedulerMode.OFF);

            assertEquals("STOPPED", settings.getStateString());
        }

        @Test
        @DisplayName("getStateString returns SCHEDULE_CONFIG_ERROR for RANGES with empty ranges")
        void testGetStateStringConfigError() {
            OrchestratorSettings settings = new OrchestratorSettings();
            settings.setMode(SchedulerMode.RANGES);
            settings.setRanges(new OrchestratorTimeRangeCollection());

            assertEquals("SCHEDULE_CONFIG_ERROR", settings.getStateString());
        }
    }

    @Nested
    @DisplayName("equals and hashCode tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("equals returns true for same settings")
        void testEqualsSameSettings() {
            List<OrchestratorTimeRange> rangesList = new ArrayList<>();
            OrchestratorSettings settings1 = new OrchestratorSettings(30, SchedulerMode.ON, rangesList);
            OrchestratorSettings settings2 = new OrchestratorSettings(30, SchedulerMode.ON, rangesList);

            assertEquals(settings1, settings2);
        }

        @Test
        @DisplayName("equals returns false for different frequency")
        void testEqualsDifferentFrequency() {
            List<OrchestratorTimeRange> rangesList = new ArrayList<>();
            OrchestratorSettings settings1 = new OrchestratorSettings(30, SchedulerMode.ON, rangesList);
            OrchestratorSettings settings2 = new OrchestratorSettings(60, SchedulerMode.ON, rangesList);

            assertNotEquals(settings1, settings2);
        }

        @Test
        @DisplayName("equals returns false for different mode")
        void testEqualsDifferentMode() {
            List<OrchestratorTimeRange> rangesList = new ArrayList<>();
            OrchestratorSettings settings1 = new OrchestratorSettings(30, SchedulerMode.ON, rangesList);
            OrchestratorSettings settings2 = new OrchestratorSettings(30, SchedulerMode.OFF, rangesList);

            assertNotEquals(settings1, settings2);
        }

        @Test
        @DisplayName("equals returns false for null")
        void testEqualsNull() {
            OrchestratorSettings settings = new OrchestratorSettings();

            assertNotEquals(null, settings);
        }

        @Test
        @DisplayName("equals returns false for different type")
        void testEqualsDifferentType() {
            OrchestratorSettings settings = new OrchestratorSettings();

            assertNotEquals("string", settings);
        }

        @Test
        @DisplayName("hashCode is consistent")
        void testHashCodeConsistent() {
            List<OrchestratorTimeRange> rangesList = new ArrayList<>();
            OrchestratorSettings settings1 = new OrchestratorSettings(30, SchedulerMode.ON, rangesList);
            OrchestratorSettings settings2 = new OrchestratorSettings(30, SchedulerMode.ON, rangesList);

            assertEquals(settings1.hashCode(), settings2.hashCode());
        }

        @Test
        @DisplayName("Different settings have different hashCodes")
        void testHashCodeDifferent() {
            List<OrchestratorTimeRange> rangesList = new ArrayList<>();
            OrchestratorSettings settings1 = new OrchestratorSettings(30, SchedulerMode.ON, rangesList);
            OrchestratorSettings settings2 = new OrchestratorSettings(60, SchedulerMode.OFF, rangesList);

            assertNotEquals(settings1.hashCode(), settings2.hashCode());
        }
    }

    @Nested
    @DisplayName("setValuesFromRepository tests")
    class SetValuesFromRepositoryTests {

        @Test
        @DisplayName("setValuesFromRepository loads all values")
        void testSetValuesFromRepositoryLoadsAllValues() {
            when(systemParametersRepository.getSchedulerFrequency()).thenReturn("45");
            when(systemParametersRepository.getSchedulerMode()).thenReturn("OFF");
            when(systemParametersRepository.getSchedulerRanges()).thenReturn("[]");

            OrchestratorSettings settings = new OrchestratorSettings();
            settings.setValuesFromRepository(systemParametersRepository);

            assertEquals(45, settings.getFrequency());
            assertEquals(SchedulerMode.OFF, settings.getMode());
        }

        @Test
        @DisplayName("setValuesFromRepository throws on null repository")
        void testSetValuesFromRepositoryNullThrows() {
            OrchestratorSettings settings = new OrchestratorSettings();

            assertThrows(IllegalArgumentException.class, () -> {
                settings.setValuesFromRepository(null);
            });
        }

        @Test
        @DisplayName("setValuesFromRepository with RANGES mode")
        void testSetValuesFromRepositoryRangesMode() {
            when(systemParametersRepository.getSchedulerFrequency()).thenReturn("30");
            when(systemParametersRepository.getSchedulerMode()).thenReturn("RANGES");
            when(systemParametersRepository.getSchedulerRanges()).thenReturn("[]");

            OrchestratorSettings settings = new OrchestratorSettings();
            settings.setValuesFromRepository(systemParametersRepository);

            assertEquals(30, settings.getFrequency());
            assertEquals(SchedulerMode.RANGES, settings.getMode());
            assertNotNull(settings.getRanges());
        }
    }

    @Nested
    @DisplayName("Edge cases and boundary tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Very large frequency value is accepted")
        void testLargeFrequency() {
            OrchestratorSettings settings = new OrchestratorSettings();

            settings.setFrequency(Integer.MAX_VALUE);

            assertEquals(Integer.MAX_VALUE, settings.getFrequency());
        }

        @Test
        @DisplayName("Minimum valid frequency (1) is accepted")
        void testMinimumFrequency() {
            OrchestratorSettings settings = new OrchestratorSettings();

            settings.setFrequency(1);

            assertEquals(1, settings.getFrequency());
        }

        @Test
        @DisplayName("All scheduler modes can be set and retrieved")
        void testAllSchedulerModes() {
            OrchestratorSettings settings = new OrchestratorSettings();

            for (SchedulerMode mode : SchedulerMode.values()) {
                settings.setMode(mode);
                assertEquals(mode, settings.getMode());
            }
        }

        @Test
        @DisplayName("Multiple setRanges calls replace previous value")
        void testSetRangesReplacesValue() {
            OrchestratorSettings settings = new OrchestratorSettings();
            OrchestratorTimeRangeCollection ranges1 = new OrchestratorTimeRangeCollection();
            OrchestratorTimeRangeCollection ranges2 = new OrchestratorTimeRangeCollection();

            settings.setRanges(ranges1);
            assertSame(ranges1, settings.getRanges());

            settings.setRanges(ranges2);
            assertSame(ranges2, settings.getRanges());
        }
    }

    @Nested
    @DisplayName("SchedulerMode enum tests")
    class SchedulerModeEnumTests {

        @Test
        @DisplayName("SchedulerMode has exactly 3 values")
        void testSchedulerModeCount() {
            assertEquals(3, SchedulerMode.values().length);
        }

        @Test
        @DisplayName("SchedulerMode values are ON, RANGES, OFF")
        void testSchedulerModeValues() {
            SchedulerMode[] modes = SchedulerMode.values();

            assertTrue(containsMode(modes, SchedulerMode.ON));
            assertTrue(containsMode(modes, SchedulerMode.RANGES));
            assertTrue(containsMode(modes, SchedulerMode.OFF));
        }

        @Test
        @DisplayName("SchedulerMode valueOf works correctly")
        void testSchedulerModeValueOf() {
            assertEquals(SchedulerMode.ON, SchedulerMode.valueOf("ON"));
            assertEquals(SchedulerMode.OFF, SchedulerMode.valueOf("OFF"));
            assertEquals(SchedulerMode.RANGES, SchedulerMode.valueOf("RANGES"));
        }

        private boolean containsMode(SchedulerMode[] modes, SchedulerMode target) {
            for (SchedulerMode mode : modes) {
                if (mode == target) {
                    return true;
                }
            }
            return false;
        }
    }
}
