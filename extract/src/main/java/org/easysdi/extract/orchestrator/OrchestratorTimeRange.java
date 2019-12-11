/*
 * Copyright (C) 2019 arx iT
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
package org.easysdi.extract.orchestrator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.easysdi.extract.utils.DateTimeUtils;
import org.easysdi.extract.web.model.json.PublicField;
import org.joda.time.DateTime;



/**
 *
 * @author Yves Grasset
 */
public class OrchestratorTimeRange {

    public static final int MAXIMUM_DAY_INDEX = 7;

    public static final int MINIMUM_DAY_INDEX = 1;

    @JsonProperty("dayto")
    @JsonView(PublicField.class)
    private int endDayIndex;

    @JsonProperty("timeto")
    @JsonView(PublicField.class)
    private String endTime;

    @JsonProperty("dayfrom")
    @JsonView(PublicField.class)
    private int startDayIndex;

    @JsonProperty("timefrom")
    @JsonView(PublicField.class)
    private String startTime;



    /**
     * @return the endDayIndex
     */
    public final int getEndDayIndex() {
        return this.endDayIndex;
    }



    /**
     * @param newEndDayIndex the endDayIndex to set
     */
    public final void setEndDayIndex(int newEndDayIndex) {

        if (newEndDayIndex < OrchestratorTimeRange.MINIMUM_DAY_INDEX || newEndDayIndex > OrchestratorTimeRange.MAXIMUM_DAY_INDEX) {
            throw new IllegalArgumentException("The end day index must be between 1 and 7.");
        }

        this.endDayIndex = newEndDayIndex;
    }



    /**
     * @return the endTime
     */
    public final String getEndTime() {
        return endTime;
    }



    /**
     * @param newEndTime the endTime to set
     */
    public final void setEndTime(String newEndTime) {

        if (StringUtils.isBlank(newEndTime) || !DateTimeUtils.isTimeStringValid(newEndTime, true)) {
            throw new IllegalArgumentException("The end time is not a valid time string with the format HH:mm.");
        }

        this.endTime = newEndTime;
    }



    /**
     * @return the startDayIndex
     */
    public final int getStartDayIndex() {
        return startDayIndex;
    }



    /**
     * @param newStartDayIndex the startDayIndex to set
     */
    public final void setStartDayIndex(int newStartDayIndex) {

        if (newStartDayIndex < OrchestratorTimeRange.MINIMUM_DAY_INDEX
                || newStartDayIndex > OrchestratorTimeRange.MAXIMUM_DAY_INDEX) {
            throw new IllegalArgumentException("The end day index must be between 1 and 7.");
        }

        this.startDayIndex = newStartDayIndex;
    }



    /**
     * @return the startTime
     */
    public final String getStartTime() {
        return startTime;
    }



    /**
     * @param newStartTime the startTime to set
     */
    public final void setStartTime(String newStartTime) {

        if (StringUtils.isBlank(newStartTime) || !DateTimeUtils.isTimeStringValid(newStartTime, true)) {
            throw new IllegalArgumentException("The end time is not a valid time string with the format HH:mm.");
        }

        this.startTime = newStartTime;
    }



    public OrchestratorTimeRange() {
        this(OrchestratorTimeRange.MINIMUM_DAY_INDEX, OrchestratorTimeRange.MAXIMUM_DAY_INDEX);
    }



    public OrchestratorTimeRange(final int startDay, final int endDay) {
        this(startDay, endDay, "00:00", "24:00");
    }



    public OrchestratorTimeRange(final int startDay, final int endDay, final String startTime, final String endTime) {
        this.setStartDayIndex(startDay);
        this.setEndDayIndex(endDay);
        this.setStartTime(startTime);
        this.setEndTime(endTime);
    }



    public static List<OrchestratorTimeRange> fromCollectionJson(String rangeCollectionJson) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.readValue(rangeCollectionJson, new TypeReference<List<OrchestratorTimeRange>>() {
            });

        } catch (IOException ex) {

            return null;
        }
    }



    @Override
    public final boolean equals(Object object) {

        if (object == null || !(object instanceof OrchestratorTimeRange)) {
            return false;
        }

        OrchestratorTimeRange other = (OrchestratorTimeRange) object;

        return this.startDayIndex == other.startDayIndex && this.endDayIndex == other.endDayIndex
                && this.startTime.equals(other.startTime) && this.endTime.equals(other.endTime);
    }



    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.endDayIndex;
        hash = 53 * hash + Objects.hashCode(this.endTime);
        hash = 53 * hash + this.startDayIndex;
        hash = 53 * hash + Objects.hashCode(this.startTime);
        return hash;
    }



    public final boolean isInRange(DateTime dateTime) {
        return this.fitsDayRange(dateTime) && this.fitsTimeRange(dateTime);
    }



    public final boolean checkValidity() {

        if (this.startDayIndex < OrchestratorTimeRange.MINIMUM_DAY_INDEX
                || this.startDayIndex > OrchestratorTimeRange.MAXIMUM_DAY_INDEX) {
            return false;
        }

        if (this.endDayIndex < OrchestratorTimeRange.MINIMUM_DAY_INDEX
                || this.endDayIndex > OrchestratorTimeRange.MAXIMUM_DAY_INDEX) {
            return false;
        }

        if (!DateTimeUtils.isTimeStringValid(this.startTime, true)) {
            return false;
        }

        if (!DateTimeUtils.isTimeStringValid(this.endTime, true)) {
            return false;
        }

        return true;
    }



    public final String toJson() {
        return null;
    }



    private boolean fitsDayRange(final DateTime dateTime) {
        final int dayOfWeek = dateTime.getDayOfWeek();

        if (this.startDayIndex <= this.endDayIndex) {
            return dayOfWeek >= this.startDayIndex && dayOfWeek <= this.endDayIndex;
        }

        return dayOfWeek >= this.startDayIndex || dayOfWeek <= this.endDayIndex;
    }



    private boolean fitsTimeRange(final DateTime dateTime) {
        return DateTimeUtils.compareWithTimeString(dateTime, startTime) >= 0
                && DateTimeUtils.compareWithTimeString(dateTime, endTime) <= 0;
    }

}
