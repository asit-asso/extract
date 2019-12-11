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
package org.easysdi.extract.web.controllers;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import org.easysdi.extract.connectors.ConnectorDiscovererWrapper;
import org.easysdi.extract.connectors.common.IConnector;
import org.easysdi.extract.domain.Connector;
import org.easysdi.extract.domain.Rule;
import org.easysdi.extract.persistence.ConnectorsRepository;
import org.easysdi.extract.persistence.ProcessesRepository;
import org.easysdi.extract.persistence.RequestsRepository;
import org.easysdi.extract.persistence.RulesRepository;
import org.easysdi.extract.web.Message.MessageType;
import org.easysdi.extract.web.model.ConnectorModel;
import org.easysdi.extract.web.model.ProcessModel;
import org.easysdi.extract.web.model.RuleModel;
import org.easysdi.extract.web.validators.ConnectorValidator;
import org.easysdi.extract.web.validators.PluginItemModelParameterValidator;
import org.easysdi.extract.web.validators.RuleValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;



/**
 * Processes the web requests related to the connectors management.
 *
 * @author Yves Grasset
 */
@Controller
@Scope("session")
@RequestMapping("/connectors")
public class ConnectorsController extends BaseController {

    /**
     * The string that identifies the part of the website that this controller manages.
     */
    private static final String CURRENT_SECTION_IDENTIFIER = "connectors";

    /**
     * The string that identifies the view to display the information of a connector.
     */
    private static final String DETAILS_VIEW = "connectors/details";

    /**
     * The string that identifies the view to display all the connectors.
     */
    private static final String LIST_VIEW = "connectors/list";

    /**
     * The string that tell this connector to redirect the user to the connectors list.
     */
    private static final String REDIRECT_TO_LIST = "redirect:/connectors";

    /**
     * The code of the language used to display the texts in the application.
     */
    @Value("${extract.i18n.language}")
    private String applicationLanguage;

    /**
     * Application log file writer.
     */
    private final Logger logger = LoggerFactory.getLogger(ConnectorsController.class);

    /**
     * The link between the connector data objects and the data source.
     */
    @Autowired
    private ConnectorsRepository connectorsRepository;

    /**
     * The link between the requests data objects and the data source.
     */
    @Autowired
    private RequestsRepository requestsRepository;

    /**
     * The link between the rule data objects and the data source.
     */
    @Autowired
    private RulesRepository rulesRepository;

    /**
     * The link between the process data objects and the data source.
     */
    @Autowired
    private ProcessesRepository processesRepository;

    /**
     * Access to the currently available connector plugins.
     */
    @Autowired
    private ConnectorDiscovererWrapper connectorDiscoveryWrapper;



    /**
     * Instantiates this controller.
     */
    @Autowired
    public ConnectorsController() {
        this.logger.debug("Instantiating the connectors controller.");
    }



    /**
     * Defines the object that will validate the connector models submitted to this controller.
     *
     * @param binder the object that makes a relation between web request parameters and Java beans.
     */
    @InitBinder("connector")
    public final void initBinder(final WebDataBinder binder) {
        binder.setValidator(new ConnectorValidator(new PluginItemModelParameterValidator(), new RuleValidator()));
    }



    /**
     * Processes the submission of data to create a new connector instance.
     *
     * @param connectorModel     the data for the new connector instance
     * @param bindingResult      the object that holds the validation information for the connector data
     * @param model              the collection of model object to be communicated to the next view
     * @param redirectAttributes The data that needs to be passed to the redirected page
     * @return the identifier of the next view to display
     */
    @PostMapping("add")
    public final String addItem(@Valid @ModelAttribute("connector") final ConnectorModel connectorModel,
            final BindingResult bindingResult, final ModelMap model, final RedirectAttributes redirectAttributes) {
        this.logger.debug("Processing the data to add a controller.");

        if (!this.isCurrentUserAdmin()) {
            return ConnectorsController.REDIRECT_TO_ACCESS_DENIED;
        }

        if (bindingResult.hasErrors()) {
            this.logger.info("The connector add failed.");
//            model.addAttribute("processes", this.getAllProcesses());
//            model.addAttribute("isNew", true);
//            this.addJavascriptMessagesAttribute(model);

            return this.prepareModelForDetailsView(model, true);
        }

        final Connector domainConnector = connectorModel.createDomainConnector();

        //save rules
        int position = 1;
        for (RuleModel ruleModel : connectorModel.getRules()) {
            Rule domainRule = this.rulesRepository.findOne(ruleModel.getId());
            org.easysdi.extract.domain.Process domainProcess = null;
            if (ruleModel.getProcessId() >= 0) {
                domainProcess = this.processesRepository.findOne(ruleModel.getProcessId());
            }
            if (domainRule != null) {
                ruleModel.updateDomainRule(domainRule, domainConnector, domainProcess);
                domainRule.setPosition(position);
            }
            position++;
        }
        this.connectorsRepository.save(domainConnector);

        this.addStatusMessage(redirectAttributes, "connectorsList.connector.added", MessageType.SUCCESS);
        return ConnectorsController.REDIRECT_TO_LIST;
    }



