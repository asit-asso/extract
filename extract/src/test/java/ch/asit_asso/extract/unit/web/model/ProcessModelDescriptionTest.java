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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ch.asit_asso.extract.unit.web.model;

import java.util.Collections;
import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.persistence.RequestsRepository;
import ch.asit_asso.extract.plugins.implementation.TaskProcessorDiscovererWrapper;
import ch.asit_asso.extract.web.model.ProcessModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the description added to processes (issue #374).
 *
 * @author Bruno Alves
 */
@DisplayName("Process Description Model Tests (issue #374)")
class ProcessModelDescriptionTest {

    private static final String DESCRIPTION = "Documents the purpose of this process.";

    private RequestsRepository requestsRepository;



    @BeforeEach
    void setUp() {
        this.requestsRepository = mock(RequestsRepository.class);
        when(this.requestsRepository.findByStatusAndProcessIn(eq(Request.Status.ONGOING), any()))
                .thenReturn(Collections.emptyList());
        when(this.requestsRepository.findByStatusNotAndProcessIn(eq(Request.Status.FINISHED), any()))
                .thenReturn(Collections.emptyList());
    }



    @Test
    @DisplayName("A description round-trips between the domain object and the form model")
    void descriptionRoundTripsBetweenDomainObjectAndModel() {
        final ProcessModel model = this.modelFor(this.process(ProcessModelDescriptionTest.DESCRIPTION));
        final Process updatedProcess = this.process("Previous description");

        assertEquals(ProcessModelDescriptionTest.DESCRIPTION, model.getDescription());

        model.updateDomainObject(updatedProcess, null, null);

        assertEquals(ProcessModelDescriptionTest.DESCRIPTION, updatedProcess.getDescription());
    }



    @Test
    @DisplayName("A null description remains null through the form model")
    void nullDescriptionRemainsNullThroughModel() {
        final ProcessModel model = this.modelFor(this.process(null));
        final Process updatedProcess = this.process("Previous description");

        assertNull(model.getDescription());

        model.updateDomainObject(updatedProcess, null, null);

        assertNull(updatedProcess.getDescription());
    }



    @Test
    @DisplayName("A cloned process retains its description")
    void clonedProcessRetainsDescription() {
        final Process copiedProcess = this.process(ProcessModelDescriptionTest.DESCRIPTION).createCopy();

        assertEquals(ProcessModelDescriptionTest.DESCRIPTION, copiedProcess.getDescription());
    }



    private ProcessModel modelFor(final Process domainProcess) {
        return new ProcessModel(domainProcess, new TaskProcessorDiscovererWrapper(), this.requestsRepository);
    }



    private Process process(final String description) {
        final Process process = new Process(374);
        process.setName("Process description test");
        process.setDescription(description);
        process.setTasksCollection(Collections.emptyList());
        process.setUsersCollection(Collections.emptyList());
        process.setUserGroupsCollection(Collections.emptyList());
        process.setRequestsCollection(Collections.emptyList());
        process.setRulesCollection(Collections.emptyList());

        return process;
    }
}
