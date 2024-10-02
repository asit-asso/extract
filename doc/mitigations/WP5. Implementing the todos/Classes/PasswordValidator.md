## Overview

- **Purpose**: Implements strong password validation with configurable rules.
- **Main Functions**:
    - Validation of passwords against common security requirements.
    - Customizable behavior for different use cases.

---

## Properties

### Default Validation Settings:

- `uppercase`: `true` – Ensures that the password includes at least one uppercase letter.
- `lowercase`: `true` – Ensures that the password includes at least one lowercase letter.
- `digits`: `true` – Ensures that the password includes at least one digit.
- `specialChars`: `true` – Ensures that the password includes at least one special character.
- `common`: `true` – Checks if the password is too common by using a list of known common passwords.
- `sequential`: `true` – Detects sequential or repeated characters (e.g., "abc", "123").
- `stopOnFirstError`: `false` – By default, validation continues even if the first rule fails.
- `minLength`: `8` – Minimum password length.
- `maxLength`: `24` – Maximum password length.

### Patterns:

- **Uppercase Pattern**: `[A-Z]`
- **Lowercase Pattern**: `[a-z]`
- **Digit Pattern**: `\\d`
- **Special Character Pattern**: `[#_\\-$!@%&:]`

---

## Methods

### `create()`

- **Description**: Creates a new instance of `PasswordValidator`.
- **Usage**:

```java
PasswordValidator validator = PasswordValidator.create();
```

### Fluent Configuration Methods

These methods allow configuring specific password validation requirements:

1. **`withDigits(boolean digits)`**:
    
    - Enables or disables digit validation.
2. **`withUppercase(boolean uppercase)`**:
    
    - Enables or disables uppercase letter validation.
3. **`withLowerCase(boolean lowercase)`**:
    
    - Enables or disables lowercase letter validation.
4. **`withSpecialChars(boolean specialChars)`**:
    
    - Enables or disables special character validation.
5. **`withCommon(boolean common)`**:
    
    - Enables or disables the check for common passwords.
6. **`withSequential(boolean sequential)`**:
    
    - Enables or disables validation against sequential or repeated characters.
7. **`withStopOnFirstError(boolean stopOnFirstError)`**:
    
    - Configures whether validation should stop after the first error is encountered.
8. **`withLength(int minLength, int maxLength)`**:
    
    - Sets the minimum and maximum password length.

### `validate(Object target, Errors errors)`

- **Description**: Validates the password against all configured rules.
- **Parameters**:
    - `target`: The password object (usually a string) to be validated.
    - `errors`: The `Errors` object to capture validation errors.
- **Checks**:
    - Length, uppercase, lowercase, digits, special characters, common passwords, sequential/repeated characters.
- **Example**:

```java
validator.validate(password, errors);
```
### `validateField(String fieldName, String password, Errors errors)`

- **Description**: Validates a specific field (e.g., a password field) using the same validation rules.
- **Parameters**:
    - `fieldName`: The name of the field being validated.
    - `password`: The password value to validate.
    - `errors`: The `Errors` object to capture validation issues.

---

## Utility Method

### `hasSequentialOrRepeatedChars(String password)`

- **Description**: Detects whether the password contains sequential or repeated characters.
- **Usage**: This method is called internally during validation to check for sequences like "abc" or repeated characters like "111".

---
## Example Usage

Here’s an example of how to use the `PasswordValidator` class:
```java
PasswordValidator validator = PasswordValidator.create()
    .withUppercase(true)
    .withDigits(true)
    .withSpecialChars(true)
    .withLength(8, 20);

String password = "StrongPass1!";
Errors errors = new MapBindingResult(new HashMap<>(), "password");

validator.validate(password, errors);

if (errors.hasErrors()) {
    // Handle validation errors
}
```

## Key Features:

- **Configurable Rules**: The validator allows customization for different password policies.
- **Comprehensive Validation**: Checks various aspects of password security, including length, character types, and known common passwords.
- **Error Handling**: Validation can either stop at the first encountered error or continue to check all conditions.

---

## Notes

- This validator mirrors the behavior of `PasswordPolicyValidator` but does not stop on the first error unless configured to do so.
- It integrates with Spring's `Errors` object for seamless error handling in web applications.
