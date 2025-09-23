## Development

### Packaging

To generate a WAR of the application, run the following command:

``` bash
mvn package
```

In addition to the WAR file, the delivery archive of a new version
contains:

-   The latest version of the database update script
    (`sql/update_db.sql`)
-   The FME sample script (`fme/`)

### Tests

Unit tests can be run independently of packaging with the command:

``` bash
mvn -q test -Punit-tests --batch-mode --fail-at-end
```

To run integration tests:

``` bash
mvn -q verify -Pintegration-tests --batch-mode
```

For functional tests (requires the application to be running on
localhost port 8080):

``` bash
mvn -q verify -Pfunctional-tests --batch-mode
```


## Application architecture

### Foreword

This document is the technical architecture file for the Extract project. It contains the structural elements of the project, such as the general architecture, data flows, interfaces, and data model. It is primarily intended for:

* The IT project manager
* The project architects
* The production team
* The maintenance team*

### General architecture

#### Basic logic diagram

The following logical diagram illustrates the principle of a configurable process.

![basic-logic-diagram](../assets/dev-guide/basic-logic-diagram.png){width="1000"}

1. The import is performed by the application for each declared and active connector.
2. For each request element, the application applies the dispatch rules that define which process should be launched.
3. Within each process, the pre-configured tasks are executed. Each process runs independently. The tasks in a process may include the following: adding a comment, validation, extraction, archiving.
4. The export is performed for each request element by the connector that imported it.

#### Software architecture diagram

The proposed solution is based on a modular and scalable architecture. The various functional components are illustrated in the diagram below.

![software-architecture-diagram](../assets/dev-guide/software-architecture-diagram.png){width="800"}

#### Flows description

The solution consists of a single server, so no specific flow needs to be set up other than HTTP(S) access to this server (port 80 or 443).

### Data description

#### Database schema

A single database and a single schema are created, and the user who owns the schema is used for all connections from the implemented solution (read/write).

#### Conceptual data model

![data-model](../assets/dev-guide/data-model.png){width="1000"}

#### Data tables description

##### CONNECTORS

Table listing the connectors configured in the system. It is not possible to modify/delete a connector if a related request is incomplete. Deleting a connector involves deleting the associated rules and setting the id_connector of the related requests in the REQUESTS table to null.

| ``Attribute`` | Type | Description | Example |
| --- | --- | --- | --- | 
| ``id_connector`` | int | **Primary key** | *1*
| ``connector_code`` | varchar 50 | Code identifying the connector type. Retrieved from the connector itself via a specific function. The code must be unique in the connector catalog. | *easysdiv4*
| ``connector_label`` | varchar 255 | Connector label. Retrieved from the connector itself via a specific function. | *EasySdi V4*
| ``connector_params`` | varchar 4000 | Connector-specific parameters in JSON format. The list of parameters and their types are retrieved from the connector itself via a specific function. | *cf 4.1*
| ``name`` | varchar 50 | Name given to the connector instance | *asitvd_dev*
| ``import_freq`` | int | Frequency of connector queries when active | *60*
| ``active`` | boolean | Defines whether the connector is active or not | *true*
| ``last_import_msg`` |varchar 4000 | Last message returned by the connector import | *Success*
| ``last_import_date`` | datetime | Date and time of the last import | *16/11/2016 21:15*

##### RULES

Table listing the rules applied to a given connector and associating a process with each of them.

| ``Attribute`` | Type | Description | Example |
| --- | --- | --- | --- | 
| ``id_rule`` | int | **Primary key** | *1*
| ``id_process`` | int | **Foreign key** linking to the PROCESSES table | *1*
| ``id_connector`` | int | **Foreign key** linking to the CONNECTORS table | *1*
| ``rule`` | varchar 4000 | Definition of the rule according to the system's specific syntax | *prod_code == ‘11’*
| ``active`` | boolean | Defines whether the rule is active or not | *true*
| ``position`` | int | Scheduling of rules for the same connector | *5*

##### PROCESSES

