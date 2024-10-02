## Description

The `SetupController` class is a Spring MVC controller responsible for managing operations related to the initial configuration of the application, including the creation of an administrator account. It uses the `UserService` to manage users and provides views for configuration operations.
## Attributes

- **`DEFAULT_VIEW_NAME`**: Name of the default view used to display the setup page. Value: `“setup/index”`.
    
- **`DEFAULT_REDIRECT_URL`**: Redirect URL after successful configuration. Value: `“redirect:/login”`.
    
- **`logger`**: Instance of `Logger` to record log messages, in particular to track the creation of an administrator user.
    
- **`userService`**: Service used to create users. It is injected via the constructor and is responsible for managing user-related operations.

- **`appInitializationService`**: Service used to check whether the application was initialized.

## Constructor

- SetupController(UserService userService)`** : Controller constructor, which initializes the userService instance.

## Methods

### `showSetupPage`

```java
@GetMapping("/setup")
public ModelAndView showSetupPage(Model model)
```

- **Description**: Handles GET requests for the `/setup` URL. Displays the initial configuration page for administrator account creation.
- **Parameters** :
    - `model`: The model used to pass attributes to the view.
- Returns a `ModelAndView` object configured with the default view (`DEFAULT_VIEW_NAME`) and a `SetupModel` object pre-populated with a default login.t.

### `handleSetup`

```java
@PostMapping("/setup")
public String handleSetup(Model model, @Valid @ModelAttribute("model") SetupModel setupModel, BindingResult bindingResult)
```

- **Description**: Handles POST requests for the `/setup` URL. Processes data submitted by the user to create an administrator account.
- **Parameters** :
    - `model`: The model used to pass attributes to the view.
    - `setupModel`: `SetupModel` object containing user-submitted data, annotated with `@Valid` for validation.
    - `bindingResult`: Contains the results of model data validation.
- Returns the default view (`DEFAULT_VIEW_NAME`) in the event of a validation error, or redirects to the redirection URL (`DEFAULT_REDIRECT_URL`) after successful configuration.

### `handleUserCreation`

```java
private void handleUserCreation(SetupModel setupModel)
```

- **Description** : Private method that handles the creation of an administrator user from data provided by the `SetupModel` model. Registers the user using `userService` and logs the success of the operation.
- **Parameters** :
    - `setupModel` : `SetupModel` object containing the data to create the administrator user.

### `denyUnlessAdminUserIsNotCreated`

```java
private void denyUnlessAdminUserIsNotCreated()
```

- **Description** : Private method that throws a `SecurityException` if any method of the Controller is invoked after an admin has already been set up.

## Operation

1. **Displaying the Configuration Page** : When a GET request is made to `/setup`, the `showSetupPage` method is called. It prepares a model with an initial `SetupModel` object and returns the configuration view.
    
2. **Configuration data processing** : When a POST request is made to `/setup`, the `handleSetup` method is called. It validates the submitted data. If the data is valid, it calls `handleUserCreation` to create an administrator user and redirects to the login page. In the event of a validation error, it returns to the configuration view with the errors displayed.
    
3. **Administrator User Creation**: The `handleUserCreation` method uses `userService` to create an administrator user based on template data, and logs this operation.