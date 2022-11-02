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



/**
 * The difference between two points in time expressed in one temporal field, such as 1 day or
 * 15 hours.
 *
 * @author Yves Grasset
 */
public class SimpleTemporalSpan {

    /**
     * The numeric part of the span.
     */
    private final double value;

    /**
     * The temporal unit of the span, such as days or minutes.
     */
    private final TemporalField field;



    /**
     * Obtains the numerical part of this span.
     *
     * @return the numerical value
     */
    public final double getValue() {
        return this.value;
    }



    /**
     * Obtains the temporal unit of this span (such as days or minutes).
     *
     * @return the field
     */
    public final TemporalField getField() {
        return this.field;
    }



    /**
     * The different type of fields that are supported by this span.
     */
    public enum TemporalField {
        /**
         * The span is expressed in years.
         */
        YEARS,
        /**
         * The span is expressed in months.
         */
        MONTHS,
        /**
         * The span is expressed in weeks.
         */
        WEEKS,
        /**
         * The span is expressed in days.
         */
        DAYS,
        /**
         * The span is expressed in hours.
         */
        HOURS,
        /**
         * The span is expressed in minutes.
         */
        MINUTES,
        /**
         * The span is expressed in seconds.
         */
        SECONDS,
        /**
         * The span is expressed in milliseconds.
         */
        MILLISECONDS
    }



    /**
     * Creates a new simple span.
     *
     * @param numericalValue the number that defines the value of the span
     * @param temporalField  the temporal unit of the span
     */
    public SimpleTemporalSpan(final double numericalValue, final TemporalField temporalField) {

        if (temporalField == null) {
            throw new IllegalArgumentException("The temporal field cannot be null.");
        }

        this.value = numericalValue;
        this.field = temporalField;
    }



    /**
     * Converts this span to a string defined by a formatter.
     *
     * @param formatter the object that can convert this span to a string. <code>null</code> will return the result
     *                  of {@link toString()}.
     * @return the string that represents this span
     */
    public final String toString(final SimpleTemporalSpanFormatter formatter) {

        if (formatter == null) {
            return this.toString();
        }

        return formatter.format(this);
    }

}
