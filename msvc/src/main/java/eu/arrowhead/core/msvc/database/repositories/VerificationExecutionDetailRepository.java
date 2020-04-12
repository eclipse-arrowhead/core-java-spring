package eu.arrowhead.core.msvc.database.repositories;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.core.msvc.database.entities.VerificationEntry;
import eu.arrowhead.core.msvc.database.entities.VerificationExecution;
import eu.arrowhead.core.msvc.database.entities.VerificationExecutionDetail;
import eu.arrowhead.core.msvc.database.view.VerificationRunView;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationExecutionDetailRepository extends RefreshableRepository<VerificationExecutionDetail, Long> {

    VerificationRunView findViewById(final Long id);

    VerificationRunView findViewByExecutionAndVerificationEntry(final VerificationExecution execution,
                                                                final VerificationEntry entry);
}
