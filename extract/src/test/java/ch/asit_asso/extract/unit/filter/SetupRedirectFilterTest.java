package ch.asit_asso.extract.unit.filter;

import ch.asit_asso.extract.filter.SetupRedirectFilter;
import ch.asit_asso.extract.services.AppInitializationService;
import ch.asit_asso.extract.unit.MockEnabledTest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockFilterChain;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SetupRedirectFilterTest extends MockEnabledTest {

    @Mock
    private AppInitializationService appInitializationService;

    @InjectMocks
    private SetupRedirectFilter setupRedirectFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Test
    void testConfiguredApp() throws IOException, ServletException {
        when(appInitializationService.isConfigured()).thenReturn(true);
        when(request.getRequestURI()).thenReturn("/extract/config");
        setupRedirectFilter.doFilter(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    void testWhitelistedResource() throws IOException, ServletException {
        // Simuler l'application non configurée et une ressource whitelisted
        when(appInitializationService.isConfigured()).thenReturn(false);
        when(request.getRequestURI()).thenReturn("/css/style.css");
        when(appInitializationService.isConfigured()).thenReturn(true);
        setupRedirectFilter.doFilter(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    void testSetupPath() throws IOException, ServletException {
        // Simuler l'application non configurée et une requête pour /setup
        when(appInitializationService.isConfigured()).thenReturn(false);
        when(request.getContextPath()).thenReturn("/extract");
        when(request.getRequestURI()).thenReturn("/extract/setup");
        setupRedirectFilter.doFilter(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    void testRedirectToSetup() throws IOException, ServletException {
        // Simuler l'application non configurée et une requête non whitelisted
        when(appInitializationService.isConfigured()).thenReturn(false);
        when(request.getContextPath()).thenReturn("/extract");
        when(request.getRequestURI()).thenReturn("/protected/resource");
        setupRedirectFilter.doFilter(request, response, filterChain);
        verify(response, times(1)).sendRedirect("/extract/setup");
        verify(filterChain, never()).doFilter(request, response);
    }
}