Table listing the processes configured in the system. It is not possible to modify (i.e., the tasks that comprise it) a process if a related request is ONGOING. It is not possible to delete a process if a related request is incomplete or if it is associated with a connector rule.

| ``Attribute`` | Type | Description | Example |
| --- | --- | --- | --- | 
| ``id_process`` | int | **Primary key** | *1*
| ``name`` | varchar 255 | Process title | *Réseau de Gaz*

##### TASKS

Table listing the tasks (plugins) configured for a given process.

| ``Attribute`` | Type | Description | Example |
| --- | --- | --- | --- | 
| ``id_task`` | int | **Primary key**| *1*
| ``id_process`` | int | **Foreign key** linking to the PROCESSES table | *1*
| ``task_code`` | varchar 50 | Code identifying the task plugin type. Retrieved from the plugin itself via a specific function. The code must be unique in the connector catalog | *FME2016*
| ``task_label`` | varchar 255 | Task plugin label. Retrieved from the plugin itself via a specific function | *Extraction FME 2016*
| ``task_params`` | varchar 4000 | Parameters specific to the task plugin in json format. The list of parameters and their type are retrieved from the plugin itself via a specific function | *cf 0*
| ``position`` | int | Scheduling of tasks between each other for the same process | *3*

##### PROCESSES_USERS

Table ensuring the assignment of one or more users to a given process.

| ``Attribute`` | Type | Description | Example |
| --- | --- | --- | --- | 
| ``id_process`` | int | **Foreign key** linking to the PROCESSES table | *1*
| ``id_user`` | int | **Foreign key** linking to the USERS table | *1*

##### PROCESSES_USERGROUPS

Table ensuring the assignment of one or more groups to a given process.

| ``Attribute`` | Type | Description | Example |
| --- | --- | --- | --- | 
| ``id_process`` | int | **Foreign key** linking to the PROCESSES table | *1*
| ``id_usergroup`` | int | **Foreign key** linking to the USERGROUPS table | *1*

##### USERS

User table. It is not possible to delete a user if they are associated with a process. When a user is deleted, the items linked to them in the REQUEST_HISTORY table are assigned an ``id_user`` value of ``null``, indicating that the user is now unknown.

| ``Attribute`` | Type | Description | Example |
| --- | --- | --- | --- | 
| ``id_user`` | int | **Primary key** linking to the PROCESSES table | *1*
| ``user_type`` | string | User type: LOCAL: user whose Extract database is the authentication source. LDAP: user whose LDAP server is the authentication source | *LOCAL*
| ``two_factor_status`` | string | 2FA status: ACTIVE / INACTIVE / STANDBY | *ACTIVE*
| ``two_factor_forced`` | varchar 100 | 2FA imposed by the administrator ? | *true*
| ``two_factor_token`` | varchar 100 | String used to generate two-factor authentication codes (encrypted) | *Xxxxx*
| ``two_factor_standby_token`` | string | String used to generate two-factor authentication codes (encrypted) awaiting validation by the user | *Xxxxx*
| ``profile`` | string | Attribute that can take two values. ADMIN: Administrator. OPERATOR: Operator. Note: the texts corresponding to the values are managed in the application itself to ensure multilingual support. | *ADMIN*
| ``name`` | varchar 50 | Full name of the user | *Yves Blatti*
| ``login`` | varchar 50 | User login | *yblatti*
| ``pass`` | varchar 60 | User password | *******
| ``email`` | varchar 50 | User email | *yves.blatti@asitvd.ch*
| ``active`` | boolean | Defines whether the user is active or not | *true*
| ``mailactive`` | boolean | Defines whether notifications are active for the user | *true*
| ``tokenpass`` | varchar 50 | Token used for password recovery (single use). Attribute reset to null when the user next logs in (regardless of the password used) | *so37dd9sxwxdx3449ckl*
| ``tokenexpire`` | datetime | **Foreign key** linking to the USERGROUPS table. Token expiration date/time | *2016.01.01 12 :00*