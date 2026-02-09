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
package ch.asit_asso.extract.plugins.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PluginConfiguration
 */
class PluginConfigurationTest {

    private static final String CONFIG_FILE_PATH = "plugins/validation/properties/config.properties";

    @Test
    @DisplayName("Constructor with valid path loads configuration")
    void testConstructorWithValidPath() {
        PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);
        assertNotNull(config);
    }

    @Test
    @DisplayName("getProperty returns value for existing key")
    void testGetPropertyWithExistingKey() {
        PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);
        String value = config.getProperty("paramValidMessages");
        assertNotNull(value);
        assertFalse(value.isEmpty());
    }

    @Test
    @DisplayName("getProperty returns null for non-existent key")
    void testGetPropertyWithNonExistentKey() {
        PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);
        String value = config.getProperty("non.existent.key");
        assertNull(value);
    }

    @Test
    @DisplayName("Required plugin parameters are configured")
    void testRequiredParametersConfigured() {
        PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);

        assertNotNull(config.getProperty("paramValidMessages"), "paramValidMessages should be configured");
        assertNotNull(config.getProperty("paramRejectMessages"), "paramRejectMessages should be configured");
    }

    @Test
    @DisplayName("dump method does not throw exception")
    void testDumpDoesNotThrow() {
        PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);
        assertDoesNotThrow(() -> config.dump());
    }

    @Test
    @DisplayName("Constructor with non-existent path throws NullPointerException during load")
    void testConstructorWithNonExistentPath() {
        // The constructor throws NullPointerException when the file doesn't exist
        // because Properties.load() receives null from getResourceAsStream()
        assertThrows(NullPointerException.class,
            () -> new PluginConfiguration("non/existent/path.properties"));
    }

    @Test
    @DisplayName("getProperty with empty key returns null")
    void testGetPropertyWithEmptyKey() {
        PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);
        assertNull(config.getProperty(""));
    }

    @Test
    @DisplayName("Multiple PluginConfiguration instances are independent")
    void testMultipleInstances() {
        PluginConfiguration config1 = new PluginConfiguration(CONFIG_FILE_PATH);
        PluginConfiguration config2 = new PluginConfiguration(CONFIG_FILE_PATH);

        assertNotNull(config1.getProperty("paramValidMessages"));
        assertNotNull(config2.getProperty("paramValidMessages"));
        assertEquals(config1.getProperty("paramValidMessages"), config2.getProperty("paramValidMessages"));
    }
}
