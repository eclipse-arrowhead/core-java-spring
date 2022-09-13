package eu.arrowhead.core.gams.dto;

import org.apache.logging.log4j.MarkerManager;

public enum GamsPhase {
    MONITOR, ANALYZE, PLAN, EXECUTE, FAILURE;

    private final MarkerManager.Log4jMarker marker;

    private GamsPhase() {
        this.marker =  new MarkerManager.Log4jMarker(this.name());
    }

    public MarkerManager.Log4jMarker getMarker() {
        return marker;
    }
}
