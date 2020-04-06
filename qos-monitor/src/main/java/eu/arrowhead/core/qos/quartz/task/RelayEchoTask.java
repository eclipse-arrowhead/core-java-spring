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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.SSLProperties;
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
	
	@Value(CoreCommonConstants.$QOS_ENABLED_RELAY_TASK_WD)
	private boolean relayTaskEnabled;
	
	private final Logger logger = LogManager.getLogger(RelayEchoTask.class);
	
	private final String CLOUD_HAS_NO_RELAY_WARNING_MESSAGE = "The following cloud do not have a relay: ";

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		logger.debug("STARTED: relay echo task");
		
		if (arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)) {
			logger.debug("Finished: relay echo task can not run if server is in standalon mode");
			return;
		}
		
		if (!sslProperties.isSslEnabled()) {
			logger.debug("Finished: relay echo task can not run if server is not in secure mode");
			return;
		}
		
		if (!relayTaskEnabled) {
			logger.debug("Finished: relay echo task disabled");
			return;
		}
		
		final QoSRelayTestProposalRequestDTO testProposal = findCloudRelayPairToTest();
		if (testProposal.getTargetCloud() == null || testProposal.getRelay() == null) {
			logger.debug("Finished: have no cloud-relay pair to run relay echo test");
			return;
		}
		
		qosMonitorDriver.requestGatekeeperInitRelayTest(testProposal);	
		
		logger.debug("Finished: relay echo task success");
	}

	//=================================================================================================
	// assistant methods
	
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
			if(cloud.getGatekeeperRelays() != null && !cloud.getGatewayRelays().isEmpty()) {
				for (final RelayResponseDTO relay : cloud.getGatekeeperRelays()) {
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
			}else {
				if (cloud.getPublicRelays() == null || cloud.getPublicRelays().isEmpty()) {
					logger.info( CLOUD_HAS_NO_RELAY_WARNING_MESSAGE + cloud.getName() + "." + cloud.getOperator());
				}else {
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
