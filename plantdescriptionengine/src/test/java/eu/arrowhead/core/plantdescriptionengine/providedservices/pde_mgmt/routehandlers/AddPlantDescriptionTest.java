package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.FilePdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.InMemoryPdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStoreException;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessage;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.*;
import eu.arrowhead.core.plantdescriptionengine.utils.MockRequest;
import eu.arrowhead.core.plantdescriptionengine.utils.MockServiceResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpServiceResponse;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

public class AddPlantDescriptionTest {

    @Test
    public void shouldCreateEntry() throws PdStoreException {

        final var pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());
        final var handler = new AddPlantDescription(pdTracker);

        final PlantDescription description = new PlantDescriptionBuilder()
            .plantDescription("Plant Description 1A")
            .build();

        final HttpServiceResponse response = new MockServiceResponse();
        final MockRequest request = new MockRequest.Builder()
            .body(description)
            .build();

        try {
            handler.handle(request, response)
                .ifSuccess(result -> {
                    assertTrue(response.status().isPresent());
                    assertEquals(HttpStatus.CREATED, response.status().get());
                    assertNotNull(response.body());
                    assertTrue(response.body().isPresent());

                    PlantDescriptionEntry entry = (PlantDescriptionEntry) response.body().get();

                    assertEquals(entry.plantDescription(), description.plantDescription());

                    var entryInMap = pdTracker.get(entry.id());
                    assertNotNull(entryInMap);
                })
                .onFailure(Assertions::assertNull);
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void shouldAcceptUniqueMetadata() throws PdStoreException {

        final String serviceDefinition = "service_a";
        final Map<String, String> metadataA = Map.of("a", "1");
        final Map<String, String> metadataB = Map.of("a", "2");

        final var pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());
        final var handler = new AddPlantDescription(pdTracker);

        final List<PortDto> ports = List.of(
            new PortBuilder()
                .portName("port_a")
                .serviceInterface("HTTP-SECURE-JSON")
                .serviceDefinition(serviceDefinition)
                .metadata(metadataA)
                .build(),
            new PortBuilder()
                .portName("port_b")
                .serviceInterface("HTTP-SECURE-JSON")
                .serviceDefinition(serviceDefinition)
                .metadata(metadataB)
                .build());

        final PdeSystemDto system = new PdeSystemBuilder()
            .systemId("system_a")
            .systemName("System A")
            .ports(ports)
            .build();

        final var description = new PlantDescriptionBuilder()
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(List.of(system))
            .build();

        final HttpServiceResponse response = new MockServiceResponse();
        final MockRequest request = new MockRequest.Builder()
            .body(description)
            .build();

        try {
            handler.handle(request, response)
                .ifSuccess(result -> assertEquals(HttpStatus.CREATED, response.status().orElse(null)))
                .onFailure(Assertions::assertNull);
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void shouldReportInvalidDescription() throws PdStoreException {
        final String systemId = "system_a";
        final String portName = "port_a";

        final var pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());
        final var handler = new AddPlantDescription(pdTracker);

        final List<PortDto> consumerPorts = List.of(
            new PortBuilder()
                .portName(portName)
                .serviceInterface("HTTP-SECURE-JSON")
                .serviceDefinition("service_a")
                .consumer(true)
                .build(),
            new PortBuilder()
                .portName(portName) // Duplicate port name, should be reported!
                .serviceInterface("HTTP-SECURE-JSON")
                .serviceDefinition("service_b")
                .consumer(true)
                .build());

        final PdeSystemDto consumerSystem = new PdeSystemBuilder()
            .systemId(systemId)
            .systemName("System A")
            .ports(consumerPorts)
            .build();

        final var description = new PlantDescriptionBuilder()
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(List.of(consumerSystem))
            .build();

        final HttpServiceResponse response = new MockServiceResponse();
        final MockRequest request = new MockRequest.Builder()
            .body(description)
            .build();

        try {
            handler.handle(request, response)
                .ifSuccess(result -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.status().orElse(null));
                    String expectedErrorMessage = "<Duplicate port name '" + portName + "' in system '" + systemId + "'>";
                    assertTrue(response.body().isPresent());
                    String actualErrorMessage = ((ErrorMessage) response.body().get()).error();
                    assertEquals(expectedErrorMessage, actualErrorMessage);
                })
                .onFailure(Assertions::assertNull);
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void shouldHandleBackingStoreFailure() throws PdStoreException {

        final var backingStore = Mockito.mock(FilePdStore.class);
        final var pdTracker = new PlantDescriptionTracker(backingStore);
        final var handler = new AddPlantDescription(pdTracker);

        final PlantDescription description = new PlantDescriptionBuilder()
            .plantDescription("Plant Description 1A")
            .build();

        final HttpServiceResponse response = new MockServiceResponse();
        final MockRequest request = new MockRequest.Builder().body(description).build();

        doThrow(new PdStoreException("Mocked error")).when(backingStore).write(any());

        try {
            handler.handle(request, response)
                .ifSuccess(result -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.status().orElse(null)))
                .onFailure(Assertions::assertNull);
        } catch (Exception e) {
            assertNull(e);
        }

    }

}