package eu.arrowhead.common.database.repository;

import eu.arrowhead.common.database.entity.Device;
import eu.arrowhead.common.database.entity.DeviceRegistry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRegistryRepository extends RefreshableRepository<DeviceRegistry, Long>
{

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    Optional<DeviceRegistry> findByDevice(final Device device);
    Page<DeviceRegistry> findAllByDevice(final List<Device> devices, final PageRequest pageRequest);
    List<DeviceRegistry> findAllByDevice(final List<Device> devices);
}