/********************************************************************************
 * Copyright (c) 2019 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/


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
	public Page<OrchestratorStore> findAllByPriority(final int priority, final Pageable pageRequest);
	public Optional<OrchestratorStore> findByConsumerSystemAndServiceDefinitionAndPriority(final System consumerSystem,	final ServiceDefinition serviceDefinition,final int priority);
	public Optional<OrchestratorStore> findByConsumerSystemAndServiceDefinitionAndProviderSystemIdAndServiceInterfaceAndForeign(final System consumerSystem, final ServiceDefinition serviceDefinition,
																																final long providerSystemId, final ServiceInterface serviceInterface,
																																final boolean foreign);
	public List<OrchestratorStore> findAllByConsumerSystemAndServiceDefinition(final System consumerSystem,	final ServiceDefinition serviceDefinition, final Sort sortField);
	public Page<OrchestratorStore> findAllByConsumerSystemAndServiceDefinition(final System consumerSystem,	final ServiceDefinition serviceDefinition, final Pageable pageRequest);
	public Page<OrchestratorStore> findAllByConsumerSystemAndServiceDefinitionAndServiceInterface(final System system, final ServiceDefinition serviceDefinition, 
																								  final ServiceInterface serviceInterface,	final Pageable pageRequest);
	public List<OrchestratorStore> findAllByConsumerSystemAndServiceDefinitionAndServiceInterface(final System system, final ServiceDefinition serviceDefinition, 
																								  final ServiceInterface serviceInterface, final Sort sortField);
	public List<OrchestratorStore> findAllByConsumerSystemAndServiceDefinitionAndServiceInterface(final System consumerSystem, final ServiceDefinition serviceDefinition,
																								  final ServiceInterface serviceInterface);
	
	//-------------------------------------------------------------------------------------------------
	@Query("SELECT entry FROM OrchestratorStore entry WHERE priority = ?1 AND consumerSystem.id = ?2 ")
	public List<OrchestratorStore> findAllByPriorityAndSystemId(final int topPriority, final long consumerSystemId);
}