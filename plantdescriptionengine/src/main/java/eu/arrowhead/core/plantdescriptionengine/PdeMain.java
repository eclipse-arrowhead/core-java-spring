package eu.arrowhead.core.plantdescriptionengine;

import eu.arrowhead.core.plantdescriptionengine.alarms.AlarmManager;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.OrchestratorClient;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.rulebackingstore.FileRuleStore;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.SystemTracker;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.dto.SrSystem;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.FilePdStore;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.PdeManagementService;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.PdeMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.ArServiceCache;
import se.arkalix.ArSystem;
import se.arkalix.core.plugin.HttpJsonCloudPlugin;
import se.arkalix.core.plugin.or.OrchestrationOption;
import se.arkalix.core.plugin.or.OrchestrationPattern;
import se.arkalix.core.plugin.or.OrchestrationStrategy;
import se.arkalix.net.http.client.HttpClient;
import se.arkalix.security.identity.OwnedIdentity;
import se.arkalix.security.identity.TrustStore;

import javax.net.ssl.SSLException;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class PdeMain {

    private static final Logger logger = LoggerFactory.getLogger(PdeMain.class);

    /**
     * Helper class for retrieving required properties. If the specified property is
     * not present, an {@code IllegalArgumentException} is thrown.
     *
     * @param appProps A set of properties.
     * @param propName Name of the property to retrieve.
     * @return The property with the given name.
     */
    private static String getProp(Properties appProps, String propName) {
        String result = appProps.getProperty(propName);
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
    static HttpClient createHttpClient(Properties appProps) {
        final boolean secureMode = Boolean.parseBoolean(getProp(appProps, "server.ssl.enabled"));
        HttpClient client = null;
        try {
            if (!secureMode) {
                client = new HttpClient.Builder().insecure()
                    .build();
            } else {
                OwnedIdentity identity = loadIdentity(getProp(appProps, "server.ssl.pde.key-store"),
                    getProp(appProps, "server.ssl.pde.key-password").toCharArray(),
                    getProp(appProps, "server.ssl.pde.key-store-password").toCharArray());

                TrustStore trustStore = loadTrustStore(getProp(appProps, "server.ssl.pde.trust-store"),
                    getProp(appProps, "server.ssl.pde.trust-store-password").toCharArray());

                client = new HttpClient.Builder().identity(identity)
                    .trustStore(trustStore)
                    .build();
            }
        } catch (SSLException e) {
            logger.error("Failed to create PDE HTTP Client", e);
            System.exit(1);
        }
        return client;
    }

    /**
     * @param appProps               Configurations used for this instance of the
     *                               Plant Description Engine.
     * @param serviceRegistryAddress Address of the Service Registry.
     * @return An Arrowhead Framework System.
     */
    static ArSystem createArSystem(Properties appProps, InetSocketAddress serviceRegistryAddress) {

        final int pdePort = Integer.parseInt(getProp(appProps, "server.port"));

        final var strategy = new OrchestrationStrategy(
            new OrchestrationPattern().isIncludingService(true)
                .option(OrchestrationOption.METADATA_SEARCH, false)
                .option(OrchestrationOption.PING_PROVIDERS, true)
                .option(OrchestrationOption.OVERRIDE_STORE, false));

        final ArSystem.Builder systemBuilder = new ArSystem.Builder()
            .serviceCache(ArServiceCache.withEntryLifetimeLimit(Duration.ZERO))
            .localPort(pdePort)
            .plugins(new HttpJsonCloudPlugin.Builder().orchestrationStrategy(strategy)
                .serviceRegistrySocketAddress(serviceRegistryAddress)
                .build());

        final boolean secureMode = Boolean.parseBoolean(getProp(appProps, "server.ssl.enabled"));

        if (!secureMode) {
            systemBuilder.name("pde")
                .insecure();
        } else {
            final String trustStorePath = getProp(appProps, "server.ssl.pde.trust-store");
            final char[] trustStorePassword = getProp(appProps, "server.ssl.pde.trust-store-password").toCharArray();
            final String keyStorePath = getProp(appProps, "server.ssl.pde.key-store");
            final char[] keyPassword = getProp(appProps, "server.ssl.pde.key-password").toCharArray();
            final char[] keyStorePassword = getProp(appProps, "server.ssl.pde.key-store-password").toCharArray();
            systemBuilder.identity(loadIdentity(keyStorePath, keyPassword, keyStorePassword))
                .trustStore(loadTrustStore(trustStorePath, trustStorePassword));
        }

        return systemBuilder.build();

    }

    /**
     * Loads the Arrowhead certificate chain and private key required to manage an
     * <i>owned</i> system or operator identity.
     * <p>
     * The provided arguments {@code keyPassword} and {@code keyStorePassword} are
     * cleared for security reasons. If this function fails, the entire application
     * is terminated.
     *
     * @param keyStorePath     Sets path to file containing JVM-compatible key
     *                         store.
     * @param keyPassword      Password of private key associated with designated
     *                         certificate in key store.
     * @param keyStorePassword Password of provided key store.
     * @return An object holding the Arrowhead certificate chain and private key
     * required to manage an owned system or operator identity.
     */
    private static OwnedIdentity loadIdentity(String keyStorePath, char[] keyPassword, char[] keyStorePassword) {
        OwnedIdentity identity = null;
        try {
            identity = new OwnedIdentity.Loader().keyStorePath(keyStorePath)
                .keyPassword(keyPassword)
                .keyStorePassword(keyStorePassword)
                .load();
        } catch (GeneralSecurityException | IOException e) {
            logger.error("Failed to load OwnedIdentity", e);
            System.exit(1);
        }

        Arrays.fill(keyPassword, '\0');
        Arrays.fill(keyStorePassword, '\0');

        return identity;
    }

    /**
     * Loads certificates associated with a <i>trusted</i> Arrowhead systems.
     * <p>
     * The provided argument {@code password} is cleared for security reasons. If
     * this function fails, the entire application is terminated.
     *
     * @param path     Filesystem path to key store to load.
     * @param password Key store password.
     * @return Object holding certificates associated with trusted Arrowhead
     * systems, operators, clouds, companies and other authorities.
     */
    private static TrustStore loadTrustStore(String path, char[] password) {
        TrustStore trustStore = null;
        try {
            trustStore = TrustStore.read(path, password);
        } catch (GeneralSecurityException | IOException e) {
            logger.error("Failed to load OwnedIdentity", e);
            System.exit(1);
        }

        Arrays.fill(password, '\0');

        return trustStore;
    }

    /**
     * Main method of the Plant Description Engine.
     * <p>
     * Provides Plant Description management and monitoring services to the
     * Arrowhead system.
     */
    public static void main(final String[] args) {

        Properties appProps = new Properties();

        try {
            if (args.length == 1) {
                // If a command line argument is given, interpret it as the file
                // path to an application properties file:
                appProps.load(new FileInputStream(args[0]));
            } else {
                // Otherwise, load it from app resources:
                appProps.load(ClassLoader.getSystemResourceAsStream("application.properties"));
            }

        } catch (IOException e) {
            logger.error("Failed to read application.properties.", e);
            System.exit(74);
        }

        final String serviceRegistryIp = getProp(appProps, "service_registry.address");
        final int serviceRegistryPort = Integer.parseInt(getProp(appProps, "service_registry.port"));
        final var serviceRegistryAddress = new InetSocketAddress(serviceRegistryIp, serviceRegistryPort);
        final HttpClient httpClient = createHttpClient(appProps);

        final SystemTracker systemTracker = new SystemTracker(httpClient, serviceRegistryAddress);

        logger.info("Start polling Service Registry for systems...");
        systemTracker.start()
            .flatMap(result -> {

                final String ruleDirectory = getProp(appProps, "orchestration_rules");
                final String plantDescriptionsDirectory = getProp(appProps, "plant_descriptions");
                final var pdTracker = new PlantDescriptionTracker(new FilePdStore(plantDescriptionsDirectory));
                final SrSystem orchestrator = systemTracker.getSystem("orchestrator", null);
                final var orchestratorClient = new OrchestratorClient(httpClient, new FileRuleStore(ruleDirectory),
                    pdTracker, orchestrator.getAddress());

                final ArSystem arSystem = createArSystem(appProps, serviceRegistryAddress);
                final boolean secureMode = Boolean.parseBoolean(getProp(appProps, "server.ssl.enabled"));
                final AlarmManager alarmManager = new AlarmManager();

                logger.info("Initializing the Orchestrator client...");

                return orchestratorClient.initialize()
                    .flatMap(orchestratorInitializationResult -> {
                        logger.info("Orchestrator client initialized.");
                        final var mismatchDetector = new SystemMismatchDetector(pdTracker, systemTracker, alarmManager);
                        mismatchDetector.run();
                        logger.info("Starting the PDE Monitor service...");
                        return new PdeMonitorService(arSystem, pdTracker, httpClient, alarmManager, secureMode)
                            .provide();
                    })
                    .flatMap(mgmtServiceResult -> {
                        logger.info("The PDE Monitor service is ready.");
                        logger.info("Starting the PDE Management service...");
                        var pdeManagementService = new PdeManagementService(pdTracker, secureMode);
                        return arSystem.provide(pdeManagementService.getService());
                    });
            })
            .ifSuccess(consumer -> {
                logger.info("The PDE Management service is ready.");
                logger.info("The Plant Description Engine is up and running.");
            })
            .onFailure(throwable -> {
                logger.error("Failed to launch Plant Description Engine", throwable);
                System.exit(1);
            });
    }
}