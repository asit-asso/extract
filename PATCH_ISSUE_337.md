# Patch for Issue #337 - Fix Template Rendering for Cancelled Requests Without Matching Rules

## Summary

This patch fixes a template rendering issue that prevents users from closing or deleting cancelled requests that have no matching connector rules. The issue occurs when the "Client Response" section attempts to render but fails due to null `outputFolderPath` and `outputFiles`, causing the entire page to become non-functional.

**UPDATE (2025-08-26)**: Issue partially resolved by fix #333 which handled null `outputFolderPath`. This patch adds additional null safety for `outputFiles` array operations.

## Problem Description

### Issue Details
- **Issue Number**: #337
- **Title**: "Impossible de fermer une page demande après son annulation si elle n'a pas de traitement correspondant"
- **Severity**: High - Blocks user workflow completely
- **Component**: Request Details Page Template
- **Status Types Affected**: Cancelled requests without matching rules

### Root Cause Analysis

When a request has no matching connector rules and is cancelled:
1. The `outputFolderPath` remains null (no processing folder created)
2. The `getOutputFiles()` method returns an empty array
3. The template condition checks if `outputFiles` is empty but doesn't handle null folder path scenarios properly
4. Template tries to access properties on potentially null objects
5. This causes template rendering to fail, leaving buttons non-functional
6. Users cannot close, delete, or perform any action on the page

### Error Manifestation
- Page loads partially but buttons are inactive
- JavaScript errors in console due to incomplete HTML rendering
- User is stuck on the page with no way to navigate away or delete the request

## Solution

### Approach
Add proper null safety checks in the template to ensure the "Client Response" panel only renders when there's actual data to display, and ensure `outputFiles` access is safe even when `outputFolderPath` is null.

### Changes Implemented

#### 1. details.html Template Enhancement  
**File**: `extract/src/main/resources/templates/pages/requests/details.html`

**Line 353 - Main panel condition (IMPLEMENTED)**:
```html
<div class="card card-default"
     th:if="${request.waitingIntervention} or (not ${#strings.isEmpty(request.remark)}) or (${request.outputFiles != null} and not ${#arrays.isEmpty(request.outputFiles)})">
```

This ensures the panel only shows when:
- Request is waiting for intervention, OR
- There's a remark to display, OR  
- Output files exist (with null safety check)

**Line 373 - Files none condition (IMPLEMENTED)**:
```html
<div th:text="#{requestDetails.files.none}"
     th:if="(not ${request.waitingIntervention}) and (${request.outputFiles == null} or ${#arrays.isEmpty(request.outputFiles)})">{(None)}</div>
```

**Line 400 - Download button protection (IMPLEMENTED)**:
```html
th:if="${request.waitingIntervention} and ${request.outputFiles != null} and ${request.outputFiles.length} > 1"
```

## Implementation Status

✅ **All template changes have been implemented in the codebase**

The null safety checks have been added to handle requests without geographical perimeter or matching rules.

## Testing

### Test Files Created

1. **Unit Tests** (`RequestModelTest.java`):
   - `testGetOutputFilesWithNullFolderPathIssue337()` - Tests empty array return when folder path is null
   - `testUnmatchedStatusWithNullFolder()` - Tests UNMATCHED status handling
   - `testErrorStatusWithNullOutput()` - Tests ERROR status with null output
   - `testVariousCombinationsOfNullFields()` - Comprehensive null field testing

2. **Integration Tests** (`CancelledRequestWithoutRulesTest.java`):
   - Full page rendering test for cancelled requests without rules
   - Null outputFiles handling verification
   - Download button visibility test
   - Regression testing for normal requests

### Manual Testing Steps

1. **Create test scenario**:
   ```sql
   -- Find or create a request without matching rules
   SELECT * FROM requests WHERE status = 'ERROR' AND folder_out IS NULL;
   ```

2. **Test the problematic flow**:
   - Import a request with no connector rule match
   - Open the request in Extract web interface
   - Cancel the request with a note
   - Verify the page loads correctly
   - Confirm all buttons are functional
   - Test closing and deleting the request

3. **Regression testing**:
   - Test normal requests with output files
   - Test requests with remarks but no files
   - Test requests waiting for intervention
   - Test finished requests
   - Verify file download functionality still works

### Build & Test Commands

#### Lancer les tests avec Docker (sans Maven/JDK local)