    /**
     * Processes the submission of data to update an existing connector instance.
     *
     * @param connectorModel     the updated data for the connector instance
     * @param bindingResult      the object holding the validation information for the updated data
     * @param model              the collection of model object to communicate to the next view
     * @param id                 the identifier of the connector to update
     * @param redirectAttributes The data that needs to be passed to the redirected page
     * @return the identifier of the next view to display
     */
    @PostMapping(value = "{id}")
    public final String updateItem(@Valid @ModelAttribute("connector") final ConnectorModel connectorModel,
            final BindingResult bindingResult, final ModelMap model, @PathVariable final int id,
            final RedirectAttributes redirectAttributes) {
        this.logger.debug("Processing the data to update a controller.");

        if (!this.isCurrentUserAdmin()) {
            return ConnectorsController.REDIRECT_TO_ACCESS_DENIED;
        }

        if (bindingResult.hasErrors()) {
            this.logger.info("The connector update failed.");

            return this.prepareModelForDetailsView(model, false);
        }

        this.logger.debug("Fetching the controller to update.");
        final Connector domainConnector = this.connectorsRepository.findOne(id);

        if (domainConnector == null) {
            this.logger.error("No connector found in database with identifier {}.", id);
            this.addStatusMessage(redirectAttributes, "connectorsList.connector.notFound",
                    MessageType.ERROR);

            return ConnectorsController.REDIRECT_TO_LIST;
        }

        connectorModel.updateDomainConnector(domainConnector);
        this.updateConnectorRules(connectorModel, domainConnector);

        this.logger.info("Updating the connector # {} has succeeded.", domainConnector.getId());
        this.connectorsRepository.save(domainConnector);
        this.addStatusMessage(redirectAttributes, "connectorsList.connector.updated", MessageType.SUCCESS);

        return ConnectorsController.REDIRECT_TO_LIST;
    }



    /**
     * Processes a request to delete a connector instance.
     *
     * @param id                 the identifier of the connector to delete
     * @param name               the name of the connector to delete
     * @param redirectAttributes The data that needs to be passed to the redirected page
     * @return the identifier of the next view to display
     */
    @PostMapping("delete")
    public final String deleteItem(@RequestParam final int id, @RequestParam final String name,
            final RedirectAttributes redirectAttributes) {
        this.logger.debug("Processing a request to delete the connector with id {} and called \"{}\".", id, name);

        if (!this.isCurrentUserAdmin()) {
            return ConnectorsController.REDIRECT_TO_ACCESS_DENIED;
        }

        final Connector domainConnector = this.connectorsRepository.findOne(id);

        if (domainConnector == null || !domainConnector.getName().equals(name)) {
            this.logger.warn("Connector with id {} and called \"{}\" not found. Nothing to delete.", id, name);

        } else {

            if (domainConnector.hasActiveRequests(this.requestsRepository)) {
                this.logger.warn("Cannot delete connector {} because it has at least an active request.",
                        domainConnector.getId());
                this.addStatusMessage(redirectAttributes, "connectorsList.connector.hasActiveRequests",
                        MessageType.ERROR);
                return ConnectorsController.REDIRECT_TO_LIST;
            }

            this.connectorsRepository.delete(domainConnector);
            this.addStatusMessage(redirectAttributes, "connectorsList.connector.deleted",
                    MessageType.SUCCESS);
        }

        return ConnectorsController.REDIRECT_TO_LIST;
    }



    /**
     * Processes a request to delete a connector rule.
     *
     * @param connectorModel the model representing the connector currently being edited
     * @param model          the collection of model objects to communicate to the next view
     * @param ruleId         the identifier of the rule to delete
     * @param connectorId    the identifier of the connector associated to the rule
     * @return the identifier of the next view to display
     */
    @PostMapping("{connectorId}/deleteRule/{ruleId}")
    public final String deleteRule(@ModelAttribute("connector") final ConnectorModel connectorModel,
            final ModelMap model, @PathVariable final int ruleId, @PathVariable final int connectorId) {
        this.logger.debug("Processing a request to delete the rule with id {}.", ruleId);

        if (!this.isCurrentUserAdmin()) {
            return ConnectorsController.REDIRECT_TO_ACCESS_DENIED;
        }

        connectorModel.removeRule(ruleId);

        return this.prepareModelForDetailsView(model, false, connectorModel);
    }



