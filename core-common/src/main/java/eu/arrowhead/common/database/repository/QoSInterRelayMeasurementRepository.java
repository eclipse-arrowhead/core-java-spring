package eu.arrowhead.common.database.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.QoSInterRelayMeasurement;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;

@Repository
public interface QoSInterRelayMeasurementRepository extends RefreshableRepository<QoSInterRelayMeasurement,Long> {

	//-------------------------------------------------------------------------------------------------
	public Optional<QoSInterRelayMeasurement> findByCloudAndRelayAndMeasurementType(final Cloud cloud, final Relay relay, final QoSMeasurementType type);
	public List<QoSInterRelayMeasurement> findByCloudAndMeasurementType(final Cloud cloud, final QoSMeasurementType type);
}
