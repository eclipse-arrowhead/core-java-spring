package eu.arrowhead.core.msvc.database.repositories;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.core.msvc.database.entities.Mip;
import eu.arrowhead.core.msvc.database.entities.VerificationExecution;
import eu.arrowhead.core.msvc.database.entities.VerificationExecutionDetail;
import eu.arrowhead.core.msvc.database.view.VerificationRunView;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationExecutionDetailRepository extends RefreshableRepository<VerificationExecutionDetail, Long> {

    Optional<VerificationRunView> findViewById(final Long id);
    Optional<VerificationRunView> findViewByExecutionAndMip(final VerificationExecution execution, final Mip mip);
}
