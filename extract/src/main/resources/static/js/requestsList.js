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

var REQUESTS_LIST_CONNECTOR_STATUS_OK = "OK";
var REQUESTS_LIST_CONNECTOR_STATUS_ERROR = "ERROR";


function addSortAndSearchInfo(data) {
    _addSortInfo(data);
    _addSearchInfo(data);
}



/**
 * Sets the event handlers to open the details of the request displayed in a table row.
 */
function defineRowClick() {
    $('.request-row').on('click', function() {
        viewRequestDetails(this);
    });
    $('.request-row a.request-link').each(function() {
        $(this).attr("href", _getRequestUrlForRow($(this).closest("tr.request-row")));
        $(this).on("click", function(event) {
            event.stopPropagation();
        });
    });
}



/**
 * Starts the display of the connectors state updated at a given interval.
 *
 * @param {String}  connectorsDivId the identifier of the HTML element that must contain the connectors state
 *                                   information
 * @param {String}  ajaxUrl         the URL to send the connector state data requests to
 * @param {Integer} refreshInterval the number of seconds between two queries for the connectors state data
 */
function loadConnectors(connectorsDivId, ajaxUrl, refreshInterval) {

    if (!ajaxUrl) {
        return;
    }

    var interval = parseInt(refreshInterval);

    if (isNaN(interval) || interval < 10) {
        return;
    }

    if (!connectorsDivId) {
        return;
    }

    var $connectorsDiv = $("#" + connectorsDivId);

    if (!$connectorsDiv) {
        return;
    }

    _connectorsDiv = $connectorsDiv;
    _connectorsUrl = ajaxUrl;

    _refreshConnectorsState();
    setInterval(function() {
        _refreshConnectorsState();
    }, refreshInterval * 1000);
}



/**
 * Transforms the controls with the appropriate CSS classes into fields that help selecting a date.
 *
 * @param {String} language the ISO code of the language to display the calendar information in
 */
function loadDatepickers(language) {

    $(".datepicker").datepicker({
        clearBtn : true,
        format : "yyyy-mm-dd",
        language : language,
        todayHighlight : true
    });
}



/**
 * Transforms an HTML element into a table that displays requests data.
 *
 * @param {String}  tableId         the identifier of the HTML element that must contain the table
 * @param {String}  ajaxUrl         the URL to ask for requests data
 * @param {Integer} refreshInterval the number of seconds between table data refreshes
 * @param {Boolean} withPaging      <code>true</code> if the table must separate the data in pages
 * @param {Boolean} withSearching   <code>true</code> if the table must allow filtering its information
 * @param {Boolean} isServerSide    <code>true</code> if the filtering, sorting and paging must be done by the server
 * @param {Integer} pagingSize      the number of items to display in a page. This value is ignored if the
 *                                  <code>withPaging</code> attribute is <code>false</code>
 * @returns {DataTable} the created table object
 */
function loadRequestsTable(tableId, ajaxUrl, refreshInterval, withPaging, withSearching, isServerSide, pagingSize,
        dataFunction) {
    var configuration = _getRequestsTableConfiguration(ajaxUrl, withPaging, withSearching, isServerSide, pagingSize,
            dataFunction);
    var $table = $('#' + tableId);

    if (!$table) {
        console.log("ERROR - No table found with the given identifier.");
    }

    var requestsTable = $table.DataTable(configuration);

    if (refreshInterval) {
        setInterval(function() {
            requestsTable.ajax.reload(null, false);
        }, refreshInterval * 1000);
    }

    return requestsTable;
}



/**
 *
 *
 *
 */
function loadWorkingState(scheduledStopDivId, stoppedDivId, scheduleConfigErrorDivId, ajaxUrl, refreshInterval) {

    if (!ajaxUrl) {
        return;
    }

    var interval = parseInt(refreshInterval);

    if (isNaN(interval) || interval < 10) {
        return;
    }

    if (!scheduledStopDivId || !stoppedDivId) {
        return;
    }

    var $scheduledStopDiv = $("#" + scheduledStopDivId);

    if (!$scheduledStopDiv) {
        return;
    }

    var $stoppedDiv = $("#" + stoppedDivId);

    if (!$stoppedDiv) {
        return;
    }

    var $scheduleConfigErrorDiv = $("#" + scheduleConfigErrorDivId);

    if (!$scheduleConfigErrorDiv) {
        return;
    }

    _scheduledStopDiv = $scheduledStopDiv;
    _stoppedDiv = $stoppedDiv;
    _scheduleConfigErrorDiv = $scheduleConfigErrorDiv;
    _workingStateUrl = ajaxUrl;

    _refreshWorkingState();
    setInterval(function() {
        _refreshWorkingState();
    }, refreshInterval * 1000);
}



