package eu.arrowhead.common.database.repository.mscv;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import eu.arrowhead.common.database.entity.mscv.Target;
import eu.arrowhead.common.database.entity.mscv.VerificationEntryList;
import eu.arrowhead.common.database.entity.mscv.VerificationResult;
import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.common.dto.shared.mscv.Layer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationExecutionRepository extends RefreshableRepository<VerificationResult, Long> {


    Optional<VerificationResult> findTopByTargetAndVerificationListOrderByExecutionDateDesc(final Target target, final VerificationEntryList entryList);

    Optional<VerificationResult> findTopByTargetAndVerificationListLayerOrderByExecutionDateDesc(final Target target, final Layer layer);

    <S extends VerificationResult> Page<S> findAllByTargetAndVerificationListInAndExecutionDateIsBetween(final Pageable page,
                                                                                                         final Target target,
                                                                                                         final List<VerificationEntryList> entryList,
                                                                                                         final ZonedDateTime from, final ZonedDateTime to);

}
