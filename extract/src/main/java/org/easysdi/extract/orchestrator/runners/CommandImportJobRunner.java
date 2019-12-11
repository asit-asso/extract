/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easysdi.extract.orchestrator.runners;

import java.util.Arrays;
import org.easysdi.extract.batch.processor.ProductsProcessor;
import org.easysdi.extract.batch.reader.ConnectorImportReader;
import org.easysdi.extract.batch.writer.ImportedRequestsWriter;
import org.easysdi.extract.connectors.common.IConnector;
import org.easysdi.extract.connectors.common.IProduct;
import org.easysdi.extract.domain.Request;
import org.easysdi.extract.email.EmailSettings;
import org.easysdi.extract.persistence.ApplicationRepositories;
import org.easysdi.extract.persistence.ConnectorsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;



/**
 * Job launcher for order imports through a connector plugin.
 *
 * @author Yves Grasset
 */
@EnableBatchProcessing
public class CommandImportJobRunner /*extends JobRunner<Product, Request>*/ implements Runnable {

    /**
     * The Spring Data objects that link the data objects with the data source.
     */
    private final ApplicationRepositories applicationRepositories;

    /**
     * the number that identifies the instance containing the connector parameters.
     */
    private final int connectorId;

    /**
     * The instance of the connector plugin to use to import orders.
     */
    private final IConnector connectorPluginInstance;

    /**
     * The objects required to create and send an e-mail message.
     */
    private final EmailSettings emailSettings;

    /**
     * The locale of the language that the application displays messages in.
     */
    private final String language;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(CommandImportJobRunner.class);



    /**
     * Creates a new instance of this launcher.
     *
     * @param connectorIdentifier the number that identifies the instance containing the connector parameters
     * @param connectorPlugin     the connector plugin
     * @param repositories        an ensemble of objects linking the data objects with the database
     * @param smtpSettings        the objects required to create and send an email message
     * @param applicationLanguage the locale code of the language used by the application to display messages
     */
    public CommandImportJobRunner(final int connectorIdentifier, final IConnector connectorPlugin,
            final ApplicationRepositories repositories, final EmailSettings smtpSettings,
            final String applicationLanguage) {

        if (connectorIdentifier < 1) {
            throw new IllegalArgumentException("The connector identifier must be greater than 0.");
        }

        if (connectorPlugin == null) {
            throw new IllegalArgumentException("The connector plugin instance cannot be null.");
        }

        if (repositories == null) {
            throw new IllegalArgumentException("The application repositories object cannot be null.");
        }

        if (smtpSettings == null) {
            throw new IllegalArgumentException("The e-mail settings object cannot be null.");
        }

        if (applicationLanguage == null) {
            throw new IllegalArgumentException("The application language code cannot be null.");
        }

        this.connectorId = connectorIdentifier;
        this.connectorPluginInstance = connectorPlugin;
        this.applicationRepositories = repositories;
        this.emailSettings = smtpSettings;
        this.language = applicationLanguage;
    }



    /**
     * Executes a command import batch process.
     */
    @Override
    public final void run() {
        this.logger.debug("Running import job for connector with identifier {}.", this.connectorId);

        final ItemReader<IProduct> productReader = this.getReader();
        final ItemProcessor<IProduct, Request> productProcessor = this.getProcessor();
        final ItemWriter<Request> requestWriter = this.getWriter();

        try {

            IProduct product = productReader.read();

            while (product != null) {

                try {
                    Request importedRequest = productProcessor.process(product);
                    requestWriter.write(Arrays.asList(importedRequest));

                } catch (Exception exception) {
                    this.logger.error("Could not process the product \"{} - {}\".", product.getOrderLabel(),
                            product.getProductLabel(), exception);
                }

                product = productReader.read();
            }

        } catch (Exception exception) {
            this.logger.error("Could not read all the products for connector with identifier {}.",
                    this.connectorId, exception);
        }
    }



    /**
     * Obtains the object that will return the products contained in the imported commands.
     *
     * @return the products reader
     */
    public final ConnectorImportReader getReader() {
        return new ConnectorImportReader(this.connectorId, this.connectorPluginInstance,
                this.applicationRepositories.getConnectorsRepository(),
                this.applicationRepositories.getUsersRepository(), this.emailSettings, this.language);
    }



    /**
     * Obtains the object that will transform a product in a request.
     *
     * @return the product processor
     */
    public final ProductsProcessor getProcessor() {
        ConnectorsRepository repository = this.applicationRepositories.getConnectorsRepository();
        return new ProductsProcessor(repository.findOne(this.connectorId));
    }



    /**
     * Obtains the object that will make the new requests permanent.
     *
     * @return the request writer
     */
    public final ImportedRequestsWriter getWriter() {
        ImportedRequestsWriter writer
                = new ImportedRequestsWriter(this.connectorId, this.emailSettings, this.applicationRepositories);

        return writer;
    }

}
