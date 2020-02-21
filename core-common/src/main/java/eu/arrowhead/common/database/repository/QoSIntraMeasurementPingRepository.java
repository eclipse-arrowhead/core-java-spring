package eu.arrowhead.common.database.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.QoSIntraMeasurement;
import eu.arrowhead.common.database.entity.QoSIntraPingMeasurement;

@Repository
public interface QoSIntraMeasurementPingRepository extends RefreshableRepository<QoSIntraPingMeasurement,Long> {

	//=================================================================================================
	public Optional<QoSIntraPingMeasurement> findByMeasurement(final QoSIntraMeasurement measurement);
}