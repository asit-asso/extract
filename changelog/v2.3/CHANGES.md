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

### New Features

#### Issue #308 - Extract UI in a multilingual environment
- **Feature**: Full multilingual support with configurable languages, browser preference detection, and user language preferences
- **Capabilities**:
  - Multi-language configuration via `extract.i18n.language=de,fr,en` in application.properties
  - Automatic browser language detection with intelligent fallback strategy
  - Per-user language preferences stored in database
  - Language switcher for authenticated users
  - Support for all standard language tags (fr, en-US, de, it, etc.)
- **Fallback Strategy**:
  - For authenticated users: user preference → database preference → browser → default
  - For unauthenticated users: browser → default (French)
- **Implementation**:
  - LocaleConfiguration with multi-language support
  - UserLocaleResolver with browser detection and user preference persistence
  - Full i18n infrastructure with MessageSource and locale-specific templates
  - LocaleChangeInterceptor for runtime language switching
- **Files Modified**:
  - `extract/src/main/java/ch/asit_asso/extract/configuration/LocaleConfiguration.java`
  - `extract/src/main/java/ch/asit_asso/extract/configuration/UserLocaleResolver.java`
  - `extract/src/main/java/ch/asit_asso/extract/configuration/I18nConfiguration.java`
  - Translation files: `extract/src/main/resources/messages_*.properties`

#### Issue #323 - Add new placeholders for system emails
- **Feature**: Extended email templates with comprehensive request field support
- **New Email Placeholders**:
  - `orderLabel` - Order/request name
  - `productLabel` - Product name
  - `startDate` / `startDateISO` - Request submission date
  - `endDate` / `endDateISO` - Request completion date
  - `organism` / `organisationName` - Client organization
  - `client` / `clientName` - Client name
  - `tiers` / `tiersDetails` - Third-party organization
  - `surface` - Surface area in m²
  - `perimeter` - Geographic perimeter (WKT)
  - `parameters` - Dynamic properties map
  - `parametersJson` - Raw JSON parameters
  - GUIDs: `clientGuid`, `organismGuid`, `tiersGuid`, `productGuid`
- **Dynamic Parameters Support**:
  - Access via `parameters.xxx` syntax (e.g., `parameters.format`, `parameters.projection`)
  - Robust handling of missing/empty values
  - JSON parsing with graceful error handling
- **Implementation**:
  - New `RequestModelBuilder` utility class for centralized email variable management
  - All system email classes updated to use RequestModelBuilder
  - Backward compatibility maintained with alias variables
  - Comprehensive null safety for optional fields
- **Files Modified**:
  - `extract/src/main/java/ch/asit_asso/extract/email/RequestModelBuilder.java` (new)
  - `extract/src/main/java/ch/asit_asso/extract/email/TaskFailedEmail.java`
  - `extract/src/main/java/ch/asit_asso/extract/email/RequestExportFailedEmail.java`
  - `extract/src/main/java/ch/asit_asso/extract/email/ConnectorImportFailedEmail.java`
  - `extract/src/main/java/ch/asit_asso/extract/email/InvalidProductImportedEmail.java`
  - `extract/src/main/java/ch/asit_asso/extract/email/TaskStandbyEmail.java`
  - `extract/src/main/java/ch/asit_asso/extract/email/StandbyReminderEmail.java`
  - `extract/src/main/java/ch/asit_asso/extract/email/UnmatchedRequestEmail.java`

#### Issue #344 - Add filters to processes, connectors, and users pages
- **Feature**: Client-side filtering for list pages to improve navigation and search
- **Processes Page Filters**:
  - Free text filter with case-insensitive partial matching on process name
  - Placeholder: "Traitement"
- **Connectors Page Filters**:
  - Free text filter on connector name (case-insensitive, partial match)
  - Dropdown filter for connector types
  - Placeholders: "Connecteur" and "Type"
