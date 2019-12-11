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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.easysdi.extract.web.model.RequestModel;
import org.springframework.context.MessageSource;



/**
 * A model for an order that must be represented in JSON.
 *
 * @author Yves Grasset
 */
public class RequestJsonModel implements JsonModel {

    /**
     * The string identifying that this order is stopped because of an error.
     */
    private static final String ERROR_STATE = "error";

    /**
     * The string identifying that the processing of this order has completed.
     */
    private static final String FINISHED_STATE = "finished";

    /**
     * The string that identifies the localized string telling that this order is not bound to any process.
     */
    private static final String NO_PROCESS_KEY = "requestsList.process.none";

    /**
     * The string that identifies the localized string to display a time duration (such as "since 3 days").
     */
    private static final String PERIOD_STRING_KEY = "requestsList.span.period";

    /**
     * The string that identifies that this order could not be processed and has been exported as rejected.
     */
    private static final String REJECTED_STATE = "rejected";

    /**
     * The string identifying that this order is currently being processed.
     */
    private static final String RUNNING_STATE = "running";

    /**
     * The string to use to generate the relative URL to view the details of this order.
     */
    private static final String REQUEST_URL_FORMAT = "requests/%d";

    /**
     * The string to use to generate the CSS classes for the table row that will display this order.
     */
    private static final String REQUEST_ROW_CLASSES_FORMAT = "request-row %s";

    /**
     * The string identifying that this order is waiting for a validation by an operator.
     */
    private static final String STANDBY_STATE = "standby";

    /**
     * The string that identifies the localized string to display a point in time (such as "3 days ago").
     */
    private static final String TIME_POINT_STRING_KEY = "requestsList.span.timePoint";

    /**
     * The name of the person that generated this order.
     */
    @JsonView(PublicField.class)
    private String customerName;

    /**
     * The number that identifies the position of this order with the default sort.
     */
    @JsonView(PublicField.class)
    private int index;

    /**
     * Information that identify this order.
     */
    @JsonView(PublicField.class)
    private OrderInfo orderInfo;

    /**
     * Information that identify the process bound to this order, if any.
     */
    @JsonView(PublicField.class)
    private ProcessInfo processInfo;

    /**
     * The HTML attributes to add to the table row that will display this order.
     */
    @JsonProperty("DT_RowAttr")
    @JsonView(PublicField.class)
    private Map<String, String> rowAttributes;

    /**
     * A string that contains the CSS classes to apply to the row that will display this order.
     */
    @JsonProperty("DT_RowClass")
    @JsonView(PublicField.class)
    private String rowClass;

    /**
     * Information about when this order was received.
     */
    @JsonView(PublicField.class)
    private DateInfo startDateInfo;

    /**
     * The string that identifies the status of this order.
     */
    @JsonView(PublicField.class)
    private String state;

    /**
     * Information that identifies the current process task that is running, or the last one if the
     * processing is stopped.
     */
    @JsonView(PublicField.class)
    private TaskInfo taskInfo;



