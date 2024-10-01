package ch.asit_asso.extract.unit.web.model;
import ch.asit_asso.extract.web.model.SetupModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SetupModelTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // Tests valides
    @Test
    @DisplayName("Testing with a valid model")
    void testValidSetupModel() {
        SetupModel model = new SetupModel("admin");
        model.setName("Admin");
        model.setEmail("admin@example.com");
        model.setLogin("admin123");
        model.setPassword1("$Pas$word21!");
        model.setPassword2("$Pas$word21!");

        Set<ConstraintViolation<SetupModel>> violations = validator.validate(model);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Testing with a reserved word")
    void testWithReservedWord() {
        SetupModel model = new SetupModel("admin");
        model.setName("Admin");
        model.setEmail("admin@example.com");
        model.setLogin("system");
        model.setPassword1("$Pas$word21!");
        model.setPassword2("$Pas$word21!");

        Set<ConstraintViolation<SetupModel>> violations = validator.validate(model);
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("{setup.fields.login.reserved}");
    }
    // Tests pour les champs de contraintes individuelles
    @Test
    @DisplayName("Testing without the name")
    void testInvalidNameEmpty() {
        SetupModel model = new SetupModel("admin");
        model.setName(""); // Invalid: empty name

        Set<ConstraintViolation<SetupModel>> violations = validator.validate(model);
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("{setup.fields.name.constraint.mandatory}");
    }

    // Tests pour les champs de contraintes individuelles
    @Test
    @DisplayName("Testing with a name that is too short")
    void testInvalidNameTooShort() {
        SetupModel model = new SetupModel("admin");
        model.setName("Y"); // Invalid: empty name

        Set<ConstraintViolation<SetupModel>> violations = validator.validate(model);
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("{setup.fields.name.constraint.size}");
    }

    // Tests pour les champs de contraintes individuelles
    @Test
    @DisplayName("Testing with a name that is too long")
    void testInvalidNameTooLong() {
        SetupModel model = new SetupModel("admin");
        model.setName("Yyyasasfasfasfa  sadg asdg asdg asdg asdg asdg ads dg"); // Invalid: empty name

        Set<ConstraintViolation<SetupModel>> violations = validator.validate(model);
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("{setup.fields.name.constraint.size}");
    }

    // Tests pour les champs de contraintes individuelles
    @Test
    @DisplayName("Testing with an invalid email")
    void testInvalidEmailEmpty() {
        SetupModel model = new SetupModel("admin");
        model.setEmail(""); // Invalid: no email

        Set<ConstraintViolation<SetupModel>> violations = validator.validate(model);
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("{setup.fields.email.constraint.mandatory}");
    }

    // Tests pour les champs de contraintes individuelles
    @Test
    @DisplayName("Testing with an invalid email")
    void testInvalidEmailFormat() {
        SetupModel model = new SetupModel("admin");
        model.setEmail("invalid-email"); // Invalid: not an email

        Set<ConstraintViolation<SetupModel>> violations = validator.validate(model);
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("{setup.fields.email.constraint.format}");
    }

    @Test
    void testInvalidLoginEmpty() {
        SetupModel model = new SetupModel("admin");
        model.setLogin(""); // Invalid: pattern mismatch

        Set<ConstraintViolation<SetupModel>> violations = validator.validate(model);
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("{setup.fields.login.constraint.mandatory}");
    }

    @Test
    void testInvalidLoginTooShort() {
        SetupModel model = new SetupModel("admin");
        model.setLogin("Y"); // Invalid: pattern mismatch

        Set<ConstraintViolation<SetupModel>> violations = validator.validate(model);
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("{setup.fields.login.constraint.size}");
    }

    @Test
    void testInvalidLoginTooLong() {
        SetupModel model = new SetupModel("admin");
        model.setLogin("Yyyaaaaabbbbcccdddeeefffgggghhhiiiijjjjj"); // Invalid: pattern mismatch

        Set<ConstraintViolation<SetupModel>> violations = validator.validate(model);
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("{setup.fields.login.constraint.size}");
    }

    @Test
    void testInvalidLoginPattern() {
        SetupModel model = new SetupModel("admin");
        model.setLogin("invalid-login!"); // Invalid: pattern mismatch

        Set<ConstraintViolation<SetupModel>> violations = validator.validate(model);
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("{setup.fields.login.constraint.pattern}");
    }

    @Test
    void testInvalidPassword1Empty() {
        SetupModel model = new SetupModel("admin");
        model.setPassword1(""); // Invalid: too short according to PasswordPolicy

        Set<ConstraintViolation<SetupModel>> violations = validator.validate(model);
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("{setup.fields.password1.constraint.mandatory}");
    }

    @Test
    void testInvalidPassword1TooShort() {
        SetupModel model = new SetupModel("admin");
        model.setPassword1("123"); // Invalid: too short according to PasswordPolicy

        Set<ConstraintViolation<SetupModel>> violations = validator.validate(model);
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("{setup.fields.password1.constraint.policy}");
    }

    @Test
    void testInvalidPassword1TooLong() {
        SetupModel model = new SetupModel("admin");
        model.setPassword1("A".repeat(25)); // Invalid: too long password

        Set<ConstraintViolation<SetupModel>> violations = validator.validate(model);
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("{setup.fields.password1.constraint.policy}");
    }

    @Test
    void testInvalidPassword1NoUppercase() {
        SetupModel model = new SetupModel("admin");
        model.setPassword1("$pas$word21!"); // Invalid: too long password

        Set<ConstraintViolation<SetupModel>> violations = validator.validate(model);
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("{setup.fields.password1.constraint.policy}");
    }

    @Test
    void testInvalidPassword1NoLowercase() {
        SetupModel model = new SetupModel("admin");
        model.setPassword1("$PAS$WORD21!"); // Invalid: too long password

        Set<ConstraintViolation<SetupModel>> violations = validator.validate(model);
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("{setup.fields.password1.constraint.policy}");
    }

    @Test
    void testInvalidPassword1NoDigit() {
        SetupModel model = new SetupModel("admin");
        model.setPassword1("$Pas$WordSV!"); // Invalid: too long password

        Set<ConstraintViolation<SetupModel>> violations = validator.validate(model);
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("{setup.fields.password1.constraint.policy}");
    }

    @Test
    void testInvalidPassword1Sequential() {
        SetupModel model = new SetupModel("admin");
        model.setPassword1("$Pas$WordABCD!"); // Invalid: too long password

        Set<ConstraintViolation<SetupModel>> violations = validator.validate(model);
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("{setup.fields.password1.constraint.policy}");
    }

    @Test
    void testInvalidPassword1Special() {
        SetupModel model = new SetupModel("admin");
        model.setPassword1("PasSWord91"); // Invalid: too long password

        Set<ConstraintViolation<SetupModel>> violations = validator.validate(model);
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("{setup.fields.password1.constraint.policy}");
    }

    @Test
    void testInvalidPassword1Common() {
        SetupModel model = new SetupModel("admin");
        model.setPassword1("password123"); // Invalid: too long password

        Set<ConstraintViolation<SetupModel>> violations = validator.validate(model);
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("{setup.fields.password1.constraint.policy}");
    }
}
