/**
 * Jest setup file - runs before all tests
 */

// Setup jQuery globally
const $ = require('jquery');
global.$ = $;
global.jQuery = $;

// Mock window.alert
global.alert = jest.fn();

// Mock console methods if needed
global.console = {
    ...console,
    error: jest.fn(),
    warn: jest.fn(),
    log: jest.fn()
};

// Setup default DOM structure
beforeEach(() => {
    document.body.innerHTML = `
        <div id="app">
            <div id="connectors-container"></div>
            <div id="requests-container">
                <table id="requests-table"></table>
            </div>
        </div>
    `;
});

// Cleanup after each test
afterEach(() => {
    document.body.innerHTML = '';
    jest.clearAllMocks();
    jest.clearAllTimers();
});