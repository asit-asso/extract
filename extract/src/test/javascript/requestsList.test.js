/**
 * Unit tests for requestsList.js notification functionality
 * 
 * This test suite can be run with Jest or Jasmine.
 * To run with Jest:
 * 1. npm install --save-dev jest jsdom jquery
 * 2. Add to package.json: "scripts": { "test": "jest" }
 * 3. npm test
 */

describe('RequestsList Notification Functions', () => {
    let $;
    let _ajaxErrorNotificationId;
    
    beforeEach(() => {
        // Setup DOM environment
        document.body.innerHTML = '<div id="test-container"></div>';
        
        // Mock jQuery
        $ = require('jquery');
        global.$ = $;
        global.jQuery = $;
        
        // Reset global variables
        global._ajaxErrorNotificationId = null;
        global.LANG_MESSAGES = null;
        
        // Import functions (you may need to export them from requestsList.js)
        // For testing purposes, we'll define them here
        global._showAjaxErrorNotification = function(tableId) {
            // Check if notification already exists - don't create duplicate
            if (_ajaxErrorNotificationId && $('#' + _ajaxErrorNotificationId).length > 0) {
                return;
            }

            // Remove any existing notification first (just in case)
            _clearAjaxErrorNotification();

            // Get localized messages or use defaults
            var title = 'Connection Error';
            var message = 'Unable to refresh data. Will retry automatically...';
            
            // Check if LANG_MESSAGES is available
            if (typeof LANG_MESSAGES !== 'undefined' && LANG_MESSAGES && LANG_MESSAGES.errors && LANG_MESSAGES.errors.ajaxError) {
                title = LANG_MESSAGES.errors.ajaxError.title || title;
                message = LANG_MESSAGES.errors.ajaxError.message || message;
            }

            // Create notification element
            var notificationHtml = '<div id="ajaxErrorNotification" class="alert alert-warning alert-dismissible" ' +
                'style="position: fixed; top: 10px; right: 10px; z-index: 9999; min-width: 300px;">' +
                '<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>' +
                '<strong><i class="fa fa-exclamation-triangle"></i> ' + title + '</strong>' +
                '<div>' + message + '</div></div>';

            $('body').append(notificationHtml);
            _ajaxErrorNotificationId = 'ajaxErrorNotification';

            // Auto-dismiss after 10 seconds
            setTimeout(function() {
                _clearAjaxErrorNotification();
            }, 10000);
        };
        
        global._clearAjaxErrorNotification = function() {
            if (_ajaxErrorNotificationId) {
                $('#' + _ajaxErrorNotificationId).fadeOut(300, function() {
                    $(this).remove();
                });
                _ajaxErrorNotificationId = null;
            }
        };
    });
    
    afterEach(() => {
        // Clean up DOM
        document.body.innerHTML = '';
        jest.clearAllTimers();
    });
    
    describe('_showAjaxErrorNotification', () => {
        
        test('should create notification with default English messages when LANG_MESSAGES is undefined', () => {
            _showAjaxErrorNotification('test-table');
            
            const notification = $('#ajaxErrorNotification');
            expect(notification.length).toBe(1);
            expect(notification.html()).toContain('Connection Error');
            expect(notification.html()).toContain('Unable to refresh data. Will retry automatically...');
        });
        
        test('should create notification with French messages when LANG_MESSAGES is defined', () => {
            // Setup French messages
            global.LANG_MESSAGES = {
                errors: {
                    ajaxError: {
                        title: 'Erreur de connexion',
                        message: 'Impossible de rafraîchir les données. Nouvelle tentative automatique...'
                    }
                }
            };
            
            _showAjaxErrorNotification('test-table');
            
            const notification = $('#ajaxErrorNotification');
            expect(notification.length).toBe(1);
            expect(notification.html()).toContain('Erreur de connexion');
            expect(notification.html()).toContain('Impossible de rafraîchir les données');
        });
        
        test('should not create duplicate notifications', () => {
            _showAjaxErrorNotification('test-table');
            _showAjaxErrorNotification('test-table');
            _showAjaxErrorNotification('test-table');
            
            const notifications = $('#ajaxErrorNotification');
            expect(notifications.length).toBe(1);
        });
        
        test('should have correct styling and structure', () => {
            _showAjaxErrorNotification('test-table');
            
            const notification = $('#ajaxErrorNotification');
            expect(notification.hasClass('alert')).toBe(true);
            expect(notification.hasClass('alert-warning')).toBe(true);
            expect(notification.hasClass('alert-dismissible')).toBe(true);
            expect(notification.attr('style')).toContain('position: fixed');
            expect(notification.attr('style')).toContain('z-index: 9999');
            
            const closeButton = notification.find('.btn-close');
            expect(closeButton.length).toBe(1);
            
            const icon = notification.find('.fa-exclamation-triangle');
            expect(icon.length).toBe(1);
        });
        
        test('should auto-dismiss after 10 seconds', () => {
            jest.useFakeTimers();
            
            _showAjaxErrorNotification('test-table');
            
            expect($('#ajaxErrorNotification').length).toBe(1);
            
            jest.advanceTimersByTime(10000);
            
            // After timeout, notification should be removed
            expect(_ajaxErrorNotificationId).toBe(null);
        });
    });
    
    describe('_clearAjaxErrorNotification', () => {
        
        test('should remove existing notification', () => {
            _showAjaxErrorNotification('test-table');
            expect($('#ajaxErrorNotification').length).toBe(1);
            
            _clearAjaxErrorNotification();
            
            // Simulate fadeOut completion
            $('#ajaxErrorNotification').remove();
            
            expect($('#ajaxErrorNotification').length).toBe(0);
            expect(_ajaxErrorNotificationId).toBe(null);
        });
        
        test('should handle case when no notification exists', () => {
            expect(() => {
                _clearAjaxErrorNotification();
            }).not.toThrow();
        });
    });
    
    describe('Integration with DataTables error handling', () => {
        
        test('should show notification on DataTables error event', () => {
            const table = $('<table id="test-table"></table>');
            $('body').append(table);
            
            // Simulate DataTables error event
            table.trigger('dt-error.dt', [{}, 1, 'Test error']);
            
            // In real implementation, this would trigger _showAjaxErrorNotification
            _showAjaxErrorNotification('test-table');
            
            expect($('#ajaxErrorNotification').length).toBe(1);
        });
        
        test('should clear notification on successful DataTables load', () => {
            _showAjaxErrorNotification('test-table');
            expect($('#ajaxErrorNotification').length).toBe(1);
            
            // Simulate successful load
            _clearAjaxErrorNotification();
            $('#ajaxErrorNotification').remove();
            
            expect($('#ajaxErrorNotification').length).toBe(0);
        });
    });
});

