## Modélisation des menaces
### Model

The threat analysis is based on the STRIDE framework, which generally does an excellent job of identifying the main attack vectors. Only the most reasonable mitigation measures are implemented.

### Threats

#### **Spoofing (Identity Impersonation)**

- **Threats:**
    1. A malicious user could create a fake administrator account by manipulating the form data.
    2. An attacker could impersonate an administrator by directly accessing the account creation interface during the first use.
    3. Brute force attacks could be used to guess the admin password.
- **Mitigation measures:**
    - Use an email verification mechanism before confirming the creation of the admin account.
    - Add CAPTCHA to prevent the automated creation of accounts.
    - Limit the number of account creation and login attempts to prevent brute force attacks.
    - Restrict access to the admin creation page, for example, via IP protection or a temporary unique token.

> [!info] 
> Since account creation is done locally, only an operator is allowed to do so. Additionally, the account creation is part of the platform configuration and is executed immediately after the platform launches. Therefore, it is not necessary to apply the above mitigation measures.

#### **Tampering (Data Alteration)**

- **Threats:**
    1. An attacker could intercept and modify the data submitted in the form (name, email, login, password).
    2. A malicious user could try to bypass client-side validations to submit invalid or malicious data.
    3. Unauthorized access to the database could allow direct modification of the admin account information.
- **Mitigation measures:**
    - Use HTTPS protocol to ensure communication security.
    - Validate and sanitize all inputs server-side to prevent SQL injections, XSS, or other types of injection attacks.
    - Use strong encryption (bcrypt) to store passwords and protect critical data.
    - Implement roles and permissions so only accounts with the appropriate privileges can modify administrative information.

> [!info] 
> The form operates locally over HTTP. Extreme caution must be exercised during the form's creation, as the password is visible on the network where the form is initialized.  
> Regarding other mitigation measures, all fields are validated, and a strong password policy is enforced:
> 
> - At least 1 digit
> - At least 1 lowercase letter
> - At least 1 uppercase letter
> - At least 1 special character
> - No repeated characters
> - No common passwords
> 
> A CSRF token is also injected to prevent replays, and the Pbkdf2 encoder is used to hash passwords.
> 
> Role management remains the same as before.


#### **Repudiation**

- **Threats:**
    1. A user could deny creating the admin account or contest certain actions performed under their account.
    2. An attacker could use an already-created admin account's information and deny involvement in malicious actions.
- **Mitigation measures:**
    - Record timestamped logs of critical actions, including admin account creation and logins.
    - Digitally sign logs to ensure their integrity, so they cannot be tampered with.
    - Use log management tools to monitor in real-time for suspicious activities and generate alerts.

> [!info] 
> Among the listed measures, we log all creation actions for auditing purposes. However, a separate log for audit entries would be beneficial.
> 
> Other measures are too stringent for an application like this.

#### 4. **Information Disclosure**

- **Threats:**
    1. Sensitive data (login, password, email) could be exposed in the form or in transit.
    2. If the application displays detailed error messages upon failed form submission, it could reveal information about the application's structure or databases.
    3. Passwords could accidentally be stored in plain text or revealed in application logs.
- **Mitigation measures:**
    - Use generic error messages to prevent the disclosure of technical information.
    - Never display passwords in plain text in the interface or logs, and apply secure session management practices (authentication tokens, session expiration).
    - Encrypt sensitive data (login, email) and mask the password (e.g., using stars) during form submission.
    - Enforce strict error-handling policies to avoid revealing internal data to the user.

> [!info] 
> Error messages are generic, and passwords are not redisplayed. Furthermore, the password field uses stars during input.
> 
> Sensitive data is not encrypted, as there is no simple solution for this, and the necessary resources would be too significant.

#### 5. **Denial of Service (DoS)**

- **Threats:**
    1. An attacker could flood the account creation page with massive requests, preventing the admin account from being created.
    2. Repeated submissions of invalid or malicious data could degrade the application's performance.
    3. An attacker could attempt to force the admin account creation process by blocking the submission of valid data.
- **Mitigation measures:**
    - Implement rate limiting to limit the number of allowed requests within a given time interval.
    - Use DDoS protection services to prevent saturation attacks.
    - Add backoff delays or CAPTCHA after a certain number of failed form submission attempts.

> [!info] 
> The form is only accessible locally for platform configuration, which greatly reduces the risk of DDoS. Adding CAPTCHA or delays after submission would be overkill, as this form is only displayed at the first application startup and for the operator only. The risk of DDoS is practically nil.
#### **Elevation of Privilege**

- **Threats:**
    1. An unauthorized user could attempt to create an admin account with extended privileges.
    2. An attacker could exploit a flaw in the privilege validation code to access restricted functionalities.
    3. After creating the first admin account, another user could attempt to elevate their privileges without authorization.
- **Mitigation measures:**
    - Restrict access to the admin account creation interface after the first use and disable this feature.
    - Enforce strict role-based access control (RBAC) to ensure that only authorized users have access to sensitive functions.
    - Regularly audit the privileges of existing accounts and detect unauthorized privilege elevations.
    - Ensure the admin creation process is well protected by rigorous rights validation.

> [!info] 
> The functionality is disabled after the user is created, so it's not possible to create another admin account through this method. The code already enforces a role-based security policy. There is no role for admin account creation, as the operator launching the application is responsible for creating this account. This is logged by the system since they are connected to their Linux session.