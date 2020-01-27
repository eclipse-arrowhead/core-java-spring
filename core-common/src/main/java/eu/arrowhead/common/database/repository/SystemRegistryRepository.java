package eu.arrowhead.common.database.repository;

import eu.arrowhead.common.database.entity.Device;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.entity.SystemRegistry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemRegistryRepository extends RefreshableRepository<SystemRegistry, Long>
{

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    Optional<SystemRegistry> findBySystem(final System system);

    List<SystemRegistry> findByDevice(final Device device);

    Optional<SystemRegistry> findBySystemAndDevice(final System systemDb, final Device deviceDb);

	List<SystemRegistry> findAllBySystemIsIn(final List<System> systems);

    Page<SystemRegistry> findAllBySystemIsIn(final List<System> systemList, final PageRequest pageRequest);
}