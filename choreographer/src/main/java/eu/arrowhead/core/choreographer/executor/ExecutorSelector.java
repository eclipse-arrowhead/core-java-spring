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

package eu.arrowhead.core.choreographer.executor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorServiceInfoResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormListDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultListDTO;
import eu.arrowhead.core.choreographer.database.service.ChoreographerExecutorDBService;
import eu.arrowhead.core.choreographer.database.service.ChoreographerSessionDBService;
import eu.arrowhead.core.choreographer.service.ChoreographerDriver;

@Component
public class ExecutorSelector {
	
	//=================================================================================================
	// methods
	
	@Autowired
	private ChoreographerExecutorDBService executorDBService;
	
	@Autowired
	private ChoreographerSessionDBService sessionDBService;
	
	@Autowired
	private ChoreographerDriver driver;
	
	@Autowired
	private ExecutorPrioritizationStrategy prioritizationStrategy;
	
	private static final Logger logger = LogManager.getLogger(ExecutorSelector.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ChoreographerExecutor select(final String serviceDefinition, final Integer minVersion, final Integer maxVersion, final Set<Long> exclusions) {
		return selectAndInit(null, null, serviceDefinition, minVersion, maxVersion, exclusions, false);
	}

	//-------------------------------------------------------------------------------------------------
	public ChoreographerExecutor selectAndInit(final Long sessionId, final Long stepId, final String serviceDefinition, final Integer minVersion, final Integer maxVersion, final Set<Long> exclusions, final boolean init) {
		//exclusions: when a ChoreographerSessionStep failed due to executor issue, then selection can be repeated but without that executor(s)
		logger.debug("selectAndInit started...");
		Assert.isTrue(!Utilities.isEmpty(serviceDefinition), "serviceDefinition is empty");
		if (init) {
			Assert.isTrue(sessionId != null && sessionId >= 1, "Invalid session id");
			Assert.isTrue(stepId != null && stepId >= 1, "Invalid step id");
		}
		
		final int _minVersion = minVersion == null ? Defaults.DEFAULT_VERSION : minVersion; 
		final int _maxVersion = maxVersion == null ? Integer.MAX_VALUE : maxVersion;
		
		List<ChoreographerExecutor> potentials = executorDBService.getExecutorsByServiceDefinitionAndVersion(serviceDefinition, _minVersion, _maxVersion);
		potentials = filterOutLockedAndExcludedExecutors(potentials, exclusions);
		if (potentials.isEmpty()) {
			return null;
		}
		
		final Map<Long,ChoreographerExecutorServiceInfoResponseDTO> executorServiceInfos = collectExecutorServiceInfos(potentials, serviceDefinition, _minVersion, _maxVersion);
		potentials = filterOutExecutorsWithoutServiceInfos(potentials, executorServiceInfos);
		potentials = prioritizationStrategy.prioritize(potentials, executorServiceInfos);
		
		return selectVerifyAndInitFirstAvailable(sessionId, stepId, potentials, executorServiceInfos, init);
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private List<ChoreographerExecutor> filterOutLockedAndExcludedExecutors(final List<ChoreographerExecutor> original, final Set<Long> exclusions) {
		logger.debug("filterOutLockedAndExcludedExecutors started...");
		
		final Set<Long> excudedIds = exclusions == null? Set.of() : exclusions;
		
		final List<ChoreographerExecutor> filtered = new ArrayList<>(original.size());
		for (final ChoreographerExecutor executor : original) {
			if (!executor.isLocked() && !excudedIds.contains(executor.getId())) {
				filtered.add(executor);
			}
		}			
		return filtered;		
	}
	
	//-------------------------------------------------------------------------------------------------
	private Map<Long,ChoreographerExecutorServiceInfoResponseDTO> collectExecutorServiceInfos(final List<ChoreographerExecutor> potentials, final String serviceDefinition,
																							  final int minVersion, final int maxVersion) {
		logger.debug("collectExecutorServiceInfos started...");
		
		final Map<Long,ChoreographerExecutorServiceInfoResponseDTO> collected = new HashMap<>(potentials.size());
		for (final ChoreographerExecutor executor : potentials) {
			try {
				final ChoreographerExecutorServiceInfoResponseDTO info = driver.queryExecutorServiceInfo(executor.getAddress(), executor.getPort(), executor.getBaseUri(),
																										 serviceDefinition, minVersion, maxVersion);
				collected.put(executor.getId(), info);
				
			} catch (final Exception ex) {
				logger.debug(ex.getMessage());
			}
		}
		return collected;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<ChoreographerExecutor> filterOutExecutorsWithoutServiceInfos(final List<ChoreographerExecutor> original,
																			  final Map<Long,ChoreographerExecutorServiceInfoResponseDTO> executorServiceInfos) {
		logger.debug("filterOutExecutorsWithoutServiceInfos started...");
		
		final List<ChoreographerExecutor> filtered = new ArrayList<>(executorServiceInfos.keySet().size());
		for (final ChoreographerExecutor executor : original) {
			if (executorServiceInfos.containsKey(executor.getId())) {
				filtered.add(executor);
			}
		}			
		return filtered;
	}
	
	//-------------------------------------------------------------------------------------------------
	private ChoreographerExecutor selectVerifyAndInitFirstAvailable(final Long sessionId, final Long stepId, final List<ChoreographerExecutor> potentials, final Map<Long,ChoreographerExecutorServiceInfoResponseDTO> executorServiceInfos,
																	final boolean init) {
		logger.debug("selectVerifyAndInitFirstAvailable started...");
		
		if (potentials.isEmpty()) {
			return null;
		}
		
		for (final ChoreographerExecutor potential : potentials) {
			final Optional<ChoreographerExecutor> optional = executorDBService.getExecutorOptionalById(potential.getId()); //refreshing from DB
			if (optional.isEmpty()) {
				continue;
			}
			final ChoreographerExecutor executor = optional.get();
			if (!executor.isLocked()) {
				if (verifyReliedServices(executorServiceInfos.get(executor.getId()))) {
					if (init) {
						sessionDBService.registerSessionStep(sessionId, stepId, executor.getId());					
					}
					return executor;
				}
			}
		}
		return null;
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean verifyReliedServices(final ChoreographerExecutorServiceInfoResponseDTO serviceInfo) {
		logger.debug("verifyExecutorSR started...");
		
		final Set<String> availableReliedServices = new HashSet<>(serviceInfo.getDependencies().size());
		final ServiceQueryResultListDTO response = driver.multiQueryServiceRegistry(new ServiceQueryFormListDTO(serviceInfo.getDependencies()));
		for (final ServiceQueryResultDTO result : response.getResults()) {
			if (!result.getServiceQueryData().isEmpty()) {
				final String serviceDefinition = result.getServiceQueryData().get(0).getServiceDefinition().getServiceDefinition();
				availableReliedServices.add(serviceDefinition);
			}
		}
		
		return availableReliedServices.size() == serviceInfo.getDependencies().size();
	}
}
