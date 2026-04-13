package ch.asit_asso.extract.unit.connectors;

import ch.asit_asso.extract.connectors.ConnectorDiscoverer;
import ch.asit_asso.extract.connectors.common.IConnector;
import ch.asit_asso.extract.connectors.implementation.ConnectorDiscovererWrapper;
import ch.asit_asso.extract.unit.MockEnabledTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@DisplayName("ConnectorDiscovererWrapper language support")
class ConnectorDiscovererWrapperTest extends MockEnabledTest {

    private ConnectorDiscovererWrapper wrapper;

    @Mock
    private ConnectorDiscoverer connectorDiscoverer;

    @Mock
    private IConnector cachedConnector;

    @Mock
    private IConnector localizedConnector;

    @BeforeEach
    void setUp() {
        wrapper = new ConnectorDiscovererWrapper();
        wrapper.setApplicationLanguage("de,fr,en");
        ReflectionTestUtils.setField(wrapper, "connectorDiscoverer", connectorDiscoverer);
    }

    @Nested
    @DisplayName("getConnectorForLanguage")
    class GetConnectorForLanguage {

        @Test
        @DisplayName("should create a new instance with the requested language")
        void shouldCreateNewInstanceWithLanguage() {
            when(connectorDiscoverer.getConnector("easysdiv4")).thenReturn(cachedConnector);
            when(cachedConnector.newInstance("de")).thenReturn(localizedConnector);

            IConnector result = wrapper.getConnectorForLanguage("easysdiv4", "de");

            assertThat(result).isSameAs(localizedConnector);
            verify(cachedConnector).newInstance("de");
        }

        @Test
        @DisplayName("should return null when connector code is unknown")
        void shouldReturnNullForUnknownConnector() {
            when(connectorDiscoverer.getConnector("unknown")).thenReturn(null);

            IConnector result = wrapper.getConnectorForLanguage("unknown", "de");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("should pass French language when requested")
        void shouldPassFrenchLanguage() {
            IConnector frenchConnector = mock(IConnector.class);
            when(connectorDiscoverer.getConnector("easysdiv4")).thenReturn(cachedConnector);
            when(cachedConnector.newInstance("fr")).thenReturn(frenchConnector);

            IConnector result = wrapper.getConnectorForLanguage("easysdiv4", "fr");

            assertThat(result).isSameAs(frenchConnector);
            verify(cachedConnector).newInstance("fr");
        }
    }

    @Nested
    @DisplayName("getConnectorsForLanguage")
    class GetConnectorsForLanguage {

        @Test
        @DisplayName("should create new instances for all connectors with the requested language")
        void shouldLocalizeAllConnectors() {
            IConnector cachedConnector2 = mock(IConnector.class);
            IConnector localized1 = mock(IConnector.class);
            IConnector localized2 = mock(IConnector.class);

            Map<String, IConnector> cached = new LinkedHashMap<>();
            cached.put("easysdiv4", cachedConnector);
            cached.put("other", cachedConnector2);

            when(connectorDiscoverer.getConnectors()).thenReturn(cached);
            when(cachedConnector.newInstance("de")).thenReturn(localized1);
            when(cachedConnector2.newInstance("de")).thenReturn(localized2);

            Map<String, IConnector> result = wrapper.getConnectorsForLanguage("de");

            assertThat(result).hasSize(2);
            assertThat(result.get("easysdiv4")).isSameAs(localized1);
            assertThat(result.get("other")).isSameAs(localized2);
            verify(cachedConnector).newInstance("de");
            verify(cachedConnector2).newInstance("de");
        }

        @Test
        @DisplayName("should return empty map when no connectors available")
        void shouldReturnEmptyMapWhenNoConnectors() {
            when(connectorDiscoverer.getConnectors()).thenReturn(new LinkedHashMap<>());

            Map<String, IConnector> result = wrapper.getConnectorsForLanguage("de");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should preserve connector order")
        void shouldPreserveOrder() {
            IConnector c1 = mock(IConnector.class);
            IConnector c2 = mock(IConnector.class);
            IConnector c3 = mock(IConnector.class);
            IConnector l1 = mock(IConnector.class);
            IConnector l2 = mock(IConnector.class);
            IConnector l3 = mock(IConnector.class);

            Map<String, IConnector> cached = new LinkedHashMap<>();
            cached.put("alpha", c1);
            cached.put("beta", c2);
            cached.put("gamma", c3);

            when(connectorDiscoverer.getConnectors()).thenReturn(cached);
            when(c1.newInstance("en")).thenReturn(l1);
            when(c2.newInstance("en")).thenReturn(l2);
            when(c3.newInstance("en")).thenReturn(l3);

            Map<String, IConnector> result = wrapper.getConnectorsForLanguage("en");

            assertThat(result.keySet()).containsExactly("alpha", "beta", "gamma");
        }
    }
}
