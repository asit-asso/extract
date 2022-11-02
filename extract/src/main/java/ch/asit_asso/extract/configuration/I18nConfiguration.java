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
package ch.asit_asso.extract.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;



/**
 * Configuration for the localization of the application.
 *
 * @author Yves Grasset
 */
@Configuration
public class I18nConfiguration {

    /**
     * The default path of the file that contains the application strings.
     */
    private static final String DEFAULT_MESSAGES_BASENAME = "classpath:messages";

    /**
     * The string to use to generate the path of the file that contains the application strings localized in the
     * application language.
     */
    private static final String EXTRACT_MESSAGES_BASENAME_FORMAT = "classpath:static/lang/%s/messages";

    /**
     * The code of the language to use to localize the application strings.
     */
    @Value("${extract.i18n.language}")
    private String language;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(I18nConfiguration.class);



    /**
     * Initializes the access to the localized application strings.
     *
     * @return the message source for the application language
     */
    @Bean
    public MessageSource messageSource() {
        this.logger.debug("Configuring the message source for language {}.", this.language);
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        String basename = I18nConfiguration.DEFAULT_MESSAGES_BASENAME;

        if (this.language.matches("^[a-z]{2}$")) {
            basename = String.format(I18nConfiguration.EXTRACT_MESSAGES_BASENAME_FORMAT, this.language);
        }

        this.logger.debug("The message source basename is \"{}\".", basename);
        messageSource.setBasenames(basename);
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(-1);

        this.logger.debug("The message source is configured.");
        return messageSource;
    }

}
