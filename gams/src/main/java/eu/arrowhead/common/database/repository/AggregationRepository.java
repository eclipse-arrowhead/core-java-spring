package eu.arrowhead.common.database.repository;

import java.util.List;

import eu.arrowhead.common.database.entity.Aggregation;
import eu.arrowhead.common.database.entity.Sensor;

public interface AggregationRepository extends RefreshableRepository<Aggregation, Long> {
    List<Aggregation> findBySensor(final Sensor sensor);
}
