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
 * Duplicates a process.
 *
 * @param {Object}  button  The button that was clicked to trigger this method
 * @param {int}     id      The identifier of the process to duplicate
 * @param {string}  name    The name of the process to duplicate (only for display purposes)
 */
function cloneProcess(button, id, name) {
    console.log($(button).attr('data-action'));
    _executeAction(button, id, name, LANG_MESSAGES.processesList.cloneConfirm);
}



/**
 * Deletes a process.
 *
 * @param {Object}  button  The button that was clicked to trigger this method
 * @param {int}     id      The identifier of the process to delete
 * @param {string}  name    The name of the process to delete (only for display purposes)
 */
function deleteProcess(button, id, name) {
    _executeAction(button, id, name, LANG_MESSAGES.processesList.deleteConfirm)
}



/**
 * Carries an action triggered by a button click after asking for a confirmation by the user.
 *
 * @param {Object}  button         the button that was clicked to trigger the action
 * @param {int}     id             the identifier of the process that the action must carried on
 * @param {string}  name           the name of the process that the action must be carried on
 * @param {Object}  confirmTexts   the object that contains the texts to display in the confirmation box in the current
 *                                  interface language
 */
function _executeAction(button, id, name, confirmTexts) {

    if (!id || isNaN(id) || id <= 0 || !name) {
        return;
    }

    var alertButtonsTexts = LANG_MESSAGES.generic.alertButtons;
    var message = confirmTexts.message.replace('\{0\}', name);
    var confirmedCallback = function() {
        $('#processForm').attr('action', $(button).attr('data-action'));
        $('#processId').val(id);
        $('#processName').val(name);
        $('#processForm').submit();
    };

    showConfirm(confirmTexts.title, message, confirmedCallback, null, alertButtonsTexts.yes,
            alertButtonsTexts.no);
}



/**
 * Carries the appropriate actions after a button was clicked.
 * 
 * @param {Object}      button          The button that was clicked
 * @param {Function}    action          The action to execute
 */
function _handleButtonClick(button, action) {
    var $button = $(button);
    var nameMatch = /^[a-z]+\-(\d+)$/i.exec($button.attr('id'));
    
    if (nameMatch === null || nameMatch.length < 2) {
        return;
    }

    var id = parseInt(nameMatch[1]);

    if (isNaN(id)) {
        return;
    }

    var name = $button.closest('tr').find('td.nameCell > a').text();

    if (!name) {
        return;
    }

    action(button, id, name);
}


/********************* EVENT HANDLERS *********************/

$(function() {
    $('.clone-button').on('click', function() {
        _handleButtonClick(this, cloneProcess);
    });

    $('.delete-button').on('click', function() {
        _handleButtonClick(this, deleteProcess);
    });
});
