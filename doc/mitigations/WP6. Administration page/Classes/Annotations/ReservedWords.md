
## Description

The `@ReservedWords` annotation is used to prevent users to set some reserved values. Values are normalised (lower-cased) before being compared to the list of reserved words.

## Usage

The `@ReservedWords` annotation can be applied to various elements such as methods, fields, types, parameters and constructors. It is generally used on template class fields to ensure that strings cannot take a value contained in the list of words.

```java
@ReservedWords(
    words = {"system"}
)
private String login;
```
## Attributes

- **`message`** (default: `“Field contains a reserved word”`): The error message to be displayed if validation fails. This message can be internationalised using property files.
    
- **`words`** (default: `{"system"}`): The list of reserved words to test against

## Validation example

Here's an example of using the `@ReservedWords` annotation on a login field in a template class:

```java
public class UserModel {

    @ReservedWords(
        words = {"system", "admin"}
    )
    private String login;

    // Getters et setters
}
```

In this example, the login cannot have neither the value of `system` nor `admin`.