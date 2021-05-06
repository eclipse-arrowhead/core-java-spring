package eu.arrowhead.core.qos.dto.externalMonitor;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreSystemRegistrationProperties;
import eu.arrowhead.common.dto.internal.ServiceDefinitionRequestDTO;
import eu.arrowhead.common.dto.internal.ServiceInterfaceRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormListDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.core.qos.QosMonitorConstants;

public class ExternalMonitorOrchestrationRequestFactory {

	//=================================================================================================
	// members

	@Value(CoreCommonConstants.$CORE_SYSTEM_NAME)
	private String coreSystemName;

	@Value(CoreCommonConstants.$SERVER_ADDRESS)
	private String coreSystemAddress;

	@Value(CoreCommonConstants.$SERVER_PORT)
	private int coreSystemPort;

	private Logger logger = LogManager.getLogger(ExternalMonitorOrchestrationRequestFactory.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public OrchestrationFormRequestDTO createExternalMonitorOrchestrationRequest() {
		logger.debug("createExternalMonitorOrchestrationRequest started...");

		final SystemRequestDTO requester = createExternalMonitorSystemRequestDTO();
		final ServiceQueryFormDTO requestedService = createExternalPingMonitorServiceQueryFormDTO();

		final OrchestrationFormRequestDTO orchestrationForm = new OrchestrationFormRequestDTO();
		orchestrationForm.setRequesterSystem(requester);
		orchestrationForm.setRequestedService(requestedService);

		return orchestrationForm;
	}

	//-------------------------------------------------------------------------------------------------
	public SystemRequestDTO createExternalMonitorSystemRequestDTO() {
		logger.debug("createExternalMonitorSystemRequestDTO started...");

		final SystemRequestDTO requester = new SystemRequestDTO();
		requester.setSystemName(coreSystemName);
		requester.setAddress(coreSystemAddress);
		requester.setPort(coreSystemPort);
		requester.setMetadata(Map.of());

		return requester;
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceDefinitionRequestDTO createExternalPingMonitorServiceDefinitionRequestDTO() {
		logger.debug("createExternalMonitorServiceDefinitionRequestDTO started...");

		final ServiceDefinitionRequestDTO externalPingMonitorServiceDefinition = new ServiceDefinitionRequestDTO();
		externalPingMonitorServiceDefinition.setServiceDefinition(QosMonitorConstants.EXTERNAL_PING_MONITORING_SERVICE_DEFINITION);

		return externalPingMonitorServiceDefinition;
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceInterfaceRequestDTO createExternalPingMonitorServiceInterfaceRequestDTO() {
		logger.debug("createExternalPingMonitorServiceInterfaceRequestDTO started...");

		final ServiceInterfaceRequestDTO externalPingMonitorServiceInterface = new ServiceInterfaceRequestDTO();
		externalPingMonitorServiceInterface.setInterfaceName(QosMonitorConstants.EXTERNAL_PING_MONITORING_SERVICE_INTERFACE);

		return externalPingMonitorServiceInterface;
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceQueryFormDTO createExternalPingMonitorServiceQueryFormDTO() {
		logger.debug("createExternalPingMonitorServiceQueryFormDTO started...");

		final ServiceDefinitionRequestDTO serviceDefinition = createExternalPingMonitorServiceDefinitionRequestDTO();
		final ServiceInterfaceRequestDTO serviceInterface = createExternalPingMonitorServiceInterfaceRequestDTO();

		final ServiceQueryFormDTO externalPingMonitorServiceQueryForm = new ServiceQueryFormDTO();
		//TODO fill form here

		return externalPingMonitorServiceQueryForm;
	}
}
