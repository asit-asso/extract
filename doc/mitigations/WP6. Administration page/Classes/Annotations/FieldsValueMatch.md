## Description

The `@FieldsValueMatch` annotation is used to validate that the values of two fields in a template class are identical. This annotation is useful for scenarios where it is necessary to verify that two fields, such as password or password confirmation fields, are consistent with each other.

The annotation is usually applied to a template class, and uses a validator (`FieldsValueMatchValidator`) to check that field values match.

## Usage

The `@FieldsValueMatch` annotation is applied to a template class to ensure that the values of specified fields match. It is often used in registration or modification forms to check that password fields or other confirmation fields are identical.

Here's an example of how annotation can be applied to a template class:

```java
@FieldsValueMatch(
    field = "password1",
    fieldMatch = "password2",
    message = "Les mots de passe ne correspondent pas !"
)
public class UserModel {

    private String password1;
    private String password2;

    // Getters et setters
}
```

## Attributes

- **`message`** (default: `“Fields values don't match!”`) : The error message displayed when field values don't match. This message can be internationalized using property files.
    
- **`field`** : The name of the field whose value is to be matched.
    
- **`fieldMatch`**: The name of the field with which the value of the specified field (`field`) is to be compared.
    
- **`groups`**: Used to define validation groups. Constraints can be applied to different groups for conditional validations.
    
- **`payload`**: Allows you to provide additional information to the constraint. This can be used to transmit metadata.
    

## Validation example

Here's an example of using the `@FieldsValueMatch` annotation to check that the `password1` and `password2` fields have the same value:

```java
@FieldsValueMatch(
    field = "password1",
    fieldMatch = "password2",
    message = "Les mots de passe ne correspondent pas !"
)
public class RegistrationForm {

    private String password1;
    private String password2;

    // Getters et setters
}
```

In this example, when validating the form, the validator will check that the value of `password1` matches that of `password2`. If the values don't match, the specified error message will be displayed.