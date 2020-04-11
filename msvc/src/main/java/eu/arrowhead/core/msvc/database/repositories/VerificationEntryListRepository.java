package eu.arrowhead.core.msvc.database.repositories;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.core.msvc.database.entities.VerificationEntryList;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationEntryListRepository extends RefreshableRepository<VerificationEntryList, Long> {
}
