package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.FilePdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.InMemoryPdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStoreException;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessage;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PdeSystemDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntry;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionUpdate;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionUpdateDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PortDto;
import eu.arrowhead.core.plantdescriptionengine.utils.MockRequest;
import eu.arrowhead.core.plantdescriptionengine.utils.MockServiceResponse;
import eu.arrowhead.core.plantdescriptionengine.utils.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpServiceRequest;

import java.time.Instant;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

public class UpdatePlantDescriptionTest {

    private PlantDescriptionTracker pdTracker;
    private UpdatePlantDescription handler;
    private MockServiceResponse response;

    @Before
    public void initEach() throws PdStoreException {
        pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());
        handler = new UpdatePlantDescription(pdTracker);
        response = new MockServiceResponse();
    }

    @Test
    public void shouldReplaceExistingEntries() {

        final int entryId = 87;

        final PlantDescriptionEntryDto entry = TestUtils.createEntry(entryId);
        final String newName = entry.plantDescription() + " modified";
        final PlantDescriptionUpdate update = new PlantDescriptionUpdateDto.Builder()
            .plantDescription(newName)
            .build();
        final HttpServiceRequest request = new MockRequest.Builder()
            .pathParameters(List.of(String.valueOf(entryId)))
            .body(update)
            .build();

        pdTracker.put(entry)
            .flatMap(result -> handler.handle(request, response))
            .ifSuccess(result -> {
                final PlantDescriptionEntry returnedEntry = (PlantDescriptionEntry) response.getRawBody();
                assertEquals(newName, returnedEntry.plantDescription());
                assertEquals(1, pdTracker.getEntries().size());
                assertTrue(response.status().isPresent());
                assertEquals(HttpStatus.OK, response.status().get());
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldRejectInvalidId() {
        final String invalidEntryId = "InvalidId";

        final HttpServiceRequest request = new MockRequest.Builder()
            .pathParameters(List.of(invalidEntryId))
            .build();

        try {
            handler.handle(request, response).ifSuccess(result -> {
                final String expectedErrorMessage = "'" + invalidEntryId + "' is not a valid Plant Description Entry ID.";
                final String actualErrorMessage = ((ErrorMessage) response.getRawBody()).error();
                assertEquals(expectedErrorMessage, actualErrorMessage);
                assertEquals(HttpStatus.BAD_REQUEST, response.status().orElse(null));
            }).onFailure(e -> fail());
        } catch (final Exception e) {
            fail();
        }
    }

    @Test
    public void shouldRejectNonexistentIds() {
        final int nonExistentId = 9;
        final HttpServiceRequest request = new MockRequest.Builder().pathParameters(List.of(String.valueOf(nonExistentId)))
            .build();

        try {
            handler.handle(request, response).ifSuccess(result -> {
                assertEquals(HttpStatus.NOT_FOUND, response.status().orElse(null));
                final String expectedErrorMessage = "Plant Description with ID '" + nonExistentId + "' not found.";
                final ErrorMessage body = (ErrorMessage) response.getRawBody();
                final String actualErrorMessage = body.error();
                assertEquals(expectedErrorMessage, actualErrorMessage);
            }).onFailure(e -> fail());
        } catch (final Exception e) {
            fail();
        }
    }

    @Test
    public void shouldReportInvalidDescription() {
        final int entryId = 1;
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
                .portName(portName)
                .serviceInterface("HTTP-SECURE-JSON")
                .serviceDefinition("service_b")
                .consumer(true)
                .build());

        final PdeSystemDto consumerSystem = new PdeSystemDto.Builder()
            .systemId(systemId)
            .systemName("systemxyz")
            .ports(consumerPorts)
            .build();

        final PlantDescriptionUpdateDto update = new PlantDescriptionUpdateDto.Builder()
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(List.of(consumerSystem))
            .build();

        final MockRequest request = new MockRequest.Builder()
            .pathParameters(List.of(String.valueOf(entryId)))
            .body(update)
            .build();

        pdTracker.put(TestUtils.createEntry(entryId))
            .flatMap(result -> handler.handle(request, response))
            .ifSuccess(result -> {
                assertEquals(HttpStatus.BAD_REQUEST, response.status().orElse(null));
                final String expectedErrorMessage = "<Duplicate port name '" + portName + "' in system '" + systemId + "'>";
                final ErrorMessage body = (ErrorMessage) response.getRawBody();
                final String actualErrorMessage = body.error();
                assertEquals(expectedErrorMessage, actualErrorMessage);
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldHandleBackingStoreFailure() throws PdStoreException {

        final PdStore backingStore = Mockito.mock(FilePdStore.class);
        final PlantDescriptionTracker pdTracker = new PlantDescriptionTracker(backingStore);
        final UpdatePlantDescription handler = new UpdatePlantDescription(pdTracker);
        final int entryId = 87;

        final PlantDescriptionEntryDto entry = TestUtils.createEntry(entryId);
        final String newName = entry.plantDescription() + " modified";
        final PlantDescriptionUpdate update = new PlantDescriptionUpdateDto.Builder()
            .plantDescription(newName)
            .build();
        final MockServiceResponse response = new MockServiceResponse();
        final HttpServiceRequest request = new MockRequest.Builder()
            .pathParameters(List.of(String.valueOf(entryId)))
            .body(update)
            .build();

        pdTracker.put(entry)
            .flatMap(result -> {
                doThrow(new PdStoreException("Mocked error")).when(backingStore).write(any());
                return handler.handle(request, response);
            })
            .ifSuccess(result -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.status().orElse(null)))
            .onFailure(e -> fail());
    }

    @Test
    public void shouldDeactivateOldActive() {

        final Instant now = Instant.now();

        final int initiallyActiveId = 23;
        final int initiallyInactiveId = 24;

        final PlantDescriptionEntryDto initiallyActiveEntry = TestUtils.createEntry(initiallyActiveId);
        final PlantDescriptionEntryDto initiallyInactiveEntry = new PlantDescriptionEntryDto.Builder()
            .id(initiallyInactiveId)
            .plantDescription("XYZ")
            .active(false)
            .createdAt(now)
            .updatedAt(now)
            .build();

        final PlantDescriptionUpdateDto update = new PlantDescriptionUpdateDto.Builder()
            .active(true)
            .build();

        // Create a request for activating the inactive Plant Description.
        final HttpServiceRequest request = new MockRequest.Builder()
            .pathParameters(List.of(String.valueOf(initiallyInactiveId)))
            .body(update)
            .build();

        pdTracker.put(initiallyActiveEntry)
            .flatMap(result -> pdTracker.put(initiallyInactiveEntry))
            .flatMap(result -> handler.handle(request, response))
            .ifSuccess(result -> {
                assertFalse(pdTracker.get(initiallyActiveId).active());
                assertTrue(pdTracker.get(initiallyInactiveId).active());

            })
            .onFailure(e -> fail());
    }
}