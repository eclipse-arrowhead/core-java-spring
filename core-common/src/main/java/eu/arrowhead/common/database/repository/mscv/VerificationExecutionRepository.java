package eu.arrowhead.common.database.repository.mscv;

import java.time.ZonedDateTime;
import java.util.List;

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

    VerificationResult findViewById(final Long id);

    VerificationResult findTopViewByTargetAndVerificationListOrderByExecutionDateDesc(final Target target, final VerificationEntryList entryList);

    VerificationResult findTopViewByTargetAndVerificationListLayerOrderByExecutionDateDesc(final Target target, final Layer layer);

    <S extends VerificationResult> Page<S> findAllByTargetAndVerificationListInAndExecutionDateIsBetween(final Pageable page,
                                                                                                         final Target target,
                                                                                                         final List<VerificationEntryList> entryList,
                                                                                                         final ZonedDateTime from, final ZonedDateTime to);

}
