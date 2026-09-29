# ISSUE_366 - Notify the client from the e-mail notification plugin

## Status: COMPLIANT

### Issue Description

When a task requires manual validation, the diffuser wants to notify the client that processing will take
longer, without stopping the workflow. The "Notification e-mail" task plugin could not do this because the
client's e-mail address was never exposed anywhere in the application: it existed only buried inside the free-text
`clientDetails` address blob imported from the connector, with no dedicated field and no placeholder to use it as
a recipient.

### Implementation Completed

| File | Change |
| --- | --- |
| `connectors/common/IProduct.java` | Adds `getClientEmail()` next to the existing `getClient()`/`getClientGuid()`/`getClientDetails()` getters (366-1). |
| `plugins/common/ITaskProcessorRequest.java` | Adds `getClientEmail()` so task plugins can read the client's e-mail address (366-1). |
| `connectors/easysdiv4/Product.java`, `docs/extract-connector-sample/.../Product.java` | Add the `clientEmail` field and its accessors, mirroring `client`/`clientGuid`/`clientDetails` (366-1). |
| `connectors/easysdiv4/Easysdiv4.java` | Extracts the client's e-mail address as its own field, reusing the same `client/contact/address` node already located for `clientDetails` and reading its `sdi:email` child via `getElementsByTagName`, the same way `buildAddressDetailsFromXpath` already does internally, instead of leaving the address folded into that free-text blob (366-1). |
| `domain/Request.java` | Adds the nullable `clientEmail` property, persisted as `p_clientemail` (`varchar(255)`) (366-1). |
| `batch/processor/ProductsProcessor.java` | Maps `product.getClientEmail()` to `request.setClientEmail(...)` when importing a request (366-1). |
| `plugins/implementation/TaskProcessorRequest.java` | Maps `Request.getClientEmail()` so task plugins receive it through `ITaskProcessorRequest` (366-1). |
| `ArchiveRequest`, `FmeDesktopRequest`, `FmeDesktopV2Request`, `FmeServerRequest`, `QGISPrintRequest`, `RejectRequest`, `RemarkRequest`, `ValidationRequest`, `docs/extract-task-sample/.../SampleRequest.java` | Implement the new `getClientEmail()` interface method, mirroring `getClient()`/`getClientGuid()` (mechanical, no behavior change for these plugins) (366-1). |
| `sql/update_db.sql` | Adds `requests.p_clientemail VARCHAR(255)` with `ADD COLUMN IF NOT EXISTS`. |
| `plugins/email/EmailPlugin.java` | Resolves `{...}` placeholders in the recipient list ("to") the same way it already does for the subject and body, before parsing it into individual addresses. This is what makes `{clientEmail}` (and any other authorized field) usable as a recipient, not just in the message content (366-1). |
| `plugins/email/properties/configEmail.properties` | Adds `clientEmail` to `authorizedFields`; corrects the placeholder syntax documented in the file header, which described the unrelated Thymeleaf `${...}` system-email mechanism (#323) instead of this plugin's actual `{...}` regex substitution. |
| `plugins/email/lang/{fr,de}/emailHelp.html` | Updates the Description paragraph with the wording requested in the issue and documents `{clientEmail}` under "Client et organisation" / "Kunde und Organisation". |
| `docs/features/architecture.md` | Documents the new `p_clientemail` column in the REQUESTS data model table. |

### Decision: reuse the existing `clientDetails` XPath, extract the e-mail the same way `buildAddressDetailsFromXpath` does

The easySDI v4 order XML already exposes the client's e-mail inside the same `client/contact/address` element that
`clientDetails` reads. The first implementation added a second XPath key pointing straight at the `sdi:email`
child (`.../address/sdi:email`) and reused the plugin's plain `getXMLNodeLabelFromXpath` helper. A throwaway smoke
test against a realistic order XML fragment caught that this silently extracts nothing: `sdi:` is a literal prefix
on a non-namespace-aware DOM, so `getElementsByTagName("sdi:email")` (used everywhere else in this file) matches it
by literal tag name, but a raw XPath step written as `sdi:email` tries to resolve "sdi" as an XPath namespace
prefix, which is unbound here and matches nothing. The fix drops the redundant XPath key entirely and adds a
dedicated `getClientEmailFromXpath` method that locates the address node exactly like
`buildAddressDetailsFromXpath` (reusing `getOrders.xpath.clientDetails`) and then reads its `sdi:email` child via
`getElementsByTagName`, the same DOM API already proven to work for that element. `clientDetails` itself is left
untouched (it still aggregates the full postal address, phone and e-mail as free text) for backward compatibility
with any existing plugin configuration relying on it.

### Decision: substitute placeholders in the recipient list, not just subject/body

The acceptance criterion requires `{clientEmail}` to work as a *recipient*. `EmailPlugin.replaceRequestVariables`
already resolves every field in `authorizedFields` generically; special-casing `{clientEmail}` alone in the "to"
field would have meant either a second, parallel substitution mechanism or hard-coding one field name, both of
which are less maintainable than reusing the exact mechanism already applied to the subject and body. Every
`{authorizedField}` now works the same way regardless of which of the three fields it appears in.

### Decision: fail gracefully when the client's e-mail is unknown

A missing `clientEmail` (connector doesn't provide one, or the request predates this change) resolves to an empty
string, which `EmailValidator` rejects as an invalid address; the address is silently dropped from the recipient
list rather than throwing, matching the plugin's existing behavior for any other invalid address in "to". If it
was the only recipient, the task fails with the existing "no valid addressee" message instead of crashing.

### Tests

`VariableReplacementTest` covers `{clientEmail}` resolving to the mocked request's address and to an empty string
when absent. `EmailPluginTest` exercises the acceptance criterion end-to-end: `to = "{clientEmail}"` with a
resolvable address reaches the sending step (not rejected as "no addressee"), and the same template with no
known client e-mail is treated as having no valid recipient rather than failing unexpectedly.
`Easysdiv4Test` gained two tests for the (private, reflection-invoked) `getClientEmailFromXpath` method: extraction
from a realistic order XML fragment, and the missing-e-mail case resolving to an empty string. The full reactor was
recompiled (`mvn clean package`) to verify every `ITaskProcessorRequest` implementer across all task-processor
modules compiles against the new interface method, and the true full-reactor unit test suite was run
(`mvn test -Punit-tests -DskipTests=false`, since the connector/task-processor modules default `skipTests` to
`true` and are not otherwise covered by `-Punit-tests` alone): all 15 modules built successfully, 2906 tests
across the reactor, 0 failures, 0 errors. Integration tests were run via `docker-compose-test.yaml` per project
convention (`mvn verify -Pintegration-tests`): 523 tests, 0 failures, 0 errors.

A throwaway standalone smoke test (a small Java program run against a synthetic order XML fragment reproducing the
real `client/contact/address/sdi:email` structure) was used first to verify `getClientEmailFromXpath` actually
extracts the e-mail; it is what caught the first, broken XPath-based implementation described above before it
shipped, and its assertion was then turned into the two permanent `Easysdiv4Test` cases above.

### Documentation / i18n impact

- French and German plugin help pages document the new `{clientEmail}` placeholder and the updated Description
  text requested by the issue. No English help page exists for this plugin (falls back to French, pre-existing
  behavior, unrelated to this change).
- `docs/features/architecture.md` documents the new nullable `REQUESTS.p_clientemail` column.
- Database migration: idempotent addition of `requests.p_clientemail VARCHAR(255)`.

### Conclusion

The "Notification e-mail" task plugin can now notify the client of a request directly, by adding `{clientEmail}`
to its recipient list, without interrupting the task chain. The e-mail address flows end-to-end from the easySDI
v4 order XML through the domain model to the plugin, fails safe when unknown, and every other task-processor
plugin's request object was updated mechanically to keep implementing the (now slightly larger)
`ITaskProcessorRequest` interface.
