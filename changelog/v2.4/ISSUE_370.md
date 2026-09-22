# ISSUE_370 - Add a support contact link to the login page

## Status: COMPLIANT

### Issue Description

Users who have forgotten their credentials or need help do not know how to contact the operator of an Extract
instance. This issue adds an optional, instance-configured support link to the login page. It may be a `mailto:`
address or a support/ticketing URL.

### Acceptance Criteria

| Identifier | Description |
| --- | --- |
| 370-1 | The `extract.support.url` parameter is available and documented. |
| 370-2 | A configured support link is shown on the login page and uses the configured value as its href. |
| 370-3 | `mailto:` links use the current tab, while HTTP(S) links use a new tab. |
| 370-4 | No support link is shown when the parameter is empty. |

### Implementation Completed

| File | Change |
| --- | --- |
| `extract/src/main/resources/application.properties` | Adds the optional, empty-by-default `extract.support.url` setting and explains its `mailto:`, HTTP(S), and empty-value behaviour (370-1, 370-4). |
| `web/controllers/LoginController.java` | Reads `extract.support.url` with `@Value`, exposes a support-link model only when the configured value is non-blank, and continues to render the existing login view (370-2, 370-4). |
| `web/model/LoginSupportLink.java` | Preserves the configured URL as the href and contains the case-insensitive HTTP(S) detection that supplies `_blank` and `noopener noreferrer` only for external HTTP(S) links (370-2, 370-3). |
| `templates/pages/login.html` | Renders the translated support anchor next to the existing forgotten-password link; `th:href` escapes the configured value and server-provided target/rel values keep `mailto:` in the current tab (370-2, 370-3, 370-4). |
| `messages_fr.properties`, `messages_de.properties`, `messages_en.properties` | Add the `login.support.link` label in every shipped language. |
| `docs/getting-started/configure.md` | Documents the new setting, its login-only scope, and each URL behaviour (370-1). |

### Decision: non-HTTP(S) values are rendered without new-tab attributes

The setting is owned by the instance administrator and the issue does not request URL validation. Every non-blank
value is rendered as configured, including `mailto:`, `ftp:`, and other schemes. Only values beginning with
`http://` or `https://`, case-insensitively, receive `target="_blank"` and `rel="noopener noreferrer"`; every other
value stays in the current tab. This avoids applying a second policy beyond the requested new-tab distinction.

### Scope

The support anchor is deliberately limited to `templates/pages/login.html`. Password-reset and two-factor pages
share the unauthenticated layout but are not part of the mock-up's login-page scope; authenticated pages and an
administrator UI for this setting remain unchanged.

### Tests

`LoginSupportLinkTest` (new, unit) covers null, empty and whitespace values; `mailto:`; lower-case HTTP; upper-case
HTTPS; FTP; and `javascript:`. It verifies that only HTTP(S) values receive the new-tab target and relation while
the href value is preserved exactly.

`LoginControllerSupportLinkTest` (new, unit Web MVC) renders the real Thymeleaf login page. It asserts that an
HTTP(S) setting produces the configured href with `_blank` and `noopener noreferrer`, a `mailto:` setting has neither
attribute, and a whitespace-only setting renders no anchor.

### Documentation / i18n impact

- `docs/getting-started/configure.md` documents `extract.support.url` next to the other `extract.*` settings.
- The `login.support.link` message is available in French, German, and English.
- Database: no schema or data change.

### Conclusion

All four acceptance criteria are met: the support URL is configurable and documented, appears only when configured
on the login page with its exact href, opens only HTTP(S) destinations in a safe new tab, and disappears for blank
values.
