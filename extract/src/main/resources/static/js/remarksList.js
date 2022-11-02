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

/**
 * Deletes a message.
 *
 * @param {int}     id      The identifier of the message to delete
 * @param {string}  title   The title of the message to delete
 */
function deleteUser(id, title) {

    if (!id || isNaN(id) || id <= 0 || !title) {
        return;
    }

    var deleteConfirmTexts = LANG_MESSAGES.remarksList.deleteConfirm;
    var alertButtonsTexts = LANG_MESSAGES.generic.alertButtons;
    var message = deleteConfirmTexts.message.replace('\{0\}', title);
    var confirmedCallback = function() {
        $('#remarkId').val(id);
        $('#remarkTitle').val(title);
        $('#remarkForm').submit();
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

        var title = $button.closest('tr').find('td.titleCell > a').text();

        if (!title) {
            return;
        }

        deleteUser(id, title);
    });
});