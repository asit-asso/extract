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
package ch.asit_asso.extract.plugins.archive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the PluginConfiguration class.
 */
@DisplayName("PluginConfiguration")
class PluginConfigurationTest {

    private static final String CONFIG_FILE_PATH = "plugins/archivage/properties/configArchivage.properties";

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor loads configuration from valid path")
        void constructorLoadsConfigurationFromValidPath() {
            PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);

            assertNotNull(config);
        }

        @Test
        @DisplayName("Constructor throws exception for invalid path")
        void constructorThrowsExceptionForInvalidPath() {
            // The current implementation throws NullPointerException when file not found
            assertThrows(NullPointerException.class, () -> new PluginConfiguration("invalid/path.properties"));
        }
    }

    @Nested
    @DisplayName("getProperty tests")
    class GetPropertyTests {

        @Test
        @DisplayName("getProperty returns value for existing key")
        void getPropertyReturnsValueForExistingKey() {
            PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);

            String value = config.getProperty("paramPath");
            assertNotNull(value);
            assertEquals("path", value);
        }

        @Test
        @DisplayName("getProperty returns null for non-existing key")
        void getPropertyReturnsNullForNonExistingKey() {
            PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);

            String value = config.getProperty("nonExistentKey");
            assertNull(value);
        }
    }

    @Nested
    @DisplayName("Configuration values tests")
    class ConfigurationValuesTests {

        @Test
        @DisplayName("paramPath property is configured")
        void paramPathPropertyIsConfigured() {
            PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);

            assertEquals("path", config.getProperty("paramPath"));
        }

        @Test
        @DisplayName("path.properties.authorized property is configured")
        void pathPropertiesAuthorizedIsConfigured() {
            PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);

            String authorizedProperties = config.getProperty("path.properties.authorized");
            assertNotNull(authorizedProperties);
            assertTrue(authorizedProperties.contains("orderLabel"));
            assertTrue(authorizedProperties.contains("orderGuid"));
            assertTrue(authorizedProperties.contains("productGuid"));
            assertTrue(authorizedProperties.contains("productLabel"));
            assertTrue(authorizedProperties.contains("startDate"));
            assertTrue(authorizedProperties.contains("organism"));
            assertTrue(authorizedProperties.contains("client"));
        }
    }
}
