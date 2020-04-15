package eu.arrowhead.common.database.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.EventType;

@Repository
public interface EventTypeRepository extends RefreshableRepository<EventType,Long> {

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public Optional<EventType> findByEventTypeName(final String eventTypeName);
}