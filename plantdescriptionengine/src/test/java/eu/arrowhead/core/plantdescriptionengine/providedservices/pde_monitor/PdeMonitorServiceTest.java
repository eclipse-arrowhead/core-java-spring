package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor;

import eu.arrowhead.core.plantdescriptionengine.alarms.AlarmManager;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.InMemoryPdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStoreException;
import org.junit.Assert;
import org.junit.Test;
import se.arkalix.ArSystem;
import se.arkalix.net.http.client.HttpClient;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class PdeMonitorServiceTest {

    @Test
    public void shouldProvideInsecureService() throws PdStoreException {
        final PlantDescriptionTracker pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());
        final HttpClient client = new HttpClient.Builder().build();

        final ArSystem arSystem = new ArSystem.Builder()
            .name("Test System")
            .insecure()
            .build();
        final PdeMonitorService service = new PdeMonitorService(
            arSystem,
            pdTracker,
            client,
            new AlarmManager(),
            false,
            1000,
            1000
        );

        service.provide()
            .ifSuccess(serviceHandle -> assertFalse(serviceHandle.description().accessPolicyType().isSecure()))
            .onFailure(e -> fail());
    }

    @Test
    public void shouldNotAllowInsecureArSystem() throws PdStoreException {
        final PlantDescriptionTracker pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());
        final HttpClient client = new HttpClient.Builder().build();

        final ArSystem arSystem = new ArSystem.Builder()
            .name("Test System")
            .insecure()
            .build();
        final PdeMonitorService service = new PdeMonitorService(arSystem, pdTracker, client, new AlarmManager(), true, 1000, 1000);

        service.provide()
            .ifSuccess(result -> fail())
            .onFailure(Assert::assertNotNull);
    }
}