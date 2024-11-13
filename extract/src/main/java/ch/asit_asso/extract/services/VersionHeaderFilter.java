package ch.asit_asso.extract.services;

import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class VersionHeaderFilter implements Filter {

    /**
     * Constant representing the version header name
     */
    public static final String VERSION_HEADER = "X-App-Version";

    /**
     * Holds the currently configured version
     */
    private final String version;

    public VersionHeaderFilter(String version) {
        this.version = version;
    }
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (response instanceof HttpServletResponse httpResponse) {
            httpResponse.setHeader(VERSION_HEADER, this.version);
        }

        // advances to the next filter in the chain
        chain.doFilter(request, response);
    }
}
