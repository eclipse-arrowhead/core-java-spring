package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor;

import eu.arrowhead.core.plantdescriptionengine.ApiConstants;
import eu.arrowhead.core.plantdescriptionengine.MonitorInfoTracker;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.monitorable.dto.InventoryIdDto;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.monitorable.dto.SystemDataDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.ServiceRecord;
import se.arkalix.codec.json.JsonObject;
import se.arkalix.net.http.HttpMethod;
import se.arkalix.net.http.client.HttpClient;
import se.arkalix.net.http.client.HttpClientRequest;
import se.arkalix.query.ServiceQuery;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.TimerTask;

public class RetrieveMonitorInfoTask extends TimerTask {

    private static final Logger logger = LoggerFactory.getLogger(RetrieveMonitorInfoTask.class);

    private static final String SYSTEM_DATA_PATH = "/systemdata";
    private static final String INVENTORY_ID_PATH = "/inventoryid";

    private final ServiceQuery serviceQuery;
    private final HttpClient httpClient;
    private final MonitorInfoTracker monitorInfoTracker;

    public RetrieveMonitorInfoTask(
        final ServiceQuery serviceQuery,
        final HttpClient httpClient,
        final MonitorInfoTracker monitorInfoTracker
    ) {

        Objects.requireNonNull(serviceQuery, "Expected service query");
        Objects.requireNonNull(httpClient, "Expected HTTP client");
        Objects.requireNonNull(monitorInfoTracker, "Expected MonitorInfoTracker");

        this.serviceQuery = serviceQuery;
        this.httpClient = httpClient;
        this.monitorInfoTracker = monitorInfoTracker;
    }

    @Override
    public void run() {
        retrieveMonitorInfo();
    }

    /**
     * Retrieve new data from each monitorable service.
     */
    private void retrieveMonitorInfo() {
        serviceQuery.resolveAll()
            .ifSuccess(services -> {
                for (final ServiceRecord service : services) {
                    retrieveId(service);
                    retrieveSystemData(service);
                }
            })
            .onFailure(e -> logger.error("Failed to fetch monitor info from monitorable systems.", e));
    }

    /**
     * Retrieve the inventory ID of a monitorable service.
     *
     * @param service A monitorable service.
     */
    private void retrieveId(final ServiceRecord service) {
        final InetSocketAddress address = service.provider().socketAddress();

        httpClient
            .send(address,
                new HttpClientRequest()
                    .method(HttpMethod.GET)
                    .uri(service.uri() + INVENTORY_ID_PATH)
                    .header(ApiConstants.HEADER_ACCEPT, ApiConstants.APPLICATION_JSON))
            .flatMap(response -> response.bodyToIfSuccess(InventoryIdDto::decodeJson))
            .ifSuccess(inventoryId -> monitorInfoTracker.putInventoryId(service, inventoryId.id().orElse(null)))
            .onFailure(e -> {
                final String errorMessage = "Failed to retrieve inventory ID for system '" + service.provider().name()
                    + "', service '" + service.name() + "'.";
                logger.warn(errorMessage, e);
            });
    }

    /**
     * Retrieve system data of a monitorable service.
     *
     * @param service A monitorable service.
     */
    private void retrieveSystemData(final ServiceRecord service) {
        final InetSocketAddress address = service.provider().socketAddress();

        httpClient
            .send(address,
                new HttpClientRequest()
                    .method(HttpMethod.GET)
                    .uri(service.uri() + SYSTEM_DATA_PATH)
                    .header(ApiConstants.HEADER_ACCEPT, ApiConstants.APPLICATION_JSON))
            .flatMap(result -> result.bodyToIfSuccess(SystemDataDto::decodeJson))
            .ifSuccess(systemData -> {
                JsonObject json = systemData.data().orElse(null);
                monitorInfoTracker.putSystemData(service, json);
            })
            .onFailure(e -> {
                final String errorMessage = "Failed to retrieve system data for system '" + service.provider().name()
                    + "', service '" + service.name() + "'.";
                logger.error(errorMessage, e);
            });
    }

}
