package eu.arrowhead.core.plantdescriptionengine;

import eu.arrowhead.core.plantdescriptionengine.alarms.AlarmManager;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.OrchestratorClient;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.rulebackingstore.FileRuleStore;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.SystemTracker;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.dto.SrSystem;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.FilePdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStore;
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
import java.util.Properties;

public final class PdeMain {

    private static final Logger logger = LoggerFactory.getLogger(PdeMain.class);

    private static final String PDE_SYSTEM_NAME = "plantdescriptionengine";
    private static final String APP_PROPS_FILENAME=  "application.properties";

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

        final boolean secureMode = Boolean.parseBoolean(getProp(appProps, "server.ssl.enabled"));
        if (!secureMode) {
            return new HttpClient.Builder().insecure().build();
        }

        final OwnedIdentity identity = loadIdentity(
            getProp(appProps, "server.ssl.pde.key-store"),
            getProp(appProps, "server.ssl.pde.key-password").toCharArray(),
            getProp(appProps, "server.ssl.pde.key-store-password").toCharArray()
        );

        final TrustStore trustStore = loadTrustStore(
            getProp(appProps, "server.ssl.pde.trust-store"),
            getProp(appProps, "server.ssl.pde.trust-store-password").toCharArray()
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

        final int pdePort = Integer.parseInt(getProp(appProps, "server.port"));

        final OrchestrationStrategy strategy = new OrchestrationStrategy(
            new OrchestrationPattern().isIncludingService(true)
                .option(OrchestrationOption.METADATA_SEARCH, false)
                .option(OrchestrationOption.PING_PROVIDERS, true)
                .option(OrchestrationOption.OVERRIDE_STORE, false));

        final ArSystem.Builder systemBuilder = new ArSystem.Builder()
            .serviceCache(ArServiceRecordCache.withEntryLifetimeLimit(Duration.ZERO))
            .localPort(pdePort)
            .plugins(new HttpJsonCloudPlugin.Builder()
                .orchestrationStrategy(strategy)
                .serviceRegistrySocketAddress(serviceRegistryAddress)
                .build());

        final boolean secureMode = Boolean.parseBoolean(getProp(appProps, "server.ssl.enabled"));

        if (secureMode) {
            final String trustStorePath = getProp(appProps, "server.ssl.pde.trust-store");
            final char[] trustStorePassword = getProp(appProps, "server.ssl.pde.trust-store-password").toCharArray();
            final String keyStorePath = getProp(appProps, "server.ssl.pde.key-store");
            final char[] keyPassword = getProp(appProps, "server.ssl.pde.key-password").toCharArray();
            final char[] keyStorePassword = getProp(appProps, "server.ssl.pde.key-store-password").toCharArray();
            systemBuilder.identity(loadIdentity(keyStorePath, keyPassword, keyStorePassword))
                .trustStore(loadTrustStore(trustStorePath, trustStorePassword));
        } else {
            systemBuilder.name(PDE_SYSTEM_NAME).insecure();
        }

        return systemBuilder.build();

    }

    /**
     * Loads the keystore at the given path, using the given password.
     * <p>
     * {@code path} is first treated as a regular file path. If the certificate
     * cannot be found at that location, an attempt is made to load it from
     * resources (i.e. within the jar file).
     * If the keystore cannot be loaded, the application is terminated.
     *
     * @param path     Keystore path.
     * @param password Password or private key associated with the
     *                 keystore.
     * @return The loaded keystore.
     */
    private static KeyStore loadKeyStore(String path, char[] password) {

        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (KeyStoreException e) {
            logger.error("Failed to load identity keystore", e);
            System.exit(74);
        }

        File keyStoreFile = new File(path);

        if (keyStoreFile.isFile()) {
            try (FileInputStream in = new FileInputStream(keyStoreFile)) {
                keyStore.load(in, password);
            } catch (NoSuchAlgorithmException | CertificateException | IOException e) {
                logger.error("Failed to read keystore from file", e);
                System.exit(74);
            }
        } else {
            try (InputStream in = ClassLoader.getSystemResourceAsStream(path)) {
                keyStore.load(in, password);
            } catch (NoSuchAlgorithmException | CertificateException | IOException e) {
                logger.error("Failed to load keystore from resources", e);
                System.exit(74);
            }
        }

        return keyStore;
    }

