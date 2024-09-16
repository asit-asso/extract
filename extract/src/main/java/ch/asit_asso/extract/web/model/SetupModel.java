package ch.asit_asso.extract.web.model;


import ch.asit_asso.extract.web.constraints.FieldsValueMatch;
import ch.asit_asso.extract.web.constraints.PasswordPolicy;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Classe représentant les données renvoyées par l'écran "Setup administrator"
 *
 * @author Bruno Alves
 */
@FieldsValueMatch(
        field = "password1",
        fieldMatch = "password2",
        message = "{setup.passwords.not.match}"
)
public class SetupModel {

    @NotBlank(message = "{setup.fields.name.constraint.mandatory}")
    @Size(min = 2, max = 50, message = "{setup.fields.name.constraint.size}")
    private String name;

    @NotBlank(message = "{setup.fields.email.constraint.mandatory}")
    @Email(message = "{setup.fields.email.constraint.format}")
    private String email;

    @NotBlank(message = "{setup.fields.login.constraint.mandatory}")
    @Size(min = 2, max = 24, message = "{setup.fields.login.constraint.size}")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_-]{2,24}$", message = "{setup.fields.login.constraint.pattern}")
    private String login;

    @NotBlank(message = "{setup.fields.password1.constraint.mandatory}")
    @PasswordPolicy(minLength = 8, maxLength = 24, message = "{setup.fields.password1.constraint.policy}")
    private String password1;

    @NotBlank(message = "{setup.fields.password2.constraint.mandatory}")
    private String password2;

    public SetupModel(String login) {
        this.login = login;
    }

    public String getIdentifier() {
        return login;
    }

    public void setIdentifier(String identifier) {
        this.login = identifier;
    }

    public String getPassword1() {
        return password1;
    }

    public void setPassword1(String password1) {
        this.password1 = password1;
    }

    public String getPassword2() {
        return password2;
    }

    public void setPassword2(String password2) {
        this.password2 = password2;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean passwordsMatch() {
        return this.password2.equals(this.password1);
    }

    @Override
    public String toString() {
        return "SetupModel{" +
                "login='" + login + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
