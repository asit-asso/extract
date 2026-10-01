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
package ch.asit_asso.extract.plugins.fmedesktopv2;

import java.util.Calendar;

import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;



/**
 * Wraps a request to report it as rejected with a fixed remark, without copying every field.
 * <p>
 * This is used when the FME extraction failed because no data was found for the request : instead of
 * ending the task in error, the request is reported as cancelled (rejected) with an explanatory remark so
 * that the orchestrator stops the task chain and the connector notifies the client.
 * </p>
 *
 * @author Extract Team
 */
final class CancelledExtractionRequest implements ITaskProcessorRequest {

    /**
     * The request whose data is exposed unchanged by this wrapper.
     */
    private final ITaskProcessorRequest original;

    /**
     * The remark to report for this cancelled request.
     */
    private final String remark;



    /**
     * Creates a new cancelled extraction request wrapper.
     *
     * @param original the request to wrap
     * @param remark   the remark explaining the cancellation
     */
    CancelledExtractionRequest(final ITaskProcessorRequest original, final String remark) {
        this.original = original;
        this.remark = remark;
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
    public String getRemark() {
        return this.remark;
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
    public boolean isRejected() {
        return true;
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
