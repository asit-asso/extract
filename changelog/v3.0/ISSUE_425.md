# ISSUE_425 - Saving a process could overwrite a task of another process

## Status: COMPLIANT

### Issue Description

Editing a process could overwrite a task of a **different** process. The reported symptoms are two tasks at the
same position in the other process, a Python task left without its parameters, and the processes page answering a
500 to every user until the `tasks` table is repaired by hand.

The reported chronology, with real identifiers:

```
14:10:06  ProcessModel - Adding a task (ID: 1473) at position 6.
14:10:34  ProcessesController - Updating the process # 1426 has succeeded.
14:10:34  ERROR ErrorController - java.lang.IllegalArgumentException: The updated data object cannot be null.
              at PluginItemModelParameter.validateUpdatedValue
              at PluginItemModel.setParametersValuesFromMap
              at ProcessModel.fromDomainObjectsCollection
              at ProcessesController.viewList
```

### What actually happened

Three defects lined up.

1. **A temporary identifier that looks real.** A task added through the interface is given
   `ProcessModel.getTemporaryRuleId()`, the highest identifier of the form plus one. For the process 1426 that
   value was 1473, which is the identifier of the Python task of the process 1629.
2. **A lookup that ignores the process.** `TaskModel.saveInDataSource()` fetched that identifier with
   `taskRepository.findById()`, over the whole table, and found the task of the other process.
   `updateDomainTask()` then wrote the position and the parameters of the added task into it, without touching its
   `id_process`: the task stayed in the process 1629, at the position of the process 1426, with the parameters of
   the archive plugin. The task the administrator wanted was never created.
3. **A display that cannot survive it.** Reading that task back builds its model from the Python plugin, whose
   `pythonInterpreter` and `pythonScript` parameters are mandatory and no longer stored.
   `PluginItemModelParameter.validateUpdatedValue()` throws, and since the processes list builds the model of
   every process, one corrupted task took the whole page down for everyone.

The browser is only one trigger among others. The reproduction done locally needs no tampering at all: creating a
process and adding any task to it is enough, since the temporary identifier of the first task of a new process is
1, and a task carrying the identifier 1 exists in any installation whose first task has not been deleted since.

### Implementation Completed

| File | Change |
| --- | --- |
| `web/model/TaskModel.java` | `saveInDataSource()` no longer queries the whole table. A task tagged `ADDED` is always created, with a null identifier so that the sequence assigns a real one; any other task is looked for among the tasks of the process being saved only, and an identifier that is none of them is refused. |
| `web/model/ProcessModel.java` | New `getForeignTaskIds()`, which reports the submitted identifiers that do not belong to the process. `getTemporaryRuleId()` renamed `getTemporaryTaskId()`, and its comment fixed: it described rules, and claimed the value was ignored on save, which was exactly the bug. |
| `web/controllers/ProcessesController.java` | The submission is checked before anything is written, when a process is updated **and** when one is created, so a refused form leaves the data source exactly as it was. Creating a process saves its row before its tasks, so without that check a submission claiming an identifier left an empty process behind and answered a 500. |
| `web/model/PluginItemModel.java` | A stored value that no longer fits the plugin leaves the parameter empty and logs a warning, instead of throwing. A null map of values is accepted as well. |
| `messages_fr/de/en.properties` | New key `processDetails.errors.task.foreign`. |

### Why the save is refused rather than repaired

An identifier that belongs to another process can no longer come from a legitimate action: what makes a task new
is now its tag. Turning such a block into a new task would hide a submission that cannot be trusted, so it is
refused, with a message asking to reload the page. The check runs before the first write, so nothing is half
saved. `TaskModel.saveInDataSource()` refuses it too, so that a caller that skips the check cannot write either.

### Scope: the unique constraint suggested in the issue is deliberately left out

