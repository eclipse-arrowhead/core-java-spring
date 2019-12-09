package eu.arrowhead.common.database.repository;

import eu.arrowhead.common.database.entity.ChoreographerNextActionStep;
import org.springframework.stereotype.Repository;

@Repository
public interface ChoreographerNextActionStepRepository extends RefreshableRepository<ChoreographerNextActionStep,Long> {
	
}