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
package org.easysdi.extract.utils;

import java.util.Locale;
import org.springframework.context.MessageSource;



/**
 * A string converter for time spans to display in the Extract application.
 *
 * @author Yves Grasset
 */
public class ExtractSimpleTemporalSpanFormatter implements SimpleTemporalSpanFormatter {

    /**
     * The string used to generate the key for a temporal field in the singular.
     */
    private static final String TEMPORAL_FIELD_SINGULAR_KEY_FORMAT = "temporalField.singular.%s";

    /**
     * The string used to generate the key for a temporal field in the plural.
     */
    private static final String TEMPORAL_FIELD_PLURAL_KEY_FORMAT = "temporalField.plural.%s";

    /**
     * The string that identifies the localized string used to display a temporal span.
     */
    private static final String TEMPORAL_STRING_KEY = "temporalSpan.string";

    /**
     * The access to the application localized strings.
     */
    private final MessageSource messageSource;



    /**
     * Creates a new instance of this formatter.
     *
     * @param localizedStringsSource the access to the localized Extract strings
     */
    public ExtractSimpleTemporalSpanFormatter(final MessageSource localizedStringsSource) {

        if (localizedStringsSource == null) {
            throw new IllegalArgumentException("The localized strings source cannot be null.");
        }

        this.messageSource = localizedStringsSource;
    }



    @Override
    public final String format(final SimpleTemporalSpan span) {
        return this.format(span, Locale.getDefault());
    }



    @Override
    public final String format(final SimpleTemporalSpan span, final Locale locale) {

        if (span == null) {
            throw new IllegalArgumentException("The span cannot be null.");
        }

        if (locale == null) {
            throw new IllegalArgumentException("The locale cannot be null.");
        }

        return this.messageSource.getMessage(ExtractSimpleTemporalSpanFormatter.TEMPORAL_STRING_KEY,
                new Object[]{span.getValue(), this.getSpanFieldString(span, locale)}, locale);
    }



    /**
     * Obtains the string that describes the temporal field of a span in a given locale. Plural or singular
     * will be returned depending on the numerical value of the span.
     *
     * @param span   the span whose field must be localized
     * @param locale the locale to use to get the field string. If it is not supported by the application, the default
     *               one will be used
     * @return a string that contains the localized field
     */
    private String getSpanFieldString(final SimpleTemporalSpan span, final Locale locale) {
        assert span != null : "The span cannot be null.";
        assert locale != null : "The locale cannot be null.";

        final String fieldKeyFormat = (span.getValue() > 1)
                ? ExtractSimpleTemporalSpanFormatter.TEMPORAL_FIELD_PLURAL_KEY_FORMAT
                : ExtractSimpleTemporalSpanFormatter.TEMPORAL_FIELD_SINGULAR_KEY_FORMAT;

        return this.messageSource.getMessage(String.format(fieldKeyFormat, span.getField().name()), null, locale);
    }

}
