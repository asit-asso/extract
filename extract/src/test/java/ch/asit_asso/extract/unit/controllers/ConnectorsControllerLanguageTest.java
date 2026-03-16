package ch.asit_asso.extract.unit.controllers;

import ch.asit_asso.extract.connectors.common.IConnector;
import ch.asit_asso.extract.connectors.implementation.ConnectorDiscovererWrapper;
import ch.asit_asso.extract.persistence.ConnectorsRepository;
import ch.asit_asso.extract.persistence.ProcessesRepository;
import ch.asit_asso.extract.persistence.RequestsRepository;
import ch.asit_asso.extract.persistence.RulesRepository;
import ch.asit_asso.extract.unit.MockEnabledTest;
import ch.asit_asso.extract.web.controllers.ConnectorsController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@DisplayName("ConnectorsController i18n - connector labels use user locale")
class ConnectorsControllerLanguageTest extends MockEnabledTest {

    private MockMvc mockMvc;

    @Mock
    private ConnectorDiscovererWrapper connectorDiscoveryWrapper;

    @Mock
    private ConnectorsRepository connectorsRepository;

    @Mock
    private RequestsRepository requestsRepository;

    @Mock
    private ProcessesRepository processesRepository;

    @Mock
    private RulesRepository rulesRepository;

    @InjectMocks
    private ConnectorsController controller;

    private IConnector germanConnector;

    @BeforeEach
    void setUp() {
        LocaleResolver localeResolver = new LocaleResolver() {
            @Override
            public Locale resolveLocale(HttpServletRequest request) {
                return Locale.GERMAN;
            }

            @Override
            public void setLocale(HttpServletRequest request, javax.servlet.http.HttpServletResponse response,
                                  Locale locale) {
            }
        };

        ReflectionTestUtils.setField(controller, "applicationLanguage", "de,fr,en");
        ReflectionTestUtils.setField(controller, "localeResolver", localeResolver);

        germanConnector = mock(IConnector.class);
        when(germanConnector.getLabel()).thenReturn("EasySDI v4");
        when(germanConnector.getParams()).thenReturn("[]");

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("viewAddForm should request connector with user's language")
    void viewAddFormShouldUseUserLanguage() throws Exception {
        when(connectorDiscoveryWrapper.getConnectorForLanguage("easysdiv4", "de")).thenReturn(germanConnector);
        when(processesRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/connectors/add").param("type", "easysdiv4"));

        verify(connectorDiscoveryWrapper).getConnectorForLanguage("easysdiv4", "de");
        verify(connectorDiscoveryWrapper, never()).getConnector("easysdiv4");
    }

    @Test
    @DisplayName("viewList should request connectors with user's language")
    void viewListShouldUseUserLanguage() throws Exception {
        Map<String, IConnector> localizedMap = new LinkedHashMap<>();
        localizedMap.put("easysdiv4", germanConnector);

        when(connectorDiscoveryWrapper.getConnectorsForLanguage("de")).thenReturn(localizedMap);
        when(connectorsRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/connectors"));

        verify(connectorDiscoveryWrapper).getConnectorsForLanguage("de");
        verify(connectorDiscoveryWrapper, never()).getConnectors();
    }
}
