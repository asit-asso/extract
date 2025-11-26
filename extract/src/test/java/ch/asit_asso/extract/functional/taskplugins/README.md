# Functional Tests for Task Plugins (Issue #346)

## Overview

This directory contains **functional tests** for task plugins that require external dependencies or system installations.

## What are Functional Tests?

**Functional tests** differ from integration tests in that they:
- ❗ Require external software installed (Python, FME, etc.)
- ❗ Execute real processes/scripts on the system
- ❗ Depend on the runtime environment
- ❗ Are typically slower than integration tests
- ❗ May not run in all CI/CD environments

**Integration tests**, on the other hand:
- ✅ Test component integration within the Java/Spring ecosystem
- ✅ Don't require external software beyond Java/Maven
- ✅ Use mocked or in-memory dependencies
- ✅ Run reliably in any environment

## Test Classes

### `PythonPluginFunctionalTest.java`
Functional tests for the Python plugin (Issue #346).

**Prerequisites:**
- Python 3 installed and available in PATH
- Ability to execute Python scripts
- Write permissions to `/tmp/extract-test`

**Coverage:**
- ✅ Basic script execution (exit code 0)
- ✅ Parameters JSON file creation
- ✅ GeoJSON format validation
- ✅ Geometry types: Polygon, MultiPolygon, Point, LineString
- ✅ Donut geometries (interior rings)
- ✅ Feature properties with metadata
- ✅ Dynamic parameters in properties
- ✅ Error handling (non-zero exit codes)
- ✅ Script not found scenarios
- ✅ Output files in FolderOut

**Tests:** 13 functional tests

### Python Test Scripts

Located in `python_scripts/`:
- `test_success.py` - Basic successful execution
- `test_read_parameters.py` - Reads and validates parameters.json
- `test_verify_geojson.py` - Verifies GeoJSON geometry format
- `test_check_properties.py` - Checks metadata properties
- `test_failure.py` - Intentional failure (exit code 1)
- `test_create_output.py` - Creates output file in FolderOut

## Running the Tests

### Run All Functional Tests
```bash
cd extract
./mvnw verify -Pfunctional-tests
```

### Run Only Python Plugin Functional Tests
```bash
cd extract
./mvnw test -Dtest=PythonPluginFunctionalTest -Pfunctional-tests
```

### Run Specific Test
```bash
cd extract
./mvnw test -Dtest=PythonPluginFunctionalTest#testPythonPluginBasicExecution -Pfunctional-tests
```

## Maven Profile Configuration

From `pom.xml`:
```xml
<profile>
    <id>functional-tests</id>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
                <configuration>
                    <includes>
                        <include>ch.asit_asso.extract.functional.**.*FunctionalTest</include>
                    </includes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</profile>
```

## Test Requirements

### Python Plugin Tests
- **Python Version**: Python 3.x
- **System Access**: Read/write to temp directories
- **Environment**: Linux or macOS (Windows may require path adjustments)

## Success Criteria

- ✅ 13 functional tests created
- ✅ Full coverage of Python plugin features
- ✅ GeoJSON format validation
- ✅ All geometry types tested
- ✅ Error scenarios covered
- ✅ Proper test categorization (functional vs integration)

## Why Separate from Integration Tests?

The Python plugin tests were initially created as integration tests but moved to functional tests because:

1. **External Dependency**: Requires Python interpreter installed
2. **System Execution**: Executes real Python scripts
3. **Environment-Specific**: Behavior may vary based on Python version
4. **CI/CD Considerations**: May not run in minimal CI environments
5. **Test Speed**: Slower due to process spawning

This separation allows:
- Integration tests to run fast and reliably everywhere
- Functional tests to run in environments where dependencies are available
- Better test organization and maintenance

## Note on FmeDesktopIntegrationTest

The existing `FmeDesktopIntegrationTest` is currently in the `integration` package but is technically a functional test (requires FME installed). This is a known inconsistency in the codebase that may be addressed in the future.

## Future Enhancements

Consider adding functional tests for:
- FME Flow plugin (if implemented)
- QGIS plugin
- Other plugins requiring external software
- Performance/stress tests with large datasets
