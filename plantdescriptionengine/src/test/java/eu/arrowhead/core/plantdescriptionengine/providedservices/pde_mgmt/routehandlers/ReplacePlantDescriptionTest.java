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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpServiceRequest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

public class ReplacePlantDescriptionTest {

    @Test
    public void shouldCreateEntry() throws PdStoreException {

        final PlantDescriptionTracker pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());
        final ReplacePlantDescription handler = new ReplacePlantDescription(pdTracker);
        final MockServiceResponse response = new MockServiceResponse();

        final PlantDescription description = new PlantDescriptionDto.Builder()
            .plantDescription("Plant Description 1A")
            .active(true)
            .build();

        final HttpServiceRequest request = new MockRequest.Builder()
            .pathParameters(List.of("35"))
            .body(description)
            .build();

        try {
            handler.handle(request, response).ifSuccess(result -> {
                assertEquals(HttpStatus.CREATED, response.status().orElse(null));
                assertNotNull(response.getRawBody());

                final PlantDescriptionEntry entry = (PlantDescriptionEntry) response.getRawBody();
                assertEquals(entry.plantDescription(), description.plantDescription());

                final PlantDescriptionEntryDto entryInMap = pdTracker.get(entry.id());
                assertNotNull(entryInMap);
            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            fail();
        }
    }

    @Test
    public void shouldReplaceExistingEntry() throws PdStoreException {

        final PlantDescriptionTracker pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());
        final ReplacePlantDescription handler = new ReplacePlantDescription(pdTracker);
        final int entryId = 87;

        final PlantDescriptionEntryDto entry = TestUtils.createEntry(entryId);
        final String newName = entry.plantDescription() + " modified";
        final PlantDescription description = new PlantDescriptionDto.Builder()
            .plantDescription(newName)
            .active(true)
            .build();
        final MockServiceResponse response = new MockServiceResponse();
        final HttpServiceRequest request = new MockRequest.Builder()
            .pathParameters(List.of(String.valueOf(entryId)))
            .body(description)
            .build();

        pdTracker.put(entry);

        final int sizeBeforePut = pdTracker.getEntries().size();

        try {
            handler.handle(request, response).ifSuccess(result -> {
                assertEquals(HttpStatus.CREATED, response.status().orElse(null));

                final PlantDescriptionEntry returnedEntry = (PlantDescriptionEntry) response.getRawBody();
                assertEquals(returnedEntry.plantDescription(), newName);
                assertEquals(sizeBeforePut, pdTracker.getEntries().size());
            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            fail();
        }
    }

    @Test
    public void shouldRejectInvalidId() throws PdStoreException {
        final PlantDescriptionTracker pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());
        final ReplacePlantDescription handler = new ReplacePlantDescription(pdTracker);
        final String invalidEntryId = "InvalidId";

        final HttpServiceRequest request = new MockRequest.Builder()
            .pathParameters(List.of(invalidEntryId))
            .build();

        final MockServiceResponse response = new MockServiceResponse();

        try {
            handler.handle(request, response)
                .ifSuccess(result -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.status().orElse(null));

                    final String expectedBody = invalidEntryId + " is not a valid Plant Description Entry ID.";
                    assertEquals(expectedBody, response.getRawBody());
                })
                .onFailure(e -> fail());
        } catch (final Exception e) {
            fail();
        }
    }

    @Test
    public void shouldReportInvalidDescription() throws PdStoreException {
        final int entryId = 1;
        final String systemId = "system_a";
        final String portName = "port_a";

        final PlantDescriptionTracker pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());
        final ReplacePlantDescription handler = new ReplacePlantDescription(pdTracker);

        pdTracker.put(TestUtils.createEntry(entryId));

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
            .systemName("System A")
            .ports(consumerPorts)
            .build();

        final PlantDescriptionDto description = new PlantDescriptionDto.Builder()
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(List.of(consumerSystem))
            .include(new ArrayList<>())
            .connections(new ArrayList<>())
            .build();

        final MockServiceResponse response = new MockServiceResponse();
        final MockRequest request = new MockRequest.Builder()
            .pathParameters(List.of(String.valueOf(entryId)))
            .body(description)
            .build();

        try {
            handler.handle(request, response).ifSuccess(result -> {
                assertEquals(HttpStatus.BAD_REQUEST, response.status().orElse(null));
                final String expectedErrorMessage = "<Duplicate port name '" + portName + "' in system '" + systemId + "'>";
                final String actualErrorMessage = ((ErrorMessage) response.getRawBody()).error();
                assertEquals(expectedErrorMessage, actualErrorMessage);
            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            fail();
        }
    }

    @Test
    public void shouldHandleBackingStoreFailure() throws PdStoreException {

        final PdStore backingStore = Mockito.mock(FilePdStore.class);
        final PlantDescriptionTracker pdTracker = new PlantDescriptionTracker(backingStore);
        final ReplacePlantDescription handler = new ReplacePlantDescription(pdTracker);

        final PlantDescription description = new PlantDescriptionDto.Builder()
            .plantDescription("Plant Description 1A")
            .build();

        final MockServiceResponse response = new MockServiceResponse();
        final HttpServiceRequest request = new MockRequest.Builder()
            .pathParameters(List.of("87"))
            .body(description)
            .build();

        doThrow(new PdStoreException("Mocked error")).when(backingStore).write(any(PlantDescriptionEntryDto.class));

        try {
            handler.handle(request, response)
                .ifSuccess(result -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.status().orElse(null)))
                .onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            fail();
        }
    }
}