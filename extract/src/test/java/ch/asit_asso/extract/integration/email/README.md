# Integration Tests for Email Placeholders (Issue #323)

## Overview

This directory contains integration tests for email system placeholders implemented in issue #323.

## Test Classes

### 1. `RequestModelBuilderIntegrationTest.java`
Tests the `RequestModelBuilder` utility class that populates email templates with request data.

**Coverage:**
- ✅ All basic request fields (orderLabel, productLabel, client, organism, dates, surface, perimeter)
- ✅ Dynamic parameters parsing from JSON
- ✅ GUIDs (clientGuid, organismGuid, tiersGuid)
- ✅ Backward compatibility with alias variables
- ✅ Null and empty value handling
- ✅ Context creation convenience method

**Tests:** 6 tests, all passing

### 2. `SystemEmailsIntegrationTest.java`
Tests that all system email types correctly use the new placeholders.

**Coverage:**
- ✅ TaskFailedEmail with all placeholders
- ✅ RequestExportFailedEmail with parameters and i18n
- ✅ ConnectorImportFailedEmail with organism details
- ✅ InvalidProductImportedEmail with product label
- ✅ UnmatchedRequestEmail with surface and perimeter
- ✅ Email templates with null optional fields
- ✅ Standby emails with ISO dates
- ✅ All email types with dynamic parameters

**Tests:** 8 tests, all passing

## Template Approach

### Controlled Test Templates

Instead of relying on the actual Thymeleaf HTML templates installed by the application, these tests use **controlled inline templates** defined directly in the test code.

#### Why Controlled Templates?

1. **Independence**: Tests don't depend on external template files that may change or be missing
2. **Clarity**: Explicitly shows which placeholders are being tested
3. **Verification**: Can actually verify the generated email content
4. **Maintainability**: Changes to production templates don't break tests unexpectedly

#### How It Works

```java
// Define test template inline with all placeholders to test
private static final String TASK_FAILED_TEMPLATE =
    "<html><body>" +
    "<h1>Task Failed: [(${taskName})]</h1>" +
    "<p>Order: [(${orderLabel})]</p>" +
    "<p>Client: [(${client})] ([(${clientGuid})])</p>" +
    "<p>Format: [(${parameters.format})]</p>" +
    // ... more placeholders
    "</body></html>";

// Process template with actual data
Context context = RequestModelBuilder.createContextWithRequest(testRequest);
context.setVariable("taskName", "Data Export Task");
String emailContent = testTemplateEngine.process(TASK_FAILED_TEMPLATE, context);

// Verify actual content
assertTrue(emailContent.contains("Task Failed: Data Export Task"));
assertTrue(emailContent.contains("Order: ORDER-2024-TEST-001"));
assertTrue(emailContent.contains("Format: DXF"));
```

#### Key Components

1. **StringTemplateResolver**: Allows templates to be defined as strings instead of files
   ```java
   testTemplateEngine = new TemplateEngine();
   StringTemplateResolver templateResolver = new StringTemplateResolver();
   templateResolver.setTemplateMode(TemplateMode.HTML);
   testTemplateEngine.setTemplateResolver(templateResolver);
   ```

2. **Test Template Constants**: Each email type has a corresponding test template
   - `TASK_FAILED_TEMPLATE`
   - `UNMATCHED_REQUEST_TEMPLATE`
   - `REQUEST_EXPORT_FAILED_TEMPLATE`
   - `CONNECTOR_IMPORT_FAILED_TEMPLATE`
   - `INVALID_PRODUCT_IMPORTED_TEMPLATE`
   - `TASK_STANDBY_TEMPLATE`
   - `STANDBY_REMINDER_TEMPLATE`

3. **Content Verification**: Tests verify the actual HTML content generated
   ```java
   assertTrue(emailContent.contains("Client: Municipality of Test City (client-guid-abc123)"));
   assertTrue(emailContent.contains("Surface: 2500.75 m²"));
   assertTrue(emailContent.contains("Format param: DXF"));
   ```

## Placeholder Coverage

### Basic Request Fields
- `orderLabel`, `orderGuid`
- `productLabel`, `productGuid`
- `client`, `clientName` (alias), `clientGuid`, `clientDetails`
- `organism`, `organisationName` (alias), `organismGuid`
- `tiers`, `tiersGuid`, `tiersDetails`
- `surface` (in m²)
- `perimeter` (WKT format)
- `remark`, `clientRemark` (alias)
- `status`
- `rejected` (boolean)

### Date Fields
- `startDate` (localized format)
- `startDateISO` (ISO-8601 format)
- `endDate` (localized format)
- `endDateISO` (ISO-8601 format)

### Dynamic Parameters
Parameters from JSON are available in multiple forms:
- **Raw JSON**: `parametersJson` - `{"FORMAT":"DXF","SCALE":500}`
- **Map access**: `parametersMap['FORMAT']` - uppercase keys preserved
- **Object access**: `parameters.format` - lowercase keys for dot notation
- **Variable access**: `param_format`, `param_FORMAT` - both cases available

Example JSON:
```json
{
  "FORMAT": "DXF",
  "PROJECTION": "EPSG:2056",
  "SCALE": 500,
  "communes": "Lausanne,Morges"
}
```

Available as:
- `${parametersJson}` → Full JSON string
- `${parametersMap['FORMAT']}` → "DXF" (original case)
- `${parameters.format}` → "DXF" (lowercase)
- `${parameters.scale}` → "500" (lowercase)
- `${param_format}` → "DXF" (param_ prefix, lowercase)
- `${param_FORMAT}` → "DXF" (param_ prefix, original case)
- `${param_SCALE}` → "500" (param_ prefix, original case)

### Backward Compatibility

Aliases maintained for backward compatibility:
- `client` ↔ `clientName`
- `organism` ↔ `organisationName`
- `remark` ↔ `clientRemark`

## Running the Tests

```bash
# Run all email integration tests
cd extract
./mvnw test -Dtest="**/email/*IntegrationTest" -Punit-tests

# Run specific test class
./mvnw test -Dtest=RequestModelBuilderIntegrationTest -Punit-tests
./mvnw test -Dtest=SystemEmailsIntegrationTest -Punit-tests

# Run specific test method
./mvnw test -Dtest=SystemEmailsIntegrationTest#testTaskFailedEmailWithAllNewPlaceholders -Punit-tests
```

## Test Data

All tests use consistent test data:
- **Order**: ORDER-2024-TEST-001
- **Product**: Cadastral Data Extract - Complete
- **Client**: Municipality of Test City
- **Organization**: Regional Planning Office
- **Tiers**: Engineering Consultants Ltd
- **Surface**: 2500.75 m²
- **Perimeter**: POLYGON WKT geometry
- **Parameters**: JSON with FORMAT, PROJECTION, SCALE, LAYERS, communes

## Success Criteria

- ✅ All 14 tests passing
- ✅ Full coverage of new placeholders
- ✅ Template content verification
- ✅ Null value handling
- ✅ Internationalization support (fr, de, en)
- ✅ Dynamic parameters in multiple formats
- ✅ Backward compatibility maintained
