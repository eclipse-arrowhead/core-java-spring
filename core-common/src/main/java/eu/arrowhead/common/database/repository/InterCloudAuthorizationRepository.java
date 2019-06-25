package eu.arrowhead.common.database.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.InterCloudAuthorization;
import eu.arrowhead.common.database.entity.IntraCloudAuthorization;

@Repository
public interface InterCloudAuthorizationRepository extends RefreshableRepository<InterCloudAuthorization, Long> {

	Optional<IntraCloudAuthorization> findByCloudIdAndServiceDefinitionId(long cloudId, long serviceDefinitionId);

}
