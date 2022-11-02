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
package ch.asit_asso.extract.batch.reader;

import java.util.ArrayDeque;
import java.util.Queue;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.Request.Status;
import ch.asit_asso.extract.persistence.RequestsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;



/**
 * An object that will fetch the requests that have a certain status.
 *
 * @author Yves Grasset
 */
public class RequestByStatusReader implements ItemReader<Request> {

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(RequestByStatusReader.class);

    /**
     * The collection that contains the requests with the desired status that still have to be processed.
     */
    private Queue<Request> requestsQueue;

    /**
     * The link between the requests data objects and the database.
     */
    private RequestsRepository requestsRepository;

    /**
     * The status of the requests to fetch.
     */
    private final Status requestStatus;



    /**
     * Creates a new instance of the request reader.
     *
     * @param status     the state that the requests to fetch must be in
     * @param repository the link between the request data objects and the data source
     */
    public RequestByStatusReader(final Status status, final RequestsRepository repository) {

        if (repository == null) {
            throw new IllegalArgumentException("The requests repository cannot be null.");
        }

        if (status == null) {
            throw new IllegalArgumentException("The status of the requests to fetch cannot be null.");
        }

        this.requestsRepository = repository;
        this.requestStatus = status;
        this.fetchRequests();
    }



    /**
     * Gets the next request to process.
     *
     * @return a request or <code>null</code> if none remains to be processed
     */
    @Override
    public final Request read() {
        assert this.requestsQueue != null : "The requests queue should be initialized by now.";

        return this.requestsQueue.poll();
    }



    /**
     * Reads all the requests in the database that have the desired status.
     */
    private void fetchRequests() {
        assert this.requestsRepository != null : "The requests repository must be set.";
        assert this.requestStatus != null : "The status of the requests to fetch must be set.";

        this.requestsQueue = new ArrayDeque<>(this.requestsRepository.findByStatus(this.requestStatus));
        int numberOfRequests = this.requestsQueue.size();
        this.logger.debug("Found {} request{} with status {}.", numberOfRequests, (numberOfRequests > 1) ? "s" : "",
                this.requestStatus);
    }

}
