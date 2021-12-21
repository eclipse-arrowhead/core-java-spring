package eu.arrowhead.core.plantdescriptionengine;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.arkalix.ArSystem;
import se.arkalix.net.http.client.HttpClient;

import java.net.InetSocketAddress;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PdeMainTest {

    final private static InetSocketAddress serviceRegistryAddress = new InetSocketAddress("0.0.0.0", 5000);
    final private static String port = "8000";
    final private String hostname = "localhost";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

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
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Missing field '" + PropertyNames.TRUST_STORE + "' in application properties.");
        PdeMain.createArSystem(appProps, serviceRegistryAddress);
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

    @Test
    public void shouldLoadAppProps() {
        Properties appProps = PdeMain.loadAppProps();
        System.out.println(appProps);
        assertEquals("certificates/truststore.p12", appProps.getProperty(PropertyNames.TRUST_STORE));
    }

    @Test
    public void shouldLoadPlantDescriptionTracker() {
        final Properties appProps = getInsecureAppProps();
        appProps.setProperty(PropertyNames.PD_MAX_SIZE, "1000");
        appProps.setProperty(PropertyNames.DB_CONNECTION_URL, "jdbc:h2:mem:testdb");
        appProps.setProperty(PropertyNames.DB_USERNAME, "root");
        appProps.setProperty(PropertyNames.DB_PASSWORD, "password");
        appProps.setProperty(PropertyNames.DB_DRIVER_CLASS_NAME, "org.h2.Driver");
        assertNotNull(PdeMain.loadPlantDescriptionTracker(appProps));
    }
}