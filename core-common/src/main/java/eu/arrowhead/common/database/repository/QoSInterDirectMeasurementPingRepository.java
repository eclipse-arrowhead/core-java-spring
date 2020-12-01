package eu.arrowhead.common.database.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.QoSInterDirectMeasurement;
import eu.arrowhead.common.database.entity.QoSInterDirectPingMeasurement;

@Repository
public interface QoSInterDirectMeasurementPingRepository extends RefreshableRepository<QoSInterDirectPingMeasurement,Long> {

	//=================================================================================================
	public Optional<QoSInterDirectPingMeasurement> findByMeasurement(final QoSInterDirectMeasurement measurement);
}