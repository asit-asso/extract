package ch.asit_asso.extract.services;

import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.interceptors.AppInitializationInterceptor;
import ch.asit_asso.extract.persistence.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@CacheConfig(cacheNames = "setup")
public class AppInitializationService {

    private final UsersRepository repository;

    private static Logger logger = LoggerFactory.getLogger(AppInitializationService.class);

    public AppInitializationService(UsersRepository repository) {
        this.repository = repository;
    }

    @Cacheable(sync = true)
    public boolean isConfigured() {
        logger.info("Checking if configured !");
        Arrays.stream(repository.findByProfileAndActiveTrue(User.Profile.ADMIN))
                .toList().forEach(user -> logger.info("User {} is an admin", user.getName()));
        return repository.existsByProfile(User.Profile.ADMIN);
    }

    @CacheEvict(allEntries = true)
    public void notifyAdminCreated() {
        logger.info("Cleaning up !");
    }
}
