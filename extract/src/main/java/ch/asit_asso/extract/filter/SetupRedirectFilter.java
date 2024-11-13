package ch.asit_asso.extract.filter;

import ch.asit_asso.extract.services.AppInitializationService;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

public class SetupRedirectFilter extends OncePerRequestFilter {

    private final AppInitializationService appInitializationService;

    private static final Set<String> WHITELISTED_EXTENSIONS = Set.of(
            ".js", ".css", ".png", ".jpg", ".jpeg", ".gif", ".svg", ".woff", ".woff2", ".ttf", ".eot", ".webmanifest"
    );

    public SetupRedirectFilter(AppInitializationService appInitializationService) {
        this.appInitializationService = appInitializationService;
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {

        if (requestCanBeForwarded(request)) {
            // If the application is configured, if it’s an authorized resource, or if the request concerns "/setup"
            filterChain.doFilter(request, response);
        } else {
            // Redirect to "/setup" if none of the conditions are met
            response.sendRedirect(getRelativeSetupPath(request));
        }
    }

    private boolean requestCanBeForwarded(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        boolean configured = appInitializationService.isConfigured();
        boolean whitelisted = isWhitelistedResource(requestURI.toLowerCase());
        boolean starts = requestURI.startsWith(getRelativeSetupPath(request));
        boolean decision = configured || whitelisted || starts;
        logger.debug("Request to [" + request.getRequestURI() + "] decision: " + (decision ? "FORWARD":"REDIRECT") + " [C:" + configured + ",W:" + whitelisted + ",S:" + starts + "]");
        return decision;
    }

    private String getRelativeSetupPath(HttpServletRequest request)
    {
        return request.getContextPath() + "/setup";
    }

    private boolean isWhitelistedResource(String uri) {
        return isWhitelistedJavacriptFile(uri) || WHITELISTED_EXTENSIONS.stream().anyMatch(uri::endsWith);
    }

    private boolean isWhitelistedJavacriptFile(String uri) {
        return uri.endsWith("registersw.js") || (uri.endsWith(".js") && uri.startsWith("/extract/assets/index"));
    }
}