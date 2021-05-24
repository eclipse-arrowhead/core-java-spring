package eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry;

import eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.dto.SrSystem;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.dto.SrSystemDto;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.dto.SrSystemListDto;
import eu.arrowhead.core.plantdescriptionengine.utils.MockClientResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.client.HttpClient;
import se.arkalix.net.http.client.HttpClientRequest;
import se.arkalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class SystemTrackerTest {

    private HttpClient httpClient;
    private SystemTracker systemTracker;

    @BeforeEach
    public void initEach() {
        httpClient = Mockito.mock(HttpClient.class);
        systemTracker = new SystemTracker(
            httpClient,
            new InetSocketAddress("0.0.0.0", 5000),
            5000
        );
    }

    @Test
    public void shouldThrowWhenNotInitialized() {
        final Exception exception = assertThrows(RuntimeException.class, () -> systemTracker.getSystem("abc"));
        assertEquals("SystemTracker has not been initialized.", exception.getMessage());
    }

    @Test
    public void shouldFindSystemByName() {
        final String systemName = "abc";
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
                final SrSystem system = systemTracker.getSystem(systemName);
                assertEquals(systemId, system.id());
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldFindSystemsByMetadata() {
        final String systemName = "Sys-A";
        final int systemId1 = 92;
        final int systemId2 = 93;
        final Map<String, String> metadata1 = Map.of("abc", "1");
        final Map<String, String> metadata2 = Map.of("xyz", "2");

        final SrSystemDto system1 = new SrSystemDto.Builder()
            .id(systemId1)
            .systemName(systemName)
            .metadata(metadata1)
            .address("0.0.0.0")
            .port(5009)
            .build();

        final SrSystemDto system2 = new SrSystemDto.Builder()
            .id(systemId2)
            .systemName(systemName)
            .metadata(metadata2)
            .address("0.0.0.2")
            .port(5010)
            .build();

        // Create some fake data for the HttpClient to respond with:
        final MockClientResponse response = new MockClientResponse().status(HttpStatus.OK)
            .body(new SrSystemListDto.Builder().count(1)
                .data(system1, system2)
                .build());

        when(httpClient.send(any(InetSocketAddress.class), any(HttpClientRequest.class)))
            .thenReturn(Future.success(response));

        systemTracker.start()
            .ifSuccess(result -> {
                final SrSystem retrievedSystem1 = systemTracker.getSystem(systemName, metadata1);
                final SrSystem retrievedSystem2 = systemTracker.getSystem(systemName, metadata2);
                assertEquals(systemId1, retrievedSystem1.id());
                assertEquals(systemId2, retrievedSystem2.id());
            })
            .onFailure(e -> fail());
    }
}
