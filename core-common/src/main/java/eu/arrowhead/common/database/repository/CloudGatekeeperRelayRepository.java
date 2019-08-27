package eu.arrowhead.common.database.repository;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.CloudGatekeeperRelay;

@Repository
public interface CloudGatekeeperRelayRepository extends RefreshableRepository<CloudGatekeeperRelay,Long> {

}