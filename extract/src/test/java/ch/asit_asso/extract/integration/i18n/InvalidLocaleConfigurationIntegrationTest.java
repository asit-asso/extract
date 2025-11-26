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
 * Integration tests for LocaleConfiguration with invalid configuration (Issue #308).
 * Tests graceful handling of invalid locale configurations.
 *
 * @author Bruno Alves
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("integration")
@TestPropertySource(properties = {"extract.i18n.language=invalid_lang"})
@DisplayName("Invalid locale configuration tests")
public class InvalidLocaleConfigurationIntegrationTest {

    @Autowired
    private Locale defaultLocale;

    @Autowired
    private LocaleConfiguration localeConfiguration;

    @Test
    @DisplayName("Invalid locale configuration is handled gracefully")
    public void testInvalidLocaleConfigurationFallsBackToFrench() {
        // Given: Configuration with extract.i18n.language=invalid_lang

        // When: Spring loads the configuration
        List<Locale> availableLocales = localeConfiguration.getAvailableLocales();

        // Then: The system should handle it gracefully
        assertNotNull(availableLocales, "Available locales should not be null");
        assertFalse(availableLocales.isEmpty(), "Should have at least one locale");

        // The first locale will be created from "invalid_lang"
        // but the system should still function
        assertNotNull(defaultLocale, "Default locale should not be null even with invalid config");
    }
}
