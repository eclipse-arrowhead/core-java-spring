/********************************************************************************
 * Copyright (c) 2021 AITIA
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

package eu.arrowhead.core.orchestrator.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.arrowhead.common.database.entity.OrchestratorStoreFlexible;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.core.orchestrator.database.service.OrchestratorStoreFlexibleDBService;

@Service
public class OrchestratorFlexibleDriver { //TODO unit tests
	
	//=================================================================================================
	// members
	
	@Autowired
	private OrchestratorStoreFlexibleDBService dbService;
	
	@Autowired
	private OrchestratorDriver driver;
	
	private Comparator<OrchestratorStoreFlexible> ruleComparator = getRuleComparator();
	
	private static final Logger logger = LogManager.getLogger(OrchestratorFlexibleDriver.class);

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	/* default */ SystemResponseDTO queryConsumerSystem(final SystemRequestDTO request) {
		logger.debug("queryConsumerSystem started ...");
		Assert.notNull(request, "Request is null.");
		
		return driver.queryServiceRegistryBySystemRequestDTO(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	/* default */ List<OrchestratorStoreFlexible> collectAndSortMatchingRules(final OrchestrationFormRequestDTO originalRequest, final SystemResponseDTO consumerSystem) {
		logger.debug("collectAndSortMatchingRules started ...");
		
		final Set<OrchestratorStoreFlexible> rules = new HashSet<>();
		
		final ServiceQueryFormDTO requestedService = originalRequest.getRequestedService();
		final boolean needInterfaceFilter = requestedService.getInterfaceRequirements() != null && !requestedService.getInterfaceRequirements().isEmpty();
		final List<String> normalizedInterfaceRequirements = normalizeInterfaceNames(requestedService.getInterfaceRequirements());
		
		List<OrchestratorStoreFlexible> nameBasedRules = dbService.getMatchedRulesByServiceDefinitionAndConsumerName(requestedService.getServiceDefinitionRequirement(), consumerSystem.getSystemName());
		if (needInterfaceFilter) {
			//TODO: continue
		}
		
		// find rules (if request contains service intf requirements then for every query we need post-processing: service_intf is null or service_intf IN (request's service interface requirements))
		// a) use service def and requester system name and metadata is empty => add all to the rules list
		// b) use service def where consumer metadata is available: for every such rule
		//     0) if consumer system name is not empty and not equals to requester name => drop it
		//     1) create a union of system metadata and requester metadata (once)
		//     2) if consumer metadata (from store) is part of the metadata of union, then this rule is valid => add to the rules list

		// sorting rules by priority and id 
		final List<OrchestratorStoreFlexible> result = new ArrayList<>(rules);
		Collections.sort(result, ruleComparator);
		
		return result;
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private Comparator<OrchestratorStoreFlexible> getRuleComparator() {
		logger.debug("getRuleComparator started ...");

		return Comparator.comparing(OrchestratorStoreFlexible::getPriority)
						 .thenComparing(OrchestratorStoreFlexible::getId);
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<String> normalizeInterfaceNames(final List<String> interfaceNames) { 
		logger.debug("normalizeInterfaceNames started...");
		
		if (interfaceNames == null) {
			return List.of();
		}
		
		return interfaceNames.parallelStream().filter(Objects::nonNull).filter(e -> !e.isBlank()).map(e -> e.toUpperCase().trim()).collect(Collectors.toList());
	}
}