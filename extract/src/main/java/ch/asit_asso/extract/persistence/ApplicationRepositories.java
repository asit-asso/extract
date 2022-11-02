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
package ch.asit_asso.extract.persistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



/**
 * A grouping of all the links between the various data objects and the data source.
 *
 * @author Yves Grasset
 */
@Component
public class ApplicationRepositories {

    /**
     * The link between connector data objects and the data source.
     */
    @Autowired
    private ConnectorsRepository connectorsRepository;

    /**
     * The link between process data objects and the data source.
     */
    @Autowired
    private ProcessesRepository processesRepository;

    /**
     * The link between request history record data objects and the data source.
     */
    @Autowired
    private RequestHistoryRepository requestHistoryRepository;

    /**
     * The link between request data objects and the data source.
     */
    @Autowired
    private RequestsRepository requestsRepository;

    /**
     * The link between rule data objects and the data source.
     */
    @Autowired
    private RulesRepository rulesRepository;

    /**
     * The link between application setting data objects and the data source.
     */
    @Autowired
    private SystemParametersRepository parametersRepository;

    /**
     * The link between task data objects and the data source.
     */
    @Autowired
    private TasksRepository tasksRepository;

    /**
     * The link between application user data objects and the data source.
     */
    @Autowired
    private UsersRepository usersRepository;



    /**
     * Obtains the link between the connector data objects and the data source.
     *
     * @return the connectors repository
     */
    public final ConnectorsRepository getConnectorsRepository() {
        return this.connectorsRepository;
    }



    /**
     * Obtains the link between the process data objects and the data source.
     *
     * @return the processes repository
     */
    public final ProcessesRepository getProcessesRepository() {
        return this.processesRepository;
    }



    /**
     * Obtains the link between the request history record data objects and the data source.
     *
     * @return the request history records repository
     */
    public final RequestHistoryRepository getRequestHistoryRepository() {
        return this.requestHistoryRepository;
    }



    /**
     * Obtains the link between the request data objects and the data source.
     *
     * @return the requests repository
     */
    public final RequestsRepository getRequestsRepository() {
        return this.requestsRepository;
    }



    /**
     * Obtains the link between the rule data objects and the data source.
     *
     * @return the rules repository
     */
    public final RulesRepository getRulesRepository() {
        return this.rulesRepository;
    }



    /**
     * Obtains the link between the application setting data objects and the data source.
     *
     * @return the application settings repository
     */
    public final SystemParametersRepository getParametersRepository() {
        return this.parametersRepository;
    }



    /**
     * Obtains the link between the task data objects and the data source.
     *
     * @return the tasks repository
     */
    public final TasksRepository getTasksRepository() {
        return this.tasksRepository;
    }



    /**
     * Obtains the link between the user data objects and the data source.
     *
     * @return the users repository
     */
    public final UsersRepository getUsersRepository() {
        return this.usersRepository;
    }

}
