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
 * Integration tests for LocaleConfiguration with single language (Issue #308).
 * Tests single language configuration and multilingual mode detection.
 *
 * @author Bruno Alves
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("integration")
@TestPropertySource(properties = {"extract.i18n.language=fr"})
@DisplayName("Single language configuration tests")
public class SingleLanguageConfigurationIntegrationTest {

    @Autowired
    private LocaleConfiguration localeConfiguration;

    @Autowired
    private Locale defaultLocale;

    @Test
    @DisplayName("Single language configuration")
    public void testSingleLanguageConfiguration() {
        // Given: Configuration with extract.i18n.language=fr (single language)

        // When: Getting available locales
        List<Locale> availableLocales = localeConfiguration.getAvailableLocales();
        boolean isMultilingual = localeConfiguration.isMultilingualMode();

        // Then: Should have only one locale and multilingual should be false
        assertNotNull(availableLocales, "Available locales should not be null");
        assertEquals(1, availableLocales.size(), "Should have exactly 1 locale");
        assertEquals("fr", availableLocales.get(0).getLanguage(),
            "Single locale should be French");
        assertEquals("fr", defaultLocale.getLanguage(),
            "Default locale should be French");
        assertFalse(isMultilingual,
            "Multilingual mode should be false for single language configuration");
    }
}
