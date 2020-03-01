package eu.arrowhead.common.database.repository;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.QoSInterPingMeasurementLogDetails;

@Repository
public interface QoSInterPingMeasurementLogDetailsRepository extends RefreshableRepository<QoSInterPingMeasurementLogDetails, Long> {

}
