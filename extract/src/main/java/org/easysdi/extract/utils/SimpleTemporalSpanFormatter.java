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



/**
 * A simple time span converter.
 *
 * @author Yves Grasset
 */
public interface SimpleTemporalSpanFormatter {

    /**
     * Converts a simple time span into a string in the default locale.
     *
     * @param span the temporal interval to convert
     * @return the localized string
     */
    String format(SimpleTemporalSpan span);



    /**
     * Converts a simple time span into a string in the given locale.
     *
     * @param span   the temporal interval to convert
     * @param locale the locale to use to generate the string. If it is not supported by the message source, the
     *               default one will be used
     * @return the localized string
     */
    String format(SimpleTemporalSpan span, Locale locale);

}
