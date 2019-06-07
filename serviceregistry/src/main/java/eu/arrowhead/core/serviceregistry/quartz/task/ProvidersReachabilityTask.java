package eu.arrowhead.core.serviceregistry.quartz.task;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
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

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.database.entity.ServiceRegistry;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.service.ServiceRegistryDBService;

@Component
@DisallowConcurrentExecution
public class ProvidersReachabilityTask implements Job {
	
	protected Logger logger = LogManager.getLogger(ProvidersReachabilityTask.class);
	private static final int PAGE_SIZE = 1000;
	
	@Autowired
	private ServiceRegistryDBService serviceRegistryDBService;
	
	@Value(CommonConstants.$SERVICE_REGISTRY_PING_TIMEOUT_WD)
	private int timeout;
	
	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		logger.debug("STARTED: Providers reachability task");
		final List<ServiceRegistry> removedServiceRegistries = checkProvidersReachability();
		logger.debug("FINISHED: Providers reachability task. Number of removed service registry entry: {}", removedServiceRegistries.size());
	}
	
	public List<ServiceRegistry> checkProvidersReachability() {
		final List<ServiceRegistry> removedServiceRegistryEntries = new ArrayList<>();
		int pageIndexCounter = 0;
		Page<ServiceRegistry> pageOfServiceEntries;
		try {
			pageOfServiceEntries = serviceRegistryDBService.getServiceReqistryEntries(pageIndexCounter, PAGE_SIZE, Direction.ASC, CommonConstants.COMMON_FIELD_NAME_ID);
			if (pageOfServiceEntries.isEmpty()) {
				logger.debug("Servise Registry database is empty");
			} else {
				final int totalPages = pageOfServiceEntries.getTotalPages();
				removedServiceRegistryEntries.addAll(pingAndRemoveRegisteredServices(pageOfServiceEntries));
				pageIndexCounter++;
				while (pageIndexCounter < totalPages) {
					pageOfServiceEntries = serviceRegistryDBService.getServiceReqistryEntries(pageIndexCounter, PAGE_SIZE, Direction.ASC, CommonConstants.COMMON_FIELD_NAME_ID);
					removedServiceRegistryEntries.addAll(pingAndRemoveRegisteredServices(pageOfServiceEntries));
					pageIndexCounter++;
				}
			}
		} catch (final IllegalArgumentException exception) {
			logger.debug(exception.getMessage());
		}
		
		return removedServiceRegistryEntries;
	}
	
	private List<ServiceRegistry> pingAndRemoveRegisteredServices(final Page<ServiceRegistry> pageOfServiceEntries) {
		final List<ServiceRegistry> toBeRemoved = new ArrayList<>();
		for (final ServiceRegistry serviceRegistryEntry : pageOfServiceEntries) {
			final System provider = serviceRegistryEntry.getSystem();
			final String address = provider.getAddress();
			final int port = provider.getPort();
			if (! pingService(address, port)) {				
				toBeRemoved.add(serviceRegistryEntry);
				logger.debug("REMOVED: {}", serviceRegistryEntry);
			}
		}
		serviceRegistryDBService.removeBulkOfServiceRegistryEntries(toBeRemoved);
		
		return toBeRemoved;
	}
	
	private boolean pingService(final String address, final int port) {
		final InetSocketAddress providerHost = new InetSocketAddress(address, port);
		try (Socket socket = new Socket()) {
			socket.connect(providerHost, timeout);
			return true;
		} catch (final IOException exception) {
			return false;
		}
	}
}