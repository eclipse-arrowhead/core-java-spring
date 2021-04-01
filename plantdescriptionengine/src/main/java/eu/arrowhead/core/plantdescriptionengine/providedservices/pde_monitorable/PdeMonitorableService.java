package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitorable;

import java.util.Objects;
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

    private static final String SERVICE_NAME = "monitorable";
    private static final String BASE_PATH = "/pde/monitorable";
    private static final String INVENTORY_ID_PATH = "/inventoryid";
    private static final String PING_PATH = "/ping";
    private static final String SYSTEM_DATA_PATH = "/systemdata";

    private final String systemName;
    final boolean secure;

    /**
     *
     * @param systemName Name of the system running the service.
     * @param secure     Specifies whether or not to run the service in secure
     *                   mode.
     */
    public PdeMonitorableService(final String systemName, final boolean secure) {

        Objects.requireNonNull(systemName, "Expected system name");

        this.secure = secure;
        this.systemName = systemName;
    }

    /**
     * @return An HTTP Service enabling monitoring of the Plant Description
     * Engine.
     */
    public HttpService getService() {
        return new HttpService()
            .name(SERVICE_NAME)
            .codecs(CodecType.JSON)
            .basePath(BASE_PATH)
            .get(INVENTORY_ID_PATH, new GetInventoryId())
            .get(SYSTEM_DATA_PATH, new GetSystemData(systemName))
            .get(PING_PATH, new GetPing())
            .catcher(CodecException.class, new CodecExceptionCatcher())
            .accessPolicy(secure ? AccessPolicy.cloud() : AccessPolicy.unrestricted());
    }

}
