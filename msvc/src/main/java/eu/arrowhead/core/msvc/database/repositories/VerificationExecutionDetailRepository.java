package eu.arrowhead.core.msvc.database.repositories;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.core.msvc.database.entities.VerificationEntry;
import eu.arrowhead.core.msvc.database.entities.VerificationExecution;
import eu.arrowhead.core.msvc.database.entities.VerificationExecutionDetail;
import eu.arrowhead.core.msvc.database.view.VerificationExecutionDetailView;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationExecutionDetailRepository extends RefreshableRepository<VerificationExecutionDetail, Long> {

    VerificationExecutionDetailView findViewById(final Long id);

    VerificationExecutionDetailView findViewByExecutionAndVerificationEntry(final VerificationExecution execution,
                                                                            final VerificationEntry entry);
}
