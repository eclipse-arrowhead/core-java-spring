package eu.arrowhead.core.plantdescriptionengine;

public class PropertyNames {

    public final static String DB_CONNECTION_URL = "spring.datasource.url";
    public final static String DB_USERNAME = "spring.datasource.username";
    public final static String DB_PASSWORD = "spring.datasource.password";
    public final static String DB_DRIVER_CLASS_NAME = "spring.datasource.driver-class-name";
    public final static String FILENAME = "application.properties";
    public static final String SERVER_HOSTNAME = "server.hostname";
    public static final String SERVER_PORT = "server.port";
    public static final String PD_DIRECTORY = "plant_descriptions";
    public static final String PD_MAX_SIZE = "plant_description_max_size";
    public final static String ORCHESTRATION_RULES = "orchestration_rules";
    public static final String SERVICE_REGISTRY_ADDRESS = "sr_address";
    public static final String SERVICE_REGISTRY_PORT = "sr_port";
    public static final String SYSTEM_POLL_INTERVAL = "system_poll_interval";
    public static final String PING_INTERVAL = "ping_interval";
    public static final String FETCH_INTERVAL = "fetch_interval";
    public final static String SSL_ENABLED = "server.ssl.enabled";
    public final static String KEY_STORE = "server.ssl.key-store";
    public final static String KEY_STORE_PASSWORD = "server.ssl.key-store-password";
    public final static String KEY_PASSWORD = "server.ssl.key-password";
    public final static String TRUST_STORE = "server.ssl.trust-store";
    public final static String TRUST_STORE_PASSWORD = "server.ssl.trust-store-password";

    private PropertyNames() {
    }


}
