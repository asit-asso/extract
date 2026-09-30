# ISSUE_368 - Better handle extractions that return no data

## Status: COMPLIANT

### Issue Description

A client request's extraction perimeter often does not exactly match the real coverage of the underlying data
(the product's declared perimeter in viageo/plans-réseaux is frequently coarser than reality). When this happens,
the extraction plugin (FME Form, FME Flow, or Python) returns no file and the task ends in error, requiring a
manual intervention even though the correct outcome is simply "no data available for this perimeter".

### Implementation Completed

An opt-in "cancel gracefully when no data" option was added to the three extraction plugins named in the issue:
`Extraction FME Form (Version 2)`, `Extraction FME Flow (Version 2)`, and `Extraction Python`. When enabled and the
extraction fails, each plugin searches its own failure output for the fixed literal marker `noDataForExtract`
(`.*noDataForExtract.*`, matched via `Pattern.quote(...)` so the literal string is search safely regardless of
regex metacharacters). If found, the task does not end in error: the request is reported as gracefully cancelled
instead, using the exact same mechanism already used by the `extract-task-reject` plugin.

| File | Change |
| --- | --- |
| `extract-task-fmedesktop-v2/.../FmeDesktopV2Plugin.java` | Adds `cancelOnNoData`/`cancellationRemark` parameters (368-1). Conditionally appends `LOG_STANDARDOUT yes` to the FME command line and captures stdout (instead of discarding it) only when the option is enabled, per the issue's requirement that FME's log may land in either stream depending on version. On the existing exit-code≠0 failure path, when enabled, searches the concatenated stderr+stdout for the marker (368-2, 368-3). The success path and the disabled-option path are unchanged (368-4, 368-5). |
| `extract-task-fmeserver-v2/.../FmeServerV2Plugin.java` | Adds the same two parameters. On the existing `TRANSFORMATION_FAILED` error branch (HTTP status ≠ 200/201), searches the already-extracted `.serviceResponse.statusInfo.message` text (no changes to the JSON parsing itself) for the marker before falling back to the unchanged error behavior (368-2, 368-3). The `NO_DOWNLOAD_URL` branch and the success path are untouched (368-4, 368-5). |
| `extract-task-python/.../PythonPlugin.java` | Adds the same two parameters. `executePythonScript()` was refactored to return a small private `ScriptExecutionOutcome` record (`success`/`cancelledNoData`/`message`) instead of a bare nullable `String`, since a plain string return could not unambiguously signal "cancelled, not an error" alongside the existing null=success/non-null=error convention. On the exit-code≠0 path, checks the combined stdout+stderr and any captured traceback text for the marker before the existing exit-code-specific error formatting (368-2, 368-3). The success path and the disabled-option path are unchanged (368-4, 368-5). |
| `CancelledExtractionRequest.java` (new, one per module: `fmedesktopv2`, `fmeserverv2`, `python` packages) | A package-private `ITaskProcessorRequest` decorator: delegates every getter to the wrapped original request except `isRejected()` (hardcoded `true`) and `getRemark()` (returns the configured cancellation remark). |
| `.../lang/{fr,de,en}/messages.properties` (all 3 modules) | Adds labels/help text for the two new parameters and a new "request cancelled, no data found" result message, in all three locales. |
| `.../lang/{fr,de,en}/help.html` (all 3 modules) | Appends the "Annuler le traitement en l'absence de données" section from the issue at the end of each plugin's help, with the plugin-specific middle paragraph (extraction log / HTTP response / stderr+stdout) and natural DE/EN translations. |
| Test files (all 3 modules) | New tests covering acceptance criteria 368-2 through 368-5 per plugin (see Tests below), plus `getParams()` assertions for the two new parameters (368-1). |

### Decision: reuse the Reject plugin's exact "rejected + remark" mechanism, no orchestrator changes

`RequestTaskRunner` already stops a request's task chain unconditionally the moment the request returned by a
task's `ITaskProcessorResult.getRequestData()` has `isRejected()==true`, and marks it for export with its
`getRemark()` — which is how the connector notifies the actual client on the originating platform (there is no
separate "notify the client" primitive to call from a task plugin; a rejected/cancelled request is only ever
communicated back to the client through the normal export flow, exactly like an explicit `extract-task-reject`
task already does today). Reusing this existing, generic mechanism means issue #368's "the request is
automatically cancelled and the client is notified" and "no subsequent tasks run" requirements are satisfied by
every plugin with zero changes to the orchestrator.

### Decision: a lean per-module decorator instead of a field-by-field request copy

None of the three plugins had a ready-to-use `ITaskProcessorRequest` implementation with a copy-constructor and
`setRejected`/`setRemark` (unlike `RejectRequest`/`RemarkRequest`/etc. in their own modules). Rather than writing
an ~18-field copy class per plugin — fragile, and silently wrong forever if a future interface field is added and
one copy is missed — each module gets a small `CancelledExtractionRequest` decorator that delegates every method
to the original request except the two that need to change. `extract-task-fmeserver-v2`'s existing
`FmeServerV2Request` class was deliberately left untouched: it is a wrapper used only to build the outgoing
GeoJSON body, not an alternate `ITaskProcessorRequest` implementation, and reusing it for this purpose would have
mixed two unrelated responsibilities.

### Decision: the cancellation remark is not a globally required parameter

The issue describes the remark field as required "if the option is activated" (an expandable, opt-in UI section).
Marking it `req=true` at the parameter-schema level would force every administrator editing an *existing*,
already-configured task of these three plugins to suddenly fill in a field they never intended to use, since the
field is genuinely new and no existing task configuration has a value for it. Instead, the field stays optional at
the schema level, and each plugin defensively requires it to be non-blank (in addition to the checkbox being
enabled) before attempting the no-data cancellation; if the checkbox is on but the remark is blank, the extraction
falls through to its normal, unchanged error behavior rather than crashing or silently discarding the failure.
No collapsible/expandable UI widget was built for the "option dépliable" from the Figma mockups: no such
conditional-field-visibility mechanism exists anywhere in this codebase's process task configuration form today,
and introducing one was judged disproportionate to this specific, backend-focused issue; the two parameters render
through the existing generic boolean/multitext parameter form.

### Tests

Each of the three plugins gained tests covering:
- **368-2**: option enabled, extraction fails, output contains `noDataForExtract` → result status `SUCCESS`,
  `getRequestData().isRejected()==true`, `getRequestData().getRemark()` equals the configured remark.
- **368-3**: option enabled, extraction fails, output does not contain the marker → result status `ERROR`,
  unchanged from before this issue.
- **368-4**: option enabled, extraction succeeds (even if the marker text is reachable somewhere) → normal
  success, request not rejected.
- **368-5**: option disabled, extraction fails with the marker present → result status `ERROR`, marker ignored.

`FmeDesktopV2PluginTest` drives a real subprocess test-double script. `FmeFlowV2PluginTest` drives the plugin's
real HTTP response-handling code via a reflection-based invocation of the private `processErrorResponse` method
(this plugin has no existing HTTP-mocking seam) for 368-2/368-3/368-5, and a real local JDK `HttpServer` for
368-4 to prove the full success path is untouched. `PythonPluginTest` runs a real Python interpreter against a new
`no_data_script.py` fixture (mirroring the existing `error_script.py` test), skipping gracefully if no interpreter
is available in the environment running the tests.

Full reactor verification: `mvn install` (15 modules) — `BUILD SUCCESS`. `mvn test -Punit-tests -DskipTests=false`
(full reactor including the connector/task-processor modules, which default `skipTests=true`) — 0 failures, 0
errors. Integration tests via `docker-compose-test.yaml` (`mvn verify -Pintegration-tests`) — 0 failures, 0 errors.

### Documentation / i18n impact

- French, German and English help pages for all three plugins document the new option, each with its
  plugin-specific wording (extraction log for FME Form, HTTP response for FME Flow, stderr/stdout for Python) as
  specified in the issue.
- No database schema changes.

### Conclusion

An administrator can now configure any of the three extraction plugins to gracefully cancel a request instead of
failing it, when the extraction reports the fixed `noDataForExtract` marker in its own failure output. The
cancellation reuses the exact same "rejected + remark" path that `extract-task-reject` already relies on, so no
orchestrator or export-side changes were necessary, and the option has no effect at all unless explicitly enabled
with a non-blank cancellation remark.
