package eu.arrowhead.core.plantdescriptionengine;

import eu.arrowhead.core.plantdescriptionengine.alarms.AlarmManager;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.OrchestratorClient;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.rulebackingstore.SqlRuleStore;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.SystemTracker;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.dto.SrSystem;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStoreException;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.SqlPdStore;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.PdeManagementService;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.PdeMonitorService;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitorable.PdeMonitorableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.ArServiceRecordCache;
import se.arkalix.ArSystem;
import se.arkalix.core.plugin.HttpJsonCloudPlugin;
import se.arkalix.core.plugin.or.OrchestrationOption;
import se.arkalix.core.plugin.or.OrchestrationPattern;
import se.arkalix.core.plugin.or.OrchestrationStrategy;
import se.arkalix.net.http.client.HttpClient;
import se.arkalix.security.identity.OwnedIdentity;
import se.arkalix.security.identity.TrustStore;
import se.arkalix.util.concurrent.Future;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

public final class PdeMain {

    private static final Logger logger = LoggerFactory.getLogger(PdeMain.class);

    /**
     * Helper method for retrieving required properties. If the specified
     * property is not present, an {@code IllegalArgumentException} is thrown.
     *
     * @param appProps A set of properties.
     * @param propName Name of the property to retrieve.
     * @return The property with the given name.
     */
    private static String getProp(final Properties appProps, final String propName) {
        final String result = appProps.getProperty(propName);
        if (result == null) {
            throw new IllegalArgumentException("Missing field '" + propName + "' in application properties.");
        }
        return result;
    }

    /**
     * @param appProps Configurations used for this instance of the Plant
     *                 Description Engine.
     * @return HTTP client useful for consuming Arrowhead services.
     */
    static HttpClient createHttpClient(final Properties appProps) {

        Objects.requireNonNull(appProps, "Expected application properties.");

        final boolean secureMode = Boolean.parseBoolean(getProp(appProps, PropertyNames.SSL_ENABLED));
        if (!secureMode) {
            return new HttpClient.Builder().insecure().build();
        }

        final OwnedIdentity identity = loadIdentity(
            getProp(appProps, PropertyNames.KEY_STORE),
            getProp(appProps, PropertyNames.KEY_PASSWORD).toCharArray(),
            getProp(appProps, PropertyNames.KEY_STORE_PASSWORD).toCharArray()
        );

        final TrustStore trustStore = loadTrustStore(
            getProp(appProps, PropertyNames.TRUST_STORE),
            getProp(appProps, PropertyNames.TRUST_STORE_PASSWORD).toCharArray()
        );

        return new HttpClient.Builder()
            .identity(identity)
            .trustStore(trustStore)
            .build();
    }

    /**
     * @param appProps               Configurations used for this instance of
     *                               the Plant Description Engine.
     * @param serviceRegistryAddress Address of the Service Registry.
     * @return An Arrowhead Framework System.
     */
    static ArSystem createArSystem(final Properties appProps, final InetSocketAddress serviceRegistryAddress) {

        Objects.requireNonNull(appProps, "Expected application properties.");
        Objects.requireNonNull(serviceRegistryAddress, "Expected service registry address.");

        final String pdeHostname = getProp(appProps, PropertyNames.SERVER_HOSTNAME);
        final int pdePort = Integer.parseInt(getProp(appProps, PropertyNames.SERVER_PORT));

        final OrchestrationStrategy strategy = new OrchestrationStrategy(
            new OrchestrationPattern().isIncludingService(true)
                .option(OrchestrationOption.PING_PROVIDERS, true)
                .option(OrchestrationOption.OVERRIDE_STORE, false));

        final ArSystem.Builder systemBuilder = new ArSystem.Builder()
            .serviceCache(ArServiceRecordCache.withEntryLifetimeLimit(Duration.ZERO))
            .localHostnamePort(pdeHostname, pdePort)
            .plugins(new HttpJsonCloudPlugin.Builder()
                .orchestrationStrategy(strategy)
                .serviceRegistrySocketAddress(serviceRegistryAddress)
                .build());

        final boolean secureMode = Boolean.parseBoolean(getProp(appProps, PropertyNames.SSL_ENABLED));

        if (secureMode) {
            final String trustStorePath = getProp(appProps, PropertyNames.TRUST_STORE);
            final char[] trustStorePassword = getProp(appProps, PropertyNames.TRUST_STORE_PASSWORD).toCharArray();
            final String keyStorePath = getProp(appProps, PropertyNames.KEY_STORE);
            final char[] keyPassword = getProp(appProps, PropertyNames.KEY_PASSWORD).toCharArray();
            final char[] keyStorePassword = getProp(appProps, PropertyNames.KEY_STORE_PASSWORD).toCharArray();
            systemBuilder.identity(loadIdentity(keyStorePath, keyPassword, keyStorePassword))
                .trustStore(loadTrustStore(trustStorePath, trustStorePassword));
        } else {
            systemBuilder.name(ApiConstants.PDE_SYSTEM_NAME).insecure();
        }

        return systemBuilder.build();

    }

