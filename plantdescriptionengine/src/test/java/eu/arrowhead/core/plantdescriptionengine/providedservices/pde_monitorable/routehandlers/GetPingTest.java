package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitorable.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitorable.dto.Ping;
import eu.arrowhead.core.plantdescriptionengine.utils.MockRequest;
import eu.arrowhead.core.plantdescriptionengine.utils.MockServiceResponse;
import org.junit.jupiter.api.Test;
import se.arkalix.net.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class GetPingTest {

    @Test
    public void shouldReturnPing() {
        final GetPing handler = new GetPing();
        final MockRequest request = new MockRequest();
        final MockServiceResponse response = new MockServiceResponse();

        handler.handle(request, response).ifSuccess(result -> {
            assertEquals(HttpStatus.OK, response.status().orElse(null));

            final Ping ping = (Ping) response.getRawBody();
            assertTrue(ping.ping());
        })
        .onFailure(e -> fail());

    }
}
