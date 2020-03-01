package eu.arrowhead.common.database.repository;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.QoSInterMeasurement;

@Repository
public interface QoSInterMeasurementReository extends RefreshableRepository<QoSInterMeasurement,Long> {

}
