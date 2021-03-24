package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor;

import eu.arrowhead.core.plantdescriptionengine.MonitorInfo;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.monitorable.dto.InventoryIdBuilder;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.monitorable.dto.SystemDataBuilder;
import eu.arrowhead.core.plantdescriptionengine.utils.MockClientResponse;
import eu.arrowhead.core.plantdescriptionengine.utils.RequestMatcher;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.arkalix.description.ProviderDescription;
import se.arkalix.description.ServiceDescription;
import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.descriptor.TransportDescriptor;
import se.arkalix.dto.json.value.JsonBoolean;
import se.arkalix.dto.json.value.JsonObject;
import se.arkalix.dto.json.value.JsonPair;
import se.arkalix.net.http.HttpMethod;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.client.HttpClient;
import se.arkalix.net.http.client.HttpClientRequest;
import se.arkalix.query.ServiceQuery;
import se.arkalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

public class RetrieveMonitorInfoTaskTest {

    @Test
    public void shouldRetrieveMonitorInfo() {

        final String serviceUri = "some.service.uri";
        final String systemName = "System-xyz";

        final HttpClient httpClient = Mockito.mock(HttpClient.class);
        final var monitorInfo = new MonitorInfo();
        final ServiceQuery serviceQuery = Mockito.mock(ServiceQuery.class);
        final ServiceDescription service = Mockito.mock(ServiceDescription.class);
        Set<ServiceDescription> services = Set.of(service);
        final var resolveResult = Future.success(services);
        String inventoryId = "ABC";
        final MockClientResponse inventoryIdResponse = new MockClientResponse()
            .status(HttpStatus.OK)
            .body(new InventoryIdBuilder()
                .id("ABC")
                .build());

        final JsonObject systemData = new JsonObject(List.of(new JsonPair("a", JsonBoolean.TRUE)));
        final MockClientResponse systemDataResponse = new MockClientResponse()
            .status(HttpStatus.OK)
            .body(new SystemDataBuilder()
                .data(systemData)
                .build());
        ProviderDescription provider = Mockito.mock(ProviderDescription.class);
        InetSocketAddress address = new InetSocketAddress("1.1.1.1", 8443);
        when(serviceQuery.name("monitorable")).thenReturn(serviceQuery);
        when(serviceQuery.transports(TransportDescriptor.HTTP)).thenReturn(serviceQuery);
        when(serviceQuery.encodings(EncodingDescriptor.JSON)).thenReturn(serviceQuery);
        when(service.provider()).thenReturn(provider);
        when(service.uri()).thenReturn(serviceUri);
        when(provider.name()).thenReturn(systemName);
        when(provider.socketAddress()).thenReturn(address);

        final var inventoryIdRequest = new HttpClientRequest()
            .method(HttpMethod.GET)
            .uri(service.uri() + "/inventoryid")
            .header("accept", "application/json");
        final var systemDataRequest = new HttpClientRequest()
            .method(HttpMethod.GET)
            .uri(service.uri() + "/systemdata")
            .header("accept", "application/json");
        when(httpClient.send(any(InetSocketAddress.class), argThat(new RequestMatcher(inventoryIdRequest))))
            .thenReturn(Future.success(inventoryIdResponse));
        when(httpClient.send(any(InetSocketAddress.class), argThat(new RequestMatcher(systemDataRequest))))
            .thenReturn(Future.success(systemDataResponse));
        when(serviceQuery.resolveAll()).thenReturn(resolveResult);

        final var task = new RetrieveMonitorInfoTask(serviceQuery, httpClient, monitorInfo);

        task.run();
        final var systemInfo = monitorInfo.getSystemInfo(systemName, null);

        assertEquals(1, systemInfo.size());
        assertEquals(inventoryId, systemInfo.get(0).inventoryId);
        assertEquals(systemData, systemInfo.get(0).systemData);
    }

    @Test
    public void shouldHandleServiceRequestError() {

        final String systemName = "System-xyz";

        final HttpClient httpClient = Mockito.mock(HttpClient.class);
        final var monitorInfo = new MonitorInfo();
        final ServiceQuery serviceQuery = Mockito.mock(ServiceQuery.class);
        final var error = new Throwable("Some error");
        final Future<Set<ServiceDescription>> resolveResult = Future.failure(error);
        when(serviceQuery.resolveAll()).thenReturn(resolveResult);


        final var task = new RetrieveMonitorInfoTask(serviceQuery, httpClient, monitorInfo);

        task.run();
        final var systemInfo = monitorInfo.getSystemInfo(systemName, null);
        assertEquals(0, systemInfo.size());
    }

