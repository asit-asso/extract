/*
 * Copyright (C) 2026 asit-asso
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.asit_asso.extract.unit.web.model;

import java.util.stream.Stream;
import ch.asit_asso.extract.web.model.LoginSupportLink;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the support-link model shown on the login page (issue #370).
 */
@DisplayName("Login support link model (issue #370)")
class LoginSupportLinkTest {

    /**
     * Checks the protocol decision and the attributes exposed to the login template.
     *
     * @param url the administrator-configured URL
     * @param opensInNewTab whether the URL is expected to receive external-link attributes
     */
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("supportUrls")
    @DisplayName("Only HTTP(S) support links receive new-tab attributes")
    void onlyHttpUrlsOpenInNewTab(final String url, final boolean opensInNewTab) {
        LoginSupportLink link = new LoginSupportLink(url);

        assertEquals(url, link.getUrl(), "The configured value must remain the link href");
        assertEquals(opensInNewTab, LoginSupportLink.isExternalHttpUrl(url));
        assertEquals(opensInNewTab ? "_blank" : null, link.getTarget());
        assertEquals(opensInNewTab ? "noopener noreferrer" : null, link.getRel());
    }


    /**
     * Provides the protocol and blank-value cases specified for the support link.
     *
     * @return URL values and their expected external-link status
     */
    private static Stream<Arguments> supportUrls() {
        return Stream.of(
                Arguments.of(null, false),
                Arguments.of("", false),
                Arguments.of("   ", false),
                Arguments.of("mailto:support@example.com", false),
                Arguments.of("http://example.com/tickets", true),
                Arguments.of("HTTPS://example.com/tickets", true),
                Arguments.of("ftp://example.com/tickets", false),
                Arguments.of("javascript:alert('support')", false)
        );
    }
}
