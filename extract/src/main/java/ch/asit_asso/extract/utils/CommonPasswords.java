package ch.asit_asso.extract.utils;


import java.util.Arrays;

public class CommonPasswords {
    public static final String[] COMMON_PASSWORDS = {
            "123456",
            "password",
            "123456789",
            "qwerty",
            "abc123",
            "password1",
            "12345678",
            "12345",
            "1234567",
            "password123",
            "qwerty123",
            "1q2w3e4r",
            "admin",
            "letmein",
            "welcome",
            "monkey",
            "123123",
            "1234",
            "sunshine",
            "iloveyou",
            "123321",
            "qwertyuiop",
            "123",
            "admin123",
            "password1",
            "iloveyou1",
            "123456a",
            "qwerty1",
            "password12",
            "1q2w3e4r5t",
            "abcdef",
            "password1234",
            "qwert",
            "1q2w3e",
            "qwertyui",
            "123456b",
            "welcome1",
            "password12345",
            "123123123",
            "qwerty1234",
            "qwerty123456",
            "letmein1",
            "monkey1",
            "password!23",
            "123qwe",
            "abc123456",
            "1q2w3e4r5t6y",
            "football",
            "123qweas",
            "extract",
            "extract123",
            "viageo",
            "viageo.ch",
            "plansreseaux",
            "plans-reseaux",
            "plans-reseaux.ch",
            "asitasit",
            "asitvd123",
            "motdepasse",
            "motdepasse21"
    };

    /**
     * Vérifie si le mot de passe fourni est dans la liste des mots de passe courants.
     *
     * @param password Le mot de passe à vérifier.
     * @return true si le mot de passe est commun, false sinon.
     */
    public static boolean isCommon(String password) {
        return Arrays.asList(COMMON_PASSWORDS).contains(password);
    }
}
