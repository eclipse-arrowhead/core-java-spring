package eu.arrowhead.common.database.repository;

import eu.arrowhead.common.database.entity.ChoreographerSession;
import org.springframework.stereotype.Repository;

@Repository
public interface ChoreographerSessionRepository extends RefreshableRepository<ChoreographerSession,Long> {

}
