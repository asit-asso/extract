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
});