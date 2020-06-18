package eu.arrowhead.common.database.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.CloudGatekeeperRelay;
import eu.arrowhead.common.database.entity.Relay;

@Repository
public interface CloudGatekeeperRelayRepository extends RefreshableRepository<CloudGatekeeperRelay,Long> {
	
	Optional<CloudGatekeeperRelay> findByCloudAndRelay(final Cloud cloud, final Relay relay);
}