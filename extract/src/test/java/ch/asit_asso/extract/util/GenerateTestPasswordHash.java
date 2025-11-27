package ch.asit_asso.extract.util;

import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

public class GenerateTestPasswordHash {
    public static void main(String[] args) {
        // Créer l'encodeur avec les mêmes paramètres que l'application
        Pbkdf2PasswordEncoder encoder = new Pbkdf2PasswordEncoder();

        // Le mot de passe utilisé dans les tests
        String password = "motdepasse21";

        // Générer le hash
        String hash = encoder.encode(password);

        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        System.out.println("\nSQL Update command:");
        System.out.println("UPDATE users SET pass = '" + hash + "' WHERE login = 'admin';");

        // Vérifier que ça fonctionne
        boolean matches = encoder.matches(password, hash);
        System.out.println("\nVerification: " + matches);
    }
}