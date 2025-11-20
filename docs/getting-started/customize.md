---
title: Customize
---

## Add a new language

### For the main module

You can set a new language for the Extract application user interface. If you want to translate the main module of Extract, do the following :

1. Create a new `messages_{lang-code}.properties` files under `WEB-INF\classes` named after the two-character ISO code of the desired language (e.g. ``messages_it.properties``)
2. Create a new directory in ``WEB-INF\classes\static\lang``, named after the two-character ISO code of the desired language (e.g. ``it``)
2. Copy the contents of a previously defined language directory (``messages.js`` and any `html` files)
3. Translate all messages stored in these files

The date selector used on the home page can also be translated. Its language files are located in the ``WEB-INF\classes\static\lib\bootstrap-datepicker\dist\locales`` directory. If the desired language is not yet defined :

1. Copy an existing language file by changing the language code in its name
2. Translate all containing strings

To apply the new language, update the ``extract.i18n.language`` property in the ``WEB-INF\classes\application.properties`` file, then restart the Tomcat application (see [Application settings](../configure/#application-settings))

### For plugins

If you want to translate connector and extraction plugin as well, the easiest is to work on the code base of Extract and re-package the application :

1. In all plugins, create a new directory under `{plugin-name/src/main/resources/plugins/lang}`, named after the two-character ISO code of the desired language (e.g. ``it``)
2. Copy the contents of a previously defined language directory (`messages.properties`, ``messages.js`` and any `html` files)
3. Translate all messages stored in these files
4. Re-package the application `mvn clean package "-Dmaven.test.skip"` and re-deploy it on tomcat

### Fallback language

* For all `messages.properties` and `messages.js` files, there is a fallback mechanism that if a key is missing in a localization file, it tries first to find it in the default language (the first one specified in the `extract.i18n.language` property), than in french.
* For all `*Help.html` files, there is no fallback mechanism. If the file is missing, the tooltip help will be empty.

## Plugins
!!! Info
    The type of connector providing the requests and the tasks that can be included in a process are managed by plugins that can be added, removed or updated independently of the application itself.

### Installing or updating a plugin

If a new connector type or a new task is available, simply place its JAR in the ``WEB-INF/classes/connectors`` (for connectors) or ``WEB-INF/classes/task_processors`` (for tasks) directory. When performing an upgrade, it is recommended to delete the JAR file of the previous version to prevent any potential conflicts.

After making changes to the plugins, the Tomcat Extract application must be restarted.

### Development of a new connector plugin
!!! Info
    Documented sample code is available to help you create a new connector. You'll find it in the docs folder of the application repository : [https://github.com/benoitregamey/extract/tree/new-documentation/docs/extract-connector-sample](https://github.com/benoitregamey/extract/tree/new-documentation/docs/extract-connector-sample){target="_blank"}.

* The project must be structured as a Java module, requiring the inclusion of a ``module-info.java`` file that declares its dependencies
* The new connector project must define a dependency on the ``extract-interface`` project
* The connector's main class must implement the ``IConnector`` interface
* The connector must declare a constructor without parameters
* The value returned by the ``getCode`` method must be unique across all Extract connectors
* The ``getParams`` method must return the connector's parameters as a JSON-formatted array. If the connector doesn't accept parameters, return an empty array (``[]``).
* Static files used by the connector (such as property files, language files, etc.) should be placed in the ``resources/connectors/<plugin code>/`` subdirectory.
* A file named ``ch.asit_asso.extract.connectors.common.IConnector`` must be created in the ``resources/META-INF/services`` subdirectory. The file should contain the fully qualified name of the main class.

### Development of a new task plugin
!!! Info
    Documented sample code is available to help you create a new connector. You'll find it in the docs folder of the application repository : [https://github.com/benoitregamey/extract/tree/new-documentation/docs/extract-task-sample](https://github.com/benoitregamey/extract/tree/new-documentation/docs/extract-task-sample){target="_blank"}.

* The project must be structured as a Java module, requiring the inclusion of a ``module-info.java`` file that declares its dependencies
* The new task project must define a dependency on the ``extract-interface`` project
* The task's main class must implement the ``ITaskProcessor`` interface
* The task plugin must declare a constructor without parameters
* The value returned by the ``getCode`` method must be unique across all Extract tasks
* The ``getParams`` method must return the plugin parameters as a JSON-formatted array. If the plugin doesn't accept parameters, return an empty array (``[]``).
* The ``getPicto`` method must return the name of the [FontAwesome](https://fontawesome.com/icons){target="_blank"} icon to be displayed in the plugin's title bar
* Static files used by the plugin (such as property files, language files, etc.) should be placed in the ``resources/plugin/<plugin code>/`` subdirectory.
* A file named ``ch.asit_asso.extract.plugins.common.ITaskProcessor`` must be created in the ``resources/META-INF/services`` subdirectory. The file should contain the fully qualified name of the plugin main class. If this requirement is not met, the plugin will not be detected by Extract.
* The task plugin is allowed to modify specific attributes (see below) of the original request when returning a result :

    | Attribute   | Purpose                              |
    | ----------- | ------------------------------------ |
    | `rejected`  | Indicates whether Extract should process the request. If set to `true`, the processing is interrupted and the request is immediately exported back to the origin server without any result. In this case, the ``remark`` attribute must be provided to explain the reason for the interruption. |
    | `remark`       | An explanatory note included with the request to inform the requester about how their data is being processed. It's recommended to append this note to the original attribute content to preserve all existing information. |

Changes to any other request attributes will be ignored.

## Basemap

To customize the map layers that display the order perimeter within the request details view, create a configuration file named ``map.custom.js`` and place it under the ``WEB-INF/classes/static/js/requestMap`` directory.

This directory includes a ``map.custom.js.dist`` file that provides an example configuration, which you can use as a template to create your own setup.

The following constraints apply :

* The ``map.custom.js`` file defines an ``initializeMap`` function, which returns a ``Promise`` that resolves with the initialized map instance.
* The map's ``target`` property must be set to the string ``"orderMap"``.
* Each map layer must include a ``title`` attribute in its ``options``. This title will be used as a label in the layer list widget.
* Any layer representing a base map must include a ``type`` attribute in its ``options`` with the value ``"base"``

## System emails

Emails are generated using HTML templates with the Thymeleaf 3.1 engine (learn more at [Thymeleaf Documentation](https://www.thymeleaf.org/doc/tutorials/3.1/usingthymeleaf.html){target="_blank"}). These templates can be found in the following directory: ``WEB-INF\classes\templates\email\html``. The relevant files are listed below:

| Path   | Usage                              |
| ----------- | ------------------------------------ |
| `layout\master`  | General layout used by all system emails |
| `exportFailed.html` | Request's export failed |
| `importFailed.html`  | Request's import failed  |
| `invalidProductImported.html` | An imported request includes a product that cannot be processed |
| `passwordReset.html`  | Send password reset code |
| `taskFailed.html` | Request processing interrupted due to an error |
| `taskStandby.html`  | Request processing awaits an operator validation |
| `taskStandbyNotification.html` | Remind that a request is still awaiting validation |
| `unmatchedRequest.html` | An imported request does not met any rule defined for the connector |

The message delivery follows the Model-View-Controller (MVC) architecture. The content displayed in the message depends on the data transmitted by Extract..

To support internationalization of the application, all displayed strings, including emails, are stored in language-specific files. These files can be found in the ``WEB-INF\classes\messages_{language}.properties`` file. The strings are defined using the following format:

```
string.id=Value text
```

Parameters within strings are specified by their index (starting from 0) enclosed in curly braces. For example, ``{0}`` refers to the first parameter.

**Warning** : Ensure that the number of parameters expected by the string matches the number of parameters provided. Otherwise, message delivery will fail.

In email templates, strings are specified using the ``th:text`` attribute. When the string does not require any parameters, use the following syntax:

```
th:text="#{string.id}"
```

When the string does require parameters, use the following syntax:

```
th:text="${#messages.msg('string.id', param0, param1,…)}"
```

The plain text in email templates serves only as a placeholder for previewing the template in an HTML viewer. When the email is generated, these placeholders are automatically replaced with the corresponding values from the language file.

Following parameters can be used (provided that this information is available when sending the email) :

* `orderLable` : request's label
* `productLabel` : Product name
* `startDate` : Submission date (e.g., “20 nov. 2025, 10:24:49”)
* `startDateISO` : Submission date ISO (e.g., “2025-11-20T09:24:49.221Z”)
* `endDate` : End date (e.g., “20 nov. 2025, 10:24:49”)
* `endDateISO` : End date ISO (e.g., “20 nov. 2025, 10:24:49”)
* `status` : Request status
* `rejected` : Boolean, true if the request was rejected
* `client` or `clientName` : Client name
* `clientGuid` : Unique client identifier
* `organism` or `organisationName` : Client organization name
* `organismGuid` : Unique organization identifier
* `tiers` : Third party organization name
* `perimeter` : Geographic perimeter (WKT format)
* `surface` : Area of the order

The dynamic parameter can be accessed as follow :

`parameter.KEY_NAME`, e.g. `parameter.FORMAT` for the format if exists.

Parameter can also be used outside of a localized string using the following syntax :

```html
<p th:text="${clientName}"></p>
```

If a parameter does not exist, the value is replaced by an empty string. If a dynamic parameter does not exist, the value is replaced by `null`.


<br>
<br>
<br>
<br>
<br>