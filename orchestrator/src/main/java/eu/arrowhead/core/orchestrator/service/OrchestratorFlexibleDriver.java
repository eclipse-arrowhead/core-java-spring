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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.OrchestratorStoreFlexible;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
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
	
	private final Comparator<OrchestratorStoreFlexible> ruleComparator = getRuleComparator();
	
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
		if (needInterfaceFilter && !nameBasedRules.isEmpty()) {
			nameBasedRules = filterByInterfaces(nameBasedRules, normalizedInterfaceRequirements);
		}
		rules.addAll(nameBasedRules);
		
		final Map<String,String> currentConsumerMetadata = createMetadataMerge(originalRequest.getRequesterSystem().getMetadata(), consumerSystem.getMetadata());
		List<OrchestratorStoreFlexible> metadataBasedRules = dbService.getMatchedRulesByServiceDefinitionAndNonNullConsumerMetadata(requestedService.getServiceDefinitionRequirement());
		if (needInterfaceFilter && !metadataBasedRules.isEmpty()) {
			metadataBasedRules = filterByInterfaces(metadataBasedRules, normalizedInterfaceRequirements);
		}
		
		for (final OrchestratorStoreFlexible rule : metadataBasedRules) {
			if (!Utilities.isEmpty(rule.getConsumerSystemName()) && !rule.getConsumerSystemName().equals(consumerSystem.getSystemName())) {
				// name matching failed
				continue;
			}
			
			if (isMetadataMatch(rule.getConsumerSystemMetadata(), currentConsumerMetadata)) {
				rules.add(rule);
			}
		}
		
		// sorting rules by priority and id 
		final List<OrchestratorStoreFlexible> result = new ArrayList<>(rules);
		Collections.sort(result, ruleComparator);
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	/* default */ Map<OrchestratorStoreFlexible,ServiceQueryResultDTO> queryServiceRegistry(final OrchestrationFormRequestDTO request, final List<OrchestratorStoreFlexible> rules) {
		logger.debug("queryServiceRegistry started ...");
		
		//TODO: continue
		
		// for every rule,
		//	   1) use query with service def, rule's service intf (if any), service metadata (if any, if metadata_search flag is true, then merge the two metadata_requirements before query), ping flag, security (if any), version (if any)

		return null;
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
	private List<String> normalizeInterfaceNames(final List<String> interfaceNames) { 
		logger.debug("normalizeInterfaceNames started...");
		
		if (interfaceNames == null) {
			return List.of();
		}
		
		return interfaceNames.parallelStream().filter(Objects::nonNull).filter(e -> !e.isBlank()).map(e -> e.toUpperCase().trim()).collect(Collectors.toList());
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<OrchestratorStoreFlexible> filterByInterfaces(final List<OrchestratorStoreFlexible> candidates, final List<String> interfaceNames) {
		logger.debug("filterByInterfaces started...");
		
		final List<OrchestratorStoreFlexible> result = new ArrayList<>();
		for (final OrchestratorStoreFlexible rule : candidates) {
			if (Utilities.isEmpty(rule.getServiceInterfaceName()) || interfaceNames.contains(rule.getServiceInterfaceName())) {
				result.add(rule);
			}
		}
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	// primary takes precedence if there is any key match
	private Map<String,String> createMetadataMerge(final Map<String,String> primary, final Map<String,String> secondary) {
		logger.debug("createMetadataMerge started...");
		
		final Map<String,String> _primary = normalizeMetadata(primary);
		final Map<String,String> _secondary = normalizeMetadata(secondary);
		
		final Map<String,String> result = new HashMap<>(_secondary);
		result.putAll(_primary);
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	// throws IllegalStateException if two keys are identical after trim
	@SuppressWarnings("squid:RedundantThrowsDeclarationCheck")
	private Map<String,String> normalizeMetadata(final Map<String,String> metadata) throws IllegalStateException { 
		logger.debug("normalizeMetadata started...");
		if (metadata == null) {
			return Map.of();
		}
		
		return metadata.entrySet().parallelStream().filter(e -> e.getValue() != null).collect(Collectors.toMap(e -> e.getKey().trim(),
																	  		 								   e -> e.getValue().trim()));
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean isMetadataMatch(final String metadataFromRule, final Map<String,String> currentMetadata) {
		logger.debug("isMetadataMatch started...");
		
		final Map<String,String> _metadataFromRule = normalizeMetadata(Utilities.text2Map(metadataFromRule));
		
		return currentMetadata.entrySet().containsAll(_metadataFromRule.entrySet());
	}

}