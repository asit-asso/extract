package ch.asit_asso.extract.unit.persistance;

import ch.asit_asso.extract.domain.RecoveryCode;
import ch.asit_asso.extract.persistence.RecoveryCodeRepository;
import org.jetbrains.annotations.NotNull;

public class RecoveryCodeRepositoryStub extends JpaRepositoryStub<RecoveryCode> implements RecoveryCodeRepository {

    @Override
    public @NotNull Integer getEntityId(@NotNull RecoveryCode entity) {

        return entity.getId();
    }



    @Override
    public void setEntityId(@NotNull RecoveryCode entity, @NotNull Integer newId) {
        entity.setId(newId);
    }
}
