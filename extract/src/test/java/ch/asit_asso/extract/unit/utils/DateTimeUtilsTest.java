package ch.asit_asso.extract.unit.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.atomic.AtomicReference;
import ch.asit_asso.extract.utils.DateTimeUtils;
import ch.asit_asso.extract.utils.SimpleTemporalSpan;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateTimeUtilsTest {

    @Test
    void compareDateTimeWithTimeStringAfter() {
        String timeString = "13:30";
        DateTime dateTime = new DateTime(2024, 7, 22, 11, 25);

        int comparisonResult = DateTimeUtils.compareWithTimeString(dateTime, timeString);

        assertTrue(comparisonResult < 0);
    }

    @Test
    void compareDateTimeWithTimeStringBefore() {
        String timeString = "13:30";
        DateTime dateTime = new DateTime(2024, 7, 22, 14, 25);

        int comparisonResult = DateTimeUtils.compareWithTimeString(dateTime, timeString);

        assertTrue(comparisonResult > 0);
    }


    @Test
    void compareDateTimeWithTimeStringEqual() {
        String timeString = "13:30";
        DateTime dateTime = new DateTime(2024, 7, 22, 13, 30);

        int comparisonResult = DateTimeUtils.compareWithTimeString(dateTime, timeString);

        assertEquals(0, comparisonResult);
    }


    @Test
    void compareDateTimeWithTimeStringEqualExceptSeconds() {
        String timeString = "13:30";
        DateTime dateTime = new DateTime(2024, 7, 22, 13, 30, 43);

        int comparisonResult = DateTimeUtils.compareWithTimeString(dateTime, timeString);

        assertEquals(1, comparisonResult);
    }

    @Test
    void compareDateTimeWithTimeStringWithInvalidTimeString() {
        String timeString = "1330";
        DateTime dateTime = new DateTime(2024, 7, 22, 11, 25);

        assertThrows(IllegalArgumentException.class, () -> DateTimeUtils.compareWithTimeString(dateTime, timeString));
    }


    @Test
    void compareNullDateTimeWithTimeString() {
        String timeString = "13:30";

        assertThrows(IllegalArgumentException.class, () -> DateTimeUtils.compareWithTimeString(null, timeString));
    }


    @Test
    void compareTimeStringToTimeStringAfter() {
        String timeString1 = "11:47";
        String timeString2 = "13:30";

        int comparisonResult = DateTimeUtils.compareTimeStrings(timeString1, timeString2);

        assertTrue(comparisonResult < 0);

    }


    @Test
    void compareTimeStringToTimeStringBefore() {
        String timeString1 = "15:47";
        String timeString2 = "13:30";

        int comparisonResult = DateTimeUtils.compareTimeStrings(timeString1, timeString2);

        assertTrue(comparisonResult > 0);
    }


    @Test
    void compareTimeStringToTimeStringEqual() {
        String timeString1 = "13:30";
        String timeString2 = "13:30";

        int comparisonResult = DateTimeUtils.compareTimeStrings(timeString1, timeString2);

        assertEquals(0, comparisonResult);
    }


    @Test
    void compareTimeStringToInvalidTimeString() {
        String timeString1 = "15:25";
        String timeString2 = "1330";

        assertThrows(IllegalArgumentException.class, () -> DateTimeUtils.compareTimeStrings(timeString1, timeString2));
    }


    @Test
    void compareInvalidTimeStringToTimeString() {
        String timeString1 = "1525";
        String timeString2 = "13:30";

        assertThrows(IllegalArgumentException.class, () -> DateTimeUtils.compareTimeStrings(timeString1, timeString2));
    }



    @Test
    void parseTimeStringInvalid() {
        String timeString = "1525";

        assertThrows(IllegalArgumentException.class, () -> DateTimeUtils.parseTimeString(timeString));
    }



    @Test
    void parseTimeStringBeyond24() {
        String timeString = "37:25";

        assertThrows(IllegalArgumentException.class, () -> DateTimeUtils.parseTimeString(timeString));
    }



    @Test
    void parseTimeStringWithoutLeadingZero() {
        String timeString = "7:33";
        AtomicReference<Period> parsedPeriodReference = new AtomicReference<>();

        assertDoesNotThrow(() -> parsedPeriodReference.set(DateTimeUtils.parseTimeString(timeString)));
        Period period = parsedPeriodReference.get();

        assertNotNull(period);
        assertEquals(7, period.getHours());
        assertEquals(33, period.getMinutes());
    }



    @Test
    void parseTimeStringBeforeNoon() {
        String timeString = "09:14";

        Period period = DateTimeUtils.parseTimeString(timeString);

        assertNotNull(period);
        assertEquals(9, period.getHours());
        assertEquals(14, period.getMinutes());
    }



    @Test
    void parseTimeStringAfterNoon() {
        String timeString = "22:58";

        Period period = DateTimeUtils.parseTimeString(timeString);

        assertNotNull(period);
        assertEquals(22, period.getHours());
        assertEquals(58, period.getMinutes());
    }



    @Test
    void secondsToTimeStringBeforeNoon() {
        final int seconds = 33900;
        final String timeString = DateTimeUtils.secondsToTimeString(seconds);

        assertEquals("09:25", timeString);
    }



    @Test
    void secondsToTimeStringWithSeconds() {
        final int seconds = 33911;

        final String timeString = DateTimeUtils.secondsToTimeString(seconds);

        assertEquals("09:25", timeString);
    }


    @Test
    void secondsToTimeStringAfterNoon() {
        final int seconds = 65160;
        final String timeString = DateTimeUtils.secondsToTimeString(seconds);

        assertEquals("18:06", timeString);
    }



    @Test
    void secondsToTimeStringMidnight() {
        final int seconds = 0;

        final String timeString = DateTimeUtils.secondsToTimeString(seconds);

        assertEquals("00:00", timeString);
    }


    @Test
    void secondsToTimeString2400() {
        final int seconds = 86400;

        final String timeString = DateTimeUtils.secondsToTimeString(seconds);

        assertEquals("24:00", timeString);
    }



    @Test
    void negativeSecondsToTimeString() {
        int seconds = -12785;

        assertThrows(IllegalArgumentException.class, () -> DateTimeUtils.secondsToTimeString(seconds));
    }



    @Test
    void Above24HoursSecondsToTimeString() {
        final int seconds = 118885;

        assertThrows(IllegalArgumentException.class, () -> DateTimeUtils.secondsToTimeString(seconds));
    }



    @Test
    void timeStringToSecondsAfterNoon() {
        final String timeString = "19:54";

        final long seconds = DateTimeUtils.timeStringToSeconds(timeString);

        assertEquals(71640, seconds);
    }



    @Test
    void timeStringToSecondsBeforeNoon() {
        final String timeString = "07:38";

        final long seconds = DateTimeUtils.timeStringToSeconds(timeString);

        assertEquals(27480, seconds);
    }



    @Test
    void timeStringToSecondsBeforeNoonWithoutLeadingZero() {
        final String timeString = "7:38";

        final AtomicReference<Long> secondsReference = new AtomicReference<>();
        assertDoesNotThrow(() -> secondsReference.set(DateTimeUtils.timeStringToSeconds(timeString)));
        final Long seconds = secondsReference.get();

        assertNotNull(seconds);
        assertEquals(27480, seconds);
    }



    @Test
    void timeStringToSecondsInvalidNoon() {
        final String timeString = "06:85";

        assertThrows(IllegalArgumentException.class, () -> DateTimeUtils.timeStringToSeconds(timeString));
    }



    @Test
    void timeStringToSecondsMidnight() {
        final String timeString = "00:00";

        final long seconds = DateTimeUtils.timeStringToSeconds(timeString);

        assertEquals(0, seconds);
    }



    @Test
    void timeStringToSeconds2400() {
        final String timeString = "24:00";

        final long seconds = DateTimeUtils.timeStringToSeconds(timeString);

        assertEquals(86400, seconds);
    }



    @Test
    void timeStringToSecondsBeyond24() {
        final String timeString = "31:38";

        assertThrows(IllegalArgumentException.class, () -> DateTimeUtils.timeStringToSeconds(timeString));
    }



    @Test
    void getFloorDifferenceInYears() {
        final Calendar startDate = new GregorianCalendar(1995, Calendar.DECEMBER,11, 14,32, 28);
        final Calendar endDate = new GregorianCalendar(2024, Calendar.MAY, 24, 13,5, 9);

        final SimpleTemporalSpan span = DateTimeUtils.getFloorDifference(startDate, endDate);

        assertEquals(SimpleTemporalSpan.TemporalField.YEARS, span.getField());
        assertEquals(28, span.getValue());
    }



    @Test
    void getFloorDifferenceInMonths() {
        final Calendar startDate = new GregorianCalendar(2023, Calendar.DECEMBER,11, 14,32, 28);
        final Calendar endDate = new GregorianCalendar(2024, Calendar.MAY,24, 13,5, 9);

        final SimpleTemporalSpan span = DateTimeUtils.getFloorDifference(startDate, endDate);

        assertEquals(SimpleTemporalSpan.TemporalField.MONTHS, span.getField());
        assertEquals(5, span.getValue());

    }



    @Test
    void getFloorDifferenceInWeeks() {
        final Calendar startDate = new GregorianCalendar(2024, Calendar.MAY,11, 14,32, 28);
        final Calendar endDate = new GregorianCalendar(2024, Calendar.MAY,24, 13,5, 9);

        final SimpleTemporalSpan span = DateTimeUtils.getFloorDifference(startDate, endDate);

        assertEquals(SimpleTemporalSpan.TemporalField.WEEKS, span.getField());
        assertEquals(1, span.getValue());
    }



    @Test
    void getFloorDifferenceInDays() {
        final Calendar startDate = new GregorianCalendar(2024, Calendar.MAY,20, 14,32, 28);
        final Calendar endDate = new GregorianCalendar(2024, Calendar.MAY,24, 13,5, 9);

        final SimpleTemporalSpan span = DateTimeUtils.getFloorDifference(startDate, endDate);

        assertEquals(SimpleTemporalSpan.TemporalField.DAYS, span.getField());
        assertEquals(3, span.getValue());
    }



    @Test
    void getFloorDifferenceInHours() {
        final Calendar startDate = new GregorianCalendar(2024, Calendar.MAY,24, 8,32, 28);
        final Calendar endDate = new GregorianCalendar(2024, Calendar.MAY,24, 13,5, 9);

        final SimpleTemporalSpan span = DateTimeUtils.getFloorDifference(startDate, endDate);

        assertEquals(SimpleTemporalSpan.TemporalField.HOURS, span.getField());
        assertEquals(4, span.getValue());
    }



    @Test
    void getFloorDifferenceInMinutes() {
        final Calendar startDate = new GregorianCalendar(2024, Calendar.MAY,24, 12,32, 28);
        final Calendar endDate = new GregorianCalendar(2024, Calendar.MAY,24, 13,5, 9);

        final SimpleTemporalSpan span = DateTimeUtils.getFloorDifference(startDate, endDate);

        assertEquals(SimpleTemporalSpan.TemporalField.MINUTES, span.getField());
        assertEquals(32, span.getValue());
    }



    @Test
    void getFloorDifferenceInSeconds() {
        final Calendar startDate = new GregorianCalendar(2024, Calendar.MAY,24, 13,4, 28);
        final Calendar endDate = new GregorianCalendar(2024, Calendar.MAY,24, 13,5, 9);

        final SimpleTemporalSpan span = DateTimeUtils.getFloorDifference(startDate, endDate);

        assertEquals(SimpleTemporalSpan.TemporalField.SECONDS, span.getField());
        assertEquals(41, span.getValue());
    }



    @Test
    void getFloorDifferenceBelowSecond() {
        final Calendar startDate = new GregorianCalendar(2024, Calendar.MAY,24, 12,32, 28);
        final Calendar endDate = ((GregorianCalendar) startDate.clone());
        endDate.setTimeInMillis(startDate.getTimeInMillis() + 587);

        final SimpleTemporalSpan span = DateTimeUtils.getFloorDifference(startDate, endDate);

        assertEquals(SimpleTemporalSpan.TemporalField.SECONDS, span.getField());
        assertEquals(0, span.getValue());
    }



    @Test
    void isValidTimeStringValid() {
        final String timeString = "15:47";

        final boolean isValid = DateTimeUtils.isTimeStringValid(timeString);

        assertTrue(isValid);
    }



    @Test
    void isInvalidTimeStringValid() {
        final String timeString = "1547";

        final boolean isValid = DateTimeUtils.isTimeStringValid(timeString);

        assertFalse(isValid);
    }



    @Test
    void testIs2400TimeStringValid() {
        final String timeString = "24:00";

        final boolean isValid = DateTimeUtils.isTimeStringValid(timeString);

        assertFalse(isValid);
    }



    @Test
    void testIs2400TimeStringValidWithAllow24() {
        final String timeString = "24:00";

        final boolean isValid = DateTimeUtils.isTimeStringValid(timeString, true);

        assertTrue(isValid);
    }



    @Test
    void testIsBeyond24HoursTimeStringValid() {
        final String timeString = "35:47";

        final boolean isValid = DateTimeUtils.isTimeStringValid(timeString);

        assertFalse(isValid);
    }



    @Test
    void testIsBeyond60MinutesTimeStringValid() {
        final String timeString = "15:82";

        final boolean isValid = DateTimeUtils.isTimeStringValid(timeString);

        assertFalse(isValid);
    }
}
