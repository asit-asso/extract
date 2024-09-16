package ch.asit_asso.extract.interceptors;

import ch.asit_asso.extract.services.AppInitializationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AppInitializationInterceptor implements HandlerInterceptor {

    private final AppInitializationService appInitializationService;

    private final Logger logger = LoggerFactory.getLogger(AppInitializationInterceptor.class);

    public AppInitializationInterceptor(AppInitializationService userVerifierService) {
        this.appInitializationService = userVerifierService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String setupPagePath = request.getContextPath() + "/setup";
        if (!appInitializationService.isConfigured()) {
            if (!request.getRequestURI().equals(setupPagePath)) {
                response.sendRedirect(setupPagePath);
                return false;
            }
        } else {
            if (request.getRequestURI().equals(setupPagePath)) {
                response.sendError(403, "Forbidden");
                return false;
            }
        }
        return true;
    }
}