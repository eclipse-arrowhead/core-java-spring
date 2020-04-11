package eu.arrowhead.core.msvc.database.repositories;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.core.msvc.database.entities.Mip;
import eu.arrowhead.core.msvc.database.view.MipView;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MipRepository extends RefreshableRepository<Mip, Long> {

    Optional<MipView> findViewById(final Long id);

}
