# ISSUE_374 - Add a description to processes

## Status: COMPLIANT

### Issue Description

Processes could only be identified by their name and sequence of tasks. Administrators now need an optional
free-text description to document a process, see it in the process list, and find the process by that description.

### Implementation Completed

| File | Change |
| --- | --- |
| `domain/Process.java` | Adds the nullable `description` property as `varchar(4000)`, including accessors and copying it in `createCopy()` (374-1). |
| `web/model/ProcessModel.java` | Maps `description` from the domain object to the form model and back to the domain object on creation and update (374-1). |
| `web/validators/ProcessValidator.java` | Keeps the field optional and rejects descriptions longer than 4,000 characters with a localized validation error (374-1). |
| `templates/pages/processes/details.html` | Adds the localized, escaped multiline Description field directly below Name, with a matching `maxlength` and read-only rendering (374-1). |
| `templates/pages/processes/list.html` | Adds an escaped Description column between Name and Tasks. A null description renders as an empty cell, without placeholder text (374-2, 374-4). |
| `sql/update_db.sql` | Adds `processes.description VARCHAR(4000)` with `ADD COLUMN IF NOT EXISTS` (374-1). |
| `messages_fr/de/en.properties` | Adds the Description header, form label and length error in all supported languages, and clarifies that the list filter also searches descriptions and tasks. |

### Decision: descriptions are copied with a process

Duplicating a process now retains its description, as the description documents the process itself just as its
operators and task configuration do. The clone still receives the existing copy-name suffix and a new identifier.

### Scope: filtering stays client-side

The process list is an unpaged DataTables table. `processesList.js` calls DataTables' existing global
`table.search(...)`, which searches every rendered table column. Adding the Description cell to the same table
therefore makes descriptions searchable without a second filtering mechanism or a server-side query (374-3).

### Tests

`ProcessModelDescriptionTest` (unit) proves a description round-trips through `ProcessModel`, null stays null,
and a copied process retains it. `ProcessValidatorTest` covers the optional field, the exact 4,000-character
boundary, and rejection at 4,001 characters. `ProcessDescriptionIntegrationTest` posts a process through the real
controller, flushes and clears the persistence context, then reads the description back from PostgreSQL.

### Documentation / i18n impact

- French, German and English contain the new form, table and validation strings.
- `docs/features/architecture.md` documents the new nullable `PROCESSES.description` column.
- `docs/features/admin-guide.md` documents entering, copying, listing and filtering descriptions.
- Database migration: idempotent addition of `processes.description VARCHAR(4000)`.
- `docs/assets/dev-guide/data-model.png` could not be regenerated for this change.

### Conclusion

Administrators can enter an optional description for each process, see it safely rendered in the list, and use the
existing process filter to match it. Empty descriptions remain visibly empty, and duplicated processes preserve
their documentation.
