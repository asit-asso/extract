package ch.asit_asso.extract.util;

import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        Pbkdf2PasswordEncoder encoder = new Pbkdf2PasswordEncoder();
        String password = "motdepasse21";
        String encodedPassword = encoder.encode(password);
        System.out.println("Password: " + password);
        System.out.println("Encoded: " + encodedPassword);

        // Verify it works
        boolean matches = encoder.matches(password, encodedPassword);
        System.out.println("Verification: " + matches);
    }
}