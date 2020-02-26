package eu.arrowhead.common.database.repository;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.AuthorizationInterCloudInterfaceConnection;

@Repository
public interface AuthorizationInterCloudInterfaceConnectionRepository extends RefreshableRepository<AuthorizationInterCloudInterfaceConnection,Long> {
}