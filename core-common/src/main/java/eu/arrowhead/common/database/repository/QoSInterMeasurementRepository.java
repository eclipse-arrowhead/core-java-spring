package eu.arrowhead.common.database.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.QoSInterMeasurement;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;

@Repository
public interface QoSInterMeasurementRepository extends RefreshableRepository<QoSInterMeasurement,Long> {

	//=================================================================================================
	// methods
	public Optional<QoSInterMeasurement> findByAddressAndMeasurementType(final String address, final QoSMeasurementType type);
}
