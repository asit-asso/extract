# ISSUE_428 - Saving a connector could move a rule of another connector

## Status: COMPLIANT

### Issue Description

Twin of #425, on the connector side. Saving a connector wrote into whichever rule carried the identifier that the
form sent, wherever that rule lived.

### What the reproduction showed

Worse than the issue described. One save destroys **two** rules:

| Before | After |
| --- | --- |
| connector "Bouchon" holds rule 1, `orderlabel == "AAA"` | rule 1 now belongs to the edited connector, criteria overwritten |
| edited connector holds its own rule 15 | rule 15 **deleted** |
| | "Bouchon" left with no rule at all |

The rule is not copied, it is **moved**: `RuleModel.updateDomainRule()` calls `setConnector(domainConnector)`. The
connector it came from silently loses the rule that routed part of its requests. The rule of the edited connector
that the block stood for is then deleted as a leftover, because it is no longer among the submitted identifiers.

Two more defects were met on the way, both on the same page, both answering a 500 where a message was due:

1. **A rule identifier that exists nowhere.** `updateConnectorRules()` threw
   `UnsupportedOperationException("Impossible to move a rule that does not exist")`, after the rules already
   handled by the loop had been written.
2. **A rule without a process.** `RuleValidator` rejected the field `idProcess`, while the property is named
   `processId`. Spring answered `NotReadablePropertyException`, so leaving the process of a rule empty — a plain
   user mistake — took the page down instead of showing "Veuillez associer un traitement à la règle".

### Why the interface did not show it more often

Unlike the tasks of a process, `updateConnectorRules()` did honour the `ADDED` tag, so adding a rule through the
interface has always created it, whatever identifier the form made up. What was left exposed is an **untagged**
block carrying a foreign identifier: a stale form, a browser restoring a value into a field of the same name — the
forms of two connectors are identical — or a forged request.

### Implementation Completed

| File | Change |
| --- | --- |
| `web/model/RuleModel.java` | New `isNew()`, which reads the tag and **only** the tag. An identifier is not enough to tell a new rule from an existing one, and must not be: an untagged rule whose identifier is missing, negative or unknown is refused rather than created, or the check below could be walked around by sending an invalid identifier. |
| `web/model/ConnectorModel.java` | New `getForeignRuleIds()`, in two forms: against the rules the connector holds when it is updated, against nothing when it is created, since a connector being created holds no rule. The comment of `getTemporaryRuleId()` was fixed: it claimed the value it returns is ignored on save, which was exactly the bug. |
| `web/controllers/ConnectorsController.java` | `updateConnectorRules()` looks for a rule among the rules of the connector being saved, never in the whole table. Both the update and the creation check the whole submission before anything is written. The creation no longer fetches rules by identifier at all. |
| `web/validators/RuleValidator.java` | Rejects `processId`, the name the property actually has. |
| `messages_fr/de/en.properties` | New key `connectorDetails.errors.rule.foreign`. |

### Scope: rules submitted at creation

A connector being created holds no rule, and the interface offers no way to add one before the first save: the
rules panel only appears once the connector exists. A submission that carries rules at creation can therefore only
come from a forged or stale request. Those claiming an identifier are refused; those tagged as added are ignored,
which is what the previous code did too.

### Tests

`RuleOwnershipTest` (new, unit) covers the model: the tag alone makes a rule new, an untagged rule is not new even
without a usable identifier, a foreign identifier is reported, own rules are accepted, the temporary identifier of
an added rule is not reported, and every identifier is reported when the connector is being created.

`ConnectorRuleOwnershipIntegrationTest` (new, integration) runs the real controller against PostgreSQL: a foreign
identifier is refused and both connectors come out untouched, an identifier that exists nowhere is refused instead
of failing, creating a connector with a rule claiming an identifier writes neither the connector nor the rule, and
a rule added with the identifier of another connector's rule is created with an identifier of its own.

Run against the unpatched code, three of those four integration cases fail: two save-through where a refusal was
due, and the unknown identifier answers 500. The fourth guards the tag path, which was already correct.

### Finding the installations that are already hit

A connector that lost a rule reports nothing, and neither does a task left with the settings of another plugin
when that plugin has no mandatory parameter. An installation that has never answered a 500 can be affected all the
same, so the data has to be looked at rather than waited for. Three traces to look for:

- two tasks sharing a position in the same process;
- a task whose stored parameters are the ones another plugin expects;
- a connector left without any rule.

Read-only queries for those three checks are posted on the issue itself rather than shipped here: they read what
the plugins of a given installation usually store, so they are leads to look at and not verdicts, and they belong
to the operators running the repair, not to this fix.

### Documentation / i18n impact

- i18n: one new key, `connectorDetails.errors.rule.foreign`, in French, German and English.
- `docs/features/architecture.md`: the "Saving the tasks of a process" subsection said the second rule was not
  enforced on the connector side. It now is, and the subsection says so, renamed to cover both.
- Database: no migration.

### Conclusion

A connector can no longer write into a rule that is not its own, a submission that references one is refused
before anything is saved, and the two ways this page used to answer a 500 now answer a message.
