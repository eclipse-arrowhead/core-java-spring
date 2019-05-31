package eu.arrowhead.core.serviceregistry.quartz.task;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.database.entity.ServiceRegistry;
import eu.arrowhead.common.database.service.ServiceRegistryDBService;

@Component
@DisallowConcurrentExecution
public class ServiceEndOfValidityCheckTask implements Job {
	
	protected Logger logger = LogManager.getLogger(ServiceEndOfValidityCheckTask.class);
	private final int pageSize = 1000;
	
	@Autowired
	private ServiceRegistryDBService serviceRegistryDBService;

	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		logger.info("STARTED: Services end of validity check task");
		final List<Long> removedServiceRegistryIDs = checkServicesEndOfValidity();
		logger.info("FINISHED: Services end of validity check task. Number of removed service registry entry: " + removedServiceRegistryIDs.size());
	}
	
	public List<Long> checkServicesEndOfValidity() {
		final List<Long> removedServiceRegistryIDs = new ArrayList<>();
		int pageIndexCounter = 0;
		Page<ServiceRegistry> pageOfServiceEntries;
		try {
			pageOfServiceEntries = serviceRegistryDBService.getAllServiceReqistryEntries(pageIndexCounter, pageSize, Direction.ASC, CommonConstants.COMMON_FIELD_NAME_ID);
			if (pageOfServiceEntries.isEmpty()) {
				logger.debug("Servise Registry database is empty");
			} else {
				final int totalPages = pageOfServiceEntries.getTotalPages();
				removedServiceRegistryIDs.addAll(removeRegisteredServicesWithInvalidTTL(pageOfServiceEntries));
				pageIndexCounter++;
				while (pageIndexCounter < totalPages) {
					pageOfServiceEntries = serviceRegistryDBService.getAllServiceReqistryEntries(pageIndexCounter, pageSize, Direction.ASC, CommonConstants.COMMON_FIELD_NAME_ID);
					removedServiceRegistryIDs.addAll(removeRegisteredServicesWithInvalidTTL(pageOfServiceEntries));
					pageIndexCounter++;
				}
			}
		} catch (final IllegalArgumentException exception) {
			logger.debug(exception.getMessage());
		}
		return removedServiceRegistryIDs;
	}
	
	private List<Long> removeRegisteredServicesWithInvalidTTL(final Page<ServiceRegistry> pageOfServiceEntries) {
		final List<Long> removedServiceRegistryIDs = new ArrayList<>();
		for (final ServiceRegistry serviceRegistryEntry : pageOfServiceEntries) {
			final ZonedDateTime endOfValidity = serviceRegistryEntry.getEndOfValidity();
			if (endOfValidity != null && ! isTTLValid(endOfValidity)) {
				serviceRegistryDBService.removeServiceRegistryEntryById(serviceRegistryEntry.getId());
				removedServiceRegistryIDs.add(serviceRegistryEntry.getId());
				logger.debug("REMOVED: " + serviceRegistryEntry);
			}
		}
		return removedServiceRegistryIDs;
	}
	
	private boolean isTTLValid(final ZonedDateTime endOfValidity) {
		return endOfValidity.isAfter(ZonedDateTime.now());
	}
	
}
