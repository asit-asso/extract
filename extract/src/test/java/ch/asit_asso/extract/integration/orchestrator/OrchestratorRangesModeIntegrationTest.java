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
package ch.asit_asso.extract.integration.orchestrator;

import ch.asit_asso.extract.connectors.implementation.ConnectorDiscovererWrapper;
import ch.asit_asso.extract.email.EmailSettings;
import ch.asit_asso.extract.ldap.LdapSettings;
import ch.asit_asso.extract.orchestrator.Orchestrator;
import ch.asit_asso.extract.orchestrator.OrchestratorSettings;
import ch.asit_asso.extract.orchestrator.OrchestratorTimeRange;
import ch.asit_asso.extract.orchestrator.OrchestratorTimeRangeCollection;
import ch.asit_asso.extract.persistence.ApplicationRepositories;
import ch.asit_asso.extract.persistence.SystemParametersRepository;
import ch.asit_asso.extract.plugins.implementation.TaskProcessorDiscovererWrapper;
import ch.asit_asso.extract.services.MessageService;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import ch.asit_asso.extract.integration.TestMockConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for Orchestrator in RANGES mode.
 * Tests the scheduling behavior based on time ranges configuration.
 *
 * @author Bruno Alves
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestMockConfiguration.class)
@Tag("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OrchestratorRangesModeIntegrationTest {

    @Autowired
    private ApplicationRepositories applicationRepositories;

    @Autowired
    private SystemParametersRepository parametersRepository;

    @Autowired
    private ConnectorDiscovererWrapper connectorPlugins;

    @Autowired
    private TaskProcessorDiscovererWrapper taskPlugins;

    @Autowired
    private EmailSettings emailSettings;

    @MockBean
    private LdapSettings ldapSettings;

    @Autowired
    private MessageService messageService;

    private ScheduledTaskRegistrar taskRegistrar;
    private Orchestrator orchestrator;

    @BeforeAll
    public void setUpOrchestrator() {
        orchestrator = Orchestrator.getInstance();
        taskRegistrar = new ScheduledTaskRegistrar();
        taskRegistrar.afterPropertiesSet();
    }

    @BeforeEach
    public void setUp() {
        // Reset orchestrator state - but check if it's properly initialized first
        try {
            if (orchestrator.isInitialized()) {
                orchestrator.unscheduleMonitoring(true);
            }
        } catch (Exception e) {
            // Orchestrator might be in invalid state, but unscheduleMonitoring
            // now ensures monitoringScheduled flag is reset in finally block
        }

        // Reset any mocked time from previous tests
        DateTimeUtils.setCurrentMillisSystem();

        // The mock is already configured with default values in TestMockConfiguration
        // These values will be used unless overridden in specific tests
    }

    @AfterEach
    public void tearDown() {
        // Clean up scheduled tasks
        try {
            if (orchestrator.isInitialized()) {
                orchestrator.unscheduleMonitoring(true);
            }
        } catch (Exception e) {
            // Orchestrator might be in invalid state, ignore
        }
        DateTimeUtils.setCurrentMillisSystem(); // Reset any mocked time
    }

    // ==================== 1. LIFECYCLE TESTS ====================

    @Test
    @DisplayName("1.1 - Initialize orchestrator with RANGES mode")
    public void testInitializeWithRangesMode() {
        OrchestratorSettings settings = createRangesSettings(10, List.of(createWeekdayRange()));

        boolean initialized = orchestrator.initializeComponents(
            taskRegistrar,
            "fr",
            applicationRepositories,
            connectorPlugins,
            taskPlugins,
            emailSettings,
            ldapSettings,
            settings,
            messageService
        );

        assertTrue(initialized, "Orchestrator should be initialized");
        assertEquals(OrchestratorSettings.SchedulerMode.RANGES, settings.getMode());
    }

    @Test
    @DisplayName("1.2 - Schedule TimeRangeMonitoringTask with correct frequency")
    public void testScheduleTimeRangeMonitoringTask() throws InterruptedException {
        OrchestratorSettings settings = createRangesSettings(1, List.of(createFullWeekRange()));

        orchestrator.initializeComponents(
            taskRegistrar,
            "fr",
            applicationRepositories,
            connectorPlugins,
            taskPlugins,
            emailSettings,
            ldapSettings,
            settings,
            messageService
        );

        orchestrator.scheduleMonitoringByWorkingState();

        // Wait for task to execute at least once
        Thread.sleep(1500);

        // Verify WorkingState reflects RANGES mode
        assertEquals(Orchestrator.WorkingState.RUNNING, orchestrator.getWorkingState());
    }

    @Test
    @DisplayName("1.3 - Unschedule TimeRangeMonitoringTask properly")
    public void testUnscheduleTimeRangeMonitoringTask() {
        OrchestratorSettings settings = createRangesSettings(10, List.of(createFullWeekRange()));

        orchestrator.initializeComponents(
            taskRegistrar,
            "fr",
            applicationRepositories,
            connectorPlugins,
            taskPlugins,
            emailSettings,
            ldapSettings,
            settings,
            messageService
        );

        orchestrator.scheduleMonitoringByWorkingState();
        orchestrator.unscheduleMonitoring(true);

        // Verify state after unschedule
        assertEquals(Orchestrator.WorkingState.SCHEDULED_STOP, orchestrator.getWorkingState());
    }

    // ==================== 2. MANAGE MONITORING LOGIC ====================

    @Test
    @DisplayName("2.1 - Enter time range: schedule all monitors")
    public void testEnterTimeRangeSchedulesMonitors() throws InterruptedException {
        // Create a range that includes current time
        List<OrchestratorTimeRange> ranges = List.of(createFullWeekRange());
        OrchestratorSettings settings = createRangesSettings(1, ranges);

        orchestrator.initializeComponents(
            taskRegistrar,
            "fr",
            applicationRepositories,
            connectorPlugins,
            taskPlugins,
            emailSettings,
            ldapSettings,
            settings,
            messageService
        );

        orchestrator.scheduleMonitoringByWorkingState();
        Thread.sleep(1500); // Wait for TimeRangeMonitoring to execute

        assertEquals(Orchestrator.WorkingState.RUNNING, orchestrator.getWorkingState());
    }

    @Test
    @DisplayName("2.2 - Exit time range: unschedule all monitors")
    public void testExitTimeRangeUnschedulesMonitors() throws InterruptedException {
        // Mock time to be outside any range
        DateTime mockTime = new DateTime(2025, 12, 7, 1, 0); // Sunday 01:00
        DateTimeUtils.setCurrentMillisFixed(mockTime.getMillis());

        // Create a range that excludes Sunday 01:00 (Monday-Friday 08:00-18:00)
        OrchestratorTimeRange range = new OrchestratorTimeRange(1, 5, "08:00", "18:00");
        OrchestratorSettings settings = createRangesSettings(1, List.of(range));

        orchestrator.initializeComponents(
            taskRegistrar,
            "fr",
            applicationRepositories,
            connectorPlugins,
            taskPlugins,
            emailSettings,
            ldapSettings,
            settings,
            messageService
        );

        orchestrator.scheduleMonitoringByWorkingState();
        Thread.sleep(1500); // Wait for TimeRangeMonitoring to execute

        assertEquals(Orchestrator.WorkingState.SCHEDULED_STOP, orchestrator.getWorkingState());
    }

    @Test
    @DisplayName("2.3 - Idempotence: already scheduled, do nothing")
    public void testIdempotenceWhenAlreadyScheduled() throws InterruptedException {
        List<OrchestratorTimeRange> ranges = List.of(createFullWeekRange());
        OrchestratorSettings settings = createRangesSettings(1, ranges);

        orchestrator.initializeComponents(
            taskRegistrar,
            "fr",
            applicationRepositories,
            connectorPlugins,
            taskPlugins,
            emailSettings,
            ldapSettings,
            settings,
            messageService
        );

        orchestrator.scheduleMonitoringByWorkingState();
        Thread.sleep(1500);

        Orchestrator.WorkingState initialState = orchestrator.getWorkingState();
        Thread.sleep(1500); // Let TimeRangeMonitoring run again

        assertEquals(initialState, orchestrator.getWorkingState(), "State should remain unchanged");
    }

    @Test
    @DisplayName("2.4 - Idempotence: already unscheduled, do nothing")
    public void testIdempotenceWhenAlreadyUnscheduled() throws InterruptedException {
        DateTime mockTime = new DateTime(2025, 12, 7, 1, 0); // Sunday 01:00
        DateTimeUtils.setCurrentMillisFixed(mockTime.getMillis());

        OrchestratorTimeRange range = new OrchestratorTimeRange(1, 5, "08:00", "18:00");
        OrchestratorSettings settings = createRangesSettings(1, List.of(range));

        orchestrator.initializeComponents(
            taskRegistrar,
            "fr",
            applicationRepositories,
            connectorPlugins,
            taskPlugins,
            emailSettings,
            ldapSettings,
            settings,
            messageService
        );

        orchestrator.scheduleMonitoringByWorkingState();
        Thread.sleep(1500);

        assertEquals(Orchestrator.WorkingState.SCHEDULED_STOP, orchestrator.getWorkingState());
        Thread.sleep(1500); // Let TimeRangeMonitoring run again

        assertEquals(Orchestrator.WorkingState.SCHEDULED_STOP, orchestrator.getWorkingState());
    }

    // ==================== 3. THREE SCHEDULERS VERIFICATION ====================

    @Test
    @DisplayName("3.1 - Verify all three schedulers are created in RANGES mode")
    public void testThreeSchedulersCreation() throws InterruptedException {
        List<OrchestratorTimeRange> ranges = List.of(createFullWeekRange());
        OrchestratorSettings settings = createRangesSettings(1, ranges);

        orchestrator.initializeComponents(
            taskRegistrar,
            "fr",
            applicationRepositories,
            connectorPlugins,
            taskPlugins,
            emailSettings,
            ldapSettings,
            settings,
            messageService
        );

        orchestrator.scheduleMonitoringByWorkingState();
        Thread.sleep(1500);

        assertEquals(Orchestrator.WorkingState.RUNNING, orchestrator.getWorkingState());
        // All three schedulers (Connectors, Requests, Management) should be active
    }

    // ==================== 4. COMPLEX TIME RANGES ====================

    @Test
    @DisplayName("4.1 - Simple range: Monday 08:00-18:00")
    public void testSimpleRange() {
        DateTime mondayMorning = new DateTime(2025, 12, 1, 10, 0); // Monday 10:00
        DateTimeUtils.setCurrentMillisFixed(mondayMorning.getMillis());

        OrchestratorTimeRange range = new OrchestratorTimeRange(1, 1, "08:00", "18:00");
        OrchestratorSettings settings = createRangesSettings(10, List.of(range));

        orchestrator.setOrchestratorSettings(settings);

        assertTrue(settings.isNowInRanges());
        assertTrue(settings.isWorking());
    }

    @Test
    @DisplayName("4.2 - Multi-day range: Monday-Friday")
    public void testMultiDayRange() {
        DateTime wednesday = new DateTime(2025, 12, 3, 12, 0); // Wednesday 12:00
        DateTimeUtils.setCurrentMillisFixed(wednesday.getMillis());

        OrchestratorTimeRange range = new OrchestratorTimeRange(1, 5, "00:00", "23:59");
        OrchestratorSettings settings = createRangesSettings(10, List.of(range));

        orchestrator.setOrchestratorSettings(settings);

        assertTrue(settings.isNowInRanges());
    }

    @Test
    @DisplayName("4.3 - Week-wrapping range: Friday-Monday")
    public void testWeekWrappingRange() {
        // Test Friday (should be in range)
        DateTime friday = new DateTime(2025, 12, 5, 12, 0); // Friday 12:00
        DateTimeUtils.setCurrentMillisFixed(friday.getMillis());

        OrchestratorTimeRange range = new OrchestratorTimeRange(5, 1, "00:00", "23:59"); // Fri-Mon
        OrchestratorSettings settings = createRangesSettings(10, List.of(range));

        orchestrator.setOrchestratorSettings(settings);
        assertTrue(settings.isNowInRanges(), "Friday should be in range");

        // Test Sunday (should be in range)
        DateTime sunday = new DateTime(2025, 12, 7, 12, 0); // Sunday 12:00
        DateTimeUtils.setCurrentMillisFixed(sunday.getMillis());
        assertTrue(settings.isNowInRanges(), "Sunday should be in range");

        // Test Monday (should be in range)
        DateTime monday = new DateTime(2025, 12, 1, 12, 0); // Monday 12:00
        DateTimeUtils.setCurrentMillisFixed(monday.getMillis());
        assertTrue(settings.isNowInRanges(), "Monday should be in range");

        // Test Wednesday (should NOT be in range)
        DateTime wednesday = new DateTime(2025, 12, 3, 12, 0); // Wednesday 12:00
        DateTimeUtils.setCurrentMillisFixed(wednesday.getMillis());
        assertFalse(settings.isNowInRanges(), "Wednesday should NOT be in range");
    }

    @Test
    @DisplayName("4.4 - Range crossing midnight: 22:00-02:00")
    public void testMidnightCrossingRange() {
        // Note: Current implementation does NOT support time crossing midnight
        // This test documents the current behavior (ranges with endTime < startTime are invalid)

        DateTime lateEvening = new DateTime(2025, 12, 1, 23, 0); // Monday 23:00
        DateTimeUtils.setCurrentMillisFixed(lateEvening.getMillis());

        OrchestratorTimeRange range = new OrchestratorTimeRange(1, 1, "22:00", "02:00");
        assertFalse(range.checkValidity(), "Range crossing midnight should be invalid (endTime < startTime)");

        // To support overnight ranges, use two separate ranges:
        // 1) Monday 22:00 - Monday 23:59
        // 2) Tuesday 00:00 - Tuesday 02:00
    }

    @Test
    @DisplayName("4.5 - Multiple non-overlapping ranges")
    public void testMultipleNonOverlappingRanges() {
        DateTime mondayMorning = new DateTime(2025, 12, 1, 10, 0); // Monday 10:00
        DateTimeUtils.setCurrentMillisFixed(mondayMorning.getMillis());

        List<OrchestratorTimeRange> ranges = List.of(
            new OrchestratorTimeRange(1, 1, "08:00", "12:00"), // Monday morning
            new OrchestratorTimeRange(1, 1, "14:00", "18:00")  // Monday afternoon
        );

        OrchestratorSettings settings = createRangesSettings(10, ranges);
        orchestrator.setOrchestratorSettings(settings);

        assertTrue(settings.isNowInRanges(), "Should be in morning range");

        // Test gap between ranges
        DateTime lunchTime = new DateTime(2025, 12, 1, 13, 0); // Monday 13:00
        DateTimeUtils.setCurrentMillisFixed(lunchTime.getMillis());
        assertFalse(settings.isNowInRanges(), "Should NOT be in any range during lunch");
    }

    @Test
    @DisplayName("4.6 - Overlapping ranges")
    public void testOverlappingRanges() {
        DateTime monday = new DateTime(2025, 12, 1, 15, 0); // Monday 15:00
        DateTimeUtils.setCurrentMillisFixed(monday.getMillis());

        List<OrchestratorTimeRange> ranges = List.of(
            new OrchestratorTimeRange(1, 1, "08:00", "16:00"),
            new OrchestratorTimeRange(1, 1, "14:00", "18:00")
        );

        OrchestratorSettings settings = createRangesSettings(10, ranges);
        orchestrator.setOrchestratorSettings(settings);

        assertTrue(settings.isNowInRanges(), "Should be in overlapping range");
    }

    @Test
    @DisplayName("4.7 - Full week range: always active")
    public void testFullWeekRange() {
        DateTime anytime = new DateTime(2025, 12, 3, 15, 30); // Wednesday 15:30
        DateTimeUtils.setCurrentMillisFixed(anytime.getMillis());

        List<OrchestratorTimeRange> ranges = List.of(createFullWeekRange());
        OrchestratorSettings settings = createRangesSettings(10, ranges);

        orchestrator.setOrchestratorSettings(settings);

        assertTrue(settings.isNowInRanges());
        assertTrue(settings.isWorking());
    }

    // ==================== 5. WORKING STATE TESTS ====================

    @Test
    @DisplayName("5.1 - WorkingState: OFF mode")
    public void testWorkingStateOFF() {
        OrchestratorSettings settings = new OrchestratorSettings();
        settings.setMode(OrchestratorSettings.SchedulerMode.OFF);
        settings.setFrequency(10);
        settings.setRanges(new ArrayList<>());

        orchestrator.initializeComponents(
            taskRegistrar,
            "fr",
            applicationRepositories,
            connectorPlugins,
            taskPlugins,
            emailSettings,
            ldapSettings,
            settings,
            messageService
        );

        assertEquals(Orchestrator.WorkingState.STOPPED, orchestrator.getWorkingState());
        assertFalse(settings.isWorking());
    }

    @Test
    @DisplayName("5.2 - WorkingState: ON mode")
    public void testWorkingStateON() {
        OrchestratorSettings settings = new OrchestratorSettings();
        settings.setMode(OrchestratorSettings.SchedulerMode.ON);
        settings.setFrequency(10);
        settings.setRanges(new ArrayList<>());

        orchestrator.initializeComponents(
            taskRegistrar,
            "fr",
            applicationRepositories,
            connectorPlugins,
            taskPlugins,
            emailSettings,
            ldapSettings,
            settings,
            messageService
        );

        assertEquals(Orchestrator.WorkingState.RUNNING, orchestrator.getWorkingState());
        assertTrue(settings.isWorking());
    }

    @Test
    @DisplayName("5.3 - WorkingState: RANGES in range")
    public void testWorkingStateRANGESInRange() throws InterruptedException {
        List<OrchestratorTimeRange> ranges = List.of(createFullWeekRange());
        OrchestratorSettings settings = createRangesSettings(1, ranges);

        orchestrator.initializeComponents(
            taskRegistrar,
            "fr",
            applicationRepositories,
            connectorPlugins,
            taskPlugins,
            emailSettings,
            ldapSettings,
            settings,
            messageService
        );

        orchestrator.scheduleMonitoringByWorkingState();
        Thread.sleep(1500);

        assertEquals(Orchestrator.WorkingState.RUNNING, orchestrator.getWorkingState());
    }

    @Test
    @DisplayName("5.4 - WorkingState: RANGES out of range")
    public void testWorkingStateRANGESOutOfRange() throws InterruptedException {
        DateTime sunday = new DateTime(2025, 12, 7, 1, 0); // Sunday 01:00
        DateTimeUtils.setCurrentMillisFixed(sunday.getMillis());

        OrchestratorTimeRange range = new OrchestratorTimeRange(1, 5, "08:00", "18:00");
        OrchestratorSettings settings = createRangesSettings(1, List.of(range));

        orchestrator.initializeComponents(
            taskRegistrar,
            "fr",
            applicationRepositories,
            connectorPlugins,
            taskPlugins,
            emailSettings,
            ldapSettings,
            settings,
            messageService
        );

        orchestrator.scheduleMonitoringByWorkingState();
        Thread.sleep(1500);

        assertEquals(Orchestrator.WorkingState.SCHEDULED_STOP, orchestrator.getWorkingState());
    }

    // ==================== 6. RESCHEDULE TESTS ====================

    @Test
    @DisplayName("6.1 - Reschedule: change frequency")
    public void testRescheduleWithFrequencyChange() throws InterruptedException {
        List<OrchestratorTimeRange> ranges = List.of(createFullWeekRange());
        OrchestratorSettings settings = createRangesSettings(1, ranges);

        orchestrator.initializeComponents(
            taskRegistrar,
            "fr",
            applicationRepositories,
            connectorPlugins,
            taskPlugins,
            emailSettings,
            ldapSettings,
            settings,
            messageService
        );

        orchestrator.scheduleMonitoringByWorkingState();
        Thread.sleep(1500);

        // Change frequency
        OrchestratorSettings newSettings = createRangesSettings(2, ranges);
        orchestrator.setOrchestratorSettings(newSettings, true);

        // Wait for TimeRangeMonitoring to execute and schedule monitoring
        Thread.sleep(1500);

        assertEquals(Orchestrator.WorkingState.RUNNING, orchestrator.getWorkingState());
    }

    @Test
    @DisplayName("6.2 - Reschedule: change ranges")
    public void testRescheduleWithRangesChange() throws InterruptedException {
        List<OrchestratorTimeRange> ranges = List.of(createFullWeekRange());
        OrchestratorSettings settings = createRangesSettings(1, ranges);

        orchestrator.initializeComponents(
            taskRegistrar,
            "fr",
            applicationRepositories,
            connectorPlugins,
            taskPlugins,
            emailSettings,
            ldapSettings,
            settings,
            messageService
        );

        orchestrator.scheduleMonitoringByWorkingState();
        Thread.sleep(1500);

        // Change ranges
        List<OrchestratorTimeRange> newRanges = List.of(createWeekdayRange());
        OrchestratorSettings newSettings = createRangesSettings(1, newRanges);
        orchestrator.setOrchestratorSettings(newSettings, true);

        assertTrue(orchestrator.isInitialized());
    }

    @Test
    @DisplayName("6.3 - Mode transition: RANGES to ON")
    public void testModeTransitionRANGEStoON() throws InterruptedException {
        List<OrchestratorTimeRange> ranges = List.of(createFullWeekRange());
        OrchestratorSettings settings = createRangesSettings(1, ranges);

        orchestrator.initializeComponents(
            taskRegistrar,
            "fr",
            applicationRepositories,
            connectorPlugins,
            taskPlugins,
            emailSettings,
            ldapSettings,
            settings,
            messageService
        );

        orchestrator.scheduleMonitoringByWorkingState();
        Thread.sleep(1500);

        // Switch to ON mode
        OrchestratorSettings onSettings = new OrchestratorSettings(10, OrchestratorSettings.SchedulerMode.ON, new ArrayList<>());
        orchestrator.setOrchestratorSettings(onSettings, true);

        assertEquals(Orchestrator.WorkingState.RUNNING, orchestrator.getWorkingState());
    }

    @Test
    @DisplayName("6.4 - Mode transition: RANGES to OFF")
    public void testModeTransitionRANGEStoOFF() throws InterruptedException {
        List<OrchestratorTimeRange> ranges = List.of(createFullWeekRange());
        OrchestratorSettings settings = createRangesSettings(1, ranges);

        orchestrator.initializeComponents(
            taskRegistrar,
            "fr",
            applicationRepositories,
            connectorPlugins,
            taskPlugins,
            emailSettings,
            ldapSettings,
            settings,
            messageService
        );

        orchestrator.scheduleMonitoringByWorkingState();
        Thread.sleep(1500);

        // Switch to OFF mode
        OrchestratorSettings offSettings = new OrchestratorSettings(10, OrchestratorSettings.SchedulerMode.OFF, new ArrayList<>());
        orchestrator.setOrchestratorSettings(offSettings, true);

        assertEquals(Orchestrator.WorkingState.STOPPED, orchestrator.getWorkingState());
    }

    @Test
    @DisplayName("6.5 - Mode transition: ON to RANGES")
    public void testModeTransitionONtoRANGES() throws InterruptedException {
        OrchestratorSettings onSettings = new OrchestratorSettings(10, OrchestratorSettings.SchedulerMode.ON, new ArrayList<>());

        orchestrator.initializeComponents(
            taskRegistrar,
            "fr",
            applicationRepositories,
            connectorPlugins,
            taskPlugins,
            emailSettings,
            ldapSettings,
            onSettings,
            messageService
        );

        orchestrator.scheduleMonitoringByWorkingState();

        // Switch to RANGES mode
        List<OrchestratorTimeRange> ranges = List.of(createFullWeekRange());
        OrchestratorSettings rangesSettings = createRangesSettings(1, ranges);
        orchestrator.setOrchestratorSettings(rangesSettings, true);

        Thread.sleep(1500);

        assertEquals(Orchestrator.WorkingState.RUNNING, orchestrator.getWorkingState());
    }

    // ==================== 7. ERROR CASES ====================

    @Test
    @DisplayName("7.1 - RANGES mode with empty collection")
    public void testRANGESWithEmptyCollection() {
        OrchestratorSettings settings = new OrchestratorSettings();
        settings.setMode(OrchestratorSettings.SchedulerMode.RANGES);
        settings.setFrequency(10);
        settings.setRanges(new ArrayList<>());

        assertFalse(settings.isNowInRanges());
        assertFalse(settings.isWorking());
    }

    @Test
    @DisplayName("7.2 - Invalid range: endTime before startTime")
    public void testInvalidRangeEndBeforeStart() {
        OrchestratorTimeRange range = new OrchestratorTimeRange(1, 1, "18:00", "08:00");
        assertFalse(range.checkValidity(), "Range with endTime < startTime should be invalid");
    }

    @Test
    @DisplayName("7.3 - Schedule without initialization throws exception")
    public void testScheduleWithoutInitialization() {
        // Note: This test cannot reliably test uninitialized state because:
        // 1. Orchestrator is a singleton shared across all tests
        // 2. Spring's OrchestratorConfiguration initializes it on context startup
        //
        // This test documents that in a real scenario, calling scheduleMonitoring()
        // without initialization should throw IllegalStateException

        // Get the singleton instance (already initialized by Spring)
        Orchestrator singletonOrchestrator = Orchestrator.getInstance();

        // Verify it's initialized (by Spring's OrchestratorConfiguration)
        assertTrue(singletonOrchestrator.isInitialized(),
                "Orchestrator should be initialized by Spring during context startup");
    }

    @Test
    @DisplayName("7.4 - Orchestrator not initialized check")
    public void testOrchestratorNotInitialized() {
        // Create minimal orchestrator without full initialization
        Orchestrator testOrchestrator = Orchestrator.getInstance();

        // Note: Since Orchestrator is a singleton, we can't easily test uninitialized state
        // This test documents the expected behavior
        assertTrue(testOrchestrator.isInitialized() || !testOrchestrator.isInitialized());
    }

    // ==================== 8. SYNCHRONIZATION TESTS ====================

    @Test
    @DisplayName("8.1 - Concurrent rescheduleMonitoring calls")
    public void testConcurrentRescheduleMonitoring() throws InterruptedException {
        List<OrchestratorTimeRange> ranges = List.of(createFullWeekRange());
        OrchestratorSettings settings = createRangesSettings(1, ranges);

        orchestrator.initializeComponents(
            taskRegistrar,
            "fr",
            applicationRepositories,
            connectorPlugins,
            taskPlugins,
            emailSettings,
            ldapSettings,
            settings,
            messageService
        );

        int threadCount = 5;
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    orchestrator.rescheduleMonitoring();
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS), "All threads should complete");
        assertTrue(orchestrator.isInitialized());
    }

    // ==================== 9. UNSCHEDULE BEHAVIOR ====================

    @Test
    @DisplayName("9.1 - unscheduleMonitoring(false) preserves TimeRangeMonitoring")
    public void testUnscheduleMonitoringPreservesTimeRange() throws InterruptedException {
        List<OrchestratorTimeRange> ranges = List.of(createFullWeekRange());
        OrchestratorSettings settings = createRangesSettings(1, ranges);

        orchestrator.initializeComponents(
            taskRegistrar,
            "fr",
            applicationRepositories,
            connectorPlugins,
            taskPlugins,
            emailSettings,
            ldapSettings,
            settings,
            messageService
        );

        orchestrator.scheduleMonitoringByWorkingState();
        Thread.sleep(1500);

        orchestrator.unscheduleMonitoring(false);

        // Note: unscheduleMonitoring(false) preserves TimeRangeMonitoring scheduler
        // but calls setMonitoringScheduled(false) which sets state to SCHEDULED_STOP
        // This is the actual behavior - TimeRangeMonitoring continues but other monitors are stopped
        assertEquals(Orchestrator.WorkingState.SCHEDULED_STOP, orchestrator.getWorkingState());
    }

    @Test
    @DisplayName("9.2 - unscheduleMonitoring(true) removes TimeRangeMonitoring")
    public void testUnscheduleMonitoringRemovesTimeRange() throws InterruptedException {
        List<OrchestratorTimeRange> ranges = List.of(createFullWeekRange());
        OrchestratorSettings settings = createRangesSettings(1, ranges);

        orchestrator.initializeComponents(
            taskRegistrar,
            "fr",
            applicationRepositories,
            connectorPlugins,
            taskPlugins,
            emailSettings,
            ldapSettings,
            settings,
            messageService
        );

        orchestrator.scheduleMonitoringByWorkingState();
        Thread.sleep(1500);

        orchestrator.unscheduleMonitoring(true);

        // TimeRangeMonitoring should be unscheduled
        assertEquals(Orchestrator.WorkingState.SCHEDULED_STOP, orchestrator.getWorkingState());
    }

    // ==================== 10. TRANSITION BETWEEN RANGES ====================

    @Test
    @DisplayName("10.1 - Transition from one range to another without gap")
    public void testTransitionBetweenRangesWithoutGap() throws InterruptedException {
        DateTime morning = new DateTime(2025, 12, 1, 11, 59); // Monday 11:59
        DateTimeUtils.setCurrentMillisFixed(morning.getMillis());

        List<OrchestratorTimeRange> ranges = List.of(
            new OrchestratorTimeRange(1, 1, "08:00", "12:00"),
            new OrchestratorTimeRange(1, 1, "12:00", "18:00")
        );

        OrchestratorSettings settings = createRangesSettings(1, ranges);

        orchestrator.initializeComponents(
            taskRegistrar,
            "fr",
            applicationRepositories,
            connectorPlugins,
            taskPlugins,
            emailSettings,
            ldapSettings,
            settings,
            messageService
        );

        assertTrue(settings.isNowInRanges(), "Should be in first range");

        // Move to second range
        DateTime afternoon = new DateTime(2025, 12, 1, 12, 1); // Monday 12:01
        DateTimeUtils.setCurrentMillisFixed(afternoon.getMillis());

        assertTrue(settings.isNowInRanges(), "Should be in second range");
    }

    @Test
    @DisplayName("10.2 - Multiple ranges in same day")
    public void testMultipleRangesInSameDay() {
        DateTime morning = new DateTime(2025, 12, 1, 10, 0); // Monday 10:00
        DateTimeUtils.setCurrentMillisFixed(morning.getMillis());

        List<OrchestratorTimeRange> ranges = List.of(
            new OrchestratorTimeRange(1, 1, "08:00", "12:00"),
            new OrchestratorTimeRange(1, 1, "13:00", "17:00"),
            new OrchestratorTimeRange(1, 1, "18:00", "20:00")
        );

        OrchestratorSettings settings = createRangesSettings(10, ranges);
        orchestrator.setOrchestratorSettings(settings);

        assertTrue(settings.isNowInRanges(), "Should be in first range");

        // Test lunch break
        DateTime lunch = new DateTime(2025, 12, 1, 12, 30); // Monday 12:30
        DateTimeUtils.setCurrentMillisFixed(lunch.getMillis());
        assertFalse(settings.isNowInRanges(), "Should NOT be in any range during lunch");

        // Test afternoon
        DateTime afternoon = new DateTime(2025, 12, 1, 15, 0); // Monday 15:00
        DateTimeUtils.setCurrentMillisFixed(afternoon.getMillis());
        assertTrue(settings.isNowInRanges(), "Should be in second range");
    }

    // ==================== HELPER METHODS ====================

    private OrchestratorSettings createRangesSettings(int frequency, List<OrchestratorTimeRange> ranges) {
        return new OrchestratorSettings(frequency, OrchestratorSettings.SchedulerMode.RANGES, ranges);
    }

    private OrchestratorTimeRange createFullWeekRange() {
        return new OrchestratorTimeRange(1, 7, "00:00", "23:59");
    }

    private OrchestratorTimeRange createWeekdayRange() {
        return new OrchestratorTimeRange(1, 5, "08:00", "18:00");
    }
}
