package eu.arrowhead.common.database.repository.mscv;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.common.database.entity.mscv.Target;
import eu.arrowhead.common.database.entity.mscv.VerificationEntryList;
import eu.arrowhead.common.database.entity.mscv.VerificationResult;
import eu.arrowhead.common.database.view.mscv.VerificationExecutionView;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationExecutionRepository extends RefreshableRepository<VerificationResult, Long> {

    VerificationExecutionView findViewById(final Long id);
    VerificationExecutionView findTopViewByTargetAndVerificationList(final Target target, final VerificationEntryList entryList);

}
