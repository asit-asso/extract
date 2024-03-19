/**
 * @file Contains the methods to process the form used to edit or add a user.
 * @author Yves Grasset
 */

/**
 * Sends the data about the current user to the server for adding or updating.
 */
function submitUserData() {
    $('#userForm').submit();
}

function submitUserMigrate(url) {
    var deleteConfirmTexts = LANG_MESSAGES.userDetails.migrateConfirm;
    var alertButtonsTexts = LANG_MESSAGES.generic.alertButtons;
    var confirmedCallback = function() {
        $('#userForm').attr('action', url);
        $('#userForm').submit();
    };
    showConfirm(deleteConfirmTexts.title, deleteConfirmTexts.message, confirmedCallback, null, deleteConfirmTexts.alertButtons.migrate, alertButtonsTexts.cancel);
}

function processProfileChange(switchToAdmin) {
    $('.force-2fa-usage-block').toggle(!switchToAdmin);
}

function update2faStatusButton(currentStatus, selectedAction, statusStrings, statusActionStrings) {
    var container = $('.two-factor-button-container');
    container.empty();
    var buttonTemplate = '<a class="btn btn-sm dropdown-toggle" data-bs-toggle="dropdown"></a>';
    var buttonTextTemplate = '<span></span>';
    var dropDownListTemplate = '<ul class="dropdown-menu" role="menu"></ul>';
    var dropDownItemTemplate = '<li class="dropdown-item" role="menuitem"></li>';
    var statusClass;
    var buttonText;
    var itemTexts = [];
    var itemClasses = [];
    var itemActions = [];


    switch(currentStatus) {

        case 'ACTIVE':

            switch (selectedAction) {

                case 'INACTIVE':
                    buttonText = statusActionStrings[selectedAction];
                    statusClass = 'btn-danger';
                    itemTexts.push(statusStrings.ACTIVE, statusActionStrings.STANDBY);
                    itemClasses.push('text-success', 'text-warning');
                    itemActions.push('ACTIVE', 'STANDBY');
                    break;

                case 'STANDBY':
                    buttonText = statusActionStrings[selectedAction];
                    statusClass = 'btn-warning';
                    itemTexts.push(statusStrings.ACTIVE, statusActionStrings.INACTIVE);
                    itemClasses.push('text-success', 'text-danger');
                    itemActions.push('ACTIVE', 'INACTIVE');
                    break;

                default:
                    buttonText = statusStrings[currentStatus];
                    statusClass = 'btn-extract-filled';
                    itemTexts.push(statusActionStrings.STANDBY, statusActionStrings.INACTIVE);
                    itemClasses.push('text-warning', 'text-danger');
                    itemActions.push('STANDBY', 'INACTIVE');
                    selectedAction = currentStatus;
                    break;
            }
            break;

        case 'INACTIVE':

            if (selectedAction === 'ACTIVE') {
                buttonText = statusActionStrings[selectedAction];
                statusClass = 'btn-extract-filled';
                itemTexts.push(statusStrings.INACTIVE);
                itemClasses.push('text-danger');
                itemActions.push('INACTIVE');

            } else {
                buttonText = buttonText = statusStrings[currentStatus];;
                statusClass = 'btn-danger';
                itemTexts.push(statusActionStrings.ACTIVE);
                itemClasses.push('text-success');
                itemActions.push('ACTIVE');
                selectedAction = currentStatus;
            }
            break;

        case 'STANDBY':

            if (selectedAction === 'INACTIVE') {
                buttonText = statusActionStrings[selectedAction];
                statusClass = 'btn-danger';
                itemTexts.push(statusStrings.STANDBY);
                itemClasses.push('text-warning');
                itemActions.push('STANDBY');

            } else {
                buttonText = buttonText = statusStrings[currentStatus];;
                statusClass = 'btn-warning';
                itemTexts.push(statusActionStrings.INACTIVE);
                itemClasses.push('text-danger');
                itemActions.push('INACTIVE');
                selectedAction = currentStatus;
            }
            break;
    }

    var buttonElement = $(buttonTemplate);
    buttonElement.addClass(statusClass);
    var buttonTextElement = $(buttonTextTemplate);
    buttonTextElement.text(buttonText);
    buttonElement.append(buttonTextElement);
    container.append(buttonElement);

    var dropDownElement = $(dropDownListTemplate);
    container.append(dropDownElement);

    for (var itemIndex = 0; itemIndex < itemTexts.length; itemIndex++) {
        var dropDownItemElement = $(dropDownItemTemplate);
        dropDownItemElement.addClass(itemClasses[itemIndex]);
        var dropDownLinkElement = $('<a></a>');
        dropDownLinkElement.text(itemTexts[itemIndex]);
        dropDownLinkElement.attr('data-action', itemActions[itemIndex]);
        dropDownItemElement.append(dropDownLinkElement);
        dropDownElement.append(dropDownItemElement);
        dropDownLinkElement.on('click', function() {
            update2faStatusButton(currentStatus, $(this).attr('data-action'), statusStrings, statusActionStrings);
        });
    }

    $('#twoFactorStatus').val(selectedAction);
}
