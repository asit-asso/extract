# Changelog

## v2.3.0 (In Development)

### Bug Fixes

#### Issue #321 - Replace intrusive alerts with non-blocking notifications
- **Problem**: Intrusive JavaScript alerts and DataTables warnings blocking UI when network errors occur
- **Solution**: Implemented graceful error handling with Bootstrap notifications
- **Features**:
  - Replace all JavaScript alert() calls with Bootstrap notifications
  - Set DataTables error mode to 'none' to suppress default alerts
  - Add non-intrusive notification that appears in top-right corner
  - Notifications auto-dismiss after 10 seconds with manual close option
  - Prevent duplicate notifications from stacking
  - Handle authentication redirects gracefully (302 status)
  - Add JSON dataType specification for AJAX calls
  - Detect and redirect to login when session expires
  - Clear notifications automatically when connection is restored
  - Support internationalization with French translations
- **Testing**:
  - Added comprehensive unit tests (16 passing tests)
  - Tests cover notifications, localization, error handling
  - Test files in `extract/src/test/javascript/`
  - Run tests: `cp package-test.json package.json && yarn test`
- **Files Modified**:
  - `extract/src/main/resources/static/js/requestsList.js` - Main notification implementation
  - `extract/src/main/resources/static/lang/fr/messages.js` - French translations
  - `extract/src/test/javascript/requestsList.test.js` - Unit tests
  - `extract/src/test/javascript/setup.js` - Test configuration
  - `extract/src/test/javascript/README.md` - Test documentation
  - `extract/jest.config.js` - Jest configuration
  - `extract/package-test.json` - Test dependencies

### Infrastructure

#### Docker Build Fix
- Fixed Alpine Linux repository issue in Docker images
- Updated from `openjdk:8u111-jre-alpine` to `eclipse-temurin:8-jre-alpine`
- Files modified:
  - `docker/ldap-ad/Dockerfile`
  - `docker/tomcat/Dockerfile`