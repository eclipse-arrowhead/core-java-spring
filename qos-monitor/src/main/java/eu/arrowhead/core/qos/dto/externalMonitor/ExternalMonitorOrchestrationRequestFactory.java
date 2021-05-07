package eu.arrowhead.core.qos.dto.externalMonitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.dto.internal.ServiceDefinitionRequestDTO;
import eu.arrowhead.common.dto.internal.ServiceInterfaceRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFlags.Flag;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.core.qos.QosMonitorConstants;
import eu.arrowhead.core.qos.service.event.QosMonitorEventType;

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

		final OrchestrationFormRequestDTO orchestrationForm = new OrchestrationFormRequestDTO.Builder( requester )
				.requestedService(requestedService)
				.flag(Flag.ENABLE_INTER_CLOUD, false)
				.flag(Flag.MATCHMAKING, false)
				.flag(Flag.ENABLE_QOS, false)
				.flag(Flag.OVERRIDE_STORE, true)
				.build();

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
	public HashMap<String, String> createExternalPingMonitorServiceMetadataRequirements() {
		logger.debug("createExternalPingMonitorServiceMetadataRequirements started...");

		final HashMap<String, String> externalPingMonitorServiceMetadataRequirements = new HashMap();
		externalPingMonitorServiceMetadataRequirements.put("serviceDataExchangeDescription", "https-request-and-https-costume-ack-response-followed-by-event-sequence");
		externalPingMonitorServiceMetadataRequirements.put("https-request", "externalPingMonitoringRequest.json.schema");
		externalPingMonitorServiceMetadataRequirements.put("https-response", "externalPingMonitoringACKResponse.json.schema");
		externalPingMonitorServiceMetadataRequirements.put("normalEventSequenceList", 
				QosMonitorEventType.RECEIVED_MONITORING_REQUEST.name() + "," +
				QosMonitorEventType.STARTED_MONITORING_MEASUREMENT.name() + "," +
				QosMonitorEventType.FINISHED_MONITORING_MEASUREMENT.name() + ",");
		externalPingMonitorServiceMetadataRequirements.put("optinalEvent", QosMonitorEventType.INTERUPTED_MONITORING_MEASUREMENT.name());
		externalPingMonitorServiceMetadataRequirements.put("optinalEventOccurrence", "anytime");
		externalPingMonitorServiceMetadataRequirements.put(QosMonitorEventType.RECEIVED_MONITORING_REQUEST.name() + "payloadSchema", 
				QosMonitorEventType.RECEIVED_MONITORING_REQUEST.name() + "json.schema");
		externalPingMonitorServiceMetadataRequirements.put(QosMonitorEventType.STARTED_MONITORING_MEASUREMENT.name() + "payloadSchema", 
				QosMonitorEventType.STARTED_MONITORING_MEASUREMENT.name() + "json.schema");
		externalPingMonitorServiceMetadataRequirements.put(QosMonitorEventType.FINISHED_MONITORING_MEASUREMENT.name() + "payloadSchema", 
				QosMonitorEventType.FINISHED_MONITORING_MEASUREMENT.name() + "json.schema");
		externalPingMonitorServiceMetadataRequirements.put(QosMonitorEventType.INTERUPTED_MONITORING_MEASUREMENT.name() + "payloadSchema", 
				QosMonitorEventType.INTERUPTED_MONITORING_MEASUREMENT.name() + "json.schema");
		externalPingMonitorServiceMetadataRequirements.put("echoServiceIsAMust", "false");

		return externalPingMonitorServiceMetadataRequirements;
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceQueryFormDTO createExternalPingMonitorServiceQueryFormDTO() {
		logger.debug("createExternalPingMonitorServiceQueryFormDTO started...");

		final HashMap<String, String> metadata = createExternalPingMonitorServiceMetadataRequirements();

		final ServiceQueryFormDTO externalPingMonitorServiceQueryForm = new ServiceQueryFormDTO();
		externalPingMonitorServiceQueryForm.setInterfaceRequirements(List.of(QosMonitorConstants.EXTERNAL_PING_MONITORING_SERVICE_INTERFACE));
		externalPingMonitorServiceQueryForm.setServiceDefinitionRequirement(QosMonitorConstants.EXTERNAL_PING_MONITORING_SERVICE_DEFINITION);
		externalPingMonitorServiceQueryForm.setSecurityRequirements(List.of(ServiceSecurityType.CERTIFICATE));
		externalPingMonitorServiceQueryForm.setMetadataRequirements(metadata);
		externalPingMonitorServiceQueryForm.setPingProviders(false);

		return externalPingMonitorServiceQueryForm;
	}
}