    /**
     * Loads the keystore at the given path, using the given password.
     * <p>
     * If the keystore cannot be loaded, the application is terminated.
     *
     * @param path     Keystore path.
     * @param password Password  associated with the keystore.
     * @return The loaded keystore.
     */
    private static KeyStore loadKeyStore(String path, char[] password) {

        KeyStore keyStore = null;

        try (InputStream in = getResource(path)) {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(in, password);
        } catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException e) {
            terminate("Failed to load key store from path " + path, e);
        }

        return keyStore;
    }

    /**
     * Loads the resource at the given path.
     * {@code path} is first treated as a regular file path. If the resource
     * cannot be found at that location, an attempt is made to load it from
     * resources (i.e. within the jar file).
     *
     * @param path path to the resource.
     * @return An {@code InputStream} object representing the resource.
     */
    private static InputStream getResource(final String path) throws IOException {
        File file = new File(path);
        if (file.isFile()) {
            return new FileInputStream(file);
        } else {
            return PdeMain.class.getResourceAsStream("/" + path);
        }
    }

    /**
     * Loads the Arrowhead certificate chain and private key required to manage
     * an <i>owned</i> system or operator identity.
     * <p>
     * If the certificate cannot be read, the entire application is terminated.
     * The provided arguments {@code keyPassword} and {@code keyStorePassword}
     * are cleared for security reasons.
     *
     * @param keyStorePath     Keystore path.
     * @param keyPassword      Password or private key associated with the
     *                         keystore.
     * @param keyStorePassword Password of provided key store.
     * @return An object holding the Arrowhead certificate chain and private key
     * required to manage an owned system or operator identity.
     */
    private static OwnedIdentity loadIdentity(
        final String keyStorePath,
        final char[] keyPassword,
        final char[] keyStorePassword
    ) {
        KeyStore keyStore = loadKeyStore(keyStorePath, keyStorePassword);
        OwnedIdentity identity = null;

        try {
            identity = new OwnedIdentity.Loader()
                .keyStore(keyStore)
                .keyPassword(keyPassword)
                .keyStorePassword(keyStorePassword)
                .load();

        } catch (final GeneralSecurityException | IOException e) {
            terminate("Failed to load OwnedIdentity", e);
        }

        Arrays.fill(keyPassword, '\0');
        Arrays.fill(keyStorePassword, '\0');

        return identity;
    }

    /**
     * Loads certificates associated with a <i>trusted</i> Arrowhead systems.
     * <p>
     * If the certificate cannot be read, the entire application is terminated.
     * The provided argument {@code password} is cleared for security reasons.
     *
     * @param path     Truststore path.
     * @param password Password or private key associated with the
     *                 truststore.
     * @return Object holding certificates associated with trusted Arrowhead
     * systems, operators, clouds, companies and other authorities.
     */
    private static TrustStore loadTrustStore(final String path, final char[] password) {
        KeyStore keyStore = loadKeyStore(path, password);
        TrustStore trustStore = null;

        try {
            trustStore = TrustStore.from(keyStore);
        } catch (KeyStoreException e) {
            terminate("Failed to read trust store", e);
        }

        Arrays.fill(password, '\0');

        return trustStore;
    }

    static Properties loadAppProps() {
        final Properties appProps = new Properties();

        try (InputStream in = getResource(PropertyNames.FILENAME)) {
            appProps.load(in);
        } catch (final IOException e) {
            logger.error("Failed reading " + PropertyNames.FILENAME, e);
        }

        if (appProps.isEmpty()) {
            terminate("No valid application properties, exiting.");
        }

        return appProps;
    }

    static PlantDescriptionTracker loadPlantDescriptionTracker(Properties appProps) {
        Objects.requireNonNull(appProps, "Expected application properties.");

        final int maxPdBytes = Integer.parseInt(getProp(appProps, PropertyNames.PD_MAX_SIZE));
        final SqlPdStore pdStore = new SqlPdStore(maxPdBytes);
        PlantDescriptionTracker pdTracker = null;

        try {
            pdStore.init(
                getProp(appProps, PropertyNames.DB_DRIVER_CLASS_NAME),
                getProp(appProps, PropertyNames.DB_CONNECTION_URL),
                getProp(appProps, PropertyNames.DB_USERNAME),
                getProp(appProps, PropertyNames.DB_PASSWORD)
            );
            pdTracker = new PlantDescriptionTracker(pdStore);
        } catch (PdStoreException e) {
            terminate("Failed to create Plant Description tracker.", e);
        }
        return pdTracker;
    }

    private static InetSocketAddress getServiceRegistryAddress(final Properties appProps) {
        final String serviceRegistryIp = getProp(appProps, PropertyNames.SERVICE_REGISTRY_ADDRESS);
        final int serviceRegistryPort = Integer.parseInt(getProp(appProps, PropertyNames.SERVICE_REGISTRY_PORT));
        return new InetSocketAddress(serviceRegistryIp, serviceRegistryPort);
    }

    private static Future<InetSocketAddress> getOrchestratorAddress(final SystemTracker systemTracker) {
        return systemTracker.getSystemWithRetries(ApiConstants.ORCHESTRATOR_SYSTEM_NAME)
            .map(SrSystem::getAddress);
    }

    private static void terminate(final String message, final Throwable e) {
        logger.error(message, e);
        System.exit(1);
    }

    private static void terminate(final String message) {
        terminate(message, null);
    }

    /**
     * Main method of the Plant Description Engine.
     * <p>
     * Provides Plant Description management and monitoring services to the
     * Arrowhead system of systems.
     */
    public static void main(final String[] args) {

        final Properties appProps = loadAppProps();
        final HttpClient httpClient = createHttpClient(appProps);
        final InetSocketAddress serviceRegistryAddress = getServiceRegistryAddress(appProps);
        final int systemPollInterval = Integer.parseInt(getProp(appProps, PropertyNames.SYSTEM_POLL_INTERVAL));
        final SystemTracker systemTracker = new SystemTracker(httpClient, serviceRegistryAddress, systemPollInterval);
        final PlantDescriptionTracker pdTracker = loadPlantDescriptionTracker(appProps);

        final ArSystem arSystem = createArSystem(appProps, serviceRegistryAddress);
        final boolean secureMode = Boolean.parseBoolean(getProp(appProps, PropertyNames.SSL_ENABLED));

        logger.info("Contacting Service Registry...");
        systemTracker.start()
            .flatMap(systemTrackerResult -> getOrchestratorAddress(systemTracker))
            .flatMap(orchestratorAddress -> {
                SqlRuleStore ruleStore = new SqlRuleStore();
                ruleStore.init(
                    getProp(appProps, PropertyNames.DB_DRIVER_CLASS_NAME),
                    getProp(appProps, PropertyNames.DB_CONNECTION_URL),
                    getProp(appProps, PropertyNames.DB_USERNAME),
                    getProp(appProps, PropertyNames.DB_PASSWORD)
                );
                final OrchestratorClient orchestratorClient = new OrchestratorClient(
                    httpClient,
                    ruleStore,
                    pdTracker,
                    orchestratorAddress
                );
                logger.info("Initializing the Orchestrator client...");
                return orchestratorClient.initialize();
            })
            .flatMap(orchestratorInitializationResult -> {
                logger.info("Orchestrator client initialized.");
                final AlarmManager alarmManager = new AlarmManager();
                final SystemMismatchDetector mismatchDetector = new SystemMismatchDetector(
                    pdTracker,
                    systemTracker,
                    alarmManager
                );
                mismatchDetector.run();
                logger.info("Starting the PDE Monitor service...");
                final int pingInterval = Integer.parseInt(getProp(appProps, PropertyNames.PING_INTERVAL));
                final int fetchInterval = Integer.parseInt(getProp(appProps, PropertyNames.FETCH_INTERVAL));
                return new PdeMonitorService(
                    arSystem,
                    pdTracker,
                    httpClient,
                    alarmManager,
                    secureMode,
                    pingInterval,
                    fetchInterval
                ).provide();
            })
            .flatMap(monitorServiceResult -> {
                logger.info("The PDE Monitor service is ready.");
                logger.info("Starting the PDE Management service...");
                final PdeManagementService pdeManagementService = new PdeManagementService(
                    pdTracker,
                    secureMode
                );
                return arSystem.provide(pdeManagementService.getService());
            })
            .flatMap(mgmtServiceResult -> {
                logger.info("The PDE Management service is ready.");
                logger.info("Starting the PDE Monitorable service...");
                final PdeMonitorableService pdeMonitorableService = new PdeMonitorableService(secureMode);
                return arSystem.provide(pdeMonitorableService.getService());
            })
            .ifSuccess(consumer -> {
                logger.info("The PDE Monitorable service is ready.");
                logger.info("The Plant Description Engine is up and running.");
            })
            .onFailure(e -> terminate("Failed to launch Plant Description Engine.", e));
    }

}