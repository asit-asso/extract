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
package ch.asit_asso.extract.web.model;

import java.util.HashMap;
import java.util.Map;
import ch.asit_asso.extract.connectors.common.IConnector;
import ch.asit_asso.extract.connectors.common.IConnectorImportResult;
import ch.asit_asso.extract.connectors.common.IExportRequest;
import ch.asit_asso.extract.connectors.common.IExportResult;



/**
 * A fake connector plugin to use in unit tests.
 *
 * @author Yves Grasset
 */
public class DummyConnectorPlugin implements IConnector {

    private final Map<String, String> parametersValuesMap;



    public DummyConnectorPlugin() {
        this.parametersValuesMap = new HashMap<>();
        this.parametersValuesMap.put("titi", "toto");
        this.parametersValuesMap.put("tata", "tutu");
    }



    private DummyConnectorPlugin(Map<String, String> parametersValues) {
        this.parametersValuesMap = parametersValues;
    }



    @Override
    public IConnector newInstance(String language) {
        return new DummyConnectorPlugin();
    }



    @Override
    public IConnector newInstance(String language, Map<String, String> inputs) {
        return new DummyConnectorPlugin(inputs);
    }



    @Override
    public String getCode() {
        return "DummyConnector";
    }



    @Override
    public String getDescription() {
        return "This a dummy connector plugin.";
    }



    @Override
    public String getHelp() {
        return "This dummy connector is meant to be used in unit tests.";
    }



    @Override
    public String getLabel() {
        return "Dummy connector plugin";
    }



    @Override
    public String getPicto() {
        return "fa-check";
    }



    @Override
    public String getParams() {
        return "["
                + "{\"code\" : \"url\", \"label\" : \"Service URL\", \"type\" : \"text\", \"req\" : \"true\", \"maxlength\" : 255},"
                + "{\"code\" : \"login\", \"label\" : \"User name\", \"type\" : \"text\", \"req\" : \"true\", \"maxlength\" : 50},"
                + "{\"code\" : \"password\", \"label\" : \"Password\", \"type\" : \"pass\", \"req\" : \"true\", \"maxlength\" : 50}"
                + "]";
    }



    @Override
    public IConnectorImportResult importCommands() {
        return null;
    }



    @Override
    public IExportResult exportResult(IExportRequest request) {
        return null;
    }

}
