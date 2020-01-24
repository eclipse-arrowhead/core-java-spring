package eu.arrowhead.core.qos.quartz.task;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icmp4j.IcmpPingRequest;
import org.icmp4j.IcmpPingResponse;
import org.icmp4j.IcmpPingUtil;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.repository.QoSIntraMeasurementRepository;
import eu.arrowhead.common.dto.internal.ServiceRegistryListResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.http.HttpService;

@Component
@DisallowConcurrentExecution
public class PingTask implements Job {

	//=================================================================================================
	// members
	private static final String NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE = " is null or blank.";
	private static final int TIMES_TO_REPEAT = 10;
	private static final int REST_BETWEEN_PINGS_MILLSEC = 1000;

	protected Logger logger = LogManager.getLogger(PingTask.class);
	
	@Autowired
	private QoSIntraMeasurementRepository qoSIntraMeasurementRepository;
	
	@Autowired
	private HttpService httpService;
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		logger.debug("STARTED: ping  task");

		Set<SystemResponseDTO> systems = getSystemsToMessure();
		for (final SystemResponseDTO systemResponseDTO : systems) {

			pingSystem(systemResponseDTO);

		}

		logger.debug("Finished: ping  task");
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private Set<SystemResponseDTO> getSystemsToMessure() {
		logger.debug("getSystemToMessure started...");

		final ServiceRegistryListResponseDTO serviceRegistryListResponseDTO = queryServiceRegistryAll();
		
		final Set<SystemResponseDTO> systemList = serviceRegistryListResponseDTO.getData().stream().map(ServiceRegistryResponseDTO::getProvider).collect(Collectors.toSet());
		
		return systemList;
	}

	//-------------------------------------------------------------------------------------------------
	private void pingSystem(final SystemResponseDTO system ) {
		logger.debug("pingSystem started...");
		
		if (system == null || Utilities.isEmpty(system.getAddress())) {
			throw new InvalidParameterException("System.address" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
		}

		final String address = system.getAddress();
		try {
			final IcmpPingRequest request = IcmpPingUtil.createIcmpPingRequest ();
			request.setHost (address);

			for (int count = 0; count < TIMES_TO_REPEAT; count ++) {
				final IcmpPingResponse response = IcmpPingUtil.executePingRequest (request);
				final String formattedResponse = IcmpPingUtil.formatResponse (response);
				logger.info(formattedResponse);

				Thread.sleep (REST_BETWEEN_PINGS_MILLSEC);
			}
		} catch ( final InterruptedException | IllegalArgumentException ex) {
			logger.debug("" + ex.getMessage());
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private ServiceRegistryListResponseDTO queryServiceRegistryAll() {
		logger.debug("queryServiceRegistryAll started...");

		try {
			final UriComponents queryBySystemDTOUri = getQueryAllUri();
			final ResponseEntity<ServiceRegistryListResponseDTO> response = httpService.sendRequest(queryBySystemDTOUri, HttpMethod.GET, ServiceRegistryListResponseDTO.class);

			return response.getBody();

		} catch (final ClassCastException ex) {
			throw new ArrowheadException("QoS Mointor can't find Service Registry Query All  URI.");
		}

	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getQueryAllUri() {
		logger.debug("getQueryUri started...");

		if (arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)) {
			try {
				return (UriComponents) arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("QoS Mointor can't find Service Registry Query All  URI.");
			}
		}

		throw new ArrowheadException("QoS Mointor can't find Service Registry Query All URI.");
	}
}