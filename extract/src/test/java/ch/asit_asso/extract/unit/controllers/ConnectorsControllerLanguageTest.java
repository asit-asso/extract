package ch.asit_asso.extract.unit.controllers;

import ch.asit_asso.extract.connectors.common.IConnector;
import ch.asit_asso.extract.connectors.implementation.ConnectorDiscovererWrapper;
import ch.asit_asso.extract.persistence.ConnectorsRepository;
import ch.asit_asso.extract.persistence.ProcessesRepository;
import ch.asit_asso.extract.persistence.RequestsRepository;
import ch.asit_asso.extract.persistence.RulesRepository;
import ch.asit_asso.extract.unit.MockEnabledTest;
import ch.asit_asso.extract.web.controllers.BaseController;
import ch.asit_asso.extract.web.controllers.ConnectorsController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@DisplayName("ConnectorsController i18n - connector labels use user locale")
class ConnectorsControllerLanguageTest extends MockEnabledTest {

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

    @Mock
    private LocaleResolver localeResolver;

    private ConnectorsController controller;

    private IConnector germanConnector;

    @BeforeEach
    void setUp() throws Exception {
        controller = new ConnectorsController();
        ReflectionTestUtils.setField(controller, "logger",
                LoggerFactory.getLogger(ConnectorsController.class));
        ReflectionTestUtils.setField(controller, "connectorDiscoveryWrapper", connectorDiscoveryWrapper);
        ReflectionTestUtils.setField(controller, "connectorsRepository", connectorsRepository);
        ReflectionTestUtils.setField(controller, "processesRepository", processesRepository);
        ReflectionTestUtils.setField(controller, "rulesRepository", rulesRepository);
        ReflectionTestUtils.setField(controller, "requestsRepository", requestsRepository);
        java.lang.reflect.Field appLangField = BaseController.class.getDeclaredField("applicationLanguage");
        appLangField.setAccessible(true);
        appLangField.set(controller, "de,fr,en");

        java.lang.reflect.Field localeField = BaseController.class.getDeclaredField("localeResolver");
        localeField.setAccessible(true);
        localeField.set(controller, localeResolver);

        when(localeResolver.resolveLocale(any())).thenReturn(Locale.GERMAN);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", "pass",
                        List.of(new SimpleGrantedAuthority("ADMIN"))));

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));

        germanConnector = mock(IConnector.class);
        when(germanConnector.getLabel()).thenReturn("EasySDI v4");
        when(germanConnector.getParams()).thenReturn("[]");
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("viewAddForm should request connector with user's language")
    void viewAddFormShouldUseUserLanguage() {
        when(connectorDiscoveryWrapper.getConnectorForLanguage("easysdiv4", "de")).thenReturn(germanConnector);
        when(processesRepository.findAll()).thenReturn(Collections.emptyList());

        ModelMap model = new ModelMap();
        controller.viewAddForm(model, "easysdiv4");

        verify(connectorDiscoveryWrapper).getConnectorForLanguage("easysdiv4", "de");
        verify(connectorDiscoveryWrapper, never()).getConnector("easysdiv4");
    }

    @Test
    @DisplayName("viewList should request connectors with user's language")
    void viewListShouldUseUserLanguage() {
        Map<String, IConnector> localizedMap = new LinkedHashMap<>();
        localizedMap.put("easysdiv4", germanConnector);

        when(connectorDiscoveryWrapper.getConnectorsForLanguage("de")).thenReturn(localizedMap);
        when(connectorsRepository.findAll()).thenReturn(Collections.emptyList());

        ModelMap model = new ModelMap();
        controller.viewList(model);

        verify(connectorDiscoveryWrapper).getConnectorsForLanguage("de");
        verify(connectorDiscoveryWrapper, never()).getConnectors();
    }
}