Un script `run-tests.sh` est fourni pour exécuter tous les tests dans Docker, sans avoir besoin d'installer Maven ou JDK localement.

```bash
# Première utilisation : construire le projet
./run-tests.sh build

# Lancer les tests unitaires (incluant RequestModelTest)
./run-tests.sh unit

# Lancer les tests d'intégration avec les services
./run-tests.sh integration

# Lancer tous les tests
./run-tests.sh all

# Lancer un test spécifique
./run-tests.sh specific RequestModelTest

# Nettoyer le cache et reconstruire
./run-tests.sh clean
```

**Avantages :**
- ✅ Aucune installation locale requise (sauf Docker)
- ✅ Environnement de test isolé et reproductible
- ✅ Cache Maven persistant pour des builds rapides
- ✅ Gestion automatique des services pour les tests d'intégration

### État actuel des tests

```bash
# Tests unitaires: 270 passent / 1 échec (non lié à #337)
# - RequestModelTest: ✅ Tous les tests passent
# - Test en échec: ImageUtilsTest.checkPngUrl (problème réseau existant)
```

## Deployment Instructions

### 1. Changes Already Applied
✅ Template modifications have been applied to:
- Line 353: Main panel condition with null safety
- Line 373: Files none condition with null check
- Line 400: Download button with null protection

### 2. No Manual Changes Required
All fixes have been implemented in the codebase.

### 3. Build and Deploy

#### Using Maven
```bash
cd extract
mvn clean package -DskipTests
cp target/extract##2.2.0.war $TOMCAT_HOME/webapps/
$TOMCAT_HOME/bin/shutdown.sh && $TOMCAT_HOME/bin/startup.sh
```

#### Using Docker
```bash
cd extract
mvn clean package -DskipTests
docker-compose restart tomcat
```

### 4. Verification
```bash
# Monitor logs for template errors
tail -f $TOMCAT_HOME/logs/catalina.out | grep -i "template\|thymeleaf"

# Test the fix
# 1. Find a cancelled request without output folder
# 2. Access: http://localhost:8080/extract/requests/details/{id}
# 3. Verify all buttons work
```

## Rollback Procedure

```bash
# Restore original template
cp extract/src/main/resources/templates/pages/requests/details.html.backup-337 \
   extract/src/main/resources/templates/pages/requests/details.html

# Rebuild and redeploy
cd extract
mvn clean package -DskipTests
cp target/extract##2.2.0.war $TOMCAT_HOME/webapps/
$TOMCAT_HOME/bin/shutdown.sh && $TOMCAT_HOME/bin/startup.sh
```

## Impact Analysis

### Positive Impact
- Users can manage ALL cancelled requests, even those without matching rules
- No more stuck pages with non-functional buttons
- Better error resilience in template rendering
- Improved user experience when dealing with failed imports

### Risk Assessment
- **Low Risk**: Only adds defensive null checks
- **No Breaking Changes**: Existing functionality preserved
- **No Database Changes**: Works with current schema
- **No API Changes**: Only template rendering improved

## Performance Considerations

- **Zero Performance Impact**: Only adds null checks in template
- **No Additional Queries**: Uses existing model data
- **No Memory Overhead**: Simple conditional rendering

## Security Considerations

- **No Security Impact**: Changes are purely defensive
- **No New Attack Vectors**: Only adds null safety
- **No Data Exposure**: No new data displayed

## Related Issues

- **Issue #333**: Similar null pointer fix for `outputFolderPath`
- **Issue #321**: DataTables error handling improvements
- Both issues improve error resilience in the UI

## Alternative Solutions Considered

1. **Model-level fix**: Ensure `outputFiles` never returns null
   - Rejected: Current behavior is correct, template should handle nulls
   
2. **Controller-level fix**: Add flags to indicate data availability
   - Rejected: Over-engineering, template conditions are sufficient

3. **JavaScript workaround**: Enable buttons via client-side script
   - Rejected: Doesn't fix root cause, adds complexity

## Future Improvements

1. **Enhanced Error Messages**: Show specific message when no rules match
2. **Request Recovery**: Add ability to reassign requests to different processes
3. **Audit Trail**: Log when requests fail due to no matching rules
4. **Admin Dashboard**: Show statistics on unmatched requests

## Contact

For questions about this patch:
- GitHub Issue: https://github.com/asit-asso/extract/issues/337
- Project Wiki: https://github.com/asit-asso/extract/wiki