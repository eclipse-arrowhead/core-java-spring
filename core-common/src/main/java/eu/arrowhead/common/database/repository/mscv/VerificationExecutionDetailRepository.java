package eu.arrowhead.common.database.repository.mscv;

import java.util.List;

import eu.arrowhead.common.database.entity.mscv.VerificationResult;
import eu.arrowhead.common.database.entity.mscv.VerificationResultDetail;
import eu.arrowhead.common.database.repository.RefreshableRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationExecutionDetailRepository extends RefreshableRepository<VerificationResultDetail, Long> {

    <S extends VerificationResultDetail> List<S> findAllByExecution(final VerificationResult execution);
}
