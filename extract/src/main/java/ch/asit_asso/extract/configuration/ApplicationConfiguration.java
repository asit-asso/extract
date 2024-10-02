package ch.asit_asso.extract.configuration;

import ch.asit_asso.extract.services.VersionHeaderFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
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

//    @Bean
//    public FilterRegistrationBean<VersionHeaderFilter> loggingFilter(){
//        FilterRegistrationBean<VersionHeaderFilter> registrationBean = new FilterRegistrationBean<>();
//
//        registrationBean.setFilter(new VersionHeaderFilter(appVersion));
//        registrationBean.addUrlPatterns("/*");  // S'applique à toutes les URL
//
//        return registrationBean;
//    }
}
