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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for LocaleConfiguration with complex language tags (Issue #308).
 * Tests support for language tags with country codes (e.g., en-US, fr-FR).
 *
 * @author Bruno Alves
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("integration")
@TestPropertySource(properties = {"extract.i18n.language=en-US,fr-FR,de-CH,it"})
@DisplayName("Complex language tag configuration tests")
public class ComplexLanguageTagConfigurationIntegrationTest {

    @Autowired
    private LocaleConfiguration localeConfiguration;

    @Autowired
    private Locale defaultLocale;

    @Test
    @DisplayName("Language tags with country codes are supported")
    public void testLanguageTagsWithCountryCodes() {
        // Given: Configuration with extract.i18n.language=en-US,fr-FR,de-CH,it

        // When: Getting available locales
        List<Locale> availableLocales = localeConfiguration.getAvailableLocales();

        // Then: All locales including country codes should be loaded
        assertNotNull(availableLocales, "Available locales should not be null");
        assertEquals(4, availableLocales.size(),
            "Should have 4 configured locales");

        // Verify en-US
        assertTrue(availableLocales.stream()
            .anyMatch(l -> "en".equals(l.getLanguage()) && "US".equals(l.getCountry())),
            "English (US) locale should be available");

        // Verify fr-FR
        assertTrue(availableLocales.stream()
            .anyMatch(l -> "fr".equals(l.getLanguage()) && "FR".equals(l.getCountry())),
            "French (France) locale should be available");

        // Verify de-CH
        assertTrue(availableLocales.stream()
            .anyMatch(l -> "de".equals(l.getLanguage()) && "CH".equals(l.getCountry())),
            "German (Switzerland) locale should be available");

        // Verify it (language only, no country)
        assertTrue(availableLocales.stream()
            .anyMatch(l -> "it".equals(l.getLanguage()) && l.getCountry().isEmpty()),
            "Italian locale should be available");

        // Default should be en-US (first in list)
        assertEquals("en", defaultLocale.getLanguage(),
            "Default locale language should be English");
        assertEquals("US", defaultLocale.getCountry(),
            "Default locale country should be US");
    }
}
