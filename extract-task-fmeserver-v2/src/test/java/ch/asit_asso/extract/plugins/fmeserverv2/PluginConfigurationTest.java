package ch.asit_asso.extract.plugins.fmeserverv2;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PluginConfiguration class
 */
class PluginConfigurationTest {

    private static final String DEFAULT_CONFIG_PATH = "plugins/fmeserverv2/properties/config.properties";

    @Test
    void testDefaultConstructor() {
        PluginConfiguration config = new PluginConfiguration();

        assertNotNull(config);
        assertTrue(config.isConfigurationLoaded());
        assertTrue(config.getPropertyCount() > 0);
    }

    @Test
    void testConstructorWithValidPath() {
        PluginConfiguration config = new PluginConfiguration(DEFAULT_CONFIG_PATH);

        assertNotNull(config);
        assertTrue(config.isConfigurationLoaded());
        assertTrue(config.getPropertyCount() > 0);
    }

    @Test
    void testConstructorWithNullPath() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new PluginConfiguration(null);
        });

        assertTrue(exception.getMessage().contains("null"));
    }

    @Test
    void testConstructorWithEmptyPath() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new PluginConfiguration("");
        });

        assertTrue(exception.getMessage().contains("empty") || exception.getMessage().contains("null"));
    }

    @Test
    void testConstructorWithWhitespacePath() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new PluginConfiguration("   ");
        });

        assertNotNull(exception);
    }

    @Test
    void testConstructorWithPathTraversal() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new PluginConfiguration("../../../etc/passwd");
        });

        assertTrue(exception.getMessage().contains("invalid"));
    }

    @Test
    void testConstructorWithBackslash() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new PluginConfiguration("path\\with\\backslashes");
        });

        assertTrue(exception.getMessage().contains("invalid"));
    }

    @Test
    void testConstructorWithNonExistentPath() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            new PluginConfiguration("nonexistent/config.properties");
        });

        assertNotNull(exception);
        assertNotNull(exception.getMessage());
    }

    @Test
    void testGetPropertyWithValidKey() {
        PluginConfiguration config = new PluginConfiguration();

        // These keys should exist in the default config
        String requestIdParam = config.getProperty("paramRequestInternalId");
        assertNotNull(requestIdParam);
        assertFalse(requestIdParam.isEmpty());
    }

    @Test
    void testGetPropertyWithNonExistentKey() {
        PluginConfiguration config = new PluginConfiguration();

        String value = config.getProperty("nonexistent.key.that.does.not.exist");

        assertNull(value);
    }

    @Test
    void testGetPropertyWithNullKey() {
        PluginConfiguration config = new PluginConfiguration();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            config.getProperty(null);
        });

        assertTrue(exception.getMessage().contains("null") || exception.getMessage().contains("empty"));
    }

    @Test
    void testGetPropertyWithEmptyKey() {
        PluginConfiguration config = new PluginConfiguration();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            config.getProperty("");
        });

        assertNotNull(exception);
    }

    @Test
    void testGetPropertyWithWhitespaceKey() {
        PluginConfiguration config = new PluginConfiguration();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            config.getProperty("   ");
        });

        assertNotNull(exception);
    }

    @Test
    void testGetPropertyWithDefault() {
        PluginConfiguration config = new PluginConfiguration();

        String value = config.getProperty("nonexistent.key", "defaultValue");

        assertEquals("defaultValue", value);
    }

    @Test
    void testGetPropertyWithDefaultForExistingKey() {
        PluginConfiguration config = new PluginConfiguration();

        String value = config.getProperty("paramRequestInternalId", "defaultValue");

        assertNotNull(value);
        assertNotEquals("defaultValue", value);
    }

    @Test
    void testHasPropertyWithValidKey() {
        PluginConfiguration config = new PluginConfiguration();

        assertTrue(config.hasProperty("paramRequestInternalId"));
    }

    @Test
    void testHasPropertyWithNonExistentKey() {
        PluginConfiguration config = new PluginConfiguration();

        assertFalse(config.hasProperty("nonexistent.key"));
    }

    @Test
    void testHasPropertyWithNullKey() {
        PluginConfiguration config = new PluginConfiguration();

        assertFalse(config.hasProperty(null));
    }

    @Test
    void testHasPropertyWithEmptyKey() {
        PluginConfiguration config = new PluginConfiguration();

        assertFalse(config.hasProperty(""));
    }

    @Test
    void testHasPropertyWithWhitespaceKey() {
        PluginConfiguration config = new PluginConfiguration();

        assertFalse(config.hasProperty("   "));
    }

    @Test
    void testGetPropertyCount() {
        PluginConfiguration config = new PluginConfiguration();

        int count = config.getPropertyCount();

        assertTrue(count > 0);
        // Config should have at least the 4 required keys
        assertTrue(count >= 4);
    }

    @Test
    void testIsConfigurationLoaded() {
        PluginConfiguration config = new PluginConfiguration();

        assertTrue(config.isConfigurationLoaded());
    }

    @Test
    void testReloadConfiguration() {
        PluginConfiguration config = new PluginConfiguration();

        int initialCount = config.getPropertyCount();
        assertTrue(initialCount > 0);

        config.reloadConfiguration();

        assertTrue(config.isConfigurationLoaded());
        assertEquals(initialCount, config.getPropertyCount());
    }

    @Test
    void testReloadConfigurationWithPath() {
        PluginConfiguration config = new PluginConfiguration();

        int initialCount = config.getPropertyCount();

        config.reloadConfiguration(DEFAULT_CONFIG_PATH);

        assertTrue(config.isConfigurationLoaded());
        assertEquals(initialCount, config.getPropertyCount());
    }

    @Test
    void testReloadConfigurationWithInvalidPath() {
        PluginConfiguration config = new PluginConfiguration();

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            config.reloadConfiguration("invalid/path.properties");
        });

        assertNotNull(exception);
    }

    @Test
    void testAllRequiredPropertiesExist() {
        PluginConfiguration config = new PluginConfiguration();

        // Verify all required properties exist
        assertTrue(config.hasProperty("paramRequestInternalId"));
        assertTrue(config.hasProperty("paramRequestFolderOut"));
        assertTrue(config.hasProperty("paramRequestPerimeter"));
        assertTrue(config.hasProperty("paramRequestParameters"));
    }

    @Test
    void testPropertiesAreNotEmpty() {
        PluginConfiguration config = new PluginConfiguration();

        String requestId = config.getProperty("paramRequestInternalId");
        String folderOut = config.getProperty("paramRequestFolderOut");

        assertNotNull(requestId);
        assertNotNull(folderOut);
        assertFalse(requestId.trim().isEmpty());
        assertFalse(folderOut.trim().isEmpty());
    }

    @Test
    void testMultipleInstancesIndependent() {
        PluginConfiguration config1 = new PluginConfiguration();
        PluginConfiguration config2 = new PluginConfiguration();

        assertTrue(config1.isConfigurationLoaded());
        assertTrue(config2.isConfigurationLoaded());

        assertEquals(config1.getPropertyCount(), config2.getPropertyCount());
    }

    @Test
    void testConfigurationIsImmutableAfterLoad() {
        PluginConfiguration config = new PluginConfiguration();

        String value1 = config.getProperty("paramRequestInternalId");
        String value2 = config.getProperty("paramRequestInternalId");

        assertEquals(value1, value2);
    }
}
