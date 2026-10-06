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
package ch.asit_asso.extract.connectors.easysdiv4;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/**
 * Ensures that the contact information of an order is split into its individual parts, and that collapsing
 * those parts back into a single string still produces the legacy <code>clientDetails</code> value.
 */
class Easysdiv4ContactDetailsTest {

    private static final String ADDRESS_XPATH = "//order/client/contact/address";

    private final Easysdiv4 connector = new Easysdiv4();


    private Document parse(final String xml) throws Exception {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();

        return builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }



    private Document orderWithAddressContent(final String addressContent) throws Exception {
        return this.parse("<orders><order><client><contact><address>" + addressContent
                + "</address></contact></client></order></orders>");
    }



    @Test
    @DisplayName("A complete contact is split into an address, an e-mail address and a phone number")
    void splitsCompleteContact() throws Exception {
        final Document document = this.orderWithAddressContent(
                "<sdi:addressstreet1>Avenue de la Praille 45</sdi:addressstreet1>"
                        + "<sdi:zip>1227</sdi:zip>"
                        + "<sdi:locality>Carouge</sdi:locality>"
                        + "<sdi:phone>+41 22 123 45 67</sdi:phone>"
                        + "<sdi:email>david.test@example.com</sdi:email>");

        final Easysdiv4.ContactDetails contact = this.connector.buildContactDetailsFromXpath(document,
                Easysdiv4ContactDetailsTest.ADDRESS_XPATH);

        assertEquals("Avenue de la Praille 45\n1227 Carouge", contact.getAddress());
        assertEquals("david.test@example.com", contact.getEmail());
        assertEquals("+41 22 123 45 67", contact.getPhone());
    }



    @Test
    @DisplayName("The second street line becomes an additional address line")
    void keepsSecondStreetLineInAddress() throws Exception {
        final Document document = this.orderWithAddressContent(
                "<sdi:addressstreet1>Chemin des Fleurs 7</sdi:addressstreet1>"
                        + "<sdi:addressstreet2>Case postale 1234</sdi:addressstreet2>"
                        + "<sdi:zip>1004</sdi:zip>"
                        + "<sdi:locality>Lausanne</sdi:locality>");

        final Easysdiv4.ContactDetails contact = this.connector.buildContactDetailsFromXpath(document,
                Easysdiv4ContactDetailsTest.ADDRESS_XPATH);

        assertEquals("Chemin des Fleurs 7\nCase postale 1234\n1004 Lausanne", contact.getAddress());
        assertNull(contact.getEmail());
        assertNull(contact.getPhone());
    }



    @Test
    @DisplayName("An order without any contact element produces no contact information")
    void returnsNothingWhenTheAddressIsMissing() throws Exception {
        final Document document = this.parse("<orders><order><client/></order></orders>");

        final Easysdiv4.ContactDetails contact = this.connector.buildContactDetailsFromXpath(document,
                Easysdiv4ContactDetailsTest.ADDRESS_XPATH);

        assertNull(contact.getAddress());
        assertNull(contact.getEmail());
        assertNull(contact.getPhone());
        assertEquals("", contact.toDetailsString());
    }



    @Test
    @DisplayName("The collapsed contact keeps the legacy client details format")
    void collapsesToTheLegacyDetailsString() throws Exception {
        final Document document = this.orderWithAddressContent(
                "<sdi:addressstreet1>Avenue de la Praille 45</sdi:addressstreet1>"
                        + "<sdi:addressstreet2>Case postale 1234</sdi:addressstreet2>"
                        + "<sdi:zip>1227</sdi:zip>"
                        + "<sdi:locality>Carouge</sdi:locality>"
                        + "<sdi:phone>+41 22 123 45 67</sdi:phone>"
                        + "<sdi:email>david.test@example.com</sdi:email>");

        final Easysdiv4.ContactDetails contact = this.connector.buildContactDetailsFromXpath(document,
                Easysdiv4ContactDetailsTest.ADDRESS_XPATH);

        assertEquals("Avenue de la Praille 45\r\nCase postale 1234\r\n1227 Carouge\r\n+41 22 123 45 67\r\n"
                + "david.test@example.com", contact.toDetailsString());
    }

}
