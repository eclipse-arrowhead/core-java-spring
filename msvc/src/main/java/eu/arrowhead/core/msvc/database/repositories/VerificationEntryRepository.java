package eu.arrowhead.core.msvc.database.repositories;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.core.msvc.database.entities.VerificationEntry;
import eu.arrowhead.core.msvc.database.entities.VerificationEntryList;
import eu.arrowhead.core.msvc.database.view.VerificationEntryView;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface VerificationEntryRepository extends RefreshableRepository<VerificationEntry, Long> {
    Optional<VerificationEntryView> findViewById(final Long id);

    Set<VerificationEntryView> findViewByVerificationList(final VerificationEntryList entryList);
}
