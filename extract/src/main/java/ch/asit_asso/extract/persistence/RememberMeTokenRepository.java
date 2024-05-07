package ch.asit_asso.extract.persistence;

import java.util.Collection;
import ch.asit_asso.extract.domain.RememberMeToken;
import ch.asit_asso.extract.domain.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface RememberMeTokenRepository extends PagingAndSortingRepository<RememberMeToken, Integer> {

    int deleteByUser(@NotNull User user);

    Collection<RememberMeToken> getExpiredTokens(@NotNull @Param("user") User user);

    Collection<RememberMeToken> getValidTokens(@NotNull @Param("user") User user);
}
