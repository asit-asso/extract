# ISSUE_367 - Include the client details in the parameters

## Status: ✅ COMPLIANT

### Issue Description
Integrators need the client contact information (address, e-mail, telephone) inside their
extraction processes. Until now the connectors collapsed every contact item into the single
multiline `p_clientdetails` column, so the only way to exploit them was to query the Extract
database directly. The contact information had to be exploded into dedicated columns and
exposed in the `parameters.json` of the FME Form V2, FME Flow V2 and Python extraction plugins.

### Acceptance Criteria
| Identifier | Description |
| --- | --- |
| 367-1 | The requests table is modified as per the specification |
| 367-2 | Backward compatibility is ensured, the entries already present in the requests table are modified as well |
| 367-3 | The `parameters.json` file of the 3 aforementioned extraction plugins is extended and the new parameters can be used directly in the extractions |
| 367-4 | Any other plugin using this information must keep working |

### Implementation Completed
1. **Database (367-1)**: added the `p_clientaddress` (multiline, first line holds the street and
   number, last line holds the ZIP code and the locality), `p_clientemail` and `p_clientphone`
   columns, mapped on the `Request` entity and documented in `docs/features/architecture.md`.
2. **Backfill (367-2)**: `sql/update_db.sql` adds the columns with `ADD COLUMN IF NOT EXISTS` and
   splits the existing `p_clientdetails` blobs back into the three columns. Each line is classified
   by its content: a line holding an e-mail token becomes the e-mail address, a line made only of
   digits and punctuation (optionally behind a `Tel:` label) **and holding at least six digits**
   becomes the telephone number — the digit floor keeps a ZIP code left alone by an empty locality
   (`1880`, `75008`) in the address rather than turning it into a telephone number — and the
   remaining lines form the postal address. The statement only fills rows whose three columns are
   still `NULL`, which makes it idempotent and prevents it from overwriting connector-provided data.
3. **Import chain**: `Easysdiv4` now extracts the `sdi:addressstreet1`, `sdi:addressstreet2`,
   `sdi:zip`, `sdi:locality`, `sdi:phone` and `sdi:email` nodes into a `ContactDetails` holder that
   keeps the parts apart. The values travel through `IProduct`, `ProductsProcessor`, `Request` and
   `TaskProcessorRequest` down to the task plugins. `ContactDetails.getEmail()` only exposes a value
   that has the shape of an e-mail address (`Easysdiv4.looksLikeEmailAddress`); any other content of
   the `sdi:email` element is logged and kept in the legacy details string only, so that it can never
   become a notification recipient.
4. **Plugins (367-3)**: the three serializers now write `ClientAddress`, `ClientEmail` and
   `ClientPhone` right after `ClientName` — `FmeDesktopV2Plugin`, `FmeServerV2Request` and
   `PythonPlugin`. The two FME plugins expose the key names through their `config.properties`, as
   they already did for the other properties. The nine localized `help.html` pages were updated.
5. **Compatibility (367-4)**: `getClientAddress()` and `getClientPhone()` are **`default` methods**
   on `IProduct` and `ITaskProcessorRequest`, so connectors and task processors written against the
   previous version of the interfaces still compile and run; they simply report an unknown value.
   `getClientEmail()` is the abstract method introduced by #366 (PR #447), implemented by every
   request class of the bundled plugins; this change reuses it instead of declaring a second one. The legacy
   `clientDetails` value is left strictly unchanged — `ContactDetails.toDetailsString()` rebuilds
   the very same CRLF-separated string — so the e-mail, QGIS Print and legacy FME plugins are
   unaffected.

### Tests
- `Easysdiv4ContactDetailsTest` (new): verifies the splitting of a complete contact, the handling
  of a second street line, the absence of a contact element, and above all that collapsing the
  parts back produces the unchanged legacy `clientDetails` string.
- `FmeServerV2RequestTest.testPropertiesContainClientInfo`: extended with the three new properties,
  and new `testClientContactIsNullForARequestThatDoesNotCarryIt`, which pins the 367-4 path: a
  request coming from a connector that ignores the contact details yields the three keys with a
  `null` value instead of failing.
- `FmeDesktopV2PluginTest.testParametersContainClientContactDetails` (new): reads back the
  generated `parameters.json` and asserts the three properties. This is the only plugin whose key
  names come exclusively from `config.properties`, so a missing mapping would produce a `null` JSON
  field name here.
- `PythonPluginIntegrationTest.testParametersJsonCreationWithAllMetadata`: extended with the three
  new properties.
- The migration was executed against PostgreSQL 12 on thirteen representative `p_clientdetails`
  shapes (full contact, address only, two street lines, `Tel:`/`Mail:` labelled, empty, `NULL`,
  LF-only separators, ZIP without locality, five-digit foreign ZIP, six-digit unlabelled number,
  four-digit labelled extension, label without punctuation, street name starting with the label
  word): all were classified correctly. In particular a bare ZIP code (`1880`, `75008`) stayed in
  the address, a short but explicitly labelled `Tel : 1234` was recognised as a telephone number,
  `Tel 021 123 45 67` was recognised without any punctuation after the label, and `Telstrasse 5`
  remained an address line. A row whose e-mail had already been set by an operator was left
  untouched, and a second run of the whole block updated no row at all.
- Backfill left all three columns `NULL` on an already updated database (reported in review). Cause:
  the block runs at the end of `sql/update_db.sql`, and an older statement of that script was not
  replayable: it dropped the `fk_processes_usergroups_*` constraints from `requests_users` while
  re-creating them on `requests_usergroups`, so every run after the first one failed on
  `ADD CONSTRAINT ... already exists`. `psql` without `ON_ERROR_STOP` (the Docker setup) carries on,
  but a client that stops at the first error or runs the script in one transaction (pgAdmin,
  DBeaver, `psql -v ON_ERROR_STOP=1`, `--single-transaction`) never reached the backfill. The drops
  now target `requests_usergroups`. Verified on a schema created by Hibernate with a pre-existing
  request: three consecutive runs with `ON_ERROR_STOP=1`, one of them in a single transaction,
  finish with no error and fill the three columns.
- `extract-connector-easysdiv4/pom.xml`: the `unit-tests` profile passed `${skipTests}`, whose
  module default is `true`, so this module's tests were silently skipped. It now sets `false`, like
  the other modules, which activates the 52 tests of the connector.

### Conclusion
The four criteria are satisfied: the table carries the three new columns, the pre-existing rows are
backfilled idempotently, the three extraction plugins expose the new parameters, and the plugins
consuming the historical client fields keep working unchanged.

### Rebase on #366 (PR #447)

This change and #366 both needed the client's e-mail address. After #447 was rebased on `master` and
fixed, this branch was rebased on top of it and its duplicate plumbing was dropped: the `default`
`getClientEmail()` on both interfaces, the `clientEmail` field/accessors already present on
`Product`, `Request` and `TaskProcessorRequest`, and the separate `p_clientemail` migration line.
The only e-mail-specific contribution kept here is the structured extraction itself: #447's
`getClientEmailFromXpath` helper (which re-read the same `client/contact/address` element) was
removed in favour of `ContactDetails`, and its safety net (trimming, shape check of the value) and
its `Easysdiv4Test` cases now exercise `buildContactDetailsFromXpath(...).getEmail()`.
