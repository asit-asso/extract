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
package ch.asit_asso.extract.integration.connectors;

import java.util.ArrayList;
import ch.asit_asso.extract.domain.Connector;
import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.Rule;
import ch.asit_asso.extract.connectors.common.IConnector;
import ch.asit_asso.extract.connectors.implementation.ConnectorDiscovererWrapper;
import ch.asit_asso.extract.integration.DatabaseTestHelper;
import ch.asit_asso.extract.integration.WithMockApplicationUser;
import ch.asit_asso.extract.persistence.ConnectorsRepository;
import ch.asit_asso.extract.persistence.ProcessesRepository;
import ch.asit_asso.extract.persistence.RulesRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the rules written when a connector is saved (issue #428).
 *
 * Saving a connector used to write into whichever rule carried the identifier that the form sent, wherever that
 * rule lived, and to move it to the edited connector on the way. The connector it came from lost it without any
 * sign, and the rule of the edited connector that the block stood for was deleted as a leftover: one save
 * destroyed two rules.
 *
 * These tests run the real controller against a real PostgreSQL, and assert on the other connector, which must
 * come out of the save exactly as it went in.
 *
 * @author Bruno Alves
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("Connector Rule Ownership Integration Tests (issue #428)")
class ConnectorRuleOwnershipIntegrationTest {

    /**
     * Prefixes the seeded values, so that they cannot collide with the test data set.
     */
    private static final String MARKER = "ZZ428";

    /**
     * The criteria that the untouched rule must still hold.
     */
    private static final String OTHER_CRITERIA = "orderlabel == \"ZZ428-other\"";
    @MockBean
    private ConnectorDiscovererWrapper connectorDiscoverer;

    @BeforeEach
    void configureConnectorPlugin() {
        IConnector plugin = mock(IConnector.class);
        when(plugin.getCode()).thenReturn("test");
        when(plugin.getParams()).thenReturn("[]");
        when(this.connectorDiscoverer.getConnectorForLanguage("test", "fr")).thenReturn(plugin);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConnectorsRepository connectorsRepository;

    @Autowired
    private ProcessesRepository processesRepository;

    @Autowired
    private RulesRepository rulesRepository;

    @Autowired
    private DatabaseTestHelper dbHelper;



    @Nested
    @DisplayName("1. Submitting a rule that belongs to another connector")
    class ForeignRuleTests {

        @Test
        @DisplayName("1.1 - The save is refused, the other connector keeps its rule and this one keeps its own")
        @WithMockApplicationUser(username = "admin", userId = 2, role = "ADMIN")
        @Transactional
        void aForeignRuleIdentifierIsRefused() throws Exception {
            final Process domainProcess = ConnectorRuleOwnershipIntegrationTest.this.process();
            final Connector otherConnector = ConnectorRuleOwnershipIntegrationTest.this.connector("other");
            final Rule otherRule = ConnectorRuleOwnershipIntegrationTest.this.rule(otherConnector, domainProcess,
                                                                                  ConnectorRuleOwnershipIntegrationTest.OTHER_CRITERIA);
            final Connector editedConnector = ConnectorRuleOwnershipIntegrationTest.this.connector("edited");
            final Rule ownRule = ConnectorRuleOwnershipIntegrationTest.this.rule(editedConnector, domainProcess,
                                                                                "orderlabel == \"ZZ428-own\"");

            ConnectorRuleOwnershipIntegrationTest.this.mockMvc.perform(
                    ConnectorRuleOwnershipIntegrationTest.this.saveWithRule(editedConnector, otherRule.getId(),
                                                                           domainProcess.getId(), false))
                                                              .andExpect(status().isOk());

            final Rule reloadedOtherRule
                    = ConnectorRuleOwnershipIntegrationTest.this.rulesRepository.findById(otherRule.getId())
                                                                               .orElseThrow();
            assertEquals(otherConnector.getId(), reloadedOtherRule.getConnector().getId(),
                         "The rule of the other connector has been moved.");
            assertEquals(ConnectorRuleOwnershipIntegrationTest.OTHER_CRITERIA, reloadedOtherRule.getRule(),
                         "The rule of the other connector lost its criteria.");
            assertEquals("orderlabel == \"ZZ428-own\"",
                         ConnectorRuleOwnershipIntegrationTest.this.rulesRepository.findById(ownRule.getId())
                                                                                  .orElseThrow().getRule(),
                         "The rule of the edited connector has been touched.");
        }



        @Test
        @DisplayName("1.2 - A rule identifier that exists nowhere is refused instead of failing")
        @WithMockApplicationUser(username = "admin", userId = 2, role = "ADMIN")
        @Transactional
        void anUnknownRuleIdentifierIsRefused() throws Exception {
            final Process domainProcess = ConnectorRuleOwnershipIntegrationTest.this.process();
            final Connector editedConnector = ConnectorRuleOwnershipIntegrationTest.this.connector("edited");
            final Rule ownRule = ConnectorRuleOwnershipIntegrationTest.this.rule(editedConnector, domainProcess,
                                                                                "orderlabel == \"ZZ428-own\"");

            ConnectorRuleOwnershipIntegrationTest.this.mockMvc.perform(
                    ConnectorRuleOwnershipIntegrationTest.this.saveWithRule(editedConnector, 999999,
                                                                           domainProcess.getId(), false))
                                                              .andExpect(status().isOk());

            assertEquals("orderlabel == \"ZZ428-own\"",
                         ConnectorRuleOwnershipIntegrationTest.this.rulesRepository.findById(ownRule.getId())
                                                                                  .orElseThrow().getRule(),
                         "The rule of the edited connector has been touched.");
        }



        @Test
        @DisplayName("1.3 - Creating a connector with a rule that claims an identifier writes nothing at all")
        @WithMockApplicationUser(username = "admin", userId = 2, role = "ADMIN")
        @Transactional
        void aForeignRuleIdentifierIsRefusedOnCreation() throws Exception {
            final Process domainProcess = ConnectorRuleOwnershipIntegrationTest.this.process();
            final Connector otherConnector = ConnectorRuleOwnershipIntegrationTest.this.connector("other");
            final Rule otherRule = ConnectorRuleOwnershipIntegrationTest.this.rule(otherConnector, domainProcess,
                                                                                  ConnectorRuleOwnershipIntegrationTest.OTHER_CRITERIA);
            final long connectorsBefore
                    = ConnectorRuleOwnershipIntegrationTest.this.connectorsRepository.count();

            ConnectorRuleOwnershipIntegrationTest.this.mockMvc.perform(
                    ConnectorRuleOwnershipIntegrationTest.this.createWithRule(otherRule.getId(),
                                                                             domainProcess.getId()))
                                                              .andExpect(status().isOk());

            assertEquals(connectorsBefore,
                         ConnectorRuleOwnershipIntegrationTest.this.connectorsRepository.count(),
                         "No connector must have been created.");
            final Rule reloadedOtherRule
                    = ConnectorRuleOwnershipIntegrationTest.this.rulesRepository.findById(otherRule.getId())
                                                                               .orElseThrow();
            assertEquals(otherConnector.getId(), reloadedOtherRule.getConnector().getId(),
                         "The rule of the other connector has been moved.");
        }
    }



    @Nested
    @DisplayName("2. Adding a rule")
    class AddedRuleTests {

        @Test
        @DisplayName("2.1 - A rule added with the identifier of another connector's rule leaves that rule alone")
        @WithMockApplicationUser(username = "admin", userId = 2, role = "ADMIN")
        @Transactional
        void anAddedRuleDoesNotOverwriteTheRuleOfAnotherConnector() throws Exception {
            final Process domainProcess = ConnectorRuleOwnershipIntegrationTest.this.process();
            final Connector otherConnector = ConnectorRuleOwnershipIntegrationTest.this.connector("other");
            final Rule otherRule = ConnectorRuleOwnershipIntegrationTest.this.rule(otherConnector, domainProcess,
                                                                                  ConnectorRuleOwnershipIntegrationTest.OTHER_CRITERIA);
            final Connector editedConnector = ConnectorRuleOwnershipIntegrationTest.this.connector("edited");

            ConnectorRuleOwnershipIntegrationTest.this.mockMvc.perform(
                    ConnectorRuleOwnershipIntegrationTest.this.saveWithRule(editedConnector, otherRule.getId(),
                                                                           domainProcess.getId(), true))
                                                              .andExpect(status().is3xxRedirection());

            final Rule reloadedOtherRule
                    = ConnectorRuleOwnershipIntegrationTest.this.rulesRepository.findById(otherRule.getId())
                                                                               .orElseThrow();
            assertEquals(otherConnector.getId(), reloadedOtherRule.getConnector().getId(),
                         "The rule of the other connector has been moved.");
            assertEquals(ConnectorRuleOwnershipIntegrationTest.OTHER_CRITERIA, reloadedOtherRule.getRule(),
                         "The rule of the other connector lost its criteria.");
            final java.util.List<Rule> savedRules
                    = ConnectorRuleOwnershipIntegrationTest.this.rulesRepository
                            .findByConnectorOrderByPosition(editedConnector);
            assertEquals(1, savedRules.size(), "The edited connector must have got its new rule.");
            assertNotEquals(otherRule.getId(), savedRules.get(0).getId(),
                            "The new rule must have an identifier of its own.");
        }
    }



    /**
     * Creates the process that the rules point to.
     *
     * @return the process data object
     */
    private Process process() {
        final Process domainProcess = new Process();
        domainProcess.setName(ConnectorRuleOwnershipIntegrationTest.MARKER + " process");
        domainProcess.setTasksCollection(new ArrayList<>());
        domainProcess.setUsersCollection(new ArrayList<>());
        domainProcess.setUserGroupsCollection(new ArrayList<>());

        return this.processesRepository.save(domainProcess);
    }



    /**
     * Creates a connector that holds no rule.
     *
     * The collection of rules is set even though it is empty, because a connector that has just been persisted
     * carries a null one until it is read again, whereas the application always works on connectors that it has
     * loaded.
     *
     * @param nameSuffix what tells this connector apart from the other ones of the test
     * @return the connector data object
     */
    private Connector connector(final String nameSuffix) {
        final int connectorId = this.dbHelper.createTestConnector(
                String.format("%s %s", ConnectorRuleOwnershipIntegrationTest.MARKER, nameSuffix));
        final Connector domainConnector = this.connectorsRepository.findById(connectorId).orElseThrow();

        if (domainConnector.getRulesCollection() == null) {
            domainConnector.setRulesCollection(new ArrayList<>());
        }

        return domainConnector;
    }



    /**
     * Creates a rule and adds it to a connector.
     *
     * @param domainConnector the connector that the rule belongs to
     * @param domainProcess   the process that the rule points to
     * @param criteria        the expression that the requests must match
     * @return the rule data object
     */
    private Rule rule(final Connector domainConnector, final Process domainProcess, final String criteria) {
        final Rule domainRule = new Rule();
        domainRule.setConnector(domainConnector);
        domainRule.setProcess(domainProcess);
        domainRule.setRule(criteria);
        domainRule.setActive(true);
        domainRule.setPosition(domainConnector.getRulesCollection().size() + 1);
        final Rule savedRule = this.rulesRepository.save(domainRule);
        domainConnector.getRulesCollection().add(savedRule);

        return savedRule;
    }



    /**
     * Builds the submission of a connector holding a single rule that carries a given identifier.
     *
     * @param editedConnector the connector being saved
     * @param ruleId          the identifier that the rule block carries
     * @param processId       the process that the rule points to
     * @param added           <code>true</code> to tag the rule as one that the interface has just added
     * @return the request to perform
     */
    private MockHttpServletRequestBuilder saveWithRule(final Connector editedConnector, final int ruleId,
            final int processId, final boolean added) {
        return this.withConnectorFields(post("/connectors/{id}", editedConnector.getId()),
                                        editedConnector.getName())
                .param("id", String.valueOf(editedConnector.getId()))
                .param("rules[0].id", String.valueOf(ruleId))
                .param("rules[0].tag", added ? "ADDED" : "")
                .param("rules[0].position", "1")
                .param("rules[0].connectorId", String.valueOf(editedConnector.getId()))
                .param("rules[0].rule", "orderlabel == \"" + ConnectorRuleOwnershipIntegrationTest.MARKER + "\"")
                .param("rules[0].processId", String.valueOf(processId))
                .param("rules[0].active", "1");
    }



    /**
     * Builds the creation of a connector holding a single rule that claims a given identifier.
     *
     * @param ruleId    the identifier that the rule block carries
     * @param processId the process that the rule points to
     * @return the request to perform
     */
    private MockHttpServletRequestBuilder createWithRule(final int ruleId, final int processId) {
        return this.withConnectorFields(post("/connectors/add"),
                                        ConnectorRuleOwnershipIntegrationTest.MARKER + " created")
                .param("id", "0")
                .param("rules[0].id", String.valueOf(ruleId))
                .param("rules[0].tag", "")
                .param("rules[0].position", "1")
                .param("rules[0].connectorId", "0")
                .param("rules[0].rule", "orderlabel == \"" + ConnectorRuleOwnershipIntegrationTest.MARKER + "\"")
                .param("rules[0].processId", String.valueOf(processId))
                .param("rules[0].active", "1");
    }



    /**
     * Adds the fields that every connector submission carries.
     *
     * @param request the request being built
     * @param name    the name of the connector
     * @return the request, with the connector fields set
     */
    private MockHttpServletRequestBuilder withConnectorFields(final MockHttpServletRequestBuilder request,
            final String name) {
        return request.with(csrf())
                .param("name", name)
                .param("typeCode", "test")
                .param("typeLabel", "Test Connector")
                .param("importFrequency", "240")
                .param("maximumRetries", "0")
                .param("active", "false");
    }
}
