# ISSUE_432 - New installation: impossible to create the first admin user

## Status: ✅ COMPLIANT

### Issue Description
On a fresh installation with an empty database on which `sql/update_db.sql` had been run, the page
used to create the first administrator was never displayed: the login page appeared instead, while
no usable account existed. Skipping the script made the setup page work again.

### Root Cause
Three pieces combined:
1. `UsersInitializer.createSystemUser()` creates the hidden `system` user **without any profile**,
   so its `profile` column is `NULL`.
2. `sql/update_db.sql` contained `UPDATE users SET profile = 'ADMIN' WHERE profile IS NULL`, which
   promoted that hidden user to administrator.
3. `AppInitializationService.isConfigured()` answered `repository.existsByProfile(ADMIN)`, so it saw
   the system user, declared the application configured, and `SetupRedirectFilter` stopped redirecting
   to `/setup` while `SetupController` rejected the page with a `SecurityException`.

Running the script before creating the first account was therefore enough to lock the installation,
which matches the reported workaround exactly.

### Implementation Completed
1. **Robust check**: `AppInitializationService.isConfigured()` now calls the new repository method
   `UsersRepository.existsByProfileAndLoginNot(Profile, String)` and explicitly excludes
   `User.SYSTEM_USER_LOGIN`. The hidden user can no longer make the application believe it is
   configured, whatever profile it ends up carrying.
2. **Source of the corruption**: the promotion statement in `sql/update_db.sql` now excludes the
   system user, and a following statement resets `profile` to `NULL` for it, which repairs the
   installations already broken by a previous run of the script.
3. **Test fixtures**: `sql/create_test_data.sql` seeded the system user with the `ADMIN` profile and
   re-forced it in its `ON CONFLICT DO UPDATE` clause. As it runs *after* `update_db.sql`, it undid
   the repair and kept the integration database in the very state that causes the bug, which is one
   more reason why no test could ever reproduce it. The seed now leaves that profile `NULL`.

### Tests
- `FirstAdminSetupIntegrationTest` test `3.2` was a placeholder: it asserted that the system user
  *carried* the ADMIN profile and then only stated in comments that this should not count. It is now
  a real regression test that puts the database in the broken state (system user promoted, no other
  administrator) and asserts that `isConfigured()` stays `false`. Verified to **fail** before the fix
  (`expected: <false> but was: <true>`) and to pass after it. This is why the existing test suite never
  caught the bug.
- Test `3.3` asserted `assertEquals("redirect:/login", "redirect:/login")`, a tautology that can never
  fail: removed.
- `AppInitializationServiceTest`: updated to the new repository method.
- The corrected SQL was executed against PostgreSQL 12 on a table holding the system user without a
  profile, a legacy user without a profile and a user that already had one. After simulating the old
  broken script, the corrected statements restored `system` to `NULL`, left `legacy_admin` as `ADMIN`
  and did not touch `already_ok`; a second run changed nothing.
- Because that seed is shared by every integration and functional test, both full suites were run in
  the docker environment rather than a filtered selection. Integration: **522 tests, 0 failure**.
- The functional suite runs against the WAR mounted into the `tomcat` container
  (`./extract/target/extract##2.3.3.war`), so it was rebuilt and the container restarted before the
  run; otherwise the result would have described a WAR predating these changes. Result:
  `178 tests, 10 failures, 35 errors, 5 skipped`, **strictly identical to the baseline** measured
  with the changes stashed, so none of them comes from this work. Those failures are environmental:
  Selenium cannot start a browser in the container (`Driver server process died prematurely`) and the
  Python functional tests do not find the `parameters.json` that a real interpreter run would create.
  In other words this suite cannot currently validate any UI behaviour, on this branch or before it.

### Conclusion
A fresh installation can again create its first administrator, whether or not `update_db.sql` has been
run, and installations already broken by the script are repaired by running the updated version.