/**
 * Sets the values that filter the records displayed in the tables with search enabled from the fields in the search
 * form.
 */
function updateFilterValues() {
    _filterText = $("#textFilter").val().toLowerCase();
    _filterProcess = $("#processFilter").val();
    _filterConnector = $("#connectorFilter").val();
    _filterStartDateFrom = new Date($("#startDateFromFilter").val()).getTime() || '';
    _filterStartDateTo = new Date($("#startDateToFilter").val()).getTime() || '';
}




/**
 * Opens the details of a request.
 *
 * @param {Object}  row     The table row that contains the details of the request to open
 */
function viewRequestDetails(row) {

    if (!row) {
        console.log("ERROR - The request row is not valid.");
        return;
    }

    location.href = _getRequestUrlForRow(row);
}



/******************** PRIVATE INTERFACE *******************/

/**
 * The URL to send the requests for connectors state data to.
 *
 * @type String
 */
var _connectorsUrl;

/**
 * The identifier of the HTML element that contains the connectors state information.
 *
 * @type String
 */
var _connectorsDiv;

/**
 * The value to use to filter the records based on the supported text fields.
 *
 * @type String
 */
var _filterText = "";

/**
 * The identifier of the connector whose requests must be displayed. (The empty string displays the requests for all
 * connectors.)
 *
 * @type String
 */
var _filterConnector = "";

/**
 * The identifier of the process whose requests must be displayed. (The empty string displays the requests for all
 * processes.)
 *
 * @type String
 */
var _filterProcess = "";

/**
 * The date from which the requests must be displayed (based on their creation date).
 *
 * @type Date
 */
var _filterStartDateFrom = "";

/**
 * The date up until which the requests must be displayed (based on their creation date).
 *
 * @type Date
 */
var _filterStartDateTo = "";

var _scheduledStopDiv;

var _stoppedDiv;

var _scheduleConfigErrorDiv;

var _workingStateUrl;

function _addSearchInfo(data) {
    data.filterText = _filterText;
    data.filterConnector = _filterConnector;
    data.filterProcess = _filterProcess;
    data.filterDateFrom = _filterStartDateFrom;
    data.filterDateTo = _filterStartDateTo;

    return data;
}



function _addSortInfo(data) {
    var orderInfo = data.order[0];
    var originalDirection = orderInfo['dir'];

    switch (orderInfo['column']) {

        case 2:
            data.sortFields = 'endDate';
            data.sortDirection = _getSortDirection(originalDirection, true);
            break;

        case 3:
            data.sortFields = 'orderLabel,productLabel';
            data.sortDirection = _getSortDirection(originalDirection);
            break;

        case 4:
            data.sortFields = 'client';
            data.sortDirection = _getSortDirection(originalDirection);
            break;

        case 5:
            data.sortFields = 'process.name';
            data.sortDirection = _getSortDirection(originalDirection);
            break;

        case 6:
            data.sortFields = 'startDate';
            data.sortDirection = _getSortDirection(originalDirection, true);
            break;

        default:
            data.sortFields = 'endDate';
            data.sortDirection = 'desc';
    }

    return data;
}


/**
 * Adds a drop-down element to the connectors state container for the connectors in a given state.
 *
 * @param {Array}   connectorsInfo  an array containing the informations for each connector to display
 * @param {String}  state           the state of the connectors to display in the drop-down element. Currently supports
 *                                   "OK" and "ERROR"
 */
