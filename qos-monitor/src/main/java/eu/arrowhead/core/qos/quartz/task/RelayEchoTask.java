package eu.arrowhead.core.qos.quartz.task;

import java.security.PublicKey;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.database.entity.QoSInterRelayMeasurement;
import eu.arrowhead.common.dto.internal.CloudAccessListResponseDTO;
import eu.arrowhead.common.dto.internal.CloudAccessResponseDTO;
import eu.arrowhead.common.dto.internal.CloudWithRelaysAndPublicRelaysListResponseDTO;
import eu.arrowhead.common.dto.internal.CloudWithRelaysAndPublicRelaysResponseDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalRequestDTO;
import eu.arrowhead.common.dto.internal.RelayResponseDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.QoSMeasurementStatus;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.qos.database.service.QoSDBService;
import eu.arrowhead.core.qos.service.QoSMonitorDriver;

@Component
@DisallowConcurrentExecution
public class RelayEchoTask implements Job {
	
	//=================================================================================================
	// members
	
	@Autowired
	private QoSMonitorDriver qosMonitorDriver;
	
	@Autowired
	private QoSDBService qosDBService;
	
	@Autowired
	protected SSLProperties sslProperties;

	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Resource(name = "relayEchoScheduler")
	private Scheduler relayEchoScheduler;
	
	@Value(CoreCommonConstants.$QOS_ENABLED_RELAY_TASK_WD)
	private boolean relayTaskEnabled;
	
	private static final List<CoreSystemService> REQUIRED_CORE_SERVICES = List.of(CoreSystemService.GATEKEEPER_PULL_CLOUDS, CoreSystemService.GATEKEEPER_COLLECT_ACCESS_TYPES,
			  																	  CoreSystemService.GATEKEEPER_COLLECT_SYSTEM_ADDRESSES, CoreSystemService.GATEKEEPER_RELAY_TEST_SERVICE);
	
	private final Logger logger = LogManager.getLogger(RelayEchoTask.class);
	
	private final String CLOUD_HAS_NO_GATEKEEPER_RELAY_WARNING_MESSAGE = "The following cloud do not have GATEKEEPER relay: ";
	private final String CLOUD_HAS_NO_GATEWAY_OR_PUBLIC_RELAY_WARNING_MESSAGE = "The following cloud do not have GATEWAY or PUBLIC relay: ";

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		logger.debug("STARTED: Relay Echo task");
		
		if (!relayTaskEnabled) {
			logger.debug("FINISHED: Relay Echo task disabled");
			shutdown();
			return;
		}
		
