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
package ch.asit_asso.extract.plugins.fmedesktop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PluginConfiguration class.
 */
@DisplayName("PluginConfiguration")
public class PluginConfigurationTest {

    private static final String CONFIG_FILE_PATH = "plugins/fme/properties/configFME.properties";

    private PluginConfiguration configuration;

    @BeforeEach
    void setUp() {
        configuration = new PluginConfiguration(CONFIG_FILE_PATH);
    }

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("Loads configuration from valid path")
        void loadsConfigurationFromValidPath() {
            PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);

            assertNotNull(config);
            assertDoesNotThrow(() -> config.getProperty("maxFmeInstances"));
        }

        @Test
        @DisplayName("Handles invalid path gracefully")
        void handlesInvalidPathGracefully() {
            assertThrows(NullPointerException.class, () -> {
                new PluginConfiguration("nonexistent/path/config.properties");
            });
        }
    }

    @Nested
    @DisplayName("getProperty tests")
    class GetPropertyTests {

        @Test
        @DisplayName("Returns maxFmeInstances property")
        void returnsMaxFmeInstancesProperty() {
            String value = configuration.getProperty("maxFmeInstances");

            assertNotNull(value);
            assertEquals("8", value);
        }

        @Test
        @DisplayName("Returns paramPath property")
        void returnsParamPathProperty() {
            String value = configuration.getProperty("paramPath");

            assertNotNull(value);
            assertEquals("path", value);
        }

        @Test
        @DisplayName("Returns paramPathFME property")
        void returnsParamPathFMEProperty() {
            String value = configuration.getProperty("paramPathFME");

            assertNotNull(value);
            assertEquals("pathFME", value);
        }

        @Test
        @DisplayName("Returns paramInstances property")
        void returnsParamInstancesProperty() {
            String value = configuration.getProperty("paramInstances");

            assertNotNull(value);
            assertEquals("instances", value);
        }

        @Test
        @DisplayName("Returns paramRequestFolderOut property")
        void returnsParamRequestFolderOutProperty() {
            String value = configuration.getProperty("paramRequestFolderOut");

            assertNotNull(value);
            assertEquals("FolderOut", value);
        }

        @Test
        @DisplayName("Returns paramRequestPerimeter property")
        void returnsParamRequestPerimeterProperty() {
            String value = configuration.getProperty("paramRequestPerimeter");

            assertNotNull(value);
            assertEquals("Perimeter", value);
        }

        @Test
        @DisplayName("Returns paramRequestParameters property")
        void returnsParamRequestParametersProperty() {
            String value = configuration.getProperty("paramRequestParameters");

            assertNotNull(value);
            assertEquals("Parameters", value);
        }

        @Test
        @DisplayName("Returns paramRequestProduct property")
        void returnsParamRequestProductProperty() {
            String value = configuration.getProperty("paramRequestProduct");

            assertNotNull(value);
            assertEquals("Product", value);
        }

        @Test
        @DisplayName("Returns paramRequestOrderLabel property")
        void returnsParamRequestOrderLabelProperty() {
            String value = configuration.getProperty("paramRequestOrderLabel");

            assertNotNull(value);
            assertEquals("OrderLabel", value);
        }

        @Test
        @DisplayName("Returns paramRequestInternalId property")
        void returnsParamRequestInternalIdProperty() {
            String value = configuration.getProperty("paramRequestInternalId");

            assertNotNull(value);
            assertEquals("Request", value);
        }

        @Test
        @DisplayName("Returns paramRequestClientGuid property")
        void returnsParamRequestClientGuidProperty() {
            String value = configuration.getProperty("paramRequestClientGuid");

            assertNotNull(value);
            assertEquals("Client", value);
        }

        @Test
        @DisplayName("Returns paramRequestOrganismGuid property")
        void returnsParamRequestOrganismGuidProperty() {
            String value = configuration.getProperty("paramRequestOrganismGuid");

            assertNotNull(value);
            assertEquals("Organism", value);
        }

        @Test
        @DisplayName("Returns paramInputData property")
        void returnsParamInputDataProperty() {
            String value = configuration.getProperty("paramInputData");

            assertNotNull(value);
            assertEquals("SourceDataset_FILEGDB", value);
        }

        @Test
        @DisplayName("Returns null for non-existent property")
        void returnsNullForNonExistentProperty() {
            String value = configuration.getProperty("nonexistent.property");

            assertNull(value);
        }

        @Test
        @DisplayName("Throws exception for null key")
        void throwsExceptionForNullKey() {
            assertThrows(NullPointerException.class, () -> configuration.getProperty(null));
        }
    }

    @Nested
    @DisplayName("Property value validation tests")
    class PropertyValueValidationTests {

        @Test
        @DisplayName("maxFmeInstances is a valid integer")
        void maxFmeInstancesIsValidInteger() {
            String value = configuration.getProperty("maxFmeInstances");

            assertDoesNotThrow(() -> Integer.parseInt(value));
            assertTrue(Integer.parseInt(value) > 0);
        }

        @Test
        @DisplayName("All request parameters are non-empty")
        void allRequestParametersAreNonEmpty() {
            String[] requestParams = {
                "paramRequestFolderOut",
                "paramRequestPerimeter",
                "paramRequestParameters",
                "paramRequestProduct",
                "paramRequestOrderLabel",
                "paramRequestInternalId",
                "paramRequestClientGuid",
                "paramRequestOrganismGuid"
            };

            for (String param : requestParams) {
                String value = configuration.getProperty(param);
                assertNotNull(value, "Property " + param + " should not be null");
                assertFalse(value.isEmpty(), "Property " + param + " should not be empty");
            }
        }
    }

    @Nested
    @DisplayName("Configuration completeness tests")
    class ConfigurationCompletenessTests {

        @Test
        @DisplayName("All FME parameter properties exist")
        void allFmeParameterPropertiesExist() {
            String[] fmeParams = {
                "paramPath",
                "paramPathFME",
                "paramInstances"
            };

            for (String param : fmeParams) {
                String value = configuration.getProperty(param);
                assertNotNull(value, "FME parameter '" + param + "' should exist");
            }
        }

        @Test
        @DisplayName("maxFmeInstances is within reasonable range")
        void maxFmeInstancesIsWithinReasonableRange() {
            String value = configuration.getProperty("maxFmeInstances");
            int maxInstances = Integer.parseInt(value);

            assertTrue(maxInstances >= 1, "maxFmeInstances should be at least 1");
            assertTrue(maxInstances <= 100, "maxFmeInstances should be at most 100");
        }

        @Test
        @DisplayName("Configuration can be accessed multiple times")
        void configurationCanBeAccessedMultipleTimes() {
            String value1 = configuration.getProperty("maxFmeInstances");
            String value2 = configuration.getProperty("maxFmeInstances");
            String value3 = configuration.getProperty("maxFmeInstances");

            assertEquals(value1, value2);
            assertEquals(value2, value3);
        }

        @Test
        @DisplayName("Different properties return different values")
        void differentPropertiesReturnDifferentValues() {
            String paramPath = configuration.getProperty("paramPath");
            String paramPathFME = configuration.getProperty("paramPathFME");
            String paramInstances = configuration.getProperty("paramInstances");

            assertNotEquals(paramPath, paramPathFME);
            assertNotEquals(paramPath, paramInstances);
            assertNotEquals(paramPathFME, paramInstances);
        }
    }

    @Nested
    @DisplayName("Edge case tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Empty string key returns null")
        void emptyStringKeyReturnsNull() {
            String value = configuration.getProperty("");

            assertNull(value);
        }

        @Test
        @DisplayName("Property with special characters in name")
        void propertyWithSpecialCharactersInName() {
            // Try to get a property with unusual characters
            String value = configuration.getProperty("non.existent.property.with.dots");

            assertNull(value);
        }

        @Test
        @DisplayName("Multiple PluginConfiguration instances are independent")
        void multiplePluginConfigurationInstancesAreIndependent() {
            PluginConfiguration config1 = new PluginConfiguration(CONFIG_FILE_PATH);
            PluginConfiguration config2 = new PluginConfiguration(CONFIG_FILE_PATH);

            assertNotSame(config1, config2);

            String value1 = config1.getProperty("maxFmeInstances");
            String value2 = config2.getProperty("maxFmeInstances");

            assertEquals(value1, value2);
        }

        @Test
        @DisplayName("Configuration handles concurrent access")
        void configurationHandlesConcurrentAccess() throws InterruptedException {
            Thread[] threads = new Thread[10];
            String[] results = new String[10];

            for (int i = 0; i < 10; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    results[index] = configuration.getProperty("maxFmeInstances");
                });
            }

            for (Thread thread : threads) {
                thread.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            // All results should be the same
            for (String result : results) {
                assertEquals("8", result);
            }
        }
    }
}
