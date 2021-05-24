package eu.arrowhead.core.plantdescriptionengine;

import org.junit.jupiter.api.Test;
import se.arkalix.ArSystem;
import se.arkalix.net.http.client.HttpClient;

import java.net.InetSocketAddress;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PdeMainTest {

    final private static InetSocketAddress serviceRegistryAddress = new InetSocketAddress("0.0.0.0", 5000);
    final private static String port = "8000";
    final private String hostname = "localhost";

    private Properties getInsecureAppProps() {
        final Properties appProps = new Properties();
        appProps.setProperty(PropertyNames.SERVER_HOSTNAME, hostname);
        appProps.setProperty(PropertyNames.SERVER_PORT, port);
        appProps.setProperty(PropertyNames.SSL_ENABLED, "false");
        return appProps;
    }

    private Properties getSecureAppProps() {
        final Properties appProps = new Properties();
        appProps.setProperty(PropertyNames.SERVER_HOSTNAME, hostname);
        appProps.setProperty(PropertyNames.SERVER_PORT, port);
        appProps.setProperty(PropertyNames.SSL_ENABLED, "true");
        appProps.setProperty(PropertyNames.KEY_STORE, "certificates/plantdescriptionengine.p12");
        appProps.setProperty(PropertyNames.TRUST_STORE, "certificates/truststore.p12");
        appProps.setProperty(PropertyNames.KEY_PASSWORD, "123456");
        appProps.setProperty(PropertyNames.TRUST_STORE_PASSWORD, "123456");
        appProps.setProperty(PropertyNames.KEY_STORE_PASSWORD, "123456");
        return appProps;
    }

    private Properties getAppPropsWithMissingTrustStore() {
        final Properties appProps = new Properties();
        appProps.setProperty(PropertyNames.SERVER_HOSTNAME, hostname);
        appProps.setProperty(PropertyNames.SERVER_PORT, port);
        appProps.setProperty(PropertyNames.SSL_ENABLED, "true");
        return appProps;
    }

    @Test
    public void shouldCreateSecureHttpClient() {
        final Properties appProps = getSecureAppProps();
        final HttpClient client = PdeMain.createHttpClient(appProps);
        assertTrue(client.isSecure());
    }

    @Test
    public void shouldCreateInSecureHttpClient() {
        final Properties appProps = getInsecureAppProps();
        final HttpClient client = PdeMain.createHttpClient(appProps);
        assertFalse(client.isSecure());
    }

    @Test
    public void shouldReportMissingField() {
        final Properties appProps = getAppPropsWithMissingTrustStore();
        final Exception exception = assertThrows(IllegalArgumentException.class,
            () -> PdeMain.createArSystem(appProps, serviceRegistryAddress));
        assertEquals(
            "Missing field '" + PropertyNames.TRUST_STORE + "' in application properties.",
            exception.getMessage()
        );
    }

    @Test
    public void shouldCreateSecureSystem() {
        final Properties appProps = getSecureAppProps();
        final ArSystem system = PdeMain.createArSystem(appProps, serviceRegistryAddress);
        assertEquals(ApiConstants.PDE_SYSTEM_NAME, system.name());
        assertTrue(system.isSecure());
    }

    @Test
    public void shouldInsecureSystem() {
        final Properties appProps = getInsecureAppProps();
        final ArSystem arSystem = PdeMain.createArSystem(appProps, serviceRegistryAddress);
        assertEquals(ApiConstants.PDE_SYSTEM_NAME, arSystem.name());
        assertFalse(arSystem.isSecure());
    }
}