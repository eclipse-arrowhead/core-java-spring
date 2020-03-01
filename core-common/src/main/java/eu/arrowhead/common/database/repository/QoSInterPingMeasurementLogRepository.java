package eu.arrowhead.common.database.repository;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.QoSInterPingMeasurementLog;

@Repository
public interface QoSInterPingMeasurementLogRepository extends RefreshableRepository<QoSInterPingMeasurementLog,Long> {

}
