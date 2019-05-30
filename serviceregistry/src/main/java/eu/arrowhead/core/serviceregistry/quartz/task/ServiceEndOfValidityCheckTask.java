package eu.arrowhead.core.serviceregistry.quartz.task;

import java.time.ZonedDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
public class ServiceEndOfValidityCheckTask implements Job {
	
	protected Logger logger = LogManager.getLogger(ServiceEndOfValidityCheckTask.class);
	private final int pageSize = 1000;
	
	@Autowired
	private ServiceRegistryDBService serviceRegistryDBService;

	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		checkServicesEndOfValidity();		
	}
	
	private void checkServicesEndOfValidity() {		
		int pageIndexCounter = 0;
		Page<ServiceRegistry> pageOfServiceEntries;
		try {
			pageOfServiceEntries = serviceRegistryDBService.getAllServiceReqistryEntries(pageIndexCounter, pageSize, Direction.ASC, CommonConstants.COMMON_FIELD_NAME_ID);
			if (pageOfServiceEntries.isEmpty()) {
				logger.info("Servise Registry database is empty");
			} else {
				final int totalPages = pageOfServiceEntries.getTotalPages();
				removeRegisteredServicesWithInvalidTTL(pageOfServiceEntries);
				pageIndexCounter++;
				while (pageIndexCounter < totalPages) {
					pageOfServiceEntries = serviceRegistryDBService.getAllServiceReqistryEntries(pageIndexCounter, pageSize, Direction.ASC, CommonConstants.COMMON_FIELD_NAME_ID);
					removeRegisteredServicesWithInvalidTTL(pageOfServiceEntries);
					pageIndexCounter++;
				}
			}
		} catch (IllegalArgumentException exception) {
			logger.debug(exception.getMessage());
		}
	}
	
	private void removeRegisteredServicesWithInvalidTTL(final Page<ServiceRegistry> pageOfServiceEntries) {
		for (final ServiceRegistry serviceRegistryEntry : pageOfServiceEntries) {
			final ZonedDateTime endOfValidity = serviceRegistryEntry.getEndOfValidity();
			if (endOfValidity != null && ! isTTLValid(endOfValidity)) {
				serviceRegistryDBService.removeServiceRegistryEntryById(serviceRegistryEntry.getId());
			}
		}
	}
	
	private boolean isTTLValid(final ZonedDateTime endOfValidity) {
		return endOfValidity.isAfter(ZonedDateTime.now());
	}
	
}
