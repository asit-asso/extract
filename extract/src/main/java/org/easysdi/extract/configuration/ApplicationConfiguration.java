package org.easysdi.extract.configuration;

import javax.servlet.ServletContextListener;
import org.easysdi.extract.context.ContextListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.thymeleaf.dialect.springdata.SpringDataDialect;



/**
 * The settings overrides for the basic objects of this application.
 *
 * @author Yves Grasset
 */
@Configuration
@EnableTransactionManagement
public class ApplicationConfiguration {

    /**
     * The configuration to use to reference Spring Data from the templates.
     *
     * @return the Spring Data dialect
     */
    @Bean
    public SpringDataDialect springDataDialect() {
        return new SpringDataDialect();
    }



    /**
     * Gets the object that carries the appropriate operations when the application is started or stopped.
     *
     * @return the listener bean
     */
    @Bean
    public ServletContextListener contextListener() {
        return new ContextListener();
    }

}
