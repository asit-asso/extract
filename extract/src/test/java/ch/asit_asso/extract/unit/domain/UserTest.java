package ch.asit_asso.extract.unit.domain;

import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.RecoveryCode;
import ch.asit_asso.extract.domain.RememberMeToken;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.domain.UserGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Entity Tests")
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor creates instance with null id")
        void defaultConstructor_createsInstanceWithNullId() {
            User newUser = new User();
            assertNull(newUser.getId());
        }

        @Test
        @DisplayName("Constructor with id sets the id correctly")
        void constructorWithId_setsIdCorrectly() {
            Integer expectedId = 42;
            User newUser = new User(expectedId);
            assertEquals(expectedId, newUser.getId());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("setId and getId work correctly")
        void setAndGetId() {
            Integer expectedId = 100;
            user.setId(expectedId);
            assertEquals(expectedId, user.getId());
        }

        @Test
        @DisplayName("setProfile and getProfile work correctly")
        void setAndGetProfile() {
            user.setProfile(User.Profile.ADMIN);
            assertEquals(User.Profile.ADMIN, user.getProfile());

            user.setProfile(User.Profile.OPERATOR);
            assertEquals(User.Profile.OPERATOR, user.getProfile());
        }

        @Test
        @DisplayName("setName and getName work correctly")
        void setAndGetName() {
            String expectedName = "John Doe";
            user.setName(expectedName);
            assertEquals(expectedName, user.getName());
        }

        @Test
        @DisplayName("setLogin and getLogin work correctly")
        void setAndGetLogin() {
            String expectedLogin = "johndoe";
            user.setLogin(expectedLogin);
            assertEquals(expectedLogin, user.getLogin());
        }

        @Test
        @DisplayName("setPassword and getPassword work correctly")
        void setAndGetPassword() {
            String expectedPassword = "hashedPassword123";
            user.setPassword(expectedPassword);
            assertEquals(expectedPassword, user.getPassword());
        }

        @Test
        @DisplayName("setEmail and getEmail work correctly")
        void setAndGetEmail() {
            String expectedEmail = "john@example.com";
            user.setEmail(expectedEmail);
            assertEquals(expectedEmail, user.getEmail());
        }

        @Test
        @DisplayName("setActive and isActive work correctly")
        void setAndIsActive() {
            user.setActive(true);
            assertTrue(user.isActive());

            user.setActive(false);
            assertFalse(user.isActive());
        }

        @Test
        @DisplayName("setMailActive and isMailActive work correctly")
        void setAndIsMailActive() {
            user.setMailActive(true);
            assertTrue(user.isMailActive());

            user.setMailActive(false);
            assertFalse(user.isMailActive());
        }

        @Test
        @DisplayName("setPasswordResetToken and getPasswordResetToken work correctly")
        void setAndGetPasswordResetToken() {
            String expectedToken = "reset-token-123";
            user.setPasswordResetToken(expectedToken);
            assertEquals(expectedToken, user.getPasswordResetToken());
        }

        @Test
        @DisplayName("setTokenExpiration and getTokenExpiration work correctly")
        void setAndGetTokenExpiration() {
            Calendar expectedDate = new GregorianCalendar(2024, Calendar.JANUARY, 15);
            user.setTokenExpiration(expectedDate);
            assertEquals(expectedDate, user.getTokenExpiration());
        }

        @Test
        @DisplayName("setTwoFactorForced and isTwoFactorForced work correctly")
        void setAndIsTwoFactorForced() {
            user.setTwoFactorForced(true);
            assertTrue(user.isTwoFactorForced());

            user.setTwoFactorForced(false);
            assertFalse(user.isTwoFactorForced());
        }

        @Test
        @DisplayName("setTwoFactorStatus and getTwoFactorStatus work correctly")
        void setAndGetTwoFactorStatus() {
            user.setTwoFactorStatus(User.TwoFactorStatus.ACTIVE);
            assertEquals(User.TwoFactorStatus.ACTIVE, user.getTwoFactorStatus());

            user.setTwoFactorStatus(User.TwoFactorStatus.INACTIVE);
            assertEquals(User.TwoFactorStatus.INACTIVE, user.getTwoFactorStatus());

            user.setTwoFactorStatus(User.TwoFactorStatus.STANDBY);
            assertEquals(User.TwoFactorStatus.STANDBY, user.getTwoFactorStatus());
        }

        @Test
        @DisplayName("setTwoFactorToken and getTwoFactorToken work correctly")
        void setAndGetTwoFactorToken() {
            String expectedToken = "2fa-token-123";
            user.setTwoFactorToken(expectedToken);
            assertEquals(expectedToken, user.getTwoFactorToken());
        }

        @Test
        @DisplayName("setTwoFactorStandbyToken and getTwoFactorStandbyToken work correctly")
        void setAndGetTwoFactorStandbyToken() {
            String expectedToken = "2fa-standby-token-456";
            user.setTwoFactorStandbyToken(expectedToken);
            assertEquals(expectedToken, user.getTwoFactorStandbyToken());
        }

        @Test
        @DisplayName("setUserType and getUserType work correctly")
        void setAndGetUserType() {
            user.setUserType(User.UserType.LOCAL);
            assertEquals(User.UserType.LOCAL, user.getUserType());

            user.setUserType(User.UserType.LDAP);
            assertEquals(User.UserType.LDAP, user.getUserType());
        }

        @Test
        @DisplayName("setLocale and getLocale work correctly")
        void setAndGetLocale() {
            user.setLocale("de");
            assertEquals("de", user.getLocale());
        }

        @Test
        @DisplayName("getLocale returns default fr when null")
        void getLocale_returnsDefaultWhenNull() {
            user.setLocale(null);
            assertEquals("fr", user.getLocale());
        }

        @Test
        @DisplayName("setProcessesCollection and getProcessesCollection work correctly")
        void setAndGetProcessesCollection() {
            Collection<Process> processes = new ArrayList<>();
            processes.add(new Process(1));
            processes.add(new Process(2));

            user.setProcessesCollection(processes);
            assertEquals(2, user.getProcessesCollection().size());
        }

        @Test
        @DisplayName("setUserGroupsCollection and getUserGroupsCollection work correctly")
        void setAndGetUserGroupsCollection() {
            Collection<UserGroup> groups = new ArrayList<>();
            groups.add(new UserGroup(1));
            groups.add(new UserGroup(2));

            user.setUserGroupsCollection(groups);
            assertEquals(2, user.getUserGroupsCollection().size());
        }

        @Test
        @DisplayName("setTwoFactorRecoveryCodesCollection and getTwoFactorRecoveryCodesCollection work correctly")
        void setAndGetTwoFactorRecoveryCodesCollection() {
            Collection<RecoveryCode> codes = new ArrayList<>();
            codes.add(new RecoveryCode(1));

            user.setTwoFactorRecoveryCodesCollection(codes);
            assertEquals(1, user.getTwoFactorRecoveryCodesCollection().size());
        }

        @Test
        @DisplayName("setRememberMeTokensCollection and getRememberMeTokensCollection work correctly")
        void setAndGetRememberMeTokensCollection() {
            Collection<RememberMeToken> tokens = new ArrayList<>();
            tokens.add(new RememberMeToken(1));

            user.setRememberMeTokensCollection(tokens);
            assertEquals(1, user.getRememberMeTokensCollection().size());
        }
    }

    @Nested
    @DisplayName("Profile and Admin Tests")
    class ProfileTests {

        @Test
        @DisplayName("isAdmin returns true for ADMIN profile")
        void isAdmin_returnsTrueForAdminProfile() {
            user.setProfile(User.Profile.ADMIN);
            assertTrue(user.isAdmin());
        }

        @Test
        @DisplayName("isAdmin returns false for OPERATOR profile")
        void isAdmin_returnsFalseForOperatorProfile() {
            user.setProfile(User.Profile.OPERATOR);
            assertFalse(user.isAdmin());
        }

        @Test
        @DisplayName("isAdmin returns false when profile is null")
        void isAdmin_returnsFalseWhenNull() {
            assertFalse(user.isAdmin());
        }
    }

    @Nested
    @DisplayName("System User Tests")
    class SystemUserTests {

        @Test
        @DisplayName("isSystemUser returns true for system login")
        void isSystemUser_returnsTrueForSystemLogin() {
            user.setLogin(User.SYSTEM_USER_LOGIN);
            assertTrue(user.isSystemUser());
        }

        @Test
        @DisplayName("isSystemUser returns false for other login")
        void isSystemUser_returnsFalseForOtherLogin() {
            user.setLogin("johndoe");
            assertFalse(user.isSystemUser());
        }

        @Test
        @DisplayName("SYSTEM_USER_LOGIN constant is correct")
        void systemUserLoginConstant_isCorrect() {
            assertEquals("system", User.SYSTEM_USER_LOGIN);
        }

        @Test
        @DisplayName("TOKEN_VALIDITY_IN_MINUTES constant is correct")
        void tokenValidityConstant_isCorrect() {
            assertEquals(20, User.TOKEN_VALIDITY_IN_MINUTES);
        }
    }

    @Nested
    @DisplayName("Password Reset Tests")
    class PasswordResetTests {

        @Test
        @DisplayName("cleanPasswordResetToken clears token and expiration")
        void cleanPasswordResetToken_clearsTokenAndExpiration() {
            user.setPasswordResetToken("some-token");
            user.setTokenExpiration(new GregorianCalendar());

            boolean result = user.cleanPasswordResetToken();

            assertTrue(result);
            assertNull(user.getPasswordResetToken());
            assertNull(user.getTokenExpiration());
        }

        @Test
        @DisplayName("cleanPasswordResetToken returns false when already clean")
        void cleanPasswordResetToken_returnsFalseWhenAlreadyClean() {
            boolean result = user.cleanPasswordResetToken();
            assertFalse(result);
        }

        @Test
        @DisplayName("setPasswordResetInfo sets token and expiration")
        void setPasswordResetInfo_setsTokenAndExpiration() {
            String token = "new-reset-token";
            user.setPasswordResetInfo(token);

            assertEquals(token, user.getPasswordResetToken());
            assertNotNull(user.getTokenExpiration());
        }

        @Test
        @DisplayName("setPasswordResetInfo throws exception for blank token")
        void setPasswordResetInfo_throwsExceptionForBlankToken() {
            assertThrows(IllegalArgumentException.class, () -> user.setPasswordResetInfo(""));
            assertThrows(IllegalArgumentException.class, () -> user.setPasswordResetInfo("   "));
            assertThrows(IllegalArgumentException.class, () -> user.setPasswordResetInfo(null));
        }
    }

    @Nested
    @DisplayName("Process Association Tests")
    class ProcessAssociationTests {

        @Test
        @DisplayName("isAssociatedToProcesses returns true when processes exist")
        void isAssociatedToProcesses_returnsTrueWhenProcessesExist() {
            user.setProcessesCollection(List.of(new Process(1)));
            assertTrue(user.isAssociatedToProcesses());
        }

        @Test
        @DisplayName("isAssociatedToProcesses returns false when no processes")
        void isAssociatedToProcesses_returnsFalseWhenNoProcesses() {
            user.setProcessesCollection(new ArrayList<>());
            assertFalse(user.isAssociatedToProcesses());
        }

        @Test
        @DisplayName("isAssociatedToProcesses returns false when null")
        void isAssociatedToProcesses_returnsFalseWhenNull() {
            assertFalse(user.isAssociatedToProcesses());
        }
    }

    @Nested
    @DisplayName("GetDistinctProcesses Tests")
    class GetDistinctProcessesTests {

        @Test
        @DisplayName("getDistinctProcesses returns direct processes")
        void getDistinctProcesses_returnsDirectProcesses() {
            Process process = new Process(1);
            user.setProcessesCollection(List.of(process));
            user.setUserGroupsCollection(new ArrayList<>());

            Collection<Process> result = user.getDistinctProcesses();
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("getDistinctProcesses includes processes from groups")
        void getDistinctProcesses_includesProcessesFromGroups() {
            Process process1 = new Process(1);
            Process process2 = new Process(2);

            UserGroup group = new UserGroup(1);
            group.setProcessesCollection(List.of(process2));

            user.setProcessesCollection(List.of(process1));
            user.setUserGroupsCollection(List.of(group));

            Collection<Process> result = user.getDistinctProcesses();
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("getDistinctProcesses removes duplicates")
        void getDistinctProcesses_removesDuplicates() {
            Process process = new Process(1);

            UserGroup group = new UserGroup(1);
            group.setProcessesCollection(List.of(process));

            user.setProcessesCollection(List.of(process));
            user.setUserGroupsCollection(List.of(group));

            Collection<Process> result = user.getDistinctProcesses();
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("IsLastActiveMemberOfProcessGroup Tests")
    class IsLastActiveMemberOfProcessGroupTests {

        @Test
        @DisplayName("returns false when no groups")
        void returnsFalseWhenNoGroups() {
            user.setUserGroupsCollection(new ArrayList<>());
            assertFalse(user.isLastActiveMemberOfProcessGroup());
        }

        @Test
        @DisplayName("returns false when group has no processes")
        void returnsFalseWhenGroupHasNoProcesses() {
            UserGroup group = new UserGroup(1);
            group.setProcessesCollection(new ArrayList<>());
            group.setUsersCollection(List.of(user));

            user.setUserGroupsCollection(List.of(group));

            assertFalse(user.isLastActiveMemberOfProcessGroup());
        }

        @Test
        @DisplayName("returns true when sole member of process group")
        void returnsTrueWhenSoleMemberOfProcessGroup() {
            user.setId(1);
            user.setActive(true);

            UserGroup group = new UserGroup(1);
            group.setProcessesCollection(List.of(new Process(1)));
            group.setUsersCollection(List.of(user));

            user.setUserGroupsCollection(List.of(group));

            assertTrue(user.isLastActiveMemberOfProcessGroup());
        }

        @Test
        @DisplayName("returns false when other active member exists")
        void returnsFalseWhenOtherActiveMemberExists() {
            user.setId(1);
            user.setActive(true);

            User otherUser = new User(2);
            otherUser.setActive(true);

            UserGroup group = new UserGroup(1);
            group.setProcessesCollection(List.of(new Process(1)));
            group.setUsersCollection(List.of(user, otherUser));

            user.setUserGroupsCollection(List.of(group));

            assertFalse(user.isLastActiveMemberOfProcessGroup());
        }

        @Test
        @DisplayName("returns true when other members are inactive")
        void returnsTrueWhenOtherMembersAreInactive() {
            user.setId(1);
            user.setActive(true);

            User inactiveUser = new User(2);
            inactiveUser.setActive(false);

            UserGroup group = new UserGroup(1);
            group.setProcessesCollection(List.of(new Process(1)));
            group.setUsersCollection(List.of(user, inactiveUser));

            user.setUserGroupsCollection(List.of(group));

            assertTrue(user.isLastActiveMemberOfProcessGroup());
        }
    }

    @Nested
    @DisplayName("Enum Tests")
    class EnumTests {

        @Test
        @DisplayName("Profile enum has all values")
        void profileEnum_hasAllValues() {
            User.Profile[] profiles = User.Profile.values();
            assertEquals(2, profiles.length);
            assertNotNull(User.Profile.ADMIN);
            assertNotNull(User.Profile.OPERATOR);
        }

        @Test
        @DisplayName("TwoFactorStatus enum has all values")
        void twoFactorStatusEnum_hasAllValues() {
            User.TwoFactorStatus[] statuses = User.TwoFactorStatus.values();
            assertEquals(3, statuses.length);
            assertNotNull(User.TwoFactorStatus.ACTIVE);
            assertNotNull(User.TwoFactorStatus.INACTIVE);
            assertNotNull(User.TwoFactorStatus.STANDBY);
        }

        @Test
        @DisplayName("UserType enum has all values")
        void userTypeEnum_hasAllValues() {
            User.UserType[] types = User.UserType.values();
            assertEquals(2, types.length);
            assertNotNull(User.UserType.LDAP);
            assertNotNull(User.UserType.LOCAL);
        }
    }

    @Nested
    @DisplayName("Equals, HashCode, and ToString Tests")
    class EqualsHashCodeToStringTests {

        @Test
        @DisplayName("equals returns true for same id")
        void equals_returnsTrueForSameId() {
            User user1 = new User(1);
            User user2 = new User(1);
            assertEquals(user1, user2);
        }

        @Test
        @DisplayName("equals returns false for different id")
        void equals_returnsFalseForDifferentId() {
            User user1 = new User(1);
            User user2 = new User(2);
            assertNotEquals(user1, user2);
        }

        @Test
        @DisplayName("equals returns false for null")
        void equals_returnsFalseForNull() {
            User user1 = new User(1);
            assertNotEquals(null, user1);
        }

        @Test
        @DisplayName("equals returns false for different type")
        void equals_returnsFalseForDifferentType() {
            User user1 = new User(1);
            assertNotEquals("not a user", user1);
        }

        @Test
        @DisplayName("hashCode is consistent for same id")
        void hashCode_isConsistentForSameId() {
            User user1 = new User(1);
            User user2 = new User(1);
            assertEquals(user1.hashCode(), user2.hashCode());
        }

        @Test
        @DisplayName("toString contains id")
        void toString_containsId() {
            User user1 = new User(42);
            String result = user1.toString();
            assertTrue(result.contains("42"));
            assertTrue(result.contains("idUser"));
        }
    }
}