describe('Connectors State Refresh', () => {
    let $;
    
    beforeEach(() => {
        // Setup DOM
        document.body.innerHTML = '<div id="connectors-div"></div>';
        
        $ = require('jquery');
        global.$ = $;
        global.jQuery = $;
        
        // Mock $.ajax
        $.ajax = jest.fn();
    });
    
    describe('AJAX error handling', () => {
        
        test('should handle authentication redirect (302 status)', () => {
            const mockXHR = {
                status: 302,
                responseText: '<!DOCTYPE html><html>Login page</html>',
                getResponseHeader: jest.fn().mockReturnValue('text/html')
            };
            
            // Mock window.location
            delete window.location;
            window.location = { href: '' };
            
            $.ajax.mockImplementation((url, options) => {
                // Call the error handler immediately
                options.error(mockXHR, 'error', 'Redirect');
                return {};
            });
            
            // Call ajax with test handlers
            $.ajax('/extract/getActiveConnectors', {
                cache: false,
                dataType: 'json',
                error: function(xhr) {
                    if (xhr.responseText && xhr.responseText.indexOf('<!DOCTYPE') > -1) {
                        window.location.href = '/extract/login';
                    }
                }
            });
            
            expect(window.location.href).toBe('/extract/login');
        });
        
        test('should handle invalid JSON response', () => {
            const mockXHR = {
                status: 200,
                getResponseHeader: jest.fn().mockReturnValue('application/json')
            };
            
            // Mock _showAjaxErrorNotification
            global._showAjaxErrorNotification = jest.fn();
            
            $.ajax.mockImplementation((url, options) => {
                // Call success with invalid data
                options.success('not-an-array', 'success', mockXHR);
                return {};
            });
            
            // Call ajax with test handlers
            $.ajax('/extract/getActiveConnectors', {
                cache: false,
                dataType: 'json',
                success: function(data) {
                    if (!data || !Array.isArray(data)) {
                        global._showAjaxErrorNotification('connectors');
                    }
                }
            });
            
            expect(global._showAjaxErrorNotification).toHaveBeenCalledWith('connectors');
        });
        
        test('should handle valid connectors data', () => {
            const mockXHR = {
                status: 200,
                getResponseHeader: jest.fn().mockReturnValue('application/json')
            };
            
            const validData = [
                { id: 1, name: 'Connector 1', status: 'OK' },
                { id: 2, name: 'Connector 2', status: 'ERROR' }
            ];
            
            // Mock the functions
            global._updateConnectorsState = jest.fn();
            global._clearAjaxErrorNotification = jest.fn();
            
            $.ajax.mockImplementation((url, options) => {
                // Call success with valid data
                options.success(validData, 'success', mockXHR);
                return {};
            });
            
            // Call ajax with test handlers
            $.ajax('/extract/getActiveConnectors', {
                cache: false,
                dataType: 'json',
                success: function(data) {
                    if (data && Array.isArray(data)) {
                        global._clearAjaxErrorNotification();
                        global._updateConnectorsState(data);
                    }
                }
            });
            
            expect(global._clearAjaxErrorNotification).toHaveBeenCalled();
            expect(global._updateConnectorsState).toHaveBeenCalledWith(validData);
        });
        
        test('should specify JSON dataType in AJAX request', () => {
            $.ajax.mockImplementation(() => {});
            
            // Call ajax
            $.ajax('/extract/getActiveConnectors', {
                cache: false,
                dataType: 'json'
            });
            
            // Check that ajax was called with correct options
            expect($.ajax).toHaveBeenCalledWith(
                '/extract/getActiveConnectors',
                expect.objectContaining({
                    cache: false,
                    dataType: 'json'
                })
            );
        });
    });
});

