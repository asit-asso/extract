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
package ch.asit_asso.extract.utils;

import java.util.Calendar;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;


/**
 * A set of helper functions to manipulate temporal objects.
 *
 * @author Yves Grasset
 */
public abstract class DateTimeUtils {

    public static final PeriodFormatterBuilder TIME_STRING_BUILDER = new PeriodFormatterBuilder().printZeroIfSupported()
                                                                                                 .minimumPrintedDigits(2)
                                                                                                 .appendHours()
                                                                                                 .appendSeparator(":")
                                                                                                 .appendMinutes();

    public static final PeriodFormatter TIME_STRING_FORMATTER = DateTimeUtils.TIME_STRING_BUILDER.toFormatter();

    public static final int SECONDS_IN_A_DAY = 3600 * 24;


    public enum DaysOfWeek {
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY,
        SUNDAY
    }



    public static int compareWithTimeString(final DateTime time, final String timeString) {

        if (time == null) {
            throw new IllegalArgumentException("The time to compare cannot be null.");
        }

        if (!DateTimeUtils.isTimeStringValid(timeString, true)) {
            throw new IllegalArgumentException("The time string to compare the date to is invalid.");
        }

        final long timeStringSeconds = DateTimeUtils.timeStringToSeconds(timeString);
        final long dateTimeSeconds = time.getSecondOfDay();

        return Long.compare(dateTimeSeconds, timeStringSeconds);
    }



    public static int compareTimeStrings(final String timeString1, final String timeString2) {
        final long seconds1 = DateTimeUtils.timeStringToSeconds(timeString1);
        final long seconds2 = DateTimeUtils.timeStringToSeconds(timeString2);

        return Long.compare(seconds1, seconds2);
    }


    public static Period parseTimeString(String timeString) {

        if (!DateTimeUtils.isTimeStringValid(timeString, true)) {
            throw new IllegalArgumentException("The time string is invalid.");
        }

        return DateTimeUtils.TIME_STRING_FORMATTER.parsePeriod(timeString).normalizedStandard(PeriodType.time());
    }



    public static String secondsToTimeString(final long seconds)
    {

        if (seconds < 0) {
            throw new IllegalArgumentException("The number of seconds cannot be negative");
        }

        if (seconds > DateTimeUtils.SECONDS_IN_A_DAY) {
            throw new IllegalArgumentException("The number of seconds cannot be larger that 24 hours");
        }

        Duration duration = new Duration(seconds * 1000L);
        Period period = duration.toPeriod().normalizedStandard(PeriodType.time());

        return DateTimeUtils.TIME_STRING_FORMATTER.print(period);
    }


    public static long timeStringToSeconds(final String timeString)
    {
        final Period period = DateTimeUtils.parseTimeString(timeString);

        return period.toStandardDuration().getStandardSeconds();
    }



    /**
     * Gets the interval between two time points expressed in the floored value of the most significant
     * temporal field.
     * <p>
     * Examples:
     * May 25, 2014 16:35:54 - May 26, 2014 19:00:00 = 1 day
     * May 25, 2014 16:35:54 - May 26, 2014 10:00:00 = 17 hours
     *
     * @param start the time point where the interval starts
     * @param end   the time point where the interval ends
     * @return a simple temporal span object that contains the numeric value of the interval and the most significant
     *         field
     */
    public static SimpleTemporalSpan getFloorDifference(final Calendar start, final Calendar end) {
        Period difference = new Period(start.getTimeInMillis(), end.getTimeInMillis());

        if (difference.getYears() > 0) {
            return new SimpleTemporalSpan(difference.getYears(), SimpleTemporalSpan.TemporalField.YEARS);
        }

        if (difference.getMonths() > 0) {
            return new SimpleTemporalSpan(difference.getMonths(), SimpleTemporalSpan.TemporalField.MONTHS);
        }

        if (difference.getWeeks() > 0) {
            return new SimpleTemporalSpan(difference.getWeeks(), SimpleTemporalSpan.TemporalField.WEEKS);
        }

        if (difference.getDays() > 0) {
            return new SimpleTemporalSpan(difference.getDays(), SimpleTemporalSpan.TemporalField.DAYS);
        }

        if (difference.getHours() > 0) {
            return new SimpleTemporalSpan(difference.getHours(), SimpleTemporalSpan.TemporalField.HOURS);
        }

        if (difference.getMinutes() > 0) {
            return new SimpleTemporalSpan(difference.getMinutes(), SimpleTemporalSpan.TemporalField.MINUTES);
        }

        return new SimpleTemporalSpan(difference.getSeconds(), SimpleTemporalSpan.TemporalField.SECONDS);
    }



    public static boolean isTimeStringValid(String timeString) {
        return DateTimeUtils.isTimeStringValid(timeString, false);
    }



    public static boolean isTimeStringValid(String timeString, boolean allow24) {

        if (allow24 && "24:00".equals(timeString)) {
            return true;
        }

        DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm");

        try {
            timeFormatter.parseDateTime(timeString);
            return true;

        } catch (UnsupportedOperationException | IllegalArgumentException exception) {
            return false;
        }
    }

}
