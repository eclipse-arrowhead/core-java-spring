package eu.arrowhead.common.database.repository;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.QoSReservation;

@Repository
public interface QoSReservationRepository extends RefreshableRepository<QoSReservation,Long> {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public List<QoSReservation> findAllByReservedToLessThanEqual(final ZonedDateTime threshold);
	public List<QoSReservation> findAllByConsumerSystemNameNotOrConsumerAddressNotOrConsumerPortNot(final String consumerSystemName, final String consumerAddress, final int consumerPort);
}