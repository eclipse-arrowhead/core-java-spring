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
import se.arkalix.net.http.service.HttpServiceRequest;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

public class ReplacePlantDescriptionTest {

    private PlantDescriptionTracker pdTracker;
    private ReplacePlantDescription handler;
    private MockServiceResponse response;

    @Before
    public void initEach() throws PdStoreException {
        pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());
        handler = new ReplacePlantDescription(pdTracker);
        response = new MockServiceResponse();
    }

    @Test
    public void shouldCreateEntry() {

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
                assertEquals(HttpStatus.OK, response.status().orElse(null));
                assertNotNull(response.getRawBody());

                final PlantDescriptionEntry entry = (PlantDescriptionEntry) response.getRawBody();
                assertEquals(entry.plantDescription(), description.plantDescription());

                final PlantDescriptionEntryDto entryInMap = pdTracker.get(entry.id());
                assertNotNull(entryInMap);
            }).onFailure(e -> fail());
        } catch (final Exception e) {
            fail();
        }
    }

    @Test
    public void shouldReplaceExistingEntry() {

        final int entryId = 87;

        final PlantDescriptionEntryDto entry = TestUtils.createEntry(entryId);
        final String newName = entry.plantDescription() + " modified";
        final PlantDescription description = new PlantDescriptionDto.Builder()
            .plantDescription(newName)
            .active(true)
            .build();
        final HttpServiceRequest request = new MockRequest.Builder()
            .pathParameters(List.of(String.valueOf(entryId)))
            .body(description)
            .build();

        pdTracker.put(entry)
            .flatMap(result -> handler.handle(request, response))
            .ifSuccess(result -> {
                assertEquals(HttpStatus.OK, response.status().orElse(null));

                final PlantDescriptionEntry returnedEntry = (PlantDescriptionEntry) response.getRawBody();
                assertEquals(returnedEntry.plantDescription(), newName);
                assertEquals(1, pdTracker.getEntries().size());
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
            .systemName("abc")
            .ports(consumerPorts)
            .build();

        final PlantDescriptionDto description = new PlantDescriptionDto.Builder()
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(List.of(consumerSystem))
            .build();

        final MockRequest request = new MockRequest.Builder()
            .pathParameters(List.of(String.valueOf(entryId)))
            .body(description)
            .build();

        pdTracker.put(TestUtils.createEntry(entryId))
            .flatMap(result -> handler.handle(request, response))
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
        final ReplacePlantDescription handler = new ReplacePlantDescription(pdTracker);

        final PlantDescription description = new PlantDescriptionDto.Builder()
            .plantDescription("Plant Description 1A")
            .build();

        final HttpServiceRequest request = new MockRequest.Builder()
            .pathParameters(List.of("87"))
            .body(description)
            .build();

        doThrow(new PdStoreException("Mocked error")).when(backingStore).write(any(PlantDescriptionEntryDto.class));

        try {
            handler.handle(request, response)
                .ifSuccess(result -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.status().orElse(null)))
                .onFailure(e -> fail());
        } catch (final Exception e) {
            fail();
        }
    }
}