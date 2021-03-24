package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor;

import eu.arrowhead.core.plantdescriptionengine.alarms.AlarmManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.description.ServiceDescription;
import se.arkalix.net.http.HttpMethod;
import se.arkalix.net.http.client.HttpClient;
import se.arkalix.net.http.client.HttpClientRequest;
import se.arkalix.query.ServiceQuery;

import java.util.Objects;
import java.util.TimerTask;

public class PingTask extends TimerTask {

    private static final Logger logger = LoggerFactory.getLogger(PingTask.class);

    private final ServiceQuery serviceQuery;
    private final HttpClient httpClient;
    private final AlarmManager alarmManager;

    public PingTask(ServiceQuery serviceQuery, HttpClient httpClient, AlarmManager alarmManager) {

        Objects.requireNonNull(serviceQuery, "Expected service query");
        Objects.requireNonNull(httpClient, "Expected HTTP client");
        Objects.requireNonNull(alarmManager, "Expected alarm manager");

        this.serviceQuery = serviceQuery;
        this.httpClient = httpClient;
        this.alarmManager = alarmManager;
    }

    @Override
    public void run() {
        ping();
    }

    /**
     * Check if each monitorable service is active.
     */
    private void ping() {
        serviceQuery.resolveAll()
            .ifSuccess(services -> {
                for (var service : services) {
                    ping(service);
                }
            })
            .onFailure(e -> logger.error("Failed to ping monitorable systems.", e));
    }

    private void ping(ServiceDescription service) {
        final var address = service.provider().socketAddress();
        final String providerName = service.provider().name();
        httpClient
            .send(address,
                new HttpClientRequest()
                    .method(HttpMethod.GET)
                    .uri(service.uri() + "/ping")
                    .header("accept",
                        "application/json"))
            .ifSuccess(result -> {
                logger.info("Successfully pinged system '" + providerName + "'.");
                alarmManager.clearSystemInactive(providerName);
            })
            .onFailure(e -> {
                logger.warn("Failed to ping system '" + providerName + "'.", e);
                alarmManager.raiseSystemInactive(providerName);
            });
    }

}
