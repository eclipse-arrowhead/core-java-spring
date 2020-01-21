package eu.arrowhead.common.database.repository;

import eu.arrowhead.common.database.entity.ChoreographerRunningStep;
import org.springframework.stereotype.Repository;

@Repository
public interface ChoreographerRunningStepRepository extends RefreshableRepository<ChoreographerRunningStep, Long> {

}
