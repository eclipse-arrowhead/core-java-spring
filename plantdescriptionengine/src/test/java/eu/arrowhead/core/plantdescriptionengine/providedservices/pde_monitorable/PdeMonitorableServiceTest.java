package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitorable;

import org.junit.jupiter.api.Test;
import se.arkalix.net.http.service.HttpService;
import se.arkalix.security.access.AccessPolicy;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PdeMonitorableServiceTest {

    final String systemName = "abc";

    @Test
    public void shouldCreateInSecureService() {
        HttpService service = new PdeMonitorableService(systemName, false).getService();
        final String expectedBasePath = "/pde/monitorable";
        final String expectedName = "monitorable";

        assertEquals(expectedBasePath, service.basePath());
        assertEquals(expectedName, service.name());
        assertEquals(AccessPolicy.unrestricted(), service.accessPolicy());
    }

    @Test
    public void shouldCreateSecureService() {
        HttpService service = new PdeMonitorableService(systemName, true).getService();
        assertEquals(AccessPolicy.cloud(), service.accessPolicy());
    }
}
