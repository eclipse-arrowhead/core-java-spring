package eu.arrowhead.common.database.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.OrchestratorStore;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.System;

@Repository
public interface OrchestratorStoreRepository extends RefreshableRepository<OrchestratorStore, Long> {
	
	@Query("SELECT entry FROM OrchestratorStore entry WHERE priority = ?1 ")
	public Page<OrchestratorStore> findAllByPriority(final int priority, PageRequest regRequest);

	@Query("SELECT entry FROM OrchestratorStore entry WHERE  consumerSystem.id = ?1 AND serviceDefinition.id = ?2 ")
	public Page<OrchestratorStore> findAllByConsumerIdAndServiceDefinitionId(final long systemId,
			final long serviceDefinitionId, final PageRequest of);
	
	@Query("SELECT entry FROM OrchestratorStore entry WHERE  consumerSystem.id = ?1 AND serviceDefinition.id = ?2 ")
	public List<OrchestratorStore> findAllByConsumerIdAndServiceDefinitionId(final long systemId,
			final long serviceDefinitionId);
	
	@Query("SELECT entry FROM OrchestratorStore entry WHERE  consumerSystem.id = ?1 AND serviceDefinition.id = ?2 AND priority = ?3 ")
	public Optional<OrchestratorStore> findByConsumerIdAndServiceDefinitionIdAndPriority(final long systemId,
			final long serviceDefinitionId, final int priority);
	
	@Query("SELECT entry FROM OrchestratorStore entry WHERE  consumerSystem.id = ?1 AND serviceDefinition.id = ?2 AND providerSystem.id = ?3 ")
	public Optional<OrchestratorStore> findByConsumerIdAndServiceDefinitionIdAndProviderId(final long consumerId,
			final long serviceDefinitionId, final long providerId);

	public List<OrchestratorStore> findAllByConsumerSystemAndServiceDefinition(System consumerSystem,
			ServiceDefinition serviceDefinition, Sort sortByField);
	
}