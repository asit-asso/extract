package ch.asit_asso.extract.unit.services;

import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.persistence.UsersRepository;
import ch.asit_asso.extract.services.AppInitializationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class AppInitializationServiceTest {

    @Test
    @DisplayName("Check that it returns true when admin is present")
    void verifyThatAdminIsConfigured() {
        UsersRepository repository = Mockito.mock(UsersRepository.class);
        when(repository.existsByProfile(User.Profile.ADMIN)).thenReturn(true);
        AppInitializationService service = new AppInitializationService(repository);
        assertTrue(service.isConfigured());
    }
    @Test
    @DisplayName("Check that it returns false when admin is not present")
    void verifyThatAdminIsNotConfigured() {
        UsersRepository repository = Mockito.mock(UsersRepository.class);
        when(repository.existsByProfile(User.Profile.ADMIN)).thenReturn(false);
        AppInitializationService service = new AppInitializationService(repository);
        assertFalse(service.isConfigured());
    }
}