## User administration

### Users list
![Users list](../assets/admin-guide/users-list.png){width="1000"}

This page lists all users, whether active or inactive. You can:

* **create a new user** by clicking on the ``Nouvel utilisateur`` button
* **manage user groups** by clicking on the ``Groupes`` button
* **edit a user** by clicking on their login
* **delete a user** by clicking on the red button with a cross. It is not possible to delete (grayey out button):
    * Your own account
    * A user directly associated with a process
    * The last active user of a group associated with a process

### User settings
![Users settings](../assets/admin-guide/users-settings.png){width="1000"}

User's settings are:

* User's full name (for display purposes)
* User's role in the application (operator or administrator). It is not possible to change the role of your own account
* The username they use to log in (must be unique within the application)
* Whether the user is active. An inactive user cannot log in to the application. It is not possible to deactivate the account of a user associated with a process.
* The email address to which notifications concerning them will be sent
* Whether email notifications are enabled. Only operator-type notifications are affected. Even if notifications are deactivated, the user will continue to receive emails allowing them to reset their password. All notifications for administrators are not affected by this setting.
* The password they use to log in. If the password is changed, it must follow the password policy.
* Whether two-factor authentication should be required for the user. In this case, the user will have to register with Google Authenticator (or compatible) the next time they log in, without the option to cancel the registration. If you choose not to require two-factor authentication, the user can still choose to enable it.

<br>
Using the buttons at the top of the page, administrators can:

* Manage the status of two-factor authentication for the user, i.e. enable it, disable it, or ask the user to register again with a new code. This is a drop-down button that displays a menu when clicked, showing the actions available depending on the user's status
* In the case of a local user and if LDAP is enabled, the administrator can choose to migrate the user to an LDAP user. The password to be used then becomes the one defined in the company directory. **Warning**: this operation cannot be reversed.

<br>
These operations will result in the loss of any other changes currently in progress.

If the user is an LDAP user, the only changes available in Extract are:

* Email notifications status
* Two-factor authentication status (including the option to enforce it or not)

### Users groups
User groups allow operators to be grouped together to be assigned to a process as a group. Group settings are accessed from the user list.

#### Groups list
![Users groups list](../assets/admin-guide/users-groups-list.png){width="1000"}

This page lists all users groups with their number of users (active or inactive). Following actions are possible:

* **Create a new group** by clicking on the `Nouveau groupe` button 
* **Edit a group** by clicking on its name
* **Delete a group** by clicking on the red button with a cross. It is not possible to delete a group associated with a process (grayed out button)

#### Groups settings
![Users groups settings](../assets/admin-guide/users-groups-settings.png){width="1000"}

Group settings are:

* Group name (mandatory and unique)
* The list of users it contains. A user can belong to several groups. For groups associated with a process users can be added or removed, but there must always be at least one active user remaining.

## Processes
A process is a sequence of tasks that generates the data requested by a client.

### Processes list
![Processes list](../assets/admin-guide/processes-list.png){width="1000"}

This page lists all processes whether or not they are associated with a rule. Following actions are available:

* **Create a process** by clicking on the `Nouveau traitement` button
* **Edit a process** by clicking on its name
* **Duplicate a process** by clicking on the green button with a copy icon. An identical copy of the process will be created, including its tasks and settings.
* **Delete a process** by clicking on the red button with a cross. Processes associated with a request that is not completed or associated with a connector's rule cannot be deleted (grayed out button).

## Connectors

## Orchestration

## Application settings

### LDAP-Based User Authentication

<br>
<br>
<br>
<br>
<br>