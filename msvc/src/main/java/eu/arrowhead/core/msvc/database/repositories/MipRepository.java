package eu.arrowhead.core.msvc.database.repositories;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.core.msvc.database.entities.Mip;
import eu.arrowhead.core.msvc.database.view.MipView;
import org.springframework.stereotype.Repository;

@Repository
public interface MipRepository extends RefreshableRepository<Mip, Long> {

    MipView findViewById(final Long id);

}
