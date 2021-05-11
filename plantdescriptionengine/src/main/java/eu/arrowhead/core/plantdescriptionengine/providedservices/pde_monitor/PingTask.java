package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor;

import eu.arrowhead.core.plantdescriptionengine.alarms.AlarmManager;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PdeSystem;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntry;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.Port;
import eu.arrowhead.core.plantdescriptionengine.utils.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.ServiceRecord;
import se.arkalix.SystemRecord;
import se.arkalix.net.http.HttpMethod;
import se.arkalix.net.http.client.HttpClient;
import se.arkalix.net.http.client.HttpClientRequest;
import se.arkalix.query.ServiceQuery;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class PingTask extends TimerTask {

    private static final String MONITORABLE_SERVICE_NAME = "monitorable";

    private static final Logger logger = LoggerFactory.getLogger(PingTask.class);

    private static final String PING_PATH = "/ping";

    private final ServiceQuery serviceQuery;
    private final HttpClient httpClient;
    private final AlarmManager alarmManager;
    private final PlantDescriptionTracker pdTracker;

    public PingTask(
        final ServiceQuery serviceQuery,
        final HttpClient httpClient,
        final AlarmManager alarmManager,
        PlantDescriptionTracker pdTracker
    ) {

        Objects.requireNonNull(serviceQuery, "Expected service query");
        Objects.requireNonNull(httpClient, "Expected HTTP client");
        Objects.requireNonNull(alarmManager, "Expected alarm manager");
        Objects.requireNonNull(pdTracker, "Expected Plant Description tracker");

        this.serviceQuery = serviceQuery;
        this.httpClient = httpClient;
        this.alarmManager = alarmManager;
        this.pdTracker = pdTracker;
    }

    @Override
    public void run() {

        serviceQuery.resolveAll()
            .ifSuccess(services -> {
                updateAlarmsForMissingServices(services);
                services.forEach(this::ping);
            })
            .onFailure(e -> logger.error("Failed to ping monitorable systems.", e));
    }

    private void updateAlarmsForMissingServices(Set<ServiceRecord> detectedMonitorableServices) {

        // TODO: Maybe this should be done per expected monitorable service
        // (i.e. per port with service definition 'monitorable'), not per
        // system?
        List<PdeSystem> expectedMonitorableSystems = getMonitorableSystems();
        for (PdeSystem system : expectedMonitorableSystems) {

            boolean hasMonitorableService = detectedMonitorableServices.stream()
                .anyMatch(service -> isProvidedBy(service.provider(), system));

            if (hasMonitorableService) {
                alarmManager.clearSystemNotMonitorable(
                    system.systemId(),
                    system.systemName().orElse(null),
                    system.metadata()
                );
            } else {
                alarmManager.raiseSystemNotMonitorable(
                    system.systemId(),
                    system.systemName().orElse(null),
                    system.metadata()
                );
            }
        }
    }

    private boolean isProvidedBy(SystemRecord provider, PdeSystem system) {
        boolean metadataMatch = Metadata.isSubset(system.metadata(), provider.metadata());
        boolean nameMatch = system.systemName().isEmpty() || system.systemName().get().equals(provider.name());
        return metadataMatch && nameMatch;
    }

    private boolean isMonitorable(final PdeSystem system) {
        for (Port port : system.ports()) {
            boolean isConsumer = port.consumer().orElse(false);
            if (!isConsumer && port.serviceDefinition().equals(MONITORABLE_SERVICE_NAME)) {
                return true;
            }
        }
        return false;
    }

    private List<PdeSystem> getMonitorableSystems() {
        PlantDescriptionEntry activeEntry = pdTracker.activeEntry();
        if (activeEntry == null) {
            return Collections.emptyList();
        }

        return activeEntry.systems()
            .stream()
            .filter(this::isMonitorable)
            .collect(Collectors.toList());
    }

    private void ping(final ServiceRecord service) {
        final InetSocketAddress address = service.provider().socketAddress();
        final String providerName = service.provider().name();

        httpClient
            .send(address,
                new HttpClientRequest()
                    .method(HttpMethod.GET)
                    .uri(service.uri() + PING_PATH)
                    .header("accept",
                        "application/json"))
            .ifSuccess(result -> {
                logger.info("Successfully pinged system '" + providerName + "'.");
                alarmManager.clearNoPingResponse(providerName);
            })
            .onFailure(e -> {
                logger.warn("Failed to ping system '" + providerName + "'.", e);
                alarmManager.raiseNoPingResponse(providerName);
            });
    }

}
