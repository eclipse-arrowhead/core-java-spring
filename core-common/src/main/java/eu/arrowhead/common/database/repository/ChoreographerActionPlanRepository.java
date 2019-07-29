package eu.arrowhead.common.database.repository;

import eu.arrowhead.common.database.entity.ChoreographerActionPlan;
import org.springframework.stereotype.Repository;

@Repository
public interface ChoreographerActionPlanRepository extends RefreshableRepository<ChoreographerActionPlan, Long> {
}
