package eu.arrowhead.common.database.repository;

import eu.arrowhead.common.database.entity.ChoreographerWorklog;
import org.springframework.stereotype.Repository;

@Repository
public interface ChoreographerWorklogRepository extends RefreshableRepository<ChoreographerWorklog, Long> {

}
