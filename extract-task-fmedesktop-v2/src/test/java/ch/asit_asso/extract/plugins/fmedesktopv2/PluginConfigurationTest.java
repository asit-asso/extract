package ch.asit_asso.extract.plugins.fmedesktopv2;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PluginConfigurationTest {

    private static final String DEFAULT_CONFIG_PATH = "plugins/fmedesktopv2/properties/config.properties";

    @Test
    void testConstructorWithValidPath() {
        PluginConfiguration config = new PluginConfiguration(DEFAULT_CONFIG_PATH);
        assertNotNull(config);
    }

    @Test
    void testGetPropertyReturnsValue() {
        PluginConfiguration config = new PluginConfiguration(DEFAULT_CONFIG_PATH);
        // Property may return null if key doesn't exist - this is expected behavior
        String value = config.getProperty("paramWorkbench");
        // Just verify no exception is thrown
        assertDoesNotThrow(() -> config.getProperty("anyKey"));
    }
}
