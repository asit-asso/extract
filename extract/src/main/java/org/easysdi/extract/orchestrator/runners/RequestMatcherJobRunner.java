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
package org.easysdi.extract.orchestrator.runners;

import java.util.Arrays;
import org.easysdi.extract.batch.processor.RequestMatchingProcessor;
import org.easysdi.extract.batch.reader.RequestByStatusReader;
import org.easysdi.extract.domain.Request;
import org.easysdi.extract.domain.Request.Status;
import org.easysdi.extract.email.EmailSettings;
import org.easysdi.extract.persistence.ApplicationRepositories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;



/**
 * Executes a batch process that will attempt to match the request that have just been created with a
 * process through the rules defined by its connector.
 *
 * @author Yves Grasset
 */
public class RequestMatcherJobRunner /*extends JobRunner<Request, Request>*/ implements Runnable {

    /**
     * An ensemble of objects linking the data objects with the database.
     */
    private final ApplicationRepositories applicationRepositories;

    /**
     * The object that assembles the configuration objects required to create and send an e-mail message.
     */
    private final EmailSettings emailSettings;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(RequestMatcherJobRunner.class);



    /**
     * Creates a new instance of the runner.
     *
     * @param repositories an ensemble of objects linking the data objects with the database
     * @param smtpSettings an object that assembles the configuration objects required to create and send an
     *                     e-mail message.
     */
    public RequestMatcherJobRunner(final ApplicationRepositories repositories, final EmailSettings smtpSettings) {

        if (repositories == null) {
            throw new IllegalArgumentException("The application repositories object cannot be null.");
        }

        if (smtpSettings == null) {
            throw new IllegalArgumentException("The e-mail settings object cannot be null.");
        }

        this.applicationRepositories = repositories;
        this.emailSettings = smtpSettings;
    }



    @Override
    public final void run() {
        this.logger.debug("Performing request process matching job.");

        final ItemReader<Request> importedRequestsReader = this.getReader();
        final ItemProcessor<Request, Request> requestMatcher = this.getProcessor();
        final ItemWriter<Request> requestWriter = this.getWriter();

        try {
            Request importedRequest = importedRequestsReader.read();

            while (importedRequest != null) {

                try {
                    Request processedRequest = requestMatcher.process(importedRequest);
                    requestWriter.write(Arrays.asList(processedRequest));

                } catch (Exception exception) {
                    this.logger.error("An error occurred during the execution of the request process matching job for"
                            + " request {}.", importedRequest.getId(), exception);
                }

                importedRequest = importedRequestsReader.read();
            }

        } catch (Exception exception) {
            this.logger.error("Could not read all the imported requests.", exception);
        }
    }



    /**
     * Obtains the object that will match the requests with a process based on the connector rules.
     *
     * @return the request matcher
     */
    public final ItemProcessor<Request, Request> getProcessor() {
        return new RequestMatchingProcessor(this.applicationRepositories.getRulesRepository(),
                this.applicationRepositories.getParametersRepository(),
                this.applicationRepositories.getUsersRepository(), this.emailSettings);
    }



    /**
     * Obtains the object that will fetch the requests that are ready to be matched with a process.
     *
     * @return the requests-to-match reader
     */
    public final ItemReader<Request> getReader() {
        return new RequestByStatusReader(Status.IMPORTED, this.applicationRepositories.getRequestsRepository());
    }



    /**
     * Obtains the object that will make the result of the process matching permanent.
     *
     * @return the matched requests writer
     */
    public final ItemWriter<Request> getWriter() {
        final RepositoryItemWriter<Request> writer = new RepositoryItemWriter<>();
        writer.setRepository(this.applicationRepositories.getRequestsRepository());
        writer.setMethodName("save");

        return writer;
    }

}
