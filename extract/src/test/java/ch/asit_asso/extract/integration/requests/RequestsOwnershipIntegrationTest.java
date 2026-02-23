package ch.asit_asso.extract.integration.requests;

/*
 * Copyright (C) 2025 arusakov
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Integration tests for RequestModel with database, specifically testing handling of IMPORTFAIL requests.
 * This addresses issue #333: Requests without geographical perimeter should be handled gracefully.
 */
@SpringBootTest
@ActiveProfiles("test")
@Tag("integration")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RequestsOwnershipIntegrationTest {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private UserGroupsRepository userGroupsRepository;
    
    @Autowired
    private RequestsRepository requestsRepository;

    @Autowired
    private ConnectorsRepository connectorsRepository;

    @Autowired
    private ProcessesRepository processesRepository;
    
    private User addTestUser(String name) {
        var testUser = new User();
        testUser.setLogin(name);
        testUser.setName(String.format("Test User %s", name));
        testUser.setEmail(String.format("%s.i18n@example.com", name));
        testUser.setPassword("password");
        testUser.setActive(true);
        testUser.setProfile(User.Profile.OPERATOR);
        return usersRepository.save(testUser); 
    }
    
    private UserGroup addTestGroup(String name, User... users) {
        var testGroup = new UserGroup();
        testGroup.setName(name);
        testGroup.setUsersCollection(List.of(users));
        return userGroupsRepository.save(testGroup);
    }
    
    private Request addTestRequest(String name, Process process, Connector connector) {
        testRequest = new Request();
        testRequest.setProductLabel(String.format("Test Request %s", name));
        testRequest.setOrderLabel(String.format("RequestOrder%s", name));
        testRequest.setClient("Test Client");
        testRequest.setStatus(Request.Status.FINISHED);
        testRequest.setFolderOut(null); // This is the key - null folder
        testRequest.setStartDate(new GregorianCalendar());
        testRequest.setConnector(connector);
        testRequest.setParameters("{}");
        testRequest.setPerimeter("{}");
        testRequest.setUsersCollection(new ArrayList<>());
        testRequest.setUserGroupsCollection(new ArrayList<>());
        return requestsRepository.save(testRequest);
    }
    
    private Connector testConnector;

    private Process testProcess;
    private Request testRequest;
    private Request testRequest1;

    private User user1;
    private UserGroup group1;
    private User user2;
    private UserGroup group2;    
    private User user3;

    @BeforeAll
    public void setUpAll() {
        // Clean up any existing test data to avoid conflicts with other integration tests
        requestsRepository.deleteAll();
        connectorsRepository.deleteAll();

        // Create a test connector
        testConnector = new Connector();
        testConnector.setName("Test Connector for RequestModel");
        testConnector.setActive(Boolean.TRUE);
        testConnector = connectorsRepository.save(testConnector);
        
        testProcess = new Process();
        testProcess.setName("Standard Process");
        testProcess = processesRepository.save(testProcess);
        
        testRequest = addTestRequest("TestRequest", testProcess, testConnector);
        testRequest1 = addTestRequest("TestRequest1", testProcess, testConnector);
        
    }
    
    @BeforeEach
    public void setUpTestData() {
        user1 = addTestUser("ownership_test_user1");
        user2 = addTestUser("ownership_test_user2");
        user3 = addTestUser("ownership_test_user3");
        
        group1 = addTestGroup("group_user1", user1);
        group2 = addTestGroup("group_user2", user2);
        
        Stream.of(testRequest, testRequest1).forEach((request) -> {
            request.setUsersCollection(new ArrayList<>());
            request.setUserGroupsCollection(new ArrayList<>());
            requestsRepository.save(testRequest);
        });

        testProcess.setUsersCollection(new ArrayList<>());
        testProcess.setUserGroupsCollection(new ArrayList<>());
        testProcess = processesRepository.save(testProcess);

        SecurityContextHolder.clearContext();
    }
    
    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();

        Stream.of(user1, user2, user3).forEach(usersRepository::delete);
        Stream.of(group1, group2).forEach(userGroupsRepository::delete);
    }
    
    private Collection<Request> getRequests(User user) {
        return usersRepository.getUserAssociatedRequestsByStatusNot(
                user.getId(), Request.Status.ERROR);
    }

    @Test
    public void testNoVisibility() {
        assertTrue(getRequests(user1).isEmpty(), "No requests visible to the user1");
        assertTrue(getRequests(user2).isEmpty(), "No requests visible to the user2");
    }
    
    @Test
    public void testUserVisibilityFromProcess() {
        testProcess.setUsersCollection(new ArrayList<>(List.of(user1)));
        processesRepository.save(testProcess);
        testRequest.setProcess(testProcess);
        requestsRepository.save(testRequest);

        assertEquals(List.of(testRequest), getRequests(user1), "One request is visible for user1");
        assertTrue(getRequests(user2).isEmpty(), "No requests visible to the user2");
        
        group1.setUsersCollection(new ArrayList<>(List.of(user2)));
        testProcess.setUserGroupsCollection(new ArrayList<>(List.of(group1)));

        assertEquals(List.of(testRequest), getRequests(user1), "One request is visible for user1");
        assertEquals(List.of(testRequest), getRequests(user1), "One request s visible for user2");
    }
    
    @Test
    public void testUserVisibilityFromRequest() {
        testRequest.setUsersCollection(new ArrayList<>(List.of(user1)));
        requestsRepository.save(testRequest);

        assertEquals(List.of(testRequest), getRequests(user1), "One request is visible for user1");
        assertTrue(getRequests(user2).isEmpty(), "No requests visible to the user2"); 

        group1.setUsersCollection(new ArrayList<>(List.of(user2)));
        testRequest.setUserGroupsCollection(new ArrayList<>(List.of(group1)));

        assertEquals(List.of(testRequest), getRequests(user1), "One request is visible for user1");
        assertEquals(List.of(testRequest), getRequests(user1), "One request s visible for user2");        
    }
    
    
}
