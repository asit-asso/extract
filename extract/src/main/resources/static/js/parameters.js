/**
 * @file Contains the methods to process the form used to edit parameters.
 * @author Florent Krin
 */

/**
 * Sends the data about the parameters to the server for updating.
 */
function submitParametersData() {
    $('#parametersForm').submit();
}



function loadTimePickers() {
    $(".timepicker").timepicker({
        className: "form-control",
        show2400: true,
        step: 30,
        timeFormat: "H:i",
        useSelect: true
    });
}

function addOrchestratorTimeRange() {
    $("#parametersForm").attr("action", $(this).attr("href"));
    $("#parametersForm").submit();
}

function removeOrchestratorTimeRange() {
    $("#parametersForm").attr("action", $(this).attr("href"));
    $("#parametersForm").submit();
}
