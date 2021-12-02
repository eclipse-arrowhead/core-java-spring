package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt;

import eu.arrowhead.core.plantdescriptionengine.ApiConstants;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.providedservices.CodecExceptionCatcher;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.routehandlers.AddPlantDescription;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.routehandlers.DeletePlantDescription;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.routehandlers.GetAllPlantDescriptions;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.routehandlers.GetPlantDescription;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.routehandlers.ReplacePlantDescription;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.routehandlers.UpdatePlantDescription;
import se.arkalix.codec.CodecException;
import se.arkalix.codec.CodecType;
import se.arkalix.net.http.service.HttpService;
import se.arkalix.security.access.AccessPolicy;

import java.util.Objects;

/**
 * This service enables management of Plant Descriptions in the Plant
 * Description Engine (PDE) core system.
 */
public class PdeManagementService {

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
        return new HttpService()
            .name(ApiConstants.MGMT_SERVICE_NAME)
            .codecs(CodecType.JSON)
            .basePath(ApiConstants.MGMT_BASE_PATH)
            .get(ApiConstants.MGMT_PD_PATH, new GetPlantDescription(pdTracker))
            .get(ApiConstants.MGMT_PDS_PATH, new GetAllPlantDescriptions(pdTracker))
            .post(ApiConstants.MGMT_PDS_PATH, new AddPlantDescription(pdTracker))
            .delete(ApiConstants.MGMT_PD_PATH, new DeletePlantDescription(pdTracker))
            .put(ApiConstants.MGMT_PD_PATH, new ReplacePlantDescription(pdTracker))
            .patch(ApiConstants.MGMT_PD_PATH, new UpdatePlantDescription(pdTracker))
            .catcher(CodecException.class, new CodecExceptionCatcher())
            .accessPolicy(secure ? AccessPolicy.cloud() : AccessPolicy.unrestricted());
    }

}