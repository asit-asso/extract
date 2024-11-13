package ch.asit_asso.extract.services;

import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.persistence.UsersRepository;
import ch.asit_asso.extract.utils.Secrets;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UsersRepository usersRepository;
    private final Secrets secrets;

    public UserService(final UsersRepository usersRepository, final Secrets secrets)
    {
        this.usersRepository = usersRepository;
        this.secrets = secrets;
    }

    public User create(User user) {
        user.setPassword(secrets.hash(user.getPassword()));
        usersRepository.save(user);
        return user;
    }
}
