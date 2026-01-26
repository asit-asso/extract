---
search:
  exclude: true
---

# Extract - Initialize a New Connector Plugin

## Introduction

This Java module is an example of a connector plugin that can be used to initialize a new plugin for the Extract project.  
The source code is documented and provides the necessary guidance to develop a new plugin.  
It can be imported into a new Java environment, but adaptations may be required depending on the expected behavior.

## Prerequisites for Use
* 64-bit OS  
* Java 17  
* Tomcat 9  

## Prerequisites for Development and Compilation
* Java 17  
* [Yarn][Yarn_Site]  
* Extract-interface project (Common interface for using connector and task plugins)  

## Initializing the New Connector Plugin
The project must be a Java module.  
The new plugin project must define a dependency on the extract-interface project.  
Required dependencies are defined in the pom.xml file.  

To initialize a new plugin, follow the steps in order:  
1. Copy the extract-connector-sample plugin code into a Java workspace. The system will prompt you to define a new name. If it does not, use the context menu (right-click) `Refactor > Rename`.  

2. Edit the **pom.xml** file of the module, replace occurrences of `extrat-connector-sample` with the new plugin name.  
   After right-clicking the file, choose `Add as Maven Project`.  

3. Right-click the namespace `ch.asit_asso.extract.connectors.sample`, choose the menu `Refactor > Rename`.  
   Enter the new class name to identify the plugin. If prompted, click the `Add in current Module` button to apply the changes only to the module.  
   This will automatically update the package name in all files of the module.  

4. Right-click the **SampleConnector.java** file, choose `Refactor > Rename`, then enter the new main class name of the plugin (e.g., EasySDIv4). This will rename the file and update all references to this class wherever it is used.  

5. Edit the **LocalizedMessages.java** file: adjust the value of the LOCALIZED_FILE_PATH_FORMAT parameter, which corresponds to the path to the `lang` directory.  

6. Check the **module-info.java** file, especially the reference to `SampleConnector`. Also verify line 4 of this file (reference to the namespace `ch.asit_asso.extract.connectors.sample`).  

7. Check the file **resources\META-INF\services\ch.asit_asso.extract.connectors.common.IConnector**, especially the reference to the class `ch.asit_asso.extract.connectors.sample.SampleConnector`.  

8. Right-click the **resources\connectors\sample** folder, choose `Refactor > Rename`, and enter the new name.  

9. Edit the file **resources\connectors\<connector>\lang\fr\messages.properties**. Modify or add the labels used by the plugin source code. This step can be done progressively during development.  

10. Edit the file **resources\connectors\<connector>\properties\config.properties**. This file contains the plugin configuration parameters used by the source code. This step can be done progressively during development.  

11. The folder **resources\connectors\<connector>\templates** contains XML templates used by the connector.  
    However, they are specific to the EasySDIv4 connector, since XML is used as the exchange format for commands. These files will likely not be useful for other types of connectors.  

## Important Points to Consider During Development

The source code is sufficiently commented to help the developer build the new plugin. Comments in **UPPERCASE** identify important code sections or functions that must be updated.  

It is recommended to make the following changes in the Connector class:  
* Adjust the `CONFIG_FILE_PATH` variable if needed  
* Change the value of the `code` parameter to one identifying the plugin (e.g., `EasySDIv4`)  

Next, adapt the functions that override the IConnector interface methods:  
* `getParams` to define the connector parameters. This method returns plugin parameters as a JSON array. If the plugin does not accept parameters, return an empty array.  
* `importCommands` to manage importing commands executed by the remote server.  
* `exportResult` to manage sending the processing result of a command back to the originating server.  

Private functions in the code handle utility operations or process received data. These can be adapted or removed as needed. These functions are clearly identified in the Connector class code.  

## Installing or Updating the Plugin in EXTRACT

Before compiling the connector, delete the **target** folder.  
Once the connector is compiled and the JAR file is generated, place the JAR into the **WEB-INF/classes/connectors** directory of the application (containing all connector plugins).  
For updates, delete the WAR of the previous version to avoid conflicts.  

```
Restarting the Tomcat EXTRACT application is then required so that the connector changes are taken into account.
```

[Yarn_Site]: https://yarnpkg.com/ "Yarn package manager site"
