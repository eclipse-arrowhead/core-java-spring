package eu.arrowhead.common.database.repository.mscv;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.common.database.entity.mscv.VerificationEntry;
import eu.arrowhead.common.database.entity.mscv.VerificationExecution;
import eu.arrowhead.common.database.entity.mscv.VerificationExecutionDetail;
import eu.arrowhead.common.database.view.mscv.VerificationExecutionDetailView;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationExecutionDetailRepository extends RefreshableRepository<VerificationExecutionDetail, Long> {

    VerificationExecutionDetailView findViewById(final Long id);

    VerificationExecutionDetailView findViewByExecutionAndVerificationEntry(final VerificationExecution execution,
                                                                            final VerificationEntry entry);
}
