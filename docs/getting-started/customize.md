---
title: Customize
---

## Add a new language

You can set a new language for the Extract application user interface:

1. Create a new directory in ``WEB-INF\classes\static\lang``, named after the two-character code of the desired language (e.g. ``it``)
2. Copy the contents of a previously defined language directory (``messages.properties`` and ``messages.js`` files)
3. Translate all messages stored in these files

The date selector used on the home page can also be translated. Its language files are located in the ``WEB-INF\classes\static\lib\bootstrap-datepicker\dist\locales`` directory. If the desired language is not yet defined :

1. Copy an existing language file by changing the language code in its name
2. Translate all containing strings

To apply the new language, update the ``extract.i18n.language`` property in the ``WEB-INF\classes\application.properties`` file, then restart the Tomcat application (see [Application settings](../configure/#application-settings))


## Plugins
!!! Info
    The type of connector providing the requests and the tasks that can be included in a process are managed by plugins that can be added, removed or updated independently of the application itself.

#### Installing or updating a plugin

If a new connector type or a new task is available, simply place its JAR in the ``WEB-INF/classes/connectors`` (for connectors) or ``WEB-INF/classes/task_processors`` (for tasks) directory. When performing an upgrade, it is recommended to delete the JAR file of the previous version to prevent any potential conflicts.

After making changes to the plugins, the Tomcat Extract application must be restarted.

#### Development of a new connector plugin
!!! Info
    Documented sample code is available to help you create a new connector. You'll find it in the docs folder of the application repository : [https://github.com/asit-asso/extract/tree/master/doc/extract-connector-sample](https://github.com/asit-asso/extract/tree/master/doc/extract-connector-sample).

* The project must be structured as a Java module, requiring the inclusion of a ``module-info.java`` file that declares its dependencies
* The new connector project must define a dependency on the ``extract-interface`` project
* The connector's main class must implement the ``IConnector`` interface
* The connector must declare a constructor without parameters
* The value returned by the ``getCode`` method must be unique across all Extract connectors
* The ``getParams`` method must return the connector's parameters as a JSON-formatted array. If the connector doesn't accept parameters, return an empty array (``[]``).
* Static files used by the connector (such as property files, language files, etc.) should be placed in the ``resources/connectors/<plugin code>/`` subdirectory.
* A file named ``ch.asit_asso.extract.connectors.common.IConnector`` must be created in the ``resources/META-INF/services`` subdirectory. The file should contain the fully qualified name of the main class.

#### Development of a new task plugin
!!! Info
    Documented sample code is available to help you create a new connector. You'll find it in the docs folder of the application repository : [https://github.com/asit-asso/extract/tree/master/doc/extract-task-sample](https://github.com/asit-asso/extract/tree/master/doc/extract-task-sample).

## Basemap

## System emails


<br>
<br>
<br>
<br>
<br>