function _createConnectorsDropDown(connectorsInfo, state) {

    if (!connectorsInfo || connectorsInfo.length === 0) {
        return;
    }

    var isError = (state === REQUESTS_LIST_CONNECTOR_STATUS_ERROR);
    var connectorDropDown = $('<div class="dropdown"></div>');
    var itemId = ((isError) ? "failed" : "ok") + "ConnectorsDropDown";
    var connectorLink = $('<a id="' + itemId + '" class="connector-state dropdown-toggle" data-toggle="dropdown"></a>');
    connectorLink.addClass((isError) ? 'connector-state-error' : 'connector-state-success');

    if (isError) {
        connectorLink.append('<i class="fa fa-exclamation-triangle text-danger"></i>&nbsp;');
    }

    connectorLink.append('<i class="fa fa-plug"></i>&nbsp;');
    var textSpan = $('<span></span>');
    textSpan.text(connectorsInfo.length);
    connectorLink.append(textSpan);
    connectorLink.append('<span class="caret"></span>');
    connectorDropDown.append(connectorLink);

    var connectorMenu = $('<ul class="dropdown-menu" role="menu" aria-labelledby="' + itemId + '"></ul>');

    for (var itemIndex = 0; itemIndex < connectorsInfo.length; itemIndex++) {
        var itemData = connectorsInfo[itemIndex];
        var connectorItem = $('<li class="dropdown-item" role="presentation"></li>');
        var itemLink = $('<a role="menuitem"></a>');
        itemLink.attr('title', itemData.stateMessage);
        itemLink.attr('href', (itemData.url) ? itemData.url : '#');
        itemLink.text(itemData.name);
        connectorItem.append(itemLink);
        connectorMenu.append(connectorItem);
    }

    connectorDropDown.append(connectorMenu);
    _connectorsDiv.append(connectorDropDown);
    connectorMenu.children("a").tooltip();
}



/**
 * Obtains the URL that shows the details of the request displayed in a given table row.
 *
 * @param {Object} row the table row object that displays the request
 * @returns {String} the request details URL
 */
function _getRequestUrlForRow(row) {
    return (row) ? $(row).attr('data-href') : "#";
}



/**
 * Obtains the information that defines how to display and sort the data in the requests tables.
 *
 * @returns {Array} the table columns configuration
 */
function _getRequestsTableColumnsConfiguration() {
    return [
        {
            "targets" : 0,
            "data" : "index",
            "visible" : false
        },
        {
            "targets" : 1,
            "data" : "state",
            "render" : function(data) {
                var stateClass;

                switch (data) {
                    case "error":
                        stateClass = "fa-exclamation-triangle text-danger";
                        break;

                    case "finished":
                        stateClass = "fa-check text-muted";
                        break;

                    case "rejected":
                        stateClass = "fa-times text-muted";
                        break;

                    case "standby":
                        stateClass = "fa-user text-warning";
                        break;

                    default:
                        stateClass = "fa-cog text-success";
                        break;
                }

                return '<i class="fa fa-2x fa-fw ' + stateClass + '"></i>';
            },
            "orderable" : false,
            "width" : "30px"
        },
        {
            "targets" : 2,
            "data" : "taskInfo",
            "render" : {
                "_" : function(data) {
                    return "<div><a href=\"#\" class=\"request-link\"><b>" + data.taskLabel + "</b></a></div><div>"
                            + data.taskDateInfo.dateText + "</div>";
                },
                "sort" : function(data) {
                    return _getTimestampStringSortValue(data.taskDateInfo.timestamp);
                }
            },
            "width" : "140px"
        },
        {
            "targets" : 3,
            "data" : "orderInfo",
            "render" : {
                "_" : function(data) {
                    return "<div><a href=\"#\" class=\"request-link\"><b>" + data.orderLabel + "</b></a> "
                            + "<span class=\"connector-name\">" + data.connectorName + "</span></div><div>"
                            + data.productLabel + "</div>";
                },
                "sort" : function(data) {
                    return data.orderLabel.toLowerCase() + "þþþ" + data.productLabel.toLowerCase();
                }
            }
        },
        {
            "targets" : 4,
            "data" : "customerName"
        },
        {
            "targets" : 5,
            "data" : "processInfo",
            "render" : function(data) {
                var processName = data.name;

                if (processName.substring(0, 2) === "##") {
                    processName = "<i>" + processName.substring(2) + "</i>";
                }

                return processName;
            },
            "width" : "140px"
        },
        {
            "targets" : 6,
            "data" : "startDateInfo",
            "render" : {
                "_" : "dateText",
                "sort" : function(data) {
                    return _getTimestampStringSortValue(data.timestamp);
                }
            },
            "width" : "100px"
        }
    ];

}



/**
 * Obtains the information that defines how to display the requests tables.
 *
 * @param {String}  ajaxUrl       the URL to call to refresh the table data
 * @param {Boolean} withPaging    <code>true</code> if the table must separate the data in pages
 * @param {Boolean} withSearching <code>true</code> if the table must allow filtering its information
 * @param {Boolean} isServerSide  <code>true</code> if the filtering, sorting and paging must be done by the server
 * @param {Integer} pagingSize    the number of items to display in a page. This value is ignored if the
 *                                <code>withPaging</code> attribute is <code>false</code>
 * @param {Function} dataFunction a function that enriches the data object that is passed to the server
 * @returns {Object} the table configuration object
 */
