
## Description

The `@PasswordPolicy` annotation is used to define custom validation rules for passwords in Java applications. It allows you to specify criteria that passwords must satisfy to be considered valid. This annotation is used in conjunction with a validator (`PasswordPolicyValidator`) which implements the validation logic.

## Usage

The `@PasswordPolicy` annotation can be applied to various elements such as methods, fields, types, parameters and constructors. It is generally used on template class fields to ensure that passwords meet specified security criteria.

```java
@PasswordPolicy(
    minLength = 8,
    maxLength = 20,
    uppercase = true,
    lowercase = true,
    digit = true,
    special = true,
    common = false,
    sequential = false
)
private String password;
```
## Attributes

- **`message`** (default: `“{validation.password.policy}”`): The error message to be displayed if validation fails. This message can be internationalized using property files.
    
- **`minLength`** (default: `0`): The minimum length required for the password. The password must contain at least this number of characters.
    
- **`maxLength`** (default: `Integer.MAX_VALUE`): The maximum length allowed for the password. The password must not exceed this number of characters.
    
- **`uppercase`** (default: `true`): Indicates whether the password must contain at least one uppercase letter. If `true`, a capital letter is required.
    
- **``lowercase`** (default: `true`): Indicates whether the password must contain at least one lowercase letter. If `true`, a lowercase letter is required.
    
- **`digit`** (default: `true`): Indicates whether the password must contain at least one digit. If `true`, a digit is required.
    
- **`special`** (default: `true`): Indicates whether the password must contain at least one special character (e.g. `!`, `@`, `#`, `$`). If `true`, a special character is required.
    
- **`common`** (default: `true`): Indicates whether the password should not be a common or frequently used string. If `true`, the password must be sufficiently complex to avoid common passwords.
    
- **`sequential`** (default: `true`): Indicates whether the password must not contain character sequences (e.g. `1234`, `abcd`). If `true`, character sequences are forbidden.
    
- **`groups`**: Used to define validation groups. Constraints can be applied to different groups for conditional validation.
    
- **`payload`**: Allows you to provide additional information to the constraint. This can be used to transmit metadata.
    

## Validation example

Here's an example of using the `@PasswordPolicy` annotation on a password field in a template class:

```java
public class UserModel {

    @PasswordPolicy(
        minLength = 8,
        maxLength = 16,
        uppercase = true,
        lowercase = true,
        digit = true,
        special = true,
        common = false,
        sequential = false
    )
    private String password;

    // Getters et setters
}
```

In this example, the password must be between 8 and 16 characters long, and contain at least one uppercase letter, one lowercase letter, one number and one special character. Common passwords and character sequences are not permitted.