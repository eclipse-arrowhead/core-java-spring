package eu.arrowhead.core.msvc.database.repositories;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.core.msvc.database.entities.VerificationEntryList;
import eu.arrowhead.core.msvc.database.view.VerificationListView;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationEntryListRepository extends RefreshableRepository<VerificationEntryList, Long> {

    <S extends VerificationEntryList> Optional<S> findOneByName(final String name);
    Optional<VerificationListView> findViewById(final Long id);
    Optional<VerificationListView> findViewByName(final String name);

}
