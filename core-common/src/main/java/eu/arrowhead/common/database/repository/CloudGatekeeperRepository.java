package eu.arrowhead.common.database.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.CloudGatekeeper;

@Repository
public interface CloudGatekeeperRepository extends RefreshableRepository<CloudGatekeeper,Long> {
	
	Optional<CloudGatekeeper> findByCloud(final Cloud cloud); 
	Optional<CloudGatekeeper> findByAddressAndPortAndServiceUri(final String address, final int port, final String serviceUri);
}