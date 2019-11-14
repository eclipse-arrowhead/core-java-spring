package eu.arrowhead.common.database.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.ChoreographerActionPlan;

@Repository
public interface ChoreographerActionPlanRepository extends RefreshableRepository<ChoreographerActionPlan,Long> {
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
    public Optional<ChoreographerActionPlan> findByActionPlanName(final String actionPlanName);
}