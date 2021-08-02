package eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry;

import eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.dto.SrSystem;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.dto.SrSystemDto;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.dto.SrSystemListDto;
import eu.arrowhead.core.plantdescriptionengine.utils.MockClientResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.client.HttpClient;
import se.arkalix.net.http.client.HttpClientRequest;
import se.arkalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class SystemTrackerTest {

    final String systemNameA = "abc";
    final String systemNameB = "def";
    final int systemIdA = 92;
    final int systemIdB = 93;
    final int systemIdC = 94;
    final int systemIdD = 95;
    final Map<String, String> systemMetadataA = Map.of("abc", "1");
    final Map<String, String> systemMetadataB = Map.of("xyz", "2");
    final Map<String, String> systemMetadataC = Map.of("xyz", "3");

    final SrSystemDto systemA = new SrSystemDto.Builder()
        .id(systemIdA)
        .systemName(systemNameA)
        .metadata(systemMetadataA)
        .address("0.0.0.0")
        .port(5008)
        .build();

    final SrSystemDto systemB = new SrSystemDto.Builder()
        .id(systemIdB)
        .systemName(systemNameB)
        .metadata(systemMetadataB)
        .address("0.0.0.0")
        .port(5009)
        .build();

    final SrSystemDto systemC = new SrSystemDto.Builder()
        .id(systemIdC)
        .systemName(systemNameA)
        .address("0.0.0.0")
        .port(5009)
        .build();

    final SrSystemDto systemD = new SrSystemDto.Builder()
        .id(systemIdD)
        .systemName(systemNameA)
        .metadata(systemMetadataA)
        .address("0.0.0.1")
        .port(5009)
        .build();

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private HttpClient httpClient;
    private SystemTracker systemTracker;

    @Before
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
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("SystemTracker has not been initialized.");
        systemTracker.getSystem("abc");
    }

    @Test
    public void shouldFindSystemByName() {
        // Create some fake data for the HttpClient to respond with:
        final MockClientResponse response = new MockClientResponse().status(HttpStatus.OK)
            .body(new SrSystemListDto.Builder().count(1)
                .data(systemA)
                .build());

        when(httpClient.send(
            any(InetSocketAddress.class),
            any(HttpClientRequest.class)
        ))
        .thenReturn(Future.success(response));

        systemTracker.start()
            .ifSuccess(result -> {
                final SrSystem system = systemTracker.getSystem(systemNameA);
                assertEquals(systemIdA, (int) system.id());
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldFindSystemsByMetadata() {

        // Create some fake data for the HttpClient to respond with:
        final MockClientResponse response = new MockClientResponse().status(HttpStatus.OK)
            .body(new SrSystemListDto.Builder().count(1)
                .data(systemA, systemC)
                .build());

        when(httpClient.send(any(InetSocketAddress.class), any(HttpClientRequest.class)))
            .thenReturn(Future.success(response));

        systemTracker.start()
            .ifSuccess(result -> {
                final SrSystem retrievedSystem1 = systemTracker.getSystem(systemNameA, systemMetadataA);
                final SrSystem retrievedSystem2 = systemTracker.getSystem(systemNameA, systemMetadataC);
                assertEquals(systemIdA, (int) retrievedSystem1.id());
                assertEquals(systemIdC, (int) retrievedSystem2.id());
            })
            .onFailure(e -> {});
    }

    @Test
    public void shouldReturnSystems() {
        // Create some fake data for the HttpClient to respond with:
        final MockClientResponse response = new MockClientResponse().status(HttpStatus.OK)
            .body(new SrSystemListDto.Builder().count(1)
                .data(systemA, systemB)
                .build());

        when(httpClient.send(any(InetSocketAddress.class), any(HttpClientRequest.class)))
            .thenReturn(Future.success(response));

        systemTracker.start()
            .ifSuccess(result -> {
                final List<SrSystem> systems = systemTracker.getSystems();
                assertEquals(2, systems.size());
            })
            .onFailure(e -> fail());
    }


    @Test
    public void shouldThrowWhenMultipleSystemsMatch() {
        // Create some fake data for the HttpClient to respond with:
        final MockClientResponse response = new MockClientResponse().status(HttpStatus.OK)
            .body(new SrSystemListDto.Builder().count(1)
                .data(systemA, systemB, systemC, systemD)
                .build());

        when(httpClient.send(any(InetSocketAddress.class), any(HttpClientRequest.class)))
            .thenReturn(Future.success(response));

        systemTracker.start()
            .map(result -> {
                systemTracker.getSystem(systemNameA, systemMetadataA);
                return result;
            })
            .ifSuccess(result -> fail())
            .onFailure(e -> {
                assertEquals("More than one system matches the given arguments.", e.getMessage());
            });
    }

    @Test
    public void shouldNotifyListenersOnAdd() {
        Listener listener = new Listener();
        systemTracker.addListener(listener);

        List<SrSystem> oldSystems = List.of(systemA);
        List<SrSystem> newSystems = List.of(systemA, systemB);

        systemTracker.notifyListeners(oldSystems, newSystems);
        assertEquals(listener.numAdded, 1);
    }

    @Test
    public void shouldNotifyListenersOnRemoved() {
        Listener listener = new Listener();
        systemTracker.addListener(listener);

        List<SrSystem> oldSystems = List.of(systemA, systemB, systemC, systemD);
        List<SrSystem> newSystems = List.of(systemA, systemD);

        systemTracker.notifyListeners(oldSystems, newSystems);
        assertEquals(listener.numRemoved, 2);
    }

    static final class Listener implements SystemUpdateListener {

        int numAdded;
        int numRemoved;

        @Override
        public void onSystemAdded(SrSystem system) {
            numAdded++;
            
        }

        @Override
        public void onSystemRemoved(SrSystem system) {
            numRemoved++;
        }
    }

}
