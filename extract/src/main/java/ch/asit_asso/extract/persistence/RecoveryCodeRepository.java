package ch.asit_asso.extract.persistence;

import ch.asit_asso.extract.domain.RecoveryCode;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RecoveryCodeRepository extends PagingAndSortingRepository<RecoveryCode, Integer> {
}
