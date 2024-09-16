package ch.asit_asso.extract.configuration;

import ch.asit_asso.extract.interceptors.AppInitializationInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AppInitializationInterceptor adminSetupInterceptor;
    private final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    public WebConfig(AppInitializationInterceptor adminSetupInterceptor) {
        this.adminSetupInterceptor = adminSetupInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        logger.info("Adding the setup screen interceptor");
        registry.addInterceptor(adminSetupInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/forbidden", "/lib/**", "/css/**", "/js/**", "/images/**", "/fonts/**", "/webjars/**", "/**/*.png", "/**/*.jpg", "/**/*.jpeg", "/**/*.gif", "/**/*.svg", "/**/*.ico");
    }
}
