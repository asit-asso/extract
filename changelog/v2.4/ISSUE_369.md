# ISSUE_369 - Add the organism name to the "Demandes traitées" datatable

## Status: COMPLIANT

### Issue Description

On the home page, the "Demandes traitées" table showed the customer of an order but not the organism they
placed it for. Two orders placed by namesakes belonging to different organisms could not be told apart,
and an order could not be found by searching for its organism.

The issue asks to concatenate the two values into the customer cell, and only when they differ:

```
{customer} ({organism})       e.g. Prénom Nom (Ma boîte SA)
DB: p_client (p_organism)
```

Every order placed from a personal account carries the same string as the customer and as the organism.
Showing both would then repeat the name twice, so the organism is left out in that case.

### Acceptance Criteria

| Identifier | Description |
| --- | --- |
| 369-1 | Le client et l'organisme apparaissent dans la table "demandes traitées" lorsqu'ils diffèrent |
| 369-2 | L'organisme n'apparaît pas lorsqu'il est exactement le même que le client |
| 369-3 | Une recherche par nom d'organisme fonctionne dans la table "demandes traitées" |

### Implementation Completed

| File | Change |
| --- | --- |
| `web/model/RequestModel.java` | New `getCustomerNameWithOrganism(Locale)`, which appends the organism to the customer when the two differ (369-1) and returns the customer alone otherwise (369-2). |
| `web/model/json/RequestJsonModel.java` | The `customerName` property that the tables read is now fed by that method. |
| `persistence/specifications/RequestSpecification.java` | `containsText(...)` now also matches `p_organism` (369-3). |
| `messages_fr/de/en.properties` | New key `requestsList.customer.withOrganism` = `{0} ({1})`. |

### Why the search had to change too

The table is paged, sorted and filtered by the database, not by the browser. The string shown in the
customer cell is built when the row is serialized and is never persisted, so a `LIKE` on `p_client` alone
would never match an organism. The criteria therefore gained a predicate of their own on `p_organism`.

A `LIKE` on a null column yields null rather than true, so an order without an organism is still returned
by a search on its customer. No `coalesce` is needed.

### Why the formatting is not in getCustomerName()

Centralizing the concatenation in `RequestModel.getCustomerName()` would have been shorter, but the
details page of an order **already shows the organism on its own line**, right above the customer
(`requests/details.html`). Reusing that getter would have displayed the organism twice on that page. The
formatted string is therefore exposed by a separate method, which only the tables read.

### Scope: what was deliberately left alone

| Element | Reason |
| --- | --- |
| Sorting by organism | `RequestSort` whitelists the sortable columns and the customer column sorts on `p_client`. The issue asks for the organism to be *displayed* and *searchable*, not to be sorted on. |
| The details page of an order | It shows the customer and the organism on separate lines already, and keeps reading the raw values. |
| `requestsList.js` | The customer cell renders `customerName` as escaped text. The value changed, not the rendering, so the column definition is untouched. |

### Equality is exact

The issue states that the organism is hidden when it is "exactement le même" as the customer, so the two
strings are compared exactly. `Acme` and `ACME` are therefore considered different and both are shown.

### Tests

`RequestModelCustomerNameTest` (new, unit) covers the formatting rule: the organism appended when it
differs, left out when it is equal, null or blank, and the edge cases (an order without a customer must
not render the string "null", a different case is a different name). The test loads the message files the
application really ships and asserts the result in French **and** in German, so it fails if the key is
missing from a locale.

`FinishedRequestsSearchIntegrationTest` (new, integration) runs the real query against PostgreSQL: a
request is found by a fragment of its organism alone, the search ignores the case, searching the customer
still works, and a request without an organism is still found by its customer.

### Documentation / i18n impact

- i18n: one new key, `requestsList.customer.withOrganism`, present in French, German and English.
- `docs/features/architecture.md`: new "Searching the requests tables" subsection under "Technical
  details". The data model is unchanged (`p_client` and `p_organism` were already documented), but the
  columns the search covers, and the fact that the displayed customer string is computed rather than
  stored, are now written down.
- Database: no migration. No column is added and `p_client` is not rewritten.

### Conclusion

The three criteria are satisfied. The organism is displayed next to the customer when it adds information,
it is hidden when it would only repeat the customer, and a search by organism returns the matching orders.
