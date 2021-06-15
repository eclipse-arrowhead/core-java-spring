package eu.arrowhead.core.plantdescriptionengine.pdtracker;

import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntry;

public interface PlantDescriptionUpdateListener {
    void onPlantDescriptionAdded(PlantDescriptionEntry entry);

    void onPlantDescriptionUpdated(PlantDescriptionEntry newState, PlantDescriptionEntry oldState);

    void onPlantDescriptionRemoved(PlantDescriptionEntry entry);
}