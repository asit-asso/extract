# Patch for Issue #333 - Fix Null Pointer Exception for IMPORTFAIL Requests

## Summary

This patch resolves a NullPointerException that occurred when users tried to view, cancel, or delete requests that failed during import (IMPORTFAIL status) and had no geographical perimeter defined. The issue prevented users from managing these failed requests through the web interface.

## Problem Description

### Issue Details
- **Issue Number**: #333
- **Severity**: High - Blocks user workflow
- **Component**: Request Details Page
- **Status Types Affected**: IMPORTFAIL

### Root Cause
When a request fails during import without a geographical perimeter being set:
1. The `outputFolderPath` field remains null in the Request entity
2. The `RequestModel.getOutputFolderPath()` method called `toString()` on null value
3. This caused a NullPointerException when rendering the request details page
4. Users could not access the details page to cancel or delete these failed requests

### Error Manifestation
```java
java.lang.NullPointerException: Cannot invoke "java.nio.file.Path.toString()" because "this.outputFolderPath" is null
    at ch.asit_asso.extract.web.model.RequestModel.getOutputFolderPath(RequestModel.java:371)
```

## Solution

### Changes Made

#### 1. RequestModel.java
**File**: `extract/src/main/java/ch/asit_asso/extract/web/model/RequestModel.java`

**Before**:
```java
public final String getOutputFolderPath() { 
    return this.outputFolderPath.toString(); 
}
```

**After**:
```java
public final String getOutputFolderPath() { 
    return this.outputFolderPath != null ? this.outputFolderPath.toString() : null; 
}
```

#### 2. details.html Template
**File**: `extract/src/main/resources/templates/pages/requests/details.html`

**Before**:
```html
<div th:text="${request.outputFolderPath}">{/var/extract/data/my-request/output/}</div>
```

**After**:
```html
<div th:if="${request.outputFolderPath != null}" th:text="${request.outputFolderPath}">{/var/extract/data/my-request/output/}</div>
<div th:if="${request.outputFolderPath == null}" th:text="#{requestDetails.tempFolder.notAvailable}">
    {(Not available)}
</div>
```

#### 3. French Translations
**File**: `extract/src/main/resources/static/lang/fr/messages.properties`

**Added**:
```properties
requestDetails.tempFolder.notAvailable=Non disponible
```

## Testing

### Manual Testing Steps

1. **Create a test scenario with IMPORTFAIL status**:
   - Import a request through a connector
   - Force the import to fail before geographical perimeter is set
   - Verify the request has IMPORTFAIL status in the database

2. **Verify the fix**:
   - Navigate to the request details page
   - Confirm the page loads without errors
   - Check that "Non disponible" (or "Not available") appears for the output folder path
   - Test that Cancel and Delete buttons work correctly

3. **Regression testing**:
   - Verify normal requests (with outputFolderPath) still display correctly
   - Test requests in various states (ONGOING, FINISHED, ERROR)
   - Confirm no impact on other request operations

### Unit Test Template
```java
@Test
public void testGetOutputFolderPathWithNull() {
    // Given
    Request request = new Request();
    // outputFolderPath is null by default
    RequestModel model = new RequestModel(request);
    
    // When
    String result = model.getOutputFolderPath();
    
    // Then
    assertNull(result);
    // Should not throw NullPointerException
}

@Test
public void testGetOutputFolderPathWithValue() {
    // Given
    Request request = new Request();
    request.setOutputFolderPath(Paths.get("/var/extract/data/test"));
    RequestModel model = new RequestModel(request);
    
    // When
    String result = model.getOutputFolderPath();
    
    // Then
    assertEquals("/var/extract/data/test", result);
}
```

## Deployment Instructions

### 1. Backup Current Files
```bash
# Create backups
cp extract/src/main/java/ch/asit_asso/extract/web/model/RequestModel.java{,.backup}
cp extract/src/main/resources/templates/pages/requests/details.html{,.backup}
cp extract/src/main/resources/static/lang/fr/messages.properties{,.backup}
```

### 2. Apply the Patch
The changes have been applied to the following files:
- `extract/src/main/java/ch/asit_asso/extract/web/model/RequestModel.java`
- `extract/src/main/resources/templates/pages/requests/details.html`
- `extract/src/main/resources/static/lang/fr/messages.properties`

### 3. Build and Deploy

#### Option A: Using Maven
```bash
# Build the WAR file
cd extract
mvn clean package -DskipTests

# Deploy to Tomcat
cp target/extract##2.2.0.war $TOMCAT_HOME/webapps/
$TOMCAT_HOME/bin/shutdown.sh
$TOMCAT_HOME/bin/startup.sh
```

#### Option B: Using Docker
```bash
# Build and restart
cd extract
mvn clean package -DskipTests
docker-compose restart tomcat
```

### 4. Verification
```bash
# Check application logs for errors
tail -f $TOMCAT_HOME/logs/catalina.out

# Test the fix
# 1. Find a request with IMPORTFAIL status in the database
# 2. Access its details page: http://localhost:8080/extract/requests/details/{id}
# 3. Verify the page loads without errors
```

## Rollback Procedure

If issues arise, rollback using the backup files:

```bash
# Restore original files
cp extract/src/main/java/ch/asit_asso/extract/web/model/RequestModel.java.backup \
   extract/src/main/java/ch/asit_asso/extract/web/model/RequestModel.java
   
cp extract/src/main/resources/templates/pages/requests/details.html.backup \
   extract/src/main/resources/templates/pages/requests/details.html
   
cp extract/src/main/resources/static/lang/fr/messages.properties.backup \
   extract/src/main/resources/static/lang/fr/messages.properties

# Rebuild and redeploy
cd extract
mvn clean package -DskipTests
cp target/extract##2.2.0.war $TOMCAT_HOME/webapps/
$TOMCAT_HOME/bin/shutdown.sh && $TOMCAT_HOME/bin/startup.sh
```

## Impact Analysis

### Positive Impact
- Users can now manage ALL failed import requests
- No more blocking NullPointerExceptions on request details page
- Better user experience with clear "Not available" message
- Consistent behavior across all request statuses

### Risk Assessment
- **Low Risk**: Changes are minimal and defensive
- **No Breaking Changes**: Backward compatible with existing data
- **No Database Changes**: Works with existing schema
- **No API Changes**: Only affects web UI rendering

## Performance Considerations

- **Minimal Impact**: Single null check added
- **No Additional Database Queries**: Uses existing data
- **No Memory Overhead**: Simple conditional rendering

## Security Considerations

- **No Security Impact**: Changes are purely defensive programming
- **No New Attack Vectors**: Only adds null safety checks
- **Data Protection**: No sensitive data exposed

## Future Improvements

Consider these enhancements in future releases:

1. **Logging Enhancement**: Add warning log when outputFolderPath is null for non-IMPORTFAIL requests
2. **Admin Dashboard**: Add metrics for IMPORTFAIL requests without perimeter
3. **Automatic Cleanup**: Implement scheduled task to clean old IMPORTFAIL requests
4. **Import Validation**: Strengthen import process to prevent null perimeter scenarios

## Related Issues

- Issue #321: DataTables error handling (completed)
- Issue #361: General bug fixes (merged)

## Contact

For questions about this patch:
- GitHub Issue: https://github.com/asit-asso/extract/issues/333
- Project Wiki: https://github.com/asit-asso/extract/wiki