package eu.arrowhead.common.database.repository;

import java.util.List;

import eu.arrowhead.common.database.entity.AbstractPolicy;
import eu.arrowhead.common.database.entity.Sensor;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyRepository<S extends AbstractPolicy> extends RefreshableRepository<S, Long> {

    List<S> findBySensor(final Sensor sensor);
}