    /**
     * Processes a request to display the form for adding a new connector.
     *
     * @param model  The object that contains the data required to display the form
     * @param typeId The string identifying the type of connector to instantiate
     * @return The name of the view to display the form
     */
    @GetMapping("add")
    public final String viewAddForm(final ModelMap model, @RequestParam("type") final String typeId) {

        if (!this.isCurrentUserAdmin()) {
            return ConnectorsController.REDIRECT_TO_ACCESS_DENIED;
        }

        final ConnectorModel connectorModel = new ConnectorModel(this.connectorDiscoveryWrapper.getConnector(typeId));

        return this.prepareModelForDetailsView(model, true, connectorModel);
    }



    /**
     * Processes a request to display the configuration of one connector.
     *
     * @param model              The object that contains the data required to display the connector
     * @param id                 The identifier of the connector to display
     * @param redirectAttributes The data that needs to be passed to the redirected page
     * @return The name of the view to display the connector
     */
    @GetMapping("{id}")
    public final String viewItem(final ModelMap model, @PathVariable final int id,
            final RedirectAttributes redirectAttributes) {

        if (!this.isCurrentUserAdmin()) {
            return ConnectorsController.REDIRECT_TO_ACCESS_DENIED;
        }

        final ConnectorModel connectorModel = this.getConnectorModel(id, redirectAttributes);

        if (connectorModel == null) {
            return ConnectorsController.REDIRECT_TO_LIST;
        }

        return this.prepareModelForDetailsView(model, false, connectorModel);
    }



    /**
     * Processes a request to add a connector rule.
     *
     * @param connectorModel The model representing the connector that is currently being edited
     * @param model          The object that contains the data required to add the rule
     * @param connectorId    The identifier of the connector that the rule must be added to
     * @return The name of the next view
     */
    @PostMapping("{id}/addRule")
    public final String addRule(@ModelAttribute("connector") final ConnectorModel connectorModel,
            final ModelMap model, @PathVariable("id") final int connectorId) {

        if (!this.isCurrentUserAdmin()) {
            return ConnectorsController.REDIRECT_TO_ACCESS_DENIED;
        }

        final RuleModel ruleModel = new RuleModel();
        ruleModel.setTag(RuleModel.TAG_ADDED);
        connectorModel.addRule(ruleModel);

        return this.prepareModelForDetailsView(model, false, connectorModel);
    }



    /**
     * Processes a request to display the list of existing connectors.
     *
     * @param model The object that contains the data required to display the list
     * @return The name of the view to display the list
     */
    @GetMapping("")
    public final String viewList(final ModelMap model) {
        try {

            if (!this.isCurrentUserAdmin()) {
                return ConnectorsController.REDIRECT_TO_ACCESS_DENIED;
            }

            return this.prepareModelForListView(model);

        } catch (Exception exception) {
            this.logger.error("An error occurred when the connectors list was prepared.", exception);
            throw exception;
        }
    }



    /**
     * Fetches a list of connectors from the repository and returns a collection of business connector
     * objects that use a plugin that is still available.
     *
     * @return an array of existing connectors
     */
    private ConnectorModel[] getAllConnectors() {
        final List<ConnectorModel> connectorsList = new ArrayList<>();

        for (Connector domainConnector : this.connectorsRepository.findAll()) {
            final IConnector plugin = this.connectorDiscoveryWrapper.getConnector(domainConnector.getConnectorCode());

            if (plugin == null) {
                this.logger.warn("The plugin \"{}\" used by connector \"{}\" is not loaded. The connector will be"
                        + " ignored until this is solved.", domainConnector.getConnectorLabel(),
                        domainConnector.getName());
                continue;
            }

            this.logger.debug("The connector plugin \"{}\" used by connector \"{}\" has been found.",
                    plugin.getLabel(), domainConnector.getName());
            connectorsList.add(new ConnectorModel(plugin, domainConnector, this.requestsRepository));
        }

        this.logger.debug("{} connector instance{} loaded.", connectorsList.size(),
                (connectorsList.size() > 1) ? "s" : "");
        return connectorsList.toArray(new ConnectorModel[]{});
    }



    /**
     * Fetches a list of connectors from the repository and returns a collection of business connector
     * objects that use a plugin that is still available.
     *
     * @return an array of existing connectors
     */
    private ProcessModel[] getAllProcesses() {
        final List<ProcessModel> processesList = new ArrayList<>();

        for (org.easysdi.extract.domain.Process domainProcess : this.processesRepository.findAll()) {

            processesList.add(new ProcessModel(domainProcess.getId(), domainProcess.getName()));
        }

        return processesList.toArray(new ProcessModel[]{});
    }



