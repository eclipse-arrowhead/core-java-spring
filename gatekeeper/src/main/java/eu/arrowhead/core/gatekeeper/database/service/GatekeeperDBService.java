package eu.arrowhead.core.gatekeeper.database.service;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.CloudGatekeeper;
import eu.arrowhead.common.database.repository.CloudGatekeeperRepository;
import eu.arrowhead.common.database.repository.CloudRepository;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;

@Service
public class GatekeeperDBService {

	//=================================================================================================
	// members
	
	@Autowired
	private CloudGatekeeperRepository cloudGatekeeperRepository;
	
	private final Logger logger = LogManager.getLogger(GatekeeperDBService.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public CloudGatekeeper getGatekeeperByCloud(final Cloud cloud) {
		logger.debug("getGatekeeperByCloud started...");
		
		try {
			final Optional<CloudGatekeeper> gatekeeperOpt = cloudGatekeeperRepository.findByCloud(cloud);
			if (gatekeeperOpt.isEmpty()) {
				throw new InvalidParameterException("Gatekeeper with cloud: " + cloud + " not exists.");
			} else {
				return gatekeeperOpt.get();
			}
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public CloudGatekeeper registerGatekeeper(final Cloud cloud, final String address, final int port, final String serviceUri, final String authenticationInfo) {
		logger.debug("registerGatekeeper started...");
		
		Assert.isTrue(cloud != null, "Cloud is null.");
		Assert.isTrue(!Utilities.isEmpty(address), "Address is null or empty.");
		Assert.isTrue(!Utilities.isEmpty(serviceUri), "ServiceUri is null or empty.");
		if (port < CommonConstants.SYSTEM_PORT_RANGE_MIN || port > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
			throw new InvalidParameterException("Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".");
		}
		
		checkUniqueConstraintOfCloudGatekeeperTable(cloud, address, port, serviceUri);
		
		final CloudGatekeeper gatekeeper = new CloudGatekeeper(cloud, address, port, serviceUri, authenticationInfo);
		return cloudGatekeeperRepository.saveAndFlush(gatekeeper);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public CloudGatekeeper updateGatekeeper(final CloudGatekeeper gatekeeper, final String address, final int port, final String serviceUri, final String authenticationInfo) {
		logger.debug("registerGatekeeper started...");
		
		Assert.isTrue(!Utilities.isEmpty(address), "Address is null or empty.");
		Assert.isTrue(!Utilities.isEmpty(serviceUri), "ServiceUri is null or empty.");
		if (port < CommonConstants.SYSTEM_PORT_RANGE_MIN || port > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
			throw new InvalidParameterException("Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".");
		}
		
		checkUniqueConstraintOfCloudGatekeeperTable(null, address, port, serviceUri);
		
		gatekeeper.setAddress(address);
		gatekeeper.setPort(port);
		gatekeeper.setServiceUri(serviceUri);
		gatekeeper.setAuthenticationInfo(authenticationInfo);
		
		return cloudGatekeeperRepository.saveAndFlush(gatekeeper);
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void checkUniqueConstraintOfCloudGatekeeperTable(final Cloud cloud, final String address, final int port, final String serviceUri) {
		logger.debug("checkUniqueConstraintOfCloudGatekeeperTable started...");
		
		try {
			
			if (cloud != null) {
				final Optional<CloudGatekeeper> gatekeeperOpt1 = cloudGatekeeperRepository.findByCloud(cloud);
				if (gatekeeperOpt1.isPresent()) {
					throw new InvalidParameterException("Gatekeeper with cloud: " + cloud + " already exists.");
				} 
				
			} 			
			
			final Optional<CloudGatekeeper> gatekeeperOpt2 = cloudGatekeeperRepository.findByAddressAndPortAndServiceUri(address, port, serviceUri);
			if (gatekeeperOpt2.isPresent()) {
				throw new InvalidParameterException("Gatekeeper with address: " + address + ", port: " + port + ", serviceUri: " + serviceUri + " already exists.");
			}
			
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
}
