package eu.arrowhead.core.qos.database.service;

import java.time.ZonedDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.QoSReservation;
import eu.arrowhead.common.database.repository.QoSReservationRepository;
import eu.arrowhead.common.exception.ArrowheadException;

@Service
public class QoSReservationDBService {

	//=================================================================================================
	// members
	
	protected final Logger logger = LogManager.getLogger(QoSReservationDBService.class);

	@Autowired
	private QoSReservationRepository qosReservationRepository;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public int releaseObsoleteReservations() {
		try {
			final ZonedDateTime now = ZonedDateTime.now();
			final List<QoSReservation> obsoletes = qosReservationRepository.findAllByReservedToLessThanEqual(now);
			qosReservationRepository.deleteAll(obsoletes);
			qosReservationRepository.flush();
			
			return obsoletes.size();
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<QoSReservation> getAllReservationsExceptMine(final String systemName, final String address, final int port) {
		Assert.isTrue(!Utilities.isEmpty(systemName), "'systemName' is null or empty.");
		Assert.isTrue(!Utilities.isEmpty(address), "'address' is null or empty.");
		
		final String validatedSystemName = systemName.trim().toLowerCase();
		final String validatedAddress = address.trim().toLowerCase();
		
		return qosReservationRepository.findAllByConsumerSystemNameNotOrConsumerAddressNotOrConsumerPortNot(validatedSystemName, validatedAddress, port);
	}
}