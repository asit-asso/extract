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
package ch.asit_asso.extract.email;

import ch.asit_asso.extract.domain.User;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LocaleUtils locale validation and fallback logic.
 */
public class LocaleUtilsTest {

    @Test
    public void testGetValidatedUserLocale_ExactMatch() {
        // Arrange
        User user = new User();
        user.setLogin("testuser");
        user.setLocale("de");
        List<Locale> availableLocales = Arrays.asList(
            Locale.forLanguageTag("de"),
            Locale.forLanguageTag("fr"),
            Locale.forLanguageTag("en")
        );

        // Act
        Locale result = LocaleUtils.getValidatedUserLocale(user, availableLocales);

        // Assert
        assertEquals(Locale.forLanguageTag("de"), result, "Should return exact locale match");
    }

    @Test
    public void testGetValidatedUserLocale_LanguageMatch() {
        // Arrange
        User user = new User();
        user.setLogin("testuser");
        user.setLocale("fr-CH"); // Swiss French
        List<Locale> availableLocales = Arrays.asList(
            Locale.forLanguageTag("de"),
            Locale.forLanguageTag("fr"),  // Should match "fr" from "fr-CH"
            Locale.forLanguageTag("en")
        );

        // Act
        Locale result = LocaleUtils.getValidatedUserLocale(user, availableLocales);

        // Assert
        assertEquals("fr", result.getLanguage(), "Should match language 'fr' from 'fr-CH'");
    }

    @Test
    public void testGetValidatedUserLocale_FallbackToFirst() {
        // Arrange
        User user = new User();
        user.setLogin("testuser");
        user.setLocale("dk"); // Danish - not in available locales
        List<Locale> availableLocales = Arrays.asList(
            Locale.forLanguageTag("de"),  // Should fallback to this (first)
            Locale.forLanguageTag("fr"),
            Locale.forLanguageTag("en")
        );

        // Act
        Locale result = LocaleUtils.getValidatedUserLocale(user, availableLocales);

        // Assert
        assertEquals(Locale.forLanguageTag("de"), result,
            "Should fallback to first available locale (de) when user locale (dk) not supported");
    }

    @Test
    public void testGetValidatedUserLocale_NullUserLocale() {
        // Arrange
        User user = new User();
        user.setLogin("testuser");
        user.setLocale(null);
        List<Locale> availableLocales = Arrays.asList(
            Locale.forLanguageTag("fr"),
            Locale.forLanguageTag("de")
        );

        // Act
        Locale result = LocaleUtils.getValidatedUserLocale(user, availableLocales);

        // Assert
        assertEquals(Locale.forLanguageTag("fr"), result, "Should fallback to first available locale when user locale is null");
    }

    @Test
    public void testGetValidatedUserLocale_EmptyUserLocale() {
        // Arrange
        User user = new User();
        user.setLogin("testuser");
        user.setLocale("");
        List<Locale> availableLocales = Arrays.asList(
            Locale.forLanguageTag("en"),
            Locale.forLanguageTag("fr")
        );

        // Act
        Locale result = LocaleUtils.getValidatedUserLocale(user, availableLocales);

        // Assert
        assertEquals(Locale.forLanguageTag("en"), result, "Should fallback to first available locale when user locale is empty");
    }

    @Test
    public void testGetValidatedUserLocale_NullUser() {
        // Arrange
        List<Locale> availableLocales = Arrays.asList(
            Locale.forLanguageTag("it"),
            Locale.forLanguageTag("es")
        );

        // Act
        Locale result = LocaleUtils.getValidatedUserLocale(null, availableLocales);

        // Assert
        assertEquals(Locale.forLanguageTag("it"), result, "Should fallback to first available locale when user is null");
    }

    @Test
    public void testParseAvailableLocales_ValidConfig() {
        // Arrange
        String config = "de,fr,en";

        // Act
        List<Locale> result = LocaleUtils.parseAvailableLocales(config);

        // Assert
        assertEquals(3, result.size());
        assertEquals("de", result.get(0).getLanguage());
        assertEquals("fr", result.get(1).getLanguage());
        assertEquals("en", result.get(2).getLanguage());
    }

    @Test
    public void testParseAvailableLocales_WithSpaces() {
        // Arrange
        String config = " de , fr , en ";

        // Act
        List<Locale> result = LocaleUtils.parseAvailableLocales(config);

        // Assert
        assertEquals(3, result.size());
        assertEquals("de", result.get(0).getLanguage());
        assertEquals("fr", result.get(1).getLanguage());
        assertEquals("en", result.get(2).getLanguage());
    }

    @Test
    public void testParseAvailableLocales_NullConfig() {
        // Act
        List<Locale> result = LocaleUtils.parseAvailableLocales(null);

        // Assert
        assertEquals(1, result.size());
        assertEquals("fr", result.get(0).getLanguage(), "Should default to French when config is null");
    }

    @Test
    public void testParseAvailableLocales_EmptyConfig() {
        // Act
        List<Locale> result = LocaleUtils.parseAvailableLocales("");

        // Assert
        assertEquals(1, result.size());
        assertEquals("fr", result.get(0).getLanguage(), "Should default to French when config is empty");
    }

    @Test
    public void testGetDefaultLocale() {
        // Arrange
        List<Locale> availableLocales = Arrays.asList(
            Locale.forLanguageTag("es"),
            Locale.forLanguageTag("pt")
        );

        // Act
        Locale result = LocaleUtils.getDefaultLocale(availableLocales);

        // Assert
        assertEquals(Locale.forLanguageTag("es"), result, "Should return first locale in the list");
    }

    @Test
    public void testGetDefaultLocale_EmptyList() {
        // Act
        Locale result = LocaleUtils.getDefaultLocale(Arrays.asList());

        // Assert
        assertEquals("fr", result.getLanguage(), "Should fallback to French when list is empty");
    }

    @Test
    public void testGetDefaultLocale_NullList() {
        // Act
        Locale result = LocaleUtils.getDefaultLocale(null);

        // Assert
        assertEquals("fr", result.getLanguage(), "Should fallback to French when list is null");
    }

    @Test
    public void testFallbackScenario_UserDanishSystemGermanFrench() {
        // This tests your specific example: user has "dk", system supports "de,fr"
        // Expected: fallback to "de" (first in list)

        // Arrange
        User user = new User();
        user.setLogin("danish_user");
        user.setLocale("dk");
        List<Locale> availableLocales = Arrays.asList(
            Locale.forLanguageTag("de"),  // German
            Locale.forLanguageTag("fr")   // French
        );

        // Act
        Locale result = LocaleUtils.getValidatedUserLocale(user, availableLocales);

        // Assert
        assertEquals("de", result.getLanguage(),
            "When user locale is 'dk' and system supports 'de,fr', should fallback to 'de' (first available)");
    }
}
