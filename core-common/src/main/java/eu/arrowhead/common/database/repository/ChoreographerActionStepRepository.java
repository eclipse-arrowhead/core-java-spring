package eu.arrowhead.common.database.repository;

import eu.arrowhead.common.database.entity.ChoreographerActionStep;
import org.springframework.stereotype.Repository;

@Repository
public interface ChoreographerActionStepRepository extends RefreshableRepository<ChoreographerActionStep, Long> {
}