    /**
     * Gets a model object to represent a given connector.
     *
     * @param connectorId        the number that uniquely identifies the connector to represent
     * @param redirectAttributes the data to pass if the request is redirected
     * @return the model for the connector, or <code>null</code> if an error has occurred
     */
    private ConnectorModel getConnectorModel(final int connectorId, final RedirectAttributes redirectAttributes) {
        final Connector domainConnector = this.connectorsRepository.findOne(connectorId);

        if (domainConnector == null) {
            this.logger.error("No connector found in database with identifier {}.", connectorId);
            this.addStatusMessage(redirectAttributes, "connectorsList.connector.notFound",
                    MessageType.ERROR);
            return null;
        }

        final String pluginCode = domainConnector.getConnectorCode();
        final IConnector connectorPlugin = this.connectorDiscoveryWrapper.getConnector(pluginCode);

        if (connectorPlugin == null) {
            this.logger.warn("The connector plugin {} used by connector {} is not available anymore.", pluginCode,
                    connectorId);
            this.addStatusMessage(redirectAttributes, "connectorsList.connector.pluginUnavailable",
                    MessageType.ERROR);
            return null;
        }

        return new ConnectorModel(connectorPlugin, domainConnector, this.requestsRepository);
    }



    /**
     * Defines the generic model attributes that ensure a proper display of the connector details view.
     * <b>Important:</b> This method does not set the attribute for the connector itself. This must be set separately,
     * if necessary.
     *
     * @param model the data to display in the view
     * @param isNew <code>true</code> if the details view shows a connector that is being created
     * @return the string that identifies the details view
     */
    private String prepareModelForDetailsView(final ModelMap model, final boolean isNew) {
        return this.prepareModelForDetailsView(model, isNew, null);
    }



    /**
     * Defines the generic model attributes that ensure a proper display of the connector details view.
     *
     * @param model          the data to display in the view
     * @param isNew          <code>true</code> if the details view shows a connector that is being created
     * @param connectorModel the model that represents the connector to display in the details view, or
     *                       <code>null</code> not to define any connector specifically (because it is set elsewhere,
     *                       for example)
     * @return the string that identifies the details view
     */
    private String prepareModelForDetailsView(final ModelMap model, final boolean isNew,
            final ConnectorModel connectorModel) {
        assert model != null : "The model must not be null.";

        model.addAttribute("processes", this.getAllProcesses());
        model.addAttribute("isNew", isNew);
        model.addAttribute("language", this.applicationLanguage);
        this.addCurrentSectionToModel(ConnectorsController.CURRENT_SECTION_IDENTIFIER, model);
        this.addJavascriptMessagesAttribute(model);

        if (connectorModel != null) {
            model.addAttribute("connector", connectorModel);
        }

        return ConnectorsController.DETAILS_VIEW;
    }



    /**
     * Defines the generic model attributes that ensure a proper display of the connectors list view.
     *
     * @param model the data to display in the view
     * @return the string that identifies the list view
     */
    private String prepareModelForListView(final ModelMap model) {
        assert model != null : "The model must not be null.";

        model.addAttribute("plugins", this.connectorDiscoveryWrapper.getConnectors().values());
        model.addAttribute("connectors", this.getAllConnectors());
        this.addJavascriptMessagesAttribute(model);
        this.addCurrentSectionToModel(ConnectorsController.CURRENT_SECTION_IDENTIFIER, model);

        return ConnectorsController.LIST_VIEW;
    }



    /**
     * Reports the modifications to the request-matching rules of a connector in the data source.
     *
     * @param connectorModel  the model representing the connector that has been edited
     * @param domainConnector the connector instance in the data source
     */
    private void updateConnectorRules(final ConnectorModel connectorModel, final Connector domainConnector) {
        final List<Rule> rulesToDelete = new ArrayList<>(domainConnector.getRulesCollection());

        for (RuleModel ruleModel : connectorModel.getRules()) {
            final org.easysdi.extract.domain.Process domainProcess
                    = this.processesRepository.findOne(ruleModel.getProcessId());
            Rule domainRule;

            if (RuleModel.TAG_ADDED.equals(ruleModel.getTag())) {
                domainRule = ruleModel.createDomainRule(domainConnector, domainProcess);
                domainRule.setPosition(ruleModel.getPosition());
            } else {
                domainRule = this.rulesRepository.findOne(ruleModel.getId());
                rulesToDelete.remove(domainRule);
                ruleModel.updateDomainRule(domainRule, domainConnector, domainProcess);
                domainRule.setPosition(ruleModel.getPosition());
            }

            this.rulesRepository.save(domainRule);
        }

        domainConnector.getRulesCollection().removeAll(rulesToDelete);
        this.rulesRepository.delete(rulesToDelete);
    }

}
