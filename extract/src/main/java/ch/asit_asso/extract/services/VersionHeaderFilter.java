package ch.asit_asso.extract.services;

import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class VersionHeaderFilter implements Filter {

    private final String version;

    public VersionHeaderFilter(String version) {
        this.version = version;
    }
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (response instanceof HttpServletResponse httpResponse) {
            httpResponse.setHeader("X-App-Version", this.version);
        }

        // Continuer avec le filtre suivant dans la chaîne
        chain.doFilter(request, response);
    }
}
