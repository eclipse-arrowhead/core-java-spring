package eu.arrowhead.common.database.repository.mscv;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.common.database.entity.mscv.VerificationEntryList;
import eu.arrowhead.common.database.view.mscv.VerificationListView;
import eu.arrowhead.common.dto.shared.mscv.Layer;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationEntryListRepository extends RefreshableRepository<VerificationEntryList, Long> {

    <S extends VerificationEntryList> Optional<S> findOneByNameAndLayer(final String name, final Layer layer);
    Optional<VerificationListView> findViewById(final Long id);
    Optional<VerificationListView> findViewByNameAndLayer(final String name, final Layer layer);

}
