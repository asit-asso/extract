package ch.asit_asso.extract.unit.authentication.twofactor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import ch.asit_asso.extract.authentication.twofactor.TwoFactorBackupCodes;
import ch.asit_asso.extract.domain.RecoveryCode;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.persistence.RecoveryCodeRepository;
import ch.asit_asso.extract.unit.MockEnabledTest;
import ch.asit_asso.extract.unit.persistance.RecoveryCodeRepositoryStub;
import ch.asit_asso.extract.utils.Secrets;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

class TwoFactorBackupCodesTest extends MockEnabledTest {

    public static final int NUMBER_OF_GENERATED_CODES = 6;

    private RecoveryCodeRepository codeRepository;

    @Mock
    private Secrets secrets;

    private TwoFactorBackupCodes twoFactorBackupCodes;

    private User user;



    @BeforeEach
    void setUp() {
        this.codeRepository = new RecoveryCodeRepositoryStub();
        this.user = new User(1);
        this.user.setTwoFactorRecoveryCodesCollection(new ArrayList<>());

        Mockito.when(this.secrets.hash(anyString())).thenAnswer(
                (Answer<String>) invocationOnMock -> invocationOnMock.getArgument(0)
        );

        Mockito.when(this.secrets.check(anyString(), anyString())).thenAnswer(
                (Answer<Boolean>) invocationOnMock -> Objects.equals(invocationOnMock.getArgument(0),
                                                                     invocationOnMock.getArgument(1))
        );

        this.twoFactorBackupCodes = new TwoFactorBackupCodes(this.user, this.codeRepository, this.secrets);
    }



    @Test
    @DisplayName("Delete the existing recovery codes of a user")
    void delete() {
        User otherUser = new User(2);
        this.setRepositoryContentForUsers(this.user, otherUser);

        this.twoFactorBackupCodes.delete();

        long userCodesNumber = this.countUserCodes(this.user);
        assertEquals(0, userCodesNumber);
        assertEquals(TwoFactorBackupCodesTest.NUMBER_OF_GENERATED_CODES, this.codeRepository.count());
    }



    @Test
    @DisplayName("Delete the existing recovery codes of a user when no code exists for this user")
    void deleteNothingSetForUser() {
        User otherUser = new User(2);
        this.setRepositoryContentForUsers(otherUser);

        this.twoFactorBackupCodes.delete();

        int userCodesNumber = this.countUserCodes(this.user);
        assertEquals(0, userCodesNumber);
        assertEquals(TwoFactorBackupCodesTest.NUMBER_OF_GENERATED_CODES, this.codeRepository.count());
    }



    @Test
    @DisplayName("Delete the existing recovery codes of a user when no code exists")
    void deleteNothingSet() {
        this.twoFactorBackupCodes.delete();

        long userCodesNumber = this.countUserCodes(this.user);
        assertEquals(0, userCodesNumber);
        assertEquals(0, this.codeRepository.count());
    }



    @Test
    @DisplayName("Generate a set of recovery codes for a user when non exists for this user")
    void generate() {
        User otherUser = new User(2);
        this.setRepositoryContentForUsers(otherUser);

        List<String> newCodes = Arrays.stream(this.twoFactorBackupCodes.generate()).toList();

        Mockito.verify(this.secrets, Mockito.times(TwoFactorBackupCodesTest.NUMBER_OF_GENERATED_CODES)).hash(anyString());
        List<String> usersTokens = this.getUserTokens(this.user);
        long userCodesNumber = usersTokens.size();
        assertEquals(TwoFactorBackupCodesTest.NUMBER_OF_GENERATED_CODES, userCodesNumber);
        assertEquals(TwoFactorBackupCodesTest.NUMBER_OF_GENERATED_CODES * 2, this.codeRepository.count());
        // The secrets hash method has been stubbed for this test to return the same string, so no need to check the hash
        assertTrue(usersTokens.containsAll(newCodes) && newCodes.containsAll(usersTokens),
                   "The list of generated tokens does not match those in the repository (hash notwithstanding).");
    }



    @Test
    @DisplayName("Generate a set of recovery codes for a user with already existing ones")
    void generateAndPurge() {
        User otherUser = new User(2);
        this.setRepositoryContentForUsers(this.user, otherUser);
        List<String> originalTokens = this.getUserTokens(this.user);

        List<String> newCodes = Arrays.stream(this.twoFactorBackupCodes.generate()).toList();

        Mockito.verify(this.secrets, Mockito.times(TwoFactorBackupCodesTest.NUMBER_OF_GENERATED_CODES)).hash(anyString());
        List<String> usersTokens = this.getUserTokens(this.user);
        long userCodesNumber = usersTokens.size();
        assertEquals(TwoFactorBackupCodesTest.NUMBER_OF_GENERATED_CODES, userCodesNumber);
        assertEquals(TwoFactorBackupCodesTest.NUMBER_OF_GENERATED_CODES * 2, this.codeRepository.count());
        // The secrets hash method has been stubbed for this test to return the same string, so no need to check the hash
        assertTrue(usersTokens.containsAll(newCodes) && newCodes.containsAll(usersTokens),
                   "The list of generated tokens does not match those in the repository (hash notwithstanding).");
        assertTrue(usersTokens.stream().noneMatch(token -> originalTokens.contains(token)),
                   "Some existing tokens have not been purged during the new recovery codes generation.");

    }



