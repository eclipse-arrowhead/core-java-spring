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

package eu.arrowhead.common.quartz.uricrawler;

import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.http.HttpService;

@Component
@DisallowConcurrentExecution
public class UriCrawlerTask implements Job {
	
	//=================================================================================================
	// members
	
	private static final String URI_CRAWLER_TASK_SCHEDULER = "uriCrawlerTaskScheduler";

	private final Logger logger = LogManager.getLogger(UriCrawlerTask.class);
	
	@Resource(name = URI_CRAWLER_TASK_SCHEDULER)
	private Scheduler uriCrawlerTaskScheduler;
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Autowired
	private HttpService httpService;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {  
		logger.debug("STARTED: URI crawler task");
		
		try {
			if (uriCrawlerTaskScheduler.isShutdown()) {
				return;
			}
		} catch (final SchedulerException ex) {
			logger.error(ex.getMessage());
			logger.debug("Stacktrace:", ex);
		}
		
		if (arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)) {
			logger.debug("FINISHED: URI crawler task can not run if server is in standalone mode");
			shutdown();
			return;
		}
		
		final List<CoreSystemService> requiredServices = getRequiredServices();
		if (requiredServices.isEmpty()) {
			logger.debug("FINISHED: URI crawler task. Have no required core services");
			shutdown();
			return;
		}
		
		final UriComponents queryUri = getQueryUri();
		checkServiceRegistryConnection(queryUri);
		int count = 0;
		for (final CoreSystemService coreSystemService : requiredServices) {
			if (findCoreSystemServiceUri(coreSystemService, queryUri)) {
				count++;
			}
		}
		
		logger.debug("FINISHED: URI crawler task. Number of acquired URI: {}/{}", count, requiredServices.size());
		
		if (count >= requiredServices.size()) {
			shutdown();
		}
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	@PreDestroy
	private void shutdown() {
		logger.debug("shutdown started...");
		try {
			if (!uriCrawlerTaskScheduler.isShutdown()) {
				uriCrawlerTaskScheduler.shutdown();
				logger.debug("SHUTDOWN: URI crawler task.");
			}
		} catch (final SchedulerException ex) {
			logger.error(ex.getMessage());
			logger.debug("Stacktrace:", ex);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	private List<CoreSystemService> getRequiredServices() throws JobExecutionException {
		logger.debug("getRequiredServices started...");
		
		if (arrowheadContext.containsKey(CoreCommonConstants.REQUIRED_URI_LIST)) {
			try {
				return (List<CoreSystemService>) arrowheadContext.get(CoreCommonConstants.REQUIRED_URI_LIST);
			} catch (final ClassCastException ex) {
				throw new JobExecutionException("URI crawler task can't find required services list.");
			}
		}
		
		waitBeforeRetry();
		throw new JobExecutionException("URI crawler task can't find required services list.", true);
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getQueryUri() throws JobExecutionException {
		logger.debug("getQueryUri started...");
		
		if (arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_URI)) {
			try {
				return (UriComponents) arrowheadContext.get(CoreCommonConstants.SR_QUERY_URI);
			} catch (final ClassCastException ex) {
				throw new JobExecutionException("URI crawler task can't find Service Registry Query URI.");
			}
		}
		
		waitBeforeRetry();
		throw new JobExecutionException("URI crawler task can't find Service Registry Query URI.", true);
	}
	
	//-------------------------------------------------------------------------------------------------
	private void waitBeforeRetry() {
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException ex) {
			// intentionally blank
		}
	}
 	
	//-------------------------------------------------------------------------------------------------
	private void checkServiceRegistryConnection(final UriComponents queryUri) throws JobExecutionException {
		logger.debug("checkServiceRegistryConnection started...");
	
		final UriComponents echoUri = createEchoUri(queryUri);
		try {
			httpService.sendRequest(echoUri, HttpMethod.GET, String.class);
		} catch (final ArrowheadException ex) {
			waitBeforeRetry();
			throw new JobExecutionException("URI crawler task can't access Service Registry.", true);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents createEchoUri(final UriComponents queryUri) {
		logger.debug("createEchoUri started...");
				
		final String scheme = queryUri.getScheme();
		final String echoUriStr = CommonConstants.SERVICEREGISTRY_URI + CommonConstants.ECHO_URI;
		return Utilities.createURI(scheme, queryUri.getHost(), queryUri.getPort(), echoUriStr);
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean findCoreSystemServiceUri(final CoreSystemService coreSystemService, final UriComponents queryUri) {
		logger.debug("findCoreSystemServiceUri started...");
		
		final String key = coreSystemService.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
		if (isAlreadyFound(key)) {
			return true;
		}
		
		final ServiceQueryFormDTO form = new ServiceQueryFormDTO.Builder(coreSystemService.getServiceDefinition()).build();
		try {
			final ResponseEntity<ServiceQueryResultDTO> response = httpService.sendRequest(queryUri, HttpMethod.POST, ServiceQueryResultDTO.class, form);
			final ServiceQueryResultDTO result = response.getBody();
			if (!result.getServiceQueryData().isEmpty()) {
				final int lastIdx = result.getServiceQueryData().size() - 1; // to make sure we use the newest one if some old entries are in the DB
				final ServiceRegistryResponseDTO entry = result.getServiceQueryData().get(lastIdx);
				final String scheme = entry.getSecure() == ServiceSecurityType.NOT_SECURE ? CommonConstants.HTTP : CommonConstants.HTTPS;
				final UriComponents uri = Utilities.createURI(scheme, entry.getProvider().getAddress(), entry.getProvider().getPort(), entry.getServiceUri());
				arrowheadContext.put(key, uri);
				
				return true;
			}
		} catch (final Exception ex) {
			logger.error(ex.getMessage());
			logger.debug("Stacktrace:", ex);
		}
		
		return false;
	}

	//-------------------------------------------------------------------------------------------------
	private boolean isAlreadyFound(final String key) {
		return arrowheadContext.containsKey(key) && (arrowheadContext.get(key) instanceof UriComponents); 
	}
}