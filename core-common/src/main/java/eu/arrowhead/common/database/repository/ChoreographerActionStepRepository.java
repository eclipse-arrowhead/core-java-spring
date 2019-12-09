package eu.arrowhead.common.database.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.ChoreographerActionStep;

@Repository
public interface ChoreographerActionStepRepository extends RefreshableRepository<ChoreographerActionStep,Long> {
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
    public Optional<ChoreographerActionStep> findByName(final String name);
}