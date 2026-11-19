# ISSUE_381 - Dropdown lists are not sorted alphabetically

## Status: COMPLIANT

### Issue Description

The content of the application's drop-downs was not sorted in case-insensitive
alphabetical order. Two distinct causes coexisted:

1. Byte-wise sorting. A PostgreSQL `ORDER BY name` compares bytes, which places all
   uppercase letters before all lowercase ones (`Zulu` before `beta`). JavaScript's
   `Array.prototype.sort()` behaves the same way.
2. No sorting at all. Some lists came back in database insertion order.

### Acceptance Criteria

| Identifier | Description |
| --- | --- |
| 381-1 | The contents of all drop-downs in the application are sorted in case-insensitive alphabetical order |

### Design decision: sorting is done in Java, not in SQL

A SQL `ORDER BY` delegates sorting to the PostgreSQL server's collation, which varies
from one Extract installation to another:

- a PostgreSQL built against glibc sorts `épsilon` next to `e`;
- a PostgreSQL built against musl (the `postgres:12-alpine` image used by the tests,
  for instance) has virtually no collation support and falls back to byte order, which
  places `épsilon` after `Zulu`;
- `ORDER BY UPPER(name)` fixes the case issue but remains subject to that collation for
  accented characters.

The resulting order would therefore depend on the deployment. Sorting is consequently
performed in the JVM, by the new class `ch.asit_asso.extract.utils.AlphabeticalOrder`.
It relies on a `java.text.Collator` with `PRIMARY` strength, which ignores both case and
accents (`é` = `e`, `Ä` = `a`). The order is thus identical across all installations,
and consistent with the client-side sorting done by
`localeCompare(..., { sensitivity: 'base' })`.

The lists involved hold at most a few hundred entries: the cost of in-memory sorting is
negligible.

### Implementation Completed

Sorting stays in the persistence layer, through `default` methods on the repositories.
Controllers remain thin and there is no duplication.

| Dropdown | Page | Fix |
| --- | --- | --- |
| Process filter | Home | `ProcessesRepository.findAllSortedByName()` |
| Connector filter | Home | `ConnectorsRepository.findAllSortedByName()` |
| User groups | Process editing | `UserGroupsRepository.findAllSortedByName()` |
| Users | Process editing | `UsersRepository.findAllActiveApplicationUsersSortedByName()` |
| User groups | Request details | same (shared consumer) |
| Users | Request details | same (shared consumer) |
| Users | Group details | `UsersRepository.findAllApplicationUsersSortedByName()` |
| Process in rules | Connector editing | `ProcessesRepository.findAllSortedByName()`, replacing an unsorted `findAll()` |
| Predefined remarks | Process editing | `RemarkRepository.findAllSortedByTitle()` |
| Filter by type | Connector list | `connectorsList.js`: `localeCompare(..., { sensitivity: 'base' })` |

The "users" and "groups" dropdowns are shared by three controllers
(`ProcessesController`, `RequestsController`, `UserGroupsController`). The fix is
applied at the source, in the repositories, which handles all of them at once without
duplication.

### Scope extension: table sorting

The issue only mentions drop-downs, but the tables (DataTables) suffered from the same
defect, visibly. Their default sorting lowercases the text, so case handling was
correct, but then compares code points, which pushed any accented letter after `z`
(`Ärgerlich` sorted after `Zurich`). The connectors, processes and groups tables were
affected.

Letting a table sort differently from the drop-down on the same page would have been
inconsistent. DataTables sorting is therefore also delegated to
`localeCompare(..., { sensitivity: 'base' })`, in `datatableConfig.js`, at the single
place where the shared configuration is built. Both the `string` and `html` types are
overridden, because DataTables classifies as `html` any cell containing a link, which is
the case for the "Name" column of most tables.

Date columns are not affected: their sort value is a zero-padded 15-character digit
string (`_getTimestampStringSortValue`), which sorts identically whatever the
comparator. The "Processed requests" table sorts server-side and is not affected.

### Scope: elements deliberately left unchanged

| Element | Reason |
| --- | --- |
| Filters on the "Users and rights" page (role, state, notifications, 2FA) | These are static options (hard-coded `<option>` elements with i18n labels), rendered in DOM order; select2 does not re-sort them. No byte-wise sorting applies to them, contrary to what the issue assumed. Sorting them alphabetically would degrade usability ("Yes / No" would become "No / Yes"), as their order is logical, not alphabetical. |
| Properties highlighted at validation (Settings) | The select2 only rehydrates the values already selected; there is no candidate list to sort. |
| Connector state panel (`connectorsStateContainer`) | This is not a drop-down but a status panel. |

### Tests

`AlphabeticalOrderTest` (new, unit) covers the ordering rule itself, without a database:
case-insensitivity, accent-insensitivity (an accented letter must not be pushed after `z`),
null names sorted last, the source collection left untouched, the empty and single-entry
cases, and the fact that a fresh comparator is handed out on every call (a `Collator`
cannot be shared between threads).

`DropdownOrderingIntegrationTest` (new) covers the six data sources:

- sorting of connectors, processes, user groups and remarks;
- sorting of active users and of all application users;
- filtering non-regression: the system user remains excluded, and so do inactive users;
- edge cases: single entry, empty result, lowercase not pushed after all uppercase,
  accented letter sorted next to its base letter (`Ärger, avion, Eau, élan, figue,
  Zurich`).

The test datasets deliberately mix cases and an accent (`zebra`, `Alpha`, `beta`,
`Zulu`, `épsilon`), so that byte-wise sorting and the expected sorting produce different
results.

`TableOrderingFunctionalTest` (new) covers the browser side. The tables are sorted by
DataTables, in the browser: no Java test can reach that code, and the project has no
JavaScript test harness, so driving a real browser is the only way to cover the ordering
override installed by `datatableConfig.js`. The test seeds mixed-case and accented names,
sorts the processes and the user groups tables on their name column, and checks the order
the browser actually renders, in both directions.

`UserGroupsListIntegrationTest` and `UserGroupManagementIntegrationTest` were aligned
with the new finders.

The `connectorsList.js` change (the type filter of the connector list) is the one part
left without an automated test: that column shows the label of the connector plugin, and
Extract ships a single connector plugin, so the drop-down legitimately holds one entry and
no ordering can be exercised against real data. The comparator itself is the same one the
tables use, and is covered by `TableOrderingFunctionalTest`.

### Documentation / i18n impact

- i18n: no new label, the change only affects an ordering. The fr/de/en parity is
  unchanged.
- `docs/features/architecture.md`: a new "Alphabetical ordering of the lists" subsection
  was added under "Technical details". The data model and the flows are unchanged, but
  the way every browsable list is ordered is now a cross-cutting convention (a server-side
  `Collator` and a client-side `localeCompare`, deliberately not a SQL `ORDER BY`), which
  a developer adding a new list needs to know about.
- Database: no migration.

### Conclusion

Criterion 381-1 is satisfied for all data-driven drop-downs, as well as for the tables.
Sorting is performed in Java by `AlphabeticalOrder` on the server side and by
`localeCompare` on the client side, which yields the same order whatever the database
collation and whatever the interface language.
