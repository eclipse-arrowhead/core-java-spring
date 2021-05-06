package eu.arrowhead.core.qos.dto.externalMonitor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.arrowhead.common.dto.internal.ServiceDefinitionRequestDTO;
import eu.arrowhead.common.dto.internal.ServiceInterfaceRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;

public class ExternalMonitorOrchestrationRequestFactory {

	//=================================================================================================
	// members

	private Logger logger = LogManager.getLogger(ExternalMonitorOrchestrationRequestFactory.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public OrchestrationFormRequestDTO createExternalMonitorOrchestrationRequest() {
		logger.debug("createExternalMonitorOrchestrationRequest started...");

		final SystemRequestDTO requester = new SystemRequestDTO();
		final ServiceDefinitionRequestDTO serviceDefinition = new ServiceDefinitionRequestDTO();
		final ServiceInterfaceRequestDTO serviceInterface = new ServiceInterfaceRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();

		final OrchestrationFormRequestDTO orchestrationForm = new OrchestrationFormRequestDTO();
		orchestrationForm.setRequesterSystem(requester);
		orchestrationForm.setRequestedService(requestedService);

		return orchestrationForm;
	}
}
