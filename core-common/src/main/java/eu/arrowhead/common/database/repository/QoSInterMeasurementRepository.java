package eu.arrowhead.common.database.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.QoSInterMeasurement;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;

@Repository
public interface QoSInterMeasurementRepository extends RefreshableRepository<QoSInterMeasurement,Long> {

	//=================================================================================================
	// methods
	//-------------------------------------------------------------------------------------------------
	public Optional<QoSInterMeasurement> findByAddressAndMeasurementType(final String address, final QoSMeasurementType type);

	//-------------------------------------------------------------------------------------------------
	public List<QoSInterMeasurement> findByCloudAndMeasurementType(final Cloud cloud, final QoSMeasurementType type);

	//-------------------------------------------------------------------------------------------------
	public Optional<QoSInterMeasurement> findByCloudAndAddressAndMeasurementType(final Cloud cloud,  final String address, final QoSMeasurementType type);
}
