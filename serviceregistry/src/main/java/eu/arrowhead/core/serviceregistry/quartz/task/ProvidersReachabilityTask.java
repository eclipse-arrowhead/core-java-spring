package eu.arrowhead.core.serviceregistry.quartz.task;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
public class ProvidersReachabilityTask implements Job {
	
	protected Logger logger = LogManager.getLogger(ProvidersReachabilityTask.class);
	private final int pageSize = 1000;
	
	@Autowired
	private ServiceRegistryDBService serviceRegistryDBService;
	
	@Value ("${ping_timeout}")
	private int timeout;
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		checkProvidersReachability();		
	}
	
	private void checkProvidersReachability() {
		int pageIndexCounter = 0;
		Page<ServiceRegistry> pageOfServiceEntries = serviceRegistryDBService.getAllServiceReqistryEntries(pageIndexCounter, pageSize, Direction.ASC, CommonConstants.COMMON_FIELD_NAME_ID);
		if (pageOfServiceEntries.isEmpty()) {
			logger.info("PROVIDERS REACHABILITY TASK: Servise Registry database is empty");
		} else {
			int totalPages = pageOfServiceEntries.getTotalPages();		
			while (pageIndexCounter < totalPages) {
				if (pageIndexCounter != 0) {
					pageOfServiceEntries = serviceRegistryDBService.getAllServiceReqistryEntries(pageIndexCounter, pageSize, Direction.ASC, CommonConstants.COMMON_FIELD_NAME_ID);
					pingAndRemoveRegisteredServices(pageOfServiceEntries);
				} else {
					pingAndRemoveRegisteredServices(pageOfServiceEntries);
				}
				pageIndexCounter++;
			}
		}
	}
	
	private void pingAndRemoveRegisteredServices(Page<ServiceRegistry> pageOfServiceEntries) {
		for (ServiceRegistry serviceRegistryEntry : pageOfServiceEntries) {
			System provider = serviceRegistryEntry.getSystem();
			String address = provider.getAddress();
			int port = provider.getPort();
			if (! pingService(address, port)) {
				serviceRegistryDBService.removeServiceRegistryEntryById(serviceRegistryEntry.getId());
			}
		}
	}
	
	private boolean pingService(String address, int port) {
		InetSocketAddress providerHost = new InetSocketAddress(address, port);
		try (Socket socket = new Socket()) {
			socket.connect(providerHost, timeout);
			return true;
		} catch (IOException exception) {
			return false;
		}
	}
	
}