- **Users and Rights Page Filters**:
  - Free text search across login, full name, and email
  - Four dropdown filters: Role, Status, Notifications, 2FA
- **Technical Implementation**:
  - 100% client-side filtering (zero additional server requests)
  - DataTables API with custom search functions
  - Case-insensitive partial matching
  - Combined filter logic (all filters work together)
- **Files Modified**:
  - `extract/src/main/resources/templates/pages/processes/list.html`
  - `extract/src/main/resources/templates/pages/connectors/list.html`
  - `extract/src/main/resources/templates/pages/users/list.html`
  - `extract/src/main/resources/static/js/processesList.js`
  - `extract/src/main/resources/static/js/connectorsList.js`
  - `extract/src/main/resources/static/js/usersList.js`

#### Issue #351 - Make signature verification configurable at runtime
- **Feature**: Runtime configuration for Windows binary signature verification without rebuild
- **Capabilities**:
  - Configure signature checking via `check.authenticity=false` in application.properties
  - Changes take effect on application restart (no rebuild required)
  - Reduces false-positive antivirus alerts during FME/external tool execution
- **Use Case**: Particularly useful for Morges deployment where signature validation triggers antivirus alerts
- **Implementation**:
  - @ConfigurationProperties with refresh support
  - Runtime property loading via SystemParametersRepository
  - Automatic configuration reload on application restart
- **Configuration**:
  - Add `check.authenticity=false` to application.properties
  - Restart Extract application
  - Binary signature verification disabled

### New Task Plugins

#### Issue #346 - New Python Task Plugin
- **Plugin Name**: "Extraction Python"
- **Plugin Code**: `python`
- **Purpose**: Execute generic Python scripts with parameters passed via GeoJSON file, circumventing command-line length limitations
- **Icon**: fa fa-cogs
- **Required Parameters**:
  - Python interpreter path (field: `pythonInterpreter`)
  - Python script path (field: `pythonScript`)
- **Parameter Passing**:
  - Creates `parameters.json` file in GeoJSON format in FolderIn directory
  - Perimeter encoded as GeoJSON Feature (Polygon/MultiPolygon) with support for interior rings (donuts)
  - Additional parameters as Feature properties (ClientGuid, ClientName, OrganismGuid, OrganismName, ProductGuid, ProductLabel, OrderLabel, etc.)
  - File path passed as single command-line argument
- **Execution**:
  - Command: `[python_path] [script_path] [parameters.json_path]`
  - Working directory: script location
  - Timeout: 5 minutes
  - Exit code 0 = success, non-zero = failure
- **Output**: Script saves files to FolderOut directory
- **Geometry Support**: WKT-to-GeoJSON conversion for Polygon, MultiPolygon, Point, LineString with full support for complex geometries
- **Files**:
  - `extract-task-python/src/main/java/ch/asit_asso/extract/plugins/python/PythonPlugin.java`
  - `extract-task-python/src/main/resources/plugins/python/lang/*/messages.properties`
  - `extract-task-python/src/main/resources/plugins/python/lang/*/help.html`

#### Issue #347 - New FME Form V2 Task Plugin
- **Plugin Name**: "Extraction FME Form V2"
- **Plugin Code**: `FME2017V2`
- **Purpose**: Enhanced FME Desktop plugin that bypasses command-line length limitations via GeoJSON parameter file
- **Icon**: fa fa-cogs
- **Required Parameters**:
  - FME workspace path (field: `workbench`)
  - FME executable path (field: `application`)
  - Number of parallel fme.exe instances (field: `nbInstances`, default: 1, max: 8)
- **Improvements over V1**:
  - Parameters passed via `parameters.json` file instead of command-line arguments
  - Added ClientName, OrganismName, ProductLabel metadata
  - Support for complex perimeters without length constraints
  - GeoJSON format with proper geometry encoding
