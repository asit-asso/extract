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
package ch.asit_asso.extract.plugins.fmeserverv2;

import java.util.Calendar;

import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;

/**
 * Wraps a request to report it as rejected with a fixed remark, without copying every field.
 *
 * <p>This is used when the FME Flow extraction failed because no data matched the extraction
 * perimeter. Rather than leaving the task in error, the original request is decorated so that it
 * is reported as rejected with an explanatory remark, which lets the orchestrator gracefully
 * cancel the request instead of failing it.</p>
 */
final class CancelledExtractionRequest implements ITaskProcessorRequest {

    /**
     * The request being decorated.
     */
    private final ITaskProcessorRequest original;

    /**
     * The remark to report for this request.
     */
    private final String remark;

    /**
     * Creates a new decorator that reports the wrapped request as rejected with a fixed remark.
     *
     * @param original the request to decorate
     * @param remark   the remark to report for this request
     */
    CancelledExtractionRequest(final ITaskProcessorRequest original, final String remark) {
        this.original = original;
        this.remark = remark;
    }

    @Override
    public boolean isRejected() {
        return true;
    }

    @Override
    public String getRemark() {
        return this.remark;
    }

    @Override
    public int getId() {
        return this.original.getId();
    }

    @Override
    public String getOrderGuid() {
        return this.original.getOrderGuid();
    }

    @Override
    public String getProductGuid() {
        return this.original.getProductGuid();
    }

    @Override
    public String getStatus() {
        return this.original.getStatus();
    }

    @Override
    public String getFolderOut() {
        return this.original.getFolderOut();
    }

    @Override
    public String getClient() {
        return this.original.getClient();
    }

    @Override
    public String getClientGuid() {
        return this.original.getClientGuid();
    }

    @Override
    public String getClientEmail() {
        return this.original.getClientEmail();
    }

    @Override
    public Calendar getEndDate() {
        return this.original.getEndDate();
    }

    @Override
    public String getFolderIn() {
        return this.original.getFolderIn();
    }

    @Override
    public String getOrderLabel() {
        return this.original.getOrderLabel();
    }

    @Override
    public String getParameters() {
        return this.original.getParameters();
    }

    @Override
    public String getPerimeter() {
        return this.original.getPerimeter();
    }

    @Override
    public String getProductLabel() {
        return this.original.getProductLabel();
    }

    @Override
    public String getOrganism() {
        return this.original.getOrganism();
    }

    @Override
    public String getOrganismGuid() {
        return this.original.getOrganismGuid();
    }

    @Override
    public Calendar getStartDate() {
        return this.original.getStartDate();
    }

    @Override
    public String getTiers() {
        return this.original.getTiers();
    }

    @Override
    public String getSurface() {
        return this.original.getSurface();
    }

}
