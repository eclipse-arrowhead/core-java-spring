package eu.arrowhead.common.database.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.entity.ServiceRegistry;

@Repository
public interface ServiceRegistryRepository extends JpaRepository<ServiceRegistry,Long> {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public Optional<ServiceRegistry> findByServiceDefinitionAndSystem(final ServiceDefinition serviceDefinition, final System system);
	public List<ServiceRegistry> findByServiceDefinition(final ServiceDefinition serviceDefinition);
}