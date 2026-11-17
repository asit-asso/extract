/*
 * Copyright (C) 2026 asit-asso
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
package ch.asit_asso.extract.web.editors;

import java.beans.PropertyEditorSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts the scroll position that a browser submits into a non-negative integer. Browsers may report a fractional
 * value (e.g. 3583.75) when the page is scrolled to the bottom with sub-pixel layout, which a plain integer binding
 * would reject.
 *
 * @author Bruno Alves
 */
public final class ScrollPositionEditor extends PropertyEditorSupport {

    private final Logger logger = LoggerFactory.getLogger(ScrollPositionEditor.class);



    /**
     * Parses the submitted text, rounding it to the nearest integer. Blank, unparseable or non-finite values
     * give 0, and the result is limited to the range from 0 to {@link Integer#MAX_VALUE}.
     *
     * @param text the submitted scroll position
     */
    @Override
    public void setAsText(final String text) {
        this.setValue(this.parse(text));
    }



    private int parse(final String text) {

        if (text == null || text.isBlank()) {
            return 0;
        }

        try {
            final double position = Double.parseDouble(text.trim());

            if (Double.isNaN(position) || Double.isInfinite(position)) {
                this.logger.debug("The scroll position \"{}\" is not a finite number. Using 0.", text);
                return 0;
            }

            return (int) Math.max(0L, Math.min(Math.round(position), Integer.MAX_VALUE));

        } catch (NumberFormatException exception) {
            this.logger.debug("The scroll position \"{}\" cannot be parsed. Using 0.", text);
            return 0;
        }
    }
}
