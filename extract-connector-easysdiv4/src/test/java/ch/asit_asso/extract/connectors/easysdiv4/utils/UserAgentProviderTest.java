package ch.asit_asso.extract.connectors.easysdiv4.utils;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.VersionInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserAgentProviderTest {

    private static final String APP_VERSION = "1.0.0";
    private UserAgentProvider userAgentProvider;

    @BeforeEach
    void setUp() {
        userAgentProvider = UserAgentProvider.withVersion(APP_VERSION);
    }

    @Test
    void testWithVersion_shouldCreateInstance() {
        UserAgentProvider provider = UserAgentProvider.withVersion(APP_VERSION);
        assertNotNull(provider);
    }

    @Test
    void testGetDefaultHeaders_shouldReturnCorrectHeader() {
        Collection<? extends Header> headers = userAgentProvider.getDefaultHeaders();
        assertEquals(1, headers.size());

        Header expectedHeader = new BasicHeader("X-Extract-Version", APP_VERSION);
        Header actualHeader = headers.iterator().next();

        assertEquals(expectedHeader.getName(), actualHeader.getName());
        assertEquals(expectedHeader.getValue(), actualHeader.getValue());
    }

    @Test
    void testGetUserAgent_shouldReturnCorrectUserAgent() {
        // Mock VersionInfo to simulate the user agent returned by Apache-HttpClient
        String expectedHttpClientUserAgent = VersionInfo.getUserAgent("Apache-HttpClient", "org.apache.http.client", UserAgentProvider.class);

        String actualUserAgent = userAgentProvider.getUserAgent();

        assertTrue(actualUserAgent.startsWith(expectedHttpClientUserAgent));
        assertTrue(actualUserAgent.contains("Extract/" + APP_VERSION));
    }

    @Test
    void testGetUserAgent_withoutAppVersion_shouldNotIncludeExtract() {
        UserAgentProvider providerWithoutVersion = UserAgentProvider.withVersion("");
        String actualUserAgent = providerWithoutVersion.getUserAgent();

        assertFalse(actualUserAgent.contains("Extract/"));
    }

    @Test
    void testGetUserAgent_withNullAppVersion_shouldNotIncludeExtract() {
        UserAgentProvider providerWithNullVersion = UserAgentProvider.withVersion(null);
        String actualUserAgent = providerWithNullVersion.getUserAgent();

        assertFalse(actualUserAgent.contains("Extract/"));
    }
}
