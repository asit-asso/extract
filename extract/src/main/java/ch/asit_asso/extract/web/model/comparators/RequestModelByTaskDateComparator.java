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
package ch.asit_asso.extract.web.model.comparators;

import java.util.Calendar;
import java.util.Comparator;

import ch.asit_asso.extract.web.model.RequestModel;


/**
 * Compares two request history entries based on their step (and on their request before that if they are
 * not related to the same one.
 *
 * @author Yves Grasset
 */
public class RequestModelByTaskDateComparator implements Comparator<RequestModel> {

    /**
     * Whether the tasks with the higher position index should be ranked higher.
     */
    private final boolean descending;



    /**
     * Creates a new comparator instance.
     */
    public RequestModelByTaskDateComparator() {
        this.descending = false;
    }



    /**
     * *
     * Creates a new comparator instance.
     *
     * @param descendingOrder <code>true</code> to rank the task with the higher position index higher.
     */
    public RequestModelByTaskDateComparator(final boolean descendingOrder) {
        this.descending = descendingOrder;
    }



    /**
     * Compares a request history entry to determine how it should be ordered compared to another.
     *
     * @param model1 the entry to compare
     * @param model2 the entry that the other entry must be compared to
     * @return a negative number if the first entry is less than the second one, a positive number if it is greater, or
     *         <code>0</code> if they are equal.
     */
    @Override
    public final int compare(final RequestModel model1, final RequestModel model2) {
        final Calendar taskDate1 = model1.getTaskDate();
        final Calendar taskDate2 = model2.getTaskDate();

        assert taskDate1 != null && taskDate2 != null : "The current task date of a request should never be null.";

        int comparisonResult = taskDate1.compareTo(taskDate2);

        return (this.descending) ? comparisonResult * -1 : comparisonResult;
    }

}
