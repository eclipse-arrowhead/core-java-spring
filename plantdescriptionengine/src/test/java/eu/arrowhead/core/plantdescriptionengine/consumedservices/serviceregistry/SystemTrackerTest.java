package eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry;

import eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.dto.SrSystem;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.dto.SrSystemDto;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.dto.SrSystemListDto;
import eu.arrowhead.core.plantdescriptionengine.utils.MockClientResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.client.HttpClient;
import se.arkalix.net.http.client.HttpClientRequest;
import se.arkalix.util.concurrent.Future;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class SystemTrackerTest {

    @Test
    public void shouldThrowWhenNotInitialized() {

        final HttpClient httpClient = new HttpClient.Builder().insecure().build();
        final SystemTracker systemTracker = new SystemTracker(httpClient, new InetSocketAddress("0.0.0.0", 5000));

        final Exception exception = assertThrows(RuntimeException.class, () -> systemTracker.getSystem("System A", null));
        assertEquals("SystemTracker has not been initialized.", exception.getMessage());
    }

    @Test
    public void shouldFindSystemByName() {

        final HttpClient httpClient = Mockito.mock(HttpClient.class);
        final SystemTracker systemTracker = new SystemTracker(httpClient, new InetSocketAddress("0.0.0.0", 5000));
        final String systemName = "Sys-A";
        final int systemId = 92;
        // Create some fake data for the HttpClient to respond with:
        final MockClientResponse response = new MockClientResponse().status(HttpStatus.OK)
            .body(new SrSystemListDto.Builder().count(1)
                .data(new SrSystemDto.Builder()
                    .id(systemId)
                    .systemName(systemName)
                    .address("0.0.0.0")
                    .port(5009)
                    .build())
                .build());

        when(httpClient.send(any(InetSocketAddress.class), any(HttpClientRequest.class)))
            .thenReturn(Future.success(response));

        systemTracker.start()
            .ifSuccess(result -> {
                final SrSystem system = systemTracker.getSystem(systemName, null);
                assertEquals(systemId, system.id());
            })
            .onFailure(Assertions::assertNull);
    }
}
