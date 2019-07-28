package eu.arrowhead.common.database.repository;

import eu.arrowhead.common.database.entity.ActionPlan;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionPlanRepository extends RefreshableRepository<ActionPlan, Long> {
}
