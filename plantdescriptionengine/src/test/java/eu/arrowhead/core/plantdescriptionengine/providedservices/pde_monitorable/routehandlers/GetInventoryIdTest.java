package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitorable.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitorable.dto.InventoryId;
import eu.arrowhead.core.plantdescriptionengine.utils.MockRequest;
import eu.arrowhead.core.plantdescriptionengine.utils.MockServiceResponse;
import org.junit.Test;
import se.arkalix.net.http.HttpStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GetInventoryIdTest {

    @Test
    public void shouldReturnNullInventoryId() {
        final GetInventoryId handler = new GetInventoryId();
        final MockRequest request = new MockRequest();
        final MockServiceResponse response = new MockServiceResponse();

        handler.handle(request, response).ifSuccess(result -> {
            assertEquals(HttpStatus.OK, response.status().orElse(null));

            final InventoryId inventoryId = (InventoryId) response.getRawBody();
            assertTrue(inventoryId.id().isEmpty());
        })
            .onFailure(e -> fail());
    }
}
