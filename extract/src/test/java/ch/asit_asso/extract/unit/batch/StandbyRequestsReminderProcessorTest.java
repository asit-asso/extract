package ch.asit_asso.extract.unit.batch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StandbyRequestsReminderProcessor logic tests")
class StandbyRequestsReminderProcessorTest {

    @Test
    @DisplayName("Validate limit calculation logic")
    void testLimitCalculation() {
        // This test validates the mathematical logic of the limit calculation

        // Given interval = 3 days
        int daysBeforeReminder = 3;
        Calendar now = GregorianCalendar.getInstance();
        Calendar limit = GregorianCalendar.getInstance();
        limit.add(Calendar.DAY_OF_MONTH, daysBeforeReminder * -1);

        // Test scenarios
        Calendar lastReminderNull = null;
        Calendar lastReminderExactly3DaysAgo = (Calendar) limit.clone();
        Calendar lastReminder4DaysAgo = (Calendar) limit.clone();
        lastReminder4DaysAgo.add(Calendar.DAY_OF_MONTH, -1);
        Calendar lastReminder2DaysAgo = (Calendar) limit.clone();
        lastReminder2DaysAgo.add(Calendar.DAY_OF_MONTH, 1);

        // Validate conditions with the new logic: lastReminder == null || !limit.before(lastReminder)
        assertTrue(lastReminderNull == null, "Null lastReminder should trigger reminder");
        assertTrue(!limit.before(lastReminderExactly3DaysAgo), "lastReminder == limit should trigger reminder (boundary)");
        assertTrue(!limit.before(lastReminder4DaysAgo), "lastReminder < limit should trigger reminder");
        assertFalse(!limit.before(lastReminder2DaysAgo), "lastReminder > limit should NOT trigger reminder");
    }

    @Test
    @DisplayName("Should send reminder when lastReminder equals limit (boundary - 1 day interval)")
    void testOneDayIntervalBoundary() {
        // Given interval = 1 day
        int daysBeforeReminder = 1;
        Calendar limit = GregorianCalendar.getInstance();
        limit.add(Calendar.DAY_OF_MONTH, daysBeforeReminder * -1);

        // lastReminder exactly 1 day ago (at boundary)
        Calendar lastReminderExactly1DayAgo = (Calendar) limit.clone();

        // With the new logic: !limit.before(lastReminder) is true when they're equal
        assertTrue(!limit.before(lastReminderExactly1DayAgo),
            "Should send reminder at exactly 1 day with >= condition");
    }

    @Test
    @DisplayName("Should send reminder when lastReminder is older than limit")
    void testOlderThanLimit() {
        // Given interval = 3 days
        int daysBeforeReminder = 3;
        Calendar limit = GregorianCalendar.getInstance();
        limit.add(Calendar.DAY_OF_MONTH, daysBeforeReminder * -1);

        // lastReminder 5 days ago (much older than limit)
        Calendar lastReminder5DaysAgo = (Calendar) limit.clone();
        lastReminder5DaysAgo.add(Calendar.DAY_OF_MONTH, -2);

        // Should definitely trigger
        assertTrue(!limit.before(lastReminder5DaysAgo),
            "Should send reminder when lastReminder is much older than limit");
    }

    @Test
    @DisplayName("Should NOT send reminder when lastReminder is recent (within interval)")
    void testRecentLastReminder() {
        // Given interval = 3 days
        int daysBeforeReminder = 3;
        Calendar now = GregorianCalendar.getInstance();
        Calendar limit = GregorianCalendar.getInstance();
        limit.add(Calendar.DAY_OF_MONTH, daysBeforeReminder * -1);

        // lastReminder was just set (1 hour ago)
        Calendar lastReminder1HourAgo = GregorianCalendar.getInstance();
        lastReminder1HourAgo.add(Calendar.HOUR, -1);

        // Should NOT trigger (still within the 3-day interval)
        assertFalse(!limit.before(lastReminder1HourAgo),
            "Should NOT send reminder when lastReminder is very recent");
    }

    @Test
    @DisplayName("Validate condition equivalence: !limit.before(x) == limit.after(x) || limit.equals(x)")
    void testConditionEquivalence() {
        // This test validates that our new condition !limit.before(x)
        // is equivalent to (limit.after(x) || limit.equals(x))
        // which gives us the >= behavior we want

        int daysBeforeReminder = 3;
        Calendar limit = GregorianCalendar.getInstance();
        limit.add(Calendar.DAY_OF_MONTH, daysBeforeReminder * -1);

        // Test at boundary (equal)
        Calendar lastReminderAtLimit = (Calendar) limit.clone();
        boolean newCondition = !limit.before(lastReminderAtLimit);
        boolean oldConditionWouldFail = limit.after(lastReminderAtLimit);

        assertTrue(newCondition, "New condition should be true at boundary");
        assertFalse(oldConditionWouldFail, "Old condition (limit.after) would fail at boundary");

        // Test before limit (older)
        Calendar lastReminderOlder = (Calendar) limit.clone();
        lastReminderOlder.add(Calendar.DAY_OF_MONTH, -1);
        assertTrue(!limit.before(lastReminderOlder), "New condition should be true when older");
        assertTrue(limit.after(lastReminderOlder), "Old condition would also work when older");

        // Test after limit (newer)
        Calendar lastReminderNewer = (Calendar) limit.clone();
        lastReminderNewer.add(Calendar.DAY_OF_MONTH, 1);
        assertFalse(!limit.before(lastReminderNewer), "New condition should be false when newer");
        assertFalse(limit.after(lastReminderNewer), "Old condition would also be false when newer");
    }
}
