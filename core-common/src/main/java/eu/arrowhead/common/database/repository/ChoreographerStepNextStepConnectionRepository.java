package eu.arrowhead.common.database.repository;

import eu.arrowhead.common.database.entity.ChoreographerStepNextStepConnection;
import org.springframework.stereotype.Repository;

@Repository
public interface ChoreographerStepNextStepConnectionRepository extends RefreshableRepository<ChoreographerStepNextStepConnection,Long> {
	
}