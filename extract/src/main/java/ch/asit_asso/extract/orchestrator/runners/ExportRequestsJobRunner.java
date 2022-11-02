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

import java.util.Arrays;
import ch.asit_asso.extract.batch.processor.ExportRequestProcessor;
import ch.asit_asso.extract.batch.reader.RequestByStatusReader;
import ch.asit_asso.extract.connectors.implementation.ConnectorDiscovererWrapper;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.Request.Status;
import ch.asit_asso.extract.email.EmailSettings;
import ch.asit_asso.extract.persistence.ApplicationRepositories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;



/**
 * Executes the process that will export the result of the requests whose processing is done.
 *
 * @author Yves Grasset
 */
public class ExportRequestsJobRunner /*extends JobRunner<Request, Request>*/ implements Runnable {

    /**
     * The locale of the language that the application displays messages in.
     */
    private final String applicationLangague;

    /**
     * The link between the connector data objects and the data source.
     */
    private final ApplicationRepositories applicationRepositories;

    /**
     * The access to all the available connector plugins.
     */
    private final ConnectorDiscovererWrapper connectorPluginDiscoverer;

    /**
     * The objects required to create and send an e-mail message.
     */
    private final EmailSettings emailSettings;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(ExportRequestsJobRunner.class);



    /**
     * *
     * Creates a new instance of this runner.
     *
     * @param repositories               an ensemble of objects that link the data objects of this application with the
     *                                   data source
     * @param connectorsPluginDiscoverer the object providing access to the available connector plugins
     * @param smtpSettings               the objects required to create and send an e-mail message
     * @param applicationLanguage        the locale code of the language used by the application to display messages
     */
    public ExportRequestsJobRunner(final EmailSettings smtpSettings, final ApplicationRepositories repositories,
            final ConnectorDiscovererWrapper connectorsPluginDiscoverer, final String applicationLanguage) {

        if (repositories == null) {
            throw new IllegalArgumentException("The application repositories object cannot be null.");
        }

        if (repositories.getConnectorsRepository() == null) {
            throw new IllegalStateException("The connectors repository cannot be null.");
        }

        if (repositories.getRequestsRepository() == null) {
            throw new IllegalStateException("The requests repository cannot be null.");
        }

        if (connectorsPluginDiscoverer == null) {
            throw new IllegalArgumentException("The connector plugins discoverer cannot be null.");
        }

        if (smtpSettings == null) {
            throw new IllegalArgumentException("The e-mail settings object cannot be null.");
        }

        if (applicationLanguage == null) {
            throw new IllegalArgumentException("The application language code cannot be null.");
        }

        this.applicationRepositories = repositories;
        this.connectorPluginDiscoverer = connectorsPluginDiscoverer;
        this.emailSettings = smtpSettings;
        this.applicationLangague = applicationLanguage;
    }



    /**
     * Executes the request export process.
     */
    @Override
    public final void run() {
        this.logger.debug("Performing request export job.");

        final ItemReader<Request> requestToExportReader = this.getReader();
        final ItemProcessor<Request, Request> requestExporter = this.getProcessor();
        final ItemWriter<Request> requestWriter = this.getWriter();

        try {
            Request requestToExport = requestToExportReader.read();

            while (requestToExport != null) {

                try {
                    Request exportedRequest = requestExporter.process(requestToExport);
                    requestWriter.write(Arrays.asList(exportedRequest));

                } catch (Exception exception) {
                    this.logger.error("Could not export request {}.", requestToExport.getId(), exception);
                }

                requestToExport = requestToExportReader.read();
            }

        } catch (Exception exception) {
            this.logger.error("Could not read all the requests to export.", exception);
        }
    }



    /**
     * Obtains the object that will fetch the requests that are ready to be exported.
     *
     * @return the exportable requests reader
     */
    public final RequestByStatusReader getReader() {
        return new RequestByStatusReader(Status.TOEXPORT, this.applicationRepositories.getRequestsRepository());
    }



    /**
     * Obtains the object that will export the requests result.
     *
     * @return the request result processor
     */
    public final ExportRequestProcessor getProcessor() {
        final String basePath = this.applicationRepositories.getParametersRepository().getBasePath();

        return new ExportRequestProcessor(this.applicationRepositories, this.connectorPluginDiscoverer, basePath,
                this.emailSettings, this.applicationLangague);
    }



    /**
     * Obtains the object that will make the result of the export permanent.
     *
     * @return the exported requests writer
     */
    public final ItemWriter<Request> getWriter() {
        RepositoryItemWriter<Request> writer = new RepositoryItemWriter<>();
        writer.setRepository(this.applicationRepositories.getRequestsRepository());
        writer.setMethodName("save");

        return writer;
    }

}
