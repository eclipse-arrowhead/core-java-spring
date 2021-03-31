package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt;

import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.providedservices.DtoReadExceptionCatcher;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.routehandlers.AddPlantDescription;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.routehandlers.DeletePlantDescription;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.routehandlers.GetAllPlantDescriptions;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.routehandlers.GetPlantDescription;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.routehandlers.ReplacePlantDescription;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.routehandlers.UpdatePlantDescription;
import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.dto.DtoReadException;
import se.arkalix.net.http.service.HttpService;
import se.arkalix.security.access.AccessPolicy;

import java.util.Objects;

/**
 * This service enables management of Plant Descriptions in the Plant
 * Description Engine (PDE) core system.
 */
public class PdeManagementService {

    private static final String SERVICE_NAME = "pde-mgmt";
    private static final String BASE_PATH = "/pde/mgmt";
    private static final String GET_ALL_PLANT_DESCRIPTIONS_PATH = "/pd";
    private static final String GET_PLANT_DESCRIPTION_PATH = "/pd/#id";
    private static final String ADD_PLANT_DESCRIPTION_PATH = "/pd";
    private static final String DELETE_PLANT_DESCRIPTION_PATH = "/pd/#id";
    private static final String REPLACE_PLANT_DESCRIPTION_PATH = "/pd/#id";
    private static final String UPDATE_PLANT_DESCRIPTION_PATH = "/pd/#id";

    private final PlantDescriptionTracker pdTracker;
    private final boolean secure;

    /**
     * Class constructor.
     *
     * @param pdTracker An object that keeps track of Plant Description
     *                  Entries.
     * @param secure    Indicates whether the service should run in secure
     *                  mode.
     */
    public PdeManagementService(final PlantDescriptionTracker pdTracker, final boolean secure) {

        Objects.requireNonNull(pdTracker, "Expected AR System");
        Objects.requireNonNull(pdTracker, "Expected plant description map");

        this.pdTracker = pdTracker;
        this.secure = secure;
    }

    /**
     * @return An HTTP Service used to manage Plant Descriptions.
     */
    public HttpService getService() {
        final HttpService service = new HttpService()
            .name(SERVICE_NAME)
            .encodings(EncodingDescriptor.JSON)
            .basePath(BASE_PATH)
            .get(GET_PLANT_DESCRIPTION_PATH, new GetPlantDescription(pdTracker))
            .get(GET_ALL_PLANT_DESCRIPTIONS_PATH, new GetAllPlantDescriptions(pdTracker))
            .post(ADD_PLANT_DESCRIPTION_PATH, new AddPlantDescription(pdTracker))
            .delete(DELETE_PLANT_DESCRIPTION_PATH, new DeletePlantDescription(pdTracker))
            .put(REPLACE_PLANT_DESCRIPTION_PATH, new ReplacePlantDescription(pdTracker))
            .patch(UPDATE_PLANT_DESCRIPTION_PATH, new UpdatePlantDescription(pdTracker))
            .catcher(DtoReadException.class, new DtoReadExceptionCatcher());

        if (secure) {
            service.accessPolicy(AccessPolicy.cloud());
        } else {
            service.accessPolicy(AccessPolicy.unrestricted());
        }
        return service;
    }

}