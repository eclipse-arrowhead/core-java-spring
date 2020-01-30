package eu.arrowhead.common.database.repository;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import eu.arrowhead.common.database.entity.QoSIntraPingMeasurementLog;

@Repository
public interface QoSIntraPingMeasurementLogRepository extends RefreshableRepository<QoSIntraPingMeasurementLog, Long> {

	//=================================================================================================
	// methods
	Optional<QoSIntraPingMeasurementLog> findByMeasuredAt(final ZonedDateTime aroundNow);
}
