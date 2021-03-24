package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor;

import eu.arrowhead.core.plantdescriptionengine.alarms.AlarmManager;
import eu.arrowhead.core.plantdescriptionengine.utils.MockClientResponse;
import eu.arrowhead.core.plantdescriptionengine.utils.RequestMatcher;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.arkalix.description.ProviderDescription;
import se.arkalix.description.ServiceDescription;
import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.descriptor.TransportDescriptor;
import se.arkalix.net.http.HttpMethod;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.client.HttpClient;
import se.arkalix.net.http.client.HttpClientRequest;
import se.arkalix.query.ServiceQuery;
import se.arkalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class PingTaskTest {

    @Test
    public void shouldClearSystemInactive() {

        final String serviceUri = "some.service.uri";
        final String systemName = "System-xyz";

        final HttpClient httpClient = Mockito.mock(HttpClient.class);
        final var alarmManager = new AlarmManager();
        final ServiceQuery serviceQuery = Mockito.mock(ServiceQuery.class);
        final ServiceDescription service = Mockito.mock(ServiceDescription.class);
        Set<ServiceDescription> services = Set.of(service);
        final var resolveResult = Future.success(services);
        final MockClientResponse response = new MockClientResponse();
        response.status(HttpStatus.OK);
        ProviderDescription provider = Mockito.mock(ProviderDescription.class);
        InetSocketAddress address = new InetSocketAddress("1.1.1.1", 8443);
        when(serviceQuery.name("monitorable")).thenReturn(serviceQuery);
        when(serviceQuery.transports(TransportDescriptor.HTTP)).thenReturn(serviceQuery);
        when(serviceQuery.encodings(EncodingDescriptor.JSON)).thenReturn(serviceQuery);
        when(service.provider()).thenReturn(provider);
        when(service.uri()).thenReturn(serviceUri);
        when(provider.name()).thenReturn(systemName);
        when(provider.socketAddress()).thenReturn(address);

        final var expectedRequest = new HttpClientRequest()
            .method(HttpMethod.GET)
            .uri(service.uri() + "/ping")
            .header("accept", "application/json");

        when(httpClient.send(any(InetSocketAddress.class), argThat(new RequestMatcher(expectedRequest))))
            .thenReturn(Future.success(response));
        when(serviceQuery.resolveAll()).thenReturn(resolveResult);

        final var pingTask = new PingTask(serviceQuery, httpClient, alarmManager);

        alarmManager.raiseSystemInactive(systemName);
        assertFalse(alarmManager.getAlarms()
            .get(0)
            .clearedAt()
            .isPresent());
        pingTask.run();
        assertTrue(alarmManager.getAlarms()
            .get(0)
            .clearedAt()
            .isPresent());
    }

    @Test
    public void shouldHandleServiceRequestError() {

        final String systemName = "System-xyz";

        final HttpClient httpClient = Mockito.mock(HttpClient.class);
        final var alarmManager = new AlarmManager();
        final ServiceQuery serviceQuery = Mockito.mock(ServiceQuery.class);
        final ServiceDescription service = Mockito.mock(ServiceDescription.class);
        final var error = new Throwable("Some error");
        final Future<Set<ServiceDescription>> resolveResult = Future.failure(error);
        final MockClientResponse response = new MockClientResponse();
        response.status(HttpStatus.OK);

        final var expectedRequest = new HttpClientRequest()
            .method(HttpMethod.GET)
            .uri(service.uri() + "/ping")
            .header("accept", "application/json");

        when(httpClient.send(any(InetSocketAddress.class), argThat(new RequestMatcher(expectedRequest))))
            .thenReturn(Future.success(response));
        when(serviceQuery.resolveAll()).thenReturn(resolveResult);

        final var pingTask = new PingTask(serviceQuery, httpClient, alarmManager);

        alarmManager.raiseSystemInactive(systemName);
        assertFalse(alarmManager.getAlarms()
            .get(0)
            .clearedAt()
            .isPresent());
        pingTask.run();
        assertFalse(alarmManager.getAlarms()
            .get(0)
            .clearedAt()
            .isPresent());
    }

    @Test
    public void shouldRaiseSystemInactive() {

        final String serviceUri = "some.service.uri";
        final String systemName = "System-xyz";
        InetSocketAddress address = new InetSocketAddress("1.1.1.1", 8443);

        final HttpClient httpClient = Mockito.mock(HttpClient.class);
        final var alarmManager = new AlarmManager();
        final ServiceQuery serviceQuery = Mockito.mock(ServiceQuery.class);
        final ServiceDescription service = Mockito.mock(ServiceDescription.class);
        Set<ServiceDescription> services = Set.of(service);
        final var resolveResult = Future.success(services);
        final MockClientResponse response = new MockClientResponse();
        response.status(HttpStatus.OK);
        ProviderDescription provider = Mockito.mock(ProviderDescription.class);
        when(serviceQuery.name("monitorable")).thenReturn(serviceQuery);
        when(serviceQuery.transports(TransportDescriptor.HTTP)).thenReturn(serviceQuery);
        when(serviceQuery.encodings(EncodingDescriptor.JSON)).thenReturn(serviceQuery);
        when(service.provider()).thenReturn(provider);
        when(service.uri()).thenReturn(serviceUri);
        when(provider.name()).thenReturn(systemName);
        when(provider.socketAddress()).thenReturn(address);
        final var error = new Throwable("Some error");

        final var expectedRequest = new HttpClientRequest()
            .method(HttpMethod.GET)
            .uri(service.uri() + "/ping")
            .header("accept", "application/json");
        when(httpClient.send(eq(address), argThat(new RequestMatcher(expectedRequest))))
            .thenReturn(Future.failure(error));
        when(serviceQuery.resolveAll()).thenReturn(resolveResult);

        final var pingTask = new PingTask(serviceQuery, httpClient, alarmManager);

        assertEquals(0, alarmManager.getAlarms().size());
        pingTask.run();
        assertEquals(1, alarmManager.getAlarms().size());
    }

}
