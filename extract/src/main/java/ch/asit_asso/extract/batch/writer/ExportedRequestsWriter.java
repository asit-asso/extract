/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.asit_asso.extract.batch.writer;

import java.util.List;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.persistence.RequestsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;



/**
 * An object that will make the result of a request import permanent.
 *
 * @author Yves Grasset
 */
public class ExportedRequestsWriter implements ItemWriter<Request> {

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(ExportedRequestsWriter.class);

    /**
     * The link between the request data objects and the data source.
     */
    private RequestsRepository requestsRepository;



    /**
     * Creates a new instance of this writer.
     *
     * @param repository the link between requests objects and the database
     */
    public ExportedRequestsWriter(final RequestsRepository repository) {

        if (repository == null) {
            throw new IllegalArgumentException("The request repository cannot be null.");
        }

        this.requestsRepository = repository;
    }



    /**
     * Persists the state of a list of exported requests.
     *
     * @param requestsList a list of requests that have been updated to reflect the result of their export.
     */
    @Override
    public final void write(final List<? extends Request> requestsList) {
        assert this.requestsRepository != null : "The request repository must be set.";

        for (Request request : requestsList) {
            this.logger.debug("The status of the request {} before saving it is {}.", request.getId(),
                    request.getStatus());
            Request savedRequest = this.requestsRepository.save(request);
            this.logger.debug("The status of the saved request {} is {}.", savedRequest.getId(),
                    savedRequest.getStatus());
        }

        this.logger.debug("{} exported requests have been saved.", requestsList.size());
    }

}
