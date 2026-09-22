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
package ch.asit_asso.extract.unit.web.controllers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ch.asit_asso.extract.configuration.I18nConfiguration;
import ch.asit_asso.extract.persistence.UsersRepository;
import ch.asit_asso.extract.web.controllers.LoginController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Rendered login-page tests for the configured support link (issue #370).
 */
@WebMvcTest(controllers = LoginController.class, properties = {
        "extract.i18n.language=fr",
        "extract.support.url=https://example.com/tickets"
})
@AutoConfigureMockMvc(addFilters = false)
@Import(I18nConfiguration.class)
@ActiveProfiles("test")
@DisplayName("Login page support link (issue #370)")
class LoginControllerSupportLinkTest {

    /**
     * The HTTP(S) support URL injected from the test application property.
     */
    private static final String HTTPS_SUPPORT_URL = "https://example.com/tickets";

    /**
     * The regular expression selecting the support anchor from the rendered response.
     */
    private static final Pattern SUPPORT_LINK_PATTERN = Pattern.compile(
            "<a\\b[^>]*\\bid=\\\"support-link\\\"[^>]*>.*?</a>", Pattern.DOTALL);

    /**
     * The Spring MVC client rendering the real Thymeleaf login page.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Dependency of the application's locale resolver, not exercised by anonymous login rendering.
     */
    @MockBean
    private UsersRepository usersRepository;

    /**
     * The controller whose property is varied for each rendered scenario.
     */
    @Autowired
    private LoginController loginController;


    /**
     * Restores the URL received from Spring's {@code @Value} injection before every scenario.
     */
    @BeforeEach
    void restoreConfiguredUrl() {
        ReflectionTestUtils.setField(this.loginController, "supportUrl", LoginControllerSupportLinkTest.HTTPS_SUPPORT_URL);
    }


    /**
     * Verifies the HTTP(S) link uses the configured href and safe new-tab attributes.
     *
     * @throws Exception when Spring MVC cannot render the page
     */
    @Test
    @DisplayName("An HTTP(S) support URL is rendered in a new tab")
    void rendersHttpSupportLinkInNewTab() throws Exception {
        String supportLink = this.renderSupportLink();

        assertThat(supportLink).contains("href=\"https://example.com/tickets\"");
        assertThat(supportLink).contains("target=\"_blank\"");
        assertThat(supportLink).contains("rel=\"noopener noreferrer\"");
        assertThat(supportLink).contains(">Support</a>");
    }


    /**
     * Verifies mail links stay in the current browser tab without external-link attributes.
     *
     * @throws Exception when Spring MVC cannot render the page
     */
    @Test
    @DisplayName("A mailto support URL has no new-tab attributes")
    void rendersMailtoSupportLinkWithoutNewTabAttributes() throws Exception {
        ReflectionTestUtils.setField(this.loginController, "supportUrl", "mailto:support@example.com");

        String supportLink = this.renderSupportLink();

        assertThat(supportLink).contains("href=\"mailto:support@example.com\"");
        assertThat(supportLink).doesNotContain("target=");
        assertThat(supportLink).doesNotContain("rel=");
    }


    /**
     * Verifies an empty or whitespace-only setting renders no support anchor.
     *
     * @throws Exception when Spring MVC cannot render the page
     */
    @Test
    @DisplayName("A blank support URL renders no support link")
    void omitsSupportLinkForBlankUrl() throws Exception {
        ReflectionTestUtils.setField(this.loginController, "supportUrl", "   ");

        MvcResult result = this.mockMvc.perform(get("/login"))
                                       .andExpect(status().isOk())
                                       .andReturn();

        assertThat(result.getResponse().getContentAsString()).doesNotContain("id=\"support-link\"");
    }


    /**
     * Renders the login page and extracts its support anchor.
     *
     * @return the rendered support anchor
     * @throws Exception when Spring MVC cannot render the page
     */
    private String renderSupportLink() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/login"))
                                       .andExpect(status().isOk())
                                       .andReturn();
        Matcher matcher = LoginControllerSupportLinkTest.SUPPORT_LINK_PATTERN.matcher(
                result.getResponse().getContentAsString());

        assertThat(matcher.find()).as("support link rendered").isTrue();
        return matcher.group();
    }
}
