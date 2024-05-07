package ch.asit_asso.extract.unit.ldap;

import java.util.ArrayList;
import java.util.List;
import ch.asit_asso.extract.domain.User.Profile;
import ch.asit_asso.extract.ldap.LdapUser;
import ch.asit_asso.extract.ldap.LdapUsersCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LdapUsersCollectionTest {

    private static final String EXISTING_USER_EMAIL = "user@mondomaine.ch";

    private static final String EXISTING_USER_LOGIN = "user";

    private static final String EXISTING_USER_NAME = "Utilisateur existant";

    private static final String NEW_USER_EMAIL = "new.user@mondomaine.ch";

    private static final String NEW_USER_LOGIN = "user2";

    private static final String NEW_USER_NAME = "Nouvel utilisateur";

    private LdapUsersCollection ldapUsersCollection;



    @BeforeEach
    void setUp() {

        this.ldapUsersCollection = new LdapUsersCollection();
    }



    @Test
    @DisplayName("Adds an existing user with a downgraded profile")
    void addExistingDowngradedUser() {

        LdapUser existingUser = new LdapUser(LdapUsersCollectionTest.EXISTING_USER_LOGIN,
                                             LdapUsersCollectionTest.EXISTING_USER_NAME,
                                             LdapUsersCollectionTest.EXISTING_USER_EMAIL, Profile.ADMIN);
        this.ldapUsersCollection.add(existingUser);
        LdapUser newUser = new LdapUser(LdapUsersCollectionTest.EXISTING_USER_LOGIN,
                                        LdapUsersCollectionTest.EXISTING_USER_NAME,
                                        LdapUsersCollectionTest.EXISTING_USER_EMAIL, Profile.OPERATOR);
        boolean added = this.ldapUsersCollection.add(newUser);
        assertFalse(added);
        assertEquals(1, this.ldapUsersCollection.size());
    }



    @Test
    @DisplayName("Adds an existing user with an upgraded profile")
    void addExistingUpgradedUser() {

        LdapUser existingUser = new LdapUser(LdapUsersCollectionTest.EXISTING_USER_LOGIN,
                                             LdapUsersCollectionTest.EXISTING_USER_NAME,
                                             LdapUsersCollectionTest.EXISTING_USER_EMAIL, Profile.OPERATOR);
        this.ldapUsersCollection.add(existingUser);
        LdapUser newUser = new LdapUser(LdapUsersCollectionTest.EXISTING_USER_LOGIN,
                                        LdapUsersCollectionTest.EXISTING_USER_NAME,
                                        LdapUsersCollectionTest.EXISTING_USER_EMAIL, Profile.ADMIN);
        boolean added = this.ldapUsersCollection.add(newUser);
        assertTrue(added);
        assertEquals(1, this.ldapUsersCollection.size());
        assertEquals(Profile.ADMIN, this.ldapUsersCollection.iterator().next().getRole());
    }



    @Test
    @DisplayName("Adds a new user")
    void addNewUser() {

        LdapUser existingUser = new LdapUser(LdapUsersCollectionTest.EXISTING_USER_LOGIN,
                                             LdapUsersCollectionTest.EXISTING_USER_NAME,
                                             LdapUsersCollectionTest.EXISTING_USER_EMAIL, Profile.OPERATOR);
        this.ldapUsersCollection.add(existingUser);
        LdapUser newUser = new LdapUser(LdapUsersCollectionTest.NEW_USER_LOGIN,
                                        LdapUsersCollectionTest.NEW_USER_NAME,
                                        LdapUsersCollectionTest.NEW_USER_EMAIL, Profile.OPERATOR);
        boolean added = this.ldapUsersCollection.add(newUser);
        assertTrue(added);
        assertEquals(2, this.ldapUsersCollection.size());
    }



    @Test
    @DisplayName("Adds an existing user with the same profile")
    void addSimilarUser() {

        LdapUser existingUser = new LdapUser(LdapUsersCollectionTest.EXISTING_USER_LOGIN,
                                             LdapUsersCollectionTest.EXISTING_USER_NAME,
                                             LdapUsersCollectionTest.EXISTING_USER_EMAIL, Profile.OPERATOR);
        this.ldapUsersCollection.add(existingUser);
        LdapUser newUser = new LdapUser(LdapUsersCollectionTest.EXISTING_USER_LOGIN,
                                        LdapUsersCollectionTest.EXISTING_USER_NAME,
                                        LdapUsersCollectionTest.EXISTING_USER_EMAIL, Profile.OPERATOR);
        boolean added = this.ldapUsersCollection.add(newUser);
        assertFalse(added);
        assertEquals(1, this.ldapUsersCollection.size());
    }



    @Test
    @DisplayName("Adds a null user")
    void addNull() {

        assertThrows(IllegalArgumentException.class, () -> {
            this.ldapUsersCollection.add(null);
        });
    }



    @Test
    @DisplayName("Add a list of valid and invalid users")
    void addAll() {

        LdapUser firstUser = new LdapUser(LdapUsersCollectionTest.EXISTING_USER_LOGIN,
                                          LdapUsersCollectionTest.EXISTING_USER_NAME,
                                          LdapUsersCollectionTest.EXISTING_USER_EMAIL, Profile.OPERATOR);
        LdapUser sameUser = new LdapUser(LdapUsersCollectionTest.EXISTING_USER_LOGIN,
                                         LdapUsersCollectionTest.EXISTING_USER_NAME,
                                         LdapUsersCollectionTest.EXISTING_USER_EMAIL, Profile.OPERATOR);
        LdapUser upgradedUser = new LdapUser(LdapUsersCollectionTest.EXISTING_USER_LOGIN,
                                             LdapUsersCollectionTest.EXISTING_USER_NAME,
                                             LdapUsersCollectionTest.EXISTING_USER_EMAIL, Profile.ADMIN);
        LdapUser newUser = new LdapUser(LdapUsersCollectionTest.NEW_USER_LOGIN, LdapUsersCollectionTest.NEW_USER_NAME,
                                        LdapUsersCollectionTest.NEW_USER_EMAIL, Profile.ADMIN);
        LdapUser downgradedUser = new LdapUser(LdapUsersCollectionTest.NEW_USER_LOGIN,
                                               LdapUsersCollectionTest.NEW_USER_NAME,
                                               LdapUsersCollectionTest.NEW_USER_EMAIL, Profile.OPERATOR);

        List<LdapUser> usersToAdd = new ArrayList<>();
        usersToAdd.add(firstUser);
        usersToAdd.add(null);
        usersToAdd.add(sameUser);
        usersToAdd.add(upgradedUser);
        usersToAdd.add(newUser);
        usersToAdd.add(downgradedUser);
        boolean modified = this.ldapUsersCollection.addAll(usersToAdd);
        assertTrue(modified);
        assertEquals(2, this.ldapUsersCollection.size());
    }



    @Test
    @DisplayName("Add a list of invalid users")
    void addAllInvalid() {

        LdapUser existingUser = new LdapUser(LdapUsersCollectionTest.EXISTING_USER_LOGIN,
                                             LdapUsersCollectionTest.EXISTING_USER_NAME,
                                             LdapUsersCollectionTest.EXISTING_USER_EMAIL, Profile.OPERATOR);
        this.ldapUsersCollection.add(existingUser);
        LdapUser sameUser = new LdapUser(LdapUsersCollectionTest.EXISTING_USER_LOGIN,
                                          LdapUsersCollectionTest.EXISTING_USER_NAME,
                                          LdapUsersCollectionTest.EXISTING_USER_EMAIL, Profile.OPERATOR);
        List<LdapUser> usersToAdd = new ArrayList<>();
        usersToAdd.add(sameUser);
        usersToAdd.add(null);
        boolean modified = this.ldapUsersCollection.addAll(usersToAdd);
        assertFalse(modified);
        assertEquals(1, this.ldapUsersCollection.size());
    }
}
