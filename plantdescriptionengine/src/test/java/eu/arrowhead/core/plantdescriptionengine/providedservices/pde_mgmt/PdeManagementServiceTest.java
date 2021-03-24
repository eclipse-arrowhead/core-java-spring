package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt;

import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.InMemoryPdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStoreException;
import org.junit.jupiter.api.Test;
import se.arkalix.security.access.AccessPolicy;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PdeManagementServiceTest {

    @Test
    public void shouldProvideSecureService() throws PdStoreException {
        final var pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());
        final var service = new PdeManagementService(pdTracker, true).getService();
        assertEquals(AccessPolicy.cloud(), service.accessPolicy());
    }

    @Test
    public void shouldProvideInsecureService() throws PdStoreException {
        final var pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());
        final var service = new PdeManagementService(pdTracker, false).getService();

        assertEquals(AccessPolicy.unrestricted(), service.accessPolicy());
    }
}