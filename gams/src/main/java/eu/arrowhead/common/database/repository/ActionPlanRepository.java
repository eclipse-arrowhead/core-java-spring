package eu.arrowhead.common.database.repository;

import java.util.Optional;

import eu.arrowhead.common.database.entity.ActionPlan;
import eu.arrowhead.common.database.entity.GamsInstance;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionPlanRepository extends RefreshableRepository<ActionPlan, Long> {
    Optional<ActionPlan> findByInstanceAndName(final GamsInstance instance, final String name);
}
