package eu.arrowhead.common.database.repository;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.AuthorizationIntraCloudInterfaceConnection;

@Repository
public interface AuthorizationIntraCloudInterfaceConnectionRepository extends RefreshableRepository<AuthorizationIntraCloudInterfaceConnection,Long> {
}