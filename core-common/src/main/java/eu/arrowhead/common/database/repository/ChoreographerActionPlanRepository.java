package eu.arrowhead.common.database.repository;

import eu.arrowhead.common.database.entity.ChoreographerAction;
import eu.arrowhead.common.database.entity.ChoreographerActionPlan;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChoreographerActionPlanRepository extends RefreshableRepository<ChoreographerActionPlan, Long> {

    public Optional<ChoreographerActionPlan> findByActionPlanName(final String actionPlanName);

}
