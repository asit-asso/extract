package ch.asit_asso.extract.unit.ldap;

import javax.naming.NamingException;
import javax.naming.directory.BasicAttribute;
import ch.asit_asso.extract.domain.User.Profile;
import ch.asit_asso.extract.ldap.LdapSettings;
import ch.asit_asso.extract.ldap.LdapUser;
import ch.asit_asso.extract.ldap.LdapUserAttributesMapper;
import ch.asit_asso.extract.unit.MockEnabledTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.ldap.core.LdapAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LdapUserAttributesMapperTest extends MockEnabledTest {

    private static final String ACTIVE_ATTRIBUTE_NAME = "userAccountControl";

    private static final String ADMINS_GROUP_NAME = "admins_extract";

    private static final String DUMMY_GROUP_NAME = "autre_groupe";

    private static final String EMAIL_ATTRIBUTE_NAME = "email";

    private static final String GROUPS_ATTRIBUTE_NAME = "memberOf";

    //private static final String INACTIVE_ACCOUNT_HEXADECIMAL_VALUE = "010010";

    private static final String INACTIVE_ACCOUNT_VALUE = "18";

    private static final String LOGIN_ATTRIBUTE_NAME = "sAMAccountName";

    private static final String NAME_ATTRIBUTE_NAME = "cn";

    private static final String OPERATORS_GROUP_NAME = "operateurs_extract";

    private static final String USER_EMAIL = "user@mondomaine.ch";

    private static final String USER_LOGIN = "user";

    private static final String USER_NAME = "Utilisateur Test";


    
    private LdapAttributes attributes;

    private LdapUserAttributesMapper mapper;

    @Mock
    private LdapSettings settings;



    @BeforeEach
    void setUp() {
        Mockito.when(settings.getAdminsGroup()).thenReturn(LdapUserAttributesMapperTest.ADMINS_GROUP_NAME);
        Mockito.when(settings.getOperatorsGroup()).thenReturn(LdapUserAttributesMapperTest.OPERATORS_GROUP_NAME);
        Mockito.when(settings.getMailAttribute()).thenReturn(LdapUserAttributesMapperTest.EMAIL_ATTRIBUTE_NAME);
        Mockito.when(settings.getLoginAttribute()).thenReturn(LdapUserAttributesMapperTest.LOGIN_ATTRIBUTE_NAME);
        Mockito.when(settings.getUserNameAttribute()).thenReturn(LdapUserAttributesMapperTest.NAME_ATTRIBUTE_NAME);

        this.mapper = new LdapUserAttributesMapper(this.settings);

        this.attributes = new LdapAttributes(true);
        this.attributes.put(LdapUserAttributesMapperTest.LOGIN_ATTRIBUTE_NAME, LdapUserAttributesMapperTest.USER_LOGIN);
        this.attributes.put(LdapUserAttributesMapperTest.NAME_ATTRIBUTE_NAME, LdapUserAttributesMapperTest.USER_NAME);
        this.attributes.put(LdapUserAttributesMapperTest.EMAIL_ATTRIBUTE_NAME, LdapUserAttributesMapperTest.USER_EMAIL);
        this.attributes.put(LdapUserAttributesMapperTest.ACTIVE_ATTRIBUTE_NAME, "0");
    }



    @Test
    @DisplayName("Parse admin")
    void mapFromAdminAttributes() throws NamingException {
        BasicAttribute groupsAttribute = new BasicAttribute(LdapUserAttributesMapperTest.GROUPS_ATTRIBUTE_NAME);
        groupsAttribute.add(LdapUserAttributesMapperTest.ADMINS_GROUP_NAME);
        groupsAttribute.add(LdapUserAttributesMapperTest.DUMMY_GROUP_NAME);
        this.attributes.put(groupsAttribute);

        LdapUser resultingUser = this.mapper.mapFromAttributes(this.attributes);

        assertNotNull(resultingUser);
        assertEquals(LdapUserAttributesMapperTest.USER_LOGIN, resultingUser.getLogin());
        assertEquals(LdapUserAttributesMapperTest.USER_NAME, resultingUser.getName());
        assertEquals(LdapUserAttributesMapperTest.USER_EMAIL, resultingUser.getEmail());
        assertTrue(resultingUser.isActive());
        assertEquals(Profile.ADMIN, resultingUser.getRole());
    }



    @Test
    @DisplayName("Parse admin and operator")
    void mapFromAdminAndOperatorAttributes() throws NamingException {
        BasicAttribute groupsAttribute = new BasicAttribute(LdapUserAttributesMapperTest.GROUPS_ATTRIBUTE_NAME);
        groupsAttribute.add(LdapUserAttributesMapperTest.ADMINS_GROUP_NAME);
        groupsAttribute.add(LdapUserAttributesMapperTest.DUMMY_GROUP_NAME);
        groupsAttribute.add(LdapUserAttributesMapperTest.OPERATORS_GROUP_NAME);
        this.attributes.put(groupsAttribute);

        LdapUser resultingUser = this.mapper.mapFromAttributes(this.attributes);

        assertNotNull(resultingUser);
        assertEquals(LdapUserAttributesMapperTest.USER_LOGIN, resultingUser.getLogin());
        assertEquals(LdapUserAttributesMapperTest.USER_NAME, resultingUser.getName());
        assertEquals(LdapUserAttributesMapperTest.USER_EMAIL, resultingUser.getEmail());
        assertTrue(resultingUser.isActive());
        assertEquals(Profile.ADMIN, resultingUser.getRole());
    }


    @Test
    @DisplayName("Parse inactive user attributes")
    void mapFromInactiveAttributes() throws NamingException {
        BasicAttribute groupsAttribute = new BasicAttribute(LdapUserAttributesMapperTest.GROUPS_ATTRIBUTE_NAME);
        groupsAttribute.add(LdapUserAttributesMapperTest.DUMMY_GROUP_NAME);
        groupsAttribute.add(LdapUserAttributesMapperTest.OPERATORS_GROUP_NAME);
        this.attributes.put(groupsAttribute);
        this.attributes.put(LdapUserAttributesMapperTest.ACTIVE_ATTRIBUTE_NAME,
                            LdapUserAttributesMapperTest.INACTIVE_ACCOUNT_VALUE);

        LdapUser resultingUser = this.mapper.mapFromAttributes(this.attributes);

        assertNotNull(resultingUser);
        assertEquals(LdapUserAttributesMapperTest.USER_LOGIN, resultingUser.getLogin());
        assertEquals(LdapUserAttributesMapperTest.USER_NAME, resultingUser.getName());
        assertEquals(LdapUserAttributesMapperTest.USER_EMAIL, resultingUser.getEmail());
        assertFalse(resultingUser.isActive());
        assertEquals(Profile.OPERATOR, resultingUser.getRole());
    }


    @Test
    @DisplayName("Parse operator")
    void mapFromOperatorAttributes() throws NamingException {
        BasicAttribute groupsAttribute = new BasicAttribute(LdapUserAttributesMapperTest.GROUPS_ATTRIBUTE_NAME);
        groupsAttribute.add(LdapUserAttributesMapperTest.DUMMY_GROUP_NAME);
        groupsAttribute.add(LdapUserAttributesMapperTest.OPERATORS_GROUP_NAME);
        this.attributes.put(groupsAttribute);

        LdapUser resultingUser = this.mapper.mapFromAttributes(this.attributes);

        assertNotNull(resultingUser);
        assertEquals(LdapUserAttributesMapperTest.USER_LOGIN, resultingUser.getLogin());
        assertEquals(LdapUserAttributesMapperTest.USER_NAME, resultingUser.getName());
        assertEquals(LdapUserAttributesMapperTest.USER_EMAIL, resultingUser.getEmail());
        assertTrue(resultingUser.isActive());
        assertEquals(Profile.OPERATOR, resultingUser.getRole());
    }



    @Test
    @DisplayName("Parse neither admin nor operator")
    void mapFromNoRoleAttributes() throws NamingException {
        BasicAttribute groupsAttribute = new BasicAttribute(LdapUserAttributesMapperTest.GROUPS_ATTRIBUTE_NAME);
        groupsAttribute.add(LdapUserAttributesMapperTest.DUMMY_GROUP_NAME);
        this.attributes.put(groupsAttribute);

        LdapUser resultingUser = this.mapper.mapFromAttributes(this.attributes);

        assertNull(resultingUser);
    }



    @Test
    @DisplayName("Parse attributes with empty groups list")
    void mapFromNoGroupAttributes() throws NamingException {
        BasicAttribute groupsAttribute = new BasicAttribute(LdapUserAttributesMapperTest.GROUPS_ATTRIBUTE_NAME);
        this.attributes.put(groupsAttribute);

        LdapUser resultingUser = this.mapper.mapFromAttributes(this.attributes);

        assertNull(resultingUser);
    }

}