    @Test
    public void shouldHandleInventoryIdFetchFailure() {

        final String serviceUri = "some.service.uri";
        final String systemName = "System-xyz";

        final HttpClient httpClient = Mockito.mock(HttpClient.class);
        final var monitorInfo = new MonitorInfo();
        final ServiceQuery serviceQuery = Mockito.mock(ServiceQuery.class);
        final ServiceDescription service = Mockito.mock(ServiceDescription.class);
        Set<ServiceDescription> services = Set.of(service);
        final var resolveResult = Future.success(services);

        final JsonObject systemData = new JsonObject(List.of(new JsonPair("a", JsonBoolean.TRUE)));
        final MockClientResponse systemDataResponse = new MockClientResponse()
            .status(HttpStatus.OK)
            .body(new SystemDataBuilder()
                .data(systemData)
                .build());
        ProviderDescription provider = Mockito.mock(ProviderDescription.class);
        InetSocketAddress address = new InetSocketAddress("1.1.1.1", 8443);
        when(serviceQuery.name("monitorable")).thenReturn(serviceQuery);
        when(serviceQuery.transports(TransportDescriptor.HTTP)).thenReturn(serviceQuery);
        when(serviceQuery.encodings(EncodingDescriptor.JSON)).thenReturn(serviceQuery);
        when(service.provider()).thenReturn(provider);
        when(service.uri()).thenReturn(serviceUri);
        when(provider.name()).thenReturn(systemName);
        when(provider.socketAddress()).thenReturn(address);

        final var inventoryIdRequest = new HttpClientRequest()
            .method(HttpMethod.GET)
            .uri(service.uri() + "/inventoryid")
            .header("accept", "application/json");
        final var systemDataRequest = new HttpClientRequest()
            .method(HttpMethod.GET)
            .uri(service.uri() + "/systemdata")
            .header("accept", "application/json");
        final var error = new Throwable("Some error");
        when(httpClient.send(any(InetSocketAddress.class), argThat(new RequestMatcher(inventoryIdRequest))))
            .thenReturn(Future.failure(error));
        when(httpClient.send(any(InetSocketAddress.class), argThat(new RequestMatcher(systemDataRequest))))
            .thenReturn(Future.success(systemDataResponse));
        when(serviceQuery.resolveAll()).thenReturn(resolveResult);

        final var task = new RetrieveMonitorInfoTask(serviceQuery, httpClient, monitorInfo);

        task.run();
        final var systemInfo = monitorInfo.getSystemInfo(systemName, null);

        assertEquals(1, systemInfo.size());
        assertNull(systemInfo.get(0).inventoryId);
        assertEquals(systemData, systemInfo.get(0).systemData);
    }

    @Test
    public void shouldHandleSystemDataFailure() {

        final String serviceUri = "some.service.uri";
        final String systemName = "System-xyz";

        final HttpClient httpClient = Mockito.mock(HttpClient.class);
        final var monitorInfo = new MonitorInfo();
        final ServiceQuery serviceQuery = Mockito.mock(ServiceQuery.class);
        final ServiceDescription service = Mockito.mock(ServiceDescription.class);
        Set<ServiceDescription> services = Set.of(service);
        final var resolveResult = Future.success(services);
        String inventoryId = "ABC";
        final MockClientResponse inventoryIdResponse = new MockClientResponse()
            .status(HttpStatus.OK)
            .body(new InventoryIdBuilder()
                .id("ABC")
                .build());

        ProviderDescription provider = Mockito.mock(ProviderDescription.class);
        InetSocketAddress address = new InetSocketAddress("1.1.1.1", 8443);
        when(serviceQuery.name("monitorable")).thenReturn(serviceQuery);
        when(serviceQuery.transports(TransportDescriptor.HTTP)).thenReturn(serviceQuery);
        when(serviceQuery.encodings(EncodingDescriptor.JSON)).thenReturn(serviceQuery);
        when(service.provider()).thenReturn(provider);
        when(service.uri()).thenReturn(serviceUri);
        when(provider.name()).thenReturn(systemName);
        when(provider.socketAddress()).thenReturn(address);

        final var inventoryIdRequest = new HttpClientRequest()
            .method(HttpMethod.GET)
            .uri(service.uri() + "/inventoryid")
            .header("accept", "application/json");
        final var systemDataRequest = new HttpClientRequest()
            .method(HttpMethod.GET)
            .uri(service.uri() + "/systemdata")
            .header("accept", "application/json");
        final var error = new Throwable("Some error");
        when(httpClient.send(any(InetSocketAddress.class), argThat(new RequestMatcher(inventoryIdRequest))))
            .thenReturn(Future.success(inventoryIdResponse));
        when(httpClient.send(any(InetSocketAddress.class), argThat(new RequestMatcher(systemDataRequest))))
            .thenReturn(Future.failure(error));
        when(serviceQuery.resolveAll()).thenReturn(resolveResult);

        final var task = new RetrieveMonitorInfoTask(serviceQuery, httpClient, monitorInfo);

        task.run();
        final var systemInfo = monitorInfo.getSystemInfo(systemName, null);

        assertEquals(1, systemInfo.size());
        assertEquals(inventoryId, systemInfo.get(0).inventoryId);
        assertNull(systemInfo.get(0).systemData);
    }

}
