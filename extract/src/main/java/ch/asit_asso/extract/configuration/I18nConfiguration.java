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

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;


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
     * The standard Spring basename for messages with locale suffixes.
     */
    private static final String SPRING_MESSAGES_BASENAME = "classpath:messages";

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
        this.logger.debug("Configuring the message source for languages: {}.", this.language);
        EnvResolvingMessageSource messageSource = new EnvResolvingMessageSource();

        // la collection des base names
        List<String> basenames = new ArrayList<>();

        // Use standard Spring basename for messages with locale suffixes
        // This will look for messages.properties, messages_fr.properties, messages_en.properties, etc.
        basenames.add("classpath:messages");

        // For backward compatibility, also check the old path structure
        String[] configuredLanguages = this.language.split(",");
        for(var lang : configuredLanguages) {
            String trimmedLang = lang.trim();
            if (trimmedLang.matches("^[a-z]{2}(-[A-Z]{2})?$")) {
                String extractBaseName = String.format(I18nConfiguration.EXTRACT_MESSAGES_BASENAME_FORMAT, trimmedLang);
                basenames.add(extractBaseName);
                this.logger.debug("Adding backward compatibility basename: {}", extractBaseName);
            }
        }

        // Set both basenames if the old structure exists
        String[] basenamesArray = basenames.toArray(new String[0]);
        this.logger.debug("Setting {} basenames: {}", basenamesArray.length, Arrays.toString(basenamesArray));
        messageSource.setBasenames(basenamesArray);

        // Disable fallback to messages.properties (without locale suffix)
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setUseCodeAsDefaultMessage(false);
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(-1);

        this.logger.debug("The message source is configured.");

        // Wrap with our custom fallback handler
        // Parse all languages from extract.i18n.language and create fallback locale list
        List<Locale> fallbackLocales = Arrays.stream(this.language.split(","))
            .map(String::trim)
            .filter(lang -> lang.matches("^[a-z]{2}(-[A-Z]{2})?$"))
            .map(Locale::forLanguageTag)
            .collect(Collectors.toList());

        this.logger.debug("Locale set to: {}", messageSource);
        this.logger.debug("Fallback locales set to: {}", fallbackLocales);
        return new FallbackMessageSource(messageSource, fallbackLocales);
    }

    /**
     * Custom MessageSource that implements cascading fallback through all languages from extract.i18n.language
     * instead of falling back to messages.properties.
     * Tries each fallback locale in order until a message is found, or returns the code if none are found.
     */
    private static class FallbackMessageSource implements MessageSource {

        private final ReloadableResourceBundleMessageSource delegate;
        private final List<Locale> fallbackLocales;
        private final Logger logger = LoggerFactory.getLogger(FallbackMessageSource.class);

        public FallbackMessageSource(ReloadableResourceBundleMessageSource delegate, List<Locale> fallbackLocales) {
            this.delegate = delegate;
            this.fallbackLocales = fallbackLocales;
            this.logger.debug("Fallback locales set to: {}", fallbackLocales);
        }

        @Override
        public String getMessage(@NotNull String code, Object[] args, String defaultMessage, @NotNull Locale locale) {
            try {
                return this.delegate.getMessage(code, args, null, locale);
            } catch (NoSuchMessageException e) {
                logKeyNotFound(code, locale);
            }

            // Try each fallback locale in order until we find the key
            for (Locale fallbackLocale : this.fallbackLocales) {
                if (!locale.equals(fallbackLocale)) {
                    try {
                        logTryingFallback(code, locale, fallbackLocale);
                        return this.delegate.getMessage(code, args, null, fallbackLocale);
                    } catch (NoSuchMessageException e) {
                        logKeyNotFound(code, locale);
                    }
                }
            }

            // If still not found, return default message or code
            return (defaultMessage != null) ? defaultMessage : code;
        }

        private void logKeyNotFound(String code, Locale locale) {
            this.logger.debug("Message key '{}' not found for locale '{}'", code, locale);
        }

        private void logTryingFallback(String code, Locale fromLocale, Locale toLocale) {
            this.logger.debug("Message key '{}' not found for locale '{}', trying fallback locale '{}'",
                    code, fromLocale, toLocale);
        }

        @Override
        public @NotNull String getMessage(@NotNull String code, Object[] args, @NotNull Locale locale) throws NoSuchMessageException {
            try {
                // Try to get message in requested locale
                return this.delegate.getMessage(code, args, locale);
            } catch (NoSuchMessageException e) {
                // Try each fallback locale in order until we find the key
                for (Locale fallbackLocale : this.fallbackLocales) {
                    if (!locale.equals(fallbackLocale)) {
                        try {
                            logTryingFallback(code, locale, fallbackLocale);
                            return this.delegate.getMessage(code, args, fallbackLocale);
                        } catch (NoSuchMessageException ex) {
                            logKeyNotFound(code, locale);
                        }
                    }
                }
                // None of the fallbacks worked, throw the original exception
                throw e;
            }
        }

        @Override
        public @NotNull String getMessage(@NotNull MessageSourceResolvable resolvable, @NotNull Locale locale) throws NoSuchMessageException {
            try {
                // Try to get message in requested locale
                return this.delegate.getMessage(resolvable, locale);
            } catch (NoSuchMessageException e) {
                // Try each fallback locale in order until we find the key
                for (Locale fallbackLocale : this.fallbackLocales) {
                    if (!locale.equals(fallbackLocale)) {
                        try {
                            String[] codes = resolvable.getCodes();
                            this.logger.debug("Keys '{}' not found in locale '{}', trying fallback locale '{}'",
                                            codes != null && codes.length > 0 ? codes[0] : "unknown",
                                            locale, fallbackLocale);
                            return this.delegate.getMessage(resolvable, fallbackLocale);
                        } catch (NoSuchMessageException ex) {
                            // Not found in this fallback, continue to next
                        }
                    }
                }
                // None of the fallbacks worked, throw the original exception
                throw e;
            }
        }
    }
}
