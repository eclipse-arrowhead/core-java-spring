package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.InMemoryPdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStoreException;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessage;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntry;
import eu.arrowhead.core.plantdescriptionengine.utils.MockRequest;
import eu.arrowhead.core.plantdescriptionengine.utils.MockServiceResponse;
import eu.arrowhead.core.plantdescriptionengine.utils.TestUtils;
import org.junit.Before;
import org.junit.Test;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpServiceRequest;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class GetPlantDescriptionTest {

    private PlantDescriptionTracker pdTracker;
    private GetPlantDescription handler;
    private MockServiceResponse response;

    @Before
    public void initEach() throws PdStoreException {
        pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());
        handler = new GetPlantDescription(pdTracker);
        response = new MockServiceResponse();
    }

    @Test
    public void shouldRespondWithNotFound() {

        final int nonExistentEntryId = 0;

        final HttpServiceRequest request = new MockRequest.Builder()
            .pathParameters(List.of(String.valueOf(nonExistentEntryId)))
            .build();

        handler.handle(request, response)
            .ifSuccess(result -> {
                assertEquals(HttpStatus.NOT_FOUND, response.status().orElse(null));
                final String expectedErrorMessage = "Plant Description with ID " + nonExistentEntryId + " not found.";
                final String actualErrorMessage = ((ErrorMessage) response.getRawBody()).error();
                assertEquals(expectedErrorMessage, actualErrorMessage);
            })
            .onFailure(e -> fail());

    }

    @Test
    public void shouldRespondWithStoredEntry() {

        final int entryId = 39;
        final HttpServiceRequest request = new MockRequest.Builder()
            .pathParameters(List.of(String.valueOf(entryId)))
            .build();

        pdTracker.put(TestUtils.createEntry(entryId))
            .flatMap(result -> handler.handle(request, response))
            .ifSuccess(result -> {
                assertEquals(HttpStatus.OK, response.status().orElse(null));
                final PlantDescriptionEntry returnedEntry = (PlantDescriptionEntry) response.getRawBody();
                assertEquals(returnedEntry
                    .id(), entryId, 0); // TODO: Add 'equals' method to entries and do a proper comparison?
            })
            .onFailure(e -> {
                e.printStackTrace();
                fail();
            });
    }

    @Test
    public void shouldNotAcceptInvalidId() {

        final int entryId = 24;
        final String invalidId = "Invalid";
        final HttpServiceRequest request = new MockRequest.Builder()
            .pathParameters(List.of(invalidId))
            .build();

        pdTracker.put(TestUtils.createEntry(entryId))
            .flatMap(result -> handler.handle(request, response))
            .ifSuccess(result -> assertEquals(
                HttpStatus.BAD_REQUEST, response.status().orElse(null)
            ))
            .onFailure(e -> fail());
    }

}