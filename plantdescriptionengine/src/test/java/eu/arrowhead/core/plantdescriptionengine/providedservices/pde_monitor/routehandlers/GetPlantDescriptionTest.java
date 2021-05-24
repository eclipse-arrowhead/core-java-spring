package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.MonitorInfoTracker;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.InMemoryPdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStoreException;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessage;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.MonitorPlantDescriptionEntry;
import eu.arrowhead.core.plantdescriptionengine.utils.MockRequest;
import eu.arrowhead.core.plantdescriptionengine.utils.MockServiceResponse;
import eu.arrowhead.core.plantdescriptionengine.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpServiceRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class GetPlantDescriptionTest {

    private PlantDescriptionTracker pdTracker;
    private GetPlantDescription handler;
    private MockServiceResponse response;
    private MonitorInfoTracker monitorInfoTracker;

    @BeforeEach
    public void initEach() throws PdStoreException {
        monitorInfoTracker = new MonitorInfoTracker();
        pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());
        handler = new GetPlantDescription(monitorInfoTracker, pdTracker);
        response = new MockServiceResponse();
    }

    @Test
    public void shouldRespondWithNotFound() {

        final int nonExistentEntryId = 0;

        final HttpServiceRequest request = new MockRequest.Builder()
            .pathParameters(List.of(String.valueOf(nonExistentEntryId)))
            .build();

        handler.handle(request, response).ifSuccess(result -> {
            final String expectedErrorMessage = "Plant Description with ID " + nonExistentEntryId + " not found.";
            final String actualErrorMessage = ((ErrorMessage) response.getRawBody()).error();
            assertEquals(HttpStatus.NOT_FOUND, response.status().orElse(null));
            assertEquals(expectedErrorMessage, actualErrorMessage);
        }).onFailure(e -> fail());

    }

    @Test
    public void shouldRespondWithStoredEntry() throws PdStoreException {

        final int entryId = 39;

        pdTracker.put(TestUtils.createEntry(entryId));

        final HttpServiceRequest request = new MockRequest.Builder()
            .pathParameters(List.of(String.valueOf(entryId)))
            .build();

        handler.handle(request, response).ifSuccess(result -> {
            final MonitorPlantDescriptionEntry returnedEntry = (MonitorPlantDescriptionEntry) response.getRawBody();
            assertEquals(HttpStatus.OK, response.status().orElse(null));
            assertEquals(returnedEntry.id(), entryId);
        }).onFailure(e -> fail());
    }

    @Test
    public void shouldNotAcceptInvalidId() throws PdStoreException {

        final int entryId = 24;
        final String invalidId = "Invalid";

        pdTracker.put(TestUtils.createEntry(entryId));

        final HttpServiceRequest request = new MockRequest.Builder()
            .pathParameters(List.of(invalidId))
            .build();

        handler.handle(request, response)
            .ifSuccess(result -> assertEquals(HttpStatus.BAD_REQUEST, response.status().orElse(null)))
            .onFailure(e -> fail());
    }

}