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
package ch.asit_asso.extract.unit.web.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import ch.asit_asso.extract.connectors.common.IConnector;
import ch.asit_asso.extract.domain.Connector;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.Rule;
import ch.asit_asso.extract.web.model.ConnectorModel;
import ch.asit_asso.extract.web.model.RuleModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Yves Grasset
 */
@Tag("unit")
public class ConnectorModelTest {
    final static String DEFAULT_IMPORT_MESSAGE = "Dummy connector, could not import";

    final static Calendar DEFAULT_UPDATE_DATE = new GregorianCalendar(2015, Calendar.MAY, 20, 16, 53, 25);

    final static int DUMMY_CONNECTOR_INSTANCE_ID = 5;

    final static int DUMMY_RULE_INSTANCE_ID = 78;

    Connector dummyConnectorInstance;
    IConnector dummyConnectorPlugin;
    RuleModel dummyRuleModel;



    @BeforeEach
    public void init() {
        this.dummyConnectorPlugin = new DummyConnectorPlugin();

        this.dummyConnectorInstance = new Connector();
        this.dummyConnectorInstance.setActive(Boolean.TRUE);
        this.dummyConnectorInstance.setConnectorCode(this.dummyConnectorPlugin.getCode());
        this.dummyConnectorInstance.setConnectorLabel(this.dummyConnectorPlugin.getLabel());
        this.dummyConnectorInstance.setId(ConnectorModelTest.DUMMY_CONNECTOR_INSTANCE_ID);
        this.dummyConnectorInstance.setImportFrequency(120);
        this.dummyConnectorInstance.setLastImportDate(ConnectorModelTest.DEFAULT_UPDATE_DATE);
        this.dummyConnectorInstance.setLastImportMessage(ConnectorModelTest.DEFAULT_IMPORT_MESSAGE);
        this.dummyConnectorInstance.setName("Test connector model");
        this.dummyConnectorInstance.setMaximumRetries(3);
        this.dummyConnectorInstance.setRequestsCollection(new ArrayList<>());

        HashMap<String, String> parametersValues = new HashMap<>();
        parametersValues.put("url", "http://titi.toto.ta");
        parametersValues.put("login", "myUser");
        parametersValues.put("password", "mYSuPeR5eCr3tPa2sW0Rd");
        this.dummyConnectorInstance.setConnectorParametersValues(parametersValues);

        Rule dummyRule = new Rule();
        dummyRule.setId(ConnectorModelTest.DUMMY_RULE_INSTANCE_ID);
        dummyRule.setActive(Boolean.TRUE);
        dummyRule.setConnector(this.dummyConnectorInstance);
        dummyRule.setPosition(1);
        dummyRule.setProcess(new ch.asit_asso.extract.domain.Process(25));
        dummyRule.setRule("product == \"SHP\"");

        this.dummyConnectorInstance.setRulesCollection(List.of(dummyRule));

        this.dummyRuleModel = new RuleModel();
        this.dummyRuleModel.setActive(true);
        this.dummyRuleModel.setId(ConnectorModelTest.DUMMY_CONNECTOR_INSTANCE_ID);
        this.dummyRuleModel.setProcessId(8);
        this.dummyRuleModel.setPosition(2);
        this.dummyRuleModel.setProcessName("Traitement test");
        this.dummyRuleModel.setRule("id > 0");
    }



    /**
     * Test of hasActiveRequests method, of class ConnectorModel.
     */
    @Test
    public void shouldDetectActiveRequests() {
        System.out.println("hasActiveRequests");
        Request request1 = new Request();
        request1.setStatus(Request.Status.FINISHED);
        request1.setConnector(this.dummyConnectorInstance);

        Request request2 = new Request();
        request2.setStatus(Request.Status.STANDBY);
        request2.setConnector(this.dummyConnectorInstance);

        this.dummyConnectorInstance.setRequestsCollection(Arrays.asList(request1, request2));
        ConnectorModel instance = new ConnectorModel(this.dummyConnectorPlugin, this.dummyConnectorInstance, null);
        assertTrue(instance.hasActiveRequests());
    }



