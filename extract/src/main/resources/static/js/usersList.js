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
 * Deletes a user.
 *
 * @param {int}     id      The identifier of the user to delete
 * @param {string}  login   The login name of the user to delete
 */
function deleteUser(id, login) {

    if (!id || isNaN(id) || id <= 0 || !login) {
        return;
    }

    var deleteConfirmTexts = LANG_MESSAGES.usersList.deleteConfirm;
    var alertButtonsTexts = LANG_MESSAGES.generic.alertButtons;
    var message = deleteConfirmTexts.message.replace('\{0\}', login);
    var confirmedCallback = function() {
        $('#userId').val(id);
        $('#userLogin').val(login);
        $('#userForm').submit();
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

        var login = $button.closest('tr').find('td.loginCell > a').text();

        if (!login) {
            return;
        }

        deleteUser(id, login);
    });
    
    // Initialize DataTable reference
    var table = $('.dataTables').DataTable();
    
    // Initialize select2 for all filter dropdowns WITHOUT auto-triggering
    $('#roleFilter, #stateFilter, #notificationsFilter, #2faFilter').select2({
        allowClear: true,
        width: 'resolve'
    }).on('select2:select select2:unselect', function(e) {
        // Prevent automatic filtering when selecting/unselecting
        e.stopImmediatePropagation();
    });
    
    // Custom search function for multiple filters
    $.fn.dataTable.ext.search.push(function(settings, data, dataIndex) {
        var textFilter = $('#textFilter').val().toLowerCase();
        var roleFilter = $('#roleFilter').val();
        var stateFilter = $('#stateFilter').val();
        var notificationsFilter = $('#notificationsFilter').val();
        var twoFAFilter = $('#2faFilter').val();
        
        // Text search across login, name, and email columns
        var login = data[0].toLowerCase();
        var name = data[1].toLowerCase();
        var email = data[2].toLowerCase();
        var textMatch = !textFilter || 
            login.indexOf(textFilter) !== -1 || 
            name.indexOf(textFilter) !== -1 || 
            email.indexOf(textFilter) !== -1;
        
        // Role filter (column 3)
        var roleText = $(table.row(dataIndex).node()).find('td:eq(3) div').text().trim();
        var roleMatch = !roleFilter || 
            (roleFilter === 'ADMIN' && roleText.indexOf('Admin') !== -1) ||
            (roleFilter === 'OPERATOR' && roleText.indexOf('Opérateur') !== -1);
        
        // State filter (column 5)
        var stateText = $(table.row(dataIndex).node()).find('td:eq(5) div').text().trim().toLowerCase();
        var stateMatch = true;
        if (stateFilter) {
            if (stateFilter === 'active') {
                // Exact match to avoid "inactif" containing "actif"
                stateMatch = (stateText === 'actif' || stateText === 'active');
            } else if (stateFilter === 'inactive') {
                stateMatch = (stateText === 'inactif' || stateText === 'inactive');
            }
        }
        
        // Notifications filter (column 6)
        var notifText = $(table.row(dataIndex).node()).find('td:eq(6) div').text().trim().toLowerCase();
        var notifMatch = true;
        if (notificationsFilter) {
            if (notificationsFilter === 'active') {
                // Exact match to avoid "inactif" containing "actif"
                notifMatch = (notifText === 'oui' || notifText === 'actif' || notifText === 'active' || notifText === 'yes');
            } else if (notificationsFilter === 'inactive') {
                // Exact match to avoid false positives
                notifMatch = (notifText === 'non' || notifText === 'inactif' || notifText === 'inactive' || notifText === 'no');
            }
        }
        
        // 2FA filter (column 7)
        var twoFAText = $(table.row(dataIndex).node()).find('td:eq(7) div').text().trim().toLowerCase();
        
        // Debug logging - remove when fixed
        // if (twoFAFilter) {
        //     console.log('2FA Filter:', twoFAFilter, 'Text found:', twoFAText);
        // }
        
        var twoFAMatch = !twoFAFilter || 
            (twoFAFilter === 'ACTIVE' && twoFAText === 'actif') ||
            (twoFAFilter === 'INACTIVE' && twoFAText === 'inactif') ||
            (twoFAFilter === 'STANDBY' && (twoFAText.indexOf('attente') !== -1 || twoFAText === 'en attente'));
        
        return textMatch && roleMatch && stateMatch && notifMatch && twoFAMatch;
    });
    
    // Apply filters function
    function applyFilters() {
        table.draw();
    }
    
    // Handle text filter - apply on Enter key
    $('#textFilter').on('keypress', function(e) {
        if (e.which === 13) { // Enter key
            e.preventDefault();
            applyFilters();
        }
    });
    
    // No automatic filter application on dropdown changes
    // Filters will only apply when search button is clicked or Enter is pressed
    
    // Handle filter button click
    $('#filterButton').on('click', function(e) {
        e.preventDefault();
        applyFilters();
    });
});