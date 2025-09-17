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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Configuration for internationalization (i18n) support.
 * Manages available languages and locale resolution.
 *
 * @author Claude Code
 */
@Configuration
public class LocaleConfiguration implements WebMvcConfigurer {

    /**
     * The configured languages from application properties.
     */
    @Value("${extract.i18n.language:fr}")
    private String languageConfig;

    /**
     * Creates the locale resolver bean that determines the locale to use.
     *
     * @return the locale resolver
     */
    @Bean
    public LocaleResolver localeResolver() {
        UserLocaleResolver resolver = new UserLocaleResolver();
        resolver.setAvailableLocales(getAvailableLocales());
        resolver.setDefaultLocale(getDefaultLocale());
        return resolver;
    }

    /**
     * Creates the locale change interceptor for handling language changes.
     *
     * @return the locale change interceptor
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    /**
     * Registers the locale change interceptor.
     *
     * @param registry the interceptor registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    /**
     * Gets the list of available locales from the configuration.
     *
     * @return the list of available locales
     */
    public List<Locale> getAvailableLocales() {
        return Arrays.stream(languageConfig.split(","))
                .map(String::trim)
                .filter(lang -> !lang.isEmpty())
                .map(Locale::forLanguageTag)
                .collect(Collectors.toList());
    }

    /**
     * Gets the default locale (first in the configuration list).
     *
     * @return the default locale
     */
    public Locale getDefaultLocale() {
        List<Locale> locales = getAvailableLocales();
        return locales.isEmpty() ? Locale.forLanguageTag("fr") : locales.get(0);
    }

    /**
     * Checks if the application is in multilingual mode.
     *
     * @return true if multiple languages are configured
     */
    public boolean isMultilingualMode() {
        return languageConfig.contains(",");
    }
    
    /**
     * Gets the configured language string.
     *
     * @return the language configuration string
     */
    public String getLanguageConfig() {
        return languageConfig;
    }
}