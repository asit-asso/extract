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
package ch.asit_asso.extract.integration.requests;

import java.util.ArrayList;
import java.util.List;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.persistence.RequestsRepository;
import ch.asit_asso.extract.persistence.specifications.RequestSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for the text search of the finished requests table (issue #369, criterion 369-3).
 *
 * The organism is displayed next to the customer in the table, but the string that the table shows is
 * built when the row is serialized and is never persisted. Searching therefore cannot rely on the
 * customer column alone: the criteria must look into the organism column too, which is what these tests
 * pin against a real PostgreSQL.
 *
 * The requests are seeded behind a marker and the assertions only bear on those, so that the rows already
 * present in the test database cannot make them flaky.
 *
 * @author Bruno Alves
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("Finished Requests Search Integration Tests (issue #369)")
class FinishedRequestsSearchIntegrationTest {

    /**
     * Prefixes the seeded values, so that the assertions can ignore the rows of the test data set.
     */
    private static final String MARKER = "ZZ369";

    @Autowired
    private RequestsRepository requestsRepository;



    @Nested
    @DisplayName("1. Searching a finished request by organism (369-3)")
    class SearchByOrganismTests {

        @Test
        @DisplayName("1.1 - A request is found by a fragment of its organism alone")
        @Transactional
        void aRequestIsFoundByItsOrganism() {
            Request expected = seedFinishedRequest("Alice Martin", MARKER + " Ma boîte SA");
            seedFinishedRequest("Bob Dupont", MARKER + " Autre organisme");

            List<Request> found = search(MARKER + " Ma boîte");

            assertEquals(List.of(expected.getId()), idsOf(found),
                         "Searching a fragment of the organism must return the matching request only");
        }


        @Test
        @DisplayName("1.2 - The search on the organism ignores the case")
        @Transactional
        void theSearchOnTheOrganismIgnoresTheCase() {
            Request expected = seedFinishedRequest("Alice Martin", MARKER + " Ma boîte SA");

            List<Request> found = search(MARKER + " MA BOÎTE");

            assertTrue(idsOf(found).contains(expected.getId()),
                       "The existing predicates lower both sides, and the organism one must do the same");
        }


        @Test
        @DisplayName("1.3 - Searching the customer still works")
        @Transactional
        void searchingTheCustomerStillWorks() {
            Request expected = seedFinishedRequest(MARKER + " Alice Martin", "Ma boîte SA");
            seedFinishedRequest(MARKER + " Bob Dupont", "Autre organisme");

            List<Request> found = search(MARKER + " Alice");

            assertEquals(List.of(expected.getId()), idsOf(found),
                         "Widening the search to the organism must not break the search on the customer");
        }
    }


    @Nested
    @DisplayName("2. Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("2.1 - A request without an organism is still found by its customer")
        @Transactional
        void aRequestWithoutAnOrganismIsStillFoundByItsCustomer() {
            Request expected = seedFinishedRequest(MARKER + " Alice Martin", null);

            List<Request> found = search(MARKER + " Alice");

            assertEquals(List.of(expected.getId()), idsOf(found),
                         "A LIKE on a null organism yields null, not true, so the OR must still match");
        }


        @Test
        @DisplayName("2.2 - A text matching nothing returns nothing")
        @Transactional
        void aTextMatchingNothingReturnsNothing() {
            seedFinishedRequest(MARKER + " Alice Martin", MARKER + " Ma boîte SA");

            List<Request> found = search(MARKER + " matches nothing at all");

            assertTrue(found.isEmpty(), "An unmatched search must return no request");
        }
    }


    // ==================== HELPER METHODS ====================

    /**
     * Runs the search the finished requests table runs, without any other filter.
     *
     * @param searchText the text typed in the search box
     * @return the finished requests that match
     */
    private List<Request> search(final String searchText) {
        Specification<Request> criteria
                = RequestSpecification.getFilterSpecification(searchText, null, null, null, null)
                                      .and(RequestSpecification.isFinished());

        return this.requestsRepository.findAll(criteria);
    }


    /**
     * Adds a finished request placed by a given customer for a given organism.
     *
     * @param customer the name of the customer
     * @param organism the name of the organism, which may be null
     * @return the saved request
     */
    private Request seedFinishedRequest(final String customer, final String organism) {
        Request request = new Request();
        request.setStatus(Request.Status.FINISHED);
        request.setClient(customer);
        request.setOrganism(organism);
        request.setOrderLabel(MARKER + "-order");
        request.setProductLabel(MARKER + "-product");
        request.setUsersCollection(new ArrayList<>());
        request.setUserGroupsCollection(new ArrayList<>());

        return this.requestsRepository.save(request);
    }


    /**
     * Reads the identifiers of requests.
     *
     * @param requests the requests whose identifiers are wanted
     * @return the identifiers
     */
    private static List<Integer> idsOf(final List<Request> requests) {
        return requests.stream().map(Request::getId).toList();
    }
}
