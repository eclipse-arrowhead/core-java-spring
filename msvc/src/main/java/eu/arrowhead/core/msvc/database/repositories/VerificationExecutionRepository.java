package eu.arrowhead.core.msvc.database.repositories;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.core.msvc.database.entities.Target;
import eu.arrowhead.core.msvc.database.entities.VerificationEntryList;
import eu.arrowhead.core.msvc.database.entities.VerificationExecution;
import eu.arrowhead.core.msvc.database.view.VerificationExecutionDetailView;
import eu.arrowhead.core.msvc.database.view.VerificationExecutionView;
import eu.arrowhead.core.msvc.database.view.VerificationListView;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationExecutionRepository extends RefreshableRepository<VerificationExecution, Long> {

    VerificationExecutionView findViewById(final Long id);
    VerificationExecutionView findTopViewByTargetAndVerificationList(final Target target, final VerificationEntryList entryList);

}
