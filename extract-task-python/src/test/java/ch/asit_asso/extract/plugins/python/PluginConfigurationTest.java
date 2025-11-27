package ch.asit_asso.extract.plugins.python;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PluginConfigurationTest {

    private static final String DEFAULT_CONFIG_PATH = "plugins/python/properties/config.properties";

    @Test
    void testConstructorWithValidPath() {
        PluginConfiguration config = new PluginConfiguration(DEFAULT_CONFIG_PATH);
        assertNotNull(config);
    }

    @Test
    void testGetPropertyReturnsValue() {
        PluginConfiguration config = new PluginConfiguration(DEFAULT_CONFIG_PATH);
        assertDoesNotThrow(() -> config.getProperty("anyKey"));
    }
}
