# ISSUE_355 - Missing navigation button on the groups page

## Status: ✅ COMPLIANT

### Issue Description
On the group management page (`/userGroups`), a "Users" button allowing navigation back
to the "Users and rights" page was missing. The users page already has a symmetrical
"Groups" button; navigation was only possible in one direction without going back
through the menu.

### Acceptance Criteria
| Identifier | Description |
| --- | --- |
| 355-1 | The new button is implemented as in the mockup and is functional (back-and-forth navigation between groups and users is possible without going through the menu) |

### Implementation Completed
1. Added a "Users" button in `templates/pages/userGroups/list.html`, pointing to
   `/users`, modelled on the existing "Groups" button of the users page
   (`btn-extract-white` style, `fa-user` icon).
2. Externalised the label through the new i18n key `userGroupsList.users.button`,
   available in **French** (Utilisateurs), **German** (Benutzer) and English (Users).

### Tests
- `UserGroupManagementIntegrationTest`: new test `5.1b` verifying that the
  `/userGroups` page renders the navigation button to `/users`.

### Conclusion
Criterion 355-1 is satisfied: back-and-forth navigation between groups and users is
possible without going through the menu.
