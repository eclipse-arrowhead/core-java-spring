package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitorable;

import eu.arrowhead.core.plantdescriptionengine.providedservices.DtoReadExceptionCatcher;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitorable.dto.InventoryIdBuilder;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitorable.dto.PingBuilder;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitorable.dto.SystemDataBuilder;
import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.dto.DtoReadException;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpService;
import se.arkalix.security.access.AccessPolicy;
import se.arkalix.util.concurrent.Future;


/**
 * This service enables monitoring Plant Description Engine core system.
 */
public class PdeMonitorableService {

    private static final String SERVICE_NAME = "monitorable";
    private static final String BASE_PATH = "/pde/monitorable";
    private static final String INVENTORY_ID_PATH = "/inventoryid";
    private static final String PING_PATH = "/ping";
    private static final String SYSTEM_DATA_PATH = "/systemdata";

    final boolean secure;

    public PdeMonitorableService(final boolean secure) {
        this.secure = secure;
    }

    /**
     * @return An HTTP Service enabling monitoring of the Plant Description
     * Engine.
     */
    public HttpService getService() {
        final HttpService service = new HttpService()
            .name(SERVICE_NAME)
            .encodings(EncodingDescriptor.JSON)
            .basePath(BASE_PATH)
            .get(INVENTORY_ID_PATH, (request, response) -> {
                response
                    .status(HttpStatus.OK)
                    .body(new InventoryIdBuilder().build());
                return Future.done();
            })
            .get(SYSTEM_DATA_PATH, (request, response) -> {
                response
                    .status(HttpStatus.OK)
                    .body(new SystemDataBuilder().build());
                return Future.done();
            })
            .get(PING_PATH, (request, response) -> {
                response
                    .status(HttpStatus.OK)
                    .body(new PingBuilder()
                        .ping(true)
                        .build());
                return Future.done();
            })
            .catcher(DtoReadException.class, new DtoReadExceptionCatcher());

        if (secure) {
            service.accessPolicy(AccessPolicy.cloud());
        } else {
            service.accessPolicy(AccessPolicy.unrestricted());
        }

        return service;
    }

}
