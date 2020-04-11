package eu.arrowhead.core.msvc.database.repositories;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.core.msvc.database.entities.VerificationExecution;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationExecutionRepository extends RefreshableRepository<VerificationExecution, Long> {
}
