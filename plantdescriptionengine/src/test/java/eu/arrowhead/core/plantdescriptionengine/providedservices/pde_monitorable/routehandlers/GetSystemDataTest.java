package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitorable.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.Constants;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitorable.dto.SystemData;
import eu.arrowhead.core.plantdescriptionengine.utils.MockRequest;
import eu.arrowhead.core.plantdescriptionengine.utils.MockServiceResponse;
import org.junit.jupiter.api.Test;
import se.arkalix.codec.json.JsonObject;
import se.arkalix.net.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class GetSystemDataTest {

    @Test
    public void shouldReturnNullSystemData() {
        final GetSystemData handler = new GetSystemData();
        final MockRequest request = new MockRequest();
        final MockServiceResponse response = new MockServiceResponse();

        handler.handle(request, response).ifSuccess(result -> {
            assertEquals(HttpStatus.OK, response.status().orElse(null));

            final SystemData systemData = (SystemData) response.getRawBody();
            assertTrue(systemData.data().isPresent());
            final JsonObject json = systemData.data().get();
            assertEquals("{[name: " + Constants.PDE_SYSTEM_NAME + "]}", json.toString());
        })
            .onFailure(e -> fail());

    }
}
