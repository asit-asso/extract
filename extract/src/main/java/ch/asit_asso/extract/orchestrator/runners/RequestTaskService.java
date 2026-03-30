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
package ch.asit_asso.extract.orchestrator.runners;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.RequestHistoryRecord;
import ch.asit_asso.extract.domain.Task;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.exceptions.SystemUserNotFoundException;
import ch.asit_asso.extract.persistence.ApplicationRepositories;
import ch.asit_asso.extract.persistence.RequestHistoryRepository;
import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



/**
 * Provides transactional operations for request task processing to prevent race conditions
 * and ensure atomic database operations.
 *
 * @author Extract Team
 */
@Service
public class RequestTaskService {

    private final Logger logger = LoggerFactory.getLogger(RequestTaskService.class);

    private final ApplicationRepositories applicationRepositories;



    public RequestTaskService(final ApplicationRepositories applicationRepositories) {

        if (applicationRepositories == null) {
            throw new IllegalArgumentException("The application repositories object cannot be null.");
        }

        this.applicationRepositories = applicationRepositories;
    }



    /**
     * Atomically creates a new history record for a task execution. Uses MAX(step) + 1 via a
     * database query to prevent step number collisions when concurrent threads process the same request.
     *
     * @param request the request to create a history record for
     * @param task    the task being executed
     * @return the persisted history record
     */
    @Transactional
    public RequestHistoryRecord createHistoryRecord(final Request request, final Task task) {

        if (request == null) {
            throw new IllegalArgumentException("The request cannot be null.");
        }

        if (task == null) {
            throw new IllegalArgumentException("The task cannot be null.");
        }

        final User systemUser = this.applicationRepositories.getUsersRepository().getSystemUser();

        if (systemUser == null) {
            throw new SystemUserNotFoundException();
        }

        final RequestHistoryRepository historyRepository = this.applicationRepositories.getRequestHistoryRepository();
        final int step = historyRepository.findNextStepByRequest(request);
        this.logger.debug("Atomically computed step {} for request {}.", step, request.getId());

        final RequestHistoryRecord historyRecord = new RequestHistoryRecord();
        historyRecord.setRequest(request);
        historyRecord.setStartDate(new GregorianCalendar());
        historyRecord.setStep(step);
        historyRecord.setProcessStep(task.getPosition());
        historyRecord.setTaskLabel(task.getLabel());
        historyRecord.setStatus(RequestHistoryRecord.Status.ONGOING);
        historyRecord.setUser(systemUser);

        return historyRepository.save(historyRecord);
    }



    /**
     * Atomically updates the request and its history record with the task result.
     * Uses optimistic locking via @Version to detect concurrent modifications.
     *
     * @param request          the request to update
     * @param historyRecord    the history record to update
     * @param taskResultStatus the status to set
     * @param message          the result message
     * @param taskEndDate      when the task ended
     * @param modifiedRequest  modified request data from the plugin, or null
     * @return the updated request
     * @throws ObjectOptimisticLockingFailureException if the request was modified concurrently
     */
    @Transactional
    public Request updateTaskResult(final Request request, final RequestHistoryRecord historyRecord,
            final RequestHistoryRecord.Status taskResultStatus, final String message,
            final Calendar taskEndDate, final ITaskProcessorRequest modifiedRequest) {

        if (request == null) {
            throw new IllegalArgumentException("The request cannot be null.");
        }

        if (historyRecord == null) {
            throw new IllegalArgumentException("The history record cannot be null.");
        }

        if (taskResultStatus == null || taskResultStatus == RequestHistoryRecord.Status.ONGOING) {
            throw new IllegalArgumentException("The task result status must be a terminal state.");
        }

        historyRecord.setStatus(taskResultStatus);
        historyRecord.setEndDate(taskEndDate);
        historyRecord.setMessage(message);
        this.applicationRepositories.getRequestHistoryRepository().save(historyRecord);

        return this.applicationRepositories.getRequestsRepository().save(request);
    }



    /**
     * Atomically deletes a history record (used when a task plugin returns NOT_RUN).
     *
     * @param historyRecord the history record to delete
     */
    @Transactional
    public void deleteHistoryRecord(final RequestHistoryRecord historyRecord) {

        if (historyRecord == null) {
            throw new IllegalArgumentException("The history record cannot be null.");
        }

        this.applicationRepositories.getRequestHistoryRepository().delete(historyRecord);
    }



    /**
     * Atomically marks a request as ready for export.
     *
     * @param request the request to mark for export
     * @return the updated request
     */
    @Transactional
    public Request markRequestForExport(final Request request) {

        if (request == null) {
            throw new IllegalArgumentException("The request cannot be null.");
        }

        request.setStatus(Request.Status.TOEXPORT);
        return this.applicationRepositories.getRequestsRepository().save(request);
    }



    /**
     * Fetches the ongoing requests with a pessimistic lock to prevent concurrent scheduler
     * cycles from dispatching the same requests.
     *
     * @return a list of locked ongoing requests
     */
    @Transactional
    public List<Request> getOngoingRequestsWithLock() {
        return this.applicationRepositories.getRequestsRepository()
                .findByStatusWithLock(Request.Status.ONGOING);
    }



    /**
     * Re-reads a request from the database to get the latest state.
     *
     * @param requestId the request identifier
     * @return the fresh request, or null if not found
     */
    @Transactional(readOnly = true)
    public Request refreshRequest(final int requestId) {
        return this.applicationRepositories.getRequestsRepository().findById(requestId).orElse(null);
    }
}
