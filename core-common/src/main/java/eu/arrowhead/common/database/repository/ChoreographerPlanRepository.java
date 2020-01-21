package eu.arrowhead.common.database.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.ChoreographerPlan;

@Repository
public interface ChoreographerPlanRepository extends RefreshableRepository<ChoreographerPlan,Long> {
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
    //public Optional<ChoreographerPlan> findByActionPlanName(final String actionPlanName);
}