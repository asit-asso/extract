/**
 * Deletes a connector.
 * 
 * @param {int}     id      The identifier of the connector to delete
 * @param {string}  name    The name of the connector to delete (only for display purposes)
 */
function deleteConnector(id, name) {

    if (!id || isNaN(id) || id <= 0 || !name) {
        return;
    }

    var deleteConfirmTexts = LANG_MESSAGES.connectorsList.deleteConfirm;
    var alertButtonsTexts = LANG_MESSAGES.generic.alertButtons;
    var message = deleteConfirmTexts.message.replace('\{0\}', name);
    var confirmedCallback = function() {
        $('#connectorId').val(id);
        $('#connectorName').val(name);
        $('#connectorForm').submit();
    };
    showConfirm(deleteConfirmTexts.title, message, confirmedCallback, null, alertButtonsTexts.yes,
            alertButtonsTexts.no);
}


/********************* EVENT HANDLERS *********************/

$(function() {
    $('.delete-button').on('click', function() {
        var $button = $(this);
        var id = parseInt($button.attr('id').replace('deleteButton-', ''));

        if (isNaN(id)) {
            return;
        }

        var name = $button.closest('tr').find('td.nameCell > a').text();

        if (!name) {
            return;
        }

        deleteConnector(id, name);
    });
    
    // Initialize DataTable reference
    var table = $('.dataTables').DataTable();
    
    // Populate type filter dropdown with unique types from the table
    var types = [];
    table.column(1).data().unique().each(function(type) {
        if (type && types.indexOf(type) === -1) {
            types.push(type);
        }
    });
    types.sort();
    types.forEach(function(type) {
        $('#typeFilter').append('<option value="' + type + '">' + type + '</option>');
    });
    
    // Initialize select2 for type filter
    $('#typeFilter').select2({
        allowClear: true,
        width: 'resolve'
    });
    
    // Custom search function for multiple filters
    $.fn.dataTable.ext.search.push(function(settings, data, dataIndex) {
        var nameFilter = $('#textFilter').val().toLowerCase();
        var typeFilter = $('#typeFilter').val();
        
        var name = data[0].toLowerCase(); // Name column
        var type = data[1]; // Type column
        
        var nameMatch = !nameFilter || name.indexOf(nameFilter) !== -1;
        var typeMatch = !typeFilter || type === typeFilter;
        
        return nameMatch && typeMatch;
    });
    
    // Apply filters function
    function applyFilters() {
        table.draw();
    }
    
    // Handle filter inputs - apply on Enter key
    $('#textFilter').on('keypress', function(e) {
        if (e.which === 13) { // Enter key
            e.preventDefault();
            applyFilters();
        }
    });
    
    // Handle type filter change (select2) - removed auto-apply
    // Filters will only apply when search button is clicked or Enter is pressed
    
    // Handle filter button click
    $('#filterButton').on('click', function(e) {
        e.preventDefault();
        applyFilters();
    });
});