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

package eu.arrowhead.core.serviceregistry.quartz.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.database.entity.ServiceRegistry;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.core.serviceregistry.database.service.RegistryUtils;
import eu.arrowhead.core.serviceregistry.database.service.ServiceRegistryDBService;

@Component
@DisallowConcurrentExecution
public class ProvidersReachabilityTask implements Job {
	
	//=================================================================================================
	// members
	
	protected final Logger logger = LogManager.getLogger(ProvidersReachabilityTask.class);
	private static final int PAGE_SIZE = 1000;
	
	@Autowired
	private ServiceRegistryDBService serviceRegistryDBService;
	
	@Value(CoreCommonConstants.$SERVICEREGISTRY_PING_TIMEOUT_WD)
	private int timeout;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		logger.debug("STARTED: Providers reachability task");
		
		final List<ServiceRegistry> removedServiceRegistries = checkProvidersReachability();
		
		logger.debug("FINISHED: Providers reachability task. Number of removed service registry entry: {}", removedServiceRegistries.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<ServiceRegistry> checkProvidersReachability() {
		final List<ServiceRegistry> removedServiceRegistryEntries = new ArrayList<>();
		int pageIndexCounter = 0;
		try {
			Page<ServiceRegistry> pageOfServiceEntries = serviceRegistryDBService.getServiceRegistryEntries(pageIndexCounter, PAGE_SIZE, Direction.ASC, CoreCommonConstants.COMMON_FIELD_NAME_ID);
			if (pageOfServiceEntries.isEmpty()) {
				logger.debug("Service Registry database is empty");
			} else {
				final int totalPages = pageOfServiceEntries.getTotalPages();
				removedServiceRegistryEntries.addAll(pingAndRemoveRegisteredServices(pageOfServiceEntries));
				pageIndexCounter++;
				while (pageIndexCounter < totalPages) {
					pageOfServiceEntries = serviceRegistryDBService.getServiceRegistryEntries(pageIndexCounter, PAGE_SIZE, Direction.ASC, CoreCommonConstants.COMMON_FIELD_NAME_ID);
					removedServiceRegistryEntries.addAll(pingAndRemoveRegisteredServices(pageOfServiceEntries));
					pageIndexCounter++;
				}
			}
		} catch (final IllegalArgumentException ex) {
			logger.debug(ex.getMessage(), ex);
		}
		
		return removedServiceRegistryEntries;
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private List<ServiceRegistry> pingAndRemoveRegisteredServices(final Page<ServiceRegistry> pageOfServiceEntries) {
		final List<ServiceRegistry> toBeRemoved = new ArrayList<>();
		for (final ServiceRegistry serviceRegistryEntry : pageOfServiceEntries) {
			final System provider = serviceRegistryEntry.getSystem();
			final String address = provider.getAddress();
			final int port = provider.getPort();
			if (!RegistryUtils.pingService(address, port, timeout)) {				
				toBeRemoved.add(serviceRegistryEntry);
				logger.debug("REMOVED: {}", serviceRegistryEntry);
			}
		}
		
		serviceRegistryDBService.removeBulkOfServiceRegistryEntries(toBeRemoved);
		
		return toBeRemoved;
	}
}