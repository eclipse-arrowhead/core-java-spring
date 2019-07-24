package eu.arrowhead.common.database.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.ForeignSystem;

@Repository
public interface ForeignSystemRepository extends RefreshableRepository<ForeignSystem,Long> {
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public Optional<ForeignSystem> findBySystemNameAndAddressAndPortAndProviderCloud(final String systemName, final String address, final int port, final Cloud providerCloud);
}