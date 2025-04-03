---
title: Install
---

## Software prerequisites

* 64-bit Windows or Linus operating system
* Java 17 (Oracle or OpenJDK full version, not Headless)
* PostgreSQL 13 or above <span style="color: grey;">(Extract may work with older versions that are not supported anymore)</span>
* Tomcat 9 (Extract is not currently compatible with Tomcat 10 due to the use of SpringBoot 2)
* A “modern” web browser (<2 years old) to access the application

## Installation

!!! Info 
    In the given paths, **[ZIP]** indicates the root of the delivered ZIP file in the release assets. Otherwise, relative paths are expressed in relation to the directory in which the application is deployed.

!!! Tip
    The latest release can be downloaded [here](https://github.com/asit-asso/extract/releases/tag/v2.2.0).

1.	Create a PostgreSQL database to store the application data. Encoding must be UTF-8.
2.	The database must be accessible by a user with read, write and object creation/deletion privileges. The database content is automatically generated when the application is first started.
3.	Deploy the `[ZIP]\application\extract##x.x.x.war` file to Tomcat server.
It is normal for any automatic start-up of the application after deployment to fail.
4. In the folder of the deployed application, go to the `WEB-INF\classes` directory and modify the ``application.properties`` file.
5. Modify the database connection properties:
    * **spring.datasource.url** : JDBC database connection string
    * **spring.datasource.username** : Database user name
    * **spring.datasource.password** : Database user password

6. (Optional:) If necessary, change the application language using the `extract.i18n.language` property in the `application.properties` file. The value must be the ISO code of the desired language (e.g. `en`). Translation files for this language must exist in a directory of the same name under `WEB-INF\classes\static\lang`. To add a new language, please refer to [Add a new language section](http://localhost:8000/extract/getting-started/customize/#add-a-new-language).
7.	Enter the application URL using the `application.external.url` property. The value of this property is used to generate links in notification e-mails.
8.	Enter a random 32-character ISO-8859-1 string for `database.encryption.secret` and `database.encryption.salt` properties. These strings are used as keys to encrypt certain database values.
9.	Save the file.
10.	In the same folder, log properties can be defined in the `logback-spring.xml` file
11.	Start the application. 
12. Connect to the database. 
13.	Execute the `[ZIP]\sql\update_db.sql` script to apply any database schema modifications that are not automatically performed by the application.
14.	Connect to the application. When logging in for the first time, the user must create an admin account:

    ![Create admin user](../assets/install/create_admin.png)

15.	Fill in the fields and click `Créer le compte`
16.	Go to `Settings`
17.	Enter the storage path. This is the directory in which data relating to request processing will be written. The Tomcat user must therefore have read and write rights to this directory.
18.	Enter SMTP server parameters for sending notifications
19.	Click on ``Enregistrer``
20.	Congratulation ! The application is now operational.

## Update the application

## Server Migration