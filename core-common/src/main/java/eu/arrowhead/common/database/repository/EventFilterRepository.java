package eu.arrowhead.common.database.repository;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.EventFilter;

@Repository
public interface EventFilterRepository extends RefreshableRepository<EventFilter, Long> {

}
