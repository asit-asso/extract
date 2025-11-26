/*
 * Copyright (C) 2017 arx iT
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
package ch.asit_asso.extract.integration.i18n;

import ch.asit_asso.extract.configuration.LocaleConfiguration;
import ch.asit_asso.extract.configuration.UserLocaleResolver;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.persistence.UsersRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UserLocaleResolver (Issue #308).
 * Tests browser locale detection, user preferences persistence, and fallback strategies.
 *
 * @author Bruno Alves
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Tag("integration")
@TestPropertySource(properties = {"extract.i18n.language=de,fr,en"})
public class UserLocaleResolverIntegrationTest {

    @Autowired
    private LocaleResolver localeResolver;

    @Autowired
    private LocaleConfiguration localeConfiguration;

    @Autowired
    private UsersRepository usersRepository;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private User testUser;

    @BeforeEach
    public void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        // Create a test user
        testUser = new User();
        testUser.setLogin("testuser_i18n");
        testUser.setName("Test User I18n");
        testUser.setEmail("testuser.i18n@example.com");
        testUser.setPassword("password");
        testUser.setActive(true);
        testUser.setProfile(User.Profile.OPERATOR);
        testUser.setLocale(null); // Start with no preference
        testUser = usersRepository.save(testUser);

        // Clear security context before each test
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    public void tearDown() {
        // Clean up security context
        SecurityContextHolder.clearContext();

        // Clean up test user if exists
        if (testUser != null && testUser.getId() != null) {
            User userToDelete = usersRepository.findById(testUser.getId()).orElse(null);
            if (userToDelete != null) {
                usersRepository.delete(userToDelete);
            }
        }
    }

    @Test
    @DisplayName("Browser locale detection for unauthenticated user")
    public void testBrowserLocaleDetectionForUnauthenticatedUser() {
        // Given: An unauthenticated user with Accept-Language header
        // Configuration has: de,fr,en
        request.addHeader("Accept-Language", "en-US,en;q=0.9,fr;q=0.8");

        // When: Resolving locale
        Locale resolvedLocale = localeResolver.resolveLocale(request);

        // Then: Should match browser preference (en is available)
        assertNotNull(resolvedLocale, "Resolved locale should not be null");
        assertEquals("en", resolvedLocale.getLanguage(),
            "Should match browser language preference (en)");
    }

    @Test
    @DisplayName("User preference persistence in database")
    public void testUserPreferencePersistenceInDatabase() {
        // Given: An authenticated user
        authenticateUser(testUser.getLogin());

        // When: Setting locale to 'de'
        Locale germanLocale = Locale.forLanguageTag("de");
        localeResolver.setLocale(request, response, germanLocale);

        // Then: User's preference should be persisted in database
        User updatedUser = usersRepository.findByLoginIgnoreCase(testUser.getLogin());
        assertNotNull(updatedUser, "User should exist in database");
        assertEquals("de", updatedUser.getLocale(),
            "User's locale preference should be persisted as 'de'");
    }

    @Test
    @DisplayName("Fallback strategy for authenticated user")
    public void testFallbackStrategyForAuthenticatedUser() {
        // Given: An authenticated user with no stored preference (empty string, not null)
        testUser.setLocale(""); // Empty string means no preference
        testUser = usersRepository.save(testUser);
        authenticateUser(testUser.getLogin());

        // Set browser locale to German using setPreferredLocales
        request.setPreferredLocales(Collections.singletonList(Locale.forLanguageTag("de")));
        request.addHeader("Accept-Language", "de-CH,de;q=0.9");

        // When: Resolving locale
        Locale resolvedLocale = localeResolver.resolveLocale(request);

        // Then: Should fall back to browser locale (de) or default locale
        assertNotNull(resolvedLocale, "Resolved locale should not be null");
        // The resolver will fall back to browser (de) if user has no preference
        assertTrue(resolvedLocale.getLanguage().equals("de") ||
                   resolvedLocale.getLanguage().equals(localeConfiguration.defaultLocale().getLanguage()),
            "Should fall back to browser locale (de) or default locale when no user preference is set");
    }

    @Test
    @DisplayName("Locale change interceptor persists preference")
    public void testLocaleChangeInterceptorPersistsPreference() {
        // Given: An authenticated user
        authenticateUser(testUser.getLogin());

        // When: Changing locale to Italian
        Locale italianLocale = Locale.forLanguageTag("it");
        localeResolver.setLocale(request, response, italianLocale);

        // Then: The preference should be persisted in session and database
        User updatedUser = usersRepository.findByLoginIgnoreCase(testUser.getLogin());
        assertNotNull(updatedUser, "User should exist");

        // Note: If Italian is not in available locales (de,fr,en), it will fall back
        List<Locale> availableLocales = localeConfiguration.getAvailableLocales();
        boolean italianAvailable = availableLocales.stream()
            .anyMatch(l -> "it".equals(l.getLanguage()));

        if (italianAvailable) {
            assertEquals("it", updatedUser.getLocale(),
                "User's locale should be updated to Italian if available");
        } else {
            // Should fall back to default locale (de)
            String expectedLocale = localeConfiguration.defaultLocale().toLanguageTag();
            assertEquals(expectedLocale, updatedUser.getLocale(),
                "User's locale should fall back to default if Italian not available");
        }
    }

    @Test
    @DisplayName("Browser locale matching logic")
    public void testBrowserLocaleMatchingLogic() {
        // Given: Available locales are de,fr,en
        // Browser sends en-GB (not exact match, but language matches)
        request.addHeader("Accept-Language", "en-GB,de-CH;q=0.9,it;q=0.8");

        // When: Resolving locale
        Locale resolvedLocale = localeResolver.resolveLocale(request);

        // Then: Should match en (language match)
        assertNotNull(resolvedLocale, "Resolved locale should not be null");
        assertEquals("en", resolvedLocale.getLanguage(),
            "Should match 'en' language even though country code differs (en-GB -> en)");
    }

    @Test
    @DisplayName("Unauthenticated user without browser preference")
    public void testUnauthenticatedUserWithoutBrowserPreference() {
        // Given: An unauthenticated user with no Accept-Language header
        // Note: MockHttpServletRequest has a default locale (usually en)

        // When: Resolving locale
        Locale resolvedLocale = localeResolver.resolveLocale(request);

        // Then: Should resolve to a valid locale from configured list
        assertNotNull(resolvedLocale, "Resolved locale should not be null");
        List<Locale> availableLocales = localeConfiguration.getAvailableLocales();
        boolean isAvailable = availableLocales.stream()
            .anyMatch(l -> l.getLanguage().equals(resolvedLocale.getLanguage()));
        assertTrue(isAvailable,
            "Resolved locale should be one of the configured locales (de, fr, en)");

        // It will either be the browser's default locale (if available) or the default locale
        Locale defaultLocale = localeConfiguration.defaultLocale();
        assertTrue(resolvedLocale.getLanguage().equals(defaultLocale.getLanguage()) ||
                   availableLocales.stream().anyMatch(l -> l.getLanguage().equals(resolvedLocale.getLanguage())),
            "Should be either default locale or a configured locale");
    }

    @Test
    @DisplayName("Authenticated user with stored preference takes precedence")
    public void testAuthenticatedUserStoredPreferenceTakesPrecedence() {
        // Given: An authenticated user with stored preference 'fr'
        testUser.setLocale("fr");
        testUser = usersRepository.save(testUser);
        authenticateUser(testUser.getLogin());

        // Set browser locale to 'en'
        request.addHeader("Accept-Language", "en-US");

        // When: Resolving locale
        Locale resolvedLocale = localeResolver.resolveLocale(request);

        // Then: User's stored preference (fr) should take precedence over browser (en)
        assertEquals("fr", resolvedLocale.getLanguage(),
            "User's stored preference should take precedence over browser locale");
    }

    @Test
    @DisplayName("Session locale is used when available")
    public void testSessionLocaleIsUsed() {
        // Given: A locale is explicitly set in session
        Locale germanLocale = Locale.forLanguageTag("de");
        localeResolver.setLocale(request, response, germanLocale);

        // When: Resolving locale in the same session
        Locale resolvedLocale = localeResolver.resolveLocale(request);

        // Then: Session locale should be used
        assertEquals("de", resolvedLocale.getLanguage(),
            "Session locale should be used when explicitly set");
    }

    @Test
    @DisplayName("Unavailable locale in database falls back to default")
    public void testUnavailableLocaleInDatabaseFallsBackToDefault() {
        // Given: User has a locale stored that is not in available locales
        testUser.setLocale("pt"); // Portuguese not in de,fr,en
        testUser = usersRepository.save(testUser);
        authenticateUser(testUser.getLogin());

        // When: Resolving locale
        Locale resolvedLocale = localeResolver.resolveLocale(request);

        // Then: Should fall back to default locale (de)
        Locale defaultLocale = localeConfiguration.defaultLocale();
        assertEquals(defaultLocale.getLanguage(), resolvedLocale.getLanguage(),
            "Should fall back to default locale when stored locale is not available");

        // And: Database should be updated with the fallback locale
        User updatedUser = usersRepository.findByLoginIgnoreCase(testUser.getLogin());
        assertEquals(defaultLocale.toLanguageTag(), updatedUser.getLocale(),
            "Database should be updated with fallback locale for consistency");
    }

    @Test
    @DisplayName("Browser locale not in available locales falls back to default")
    public void testBrowserLocaleNotAvailableFallsBackToDefault() {
        // Given: Browser sends a locale not in available locales (de,fr,en)
        request.addHeader("Accept-Language", "ja-JP,ja;q=0.9"); // Japanese

        // When: Resolving locale
        Locale resolvedLocale = localeResolver.resolveLocale(request);

        // Then: Should fall back to default locale (de)
        Locale defaultLocale = localeConfiguration.defaultLocale();
        assertEquals(defaultLocale.getLanguage(), resolvedLocale.getLanguage(),
            "Should fall back to default locale when browser locale not available");
    }

    /**
     * Helper method to authenticate a user in the security context.
     *
     * @param username the username to authenticate
     */
    private void authenticateUser(String username) {
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_USER")
        );
        Authentication auth = new UsernamePasswordAuthenticationToken(
            username, "password", authorities
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
