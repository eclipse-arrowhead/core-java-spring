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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.OrchestratorStoreFlexible;
import eu.arrowhead.common.dto.internal.DTOUtilities;
import eu.arrowhead.common.dto.shared.OrchestrationFlags;
import eu.arrowhead.common.dto.shared.OrchestrationFlags.Flag;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.PreferredProviderDataDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO.Builder;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultListDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.orchestrator.database.service.OrchestratorStoreFlexibleDBService;

@Service
public class OrchestratorFlexibleDriver { 
	
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
		Assert.notNull(originalRequest, "Original request is null.");
		Assert.notNull(originalRequest.getRequestedService(), "Requested service is null.");
		Assert.notNull(consumerSystem, "Consumer system is null.");
		Assert.isTrue(!Utilities.isEmpty(consumerSystem.getSystemName()), "Consumer system name is null or blank.");
		
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
	/* default */ List<Pair<OrchestratorStoreFlexible,ServiceQueryResultDTO>> queryServiceRegistry(final OrchestrationFormRequestDTO request, final List<OrchestratorStoreFlexible> rules) {
		logger.debug("queryServiceRegistry started ...");
		Assert.notNull(request, "Request is null");
		Assert.notNull(rules, "Rules list is null");
		
		if (rules.isEmpty()) {
			return List.of();
		}
		
		final List<ServiceQueryFormDTO> forms = createServiceQueryForms(request, rules);
		final ServiceQueryResultListDTO response = driver.multiQueryServiceRegistry(forms);

		return convertQueryResponse(rules, response);
	}
	