The issue suggests a unique constraint on `(id_process, position)`. It cannot be added as such: the tasks are
saved one after the other, each in its own transaction, and no transaction spans the loop. Swapping two tasks
therefore makes them share a position for a moment, which the local reproduction confirms, and a non-deferrable
constraint would reject a plain reordering. It would also have to be declared on the entity for the test schema
that Hibernate generates, where only a non-deferrable one can be produced. Making the whole save transactional and
adding a deferred constraint is a change of its own, worth doing, but not one to slip into a bug fix.

### Scope: connector rules

The rules of a connector are edited through the same mechanism, and `ConnectorsController.updateConnectorRules()`
does honour the `ADDED` tag, which is why the defect never showed there. It still fetches an existing rule by its
identifier alone, over the whole table, so a rule of another connector could be moved that way. This is a distinct
case, **not** covered by this fix: no such incident has been reported and the change belongs to its own commit.

### Tests

`TaskOwnershipTest` (new, unit) covers the model: a task tagged `ADDED` whose identifier is the one of another
process's task leaves it alone and is saved without an identifier, an untagged foreign identifier is refused
without any write, a task of the process is still updated in place, `getForeignTaskIds()` reports the foreign
identifiers and ignores the temporary ones, and a task whose stored parameters are those of another plugin can
still be read. The test hands the foreign task to whoever fetches it by its identifier alone, so that the old
lookup makes the test fail.

`ProcessTaskOwnershipIntegrationTest` (new, integration) runs the real controller against PostgreSQL, and covers
the save path only: the added task does not touch the other process and gets an identifier of its own, a foreign
identifier is refused with nothing written when a process is updated, and creating a process with a task claiming
an identifier writes neither the process nor the task.

Each of these tests was run against the unpatched code, and each fails there.

The tolerant read has no integration test on purpose. The task plugins are not discoverable from the context the
integration tests run in, so a task is dropped before its parameters are ever read: such a test answers 200
whatever the code does. One was written, an assertion on the rendered page showed it was proving nothing, and it
was removed rather than kept as false evidence. That path is covered by the two unit tests above, and was checked
on a deployed instance: with the two tasks of a process holding the parameters of other plugins in the database,
the processes list answers 200 and shows the parameters empty, where it answered 500 before.

### Finding the installations that are already hit

A corrupted task only takes the page down when its plugin has mandatory parameters, as the Python one has. When it
has none, as the operator validation has, the task keeps working with the settings of another plugin and nothing
is reported: the corruption is silent. An installation that has never answered a 500 can be affected all the same,
so the tasks have to be looked at rather than waited for. A task whose stored parameters do not match the ones its
plugin declares, or a process holding two tasks at the same position, is one of them.

### Repairing by hand

Unchanged: the `tasks` table has to be fixed, as described in the issue. One trap to avoid, met while reproducing
the bug. Every identifier of the application comes from the same `hibernate_sequence`; giving a repaired row an
identifier that the sequence has not reached yet makes the next insertion fail on the primary key, and not
necessarily in `tasks`. Reuse an identifier that has already been consumed, or move the sequence past the highest
identifier of **every** table that draws from it.

`sql/create_test_data.sql` did that computation over six tables only, leaving out `usergroups`, `rules`,
`remarks`, `recovery_codes` and `remember_me_tokens`. On a database where one of those holds the highest
identifier, the seeded sequence was set below it and the next row written there failed on its primary key. The
five tables are now part of the computation, each guarded by `to_regclass` so the script still runs on a schema
that lacks one. The value handed to the sequence is unchanged in principle: the highest identifier plus one, taken
across all of them.

### Documentation / i18n impact

- i18n: one new key, `processDetails.errors.task.foreign`, in French, German and English.
- `docs/features/architecture.md`: new "Saving the tasks of a process" subsection, which states the two rules to
  keep (the tag decides what is new, the lookup stays inside the process), what the connector rules do differently,
  why positions are not protected by a constraint, and what the shared sequence implies, both for the collision
  itself and for anyone repairing rows by hand.
- Database: no migration.

### Conclusion

A process can no longer write into a task that is not its own, a submission that references one is refused before
anything is saved, and a task whose parameters were corrupted no longer takes the processes page down.
