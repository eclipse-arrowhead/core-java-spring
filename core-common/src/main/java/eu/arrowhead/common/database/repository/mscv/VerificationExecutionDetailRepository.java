package eu.arrowhead.common.database.repository.mscv;

import java.util.List;

import eu.arrowhead.common.database.entity.mscv.VerificationResult;
import eu.arrowhead.common.database.entity.mscv.VerificationResultDetail;
import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.common.database.view.mscv.VerificationExecutionDetailView;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationExecutionDetailRepository extends RefreshableRepository<VerificationResultDetail, Long> {

    @Deprecated
    VerificationExecutionDetailView findViewById(final Long id);

    <S extends VerificationResultDetail> List<S> findAllByExecution(final VerificationResult execution);
}
