package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitorable;

import eu.arrowhead.core.plantdescriptionengine.ApiConstants;
import org.junit.Test;
import se.arkalix.net.http.service.HttpService;
import se.arkalix.security.access.AccessPolicy;

import static org.junit.Assert.assertEquals;

public class PdeMonitorableServiceTest {

    @Test
    public void shouldCreateInSecureService() {
        HttpService service = new PdeMonitorableService(false).getService();

        assertEquals(ApiConstants.MONITORABLE_BASE_PATH, service.basePath());
        assertEquals(ApiConstants.MONITORABLE_SERVICE_NAME, service.name());
        assertEquals(AccessPolicy.unrestricted(), service.accessPolicy());
    }

    @Test
    public void shouldCreateSecureService() {
        HttpService service = new PdeMonitorableService(true).getService();
        assertEquals(AccessPolicy.cloud(), service.accessPolicy());
    }
}
