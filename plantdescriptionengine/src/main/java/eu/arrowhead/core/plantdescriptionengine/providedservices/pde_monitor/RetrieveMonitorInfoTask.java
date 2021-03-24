package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor;

import eu.arrowhead.core.plantdescriptionengine.MonitorInfo;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.monitorable.dto.InventoryIdDto;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.monitorable.dto.SystemDataDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.description.ServiceDescription;
import se.arkalix.dto.DtoEncoding;
import se.arkalix.net.http.HttpMethod;
import se.arkalix.net.http.client.HttpClient;
import se.arkalix.net.http.client.HttpClientRequest;
import se.arkalix.query.ServiceQuery;

import java.util.Objects;
import java.util.TimerTask;

public class RetrieveMonitorInfoTask extends TimerTask {

    private static final Logger logger = LoggerFactory.getLogger(RetrieveMonitorInfoTask.class);

    private final ServiceQuery serviceQuery;
    private final HttpClient httpClient;
    private final MonitorInfo monitorInfo;

    public RetrieveMonitorInfoTask(ServiceQuery serviceQuery, HttpClient httpClient, MonitorInfo monitorInfo) {

        Objects.requireNonNull(serviceQuery, "Expected service query");
        Objects.requireNonNull(httpClient, "Expected HTTP client");
        Objects.requireNonNull(monitorInfo, "Expected monitor info");

        this.serviceQuery = serviceQuery;
        this.httpClient = httpClient;
        this.monitorInfo = monitorInfo;
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
                for (var service : services) {
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
    private void retrieveId(ServiceDescription service) {
        final var address = service.provider().socketAddress();

        // noinspection SpellCheckingInspection
        httpClient
            .send(address,
                new HttpClientRequest()
                    .method(HttpMethod.GET)
                    .uri(service.uri() + "/inventoryid")
                    .header("accept", "application/json"))
            .flatMap(result -> result.bodyAsClassIfSuccess(DtoEncoding.JSON, InventoryIdDto.class))
            .ifSuccess(inventoryId -> monitorInfo.putInventoryId(service, inventoryId.id()))
            .onFailure(e -> {
                String errorMessage = "Failed to retrieve inventory ID for system '" + service.provider().name()
                    + "', service '" + service.name() + "'.";
                logger.warn(errorMessage, e);
            });
    }

    /**
     * Retrieve system data of a monitorable service.
     *
     * @param service A monitorable service.
     */
    private void retrieveSystemData(ServiceDescription service) {
        final var address = service.provider().socketAddress();

        // noinspection SpellCheckingInspection
        httpClient
            .send(address,
                new HttpClientRequest()
                    .method(HttpMethod.GET)
                    .uri(service.uri() + "/systemdata")
                    .header("accept", "application/json"))
            .flatMap(result -> result.bodyAsClassIfSuccess(DtoEncoding.JSON, SystemDataDto.class))
            .ifSuccess(systemData -> monitorInfo.putSystemData(service, systemData.data()))
            .onFailure(e -> {
                String errorMessage = "Failed to retrieve system data for system '" + service.provider().name()
                    + "', service '" + service.name() + "'.";
                logger.error(errorMessage, e);
            });
    }

}