    @Test
    @DisplayName("Submit a valid recovery code")
    void submitExistingCode() {
        User otherUser = new User(2);
        this.setRepositoryContentForUsers(this.user, otherUser);
        String validToken = this.getUserTokens(this.user)
                                .get(ThreadLocalRandom.current()
                                                      .nextInt(0, TwoFactorBackupCodesTest.NUMBER_OF_GENERATED_CODES));

        boolean isCodeValid = this.twoFactorBackupCodes.submitCode(validToken);

        List<String> userTokens = this.getUserTokens(this.user);
        assertTrue(isCodeValid);
        Mockito.verify(this.secrets, Mockito.atLeastOnce()).check(eq(validToken), anyString());
        assertEquals(TwoFactorBackupCodesTest.NUMBER_OF_GENERATED_CODES - 1, userTokens.size());
        // The secrets hash method has been stubbed for this test to return the same string, so no need to check the hash
        assertFalse(userTokens.contains(validToken), "The submitted token is still present in the repository.");
    }



    @Test
    @DisplayName("Submit an invalid recovery code")
    void submitInvalidCode() {
        User otherUser = new User(2);
        this.setRepositoryContentForUsers(this.user, otherUser);
        String invalidToken = "This token is obviously invalid.";

        boolean isCodeValid = this.twoFactorBackupCodes.submitCode(invalidToken);

        List<String> userTokens = this.getUserTokens(this.user);
        assertFalse(isCodeValid);
        Mockito.verify(this.secrets, Mockito.atLeastOnce()).check(eq(invalidToken), anyString());
        assertEquals(TwoFactorBackupCodesTest.NUMBER_OF_GENERATED_CODES, userTokens.size());
    }



    @Test
    @DisplayName("Submit a recovery code attributed to a different user")
    void submitExistingCodeForDifferentUser() {
        User otherUser = new User(2);
        this.setRepositoryContentForUsers(this.user, otherUser);
        String otherUserToken = this.getUserTokens(otherUser).get(
                ThreadLocalRandom.current().nextInt(0, TwoFactorBackupCodesTest.NUMBER_OF_GENERATED_CODES)
        );

        boolean isCodeValid = this.twoFactorBackupCodes.submitCode(otherUserToken);

        List<String> userTokens = this.getUserTokens(this.user);
        List<String> otherUserTokens = this.getUserTokens(otherUser);
        assertFalse(isCodeValid);
        Mockito.verify(this.secrets, Mockito.atLeastOnce()).check(eq(otherUserToken), anyString());
        assertEquals(TwoFactorBackupCodesTest.NUMBER_OF_GENERATED_CODES, userTokens.size());
        assertEquals(TwoFactorBackupCodesTest.NUMBER_OF_GENERATED_CODES, otherUserTokens.size());
        // The secrets hash method has been stubbed for this test to return the same string, so no need to check the hash
        assertFalse(userTokens.contains(otherUserToken),
                    "The submitted token has been added for the current user in the repository.");
        assertTrue(otherUserTokens.contains(otherUserToken),
                   "The submitted token has been removed from the repository.");
    }



    private int countUserCodes(User user) {
        return this.getUserTokens(user).size();
    }



    private List<String> getUserTokens(User user) {
        List<RecoveryCode> codes = (List<RecoveryCode>) this.codeRepository.findAll();

        return codes.stream()
                    .filter(code -> user.getId() == code.getUser().getId())
                    .map(code -> code.getToken())
                    .toList();
    }



    private void setRepositoryContentForUsers(User... users) {

        List<RecoveryCode> codesRepositoryContent = new ArrayList<>();

        for (User user : users) {

            for (int codeCounter = 0; codeCounter < TwoFactorBackupCodesTest.NUMBER_OF_GENERATED_CODES; codeCounter++) {
                RecoveryCode code = new RecoveryCode();
                code.setToken(new StringBuilder(RandomStringUtils.randomAlphanumeric(12).toUpperCase()).toString());
                code.setUser(user);
                codesRepositoryContent.add(code);
            }
        }

        this.codeRepository.saveAll(codesRepositoryContent);
        this.updateUsersTokens(users);
        Mockito.clearInvocations(this.secrets);
    }

    private void updateUsersTokens(User... users) {
        List<RecoveryCode> codes = (List<RecoveryCode>) this.codeRepository.findAll();

        for (User user : users) {
            user.setTwoFactorRecoveryCodesCollection(codes.stream()
                                                          .filter(code -> code.getUser().getId() == user.getId())
                                                          .toList());
        }
    }
}
