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
package ch.asit_asso.extract.web.model.json;

import com.fasterxml.jackson.annotation.JsonView;



/**
 * The content of a table to be exported as JSON.
 *
 * @author Yves Grasset
 */
public class DataTableResponse {

    /**
     * The identifier of the request. (Only used for server-side processing)
     */
    @JsonView(PublicField.class)
    private final int draw;

    /**
     * The total number of items, including those that are not displayed because of filtering and paging.
     * (Only used for server-side processing)
     */
    @JsonView(PublicField.class)
    private final long recordsTotal;

    /**
     * The total number of items excluding those that are not displayed because of filtering, but including
     * those that are not displayed because of paging. (Only used for server-side processing)
     */
    @JsonView(PublicField.class)
    private final long recordsFiltered;

    /**
     * The table content.
     */
    @JsonView(PublicField.class)
    private final JsonModel[] data;

    /**
     * The message that explain if the data retrieval failed.
     */
    @JsonView(PublicField.class)
    private final String error;



    /**
     * Creates a new instance of the table content object.
     *
     * @param drawId             the identifier of the request
     * @param totalItems         the total number of items, including those that are not displayed because of filtering
     *                           and paging
     * @param totalFilteredItems the total number of items excluding those that are not displayed because of filtering,
     *                           but including
     *                           those that are not displayed because of paging
     * @param items              the data objects for each table row
     */
    public DataTableResponse(final int drawId, final long totalItems, final long totalFilteredItems,
            final JsonModel[] items) {
        this.draw = drawId;
        this.recordsTotal = totalItems;
        this.recordsFiltered = totalFilteredItems;
        this.data = items;
        this.error = null;
    }



    /**
     * Creates a new empty instance with an error message.
     *
     * @param drawId       the identifier of the request
     * @param errorMessage the string explaining the error that occurred
     */
    public DataTableResponse(final int drawId, final String errorMessage) {
        this.draw = drawId;
        this.recordsTotal = 0;
        this.recordsFiltered = 0;
        this.data = new JsonModel[0];
        this.error = errorMessage;
    }



    /**
     * Creates a new empty instance.
     *
     * @param drawId the identifier of the request
     */
    public DataTableResponse(final int drawId) {
        this.draw = drawId;
        this.recordsTotal = 0;
        this.recordsFiltered = 0;
        this.data = new JsonModel[0];
        this.error = null;
    }



    /**
     * Obtains the identifier of the request. (Only used for server-side processing.)
     *
     * @return the identifier
     */
    public final int getDraw() {
        return this.draw;
    }



    /**
     * Obtains the total number of items, including those that are not displayed because of filtering and
     * paging. (Only used for server-side processing.)
     *
     * @return the number of items
     */
    public final long getRecordsTotal() {
        return this.recordsTotal;
    }



    /**
     * Obtains the total number of items excluding those that are not displayed because of filtering, but
     * including those that are not displayed because of paging. (Only used for server-side processing.)
     *
     * @return the number of items matching the filter
     */
    public final long getRecordsFiltered() {
        return this.recordsFiltered;
    }



    /**
     * Obtains the content to display in the table.
     *
     * @return the table data
     */
    public final JsonModel[] getData() {
        return this.data;
    }



    /**
     * Obtains the message that explains if the data retrieval failed.
     *
     * @return the error message, or <code>null</code> if the data retrieval was successful
     */
    public final String getError() {
        return this.error;
    }

}
