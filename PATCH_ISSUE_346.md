# PATCH ISSUE #346: Python Task Processor Plugin

## Issue
Implement a Python task processor plugin for Extract that executes Python scripts with parameters passed via GeoJSON file.

## Solution Implemented

### 1. Created Python Plugin Module
- **Module**: `extract-task-python`
- **Main Class**: `ch.asit_asso.extract.plugins.python.PythonPlugin`
- **Interface**: Implements `ITaskProcessor` from Extract framework

### 2. Key Features
- **GeoJSON Parameter File**: Creates a `parameters.json` file as a GeoJSON Feature (not FeatureCollection)
- **WKT to GeoJSON Conversion**: Automatically converts WKT geometry to proper GeoJSON format
- **Comprehensive Error Handling**: Detailed error messages for all Python error types
- **Timeout Protection**: 5-minute timeout for script execution
- **Output Capture**: Captures both stdout and stderr for debugging

### 3. Parameter Structure
The plugin creates a GeoJSON Feature with:
```json
{
  "type": "Feature",
  "geometry": { /* converted from WKT */ },
  "properties": {
    "RequestId": 123,
    "FolderOut": "/path/to/output",
    "FolderIn": "/path/to/input",
    "OrderGuid": "...",
    "OrderLabel": "...",
    "ClientGuid": "...",
    "ClientName": "...",
    "OrganismGuid": "...",
    "OrganismName": "...",
    "ProductGuid": "...",
    "ProductLabel": "...",
    "Parameters": { /* custom parameters */ }
  }
}
```

### 4. Error Handling
Specific error detection and messages for:
- SyntaxError
- IndentationError/TabError
- ImportError/ModuleNotFoundError
- FileNotFoundError
- PermissionError
- NameError
- Timeout errors
- Exit code handling (1, 2, 126, 127, 255)

### 5. Configuration Parameters
- **pythonInterpreter**: Path to Python executable (required)
- **pythonScript**: Path to Python script to execute (required)
- **additionalArgs**: Optional additional arguments

### 6. Tests
Created comprehensive test suite:
- **PythonPluginTest**: 15 unit tests for plugin functionality
- **WKTToGeoJSONTest**: 9 tests for geometry conversion
- **Test Scripts**: 4 Python scripts for different scenarios
- **Total**: 24 tests, all passing

### 7. Files Created/Modified
- `extract-task-python/pom.xml` - Maven configuration
- `extract-task-python/src/main/java/ch/asit_asso/extract/plugins/python/PythonPlugin.java` - Main plugin
- `extract-task-python/src/main/java/ch/asit_asso/extract/plugins/python/PythonResult.java` - Result class
- `extract-task-python/src/main/java/ch/asit_asso/extract/plugins/python/LocalizedMessages.java` - i18n support
- `extract-task-python/src/main/java/ch/asit_asso/extract/plugins/python/PluginConfiguration.java` - Config handler
- `extract-task-python/src/main/java/module-info.java` - Module declaration
- `extract-task-python/src/main/resources/META-INF/services/ch.asit_asso.extract.plugins.common.ITaskProcessor` - ServiceLoader
- `extract-task-python/src/main/resources/plugins/python/lang/fr/messages.properties` - French messages
- `extract-task-python/src/main/resources/plugins/python/lang/fr/help.html` - Help documentation
- `extract-task-python/src/test/java/**` - Test classes
- `extract-task-python/src/test/resources/test_scripts/**` - Python test scripts

### 8. Dependencies Added
- JTS Core (1.19.0) - For WKT to GeoJSON conversion
- Jackson (2.14.0) - For JSON manipulation
- JUnit Jupiter (5.10.0) - For testing
- Mockito (5.5.0) - For test mocks

## Testing
```bash
cd extract-task-python
../extract/mvnw test -Punit-tests
# Results: Tests run: 24, Failures: 0, Errors: 0, Skipped: 0
```

## Deployment
The plugin JAR is automatically built and placed in:
```
extract/src/main/resources/task_processors/extract-task-python-2.2.0.jar
```

## Acceptance Criteria Met
✅ Python script execution with configurable interpreter  
✅ Parameters passed as GeoJSON Feature (not FeatureCollection)  
✅ WKT to GeoJSON conversion  
✅ Custom parameters under properties.Parameters  
✅ Comprehensive error handling with detailed messages  
✅ Full test coverage  
✅ Integration with Extract plugin framework  

## Status
**COMPLETED** - All requirements from issue #346 have been implemented and tested.