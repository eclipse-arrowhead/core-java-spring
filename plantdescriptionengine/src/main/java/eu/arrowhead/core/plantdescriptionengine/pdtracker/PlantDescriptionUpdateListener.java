package eu.arrowhead.core.plantdescriptionengine.pdtracker;

import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntry;

public interface PlantDescriptionUpdateListener {
    void onPlantDescriptionAdded(PlantDescriptionEntry entry);

    void onPlantDescriptionUpdated(PlantDescriptionEntry entry);

    void onPlantDescriptionRemoved(PlantDescriptionEntry entry);
}