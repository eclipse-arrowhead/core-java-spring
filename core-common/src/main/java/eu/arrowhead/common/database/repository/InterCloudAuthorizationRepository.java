package eu.arrowhead.common.database.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.InterCloudAuthorization;
import eu.arrowhead.common.database.entity.ServiceDefinition;

@Repository
public interface InterCloudAuthorizationRepository extends RefreshableRepository<InterCloudAuthorization,Long> {

	Optional<InterCloudAuthorization> findByCloudAndServiceDefinition(final Cloud cloud, final ServiceDefinition serviceDefinition);

}