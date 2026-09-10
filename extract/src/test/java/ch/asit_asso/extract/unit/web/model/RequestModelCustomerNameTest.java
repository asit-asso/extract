/*
 * Copyright (C) 2025 asit-asso
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
package ch.asit_asso.extract.unit.web.model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Locale;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.RequestHistoryRecord;
import ch.asit_asso.extract.web.model.RequestModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the customer string that the requests tables display (issue #369).
 *
 * The organism is appended to the customer name so that an order can be told apart from one placed by a
 * namesake, and so that the tables can be searched by organism. An order placed from a personal account
 * carries the same string as the customer and as the organism, in which case the organism must be left
 * out rather than shown twice.
 *
 * The real message files are loaded rather than a stub, so that the tests also prove the key is present
 * and correctly formatted in every locale the application ships.
 *
 * @author Bruno Alves
 */
@DisplayName("RequestModel customer name with organism (issue #369)")
class RequestModelCustomerNameTest {

    /**
     * The base folder the orders are stored under. It only has to be absolute.
     */
    private static final Path BASE_FOLDER = Paths.get("/var/extract/orders");

    /**
     * The access to the strings the application ships, in every locale.
     */
    private final MessageSource messageSource = RequestModelCustomerNameTest.buildRealMessageSource();


    @Nested
    @DisplayName("1. The organism is appended when it differs (369-1)")
    class OrganismShownTests {

        @Test
        @DisplayName("1.1 - The organism follows the customer between parentheses")
        void organismFollowsTheCustomer() {
            RequestModel model = buildModel("Prénom Nom", "Ma boîte SA");

            assertEquals("Prénom Nom (Ma boîte SA)", model.getCustomerNameWithOrganism(Locale.FRENCH));
        }


        @Test
        @DisplayName("1.2 - The same string is produced in German")
        void theSameStringIsProducedInGerman() {
            RequestModel model = buildModel("Vorname Name", "Meine Firma AG");

            assertEquals("Vorname Name (Meine Firma AG)", model.getCustomerNameWithOrganism(Locale.GERMAN),
                         "The key must be defined in German as well");
        }


        @Test
        @DisplayName("1.3 - Two customers differing only by their case are not treated as equal")
        void aDifferentCaseIsADifferentString() {
            RequestModel model = buildModel("Acme", "ACME");

            assertEquals("Acme (ACME)", model.getCustomerNameWithOrganism(Locale.FRENCH),
                         "The issue asks for an exact comparison, so a different case means a different name");
        }
    }


    @Nested
    @DisplayName("2. The organism is left out when it adds nothing (369-2)")
    class OrganismHiddenTests {

        @Test
        @DisplayName("2.1 - An organism equal to the customer is not repeated")
        void anOrganismEqualToTheCustomerIsNotRepeated() {
            RequestModel model = buildModel("Prénom Nom", "Prénom Nom");

            assertEquals("Prénom Nom", model.getCustomerNameWithOrganism(Locale.FRENCH),
                         "An order placed from a personal account must not show the name twice");
        }


        @Test
        @DisplayName("2.2 - A null organism leaves the customer alone")
        void aNullOrganismLeavesTheCustomerAlone() {
            RequestModel model = buildModel("Prénom Nom", null);

            assertEquals("Prénom Nom", model.getCustomerNameWithOrganism(Locale.FRENCH));
        }


        @Test
        @DisplayName("2.3 - A blank organism leaves the customer alone")
        void aBlankOrganismLeavesTheCustomerAlone() {
            RequestModel model = buildModel("Prénom Nom", "   ");

            assertEquals("Prénom Nom", model.getCustomerNameWithOrganism(Locale.FRENCH));
        }
    }


    @Nested
    @DisplayName("3. Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("3.1 - An order without a customer shows the organism alone")
        void anOrderWithoutACustomerShowsTheOrganismAlone() {
            RequestModel model = buildModel(null, "Ma boîte SA");

            assertEquals("Ma boîte SA", model.getCustomerNameWithOrganism(Locale.FRENCH),
                         "The customer must never be rendered as the string \"null\"");
        }


        @Test
        @DisplayName("3.2 - An order with neither a customer nor an organism yields nothing")
        void anOrderWithNeitherYieldsNothing() {
            RequestModel model = buildModel(null, null);

            assertEquals(null, model.getCustomerNameWithOrganism(Locale.FRENCH));
        }
    }


    // ==================== HELPER METHODS ====================

    /**
     * Builds the model of an order placed by a given customer for a given organism.
     *
     * @param customer the name of the customer, which may be null
     * @param organism the name of the organism, which may be null
     * @return the model of the order
     */
    private RequestModel buildModel(final String customer, final String organism) {
        Request request = new Request();
        request.setClient(customer);
        request.setOrganism(organism);
        request.setUsersCollection(new ArrayList<>());
        request.setUserGroupsCollection(new ArrayList<>());

        return new RequestModel(request, new RequestHistoryRecord[]{}, RequestModelCustomerNameTest.BASE_FOLDER,
                                this.messageSource, new String[]{});
    }


    /**
     * Builds the access to the strings that the application really ships, so that the tests fail if the
     * key is missing from a locale.
     *
     * @return the message source
     */
    private static MessageSource buildRealMessageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages");
        source.setDefaultEncoding("UTF-8");
        source.setFallbackToSystemLocale(false);

        return source;
    }
}
