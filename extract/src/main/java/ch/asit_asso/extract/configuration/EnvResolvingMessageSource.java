/*
 * Copyright (C) 2025 arusakov
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

import java.text.MessageFormat;
import java.util.Locale;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.util.StringValueResolver;

/**
 * A MessageSource that resolves ${…} placeholders against the Spring {@link Environment}
 * after the message has been loaded from the bundle.
 */
public class EnvResolvingMessageSource extends ReloadableResourceBundleMessageSource
        implements EnvironmentAware {

    private StringValueResolver placeholderResolver;

    @Override
    public void setEnvironment(Environment environment) {
        // Spring already knows how to resolve ${…} against the environment,
        // we just delegate to its built‑in resolver.
        this.placeholderResolver = environment::resolveRequiredPlaceholders;
    }

    @Override
    protected String resolveCodeWithoutArguments(String code, Locale locale) {
        var raw = super.resolveCodeWithoutArguments(code, locale);
        
        return raw == null || placeholderResolver == null ? raw : 
                placeholderResolver.resolveStringValue(raw);
    }
    
    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        var raw = super.resolveCode(code, locale);
        if (raw != null && placeholderResolver != null) {
            raw.applyPattern(placeholderResolver.resolveStringValue(raw.toPattern()));
        }
        return raw;
    }
}