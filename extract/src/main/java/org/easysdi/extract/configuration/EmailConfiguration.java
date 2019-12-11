/*
 * Copyright (C) 2017 arx iT
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.easysdi.extract.configuration;

import java.util.Collections;
import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.easysdi.extract.email.EmailSettings;
import org.easysdi.extract.persistence.SystemParametersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;



/**
 * The parameters necessary to the application in order to send notifications.
 *
 * @author Yves Grasset
 */
@Configuration
public class EmailConfiguration {

    /**
     * A string that contains the URL to access the application from the outside.
     */
    @Value("${application.external.url}")
    private String applicationExternalUrl;

    /**
     * Whether the e-mail template engine should cache the templates.
     */
    @Value("${email.templates.cache}")
    private boolean cacheEmailTemplates;

    /**
     * The character set used by the e-mail templates.
     */
    @Value("${email.templates.encoding}")
    private String emailTemplatesEncoding;

    /**
     * The path of the folder that contains the e-mail templates.
     */
    @Value("${email.templates.path}")
    private String emailTemplatesPath;

    /**
     * The object that gives access to the application strings.
     */
    @Autowired
    private MessageSource messageSource;

    /**
     * The Spring Data object that links the application parameters with the data source.
     */
    @Autowired
    private SystemParametersRepository systemParametersRepository;



    /**
     * Creates a bean that contains the configuration objects required to create and send e-mail messages.
     *
     * @return the e-mail settings bean
     */
    @Bean
    public EmailSettings emailSettings() {
        return new EmailSettings(this.systemParametersRepository, this.emailTemplateEngine(), this.messageSource,
                this.applicationExternalUrl);
    }



    /**
     * Creates a bean that can process the e-mail templates.
     *
     * @return the template engine bean
     */
    @Bean
    public TemplateEngine emailTemplateEngine() {
        final SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.addTemplateResolver(this.htmlEmailTemplateResolver());
        templateEngine.addDialect(new LayoutDialect());

        return templateEngine;
    }



    /**
     * Creates a bean that can process HTML e-mail templates.
     *
     * @return the HTML e-mail template resolver bean
     */
    @Bean
    public ITemplateResolver htmlEmailTemplateResolver() {
        final ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setOrder(1);
        resolver.setResolvablePatterns(Collections.singleton("html/*"));
        resolver.setPrefix(this.emailTemplatesPath);
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setCharacterEncoding(this.emailTemplatesEncoding);
        resolver.setCacheable(this.cacheEmailTemplates);

        return resolver;
    }

}
