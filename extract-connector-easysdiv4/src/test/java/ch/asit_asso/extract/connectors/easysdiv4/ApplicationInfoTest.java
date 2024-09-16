package ch.asit_asso.extract.connectors.easysdiv4;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationInfoTest {
    @Test
    void testApplicationInfo() {
        ApplicationInfo applicationInfo = new ApplicationInfo();
        assertNotNull(applicationInfo);
        assertEquals("2.1.3-RELEASE", applicationInfo.getApplicationVersion());
    }
}