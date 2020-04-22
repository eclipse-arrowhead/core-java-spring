package eu.arrowhead.common.database.repository;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.ServiceRegistryInterfaceConnection;

@Repository
public interface ServiceRegistryInterfaceConnectionRepository extends RefreshableRepository<ServiceRegistryInterfaceConnection,Long> {
}