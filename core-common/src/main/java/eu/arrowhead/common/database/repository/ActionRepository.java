package eu.arrowhead.common.database.repository;

import eu.arrowhead.common.database.entity.Action;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionRepository extends RefreshableRepository<Action, Long> {
}
