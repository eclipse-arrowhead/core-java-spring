package eu.arrowhead.common.database.repository;

import eu.arrowhead.common.database.entity.ChoreographerAction;
import org.springframework.stereotype.Repository;

@Repository
public interface ChoreographerActionRepository extends RefreshableRepository<ChoreographerAction, Long> {
}
