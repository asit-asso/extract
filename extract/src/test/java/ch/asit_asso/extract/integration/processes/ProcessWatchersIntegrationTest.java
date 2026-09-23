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
package ch.asit_asso.extract.integration.processes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import ch.asit_asso.extract.authentication.ApplicationUser;
import ch.asit_asso.extract.domain.Connector;
import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.domain.UserGroup;
import ch.asit_asso.extract.persistence.ConnectorsRepository;
import ch.asit_asso.extract.persistence.ProcessesRepository;
import ch.asit_asso.extract.persistence.RequestsRepository;
import ch.asit_asso.extract.persistence.UserGroupsRepository;
import ch.asit_asso.extract.persistence.UsersRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the creation of a "consultation" role for process watchers (issue #359).
 *
 * A watcher (direct or through a user group) must be able to see the requests issued by an observed process and
 * download their output files, but must never be able to act on them, unless it is also an operator of the process
 * or an administrator. Watchers must also be the ones returned for the per-request notification e-mail, and only
 * when their account is active and has notifications enabled.
 *
 * @author Bruno Alves
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("Process watchers integration tests (issue #359)")
class ProcessWatchersIntegrationTest {

    /**
     * Prefixes the seeded values, so that they cannot collide with the test data set.
     */
    private static final String MARKER = "ZZ359";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private UserGroupsRepository userGroupsRepository;

    @Autowired
    private ProcessesRepository processesRepository;

    @Autowired
    private RequestsRepository requestsRepository;

    @Autowired
    private ConnectorsRepository connectorsRepository;



    @Nested
    @DisplayName("1. Visibility of a request for a watcher of its process")
    class RequestVisibilityTests {

        @Test
        @DisplayName("1.1 - A direct watcher of the process can view the details of one of its requests")
        @Transactional
        void aDirectWatcherCanViewTheRequest() throws Exception {
            final User watcher = ProcessWatchersIntegrationTest.this.user("watcher11", true, true);
            final Process watchedProcess = ProcessWatchersIntegrationTest.this.process("11");
            watchedProcess.setWatchersCollection(new ArrayList<>(List.of(watcher)));
            ProcessWatchersIntegrationTest.this.processesRepository.save(watchedProcess);
            final Request watchedRequest = ProcessWatchersIntegrationTest.this.request("11", watchedProcess,
                    Request.Status.STANDBY, 1);

            ProcessWatchersIntegrationTest.this.mockMvc.perform(
                    get("/requests/{id}", watchedRequest.getId())
                            .with(ProcessWatchersIntegrationTest.this.asUser(watcher)))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("watcherOnly", true));
        }



        @Test
        @DisplayName("1.2 - A watcher defined through a user group can view the details of a request")
        @Transactional
        void aGroupWatcherCanViewTheRequest() throws Exception {
            final User watcher = ProcessWatchersIntegrationTest.this.user("watcher12", true, true);
            final UserGroup watchersGroup = ProcessWatchersIntegrationTest.this.group("12", watcher);
            final Process watchedProcess = ProcessWatchersIntegrationTest.this.process("12");
            watchedProcess.setWatcherGroupsCollection(new ArrayList<>(List.of(watchersGroup)));
            ProcessWatchersIntegrationTest.this.processesRepository.save(watchedProcess);
            final Request watchedRequest = ProcessWatchersIntegrationTest.this.request("12", watchedProcess,
                    Request.Status.STANDBY, 1);

            ProcessWatchersIntegrationTest.this.mockMvc.perform(
                    get("/requests/{id}", watchedRequest.getId())
                            .with(ProcessWatchersIntegrationTest.this.asUser(watcher)))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("watcherOnly", true));
        }



        @Test
        @DisplayName("1.3 - A user who is neither an operator nor a watcher of the process is denied access")
        @Transactional
        void aStrangerToTheProcessIsDenied() throws Exception {
            final User stranger = ProcessWatchersIntegrationTest.this.user("stranger13", true, true);
            final Process watchedProcess = ProcessWatchersIntegrationTest.this.process("13");
            final Request watchedRequest = ProcessWatchersIntegrationTest.this.request("13", watchedProcess,
                    Request.Status.STANDBY, 1);

            ProcessWatchersIntegrationTest.this.mockMvc.perform(
                    get("/requests/{id}", watchedRequest.getId())
                            .with(ProcessWatchersIntegrationTest.this.asUser(stranger)))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/forbidden"));
        }
    }



    @Nested
    @DisplayName("2. Authorization of actions on a request for a watcher of its process")
    class ActionAuthorizationTests {

        @Test
        @DisplayName("2.1 - A pure watcher cannot validate a standby request")
        @Transactional
        void aPureWatcherCannotValidateTheRequest() throws Exception {
            final User watcher = ProcessWatchersIntegrationTest.this.user("watcher21", true, true);
            final Process watchedProcess = ProcessWatchersIntegrationTest.this.process("21");
            watchedProcess.setWatchersCollection(new ArrayList<>(List.of(watcher)));
            ProcessWatchersIntegrationTest.this.processesRepository.save(watchedProcess);
            final Request watchedRequest = ProcessWatchersIntegrationTest.this.request("21", watchedProcess,
                    Request.Status.STANDBY, 1);

            ProcessWatchersIntegrationTest.this.mockMvc.perform(
                    post("/requests/{id}/validate", watchedRequest.getId()).with(csrf())
                            .with(ProcessWatchersIntegrationTest.this.asUser(watcher))
                            .param("currentStep", "1")
                            .param("remark", ""))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/forbidden"));

            final Request reloadedRequest
                    = ProcessWatchersIntegrationTest.this.requestsRepository.findById(watchedRequest.getId())
                                                                            .orElseThrow();
            assertEquals(Request.Status.STANDBY, reloadedRequest.getStatus(),
                        "The watcher's attempt must not have changed the request status.");
        }



        @Test
        @DisplayName("2.2 - An operator of the process can still validate a standby request")
        @Transactional
        void anOperatorCanValidateTheRequest() throws Exception {
            final User operator = ProcessWatchersIntegrationTest.this.user("operator22", true, true);
            final Process ownedProcess = ProcessWatchersIntegrationTest.this.process("22");
            ownedProcess.setUsersCollection(new ArrayList<>(List.of(operator)));
            ProcessWatchersIntegrationTest.this.processesRepository.save(ownedProcess);
            final Request ownedRequest = ProcessWatchersIntegrationTest.this.request("22", ownedProcess,
                    Request.Status.STANDBY, 1);

            ProcessWatchersIntegrationTest.this.mockMvc.perform(
                    post("/requests/{id}/validate", ownedRequest.getId()).with(csrf())
                            .with(ProcessWatchersIntegrationTest.this.asUser(operator))
                            .param("currentStep", "1")
                            .param("remark", ""))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/requests/" + ownedRequest.getId()));
        }



        @Test
        @DisplayName("2.3 - A watcher who is also an operator of the process keeps every right")
        @Transactional
        void aWatcherWhoIsAlsoAnOperatorCanValidateTheRequest() throws Exception {
            final User both = ProcessWatchersIntegrationTest.this.user("both23", true, true);
            final Process ownedAndWatchedProcess = ProcessWatchersIntegrationTest.this.process("23");
            ownedAndWatchedProcess.setUsersCollection(new ArrayList<>(List.of(both)));
            ownedAndWatchedProcess.setWatchersCollection(new ArrayList<>(List.of(both)));
            ProcessWatchersIntegrationTest.this.processesRepository.save(ownedAndWatchedProcess);
            final Request request = ProcessWatchersIntegrationTest.this.request("23", ownedAndWatchedProcess,
                    Request.Status.STANDBY, 1);

            ProcessWatchersIntegrationTest.this.mockMvc.perform(
                    post("/requests/{id}/validate", request.getId()).with(csrf())
                            .with(ProcessWatchersIntegrationTest.this.asUser(both))
                            .param("currentStep", "1")
                            .param("remark", ""))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/requests/" + request.getId()));
        }
    }



    @Nested
    @DisplayName("3. ProcessesRepository#getProcessWatchers only returns notifiable watchers")
    class ProcessWatchersRepositoryTests {

        @Test
        @DisplayName("3.1 - An active watcher with notifications enabled is returned")
        @Transactional
        void anActiveNotifiableWatcherIsReturned() {
            final User watcher = ProcessWatchersIntegrationTest.this.user("watcher31", true, true);
            final Process watchedProcess = ProcessWatchersIntegrationTest.this.process("31");
            watchedProcess.setWatchersCollection(new ArrayList<>(List.of(watcher)));
            ProcessWatchersIntegrationTest.this.processesRepository.save(watchedProcess);

            final List<User> notifiedWatchers
                    = ProcessWatchersIntegrationTest.this.processesRepository.getProcessWatchers(
                            watchedProcess.getId());

            assertEquals(1, notifiedWatchers.size(), "Only the notifiable watcher must be returned.");
            assertEquals(watcher.getId(), notifiedWatchers.get(0).getId());
        }



        @Test
        @DisplayName("3.2 - An inactive watcher or a watcher with notifications disabled is excluded")
        @Transactional
        void aNonNotifiableWatcherIsExcluded() {
            final User inactiveWatcher = ProcessWatchersIntegrationTest.this.user("inactive32", false, true);
            final User silencedWatcher = ProcessWatchersIntegrationTest.this.user("silenced32", true, false);
            final Process watchedProcess = ProcessWatchersIntegrationTest.this.process("32");
            watchedProcess.setWatchersCollection(new ArrayList<>(List.of(inactiveWatcher, silencedWatcher)));
            ProcessWatchersIntegrationTest.this.processesRepository.save(watchedProcess);

            final List<User> notifiedWatchers
                    = ProcessWatchersIntegrationTest.this.processesRepository.getProcessWatchers(
                            watchedProcess.getId());

            assertTrue(notifiedWatchers.isEmpty(),
                      "Neither the inactive watcher nor the silenced one must be returned.");
        }



        @Test
        @DisplayName("3.3 - A notifiable watcher defined through a user group is included")
        @Transactional
        void aNotifiableGroupWatcherIsIncluded() {
            final User groupWatcher = ProcessWatchersIntegrationTest.this.user("groupwatcher33", true, true);
            final UserGroup watchersGroup = ProcessWatchersIntegrationTest.this.group("33", groupWatcher);
            final Process watchedProcess = ProcessWatchersIntegrationTest.this.process("33");
            watchedProcess.setWatcherGroupsCollection(new ArrayList<>(List.of(watchersGroup)));
            ProcessWatchersIntegrationTest.this.processesRepository.save(watchedProcess);

            final List<User> notifiedWatchers
                    = ProcessWatchersIntegrationTest.this.processesRepository.getProcessWatchers(
                            watchedProcess.getId());

            assertEquals(1, notifiedWatchers.size(), "The group watcher must be returned.");
            assertEquals(groupWatcher.getId(), notifiedWatchers.get(0).getId());
        }
    }



    @Nested
    @DisplayName("4. ProcessesRepository#findWatchedProcessesByUser")
    class WatchedProcessesRepositoryTests {

        @Test
        @DisplayName("4.1 - The processes a user watches directly or through a group are both returned")
        @Transactional
        void watchedProcessesAreFoundDirectlyAndThroughAGroup() {
            final User watcher = ProcessWatchersIntegrationTest.this.user("watcher41", true, true);
            final UserGroup watchersGroup = ProcessWatchersIntegrationTest.this.group("41", watcher);

            final Process directlyWatchedProcess = ProcessWatchersIntegrationTest.this.process("41a");
            directlyWatchedProcess.setWatchersCollection(new ArrayList<>(List.of(watcher)));
            ProcessWatchersIntegrationTest.this.processesRepository.save(directlyWatchedProcess);

            final Process groupWatchedProcess = ProcessWatchersIntegrationTest.this.process("41b");
            groupWatchedProcess.setWatcherGroupsCollection(new ArrayList<>(List.of(watchersGroup)));
            ProcessWatchersIntegrationTest.this.processesRepository.save(groupWatchedProcess);

            final Collection<Process> watchedProcesses
                    = ProcessWatchersIntegrationTest.this.processesRepository.findWatchedProcessesByUser(
                            watcher.getId());

            assertEquals(2, watchedProcesses.size(),
                        "Both the directly watched process and the group-watched one must be returned.");
            assertTrue(watchedProcesses.stream().anyMatch((p) -> p.getId() == directlyWatchedProcess.getId()));
            assertTrue(watchedProcesses.stream().anyMatch((p) -> p.getId() == groupWatchedProcess.getId()));
        }



        @Test
        @DisplayName("4.2 - A user who watches nothing gets an empty list")
        @Transactional
        void aUserWithNoWatchGetsNothing() {
            final User outsider = ProcessWatchersIntegrationTest.this.user("outsider42", true, true);
            final Process someProcess = ProcessWatchersIntegrationTest.this.process("42");
            ProcessWatchersIntegrationTest.this.processesRepository.save(someProcess);

            final Collection<Process> watchedProcesses
                    = ProcessWatchersIntegrationTest.this.processesRepository.findWatchedProcessesByUser(
                            outsider.getId());

            assertFalse(watchedProcesses.stream().anyMatch((p) -> p.getId() == someProcess.getId()),
                       "A process that the user does not watch must not be returned.");
        }
    }



    /**
     * Creates a test connector.
     *
     * @param nameSuffix what tells this connector apart from the other ones of the test
     * @return the connector data object
     */
    private Connector connector(final String nameSuffix) {
        final Connector domainConnector = new Connector();
        domainConnector.setName(String.format("%s Connector %s", ProcessWatchersIntegrationTest.MARKER,
                                              nameSuffix));
        domainConnector.setActive(Boolean.TRUE);

        return this.connectorsRepository.save(domainConnector);
    }



    /**
     * Creates a test user.
     *
     * @param nameSuffix what tells this user apart from the other ones of the test
     * @param active     whether the user account is active
     * @param mailActive whether the user has e-mail notifications enabled
     * @return the user data object
     */
    private User user(final String nameSuffix, final boolean active, final boolean mailActive) {
        final User domainUser = new User();
        domainUser.setLogin(String.format("%s_%s", ProcessWatchersIntegrationTest.MARKER, nameSuffix));
        domainUser.setName(String.format("%s %s", ProcessWatchersIntegrationTest.MARKER, nameSuffix));
        domainUser.setEmail(String.format("%s.%s@example.com", ProcessWatchersIntegrationTest.MARKER, nameSuffix));
        domainUser.setPassword("password");
        domainUser.setActive(active);
        domainUser.setMailActive(mailActive);
        domainUser.setProfile(User.Profile.OPERATOR);

        return this.usersRepository.save(domainUser);
    }



    /**
     * Creates a test user group.
     *
     * @param nameSuffix what tells this group apart from the other ones of the test
     * @param members    the users that belong to the group
     * @return the user group data object
     */
    private UserGroup group(final String nameSuffix, final User... members) {
        final UserGroup domainGroup = new UserGroup();
        domainGroup.setName(String.format("%s Group %s", ProcessWatchersIntegrationTest.MARKER, nameSuffix));
        domainGroup.setUsersCollection(new ArrayList<>(List.of(members)));

        return this.userGroupsRepository.save(domainGroup);
    }



    /**
     * Creates a process that holds no task, operator or watcher.
     *
     * @param nameSuffix what tells this process apart from the other ones of the test
     * @return the process data object
     */
    private Process process(final String nameSuffix) {
        final Process domainProcess = new Process();
        domainProcess.setName(String.format("%s Process %s", ProcessWatchersIntegrationTest.MARKER, nameSuffix));
        domainProcess.setTasksCollection(new ArrayList<>());
        domainProcess.setUsersCollection(new ArrayList<>());
        domainProcess.setUserGroupsCollection(new ArrayList<>());
        domainProcess.setWatchersCollection(new ArrayList<>());
        domainProcess.setWatcherGroupsCollection(new ArrayList<>());

        return this.processesRepository.save(domainProcess);
    }



    /**
     * Creates a request attached to a process of its own connector, with an empty owners collection.
     *
     * @param nameSuffix   what tells this request apart from the other ones of the test
     * @param parentProcess the process that the request belongs to
     * @param status       the status that the request must carry
     * @param tasknum      the number of the step that is currently active for the request
     * @return the request data object
     */
    private Request request(final String nameSuffix, final Process parentProcess, final Request.Status status,
            final int tasknum) {
        final Request domainRequest = new Request();
        domainRequest.setProductLabel(String.format("%s Product %s", ProcessWatchersIntegrationTest.MARKER,
                                                     nameSuffix));
        domainRequest.setOrderLabel(String.format("%sOrder%s", ProcessWatchersIntegrationTest.MARKER, nameSuffix));
        domainRequest.setClient("Test Client");
        domainRequest.setStatus(status);
        domainRequest.setFolderOut(null);
        domainRequest.setStartDate(new GregorianCalendar());
        domainRequest.setConnector(this.connector(nameSuffix));
        domainRequest.setParameters("{}");
        domainRequest.setPerimeter("{}");
        domainRequest.setProcess(parentProcess);
        domainRequest.setTasknum(tasknum);
        domainRequest.setUsersCollection(new ArrayList<>());
        domainRequest.setUserGroupsCollection(new ArrayList<>());

        return this.requestsRepository.save(domainRequest);
    }



    /**
     * Builds a request post processor that authenticates the mock request as a given application user.
     *
     * @param domainUser the user data object to authenticate as
     * @return the post processor to append to a mock request
     */
    private RequestPostProcessor asUser(final User domainUser) {
        final ApplicationUser principal = new ApplicationUser(domainUser);
        final Authentication mockAuthentication
                = new UsernamePasswordAuthenticationToken(principal, "password", principal.getAuthorities());

        return authentication(mockAuthentication);
    }
}
