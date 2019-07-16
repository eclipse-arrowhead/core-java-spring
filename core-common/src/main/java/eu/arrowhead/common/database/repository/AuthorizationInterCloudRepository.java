package eu.arrowhead.common.database.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.AuthorizationInterCloud;
import eu.arrowhead.common.database.entity.ServiceDefinition;

@Repository
public interface AuthorizationInterCloudRepository extends RefreshableRepository<AuthorizationInterCloud,Long> {

	Optional<AuthorizationInterCloud> findByCloudAndServiceDefinition(final Cloud cloud, final ServiceDefinition serviceDefinition);

}
