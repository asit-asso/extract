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

Currently, the customization of emails sent by the application is somewhat limited. However, this section provides an overview of how the system emails work.

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

The message delivery follows the Model-View-Controller (MVC) architecture. The content displayed in the message depends on the data transmitted by Extract. Currently, only essential information is transmitted, meaning additional details about the request cannot be displayed. However, it is possible to delete specific information, as long as this complies with Thymeleaf standards (see link above), especially when a string requires a set number of parameters.

To support future internationalization of the application, all displayed strings, including emails, are stored in language-specific files. These files can be found in the ``WEB-INF\classes\static\lang\{language}\messages.properties`` file. The strings are defined using the following format:

```
string.id=Value text
```

Extended characters should be represented using the ``\u`` escape sequence, followed by the character's Unicode value in four hexadecimal digits. For instance, the character ``é`` would be represented as ``\u00e9``. On Windows, you can find the corresponding Unicode value using the character map tool.

Parameters within strings are specified by their index (starting from 0) enclosed in curly braces. For example, ``{0}`` refers to the first parameter.

**Warning** : Ensure that the number of parameters expected by the string matches the number of parameters provided. Otherwise, message delivery will fail.

To email templates, strings are specified using the ``th:text`` attribute. When the string does not require any parameters, use the following syntax:

```
th:text="#{string.id}"
```

When the string does require parameters, use the following syntax:

```
th:text="${#messages.msg('string.id', param0, param1,…)}"
```

The plain text in email templates serves only as a placeholder for previewing the template in an HTML viewer. When the email is generated, these placeholders are automatically replaced with the corresponding values from the language file.

<br>
<br>
<br>
<br>
<br>