		if (arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)) {
			logger.debug("FINISHED: Relay Echo task can not run if server is in standalon mode");
			shutdown();
			return;
		}
		
		if (!sslProperties.isSslEnabled()) {
			logger.debug("FINISHED: Relay Echo task can not run if server is not in secure mode");
			shutdown();
			return;
		}		
		
		if (!checkRequiredCoreSystemServiceUrisAvailable()) {
			logger.debug("FINISHED: Relay Echo task. Reqired Core System Sevice URIs aren't available");
			return;
		}
		
		try {
			final QoSRelayTestProposalRequestDTO testProposal = findCloudRelayPairToTest();
			if (testProposal.getTargetCloud() == null || testProposal.getRelay() == null) {
				logger.debug("FINISHED: Relay Echo task. Have no cloud-relay pair to run relay echo test");
				return;
			}
			
			qosMonitorDriver.requestGatekeeperInitRelayTest(testProposal);	
			
			logger.debug("FINISHED: Relay Echo task success");			
		} catch (final ArrowheadException ex) {
			logger.debug("FAILED: Relay Echo task: " + ex.getMessage());
		}
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void shutdown() {
		logger.debug("shutdown started...");
		try {
			relayEchoScheduler.shutdown();
			logger.debug("SHUTDOWN: Relay Echo task");
		} catch (final SchedulerException ex) {
			logger.error(ex.getMessage());
			logger.debug("Stacktrace:", ex);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean checkRequiredCoreSystemServiceUrisAvailable() {
		logger.debug("checkRequiredCoreSystemServiceUrisAvailable started...");
		for (final CoreSystemService coreSystemService : REQUIRED_CORE_SERVICES) {
			final String key = coreSystemService.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
			if (!arrowheadContext.containsKey(key) && !(arrowheadContext.get(key) instanceof UriComponents)) {
				return false;
			}
		}
		return true;
	}
	
	//-------------------------------------------------------------------------------------------------
	private QoSRelayTestProposalRequestDTO findCloudRelayPairToTest() {
		logger.debug("selectCloudRelayPairToTest started...");
		final QoSRelayTestProposalRequestDTO proposal = new QoSRelayTestProposalRequestDTO();
		
		if (!arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)) {
			throw new ArrowheadException("Public key is not available.");
		}		
		final PublicKey publicKey = (PublicKey) arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY);		
		proposal.setSenderQoSMonitorPublicKey(Base64.getEncoder().encodeToString(publicKey.getEncoded()));		
		
		final CloudWithRelaysAndPublicRelaysListResponseDTO allCloud = qosMonitorDriver.queryGatekeeperAllCloud();
		proposal.setRequesterCloud(getOwnCloud(allCloud.getData()));
		final List<CloudWithRelaysAndPublicRelaysResponseDTO> cloudsWithoutDirectAccess = filterOnCloudsWithoutDirectAccess(allCloud.getData());
		
		ZonedDateTime latestMeasurementTime = ZonedDateTime.now().plusHours(1);
		for (final CloudWithRelaysAndPublicRelaysResponseDTO cloud : cloudsWithoutDirectAccess) {
			if (cloud.getGatekeeperRelays() == null && cloud.getGatekeeperRelays().isEmpty()) {
				logger.info(CLOUD_HAS_NO_GATEKEEPER_RELAY_WARNING_MESSAGE + cloud.getName() + "." + cloud.getOperator());
			} else {
				if(cloud.getGatewayRelays() != null && !cloud.getGatewayRelays().isEmpty()) {
					for (final RelayResponseDTO relay : cloud.getGatewayRelays()) {
						final Optional<QoSInterRelayMeasurement> measurementOpt = qosDBService.getInterRelayMeasurement(cloud, relay, QoSMeasurementType.RELAY_ECHO);
						if (measurementOpt.isEmpty() || measurementOpt.get().getStatus() == QoSMeasurementStatus.NEW) {
							proposal.setTargetCloud(DTOConverter.convertCloudResponseDTOToCloudRequestDTO(cloud));
							proposal.setRelay(DTOConverter.convertRelayResponseDTOToRelayRequestDTO(relay));
							return proposal;
						} else if (measurementOpt.isPresent() && measurementOpt.get().getStatus() != QoSMeasurementStatus.PENDING) {
							final QoSInterRelayMeasurement echoMeasurement = measurementOpt.get();
							if (echoMeasurement.getLastMeasurementAt().isBefore(latestMeasurementTime)) {
								proposal.setTargetCloud(DTOConverter.convertCloudResponseDTOToCloudRequestDTO(cloud));
								proposal.setRelay(DTOConverter.convertRelayResponseDTOToRelayRequestDTO(relay));
								latestMeasurementTime = echoMeasurement.getLastMeasurementAt();
							}
						}
					}
				} else {
					if (cloud.getPublicRelays() == null || cloud.getPublicRelays().isEmpty()) {
						logger.info(CLOUD_HAS_NO_GATEWAY_OR_PUBLIC_RELAY_WARNING_MESSAGE + cloud.getName() + "." + cloud.getOperator());
					} else {
						for (final RelayResponseDTO relay : cloud.getPublicRelays()) {
							final Optional<QoSInterRelayMeasurement> measurementOpt = qosDBService.getInterRelayMeasurement(cloud, relay, QoSMeasurementType.RELAY_ECHO);
							if (measurementOpt.isEmpty() || measurementOpt.get().getStatus() == QoSMeasurementStatus.NEW) {
								proposal.setTargetCloud(DTOConverter.convertCloudResponseDTOToCloudRequestDTO(cloud));
								proposal.setRelay(DTOConverter.convertRelayResponseDTOToRelayRequestDTO(relay));
								return proposal;
							} else if (measurementOpt.isPresent() && measurementOpt.get().getStatus() != QoSMeasurementStatus.PENDING) {
								final QoSInterRelayMeasurement echoMeasurement = measurementOpt.get();
								if (echoMeasurement.getLastMeasurementAt().isBefore(latestMeasurementTime)) {
									proposal.setTargetCloud(DTOConverter.convertCloudResponseDTOToCloudRequestDTO(cloud));
									proposal.setRelay(DTOConverter.convertRelayResponseDTOToRelayRequestDTO(relay));
									latestMeasurementTime = echoMeasurement.getLastMeasurementAt();
								}
							}
						}
					}
				}
			}
		}
		
		return proposal;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<CloudWithRelaysAndPublicRelaysResponseDTO> filterOnCloudsWithoutDirectAccess(final List<CloudWithRelaysAndPublicRelaysResponseDTO> clouds) {
		logger.debug("filterOnCloudsWithoutDirectAccess started...");
		
		final List<CloudRequestDTO> cloudsToRequest = new ArrayList<>();
		for (final CloudWithRelaysAndPublicRelaysResponseDTO cloud : clouds) {
			cloudsToRequest.add(DTOConverter.convertCloudResponseDTOToCloudRequestDTO(cloud));
		}
		
		final CloudAccessListResponseDTO cloudsWithAccessTypes = qosMonitorDriver.queryGatekeeperCloudAccessTypes(cloudsToRequest);
		
		final List<CloudWithRelaysAndPublicRelaysResponseDTO> cloudsWithoutDirectAccess = new ArrayList<>();
		for (final CloudWithRelaysAndPublicRelaysResponseDTO cloud : clouds) {
			if (!cloud.getOwnCloud()) {
				for (final CloudAccessResponseDTO cloudAccess : cloudsWithAccessTypes.getData()) {
					if (!cloudAccess.isDirectAccess() && cloudAccess.getCloudOperator().equalsIgnoreCase(cloud.getOperator()) && cloudAccess.getCloudName().equalsIgnoreCase(cloud.getName())) {
						cloudsWithoutDirectAccess.add(cloud);
					}
				}
			}
		}
		
		return cloudsWithoutDirectAccess;
	}
	
	//-------------------------------------------------------------------------------------------------
	private CloudRequestDTO getOwnCloud(final List<CloudWithRelaysAndPublicRelaysResponseDTO> clouds) {
		logger.debug("getOwnCloud started...");
		
		for (final CloudWithRelaysAndPublicRelaysResponseDTO cloud : clouds) {
			if (cloud.getOwnCloud() && cloud.getSecure()) {
				return DTOConverter.convertCloudResponseDTOToCloudRequestDTO(cloud);
			}
		}
		
		throw new ArrowheadException("Secure own cloud was not found.");
	}
}
