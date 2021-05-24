package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitorable;

import eu.arrowhead.core.plantdescriptionengine.ApiConstants;
import eu.arrowhead.core.plantdescriptionengine.providedservices.CodecExceptionCatcher;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitorable.routehandlers.GetInventoryId;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitorable.routehandlers.GetPing;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitorable.routehandlers.GetSystemData;
import se.arkalix.codec.CodecException;
import se.arkalix.codec.CodecType;
import se.arkalix.net.http.service.HttpService;
import se.arkalix.security.access.AccessPolicy;

/**
 * This service enables monitoring Plant Description Engine core system.
 */
public class PdeMonitorableService {

    final boolean secure;

    /**
     * @param secure Specifies whether or not to run the service in secure
     *               mode.
     */
    public PdeMonitorableService(final boolean secure) {
        this.secure = secure;
    }

    /**
     * @return An HTTP Service enabling monitoring of the Plant Description
     * Engine.
     */
    public HttpService getService() {
        return new HttpService()
            .name(ApiConstants.MONITORABLE_SERVICE_NAME)
            .codecs(CodecType.JSON)
            .basePath(ApiConstants.MONITORABLE_BASE_PATH)
            .get(ApiConstants.MONITORABLE_ID_PATH, new GetInventoryId())
            .get(ApiConstants.MONITORABLE_SYSTEM_DATA_PATH, new GetSystemData())
            .get(ApiConstants.MONITORABLE_PING_PATH, new GetPing())
            .catcher(CodecException.class, new CodecExceptionCatcher())
            .accessPolicy(secure ? AccessPolicy.cloud() : AccessPolicy.unrestricted());
    }

}
