package eu.arrowhead.core.serviceregistry.quartz.task;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

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
	private final int pageSize = 1000;
	
	@Autowired
	private ServiceRegistryDBService serviceRegistryDBService;
	
	@Value (CommonConstants.$SERVICE_REGISTRY_PING_TIMEOUT_WD)
	private int timeout;
	
	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		checkProvidersReachability();		
	}
	
	private void checkProvidersReachability() {
		int pageIndexCounter = 0;
		Page<ServiceRegistry> pageOfServiceEntries;
		try {
			pageOfServiceEntries = serviceRegistryDBService.getAllServiceReqistryEntries(pageIndexCounter, pageSize, Direction.ASC, CommonConstants.COMMON_FIELD_NAME_ID);
			if (pageOfServiceEntries.isEmpty()) {
				logger.info("Servise Registry database is empty");
			} else {
				final int totalPages = pageOfServiceEntries.getTotalPages();
				pingAndRemoveRegisteredServices(pageOfServiceEntries);
				pageIndexCounter++;
				while (pageIndexCounter < totalPages) {
					pageOfServiceEntries = serviceRegistryDBService.getAllServiceReqistryEntries(pageIndexCounter, pageSize, Direction.ASC, CommonConstants.COMMON_FIELD_NAME_ID);
					pingAndRemoveRegisteredServices(pageOfServiceEntries);
					pageIndexCounter++;
				}
			}
		} catch (IllegalArgumentException exception) {
			logger.debug(exception.getMessage());
		}
	}
	
	private void pingAndRemoveRegisteredServices(final Page<ServiceRegistry> pageOfServiceEntries) {
		for (final ServiceRegistry serviceRegistryEntry : pageOfServiceEntries) {
			final System provider = serviceRegistryEntry.getSystem();
			final String address = provider.getAddress();
			final int port = provider.getPort();
			if (! pingService(address, port)) {
				serviceRegistryDBService.removeServiceRegistryEntryById(serviceRegistryEntry.getId());
			}
		}
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
