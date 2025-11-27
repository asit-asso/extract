/**
 * Jest configuration for Extract JavaScript unit tests
 */
module.exports = {
    // Test environment
    testEnvironment: 'jsdom',
    
    // Where to find test files
    testMatch: [
        '**/src/test/javascript/**/*.test.js',
        '**/src/test/javascript/**/*.spec.js'
    ],
    
    // Setup files to run before tests
    setupFilesAfterEnv: ['<rootDir>/src/test/javascript/setup.js'],
    
    // Module paths
    moduleNameMapper: {
        '^@/(.*)$': '<rootDir>/src/main/resources/static/$1',
        '\\.(css|less|scss|sass)$': 'identity-obj-proxy'
    },
    
    // Coverage configuration
    collectCoverageFrom: [
        'src/main/resources/static/js/**/*.js',
        '!src/main/resources/static/lib/**',
        '!**/node_modules/**'
    ],
    
    // Coverage thresholds
    coverageThreshold: {
        global: {
            branches: 70,
            functions: 70,
            lines: 70,
            statements: 70
        }
    },
    
    // Transform files
    transform: {
        '^.+\\.js$': 'babel-jest'
    },
    
    // Ignore patterns
    testPathIgnorePatterns: [
        '/node_modules/',
        '/target/',
        '/lib/'
    ],
    
    // Module paths to ignore for haste
    modulePathIgnorePatterns: [
        '<rootDir>/target/',
        '<rootDir>/src/main/resources/static/lib/'
    ],
    
    // Verbose output
    verbose: true
};