package ch.asit_asso.extract.utils;

//import org.apache.commons.validator.routines.EmailValidator;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * A set of helper functions to manipulate e-mail info.
 *
 * @author Yves Grasset
 */
public abstract class EmailUtils {

    /**
     * Checks if a given string can be parsed as a valid e-mail address.
     *
     * @param address the string to check
     * @return <code>true</code> if the string is a valid e-mail address
     */
    public static boolean isAddressValid(final String address) {

        //return EmailValidator.getInstance().isValid(address);

        // Version temporaire le temps que la faille due aux dépendances d'Apache Commons Validator soit corrigée
        // (prévu pour la v2.0 de cette librairie)
        try {
            InternetAddress emailAddr = new InternetAddress(address);
            emailAddr.validate();

        } catch (AddressException ex) {
            return false;
        }

        return true;
    }
}
