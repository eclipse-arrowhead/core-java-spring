package eu.arrowhead.core.gams.service;

import eu.arrowhead.core.gams.database.entities.AbstractSensorData;
import eu.arrowhead.core.gams.dto.PhaseResult;

public interface AnalyzeWorker {

    <T> PhaseResult<T> analyze(final AbstractSensorData data);
}
