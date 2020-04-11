package eu.arrowhead.core.msvc.database.repositories;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.core.msvc.database.entities.Standard;
import org.springframework.stereotype.Repository;

@Repository
public interface StandardRepository extends RefreshableRepository<Standard, Long> {
}
