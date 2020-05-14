package eu.arrowhead.common.database.repository.mscv;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.common.database.entity.mscv.VerificationEntry;
import eu.arrowhead.common.database.entity.mscv.VerificationResult;
import eu.arrowhead.common.database.entity.mscv.VerificationResultDetail;
import eu.arrowhead.common.database.view.mscv.VerificationExecutionDetailView;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationExecutionDetailRepository extends RefreshableRepository<VerificationResultDetail, Long> {

    VerificationExecutionDetailView findViewById(final Long id);

    VerificationExecutionDetailView findViewByExecutionAndVerificationEntry(final VerificationResult execution,
                                                                            final VerificationEntry entry);
}
