/**
 * @file Contains the code that is general to the application.
 * @author Yves Grasset
 */

/******************** INITIALIZATION ********************/

$(function() {
    $('[data-toggle="tooltip"]').tooltip();
});


/******************* PUBLIC FUNCTIONS *******************/

/**
 * Converts the HTML special characters in a string to prevent the markup that it may contained to be interpreted.
 * 
 * @param   {string}    text The string to escape
 * @returns {string}         The HTML-escaped string
 * @author Yves Grasset
 */
function escapeStringForHtml(text) {
  var map = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;'
  };

  return text.replace(/[&<>"']/g, function(m) { return map[m]; });
}



/**
 * Displays a modal window to show a message to the user.
 * 
 * @param {string}      title       The text displayed in the title bar of the alert window
 * @param {string}      message     The text displayed in the body of the alert window
 * @param {function}    [callback]  A function with the actions to be carried when the alert window is dismissed by the 
 *                                  user. No parameter will be passed.
 * @param {string}      [okLabel]   The text to display in the button that closes the alert
 * @author Yves Grasset
 */
function showAlert(title, message, callback, okLabel) {
    _showAlertModal(title, message, false, callback, null, okLabel, null);
}



/**
 * Displays a modal window to ask the user for a confirmation.
 * 
 * @param {string}   title            The text displayed in the title bar of the confirmation window.
 * @param {string}   message          The text displayed in the body of the confirmation window. Don't include HTML 
 *                                     markup as it will be escaped.
 * @param {Function} [callbackOk]     A function with the actions to be carried when the user closes the window with the 
 *                                    confirmation button. No parameter will be passed.
 * @param {Function} [callbackCancel] A function with the actions to be carried when the user closes the window with the 
 *                                    cancellation button. No parameter will be passed.
 * @param {string}   [okLabel]        The text to display in the confirmation button
 * @param {string}   [cancelLabel]    The text to display in the cancellation button
 * @author Yves Grasset
 */
function showConfirm(title, message, callbackOk, callbackCancel, okLabel, cancelLabel) {
    _showAlertModal(title, message, true, callbackOk, callbackCancel, okLabel, cancelLabel);
}



/******************* BACKGROUND FUNCTIONS *******************/

/**
 * Centers a Bootstrap modal on the page. This function is not meant to be called directly.
 * 
 * @param {string} modalElementId The identifier of the Bootstrap modal to center
 * @author Yves Grasset
 */
function _centerModal(modalElementId) {
    var modalSelector = '#' + modalElementId;
    
    if (!modalElementId || !$(modalSelector).hasClass('modal')) {
        return;
    }
    
    var $dialog=  $(modalSelector + ' .modal-dialog');
    var marginHeight = Math.round(($(window).height() - $dialog.height()) / 2);
    $dialog.css('margin', marginHeight + 'px auto');    
}



/**
 * Hides and reinitializes the alert or confirmation modal window. This function is not meant to be called directly.
 * 
 * @private
 * @author Yves Grasset
 */
function _hideAlertModal() {
    $('#alertTitle').text('');
    $('#alertBody').text('');

    if (LANG_MESSAGES) {
        $('#alertCancelButton').text(LANG_MESSAGES.generic.alertButtons.cancel);
        $('#alertOkButton').text(LANG_MESSAGES.generic.alertButtons.ok);
    }

    $('#alertCancelButton').off('click');
    $('#alertOkButton').off('click');
    $('#alertModal').modal('hide');
}



/**
 * Displays a modal window to show a message to the user. This function is not meant to be called directly.
 * 
 * @private
 * @param {string}   title            The text displayed in the title bar of the modal window.
 * @param {string}   message          The text displayed in the body of the modal window.
 * @param {boolean}  showCancel       True to show the cancellation button. If false, the callbackCancel parameter will
 *                                    of course be ignored.
 * @param {Function} [callbackOk]     A function with the actions to be carried when the user closes the window with the 
 *                                    confirmation button. No parameter will be passed.
 * @param {Function} [callbackCancel] A function with the actions to be carried when the user closes the window with the 
 *                                    cancellation button. No parameter will be passed.
 * @param {string}   [labelOk]        The text to display in the confirmation button
 * @param {string}   [labelCancel]    The text to display in the cancellation button
 * @author Yves Grasset
 */
function _showAlertModal(title, message, showCancel, callbackOk, callbackCancel, labelOk, labelCancel) {
    
    if (!title || !message) {
        return;
    }
    
    $('#alertTitle').text(title);
    $('#alertBody').text(message);
    
    var $cancelButton = $('#alertCancelButton');    
    
    if (showCancel) {

        if (labelCancel) {
            $cancelButton.text(labelCancel);
        }

        $cancelButton.show();
        $cancelButton.one('click', function() {
            _hideAlertModal();
            
            if (callbackCancel) {
                callbackCancel();
            }
        });
        
    } else {
        $cancelButton.hide();
        $cancelButton.off('click');
    }
    
    $('#alertOkButton').one('click', function() {
        _hideAlertModal();
        
        if (callbackOk) {
            callbackOk();
        }
    });

    if (labelOk) {
        $('#alertOkButton').text(labelOk);
    }


    $('#alertModal').one('shown.bs.modal', function() {
        _centerModal('alertModal');        
    });
    
    $('#alertModal').modal({
        'backdrop' : 'static',
        'show' : true
    });
}
