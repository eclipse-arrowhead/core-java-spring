package eu.arrowhead.common.database.repository;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.QoSInterPingMeasurement;

@Repository
public interface QoSInterMeasurementPingRepository extends RefreshableRepository<QoSInterPingMeasurement,Long>  {

}
