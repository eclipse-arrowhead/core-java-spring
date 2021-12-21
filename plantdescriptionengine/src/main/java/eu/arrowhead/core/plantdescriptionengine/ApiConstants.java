package eu.arrowhead.core.plantdescriptionengine;

public class ApiConstants {

    public final static String HEADER_ACCEPT = "accept";
    public final static String APPLICATION_JSON = "application/json";
    public final static String PDE_SYSTEM_NAME = "plantdescriptionengine";
    public final static String ORCHESTRATOR_SYSTEM_NAME = "orchestrator";
    public static final String NAME = "name";
    public static final String DEFAULT_SERVICE_INTERFACE = "HTTP-SECURE-JSON";
    public static final String SERVICE_REGISTRY_SYSTEMS_PATH = "/serviceregistry/pull-systems";
    public static final String ORCHESTRATOR_ECHO_PATH = "/orchestrator/echo";
    public static final String ORCHESTRATOR_STORE_PATH = "/orchestrator/store/flexible";
    public static final String MONITOR_SERVICE_NAME = "plant-description-monitor";
    public static final String MONITOR_BASE_PATH = "/pde/monitor";
    public static final String MONITOR_PDS_PATH = "/pd";
    public static final String MONITOR_PD_PATH = "/pd/#id";
    public static final String MONITOR_ALARMS_PATH = "/alarm";
    public static final String MONITOR_ALARM_PATH = "/alarm/#id";
    public static final String MGMT_SERVICE_NAME = "pde-mgmt";
    public static final String MGMT_BASE_PATH = "/pde/mgmt";
    public static final String MGMT_PDS_PATH = "/pd";
    public static final String MGMT_PD_PATH = "/pd/#id";
    public static final String MONITORABLE_SERVICE_NAME = "monitorable";
    public static final String MONITORABLE_BASE_PATH = "/pde/monitorable";
    public static final String MONITORABLE_ID_PATH = "/inventoryid";
    public static final String MONITORABLE_PING_PATH = "/ping";
    public static final String MONITORABLE_SYSTEM_DATA_PATH = "/systemdata";

    public static final int CORE_SYSTEM_MAX_RETRIES = 20;
    public static final int CORE_SYSTEM_RETRY_DELAY = 15000;

    private ApiConstants() {
    }
}