    /**
     * Test of createDomainConnector method, of class ConnectorModel.
     */
    @Test
    public void shouldCreateDomainConnector() {
        System.out.println("createDomainConnector");

        ConnectorModel instance = new ConnectorModel(this.dummyConnectorPlugin);
        instance.setActive(this.dummyConnectorInstance.isActive());
        instance.setId(this.dummyConnectorInstance.getId());
        instance.setImportFrequency(this.dummyConnectorInstance.getImportFrequency());
        instance.setLastImportDate(this.dummyConnectorInstance.getLastImportDate());
        instance.setLastImportMessage(this.dummyConnectorInstance.getLastImportMessage());
        instance.setName(this.dummyConnectorInstance.getName());
        instance.setMaximumRetries(this.dummyConnectorInstance.getMaximumRetries());
        Connector createdInstance = instance.createDomainConnector();

        assertEquals(this.dummyConnectorInstance.isActive(), createdInstance.isActive());
        assertEquals(this.dummyConnectorPlugin.getCode(), createdInstance.getConnectorCode());
        assertEquals(this.dummyConnectorPlugin.getLabel(), createdInstance.getConnectorLabel());
        assertNull(createdInstance.getId());
        assertEquals(this.dummyConnectorInstance.getImportFrequency(), createdInstance.getImportFrequency());
        assertEquals(this.dummyConnectorInstance.getMaximumRetries(), createdInstance.getMaximumRetries());
        assertNull(createdInstance.getLastImportDate());
        assertNull(createdInstance.getLastImportMessage());
        assertEquals(this.dummyConnectorInstance.getName(), createdInstance.getName());
    }



    /**
     * Test of updateDomainConnector method, of class ConnectorModel.
     */
    @Test
    public void shouldUpdateDomainConnector() {
        System.out.println("updateDomainConnector");
        Calendar newImportDate = new GregorianCalendar(2017, Calendar.FEBRUARY, 25, 12, 33, 54);
        ConnectorModel instance = new ConnectorModel(this.dummyConnectorPlugin, this.dummyConnectorInstance, null);
        instance.setActive(false);
        instance.setId(8);
        instance.setImportFrequency(360);
        instance.setMaximumRetries(2);
        instance.setLastImportDate(newImportDate);
        instance.setLastImportMessage("OK");
        instance.setName("New test connector");
        instance.updateDomainConnector(this.dummyConnectorInstance);
        assertEquals(false, this.dummyConnectorInstance.isActive());
        assertEquals(ConnectorModelTest.DUMMY_CONNECTOR_INSTANCE_ID, (long) this.dummyConnectorInstance.getId());
        assertEquals("New test connector", this.dummyConnectorInstance.getName());

        // The following properties should not be updated even if they have been changed in the model
        assertEquals(this.dummyConnectorPlugin.getCode(), this.dummyConnectorInstance.getConnectorCode());
        assertEquals(this.dummyConnectorPlugin.getLabel(), this.dummyConnectorInstance.getConnectorLabel());
        assertEquals(ConnectorModelTest.DUMMY_CONNECTOR_INSTANCE_ID,
                (long) this.dummyConnectorInstance.getId());
        assertEquals(ConnectorModelTest.DEFAULT_UPDATE_DATE, this.dummyConnectorInstance.getLastImportDate());
        assertEquals(ConnectorModelTest.DEFAULT_IMPORT_MESSAGE, this.dummyConnectorInstance.getLastImportMessage());

    }



    /**
     * Test of addRule method, of class ConnectorModel.
     */
    @Test
    public void shouldAddRule() {
        System.out.println("addRule");
        ConnectorModel instance = new ConnectorModel(this.dummyConnectorPlugin, this.dummyConnectorInstance, null);
        instance.addRule(this.dummyRuleModel);
        assertEquals(instance.getRules().length, this.dummyConnectorInstance.getRulesCollection().size() + 1);
        RuleModel addedRule = instance.getRules()[instance.getRules().length - 1];
        assertEquals(ConnectorModelTest.DUMMY_CONNECTOR_INSTANCE_ID, addedRule.getId());
        assertEquals(this.dummyRuleModel.getProcessId(), addedRule.getProcessId());
        assertEquals(this.dummyRuleModel.getPosition(), addedRule.getPosition());
        assertEquals(this.dummyRuleModel.getProcessName(), addedRule.getProcessName());
        assertEquals(this.dummyRuleModel.getRule(), addedRule.getRule());
    }



    /**
     * Test of removeRule method, of class ConnectorModel.
     */
    @Test
    public void shouldRemoveRuleById() {
        System.out.println("removeRule");
        ConnectorModel instance = new ConnectorModel(this.dummyConnectorPlugin, this.dummyConnectorInstance, null);
        instance.removeRule(ConnectorModelTest.DUMMY_RULE_INSTANCE_ID);
        assertEquals(instance.getRules().length, this.dummyConnectorInstance.getRulesCollection().size() - 1);
        assertNull(instance.getRuleById(ConnectorModelTest.DUMMY_RULE_INSTANCE_ID));
    }



    /**
     * Test of removeRule method, of class ConnectorModel.
     */
    @Test
    public void shouldRemoveRuleByModel() {
        System.out.println("removeRule");
        ConnectorModel instance = new ConnectorModel(this.dummyConnectorPlugin, this.dummyConnectorInstance, null);
        instance.addRule(this.dummyRuleModel);
        instance.removeRule(this.dummyRuleModel);
        assertEquals(instance.getRules().length, this.dummyConnectorInstance.getRulesCollection().size());
        assertNull(instance.getRuleById(this.dummyRuleModel.getId()));
    }
}
