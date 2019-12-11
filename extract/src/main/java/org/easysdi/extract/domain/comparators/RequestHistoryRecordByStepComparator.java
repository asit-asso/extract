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
package org.easysdi.extract.domain.comparators;

import java.util.Comparator;
import org.easysdi.extract.domain.Request;
import org.easysdi.extract.domain.RequestHistoryRecord;



/**
 * Compares two request history entries based on their step (and on their request before that if they are
 * not related to the same one.
 *
 * @author Yves Grasset
 */
public class RequestHistoryRecordByStepComparator implements Comparator<RequestHistoryRecord> {

    /**
     * The number that determines how the request associated to the record must be sorted compared to the
     * request step.
     */
    private static final int REQUEST_WEIGHT_FACTOR = 100;



    /**
     * Compares a request history entry to determine how it should be ordered compared to another.
     *
     * @param entry1 the entry to compare
     * @param entry2 the entry that the other entry must be compared to
     * @return a negative number if the first entry is less than the second one, a positive number if it is greater, or
     *         <code>0</code> if they are equal.
     */
    @Override
    public final int compare(final RequestHistoryRecord entry1, final RequestHistoryRecord entry2) {
        final int requestOrder = this.compareRequests(entry1.getRequest(), entry2.getRequest());

        if (requestOrder != 0) {
            return requestOrder * RequestHistoryRecordByStepComparator.REQUEST_WEIGHT_FACTOR;
        }

        final Integer step1 = entry1.getStep();
        final Integer step2 = entry2.getStep();

        if (step1 == null) {

            if (step2 != null) {
                return -1;
            }

            return 0;
        }

        if (step2 == null) {
            return 1;
        }

        return step1.compareTo(step2);
    }



    /**
     * Compares a request to determine how it should be ordered compared to another.
     *
     * @param request1 the request to compare
     * @param request2 the request that the other request must be compared to
     * @return a negative number if the first entry is less than the second one, a positive number if it is greater, or
     *         <code>0</code> if they are equal.
     */
    private int compareRequests(final Request request1, final Request request2) {
        return request1.getId().compareTo(request2.getId());
    }

}
