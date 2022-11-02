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
package ch.asit_asso.extract.email;



/**
 * An error thrown to signal that the name of a content template for an electronic message could not be
 * resolved by the template engine.
 *
 * @author Yves Grasset
 */
public class EmailTemplateNotFoundException extends Exception {

    /**
     * The name of the template that could not be resolved.
     */
    private final String templateName;



    /**
     * Creates a new instance of <code>EmailTemplateNotFoundException</code> without detail message.
     *
     * @param template the template name that could not be resolved
     */
    public EmailTemplateNotFoundException(final String template) {

        if (template == null) {
            throw new IllegalArgumentException("The template name cannot be null.");
        }

        this.templateName = template;
    }



    /**
     * Creates a new instance of <code>EmailTemplateNotFoundException</code> without detail message.
     *
     * @param template the template name that could not be resolved
     * @param cause    the exception that is at the origin of this one
     */
    public EmailTemplateNotFoundException(final String template, final Throwable cause) {
        super(cause);

        if (template == null) {
            throw new IllegalArgumentException("The template name cannot be null.");
        }

        this.templateName = template;
    }



    /**
     * Constructs an instance of <code>EmailTemplateNotFoundException</code> with the specified detail message.
     *
     * @param template the template name that could not be resolved
     * @param message  the detail message.
     */
    public EmailTemplateNotFoundException(final String template, final String message) {
        super(message);

        if (template == null) {
            throw new IllegalArgumentException("The template name cannot be null.");
        }

        this.templateName = template;
    }



    /**
     * Constructs an instance of <code>EmailTemplateNotFoundException</code> with the specified detail message.
     *
     * @param template the template name that could not be resolved
     * @param message  the detail message.
     * @param cause    the exception that is at the origin of this one
     */
    public EmailTemplateNotFoundException(final String template, final String message, final Throwable cause) {
        super(message, cause);

        if (template == null) {
            throw new IllegalArgumentException("The template name cannot be null.");
        }

        this.templateName = template;
    }



    /**
     * Obtains the name of the template that could not be resolved.
     *
     * @return the template name
     */
    public final String getTemplateName() {
        return this.templateName;
    }



    @Override
    public final String getMessage() {

        if (super.getMessage() != null) {
            return super.getMessage();
        }

        return String.format("The e-mail template \"%s\" could not be resolved.", this.getTemplateName());
    }

}
