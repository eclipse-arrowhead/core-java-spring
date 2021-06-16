package eu.arrowhead.core.plantdescriptionengine.pdtracker;

import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntry;
import se.arkalix.util.concurrent.Future;

public interface PlantDescriptionUpdateListener {
    Future<?> onPlantDescriptionAdded(PlantDescriptionEntry entry);

    Future<?> onPlantDescriptionUpdated(PlantDescriptionEntry newState, PlantDescriptionEntry oldState);

    Future<?> onPlantDescriptionRemoved(PlantDescriptionEntry entry);
}