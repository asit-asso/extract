package ch.asit_asso.extract.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

public class PasswordHashGeneratorTest {

    @Test
    public void generatePasswordHash() {
        Pbkdf2PasswordEncoder encoder = new Pbkdf2PasswordEncoder();
        String password = "motdepasse21";
        String encodedPassword = encoder.encode(password);
        System.out.println("Password: " + password);
        System.out.println("Encoded: " + encodedPassword);
        System.out.println("UPDATE users SET pass = '" + encodedPassword + "' WHERE login = 'admin';");

        // Verify it works
        boolean matches = encoder.matches(password, encodedPassword);
        System.out.println("Verification: " + matches);
    }
}