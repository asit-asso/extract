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
package ch.asit_asso.extract.persistence.sorts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;



/**
 * A set of criteria to determine how a set of requests must be sorted.
 *
 * @author Yves Grasset
 */
public final class RequestSort {

    /**
     * The list of all the field names that are accepted for sorting requests.
     */
    private static final List<String> ALLOWED_FIELDS = Arrays.asList("client", "endDate", "orderLabel",
            "process.name", "productLabel", "startDate");



    /**
     * Creates a new sort instance.
     */
    private RequestSort() {
    }



    /**
     * Builds a set of sorting criteria.
     *
     * @param sortFields    an array that contains the names of the fields that the requests must be sorted by.
     *                      Unallowed field names will be ignored
     * @param sortDirection <code>"asc"</code> to sort the data with the smaller value first or
     *                      <code>"desc"</code> to sort the data with the larger value first or
     * @return the sort criteria
     */
    public static Sort getSort(final String[] sortFields, final String sortDirection) {
        if (sortFields == null) {
            throw new IllegalArgumentException("The sort fields array cannot be null.");
        }

        if (sortDirection == null) {
            throw new IllegalArgumentException("The sort direction cannot be null.");
        }

        final Direction direction = ("desc").equals(sortDirection.toLowerCase()) ? Direction.DESC : Direction.ASC;
        List<String> safeSortFields = RequestSort.filterSortFields(sortFields);
        final List<Order> ordersList = new ArrayList<>();

        for (String sortField : safeSortFields) {
            ordersList.add(new Order(direction, sortField, Sort.NullHandling.NULLS_LAST).ignoreCase());
        }

        return Sort.by(ordersList);
    }



    /**
     * Removes the field names that are not allowed for sorting requests.
     *
     * @param sortFields an array that contains the sort fields that should be apply
     * @return a list that contains the desired sort fields that are allowed
     */
    private static List<String> filterSortFields(final String[] sortFields) {
        List<String> tempList = Arrays.asList(sortFields);
        tempList.retainAll(RequestSort.ALLOWED_FIELDS);

        return tempList;
    }

}
