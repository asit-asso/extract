package ch.asit_asso.extract.utils;

//import org.apache.commons.validator.routines.EmailValidator;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.persistence.UsersRepository;

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

        if (address == null) {
            return false;
        }

        try {
            InternetAddress emailAddr = new InternetAddress(address);
            emailAddr.validate();

        } catch (AddressException ex) {
            return false;
        }

        return true;
    }



    public static boolean isAddressInUse(final String address, final User currentUser,
                                         final UsersRepository usersRepository) {

        if (currentUser != null && currentUser.getId() != null) {
            return EmailUtils.isAddressInUseByOtherUser(address, currentUser.getLogin(), usersRepository);
        }

        return EmailUtils.isAddressInUse(address, usersRepository);
    }



    public static boolean isAddressInUse(final String address, final UsersRepository usersRepository) {

        return (usersRepository.countByEmailIgnoreCase(address) > 0);
    }



    public static boolean isAddressInUseByOtherUser(final String address, final String currentUserLogin,
                                                           final UsersRepository usersRepository) {

        return (usersRepository.countByEmailIgnoreCaseAndLoginNot(address, currentUserLogin) > 0);
    }
}
