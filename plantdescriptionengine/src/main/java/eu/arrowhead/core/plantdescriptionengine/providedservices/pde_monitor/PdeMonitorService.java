package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor;

import eu.arrowhead.core.plantdescriptionengine.MonitorInfo;
import eu.arrowhead.core.plantdescriptionengine.alarms.AlarmManager;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.providedservices.DtoReadExceptionCatcher;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.routehandlers.*;
import se.arkalix.ArServiceHandle;
import se.arkalix.ArSystem;
import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.descriptor.TransportDescriptor;
import se.arkalix.dto.DtoReadException;
import se.arkalix.net.http.client.HttpClient;
import se.arkalix.net.http.service.HttpService;
import se.arkalix.query.ServiceQuery;
import se.arkalix.security.access.AccessPolicy;
import se.arkalix.util.concurrent.Future;

import java.util.Objects;
import java.util.Timer;

/**
 * This service enables monitoring of a plant and related alarms raised by Plant
 * Description Engine core system.
 */
public class PdeMonitorService {

    private final static int fetchInfoInterval = 6000; // Milliseconds
    private final static int pingInterval = 10000; // Milliseconds

    private final ArSystem arSystem;
    private final MonitorInfo monitorInfo = new MonitorInfo();
    private final AlarmManager alarmManager;

    private final PlantDescriptionTracker pdTracker;
    private final boolean secure;

    private final PingTask pingTask;
    private final RetrieveMonitorInfoTask retrieveMonitorInfoTask;

    /**
     * Class constructor.
     *
     * @param pdTracker    An object that maps ID:s to Plant Description Entries.
     * @param arSystem     An Arrowhead Framework system used to provide this
     *                     service.
     * @param httpClient   Object for communicating with monitorable services.
     * @param alarmManager Object used for managing PDE alarms.
     * @param secure       Indicates whether the service should run in secure mode.
     */
    public PdeMonitorService(ArSystem arSystem, PlantDescriptionTracker pdTracker, HttpClient httpClient,
                             AlarmManager alarmManager, boolean secure) {
        Objects.requireNonNull(arSystem, "Expected AR System");
        Objects.requireNonNull(pdTracker, "Expected plant description tracker");
        Objects.requireNonNull(httpClient, "Expected HTTP client");
        Objects.requireNonNull(alarmManager, "Expected Alarm Manager");

        this.arSystem = arSystem;
        this.pdTracker = pdTracker;
        this.alarmManager = alarmManager;
        this.secure = secure;

        // this.monitorableClient = new MonitorablesClient(arSystem, httpClient, monitorInfo, alarmManager);

        ServiceQuery serviceQuery = arSystem.consume()
            .name("monitorable")
            .transports(TransportDescriptor.HTTP)
            .encodings(EncodingDescriptor.JSON);

        pingTask = new PingTask(serviceQuery, httpClient, alarmManager);
        retrieveMonitorInfoTask = new RetrieveMonitorInfoTask(serviceQuery, httpClient, monitorInfo);
    }

    /**
     * Registers this service with an Arrowhead system, eventually making it
     * accessible to remote Arrowhead systems.
     *
     * @return A HTTP Service used to monitor alarms raised by the Plant Description
     * Engine core system.
     */
    public Future<ArServiceHandle> provide() {
        final var service = new HttpService()
            .name("plant-description-monitor")
            .encodings(EncodingDescriptor.JSON)
            .basePath("/pde/monitor")
            .get("/pd", new GetAllPlantDescriptions(monitorInfo, pdTracker))
            .get("/pd/#id", new GetPlantDescription(monitorInfo, pdTracker))
            .get("/alarm/#id", new GetPdeAlarm(alarmManager))
            .get("/alarm", new GetAllPdeAlarms(alarmManager))
            .patch("/alarm/#id", new UpdatePdeAlarm(alarmManager))
            .catcher(DtoReadException.class, new DtoReadExceptionCatcher());

        if (secure) {
            service.accessPolicy(AccessPolicy.cloud());
        } else {
            service.accessPolicy(AccessPolicy.unrestricted());
        }

        final var timer = new Timer();

        // Periodically check if all monitorable services are active
        timer.schedule(pingTask, 0, pingInterval);

        // Periodically request data from all monitorable services
        timer.schedule(retrieveMonitorInfoTask, 0, fetchInfoInterval);

        return arSystem.provide(service);
    }

}
