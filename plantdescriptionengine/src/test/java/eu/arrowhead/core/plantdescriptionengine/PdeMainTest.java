package eu.arrowhead.core.plantdescriptionengine;

import org.junit.jupiter.api.Test;
import se.arkalix.ArSystem;
import se.arkalix.net.http.client.HttpClient;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PdeMainTest {

    @Test
    public void shouldCreateInsecureHttpClient() {

        final Properties appProps = new Properties();
        appProps.setProperty(PropertyNames.SSL_ENABLED, "false");

        final HttpClient pdeClient = PdeMain.createHttpClient(appProps);
        assertFalse(pdeClient.isSecure());
    }

    @Test
    public void shouldCreateSecureHttpClient() {

        final ClassLoader classLoader = getClass().getClassLoader();

        final File keyStoreFile = new File(Objects.requireNonNull(classLoader.getResource("crypto/keystore.p12")).getFile());
        final String keyStorePath = keyStoreFile.getAbsolutePath();

        final int port = 8000;
        final Properties appProps = new Properties();
        appProps.setProperty(PropertyNames.SERVER_PORT, Integer.toString(port));
        appProps.setProperty(PropertyNames.SSL_ENABLED, "true");
        appProps.setProperty(PropertyNames.KEY_STORE, keyStorePath);
        appProps.setProperty(PropertyNames.TRUST_STORE, keyStorePath);
        appProps.setProperty(PropertyNames.KEY_PASSWORD, "123456");
        appProps.setProperty(PropertyNames.TRUST_STORE_PASSWORD, "123456");
        appProps.setProperty(PropertyNames.KEY_STORE_PASSWORD, "123456");

        final HttpClient client = PdeMain.createHttpClient(appProps);
        assertTrue(client.isSecure());
    }

    @Test
    public void shouldCreateArSystem() {
        final String hostname = "localhost";
        final int port = 8000;
        final Properties appProps = new Properties();
        appProps.setProperty(PropertyNames.SERVER_HOSTNAME, hostname);
        appProps.setProperty(PropertyNames.SERVER_PORT, Integer.toString(port));
        appProps.setProperty(PropertyNames.SSL_ENABLED, "false");
        final InetSocketAddress address = new InetSocketAddress("0.0.0.0", 5000);
        final ArSystem arSystem = PdeMain.createArSystem(appProps, address);

        assertEquals(port, arSystem.port());
        assertEquals(Constants.PDE_SYSTEM_NAME, arSystem.name());
        assertFalse(arSystem.isSecure());
    }

    @Test
    public void shouldReportMissingField() {
        final String hostname = "localhost";
        final int port = 8000;
        final Properties appProps = new Properties();
        appProps.setProperty(PropertyNames.SERVER_HOSTNAME, hostname);
        appProps.setProperty(PropertyNames.SERVER_PORT, Integer.toString(port));
        appProps.setProperty(PropertyNames.SSL_ENABLED, "true");
        final Exception exception = assertThrows(IllegalArgumentException.class,
            () -> PdeMain.createArSystem(appProps, new InetSocketAddress("0.0.0.0", 5000)));
        assertEquals("Missing field '" + PropertyNames.TRUST_STORE + "' in application properties.", exception.getMessage());
    }

    @Test
    public void shouldCreateSecureSystem() {

        final ClassLoader classLoader = getClass().getClassLoader();

        final File keyStoreFile = new File(Objects.requireNonNull(classLoader.getResource("crypto/keystore.p12")).getFile());
        final String keyStorePath = keyStoreFile.getAbsolutePath();

        final String hostname = "localhost";
        final int port = 8000;
        final Properties appProps = new Properties();
        appProps.setProperty(PropertyNames.SERVER_HOSTNAME, hostname);
        appProps.setProperty(PropertyNames.SERVER_PORT, Integer.toString(port));
        appProps.setProperty(PropertyNames.SSL_ENABLED, "true");
        appProps.setProperty(PropertyNames.KEY_STORE, keyStorePath);
        appProps.setProperty(PropertyNames.TRUST_STORE, keyStorePath);
        appProps.setProperty(PropertyNames.KEY_PASSWORD, "123456");
        appProps.setProperty(PropertyNames.TRUST_STORE_PASSWORD, "123456");
        appProps.setProperty(PropertyNames.KEY_STORE_PASSWORD, "123456");
        final ArSystem system = PdeMain.createArSystem(appProps, new InetSocketAddress("0.0.0.0", 5000));
        assertTrue(system.isSecure());
    }
}