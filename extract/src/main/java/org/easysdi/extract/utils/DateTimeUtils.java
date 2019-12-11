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
package org.easysdi.extract.utils;

import java.util.Calendar;
import org.easysdi.extract.utils.SimpleTemporalSpan.TemporalField;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;



/**
 * A set of helper functions to manipulate temporal objects.
 *
 * @author Yves Grasset
 */
public abstract class DateTimeUtils {

    public enum DaysOfWeek {
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY,
        SUNDAY
    }



    public static final int compareWithTimeString(final DateTime time, final String timeString) {

        if (time == null) {
            throw new IllegalArgumentException("The time to compare cannot be null.");
        }

        if (!DateTimeUtils.isTimeStringValid(timeString, true)) {
            throw new IllegalArgumentException("The time string to compare the date to is invalid.");
        }

        final int timeHour = time.getHourOfDay();
        final String[] timeStringComponents = timeString.split(":");
        final int timeStringHour = Integer.valueOf(timeStringComponents[0]);

        if (timeHour < timeStringHour) {
            return -1;
        }

        if (timeHour > timeStringHour) {
            return 1;
        }

        final int timeMinutes = time.getMinuteOfHour();
        final int timeStringMinutes = Integer.valueOf(timeStringComponents[1]);

        if (timeMinutes < timeStringMinutes) {
            return -1;
        }

        if (timeMinutes > timeStringMinutes) {
            return 1;
        }

        return 0;
    }



    public static final int compareTimeStrings(final String timeString1, final String timeString2) {

        if (!DateTimeUtils.isTimeStringValid(timeString1, true)) {
            throw new IllegalArgumentException("The time string to compare is invalid.");
        }

        if (!DateTimeUtils.isTimeStringValid(timeString2, true)) {
            throw new IllegalArgumentException("The time string to be compared to is invalid.");
        }

        final String[] timeString1Components = timeString1.split(":");
        final String[] timeString2Components = timeString2.split(":");
        final int timeString1Hour = Integer.valueOf(timeString1Components[0]);
        final int timeString2Hour = Integer.valueOf(timeString2Components[0]);

        if (timeString1Hour < timeString2Hour) {
            return -1;
        }

        if (timeString1Hour > timeString2Hour) {
            return 1;
        }

        final int timeString1Minutes = Integer.valueOf(timeString1Components[1]);
        final int timeString2Minutes = Integer.valueOf(timeString2Components[1]);

        if (timeString1Minutes < timeString2Minutes) {
            return -1;
        }

        if (timeString1Minutes > timeString2Minutes) {
            return 1;
        }

        return 0;
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
     * @return an simple temporal span object that contains the numeric value of the interval and the most significant
     *         field
     */
    public static final SimpleTemporalSpan getFloorDifference(final Calendar start, final Calendar end) {
        Period difference = new Period(start.getTimeInMillis(), end.getTimeInMillis());

        if (difference.getYears() > 0) {
            return new SimpleTemporalSpan(difference.getYears(), TemporalField.YEARS);
        }

        if (difference.getMonths() > 0) {
            return new SimpleTemporalSpan(difference.getMonths(), TemporalField.MONTHS);
        }

        if (difference.getWeeks() > 0) {
            return new SimpleTemporalSpan(difference.getWeeks(), TemporalField.WEEKS);
        }

        if (difference.getDays() > 0) {
            return new SimpleTemporalSpan(difference.getDays(), TemporalField.DAYS);
        }

        if (difference.getHours() > 0) {
            return new SimpleTemporalSpan(difference.getHours(), TemporalField.HOURS);
        }

        if (difference.getMinutes() > 0) {
            return new SimpleTemporalSpan(difference.getMinutes(), TemporalField.MINUTES);
        }

        return new SimpleTemporalSpan(difference.getSeconds(), TemporalField.SECONDS);
    }



    public static final boolean isTimeStringValid(String timeString) {
        return DateTimeUtils.isTimeStringValid(timeString, false);
    }



    public static final boolean isTimeStringValid(String timeString, boolean allow24) {

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
