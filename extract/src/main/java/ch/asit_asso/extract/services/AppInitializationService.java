package ch.asit_asso.extract.services;

import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.persistence.UsersRepository;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@CacheConfig(cacheNames = "setup")
public class AppInitializationService {

    private final UsersRepository repository;

    public AppInitializationService(UsersRepository repository) {
        this.repository = repository;
    }

    /**
     * Checks whether the application already has a real administrator.
     * <p>
     * The hidden system user is explicitly excluded: it is not an application account, and a schema update
     * may have given it the administrator profile. Counting it would make the application believe it is
     * configured and prevent the creation of the first administrator (issue #432).
     *
     * @return <code>true</code> if an administrator other than the system user exists
     */
    @Cacheable(sync = true)
    public boolean isConfigured() {
        return repository.existsByProfileAndLoginNot(User.Profile.ADMIN, User.SYSTEM_USER_LOGIN);
    }
}
