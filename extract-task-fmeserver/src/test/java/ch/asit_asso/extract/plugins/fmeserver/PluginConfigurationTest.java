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
package ch.asit_asso.extract.plugins.fmeserver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the PluginConfiguration class.
 */
@DisplayName("PluginConfiguration")
class PluginConfigurationTest {

    private static final String CONFIG_FILE_PATH = "plugins/fmeserver/properties/config.properties";

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

            String value = config.getProperty("paramUrl");
            assertNotNull(value);
            assertEquals("url", value);
        }

        @Test
        @DisplayName("getProperty returns null for non-existing key")
        void getPropertyReturnsNullForNonExistingKey() {
            PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);

            String value = config.getProperty("nonExistentKey");
            assertNull(value);
        }

        // Note: Cannot test getProperty with unloaded config because constructor throws NPE for invalid path
    }

    @Nested
    @DisplayName("Configuration values tests")
    class ConfigurationValuesTests {

        @Test
        @DisplayName("paramUrl property is configured")
        void paramUrlPropertyIsConfigured() {
            PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);

            assertEquals("url", config.getProperty("paramUrl"));
        }

        @Test
        @DisplayName("paramLogin property is configured")
        void paramLoginPropertyIsConfigured() {
            PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);

            assertEquals("login", config.getProperty("paramLogin"));
        }

        @Test
        @DisplayName("paramPassword property is configured")
        void paramPasswordPropertyIsConfigured() {
            PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);

            assertEquals("pass", config.getProperty("paramPassword"));
        }

        @Test
        @DisplayName("paramRequestFolderOut property is configured")
        void paramRequestFolderOutPropertyIsConfigured() {
            PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);

            assertEquals("FolderOut", config.getProperty("paramRequestFolderOut"));
        }

        @Test
        @DisplayName("paramRequestPerimeter property is configured")
        void paramRequestPerimeterPropertyIsConfigured() {
            PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);

            assertEquals("Perimeter", config.getProperty("paramRequestPerimeter"));
        }

        @Test
        @DisplayName("paramRequestParameters property is configured")
        void paramRequestParametersPropertyIsConfigured() {
            PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);

            assertEquals("Parameters", config.getProperty("paramRequestParameters"));
        }

        @Test
        @DisplayName("paramRequestProduct property is configured")
        void paramRequestProductPropertyIsConfigured() {
            PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);

            assertEquals("Product", config.getProperty("paramRequestProduct"));
        }

        @Test
        @DisplayName("paramRequestOrderLabel property is configured")
        void paramRequestOrderLabelPropertyIsConfigured() {
            PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);

            assertEquals("OrderLabel", config.getProperty("paramRequestOrderLabel"));
        }

        @Test
        @DisplayName("paramRequestInternalId property is configured")
        void paramRequestInternalIdPropertyIsConfigured() {
            PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);

            assertEquals("Request", config.getProperty("paramRequestInternalId"));
        }

        @Test
        @DisplayName("paramRequestClientGuid property is configured")
        void paramRequestClientGuidPropertyIsConfigured() {
            PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);

            assertEquals("Client", config.getProperty("paramRequestClientGuid"));
        }

        @Test
        @DisplayName("paramRequestOrganismGuid property is configured")
        void paramRequestOrganismGuidPropertyIsConfigured() {
            PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);

            assertEquals("Organism", config.getProperty("paramRequestOrganismGuid"));
        }
    }
}
