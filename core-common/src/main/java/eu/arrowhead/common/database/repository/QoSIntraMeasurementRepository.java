package eu.arrowhead.common.database.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.QoSIntraMeasurement;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;

@Repository
public interface QoSIntraMeasurementRepository extends RefreshableRepository<QoSIntraMeasurement,Long> {

	//=================================================================================================
	// methods
	public Optional<QoSIntraMeasurement> findBySystemAndMeasurementType(final System systemToCheck,final QoSMeasurementType type);
}