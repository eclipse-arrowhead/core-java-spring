package eu.arrowhead.common.database.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.OrchestratorStore;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.ServiceInterface;
import eu.arrowhead.common.database.entity.System;

@Repository
public interface OrchestratorStoreRepository extends RefreshableRepository<OrchestratorStore,Long> {
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public Page<OrchestratorStore> findAllByPriority(final int priority, final Pageable regRequest);
	public Optional<OrchestratorStore> findByConsumerSystemAndServiceDefinitionAndPriority(final System consumerSystem,	final ServiceDefinition serviceDefinition,final int priority);
	public Optional<OrchestratorStore> findByConsumerSystemAndServiceDefinitionAndProviderSystemIdAndServiceInterfaceAndForeign(final System consumerSystem, 
																																final ServiceDefinition serviceDefinition,
																																final long providerSystemId,
																																final ServiceInterface serviceInterface,
																																final boolean foreign);
	
	public Page<OrchestratorStore> findAllByConsumerSystemAndServiceDefinition(
			final System consumerSystem, 
			final ServiceDefinition serviceDefinition, 
			final Pageable regRequest);
	
	public Page<OrchestratorStore> findAllByConsumerSystemAndServiceDefinitionAndServiceInterface(
			final System system,
			final ServiceDefinition serviceDefinition, 
			final ServiceInterface validServiceInterface, 
			final Pageable regRequest);
	
	public List<OrchestratorStore> findAllByConsumerSystemAndServiceDefinitionAndServiceInterface(
			final System system,
			final ServiceDefinition serviceDefinition, 
			final ServiceInterface validServiceInterface, 
			final Sort sortField);
	
	public List<OrchestratorStore> findAllByConsumerSystemAndServiceDefinitionAndServiceInterface(
			final System anyConsumerSystemForValidation, 
			final ServiceDefinition anyServiceDefinition,
			final ServiceInterface anyServiceInterface);
	
	@Query("SELECT entry FROM OrchestratorStore entry WHERE priority = ?1 AND consumerSystem.id = ?2 ")
	public List<OrchestratorStore> findAllByPriorityAndSystemId(int topPriority, long consumerSystemId);
	
	public List<OrchestratorStore> findAllByConsumerSystemAndServiceDefinitionAndServiceInterface(
			final System consumerSystem,
			final ServiceDefinition serviceDefinition);
}