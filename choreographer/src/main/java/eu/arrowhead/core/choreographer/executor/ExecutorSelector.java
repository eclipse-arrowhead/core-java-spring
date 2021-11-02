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
import eu.arrowhead.common.database.entity.ChoreographerStep;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorServiceInfoResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormListDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
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
	
	@Autowired
	private ExecutorMeasurementStrategy measurementStrategy;
	
	private static final Logger logger = LogManager.getLogger(ExecutorSelector.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ExecutorData select(final String serviceDefinition, final Integer minVersion, final Integer maxVersion, final Set<Long> exclusions, final boolean allowInterCloud) {
		return selectAndInit(null, null, serviceDefinition, minVersion, maxVersion, exclusions, allowInterCloud, false, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	public ExecutorData selectAndInit(final long sessionId, final ChoreographerStep step, final Set<Long> exclusions, final boolean allowInterCloud, final boolean chooseOptimal, final boolean init) {
		return selectAndInit(sessionId, step.getId(), step.getServiceDefinition(), step.getMinVersion(), step.getMaxVersion(), exclusions, allowInterCloud, chooseOptimal, init);
	}

	//-------------------------------------------------------------------------------------------------
	public ExecutorData selectAndInit(final Long sessionId, final Long stepId, final String serviceDefinition, final Integer minVersion, final Integer maxVersion, final Set<Long> exclusions, final boolean allowInterCloud, final boolean chooseOptimal,
									  final boolean init) { 
		// exclusions: when a ChoreographerSessionStep failed due to executor issue, then selection can be repeated but without that executor(s)
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
		
		return selectVerifyAndInitFirstAvailable(sessionId, stepId, potentials, executorServiceInfos, allowInterCloud, chooseOptimal, init);
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
				logger.warn("Problem while querying service info for executor {}", executor.getName());
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
	private ExecutorData selectVerifyAndInitFirstAvailable(final Long sessionId, final Long stepId, final List<ChoreographerExecutor> potentials, final Map<Long,ChoreographerExecutorServiceInfoResponseDTO> executorServiceInfos,
														   final boolean allowInterCloud, final boolean chooseOptimal, final boolean init) {
		logger.debug("selectVerifyAndInitFirstAvailable started...");
		
		if (potentials.isEmpty()) {
			return null;
		}
		
		ExecutorData bestCandidate = null;
		int bestCloudMeasurement = Integer.MAX_VALUE;
		for (final ChoreographerExecutor potential : potentials) {
			final Optional<ChoreographerExecutor> optional = executorDBService.getExecutorOptionalById(potential.getId()); // refreshing from DB
			if (optional.isEmpty()) {
				continue;
			}
			
			final ChoreographerExecutor executor = optional.get();
			if (!executor.isLocked()) {
				final SystemRequestDTO executorSystem = DTOConverter.convertSystemResponseDTOToSystemRequestDTO(driver.queryServiceRegistryBySystem(executor.getName(),
						  																															executor.getAddress(),
						  																															executor.getPort()));			
				
				final Map<Integer,List<String>> reliedServicesResponse = verifyReliedServices(executor.getName(), executorServiceInfos.get(executor.getId()), allowInterCloud);
				if (reliedServicesResponse != null) { // means executor is verified
					final boolean useOtherClouds = isExecutorUseOtherClouds(reliedServicesResponse);
					final ExecutorData executorData = new ExecutorData(executor, executorSystem, executorServiceInfos.get(executor.getId()).getDependencies(), useOtherClouds);
					
					if (!chooseOptimal || !useOtherClouds) { // local executor is always the most optimal
						if (init) {
							sessionDBService.registerSessionStep(sessionId, stepId, executor.getId());					
						}
						
						return executorData;
					}
					
					final int cloudMeasurement = measurementStrategy.getMeasurement(reliedServicesResponse);
					if (cloudMeasurement < bestCloudMeasurement) {
						bestCandidate = executorData;
						bestCloudMeasurement = cloudMeasurement;
					}
				}
			}
		}
		
		if (chooseOptimal && bestCandidate != null && init) {
			sessionDBService.registerSessionStep(sessionId, stepId, bestCandidate.getExecutor().getId());					
		}
		
		return bestCandidate;
	}
	
	//-------------------------------------------------------------------------------------------------
	private Map<Integer,List<String>> verifyReliedServices(final String executorName, final ChoreographerExecutorServiceInfoResponseDTO serviceInfo, final boolean allowInterCloud) {
		logger.debug("verifyReliedServices started...");
		
		if (Utilities.isEmpty(serviceInfo.getDependencies())) { // no dependencies
			return Map.of();
		}
		
		try {
			final Map<Integer,List<String>> response = driver.searchForServices(new ServiceQueryFormListDTO(serviceInfo.getDependencies()), allowInterCloud);
			
			for (final List<String> cloudList : response.values()) {
				if (cloudList.isEmpty()) {
					return null;
				}
			}
			
			return response;
		} catch (final Exception ex) {
			logger.warn("Problem while verifying service info for executor {}", executorName);
			logger.debug(ex.getMessage());
			
			return null;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean isExecutorUseOtherClouds(final Map<Integer,List<String>> serviceData) {
		logger.debug("isExecutorUseOtherClouds started...");
	
		for (final List<String> cloudList : serviceData.values()) {
			if (!ChoreographerDriver.OWN_CLOUD_MARKER.equals(cloudList.get(0))) {
				return true;
			}
		}
		
		return false;
	}
}