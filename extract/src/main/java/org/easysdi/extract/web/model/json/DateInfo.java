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
package org.easysdi.extract.web.model.json;

import com.fasterxml.jackson.annotation.JsonView;
import java.util.Calendar;



/**
 * A representation of information about a date that will be formatted in JSON.
 *
 * @author Yves Grasset
 */
public class DateInfo {

    /**
     * The textual representation of this date.
     */
    @JsonView(PublicField.class)
    private final String dateText;

    /**
     * The numeric representation of this date.
     */
    @JsonView(PublicField.class)
    private final long timestamp;



    /**
     * Obtains the textual representation of this date.
     *
     * @return the string with the localized representation of this date
     */
    public final String getDateText() {
        return this.dateText;
    }



    /**
     * Obtains the numeric representation of this date.
     *
     * @return the number of milliseconds that separate this date from the epoch date
     */
    public final long getTimestamp() {
        return this.timestamp;
    }



    /**
     * Create a new JSON date info instance.
     *
     * @param text the string with the localized representation of this date
     * @param date the date to represent
     */
    public DateInfo(final String text, final Calendar date) {
        this.dateText = text;
        this.timestamp = date.getTimeInMillis();
    }

}
