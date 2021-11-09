package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt;

import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.InMemoryPdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStoreException;
import org.junit.Test;
import se.arkalix.net.http.service.HttpService;
import se.arkalix.security.access.AccessPolicy;

import static org.junit.Assert.assertEquals;

public class PdeManagementServiceTest {

    @Test
    public void shouldProvideSecureService() throws PdStoreException {
        final PlantDescriptionTracker pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());
        final HttpService service = new PdeManagementService(pdTracker, true).getService();
        assertEquals(AccessPolicy.cloud(), service.accessPolicy());
    }

    @Test
    public void shouldProvideInsecureService() throws PdStoreException {
        final PlantDescriptionTracker pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());
        final HttpService service = new PdeManagementService(pdTracker, false).getService();

        assertEquals(AccessPolicy.unrestricted(), service.accessPolicy());
    }
}