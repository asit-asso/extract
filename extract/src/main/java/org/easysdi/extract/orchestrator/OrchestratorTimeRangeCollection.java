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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 *
 * @author Yves Grasset
 */
public class OrchestratorTimeRangeCollection {

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(OrchestratorTimeRangeCollection.class);

    private final List<OrchestratorTimeRange> ranges;



    public OrchestratorTimeRangeCollection() {
        this.ranges = new ArrayList<>();
    }



    public OrchestratorTimeRangeCollection(List<OrchestratorTimeRange> rangesList) {

        if (rangesList == null) {
            throw new IllegalArgumentException("The time ranges list cannot be null.");
        }

        this.ranges = new ArrayList<>(rangesList);
    }



    public static final OrchestratorTimeRangeCollection fromJson(final String rangeCollectionJson) {
        List<OrchestratorTimeRange> rangesList = OrchestratorTimeRange.fromCollectionJson(rangeCollectionJson);

        if (rangesList == null) {
            return null;
        }

        return new OrchestratorTimeRangeCollection(rangesList);
    }



    @Override
    public final boolean equals(Object object) {

        if (object == null || !(object instanceof OrchestratorTimeRangeCollection)) {
            return false;
        }

        final OrchestratorTimeRangeCollection other = (OrchestratorTimeRangeCollection) object;

        if (this.ranges.size() != other.ranges.size()) {
            return false;
        }

        for (int rangeIndex = 0; rangeIndex < this.ranges.size(); rangeIndex++) {

            if (!this.ranges.get(rangeIndex).equals(other.ranges.get(rangeIndex))) {
                return false;
            }
        }

        return true;
    }



    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + Objects.hashCode(this.ranges);
        return hash;
    }



    public final void addRange(final OrchestratorTimeRange range) {

        if (range == null) {
            throw new IllegalArgumentException("The range to add cannot be null.");
        }

        this.ranges.add(range);
    }



    public final OrchestratorTimeRange getRange(final int rangeIndex) {

        if (rangeIndex < 0 || rangeIndex > this.ranges.size()) {
            throw new IndexOutOfBoundsException("The time range index is invalid.");
        }

        return this.ranges.get(rangeIndex);
    }



    public final OrchestratorTimeRange[] getRanges() {
        return this.ranges.toArray(new OrchestratorTimeRange[]{});
    }



    public final void removeRange(final int rangeIndex) {

        if (rangeIndex < 0 || rangeIndex > this.ranges.size()) {
            throw new IndexOutOfBoundsException("The time range index is invalid.");
        }

        this.ranges.remove(rangeIndex);
    }



    public final void removeRange(final OrchestratorTimeRange range) {

        if (range == null) {
            throw new IllegalArgumentException("The range to remove cannot be null.");
        }

        this.ranges.remove(range);
    }



    public final boolean isInRanges(final DateTime dateTime) {

        for (OrchestratorTimeRange range : this.ranges) {

            if (range.isInRange(dateTime)) {
                return true;
            }
        }

        return false;
    }



    public final boolean isValid() {

        for (OrchestratorTimeRange range : this.ranges) {

            if (!range.checkValidity()) {
                return false;
            }
        }

        return true;
    }



    public final String toJson() {
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.writeValueAsString(this.ranges);

        } catch (JsonProcessingException exception) {
            this.logger.error("Could not serialize the operation time ranges of the orchestrator to JSON.", exception);
            return null;
        }
    }

}
