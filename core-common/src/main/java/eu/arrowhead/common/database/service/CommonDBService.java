package eu.arrowhead.common.database.service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.repository.CloudRepository;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.DataNotFoundException;

@Service
public class CommonDBService {

	//=================================================================================================
	// members
	
	@Autowired
	private CloudRepository cloudRepository;

	private final Logger logger = LogManager.getLogger(CommonDBService.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public Cloud getOwnCloud(final boolean isSecure) {
		logger.debug("getOwnCloud started...");
		try {
			final List<Cloud> cloudList = cloudRepository.findByOwnCloudAndSecure(true, isSecure);
			if (cloudList.isEmpty()) {
				throw new DataNotFoundException("Could not find own cloud information in the database");
			} else if (cloudList.size() > 1) {
				throw new ArrowheadException("More than one cloud is marked as own in " + (isSecure ? "SECURE" : "INSECURE") + " mode.");
			}
			
			return cloudList.get(0);
		} catch (final ArrowheadException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}		
	}
}