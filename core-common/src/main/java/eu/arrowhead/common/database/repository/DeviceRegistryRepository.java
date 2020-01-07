package eu.arrowhead.common.database.repository;

import eu.arrowhead.common.database.entity.Device;
import eu.arrowhead.common.database.entity.DeviceRegistry;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.entity.SystemRegistry;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRegistryRepository extends RefreshableRepository<DeviceRegistry,Long> {

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public Optional<DeviceRegistry> findByDevice(final Device device);
}