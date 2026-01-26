---
search:
  exclude: true
---

# Extract - Initialize a New Task Plugin

## Introduction

This Java module is an example of a task plugin that can be used to initialize a new plugin for the Extract project.  
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

## Initializing the New Task Plugin
The project must be a Java module.  
The new plugin project must define a dependency on the extract-interface project.  
Required dependencies are defined in the pom.xml file.  

To initialize a new plugin, follow the steps in order:  
1. Copy the extract-task-sample plugin code into a Java workspace. The system will prompt you to define a new name. If it does not, use the context menu (right-click) `Refactor > Rename`.  

2. Edit the **pom.xml** file of the module, replace occurrences of `extrat-task-sample` with the new plugin name.  
   After right-clicking the file, choose `Add as Maven Project`.  

3. Right-click the namespace `ch.asit_asso.extract.plugins.sample`, choose the menu `Refactor > Rename`.  
   Enter the new class name to identify the plugin. If prompted, click the `Add in current Module` button to apply the changes only to the module.  
   This will automatically update the package name in all files of the module.  

4. Right-click the **SamplePlugin.java** file, choose `Refactor > Rename`, then enter the new main class name of the plugin. This will rename the file and update all references to this class wherever it is used.  

5. Repeat step 4 for the files **SampleRequest.java** and **SampleResult.java**.  

6. Edit the **LocalizedMessages.java** file: adjust the value of the LOCALIZED_FILE_PATH_FORMAT parameter, which corresponds to the path to the `lang` directory.  

7. Check the **module-info.java** file, especially the reference to `SamplePlugin`. Also verify line 4 of this file (reference to the namespace `ch.asit_asso.extract.plugins.sample`).  

8. Check the file **resources\META-INF\services\ch.asit_asso.extract.plugins.common.ITaskProcessor**, especially the reference to the class `ch.asit_asso.extract.plugins.sample.SamplePlugin`.  

9. Right-click the **resources\plugins\sample** folder, choose `Refactor > Rename`, and enter the new name.  

10. Edit the file **resources\plugins\<plugin>\lang\fr\messages.properties**. Modify or add the labels used by the plugin source code. This step can be done progressively during development.  

11. Edit the file **resources\plugins\<plugin>\lang\fr\help.html** to describe the plugin’s functionality. *The file content is in HTML format*.  

12. Edit the file **resources\plugins\<plugin>\properties\config.properties**. This file contains the plugin configuration parameters used by the source code. This step can be done progressively during development.  

## Important Points to Consider During Development

The source code is sufficiently commented to help the developer build the new plugin. Comments in **UPPERCASE** identify important code sections or functions that must be updated.  

It is recommended to make the following changes in the Plugin class:  
* Adjust the `CONFIG_FILE_PATH` variable if needed  
* Change the value of the `code` parameter to one identifying the plugin (e.g., `remark` or `fmeserver`)  
* Change the value of the `pictoClass` parameter to an appropriate CSS class name (plugin logo image). Search for an icon on the [Font Awesome][Fontawesome_Site] website.  

Next, adapt the functions that override the ITaskProcessor interface methods:  
* `getParams` to define the task plugin parameters. This method returns plugin parameters as a JSON array. If the plugin does not accept parameters, return an empty array.  
* `execute` to handle the execution of the plugin.  

## Installing or Updating the Plugin in EXTRACT

Before compiling the plugin, delete the **target** folder.  
Once the plugin is compiled and the JAR file is generated, place the JAR into the **WEB-INF/classes/task_processors** directory of the application (containing all task plugins).  
For updates, delete the WAR of the previous version to avoid conflicts.  

```
Restarting the Tomcat EXTRACT application is then required so that the plugin changes are taken into account.
```

[Yarn_Site]: https://yarnpkg.com/ "Yarn package manager site"  
[Fontawesome_Site]: https://fontawesome.com/icons "FontAwesome website"
