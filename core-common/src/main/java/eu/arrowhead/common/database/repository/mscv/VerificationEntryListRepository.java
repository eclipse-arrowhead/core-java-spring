package eu.arrowhead.common.database.repository.mscv;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.common.database.entity.mscv.VerificationEntryList;
import eu.arrowhead.common.database.view.mscv.VerificationListView;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationEntryListRepository extends RefreshableRepository<VerificationEntryList, Long> {

    <S extends VerificationEntryList> Optional<S> findOneByName(final String name);
    Optional<VerificationListView> findViewById(final Long id);
    Optional<VerificationListView> findViewByName(final String name);

}