function _getRequestsTableConfiguration(ajaxUrl, withPaging, withSearching, isServerSide, pagingSize,
        dataFunction) {
    var tableProperties = getDataTableBaseProperties();
    tableProperties.paging = withPaging;
    tableProperties.searching = withSearching;
    tableProperties.serverSide = isServerSide;
    tableProperties.order = [[2, 'asc']];
    var pageLength = parseInt(pagingSize);

    if (!isNaN(pageLength) && pageLength > 0) {
        tableProperties.pageLength = pageLength;
    }

    tableProperties.ajax = {
        url : ajaxUrl,
        type : "GET",
        data : dataFunction
    };
    tableProperties.columnDefs = _getRequestsTableColumnsConfiguration();

    return tableProperties;
}



/**
 * Obtains a string that describe in which direction (ascending or descending) the data must be sorted.
 *
 * @param {String} originalDirection the direction string that was passed by the data table
 * @param {bolean} isInverted whether the direction should be switched for the given field
 * @returns {String} <code>"asc"</code> for ascending or <code>"desc"</code> for descending
 */
function _getSortDirection(originalDirection, isInverted) {
    var isDescending = (originalDirection.toLowerCase() === "desc");

    if (isInverted) {
        isDescending = !isDescending;
    }

    return (isDescending) ? "desc" : "asc";
}



/**
 * Obtains a value that allows to sort timestamp when a textual sort is used.
 *
 * @param {Integer} timestamp the timestamp to sort
 * @returns {String} the textual sort value
 */
function _getTimestampStringSortValue(timestamp) {

    if (timestamp === null || isNaN(timestamp)) {
        return "";
    }

    var currentTimeStamp = new Date().getTime();
    var differenceString = new String(currentTimeStamp - timestamp);

    if (differenceString.length > 15) {
        return differenceString;
    }

    return "0".repeat(15 - differenceString.length) + differenceString;
}



/**
 * Makes the background of a connector in error blink.
 */
function _pulseConnectorError() {
    $(".connector-state-error").delay(200).animate({
        opacity : 0.5
    },
            'slow').delay(50).animate({
        opacity : 1.0
    },
            'slow');
}



/**
 * Requests current information about the connectors state.
 */
function _refreshConnectorsState() {

    if (!_connectorsUrl || !_connectorsDiv) {
        return;
    }

    $.ajax(_connectorsUrl, {
        cache : false,
        error : function() {
            alert("ERROR - Could not fetch the connectors information.");
        },
        success : function(data) {

            if (!data || !Array.isArray(data)) {
                alert("ERROR - Could not fetch the connectors information.");
            }

            _updateConnectorsState(data);
        }
    });
}



/**
 * Requests current information about the connectors state.
 */
function _refreshWorkingState() {

    if (!_workingStateUrl || !_scheduledStopDiv || !_stoppedDiv || !_scheduleConfigErrorDiv) {
        return;
    }

    $.ajax(_workingStateUrl, {
        cache : false,
        error : function() {
            alert("ERROR - Could not fetch the working state information.");
        },
        success : function(data) {

            if (!data) {
                alert("ERROR - Could not fetch the working state information.");
            }

            _scheduledStopDiv.toggle(data === "SCHEDULED_STOP");
            _stoppedDiv.toggle(data === "STOPPED");
            _scheduleConfigErrorDiv.toggle(data === "SCHEDULE_CONFIG_ERROR");
        }
    });
}



/**
 * Modifies the objects that show the current state of the connectors based on the received data.
 *
 * @param {Array} connectorsInfo the current state data for each active connector
 */
function _updateConnectorsState(connectorsInfo) {

    if (!connectorsInfo || !_connectorsDiv) {
        return;
    }

    _connectorsDiv.empty();

    var okConnectors = [];
    var errorConnectors = [];

    for (var itemIndex = 0; itemIndex < connectorsInfo.length; itemIndex++) {
        var itemData = connectorsInfo[itemIndex];

        if (itemData.inError) {
            errorConnectors.push(itemData);
        } else {
            okConnectors.push(itemData);
        }
    }

    _createConnectorsDropDown(errorConnectors, REQUESTS_LIST_CONNECTOR_STATUS_ERROR);
    _createConnectorsDropDown(okConnectors, REQUESTS_LIST_CONNECTOR_STATUS_OK);
}



/********************* EVENT HANDLERS *********************/

$(function() {
    $(".select2").select2({
        allowClear : true
    });

    _pulseConnectorError();
    setInterval(function() {
        _pulseConnectorError();
    }, 250);
});