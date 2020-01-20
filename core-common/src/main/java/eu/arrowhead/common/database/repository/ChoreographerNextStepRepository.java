package eu.arrowhead.common.database.repository;

import eu.arrowhead.common.database.entity.ChoreographerStepNextStepConnection;
import org.springframework.stereotype.Repository;

@Repository
public interface ChoreographerNextStepRepository extends RefreshableRepository<ChoreographerStepNextStepConnection,Long> {
	
}