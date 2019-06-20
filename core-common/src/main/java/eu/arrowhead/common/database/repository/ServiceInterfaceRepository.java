package eu.arrowhead.common.database.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.ServiceInterface;

@Repository
public interface ServiceInterfaceRepository extends RefreshableRepository<ServiceInterface,Long> {

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public Optional<ServiceInterface> findByInterfaceName(final String interfaceName);
}