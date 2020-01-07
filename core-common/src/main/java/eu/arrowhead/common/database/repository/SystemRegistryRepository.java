package eu.arrowhead.common.database.repository;

import eu.arrowhead.common.database.entity.*;
import eu.arrowhead.common.database.entity.System;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemRegistryRepository extends RefreshableRepository<SystemRegistry,Long> {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public Optional<SystemRegistry> findBySystem(final System system);
	public List<SystemRegistry> findByDevice(final Device device);
}