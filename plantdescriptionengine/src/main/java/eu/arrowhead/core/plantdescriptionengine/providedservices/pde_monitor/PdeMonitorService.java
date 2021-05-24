package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor;

import eu.arrowhead.core.plantdescriptionengine.ApiConstants;
import eu.arrowhead.core.plantdescriptionengine.MonitorInfoTracker;
import eu.arrowhead.core.plantdescriptionengine.alarms.AlarmManager;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.providedservices.CodecExceptionCatcher;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.routehandlers.GetAllPdeAlarms;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.routehandlers.GetAllPlantDescriptions;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.routehandlers.GetPdeAlarm;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.routehandlers.GetPlantDescription;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.routehandlers.UpdatePdeAlarm;
import se.arkalix.ArServiceHandle;
import se.arkalix.ArSystem;
import se.arkalix.codec.CodecException;
import se.arkalix.codec.CodecType;
import se.arkalix.net.ProtocolType;
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

    private final int pingInterval;
    private final int fetchInterval;

    private final ArSystem arSystem;
    private final MonitorInfoTracker monitorInfoTracker = new MonitorInfoTracker();
    private final AlarmManager alarmManager;

    private final PlantDescriptionTracker pdTracker;
    private final boolean secure;

    private final PingTask pingTask;
    private final RetrieveMonitorInfoTask retrieveMonitorInfoTask;

    /**
     * Class constructor.
     *
     * @param pdTracker     An object that maps ID:s to Plant Description
     *                      Entries.
     * @param arSystem      An Arrowhead Framework system used to provide this
     *                      service.
     * @param httpClient    Object for communicating with monitorable services.
     * @param alarmManager  Object used for managing PDE alarms.
     * @param secure        Indicates whether the service should run in secure
     *                      mode.
     * @param pingInterval  Time in milliseconds between each ping to
     *                      monitorable services.
     * @param fetchInterval Time in milliseconds between each request to fetch
     *                      data from monitorable services.
     */
    public PdeMonitorService(
        final ArSystem arSystem,
        final PlantDescriptionTracker pdTracker,
        final HttpClient httpClient,
        final AlarmManager alarmManager,
        final boolean secure,
        final int pingInterval,
        final int fetchInterval
    ) {
        Objects.requireNonNull(arSystem, "Expected AR System");
        Objects.requireNonNull(pdTracker, "Expected plant description tracker");
        Objects.requireNonNull(httpClient, "Expected HTTP client");
        Objects.requireNonNull(alarmManager, "Expected Alarm Manager");

        this.arSystem = arSystem;
        this.pdTracker = pdTracker;
        this.alarmManager = alarmManager;
        this.secure = secure;
        this.pingInterval = pingInterval;
        this.fetchInterval = fetchInterval;

        final ServiceQuery serviceQuery = arSystem.consume()
            .name(ApiConstants.MONITORABLE_SERVICE_NAME)
            .codecTypes(CodecType.JSON)
            .protocolTypes(ProtocolType.HTTP);

        pingTask = new PingTask(serviceQuery, httpClient, alarmManager, pdTracker);
        retrieveMonitorInfoTask = new RetrieveMonitorInfoTask(serviceQuery, httpClient, monitorInfoTracker);
    }

    /**
     * Registers this service with an Arrowhead system, eventually making it
     * accessible to remote Arrowhead systems.
     *
     * @return A HTTP Service used to monitor alarms raised by the Plant
     * Description Engine core system.
     */
    public Future<ArServiceHandle> provide() {
        final HttpService service = new HttpService()
            .name(ApiConstants.MONITOR_SERVICE_NAME)
            .codecs(CodecType.JSON)
            .basePath(ApiConstants.MONITOR_BASE_PATH)
            .get(ApiConstants.MONITOR_PDS_PATH, new GetAllPlantDescriptions(monitorInfoTracker, pdTracker))
            .get(ApiConstants.MONITOR_PD_PATH, new GetPlantDescription(monitorInfoTracker, pdTracker))
            .get(ApiConstants.MONITOR_ALARMS_PATH, new GetAllPdeAlarms(alarmManager))
            .get(ApiConstants.MONITOR_ALARM_PATH, new GetPdeAlarm(alarmManager))
            .patch(ApiConstants.MONITOR_ALARM_PATH, new UpdatePdeAlarm(alarmManager))
            .catcher(CodecException.class, new CodecExceptionCatcher())
            .accessPolicy(secure ? AccessPolicy.cloud() : AccessPolicy.unrestricted());

        final Timer timer = new Timer();

        // Periodically check if all monitorable services are active
        timer.schedule(pingTask, 0, pingInterval);

        // Periodically request data from all monitorable services
        timer.schedule(retrieveMonitorInfoTask, 0, fetchInterval);

        return arSystem.provide(service);
    }

}
