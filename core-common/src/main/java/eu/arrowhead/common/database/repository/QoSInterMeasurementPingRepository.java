package eu.arrowhead.common.database.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.QoSInterMeasurement;
import eu.arrowhead.common.database.entity.QoSInterPingMeasurement;

@Repository
public interface QoSInterMeasurementPingRepository extends RefreshableRepository<QoSInterPingMeasurement,Long>  {

	//=================================================================================================
	public Optional<QoSInterPingMeasurement> findByMeasurement(final QoSInterMeasurement measurement);
}
