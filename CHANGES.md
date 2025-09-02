# Changelog

## v2.3.0 (In Development)

### Bug Fixes

#### Issue #337 - Fix template rendering for cancelled requests without matching rules
- **Problem**: Page becomes non-functional when viewing cancelled requests that have no matching connector rules
- **Solution**: Added comprehensive null safety checks in Thymeleaf template for Client Response panel
- **Status**: Partially resolved by fix #333 (null outputFolderPath), completed with additional null safety for outputFiles
- **Changes**:
  - Fixed panel visibility condition to check for null `outputFiles` array (line 353)
  - Added null checks before accessing `outputFiles` array properties (line 373)
  - Protected download button condition with null safety (line 400)
- **Tests Added**:
  - Unit tests in `RequestModelTest.java` for null outputFiles scenarios
  - Integration tests in `CancelledRequestWithoutRulesTest.java` for full page rendering
- **Impact**: Request details page now renders correctly for all request types, buttons remain functional
- **Files Modified**:
  - `extract/src/main/resources/templates/pages/requests/details.html`
  - `extract/src/test/java/ch/asit_asso/extract/unit/web/model/RequestModelTest.java`
  - `extract/src/test/java/ch/asit_asso/extract/integration/requests/CancelledRequestWithoutRulesTest.java`

#### Issue #333 - Fix null pointer exception for requests without geographical perimeter
- **Problem**: Users could not cancel or delete imported requests without a geographical perimeter (IMPORTFAIL status)
- **Solution**: Fixed null handling for outputFolderPath in Java model and Thymeleaf template
- **Changes**:
  - Modified `RequestModel.getOutputFolderPath()` to safely handle null values
  - Updated `details.html` template with conditional checks for null outputFolderPath  
  - Added French translation for "Non disponible" message
- **Impact**: Request details page now loads correctly for all request types, enabling cancellation/deletion
- **Files Modified**:
  - `extract/src/main/java/ch/asit_asso/extract/web/model/RequestModel.java`
  - `extract/src/main/resources/templates/pages/requests/details.html`
  - `extract/src/main/resources/static/lang/fr/messages.properties`

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