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
package ch.asit_asso.extract.plugins.qgisprint;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PluginConfiguration
 */
class PluginConfigurationTest {

    private static final String CONFIG_FILE_PATH = "plugins/qgisprint/properties/config.properties";

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
        String value = config.getProperty("paramUrl");
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

        assertNotNull(config.getProperty("paramUrl"), "paramUrl should be configured");
        assertNotNull(config.getProperty("paramTemplateLayout"), "paramTemplateLayout should be configured");
        assertNotNull(config.getProperty("paramPathProjectQGIS"), "paramPathProjectQGIS should be configured");
        assertNotNull(config.getProperty("paramLogin"), "paramLogin should be configured");
        assertNotNull(config.getProperty("paramPassword"), "paramPassword should be configured");
        assertNotNull(config.getProperty("paramLayers"), "paramLayers should be configured");
        assertNotNull(config.getProperty("paramCRS"), "paramCRS should be configured");
    }

    @Test
    @DisplayName("Default CRS is configured")
    void testDefaultCrsConfigured() {
        PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);
        String defaultCrs = config.getProperty("defaultCRS");
        assertNotNull(defaultCrs);
        assertTrue(defaultCrs.contains("EPSG"));
    }

    @Test
    @DisplayName("GetProjectSettings URL template is configured")
    void testGetProjectSettingsUrlConfigured() {
        PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);
        String urlTemplate = config.getProperty("GetProjectSettingsParamUrl");
        assertNotNull(urlTemplate);
        assertTrue(urlTemplate.contains("SERVICE=WMS") || urlTemplate.contains("GetProjectSettings"));
    }

    @Test
    @DisplayName("GetFeature URL template is configured")
    void testGetFeatureUrlConfigured() {
        PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);
        String urlTemplate = config.getProperty("GetFeatureParamUrl");
        assertNotNull(urlTemplate);
        assertTrue(urlTemplate.contains("WFS") || urlTemplate.contains("GetFeature"));
    }

    @Test
    @DisplayName("GetPrint URL template is configured")
    void testGetPrintUrlConfigured() {
        PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);
        String urlTemplate = config.getProperty("getPrintParamUrl");
        assertNotNull(urlTemplate);
    }

    @Test
    @DisplayName("XPath configurations are present")
    void testXPathConfigurationsPresent() {
        PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);

        String atlasCoverageXpath = config.getProperty("getProjectSettings.xpath.atlasCoverageLayer");
        assertNotNull(atlasCoverageXpath);

        String gmlIdXpath = config.getProperty("getFeature.xpath.gmlId");
        assertNotNull(gmlIdXpath);
    }

    @Test
    @DisplayName("Constructor with nonexistent config file throws NullPointerException")
    void testConstructorWithNonexistentFile() {
        // When the config file doesn't exist, getResourceAsStream returns null
        // Then properties.load(null) throws NullPointerException
        assertThrows(NullPointerException.class, () ->
            new PluginConfiguration("nonexistent/path/config.properties"));
    }

    @Test
    @DisplayName("GetFeature body templates are configured")
    void testGetFeatureBodyTemplatesConfigured() {
        PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);

        String pointTemplate = config.getProperty("getFeature.body.point");
        assertNotNull(pointTemplate);

        String polygonTemplate = config.getProperty("getFeature.body.polygon");
        assertNotNull(polygonTemplate);

        String polylineTemplate = config.getProperty("getFeature.body.polyline");
        assertNotNull(polylineTemplate);
    }

    @Test
    @DisplayName("Template keys are configured")
    void testTemplateKeysConfigured() {
        PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);

        String coverageLayerKey = config.getProperty("template.coveragelayer.key");
        assertNotNull(coverageLayerKey);

        String coordinatesKey = config.getProperty("template.coordinates.key");
        assertNotNull(coordinatesKey);
    }

    @Test
    @DisplayName("GetPrint exception XPath is configured")
    void testGetPrintExceptionXPathConfigured() {
        PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);

        String exceptionXpath = config.getProperty("getprint.xpath.exception");
        assertNotNull(exceptionXpath);
    }

    @Test
    @DisplayName("Multiple calls to getProperty return same value")
    void testGetPropertyConsistency() {
        PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);

        String first = config.getProperty("paramUrl");
        String second = config.getProperty("paramUrl");

        assertEquals(first, second);
    }

    @Test
    @DisplayName("Different instances return same property values")
    void testDifferentInstancesSamePropertyValues() {
        PluginConfiguration config1 = new PluginConfiguration(CONFIG_FILE_PATH);
        PluginConfiguration config2 = new PluginConfiguration(CONFIG_FILE_PATH);

        String value1 = config1.getProperty("paramUrl");
        String value2 = config2.getProperty("paramUrl");

        assertEquals(value1, value2);
    }

    @Test
    @DisplayName("getProperty with null key throws NullPointerException")
    void testGetPropertyWithNullKey() {
        PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);
        // Properties.getProperty(null) throws NullPointerException
        assertThrows(NullPointerException.class, () -> config.getProperty(null));
    }

    @Test
    @DisplayName("getProperty with empty key returns null")
    void testGetPropertyWithEmptyKey() {
        PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);
        assertNull(config.getProperty(""));
    }

    @Test
    @DisplayName("All parameter codes are non-empty")
    void testAllParameterCodesNonEmpty() {
        PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);

        String[] paramKeys = {"paramUrl", "paramTemplateLayout", "paramPathProjectQGIS",
                              "paramLogin", "paramPassword", "paramLayers", "paramCRS"};

        for (String key : paramKeys) {
            String value = config.getProperty(key);
            assertNotNull(value, "Property " + key + " should not be null");
            assertFalse(value.isEmpty(), "Property " + key + " should not be empty");
        }
    }

    @Test
    @DisplayName("URL templates contain placeholders")
    void testUrlTemplatesContainPlaceholders() {
        PluginConfiguration config = new PluginConfiguration(CONFIG_FILE_PATH);

        String projectSettingsUrl = config.getProperty("GetProjectSettingsParamUrl");
        assertNotNull(projectSettingsUrl);
        assertTrue(projectSettingsUrl.contains("%s"), "GetProjectSettings URL should contain placeholder");

        String getFeatureUrl = config.getProperty("GetFeatureParamUrl");
        assertNotNull(getFeatureUrl);
        assertTrue(getFeatureUrl.contains("%s"), "GetFeature URL should contain placeholder");

        String getPrintUrl = config.getProperty("getPrintParamUrl");
        assertNotNull(getPrintUrl);
        assertTrue(getPrintUrl.contains("%s"), "GetPrint URL should contain placeholder");
    }
}
