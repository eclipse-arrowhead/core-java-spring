package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.FilePdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.InMemoryPdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStoreException;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessage;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryDto;
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;

public class DeletePlantDescriptionTest {

    private PlantDescriptionTracker pdTracker;
    private DeletePlantDescription handler;
    private MockServiceResponse response;

    @Before
    public void initEach() throws PdStoreException {
        pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());
        handler = new DeletePlantDescription(pdTracker);
        response = new MockServiceResponse();
    }

    @Test
    public void shouldDeleteEntries() {
        final int entryId = 14;
        pdTracker.put(TestUtils.createEntry(entryId))
            .flatMap(result -> {
                final HttpServiceRequest request = new MockRequest.Builder()
                    .pathParameters(List.of(String.valueOf(entryId)))
                    .build();

                // Make sure that the entry is there before we delete it.
                assertNotNull(pdTracker.get(entryId));

                return handler.handle(request, response);
            })
            .ifSuccess(result -> {
                assertEquals(HttpStatus.OK, response.status().orElse(null));
                assertNull(pdTracker.get(entryId));
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldRejectInvalidId() {
        final String invalidEntryId = "InvalidId";

        final HttpServiceRequest request = new MockRequest.Builder()
            .pathParameters(List.of(invalidEntryId))
            .build();

        handler.handle(request, response)
            .ifSuccess(result -> {
                final String expectedErrorMessage = "'" + invalidEntryId +
                    "' is not a valid Plant Description Entry ID.";
                final String actualErrorMessage = ((ErrorMessage) response.getRawBody()).error();
                assertTrue(response.status().isPresent());
                assertEquals(HttpStatus.BAD_REQUEST, response.status().get());
                assertEquals(expectedErrorMessage, actualErrorMessage);
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldRejectNonexistentIds() {
        final int nonExistentId = 392;

        final HttpServiceRequest request = new MockRequest.Builder()
            .pathParameters(List.of(String.valueOf(nonExistentId)))
            .build();

        handler.handle(request, response)
            .ifSuccess(result -> {
                final String expectedErrorMessage = "Plant Description with ID " + nonExistentId + " not found.";
                final String actualErrorMessage = ((ErrorMessage) response.getRawBody()).error();
                assertEquals(HttpStatus.NOT_FOUND, response.status().orElse(null));
                assertEquals(expectedErrorMessage, actualErrorMessage);
            })
            .onFailure(e -> fail());

    }

    @Test
    public void shouldRejectDeletionOfIncludedEntry() {
        final Instant now = Instant.now();
        final int entryIdA = 23;
        final int entryIdB = 24;

        final PlantDescriptionEntryDto entryA = new PlantDescriptionEntryDto.Builder()
            .id(entryIdA)
            .plantDescription("A")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .build();
        final PlantDescriptionEntryDto entryB = new PlantDescriptionEntryDto.Builder()
            .id(entryIdB)
            .plantDescription("B")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .include(List.of(entryIdA))
            .build();

        pdTracker.put(entryA)
            .flatMap(result -> pdTracker.put(entryB))
            .flatMap(result -> {
                final HttpServiceRequest request = new MockRequest.Builder().pathParameters(List.of(String.valueOf(entryIdA)))
                    .build();
                return handler.handle(request, response);
            })
            .ifSuccess(result -> {
                final String expectedErrorMessage = "<Error in include list: Entry '" +
                    entryIdA + "' is required by entry '" + entryIdB + "'.>";
                final String actualErrorMessage = ((ErrorMessage) response.getRawBody()).error();
                assertEquals(HttpStatus.BAD_REQUEST, response.status().orElse(null));
                assertEquals(expectedErrorMessage, actualErrorMessage);
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldHandleBackingStoreFailure() throws PdStoreException {

        final PdStore backingStore = Mockito.mock(FilePdStore.class);
        final PlantDescriptionTracker pdTracker = new PlantDescriptionTracker(backingStore);
        final DeletePlantDescription handler = new DeletePlantDescription(pdTracker);

        final int entryId = 87;
        pdTracker.put(TestUtils.createEntry(entryId))
            .flatMap(result -> {
                final HttpServiceRequest request = new MockRequest.Builder()
                    .pathParameters(List.of(String.valueOf(entryId)))
                    .build();

                doThrow(new PdStoreException("Mocked error")).when(backingStore).remove(anyInt());
                return handler.handle(request, response);
            })
            .ifSuccess(result -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.status().orElse(null)))
            .onFailure(e -> fail());
    }

}