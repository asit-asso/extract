# ISSUE_373 - Add an operator URL to File Archiving

## Status: COMPLIANT

### Issue Description

The File Archiving plugin copies extracted files to the archive path visible to Extract. When that path is a
server-side mount, it may not be usable by an operator. The plugin now accepts a separate operator-facing URL,
keeps copying to the archive path, and exposes the operator URL in the request processing history.

### Acceptance Criteria

| Identifier | Implementation |
| --- | --- |
| 373-1 | `ArchivePlugin.getParams()` declares the optional, 255-character `operatorUrl` text parameter after the archive path, with French, German and English labels. |
| 373-2 | The operator URL is passed to the same `buildPathWithPropertyValues(...)` method as the archive path. |
| 373-3 | The archive destination remains `path`; a non-blank resolved operator URL replaces it only in the successful history message. |
| 373-4 | The request details enhancement makes only values beginning with `http://` or `https://` links; paths, `file://` and other schemes remain escaped plain text. |
| 373-5 | Every recognized archive location has a right-aligned copy button that writes the displayed value to the clipboard and briefly changes to a check icon. |

### Implementation Completed

| File | Change |
| --- | --- |
| `ArchivePlugin.java`, `configArchivage.properties` | Added optional `operatorUrl`; it uses the existing placeholder resolver and is selected only for the successful display message. Copying continues to use the resolved archive path. |
| Archive plugin `messages.properties` and `archivageHelp.html` (fr, de, en) | Added the parameter label and the operator URL help text. English resources now exist alongside French and German resources. |
| `RequestHistoryRecord.java` | Identifies the persisted French, German and English archive success prefixes and exposes the location for the request-details view without changing the stored message contract. |
| `requests/details.html`, `requestDetails.js`, `extract.css` | Renders an escaped archive location, lets the history model classify case-insensitive HTTP(S) values, and creates the external link client-side only from that classification; also adds the right-aligned copy control. |
| `messages_fr/de/en.properties` | Added the localized copy-button tooltip. |

### Decision: browser links remain deliberately narrow

The page creates an anchor only when the displayed location starts with `http://` or `https://`, case-insensitively.
It does not trim, normalize or validate the value further: a filesystem path, `file://`, `javascript:` or any other
scheme remains plain text. The URL and path originate from request data, so both the initial text and the data
attribute are escaped by Thymeleaf; the JavaScript creates links through DOM APIs rather than HTML interpolation.

### Tests

`ArchivePluginTest` covers the optional parameter definition, every supported placeholder in the operator URL,
copying to the archive path while reporting the URL, and fallback to the archive path when the URL is absent or
blank. `LocalizedMessagesTest` checks all three parameter-label translations.

`RequestHistoryRecordTest` verifies recognition of the three localized archive success messages and the HTTP(S)
boundary that leaves `file://`, `javascript:` and filesystem paths non-linkable.

The link and clipboard enhancement is client-side. Browser verification must open a request detail page with one
plain archive path and one HTTPS archive location, then confirm the link, attributes and copy acknowledgement.

### Documentation / i18n impact

- Plugin help and labels are available in French, German and English.
- `docs/features/architecture.md` documents `operatorUrl`, its placeholders and its display-only role.
- Database: no migration; the existing `request_history.last_msg` contract remains unchanged.

### Conclusion

Operators can be shown an accessible archive URL without changing where Extract stores files. Every displayed
archive location remains copyable, and only safe HTTP(S) values become external links.