	//-------------------------------------------------------------------------------------------------
	/* default */ List<ServiceRegistryResponseDTO> filterSRResultsByProviderRequirements(final List<Pair<OrchestratorStoreFlexible,ServiceQueryResultDTO>> queryDataWithRules, final List<PreferredProviderDataDTO> onlyPreferredProviders) {
		logger.debug("filterSRResultsByProviderRequirements started ...");
		Assert.notNull(queryDataWithRules, "Query data is null.");
		
		final List<ServiceRegistryResponseDTO> result = new ArrayList<>();
		
		for (final Pair<OrchestratorStoreFlexible,ServiceQueryResultDTO> pair : queryDataWithRules) {
			Assert.notNull(pair, "Query data is null.");
			final List<ServiceRegistryResponseDTO> ruleResult = filterSRResultsByProviderRequirements(pair.getKey(), pair.getValue(), onlyPreferredProviders);
			for (final ServiceRegistryResponseDTO srEntry : ruleResult) {
				if (!result.contains(srEntry)) { // don't want duplicates in result
					result.add(srEntry);
				}
			}
		}

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

	//-------------------------------------------------------------------------------------------------
	private List<ServiceQueryFormDTO> createServiceQueryForms(final OrchestrationFormRequestDTO request, final List<OrchestratorStoreFlexible> rules) {
		logger.debug("createServiceQueryForms started...");
		
		final List<ServiceQueryFormDTO> result = new ArrayList<>(rules.size());
		for (final OrchestratorStoreFlexible rule : rules) {
			result.add(createServiceQueryForm(request, rule));
		}
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private ServiceQueryFormDTO createServiceQueryForm(final OrchestrationFormRequestDTO request, final OrchestratorStoreFlexible rule) {
		logger.debug("createServiceQueryForm started...");
		Assert.notNull(rule, "Rule is null");

		final ServiceQueryFormDTO requestedService = request.getRequestedService();
		Assert.notNull(requestedService, "Requested service is null");
		Assert.isTrue(!Utilities.isEmpty(requestedService.getServiceDefinitionRequirement()), "Requested service definition is null or blank");
		
		final OrchestrationFlags orchestrationFlags = request.getOrchestrationFlags();
		Assert.notNull(orchestrationFlags, "Flags object is null");
		
		Map<String,String> finalMetadataRequirement = Utilities.text2Map(rule.getServiceMetadata());
		if (orchestrationFlags.getOrDefault(Flag.METADATA_SEARCH, false) && !Utilities.isEmpty(requestedService.getMetadataRequirements())) {
			finalMetadataRequirement = createMetadataMerge(requestedService.getMetadataRequirements(), finalMetadataRequirement);
		} else {
			finalMetadataRequirement = normalizeMetadata(finalMetadataRequirement);
		}
		
		Builder builder = new ServiceQueryFormDTO.Builder(requestedService.getServiceDefinitionRequirement()) // from original request
									  			 .metadata(finalMetadataRequirement) // from rule (and from the original request too if the metadata search flag is set)
									  			 .pingProviders(orchestrationFlags.getOrDefault(Flag.PING_PROVIDERS, false)) // from flag
									  			 .version(requestedService.getVersionRequirement()) // from the original request
									  			 .version(requestedService.getMinVersionRequirement(), requestedService.getMaxVersionRequirement()); // from the original request
		
		if (rule.getServiceInterfaceName() != null) {
			builder = builder.interfaces(rule.getServiceInterfaceName()); // from rule
			
		}
		
		if (requestedService.getSecurityRequirements() != null && !requestedService.getSecurityRequirements().isEmpty()) {
			builder = builder.security(requestedService.getSecurityRequirements()); // from the original request
			
		}
		
		return builder.build();
	}

	//-------------------------------------------------------------------------------------------------
	private List<Pair<OrchestratorStoreFlexible,ServiceQueryResultDTO>> convertQueryResponse(final List<OrchestratorStoreFlexible> rules, final ServiceQueryResultListDTO response) {
		logger.debug("convertQueryResponse started...");
		
		if (rules.size() > response.getResults().size()) {
			throw new ArrowheadException("Service Registry does not handle all query forms.");
		}
		
		final List<Pair<OrchestratorStoreFlexible,ServiceQueryResultDTO>> result = new ArrayList<>(rules.size());
		for (int i = 0; i < rules.size(); ++i) {
			result.add(new ImmutablePair<>(rules.get(i), response.getResults().get(i)));
		}
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<ServiceRegistryResponseDTO> filterSRResultsByProviderRequirements(final OrchestratorStoreFlexible rule, final ServiceQueryResultDTO srResult, final List<PreferredProviderDataDTO> onlyPreferredProviders) {
		logger.debug("filterSRResultsByProviderRequirements started...");
		Assert.notNull(rule, "Rule is null");
		Assert.notNull(srResult, "Service Registry query result is null");
		
		final List<ServiceRegistryResponseDTO> queryData = srResult.getServiceQueryData();
		if (queryData == null || queryData.isEmpty()) {
			return List.of();
		}
		
		final boolean onlyPreferred = onlyPreferredProviders != null;
		
		final List<ServiceRegistryResponseDTO> result = new ArrayList<>();
		for (final ServiceRegistryResponseDTO srEntry : queryData) {
			// if rule has a provider name, we drop every provider that does not match
			if (rule.getProviderSystemName() != null && !rule.getProviderSystemName().equals(srEntry.getProvider().getSystemName())) {
				continue;
			}
			
			// filter not-preferred providers (only preferred flag is set)
			if (onlyPreferred && !isPreferredProvider(srEntry.getProvider(), onlyPreferredProviders)) {
				continue;
			}
			
			// filter by provider metadata
			if (rule.getProviderSystemMetadata() != null && !isMetadataMatch(rule.getProviderSystemMetadata(), normalizeMetadata(srEntry.getProvider().getMetadata()))) {
				continue;
			}
			
			result.add(srEntry);
		}
		
		return result;
	}

	//-------------------------------------------------------------------------------------------------
	private boolean isPreferredProvider(final SystemResponseDTO provider, final List<PreferredProviderDataDTO> onlyPreferredProviders) {
		logger.debug("isPreferredProvider started...");
		
		for (final PreferredProviderDataDTO preferredProviderDataDTO : onlyPreferredProviders) {
			if (DTOUtilities.equalsSystemInResponseAndRequest(provider, preferredProviderDataDTO.getProviderSystem())) {
				return true;
			}
		}
		
		return false;
	}
}