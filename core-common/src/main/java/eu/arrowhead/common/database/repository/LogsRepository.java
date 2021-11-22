package eu.arrowhead.common.database.repository;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.Logs;

@Repository
public interface LogsRepository extends RefreshableRepository<Logs,String> {

}
