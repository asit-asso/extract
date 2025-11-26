# Integration Tests for List Pages Filtering (Issue #344)

## Overview

This directory contains integration tests for client-side filtering functionality on list pages implemented in issue #344.

## Test Classes

### 1. `ProcessesListFilteringIntegrationTest.java`
Tests the processes list page filtering elements.

**Coverage:**
- ✅ Text filter input presence and configuration
- ✅ DataTables configuration (searching enabled)
- ✅ Process data loaded in model
- ✅ Filter button presence
- ✅ Table structure correctness
- ✅ JavaScript includes
- ✅ Authentication requirements
- ✅ Filter form structure

**Tests:** 8 tests

### 2. `ConnectorsListFilteringIntegrationTest.java`
Tests the connectors list page filtering elements including text filter and type dropdown.

**Coverage:**
- ✅ Text filter input presence
- ✅ Type dropdown filter presence and options
- ✅ DataTables configuration
- ✅ Connector data loaded in model
- ✅ Filter button presence
- ✅ Table structure correctness
- ✅ JavaScript includes
- ✅ Authentication requirements
- ✅ Filter form structure with multiple filters
- ✅ Placeholder attributes for Select2

**Tests:** 10 tests

### 3. `UsersListFilteringIntegrationTest.java`
Tests the users list page filtering elements with the most comprehensive filtering (5 filters total).

**Coverage:**
- ✅ Text filter input (searches across login, name, email)
- ✅ Role dropdown filter (ADMIN, OPERATOR)
- ✅ State dropdown filter (active, inactive)
- ✅ Notifications dropdown filter (enabled, disabled)
- ✅ 2FA dropdown filter (ACTIVE, INACTIVE, STANDBY)
- ✅ DataTables configuration
- ✅ User data loaded in model
- ✅ Filter button presence
- ✅ Table structure with role badges
- ✅ JavaScript includes
- ✅ Authentication requirements
- ✅ Filter form structure
- ✅ Placeholder attributes for Select2
- ✅ Role badges with data-role attributes

**Tests:** 14 tests

## Testing Approach

These are **MockMvc integration tests** that verify:

1. **HTML Structure**: Filter inputs, dropdowns, buttons exist with correct IDs and classes
2. **DataTables Configuration**: JavaScript confirms `searching = true` and proper initialization
3. **Model Data**: Controllers populate the model with data collections
4. **Authentication**: Pages require ADMIN role
5. **Element Attributes**: CSS classes, data attributes, placeholders for Select2

### What These Tests DO:
- Verify HTML contains the necessary filter elements
- Check DataTables is properly configured for client-side filtering
- Confirm data is loaded into the model
- Validate page structure and JavaScript includes

### What These Tests DON'T:
- ❌ Test the actual JavaScript filtering logic (client-side behavior)
- ❌ Test DataTables search/filter functionality (would require Selenium)
- ❌ Verify filter interactions or result updates (UI testing)
- ❌ Test that filters actually filter the data (client-side JavaScript)

## Why MockMvc Instead of Selenium?

**Advantages of MockMvc approach:**
- ✅ Fast execution (no browser startup)
- ✅ No external dependencies (no WebDriver, browser)
- ✅ Deterministic and reliable
- ✅ Tests the Spring MVC layer and Thymeleaf rendering
- ✅ Easier to maintain and debug

**Limitations:**
- ⚠️ Cannot test JavaScript execution
- ⚠️ Cannot verify client-side filtering behavior
- ⚠️ Cannot test DataTables API interactions

**Rationale:** Since issue #344 is 100% client-side filtering (no AJAX, no server requests), testing the HTML structure and configuration is sufficient to verify the feature is implemented correctly. The actual filtering logic is provided by DataTables library (battle-tested) and custom JavaScript that can be tested separately if needed.

## Running the Tests

```bash
# All web integration tests
cd extract
./mvnw test -Dtest="**/web/*IntegrationTest" -Punit-tests

# Specific test class
./mvnw test -Dtest=ProcessesListFilteringIntegrationTest -Punit-tests
./mvnw test -Dtest=ConnectorsListFilteringIntegrationTest -Punit-tests
./mvnw test -Dtest=UsersListFilteringIntegrationTest -Punit-tests

# Specific test method
./mvnw test -Dtest=ProcessesListFilteringIntegrationTest#testProcessesPageContainsTextFilterInput -Punit-tests
```

## Prerequisites

These tests require:
- Configured PostgreSQL database (same as application)
- System parameters initialized in database (to avoid `/setup` redirect)
- Test profile active (`@ActiveProfiles("test")`)

## Test Data Setup

Each test class manages its own test data:
- Creates test entities in `@BeforeEach`
- Cleans up in `@AfterEach`
- Uses `@Autowired` repositories for data management
- Creates SystemParameters to prevent setup redirect

## Success Criteria

- ✅ All 32 tests created (8 + 10 + 14)
- ✅ Full coverage of filtering UI elements
- ✅ Comprehensive verification of page structure
- ✅ Authentication and authorization tests
- ✅ Multiple filter types tested (text, dropdowns, combined)
- ✅ Compatible with existing test infrastructure

## Future Enhancements

If full UI testing is needed:
1. Add Selenium WebDriver dependency
2. Create `@WebIntegrationTest` with Chrome headless
3. Test actual filter interactions and result updates
4. Verify DataTables API calls and DOM manipulation

For now, the MockMvc approach provides sufficient coverage to verify the feature implementation.
