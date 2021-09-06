package eu.arrowhead.core.qos.dto.externalMonitor;

import java.security.PublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.dto.shared.OrchestrationFlags.Flag;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.core.qos.QosMonitorConstants;

@Service
public class ExternalMonitorOrchestrationRequestFactory {

	//=================================================================================================
	// members

	@Autowired
	private SSLProperties sslProperties;

	@Value(CoreCommonConstants.$CORE_SYSTEM_NAME)
	private String coreSystemName;

	@Value(CoreCommonConstants.$SERVER_ADDRESS)
	private String coreSystemAddress;
	
	@Value(CoreCommonConstants.$SERVER_PORT)
	private int coreSystemPort;

	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;

	private SystemRequestDTO requesterSystem;

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

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private SystemRequestDTO createExternalMonitorSystemRequestDTO() {
		logger.debug("createExternalMonitorSystemRequestDTO started...");

		if(requesterSystem == null) {
			requesterSystem = new SystemRequestDTO();
			requesterSystem.setSystemName(coreSystemName);
			requesterSystem.setAddress(coreSystemAddress);
			requesterSystem.setPort(coreSystemPort);
			requesterSystem.setMetadata(null);

			if (sslProperties.isSslEnabled()) {

				final PublicKey publicKey = (PublicKey) arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY);
				requesterSystem.setAuthenticationInfo(Base64.getEncoder().encodeToString(publicKey.getEncoded()));

			}
		}

		return requesterSystem;
	}

	//-------------------------------------------------------------------------------------------------
	private ServiceQueryFormDTO createExternalPingMonitorServiceQueryFormDTO() {
		logger.debug("createExternalPingMonitorServiceQueryFormDTO started...");

		final ServiceQueryFormDTO externalPingMonitorServiceQueryForm = new ServiceQueryFormDTO();
		externalPingMonitorServiceQueryForm.setInterfaceRequirements(List.of(QosMonitorConstants.EXTERNAL_PING_MONITORING_SERVICE_INTERFACE));
		externalPingMonitorServiceQueryForm.setServiceDefinitionRequirement(QosMonitorConstants.EXTERNAL_PING_MONITORING_SERVICE_DEFINITION);
		externalPingMonitorServiceQueryForm.setSecurityRequirements(List.of(ServiceSecurityType.CERTIFICATE));
		externalPingMonitorServiceQueryForm.setPingProviders(false);

		return externalPingMonitorServiceQueryForm;
	}
}
