package eu.arrowhead.core.plantdescriptionengine;

import org.junit.jupiter.api.Test;
import se.arkalix.ArSystem;
import se.arkalix.net.http.client.HttpClient;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class PdeMainTest {

    @Test
    public void shouldCreateInsecureHttpClient() {

        Properties appProps = new Properties();
        appProps.setProperty("server.ssl.enabled", "false");

        final HttpClient pdeClient = PdeMain.createHttpClient(appProps);
        assertFalse(pdeClient.isSecure());
    }

    @Test
    public void shouldCreateSecureHttpClient() {

        ClassLoader classLoader = getClass().getClassLoader();

        File keyStoreFile = new File(Objects.requireNonNull(classLoader.getResource("crypto/keystore.p12")).getFile());
        String keyStorePath = keyStoreFile.getAbsolutePath();

        int localPort = 8000;
        Properties appProps = new Properties();
        appProps.setProperty("server.port", Integer.toString(localPort));
        appProps.setProperty("server.ssl.enabled", "true");
        appProps.setProperty("server.ssl.pde.key-store", keyStorePath);
        appProps.setProperty("server.ssl.pde.trust-store", keyStorePath);
        appProps.setProperty("server.ssl.pde.key-password", "123456");
        appProps.setProperty("server.ssl.pde.trust-store-password", "123456");
        appProps.setProperty("server.ssl.pde.key-store-password", "123456");
        final var client = PdeMain.createHttpClient(appProps);
        assertTrue(client.isSecure());
    }

    @Test
    public void shouldCreateArSystem() {
        int localPort = 8000;
        Properties appProps = new Properties();
        appProps.setProperty("server.port", Integer.toString(localPort));
        appProps.setProperty("server.ssl.enabled", "false");
        final var address = new InetSocketAddress("0.0.0.0", 5000);
        final ArSystem arSystem = PdeMain.createArSystem(appProps, address);

        assertEquals(localPort, arSystem.localPort());
        assertEquals("pde", arSystem.name());
        assertFalse(arSystem.isSecure());
    }

    @Test
    public void shouldReportMissingField() {
        int localPort = 8000;
        Properties appProps = new Properties();
        appProps.setProperty("server.port", Integer.toString(localPort));
        appProps.setProperty("server.ssl.enabled", "true");
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> PdeMain.createArSystem(appProps, new InetSocketAddress("0.0.0.0", 5000)));
        assertEquals("Missing field 'server.ssl.pde.trust-store' in application properties.", exception.getMessage());
    }

    @Test
    public void shouldCreateSecureSystem() {

        ClassLoader classLoader = getClass().getClassLoader();

        File keyStoreFile = new File(Objects.requireNonNull(classLoader.getResource("crypto/keystore.p12")).getFile());
        String keyStorePath = keyStoreFile.getAbsolutePath();

        int localPort = 8000;
        Properties appProps = new Properties();
        appProps.setProperty("server.port", Integer.toString(localPort));
        appProps.setProperty("server.ssl.enabled", "true");
        appProps.setProperty("server.ssl.pde.key-store", keyStorePath);
        appProps.setProperty("server.ssl.pde.trust-store", keyStorePath);
        appProps.setProperty("server.ssl.pde.key-password", "123456");
        appProps.setProperty("server.ssl.pde.trust-store-password", "123456");
        appProps.setProperty("server.ssl.pde.key-store-password", "123456");
        final var system = PdeMain.createArSystem(appProps, new InetSocketAddress("0.0.0.0", 5000));
        assertTrue(system.isSecure());
    }
}