package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.FilePdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.InMemoryPdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStoreException;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessage;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PdeSystemDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescription;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntry;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PortDto;
import eu.arrowhead.core.plantdescriptionengine.utils.MockRequest;
import eu.arrowhead.core.plantdescriptionengine.utils.MockServiceResponse;
import eu.arrowhead.core.plantdescriptionengine.utils.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.arkalix.net.http.HttpStatus;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

public class AddPlantDescriptionTest {

    private PlantDescriptionTracker pdTracker;
    private AddPlantDescription handler;
    private MockServiceResponse response;

    @Before
    public void initEach() throws PdStoreException {
        pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());
        handler = new AddPlantDescription(pdTracker);
        response = new MockServiceResponse();
    }

    @Test
    public void shouldCreateEntry() {

        final PlantDescription description = new PlantDescriptionDto.Builder()
            .plantDescription("Plant Description 1A")
            .build();

        final MockRequest request = new MockRequest.Builder()
            .body(description)
            .build();

        handler.handle(request, response)
            .ifSuccess(result -> {
                assertTrue(response.status().isPresent());
                assertEquals(HttpStatus.CREATED, response.status().get());

                final PlantDescriptionEntry entry = (PlantDescriptionEntry) response.getRawBody();

                assertEquals(entry.plantDescription(), description.plantDescription());

                final PlantDescriptionEntryDto entryInMap = pdTracker.get(entry.id());
                assertNotNull(entryInMap);
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldAcceptUniqueMetadata() {

        final String serviceDefinition = "service_a";
        final Map<String, String> metadataA = Map.of("a", "1");
        final Map<String, String> metadataB = Map.of("a", "2");

        final List<PortDto> ports = List.of(
            new PortDto.Builder()
                .portName("port_a")
                .serviceInterface("HTTP-SECURE-JSON")
                .serviceDefinition(serviceDefinition)
                .metadata(metadataA)
                .build(),
            new PortDto.Builder()
                .portName("port_b")
                .serviceInterface("HTTP-SECURE-JSON")
                .serviceDefinition(serviceDefinition)
                .metadata(metadataB)
                .build());

        final PdeSystemDto system = new PdeSystemDto.Builder()
            .systemId("system_a")
            .systemName("abc")
            .ports(ports)
            .build();

        final PlantDescriptionDto description = new PlantDescriptionDto.Builder()
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(List.of(system))
            .build();

        final MockRequest request = new MockRequest.Builder()
            .body(description)
            .build();

        handler.handle(request, response)
            .ifSuccess(result -> assertEquals(HttpStatus.CREATED, response.status().orElse(null)))
            .onFailure(e -> fail());
    }

    @Test
    public void shouldReportInvalidDescription() {
        final String systemId = "system_a";
        final String portName = "port_a";

        final List<PortDto> consumerPorts = List.of(
            new PortDto.Builder()
                .portName(portName)
                .serviceInterface("HTTP-SECURE-JSON")
                .serviceDefinition("service_a")
                .consumer(true)
                .build(),
            new PortDto.Builder()
                .portName(portName) // Duplicate port name, should be reported!
                .serviceInterface("HTTP-SECURE-JSON")
                .serviceDefinition("service_b")
                .consumer(true)
                .build());

        final PdeSystemDto consumerSystem = new PdeSystemDto.Builder()
            .systemId(systemId)
            .systemName("sysa")
            .ports(consumerPorts)
            .build();

        final PlantDescriptionDto description = new PlantDescriptionDto.Builder()
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(List.of(consumerSystem))
            .build();

        final MockRequest request = new MockRequest.Builder()
            .body(description)
            .build();

        handler.handle(request, response)
            .ifSuccess(result -> {
                assertEquals(HttpStatus.BAD_REQUEST, response.status().orElse(null));
                final String expectedErrorMessage = "<Duplicate port name '" + portName + "' in system '" + systemId + "'>";
                final String actualErrorMessage = ((ErrorMessage) response.getRawBody()).error();
                assertEquals(expectedErrorMessage, actualErrorMessage);
            })
            .onFailure(e -> fail());

    }

    @Test
    public void shouldHandleBackingStoreFailure() throws PdStoreException {

        final PdStore backingStore = Mockito.mock(FilePdStore.class);
        final PlantDescriptionTracker pdTracker = new PlantDescriptionTracker(backingStore);
        final AddPlantDescription handler = new AddPlantDescription(pdTracker);

        final PlantDescription description = new PlantDescriptionDto.Builder()
            .plantDescription("Plant Description 1A")
            .build();

        final MockServiceResponse response = new MockServiceResponse();
        final MockRequest request = new MockRequest.Builder().body(description).build();

        doThrow(new PdStoreException("Mocked error")).when(backingStore).write(any());

        handler.handle(request, response)
            .ifSuccess(result -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.status().orElse(null)))
            .onFailure(e -> fail());
    }

    @Test
    public void shouldDeactivateOldActive() {
        final int initiallyActiveId = 18;

        final PlantDescriptionEntryDto initiallyActiveEntry = TestUtils.createEntry(initiallyActiveId);
        final PlantDescriptionDto newDescription = new PlantDescriptionDto.Builder()
            .plantDescription("XYZ")
            .active(true)
            .build();

        pdTracker.put(initiallyActiveEntry)
            .flatMap(result -> {
                final MockRequest request = new MockRequest.Builder()
                    .body(newDescription)
                    .build();

                return handler.handle(request, response);
            })
            .ifSuccess(result -> {
                final PlantDescriptionEntryDto newEntry = (PlantDescriptionEntryDto) response.getRawBody();
                final int newId = newEntry.id();
                assertFalse(pdTracker.get(initiallyActiveId).active());
                assertTrue(pdTracker.get(newId).active());
            })
            .onFailure(e -> fail());

    }

}