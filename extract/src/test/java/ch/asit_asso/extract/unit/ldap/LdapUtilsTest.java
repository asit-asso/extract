package ch.asit_asso.extract.unit.ldap;

import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.domain.User.Profile;
import ch.asit_asso.extract.ldap.LdapUser;
import ch.asit_asso.extract.ldap.LdapUtils;
import ch.asit_asso.extract.persistence.UsersRepository;
import ch.asit_asso.extract.unit.MockEnabledTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LdapUtilsTest extends MockEnabledTest {

    private static final String EMAIL_IN_USE = "email_in_use@mondomaine.ch";
    private static final String OTHER_LOGIN = "user2";
    private static final String USER_LOGIN = "user";
    private static final boolean USER_MODIFIED_ACTIVE = false;
    private static final String USER_MODIFIED_EMAIL = "user.new@mondomaine.ch";
    private static final String USER_MODIFIED_NAME = "Utilisateur modifié";
    private static final Profile USER_MODIFIED_ROLE = Profile.ADMIN;
    private static final boolean USER_ORIGINAL_ACTIVE = true;
    private static final String USER_ORIGINAL_EMAIL = "user@mondomaine.ch";
    private static final String USER_ORIGINAL_NAME = "Utilisateur original";
    private static final Profile USER_ORIGINAL_ROLE = Profile.OPERATOR;

    private User domainUser;

    @Mock
    private UsersRepository usersRepository;

    @BeforeEach
    void setUp() {
        Mockito.when(usersRepository.countByEmailIgnoreCaseAndLoginNot(LdapUtilsTest.EMAIL_IN_USE,
                                                                       LdapUtilsTest.USER_LOGIN)).thenReturn(1);

        Mockito.when(usersRepository.countByEmailIgnoreCaseAndLoginNot(LdapUtilsTest.USER_MODIFIED_EMAIL,
                                                                       LdapUtilsTest.USER_LOGIN)).thenReturn(0);

        //Init domain user
        this.domainUser = new User();
        this.domainUser.setActive(LdapUtilsTest.USER_ORIGINAL_ACTIVE);
        this.domainUser.setLogin(LdapUtilsTest.USER_LOGIN);
        this.domainUser.setName(LdapUtilsTest.USER_ORIGINAL_NAME);
        this.domainUser.setEmail(LdapUtilsTest.USER_ORIGINAL_EMAIL);
        this.domainUser.setProfile(LdapUtilsTest.USER_ORIGINAL_ROLE);
    }



    @Test
    @DisplayName("Update users with different logins")
    void updateFromLdapWithDifferentLogins() {
        LdapUser ldapUserWithDifferentLogin = new LdapUser(LdapUtilsTest.OTHER_LOGIN, LdapUtilsTest.USER_ORIGINAL_NAME,
                                                           LdapUtilsTest.EMAIL_IN_USE, LdapUtilsTest.USER_ORIGINAL_ROLE);
        assertThrows(IllegalArgumentException.class, () -> {
            LdapUtils.updateFromLdap(this.domainUser, ldapUserWithDifferentLogin, this.usersRepository);
        });
    }


    @Test
    @DisplayName("Update the user with an e-mail address that is already in use")
    void updateFromLdapWithUnavailableEmail() {
        LdapUser ldapUserWithUnavailableEmail = new LdapUser(LdapUtilsTest.USER_LOGIN, LdapUtilsTest.USER_ORIGINAL_NAME,
                                                             LdapUtilsTest.EMAIL_IN_USE, LdapUtilsTest.USER_ORIGINAL_ROLE);
        boolean modified = LdapUtils.updateFromLdap(this.domainUser, ldapUserWithUnavailableEmail, this.usersRepository);
        assertFalse(modified);
        assertEquals(LdapUtilsTest.USER_ORIGINAL_EMAIL, this.domainUser.getEmail());
    }



    @Test
    @DisplayName("Update the user with the same property values")
    void updateFromLdapWithNoChange() {
        LdapUser identicalLdapUser = new LdapUser(LdapUtilsTest.USER_LOGIN, LdapUtilsTest.USER_ORIGINAL_NAME,
                                                  LdapUtilsTest.USER_ORIGINAL_EMAIL, LdapUtilsTest.USER_ORIGINAL_ROLE,
                                                  LdapUtilsTest.USER_ORIGINAL_ACTIVE);
        boolean modified = LdapUtils.updateFromLdap(this.domainUser, identicalLdapUser, this.usersRepository);
        assertFalse(modified);
        assertEquals(LdapUtilsTest.USER_ORIGINAL_NAME, domainUser.getName());
        assertEquals(LdapUtilsTest.USER_ORIGINAL_EMAIL, domainUser.getEmail());
        assertEquals(LdapUtilsTest.USER_ORIGINAL_ROLE, domainUser.getProfile());
        assertEquals(LdapUtilsTest.USER_ORIGINAL_ACTIVE, domainUser.isActive());
    }



    @Test
    @DisplayName("Update the user changing all the properties")
    void updateFromLdapWithAllChanges() {
        LdapUser modifiedLdapUser = new LdapUser(LdapUtilsTest.USER_LOGIN, LdapUtilsTest.USER_MODIFIED_NAME,
                                                 LdapUtilsTest.USER_MODIFIED_EMAIL, LdapUtilsTest.USER_MODIFIED_ROLE,
                                                 LdapUtilsTest.USER_MODIFIED_ACTIVE);
        boolean modified = LdapUtils.updateFromLdap(this.domainUser, modifiedLdapUser, this.usersRepository);
        assertTrue(modified);
        assertEquals(LdapUtilsTest.USER_MODIFIED_NAME, domainUser.getName());
        assertEquals(LdapUtilsTest.USER_MODIFIED_EMAIL, domainUser.getEmail());
        assertEquals(LdapUtilsTest.USER_MODIFIED_ROLE, domainUser.getProfile());
        assertEquals(LdapUtilsTest.USER_MODIFIED_ACTIVE, domainUser.isActive());
    }



    @Test
    @DisplayName("Update the user with a disabled LDAP user")
    void updateFromLdapDisable() {
        LdapUser identicalLdapUser = new LdapUser(LdapUtilsTest.USER_LOGIN, LdapUtilsTest.USER_ORIGINAL_NAME,
                                                  LdapUtilsTest.USER_ORIGINAL_EMAIL, LdapUtilsTest.USER_ORIGINAL_ROLE,
                                                  LdapUtilsTest.USER_MODIFIED_ACTIVE);
        boolean modified = LdapUtils.updateFromLdap(this.domainUser, identicalLdapUser, this.usersRepository);
        assertTrue(modified);
        assertEquals(LdapUtilsTest.USER_ORIGINAL_NAME, domainUser.getName());
        assertEquals(LdapUtilsTest.USER_ORIGINAL_EMAIL, domainUser.getEmail());
        assertEquals(LdapUtilsTest.USER_ORIGINAL_ROLE, domainUser.getProfile());
        assertEquals(LdapUtilsTest.USER_MODIFIED_ACTIVE, domainUser.isActive());
    }

}
