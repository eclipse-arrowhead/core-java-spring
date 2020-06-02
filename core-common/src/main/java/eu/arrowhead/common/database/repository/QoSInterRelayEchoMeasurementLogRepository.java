package eu.arrowhead.common.database.repository;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.QoSInterRelayEchoMeasurementLog;

@Repository
public interface QoSInterRelayEchoMeasurementLogRepository extends RefreshableRepository<QoSInterRelayEchoMeasurementLog,Long> {

}
