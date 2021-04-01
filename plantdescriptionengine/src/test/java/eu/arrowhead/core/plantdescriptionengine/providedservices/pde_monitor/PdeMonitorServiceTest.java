package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor;

import eu.arrowhead.core.plantdescriptionengine.alarms.AlarmManager;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.InMemoryPdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStoreException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import se.arkalix.ArSystem;
import se.arkalix.net.http.client.HttpClient;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

public class PdeMonitorServiceTest {

    // TODO: The test below doesn't work with ar:kalix version 0.6
    // @Test
    // public void shouldProvideInsecureService() throws PdStoreException {
    //     final PlantDescriptionTracker pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());
    //     final HttpClient client = new HttpClient.Builder().build();

    //     final ArSystem arSystem = new ArSystem.Builder()
    //         .name("Test System")
    //         .insecure()
    //         .build();
    //     final PdeMonitorService service = new PdeMonitorService(
    //         arSystem,
    //         pdTracker,
    //         client,
    //         new AlarmManager(),
    //         false,
    //         1000,
    //         1000
    //     );

    //     service.provide()
    //         .ifSuccess(serviceHandle -> {
    //             SecurityDescriptor security = serviceHandle.description().security();
    //             assertFalse(security.isSecure());
    //         })
    //         .onFailure(e -> fail());
    // }

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
            .ifSuccess(e -> fail())
            .onFailure(Assertions::assertNotNull);
    }
}