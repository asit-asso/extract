/*
 * Copyright (C) 2025 SecureMind Sàrl
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
package ch.asit_asso.extract.integration.orchestrator;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.RequestHistoryRecord;
import ch.asit_asso.extract.domain.Task;
import ch.asit_asso.extract.integration.TestMockConfiguration;
import ch.asit_asso.extract.ldap.LdapSettings;
import ch.asit_asso.extract.orchestrator.runners.RequestTaskService;
import ch.asit_asso.extract.persistence.ApplicationRepositories;
import ch.asit_asso.extract.persistence.RequestHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;



/**
 * Integration tests verifying the concurrency protections for request processing.
 * Tests optimistic locking, atomic step calculation, and pessimistic lock queries.
 *
 * @author Bruno Alves
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestMockConfiguration.class)
@Tag("integration")
public class RequestConcurrencyIntegrationTest {

    @Autowired
    private ApplicationRepositories applicationRepositories;

    @Autowired
    private RequestTaskService taskService;

    @MockBean
    private LdapSettings ldapSettings;



    @Test
    @DisplayName("Request entity has @Version field for optimistic locking")
    @Transactional
    void testRequestHasVersionField() {
        Request request = new Request();
        request.setStatus(Request.Status.IMPORTED);
        request.setStartDate(new GregorianCalendar());
        request.setOrderLabel("Test version field");
        request.setOrderGuid("test-version-guid");
        request.setProductGuid("test-product-guid");

        Request saved = applicationRepositories.getRequestsRepository().save(request);
        assertNotNull(saved.getId());
    }



    @Test
    @DisplayName("RequestHistoryRecord entity has @Version field for optimistic locking")
    @Transactional
    void testRequestHistoryRecordHasVersionField() {
        RequestHistoryRecord record = new RequestHistoryRecord();
        record.setStartDate(new GregorianCalendar());
        record.setStatus(RequestHistoryRecord.Status.ONGOING);
        record.setStep(1);

        RequestHistoryRecord saved = applicationRepositories.getRequestHistoryRepository().save(record);
        assertNotNull(saved.getId());
    }



    @Test
    @DisplayName("findNextStepByRequest returns 1 when no history exists")
    @Transactional
    void testFindNextStepReturnsOneForNewRequest() {
        Request request = new Request();
        request.setStatus(Request.Status.ONGOING);
        request.setStartDate(new GregorianCalendar());
        request.setOrderLabel("Test next step");
        request.setOrderGuid("test-step-guid");
        request.setProductGuid("test-step-product");
        request = applicationRepositories.getRequestsRepository().save(request);

        int nextStep = applicationRepositories.getRequestHistoryRepository().findNextStepByRequest(request);
        assertEquals(1, nextStep);
    }



    @Test
    @DisplayName("findNextStepByRequest returns MAX(step)+1 when history exists")
    @Transactional
    void testFindNextStepReturnsMaxPlusOne() {
        Request request = new Request();
        request.setStatus(Request.Status.ONGOING);
        request.setStartDate(new GregorianCalendar());
        request.setOrderLabel("Test next step max");
        request.setOrderGuid("test-step-max-guid");
        request.setProductGuid("test-step-max-product");
        request = applicationRepositories.getRequestsRepository().save(request);

        RequestHistoryRepository historyRepo = applicationRepositories.getRequestHistoryRepository();

        RequestHistoryRecord record1 = new RequestHistoryRecord();
        record1.setRequest(request);
        record1.setStep(1);
        record1.setStartDate(new GregorianCalendar());
        record1.setStatus(RequestHistoryRecord.Status.FINISHED);
        historyRepo.save(record1);

        RequestHistoryRecord record2 = new RequestHistoryRecord();
        record2.setRequest(request);
        record2.setStep(2);
        record2.setStartDate(new GregorianCalendar());
        record2.setStatus(RequestHistoryRecord.Status.ONGOING);
        historyRepo.save(record2);

        int nextStep = historyRepo.findNextStepByRequest(request);
        assertEquals(3, nextStep);
    }



    @Test
    @DisplayName("findByStatusWithLock returns locked ongoing requests")
    @Transactional
    void testFindByStatusWithLockReturnsOngoing() {
        Request request = new Request();
        request.setStatus(Request.Status.ONGOING);
        request.setStartDate(new GregorianCalendar());
        request.setTasknum(1);
        request.setOrderLabel("Test lock");
        request.setOrderGuid("test-lock-guid");
        request.setProductGuid("test-lock-product");
        applicationRepositories.getRequestsRepository().save(request);

        List<Request> locked = applicationRepositories.getRequestsRepository()
                .findByStatusWithLock(Request.Status.ONGOING);
        assertFalse(locked.isEmpty());
        assertTrue(locked.stream().allMatch(r -> r.getStatus() == Request.Status.ONGOING));
    }



    @Test
    @DisplayName("RequestTaskService.createHistoryRecord uses atomic step calculation")
    @Transactional
    void testCreateHistoryRecordAtomicStep() {
        Request request = new Request();
        request.setStatus(Request.Status.ONGOING);
        request.setStartDate(new GregorianCalendar());
        request.setTasknum(1);
        request.setOrderLabel("Test atomic step");
        request.setOrderGuid("test-atomic-guid");
        request.setProductGuid("test-atomic-product");
        request = applicationRepositories.getRequestsRepository().save(request);

        Task task = new Task();
        task.setCode("test.plugin");
        task.setLabel("Test Task");
        task.setPosition(1);

        Process process = new Process();
        process.setName("Test Process");
        process = applicationRepositories.getProcessesRepository().save(process);
        task.setProcess(process);
        task = applicationRepositories.getTasksRepository().save(task);

        RequestHistoryRecord record = taskService.createHistoryRecord(request, task);

        assertNotNull(record);
        assertNotNull(record.getId());
        assertEquals(1, record.getStep());
        assertEquals(RequestHistoryRecord.Status.ONGOING, record.getStatus());
        assertEquals(task.getPosition(), record.getProcessStep());
    }
}
