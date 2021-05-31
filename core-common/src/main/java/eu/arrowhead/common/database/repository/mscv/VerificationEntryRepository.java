package eu.arrowhead.common.database.repository.mscv;

import java.util.Optional;
import java.util.Set;

import eu.arrowhead.common.database.entity.mscv.VerificationEntry;
import eu.arrowhead.common.database.entity.mscv.VerificationEntryList;
import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.common.database.view.mscv.VerificationEntryView;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationEntryRepository extends RefreshableRepository<VerificationEntry, Long> {
    Optional<VerificationEntryView> findViewById(final Long id);

    Set<VerificationEntryView> findViewByVerificationList(final VerificationEntryList entryList);

}