    /**
     * Loads the Arrowhead certificate chain and private key required to manage
     * an <i>owned</i> system or operator identity.
     * <p>
     * If the certificate cannot be read, the entire application
     * is terminated.
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
            logger.error("Failed to load OwnedIdentity", e);
            System.exit(74);
        }

        Arrays.fill(keyPassword, '\0');
        Arrays.fill(keyStorePassword, '\0');

        return identity;
    }

    /**
     * Loads certificates associated with a <i>trusted</i> Arrowhead systems.
     * <p>
     * The argument {@code path} is first treated as a regular file path. If it
     * cannot be found, an attempt is made to find it in resources (i.e. within
     * the jar file). If the certificate cannot be read, the entire application
     * is terminated.
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
            logger.error("Failed to read trust store", e);
            System.exit(1);
        }

        Arrays.fill(password, '\0');

        return trustStore;
    }

    /**
     * Loads application properties.
     * <p>
     * It first searches the current working directory for a file called
     * application.properties. If not found, the application.properties file
     * from the resources directory is used instead. If neither can be read, the
     * application is terminated.
     */
    private static Properties loadAppProps() {
        final Properties appProps = new Properties();
        File appPropsFile = new File(APP_PROPS_FILENAME);

        if (appPropsFile.isFile()) {
            try (FileInputStream in = new FileInputStream(appPropsFile)) {
                appProps.load(in);
            } catch (final IOException e) {
                logger.error("Failed reading " + APP_PROPS_FILENAME + " from current directory.", e);
            }
        } else {
            try {
                appProps.load(ClassLoader.getSystemResourceAsStream(APP_PROPS_FILENAME));
            } catch (IOException e) {
                logger.error("Failed reading " + APP_PROPS_FILENAME + " from system resources.", e);
            }
        }

        if (appProps.isEmpty()) {
            logger.error("No valid application properties, exiting.");
            System.exit(74);
        }

        return appProps;
    }

    /**
     * Main method of the Plant Description Engine.
     * <p>
     * Provides Plant Description management and monitoring services to the
     * Arrowhead system of systems.
     */
    public static void main(final String[] args) {

        final Properties appProps = loadAppProps();

        final String serviceRegistryIp = getProp(appProps, "service_registry.address");
        final int serviceRegistryPort = Integer.parseInt(getProp(appProps, "service_registry.port"));
        final InetSocketAddress serviceRegistryAddress = new InetSocketAddress(serviceRegistryIp, serviceRegistryPort);

        final HttpClient httpClient = createHttpClient(appProps);

        final SystemTracker systemTracker = new SystemTracker(httpClient, serviceRegistryAddress);

        logger.info("Start polling Service Registry for systems...");
        systemTracker.start()
            .flatMap(result -> {

                final String ruleDirectory = getProp(appProps, "orchestration_rules");
                final String plantDescriptionsDirectory = getProp(appProps, "plant_descriptions");
                final PdStore pdStore = new FilePdStore(plantDescriptionsDirectory);
                final PlantDescriptionTracker pdTracker = new PlantDescriptionTracker(pdStore);
                final SrSystem orchestrator = systemTracker.getSystem("orchestrator", null);

                if (orchestrator == null) {
                    throw new RuntimeException("Could not find Orchestrator in the Service Registry.");
                }

                final OrchestratorClient orchestratorClient = new OrchestratorClient(
                    httpClient,
                    new FileRuleStore(ruleDirectory),
                    pdTracker,
                    orchestrator.getAddress()
                );

                final ArSystem arSystem = createArSystem(appProps, serviceRegistryAddress);
                final boolean secureMode = Boolean.parseBoolean(getProp(appProps, "server.ssl.enabled"));
                final AlarmManager alarmManager = new AlarmManager();

                logger.info("Initializing the Orchestrator client...");

                return orchestratorClient.initialize()
                    .flatMap(orchestratorInitializationResult -> {
                        logger.info("Orchestrator client initialized.");
                        final SystemMismatchDetector mismatchDetector = new SystemMismatchDetector(
                            pdTracker,
                            systemTracker,
                            alarmManager
                        );
                        mismatchDetector.run();
                        logger.info("Starting the PDE Monitor service...");
                        final int pingInterval = Integer.parseInt(getProp(appProps, "ping_interval"));
                        final int fetchInterval = Integer.parseInt(getProp(appProps, "fetch_interval"));
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
                        final PdeMonitorableService pdeMonitorableService = new PdeMonitorableService(
                            PDE_SYSTEM_NAME,
                            secureMode
                        );
                        return arSystem.provide(pdeMonitorableService.getService());
                    });
            })
            .ifSuccess(consumer -> {
                logger.info("The PDE Monitorable service is ready.");
                logger.info("The Plant Description Engine is up and running.");
            })
            .onFailure(e -> {
                logger.error("Failed to launch Plant Description Engine.", e);
                System.exit(1);
            });
    }
}