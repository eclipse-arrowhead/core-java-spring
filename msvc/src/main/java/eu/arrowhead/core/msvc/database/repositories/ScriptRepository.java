package eu.arrowhead.core.msvc.database.repositories;

import eu.arrowhead.common.database.repository.RefreshableRepository;
import eu.arrowhead.core.msvc.database.entities.MipCategory;
import org.springframework.stereotype.Repository;

@Repository
public interface ScriptRepository extends RefreshableRepository<MipCategory, Long> {
}
