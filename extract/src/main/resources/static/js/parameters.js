/**
 * @file Contains the methods to process the form used to edit parameters.
 * @author Florent Krin
 */

/**
 * Sends the data about the parameters to the server for updating.
 */
function submitParametersData() {
    $('#validationFocusProperties').val($('#validation-properties-select').val().join(','));
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

$(function() {
    $(".properties-select.select2").select2({
        multiple:true,
        tags: true
    });

    var propertiesString = $('#validationFocusProperties').val();

    if (propertiesString) {
        var propertiesArray = propertiesString.split(',');

        for (var tagIndex = 0; tagIndex < propertiesArray.length; tagIndex++) {
            var property = propertiesArray[tagIndex];
            $('#validation-properties-select').append(new Option(property, property, false, true));
        }

        $('#validation-properties-select').trigger('change');
    }
});
