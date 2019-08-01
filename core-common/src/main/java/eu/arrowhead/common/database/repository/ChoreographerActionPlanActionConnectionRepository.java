package eu.arrowhead.common.database.repository;

import eu.arrowhead.common.database.entity.ChoreographerAction;
import eu.arrowhead.common.database.entity.ChoreographerActionPlan;
import eu.arrowhead.common.database.entity.ChoreographerActionPlanActionConnection;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChoreographerActionPlanActionConnectionRepository extends RefreshableRepository<ChoreographerActionPlanActionConnection, Long> {

}