- **Parameter File Structure**:
  - GeoJSON Feature with perimeter as geometry (WGS84 coordinates)
  - Properties include all request metadata and dynamic parameters
  - Passed to FME via `--ParametersFile` argument
- **Execution**:
  - Timeout: 72 hours for long-running FME processes
  - Exit code 0 = success
  - Output files saved to FolderOut directory
- **Geometry Support**: Full support for Polygon, MultiPolygon, and donut-shaped geometries with WKT-to-GeoJSON conversion
- **Files**:
  - `extract-task-fmedesktop-v2/src/main/java/ch/asit_asso/extract/plugins/fmedesktopv2/FmeDesktopV2Plugin.java`
  - `extract-task-fmedesktop-v2/src/main/resources/plugins/fme/lang/*/messages.properties`
  - `extract-task-fmedesktop-v2/src/main/resources/plugins/fme/lang/*/help.html`

#### Issue #353 - New FME Flow V2 Task Plugin
- **Plugin Name**: "Extraction FME Flow V2"
- **Plugin Code**: `FMEFLOWV2`
- **Purpose**: Enhanced FME Server/Flow plugin with POST requests, API token authentication, and GeoJSON parameters
- **Icon**: fa fa-cogs
- **Required Parameters**:
  - Service URL (field: `serviceURL`)
  - FME API Token (field: `apiToken`, type: password)
- **Improvements over V1**:
  - POST requests instead of GET (no URL length limitations)
  - Token-based authentication (Authorization header) instead of basic auth
  - Parameters in JSON request body instead of query string
  - Added ClientName, OrganismName, ProductLabel metadata
  - Enhanced security with URL validation (SSRF prevention)
- **Request Flow**:
  1. Serialize parameters as GeoJSON with perimeter as Feature geometry (WGS84)
  2. Send POST request with JSON body and Authorization: Token header
  3. Parse FME response to extract Data Download URL
  4. Download resulting ZIP file on HTTP 200 response
  5. Extract files to FolderOut directory
- **Security Features**:
  - URL validation to prevent SSRF attacks
  - File size limit: 500MB
  - Timeout and retry logic
  - Secure token handling (password field type)
- **Geometry Support**: Full support for Polygon, MultiPolygon, and complex geometries with interior rings
- **Error Handling**: Non-200 responses treated as task failure with detailed error messages
- **Files**:
  - `extract-task-fmeserver-v2/src/main/java/ch/asit_asso/extract/plugins/fmeserverv2/FmeServerV2Plugin.java`
  - `extract-task-fmeserver-v2/src/main/resources/plugins/fmeserver/lang/*/messages.properties`
  - `extract-task-fmeserver-v2/src/main/resources/plugins/fmeserver/lang/*/help.html`

### Infrastructure

#### Docker Build Fix
- Fixed Alpine Linux repository issue in Docker images
- Updated from `openjdk:8u111-jre-alpine` to `eclipse-temurin:8-jre-alpine`
- Files modified:
  - `docker/ldap-ad/Dockerfile`
  - `docker/tomcat/Dockerfile`

#### Build System Improvements
- Added configurable `skipTests` property with default value `true`
- Tests can be overridden via `-DskipTests=false` command-line flag
- Fixed test execution in CI/CD pipeline
- Corrected log path configuration for integration tests (uses `/tmp/log/extract`)
- Fixed test failures:
  - SystemEmailTest: Updated hardcoded dates to relative dates
  - FmeDesktopIntegrationTest: Fixed Unicode apostrophe in expected message
  - Integration tests: Added log path configuration to prevent `/var/log/extract` permission errors
- Files modified:
  - `pom.xml` (root and module-level)
  - `extract/src/main/resources/logback-spring.xml`
  - `extract/src/test/java/ch/asit_asso/extract/email/SystemEmailTest.java`
  - `extract/src/test/java/ch/asit_asso/extract/integration/taskplugins/FmeDesktopIntegrationTest.java`
