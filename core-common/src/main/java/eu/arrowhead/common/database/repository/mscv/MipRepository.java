package eu.arrowhead.common.database.repository.mscv;

import java.util.Optional;

import eu.arrowhead.common.database.entity.mscv.Mip;
import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.common.database.view.mscv.MipView;
import org.springframework.stereotype.Repository;

@Repository
public interface MipRepository extends RefreshableRepository<Mip, Long> {

    Optional<MipView> findViewById(final Long id);

    Optional<MipView> findViewByName(final String name);

    Optional<Mip> findByCategoryNameAndExtId(final String category, final Integer extId);

    Optional<Mip> findByCategoryAbbreviationAndExtId(final String categoryAbbreviation, final Integer extId);

    Optional<Mip> findByName(final String name);
}
