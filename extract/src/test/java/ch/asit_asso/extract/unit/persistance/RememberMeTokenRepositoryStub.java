package ch.asit_asso.extract.unit.persistance;

import java.util.Calendar;
import java.util.Collection;
import ch.asit_asso.extract.domain.RememberMeToken;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.persistence.RememberMeTokenRepository;
import org.jetbrains.annotations.NotNull;

public class RememberMeTokenRepositoryStub extends JpaRepositoryStub<RememberMeToken>
        implements RememberMeTokenRepository {

    @Override
    public @NotNull Integer getEntityId(@NotNull RememberMeToken entity) {

        return entity.getId();
    }



    @Override
    public void setEntityId(@NotNull RememberMeToken entity, @NotNull Integer newId) {

        entity.setId(newId);
    }



    @Override
    public int deleteByUser(@NotNull User user) {
        Collection<RememberMeToken> userTokens = this.findByUser(user);
        boolean isModified = this.content.removeAll(userTokens);

        return (isModified) ? userTokens.size() : 0;
    }



    @Override
    public Collection<RememberMeToken> getExpiredTokens(@NotNull User user) {

        return this.findByUser(user).stream()
                                    .filter(token -> token.getTokenExpiration().before(Calendar.getInstance()))
                                    .toList();
    }



    @Override
    public Collection<RememberMeToken> getValidTokens(@NotNull User user) {

        return this.findByUser(user).stream()
                   .filter(token -> token.getTokenExpiration().compareTo(Calendar.getInstance()) >= 0)
                   .toList();
    }



    private Collection<RememberMeToken> findByUser(@NotNull User user) {
        int userId = user.getId();

        return this.content.stream()
                           .filter(token -> token.getUser().getId() == userId)
                           .toList();
    }
}