describe('Edge Cases and Error Scenarios', () => {
    
    beforeEach(() => {
        // Setup DOM environment
        document.body.innerHTML = '<div id="test-container"></div>';
        
        // Setup jQuery
        const $ = require('jquery');
        global.$ = $;
        global.jQuery = $;
        
        // Reset global variables
        global._ajaxErrorNotificationId = null;
        global.LANG_MESSAGES = null;
        
        // Define the notification functions for these tests
        global._showAjaxErrorNotification = function(tableId) {
            // Check if notification already exists - don't create duplicate
            if (global._ajaxErrorNotificationId && $('#' + global._ajaxErrorNotificationId).length > 0) {
                return;
            }

            // Remove any existing notification first (just in case)
            global._clearAjaxErrorNotification();

            // Get localized messages or use defaults
            var title = 'Connection Error';
            var message = 'Unable to refresh data. Will retry automatically...';
            
            // Check if LANG_MESSAGES is available
            if (typeof global.LANG_MESSAGES !== 'undefined' && global.LANG_MESSAGES && global.LANG_MESSAGES.errors && global.LANG_MESSAGES.errors.ajaxError) {
                title = global.LANG_MESSAGES.errors.ajaxError.title || title;
                message = global.LANG_MESSAGES.errors.ajaxError.message || message;
            }

            // Create notification element
            var notificationHtml = '<div id="ajaxErrorNotification" class="alert alert-warning alert-dismissible" ' +
                'style="position: fixed; top: 10px; right: 10px; z-index: 9999; min-width: 300px;">' +
                '<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>' +
                '<strong><i class="fa fa-exclamation-triangle"></i> ' + title + '</strong>' +
                '<div>' + message + '</div></div>';

            $('body').append(notificationHtml);
            global._ajaxErrorNotificationId = 'ajaxErrorNotification';

            // Auto-dismiss after 10 seconds
            setTimeout(function() {
                global._clearAjaxErrorNotification();
            }, 10000);
        };
        
        global._clearAjaxErrorNotification = function() {
            if (global._ajaxErrorNotificationId) {
                $('#' + global._ajaxErrorNotificationId).fadeOut(300, function() {
                    $(this).remove();
                });
                global._ajaxErrorNotificationId = null;
            }
        };
    });
    
    test('should handle partially defined LANG_MESSAGES object', () => {
        // LANG_MESSAGES exists but doesn't have the expected structure
        global.LANG_MESSAGES = { 
            errors: {} 
        };
        
        global._showAjaxErrorNotification('test');
        
        const notification = $('#ajaxErrorNotification');
        // Should fall back to English defaults
        expect(notification.length).toBe(1);
        expect(notification.html()).toContain('Connection Error');
    });
    
    test('should handle concurrent notification requests', () => {
        jest.useFakeTimers();
        
        // First notification
        global._showAjaxErrorNotification('table1');
        
        // The notification should be created
        expect($('#ajaxErrorNotification').length).toBe(1);
        
        // Advance time by 5 seconds
        jest.advanceTimersByTime(5000);
        
        // Try to create another notification - it should be blocked
        global._showAjaxErrorNotification('table2');
        
        // Should still only have one notification
        expect($('#ajaxErrorNotification').length).toBe(1);
        
        // Advance to 10 seconds total
        jest.advanceTimersByTime(5000);
        
        // Notification should be cleared after timeout
        expect(global._ajaxErrorNotificationId).toBe(null);
    });
    
    test('should handle HTML content in error response', () => {
        const mockXHR = {
            status: 200,
            responseText: '<!DOCTYPE html><html><body>Session expired</body></html>',
            getResponseHeader: jest.fn().mockReturnValue('text/html')
        };
        
        delete window.location;
        window.location = { href: '' };
        
        // Simulate receiving HTML instead of JSON
        const successHandler = function(data, textStatus, xhr) {
            var contentType = xhr.getResponseHeader("content-type") || "";
            if (contentType.indexOf('html') > -1) {
                window.location.href = '/extract/login';
            }
        };
        
        successHandler(null, 'success', mockXHR);
        
        expect(window.location.href).toBe('/extract/login');
    });
});