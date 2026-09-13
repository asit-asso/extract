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
package ch.asit_asso.extract.web.model;

import org.apache.commons.lang3.StringUtils;

/**
 * The optional support link rendered below the login form.
 */
public final class LoginSupportLink {

    /**
     * The URL prefix for unsecured HTTP links.
     */
    private static final String HTTP_PREFIX = "http://";

    /**
     * The URL prefix for secured HTTP links.
     */
    private static final String HTTPS_PREFIX = "https://";

    /**
     * The target used to open an HTTP(S) support link in another tab.
     */
    private static final String EXTERNAL_LINK_TARGET = "_blank";

    /**
     * The relation applied to an HTTP(S) support link opened in another tab.
     */
    private static final String EXTERNAL_LINK_REL = "noopener noreferrer";

    /**
     * The URL configured by the instance administrator.
     */
    private final String url;

    /**
     * Whether the configured URL is an HTTP(S) URL.
     */
    private final boolean externalHttpUrl;


    /**
     * Creates the link model for a non-blank configured URL.
     *
     * @param url the configured URL
     */
    public LoginSupportLink(final String url) {
        this.url = url;
        this.externalHttpUrl = LoginSupportLink.isExternalHttpUrl(url);
    }


    /**
     * Obtains whether a URL must be opened in a new tab.
     *
     * @param url the configured URL
     * @return <code>true</code> only when the URL begins with HTTP or HTTPS, irrespective of case
     */
    public static boolean isExternalHttpUrl(final String url) {
        return StringUtils.startsWithIgnoreCase(url, LoginSupportLink.HTTP_PREFIX)
                || StringUtils.startsWithIgnoreCase(url, LoginSupportLink.HTTPS_PREFIX);
    }


    /**
     * Gets the URL used for the link href.
     *
     * @return the configured URL
     */
    public String getUrl() {
        return this.url;
    }


    /**
     * Gets the link target for an HTTP(S) URL.
     *
     * @return <code>_blank</code> for HTTP(S), or <code>null</code> for every other URL type
     */
    public String getTarget() {
        return this.externalHttpUrl ? LoginSupportLink.EXTERNAL_LINK_TARGET : null;
    }


    /**
     * Gets the relation for an HTTP(S) URL opened in another tab.
     *
     * @return the safe external-link relation, or <code>null</code> when the link stays in the current tab
     */
    public String getRel() {
        return this.externalHttpUrl ? LoginSupportLink.EXTERNAL_LINK_REL : null;
    }
}
