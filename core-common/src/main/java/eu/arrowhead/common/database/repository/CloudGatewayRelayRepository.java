package eu.arrowhead.common.database.repository;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.CloudGatewayRelay;

@Repository
public interface CloudGatewayRelayRepository extends RefreshableRepository<CloudGatewayRelay,Long> {

}