package eu.arrowhead.common.database.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.QoSInterRelayMeasurement;
import eu.arrowhead.common.database.entity.QoSInterRelayEchoMeasurement;

@Repository
public interface QoSInterRelayEchoMeasurementRepository extends RefreshableRepository<QoSInterRelayEchoMeasurement,Long>  {

	//=================================================================================================
	public Optional<QoSInterRelayEchoMeasurement> findByMeasurement(final QoSInterRelayMeasurement measurement);
}
