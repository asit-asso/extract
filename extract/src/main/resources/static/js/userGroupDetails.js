/**
 * @file Contains the methods to process the form used to edit or add a user group.
 * @author Yves Grasset
 */

/**
 * Sends the data about the current user group to the server for adding or updating.
 */
function submitUserGroupData() {
    var usersListIdsArray = $('#users').select2('val');
    $('#usersIds').val(usersListIdsArray.join(','));
    $('#userGroupForm').submit();
}

$(function () {

    $('#userGroupSaveButton').on('click', submitUserGroupData);

    $('.select2').select2({
        multiple: true
    });

    var usersIdsArray = $('#usersIds').val().split(',');
    $('#users').val(usersIdsArray);
    $('#users').trigger('change');

});
