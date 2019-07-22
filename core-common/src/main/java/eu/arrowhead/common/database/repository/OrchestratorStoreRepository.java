package eu.arrowhead.common.database.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.OrchestratorStore;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.ServiceInterface;
import eu.arrowhead.common.database.entity.System;

@Repository
public interface OrchestratorStoreRepository extends RefreshableRepository<OrchestratorStore, Long> {
	
	public Page<OrchestratorStore> findAllByPriority(final int priority, final Pageable regRequest);
	
	public List<OrchestratorStore> findAllByConsumerSystemAndServiceDefinition(final System consumerSystem,
			final ServiceDefinition serviceDefinition);
	
	public Optional<OrchestratorStore> findByConsumerSystemAndServiceDefinitionAndProviderSystemIdAndServiceInterfaceAndForeign(final System consumerSystem,
			final ServiceDefinition serviceDefinition, final long providerSystemId, final ServiceInterface serviceInterface, final boolean foreign);

	public List<OrchestratorStore> findAllByConsumerSystemAndServiceDefinition(final System consumerSystem,
			final ServiceDefinition serviceDefinition,final Sort sortByField);

	public Page<OrchestratorStore> findAllByConsumerSystemAndServiceDefinition(System consumerSystem,
			ServiceDefinition serviceDefinition, Pageable regRequest);
		
}