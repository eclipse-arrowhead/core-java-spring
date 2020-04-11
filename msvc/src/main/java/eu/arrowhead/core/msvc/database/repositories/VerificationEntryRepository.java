package eu.arrowhead.core.msvc.database.repositories;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.core.msvc.database.entities.VerificationEntry;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationEntryRepository extends RefreshableRepository<VerificationEntry, Long> {
}
