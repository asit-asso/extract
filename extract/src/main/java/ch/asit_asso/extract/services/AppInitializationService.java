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

    @Cacheable(sync = true)
    public boolean isConfigured() {
        return repository.existsByProfile(User.Profile.ADMIN);
    }
}
