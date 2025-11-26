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
 * Integration tests for LocaleConfiguration (Issue #308).
 * Tests locale configuration loading, default locale selection, and multilingual mode detection.
 *
 * @author Bruno Alves
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("integration")
@TestPropertySource(properties = {"extract.i18n.language=de,fr,en"})
public class LocaleConfigurationIntegrationTest {

    @Autowired
    private LocaleConfiguration localeConfiguration;

    @Autowired
    private Locale defaultLocale;

    @Test
    @DisplayName("Default locale loaded from configuration")
    public void testDefaultLocaleLoadedFromConfiguration() {
        // Given: Configuration with extract.i18n.language=de,fr,en (set by @TestPropertySource)

        // When: Spring loads the configuration

        // Then: The default locale should be the first in the list (de)
        assertNotNull(defaultLocale, "Default locale should not be null");
        assertEquals("de", defaultLocale.getLanguage(),
            "Default locale should be 'de' (first in configuration list)");
    }

    @Test
    @DisplayName("Supported locales loaded from configuration")
    public void testSupportedLocalesLoadedFromConfiguration() {
        // Given: Configuration with extract.i18n.language=de,fr,en (set by @TestPropertySource)

        // When: Getting available locales
        List<Locale> availableLocales = localeConfiguration.getAvailableLocales();

        // Then: All 3 locales should be present
        assertNotNull(availableLocales, "Available locales should not be null");
        assertEquals(3, availableLocales.size(),
            "Should have 3 configured locales (de, fr, en)");

        // Verify each locale is present
        assertTrue(availableLocales.stream().anyMatch(l -> "de".equals(l.getLanguage())),
            "German locale should be available");
        assertTrue(availableLocales.stream().anyMatch(l -> "fr".equals(l.getLanguage())),
            "French locale should be available");
        assertTrue(availableLocales.stream().anyMatch(l -> "en".equals(l.getLanguage())),
            "English locale should be available");
    }

    @Test
    @DisplayName("Multilingual mode detection")
    public void testMultilingualModeDetection() {
        // Given: Configuration with extract.i18n.language=de,fr,en (set by @TestPropertySource)

        // When: Checking if multilingual mode is enabled
        boolean isMultilingual = localeConfiguration.isMultilingualMode();

        // Then: Multilingual mode should be true
        assertTrue(isMultilingual,
            "Multilingual mode should be enabled when multiple languages are configured");
    }
}
