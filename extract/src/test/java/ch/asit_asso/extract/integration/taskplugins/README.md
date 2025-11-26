# Integration Tests for Task Plugins (Issue #346)

## Overview

This directory contains **integration tests** for task plugins that test the Java logic WITHOUT requiring external dependencies.

## Difference: Integration vs Functional Tests

### Integration Tests (this directory)
- ✅ Test Java component integration
- ✅ Test data transformation logic (WKT → GeoJSON)
- ✅ Test JSON generation
- ✅ NO external software required (no Python, no FME)
- ✅ Use reflection to test internal methods
- ✅ Fast and reliable
- ✅ Run in any CI/CD environment

### Functional Tests (`../functional/taskplugins/`)
- ❗ Execute real external processes (Python scripts, FME workspaces)
- ❗ Require external software installed
- ❗ Test end-to-end behavior
- ❗ Slower execution
- ❗ May not run in minimal CI environments

## Test Classes

### `PythonPluginIntegrationTest.java`
Integration tests for Python plugin **without executing Python**.

**What is tested:**
- ✅ Parameters JSON file generation
- ✅ GeoJSON Feature structure
- ✅ WKT to GeoJSON conversion for all geometry types
- ✅ Metadata properties population
- ✅ Dynamic parameters from JSON
- ✅ Edge cases (null values, empty strings, invalid JSON)
- ✅ Special characters escaping
- ✅ Large coordinate values

**What is NOT tested:**
- ❌ Actual Python script execution
- ❌ Python interpreter behavior
- ❌ Process exit codes
- ❌ Script output parsing

**Tests:** 15 integration tests

#### Test Coverage Details

1. **testParametersJsonCreationWithAllMetadata** - All request metadata in properties
2. **testWKTPolygonToGeoJSON** - Polygon WKT → GeoJSON coordinates
3. **testWKTMultiPolygonToGeoJSON** - MultiPolygon with multiple polygons
4. **testWKTPolygonWithHoleToGeoJSON** - Donut geometry (interior rings)
5. **testWKTPointToGeoJSON** - Point geometry conversion
6. **testWKTLineStringToGeoJSON** - LineString geometry conversion
7. **testDynamicParametersInProperties** - JSON parameters merged into properties
8. **testNullPerimeterHandling** - Null geometry handled gracefully
9. **testEmptyParametersJsonHandling** - Empty parameters string
10. **testInvalidJsonParametersHandling** - Malformed JSON doesn't crash
11. **testAllNullOptionalFields** - Minimal request with nulls everywhere
12. **testComplexNestedParametersFlattening** - Nested JSON structures
13. **testSpecialCharactersEscaping** - Quotes, newlines, HTML chars
14. **testLargeCoordinateValues** - Swiss coordinate system values (millions)

## How It Works

These tests use **Java Reflection** to call the private `createParametersFile(Request)` method of the Python plugin:

```java
Method method = pythonPlugin.getClass().getDeclaredMethod("createParametersFile", Request.class);
method.setAccessible(true);
method.invoke(pythonPlugin, testRequest);
```

This allows testing the JSON generation logic in isolation without:
- Installing Python
- Creating dummy Python scripts
- Dealing with process execution
- Managing timeouts

## Running the Tests

### Run All Integration Tests
```bash
cd extract
./mvnw test -Dtest="**/integration/**/*IntegrationTest" -Punit-tests
```

### Run Only Python Plugin Integration Tests
```bash
cd extract
./mvnw test -Dtest=PythonPluginIntegrationTest -Punit-tests
```

### Run Specific Test
```bash
cd extract
./mvnw test -Dtest=PythonPluginIntegrationTest#testWKTPolygonToGeoJSON -Punit-tests
```

## Verified Behaviors

### GeoJSON Structure
Every test verifies:
- `"type": "Feature"` at root level
- `"geometry"` object with correct type and coordinates
- `"properties"` object with all metadata

### Geometry Types Tested
- ✅ **Polygon** - Simple closed ring
- ✅ **Polygon with hole** - Exterior + interior rings (donut)
- ✅ **MultiPolygon** - Multiple separate polygons
- ✅ **Point** - Single coordinate pair
- ✅ **LineString** - Multiple connected points

### Metadata Fields Verified
- `orderLabel`, `orderGuid`
- `productLabel`, `productGuid`
- `clientName`, `clientGuid`
- `organismName`, `organismGuid`
- `tiersName`, `tiersGuid`, `tiersDetails`
- `remark`
- `startDate`, `endDate` (if present)

### Dynamic Parameters
Parameters from `request.getParameters()` JSON are:
- Parsed from JSON string
- Merged into properties object
- Available alongside metadata fields

Example:
```json
{
  "type": "Feature",
  "geometry": { "type": "Polygon", "coordinates": [...] },
  "properties": {
    "orderLabel": "ORDER-001",
    "clientName": "Test Client",
    "FORMAT": "DXF",
    "PROJECTION": "EPSG:2056"
  }
}
```

## Edge Cases Covered

### Null Handling
- Null perimeter → Valid Feature without geometry
- Null parameters → Empty properties object
- Null optional fields → Not included in JSON

### Invalid Input
- Invalid JSON parameters → Gracefully ignored
- Empty strings → Treated as null
- Malformed WKT → (behavior depends on JTS library)

### Special Cases
- Special characters (quotes, newlines) → Properly escaped
- Very large coordinates (Swiss system) → Preserved accurately
- Complex nested JSON → Flattened to properties

## Success Criteria

- ✅ 15 integration tests created
- ✅ Full WKT → GeoJSON conversion coverage
- ✅ All geometry types tested
- ✅ Edge cases and error scenarios covered
- ✅ No external dependencies required
- ✅ Tests run fast (<10 seconds total)

## Relationship with Functional Tests

| Aspect | Integration Tests (here) | Functional Tests |
|--------|-------------------------|------------------|
| **Location** | `integration/taskplugins/` | `functional/taskplugins/` |
| **Dependencies** | Java + JTS library only | Python 3.x required |
| **Speed** | Fast (~1s per test) | Slower (~3-5s per test) |
| **Reliability** | Always pass | May fail if Python not available |
| **Coverage** | Input/output data transformation | End-to-end execution |
| **CI/CD** | Runs everywhere | Requires Python installation |

**Both are necessary:**
- Integration tests verify the **logic works correctly**
- Functional tests verify it **works in the real world**

## Future Enhancements

Consider adding tests for:
- Multi-ring polygons (more than 2 rings)
- 3D geometries (if supported)
- GeometryCollection type
- Performance with very large geometries (1000+ points)
- Concurrent parameters.json creation (thread safety)
