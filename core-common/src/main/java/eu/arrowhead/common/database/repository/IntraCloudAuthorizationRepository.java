package eu.arrowhead.common.database.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.IntraCloudAuthorization;

@Repository
public interface IntraCloudAuthorizationRepository extends RefreshableRepository<IntraCloudAuthorization,Long> {

	@Query("SELECT entry FROM IntraCloudAuthorization entry WHERE consumerSystem.id = ?1 AND providerSystem.id = ?2 AND serviceDefinition.id = ?3")
	public Optional<IntraCloudAuthorization> findByConsumerIdAndProviderIdAndServiceDefinitionId(final long consumerId, final long providerId, final long serviceDefinitionId);
}