    /**
     * Creates a new JSON representation of an order.
     *
     * @param model         the application model that represents the order
     * @param messageSource the access to the localized application strings
     * @param positionIndex the position of the order to model with the default sort
     */
    public RequestJsonModel(final RequestModel model, final MessageSource messageSource, final int positionIndex) {

        if (model == null) {
            throw new IllegalArgumentException("The request model cannot be null.");
        }

        if (messageSource == null) {
            throw new IllegalArgumentException("The message source cannot be null.");
        }

        this.index = positionIndex;
        this.customerName = model.getCustomerName();
        this.orderInfo = new OrderInfo(model.getOrderLabel(), model.getProductLabel());
        this.processInfo = new ProcessInfo(model.getProcessId(), this.getProcessNameFromModel(model, messageSource));

        final String startDateText = messageSource.getMessage(RequestJsonModel.TIME_POINT_STRING_KEY,
                new Object[]{model.getStartDateSpanToNow()}, Locale.getDefault());
        this.startDateInfo = new DateInfo(startDateText, model.getStartDate());

        if (model.isInError()) {
            this.state = RequestJsonModel.ERROR_STATE;

        } else if (model.isInStandby()) {
            this.state = RequestJsonModel.STANDBY_STATE;

        } else if (model.isFinished()) {
            this.state = (model.isRejected()) ? RequestJsonModel.REJECTED_STATE : RequestJsonModel.FINISHED_STATE;

        } else {
            this.state = RequestJsonModel.RUNNING_STATE;
        }

        this.rowClass = String.format(RequestJsonModel.REQUEST_ROW_CLASSES_FORMAT, this.state);
        this.rowAttributes = new HashMap<>();
        this.rowAttributes.put("data-href", String.format(RequestJsonModel.REQUEST_URL_FORMAT, model.getId()));

        final String taskDateText = messageSource.getMessage(RequestJsonModel.PERIOD_STRING_KEY,
                new Object[]{model.getTaskDateSpanToNow()}, Locale.getDefault());
        this.taskInfo = new TaskInfo(taskDateText, model.getTaskDate(), model.getCurrentStepName());
    }



    /**
     * Obtains the name of the person that generated this order.
     *
     * @return the customer's name
     */
    public final String getCustomerName() {
        return this.customerName;
    }



    /**
     * Obtains the position of this order with the default sort.
     *
     * @return the position
     */
    public final int getIndex() {
        return this.index;
    }



    /**
     * Obtains the information that identify this order.
     *
     * @return the order information
     */
    public final OrderInfo getOrderInfo() {
        return this.orderInfo;
    }



    /**
     * Obtains the information that identifies the process bound to this order, if any.
     *
     * @return the process information, or <code>null</code> if no process is bound
     */
    public final ProcessInfo getProcessInfo() {
        return this.processInfo;
    }



    /**
     * Obtains the information about when this order was received.
     *
     * @return the start date information
     */
    public final DateInfo getStartDateInfo() {
        return this.startDateInfo;
    }



    /**
     * Obtains the string that identifies the current status of the order processing.
     *
     * @return the process state
     */
    public final String getState() {
        return this.state;
    }



    /**
     * Obtains the information about the current process task that is running, or the last task
     * if the process is stopped.
     *
     * @return the task information
     */
    public final TaskInfo getTaskInfo() {
        return this.taskInfo;
    }



    /**
     * Generates JSON models for a collection of order models.
     *
     * @param modelsArray   an array that contains the order application models to export to JSON
     * @param messageSource the access to the localized application strings
     * @return an array that contains the JSON models
     */
    public static RequestJsonModel[] fromRequestModelsArray(final RequestModel[] modelsArray,
            final MessageSource messageSource) {

        if (modelsArray == null) {
            throw new IllegalArgumentException("The array of request models cannot be null.");
        }

        if (messageSource == null) {
            throw new IllegalArgumentException("The message source cannot be null.");
        }

        List<RequestJsonModel> jsonModelsList = new ArrayList<>();

        for (int modelIndex = 0; modelIndex < modelsArray.length; modelIndex++) {
            jsonModelsList.add(new RequestJsonModel(modelsArray[modelIndex], messageSource, modelIndex));
        }

        return jsonModelsList.toArray(new RequestJsonModel[]{});
    }



    /**
     * Obtains the name of the process that is bound to a given order.
     *
     * @param model         the application model for the order whose process name is requested
     * @param messageSource the access the localized application strings
     * @return the name of the process, or a default localized string if the order is not bound to a process
     */
    private String getProcessNameFromModel(final RequestModel model, final MessageSource messageSource) {
        final String processNameString = model.getProcessName();

        if (StringUtils.isNotEmpty(processNameString)) {
            return processNameString;
        }

        return String.format("##%s", messageSource.getMessage(RequestJsonModel.NO_PROCESS_KEY, null,
                Locale.getDefault()));
    }

}
