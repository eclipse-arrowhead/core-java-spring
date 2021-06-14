package eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry;

import eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.dto.SrSystem;

public interface SystemUpdateListener {
    void onSystemAdded(SrSystem system);

    void onSystemRemoved(SrSystem system);
}