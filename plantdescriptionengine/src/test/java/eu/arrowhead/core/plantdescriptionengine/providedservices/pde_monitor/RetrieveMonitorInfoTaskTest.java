package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor;

import eu.arrowhead.core.plantdescriptionengine.MonitorInfo;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.monitorable.dto.InventoryIdDto;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.monitorable.dto.SystemDataDto;
import eu.arrowhead.core.plantdescriptionengine.utils.MockClientResponse;
import eu.arrowhead.core.plantdescriptionengine.utils.RequestMatcher;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import se.arkalix.ServiceRecord;
import se.arkalix.SystemRecord;
import se.arkalix.codec.json.JsonBoolean;
import se.arkalix.codec.json.JsonObject;
import se.arkalix.codec.json.JsonPair;
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

        final String serviceUri = "http://some_service_uri";
        final String systemName = "System-xyz";

        final HttpClient httpClient = Mockito.mock(HttpClient.class);
        final MonitorInfo monitorInfo = new MonitorInfo();
        final ServiceQuery serviceQuery = Mockito.mock(ServiceQuery.class);
        final ServiceRecord service = Mockito.mock(ServiceRecord.class);
        final Set<ServiceRecord> services = Set.of(service);
        final Future<Set<ServiceRecord>> resolveResult = Future.success(services);
        final String inventoryId = "ABC";
        final MockClientResponse inventoryIdResponse = new MockClientResponse()
            .status(HttpStatus.OK)
            .body(new InventoryIdDto.Builder()
                .id("ABC")
                .build());

        final JsonObject systemData = new JsonObject(List.of(new JsonPair("a", JsonBoolean.TRUE)));
        final MockClientResponse systemDataResponse = new MockClientResponse()
            .status(HttpStatus.OK)
            .body(new SystemDataDto.Builder()
                .data(systemData)
                .build());
        final SystemRecord provider = Mockito.mock(SystemRecord.class);
        final InetSocketAddress address = new InetSocketAddress("1.1.1.1", 8443);
        when(serviceQuery.name("monitorable")).thenReturn(serviceQuery);
        // TODO: The lines below do not work with ar:kalix 0.6
        // when(serviceQuery.transports(TransportDescriptor.HTTP)).thenReturn(serviceQuery);
        // when(serviceQuery.encodings(EncodingDescriptor.JSON)).thenReturn(serviceQuery);
        when(service.provider()).thenReturn(provider);
        when(service.uri()).thenReturn(serviceUri);
        when(provider.name()).thenReturn(systemName);
        when(provider.socketAddress()).thenReturn(address);

        final HttpClientRequest inventoryIdRequest = new HttpClientRequest()
            .method(HttpMethod.GET)
            .uri(service.uri() + "/inventoryid")
            .header("accept", "application/json");
        final HttpClientRequest systemDataRequest = new HttpClientRequest()
            .method(HttpMethod.GET)
            .uri(service.uri() + "/systemdata")
            .header("accept", "application/json");
        when(httpClient.send(any(InetSocketAddress.class), argThat(new RequestMatcher(inventoryIdRequest))))
            .thenReturn(Future.success(inventoryIdResponse));
        when(httpClient.send(any(InetSocketAddress.class), argThat(new RequestMatcher(systemDataRequest))))
            .thenReturn(Future.success(systemDataResponse));
        when(serviceQuery.resolveAll()).thenReturn(resolveResult);

        final RetrieveMonitorInfoTask task = new RetrieveMonitorInfoTask(serviceQuery, httpClient, monitorInfo);

        task.run();
        final List<MonitorInfo.Bundle> systemInfo = monitorInfo.getSystemInfo(systemName, null);

        assertEquals(1, systemInfo.size());
        assertEquals(inventoryId, systemInfo.get(0).inventoryId);
        assertEquals(systemData, systemInfo.get(0).systemData);
    }

    @Test
    public void shouldHandleServiceRequestError() {

        final String systemName = "System-xyz";

        final HttpClient httpClient = Mockito.mock(HttpClient.class);
        final MonitorInfo monitorInfo = new MonitorInfo();
        final ServiceQuery serviceQuery = Mockito.mock(ServiceQuery.class);
        final Throwable error = new Throwable("Some error");
        final Future<Set<ServiceRecord>> resolveResult = Future.failure(error);
        when(serviceQuery.resolveAll()).thenReturn(resolveResult);


        final RetrieveMonitorInfoTask task = new RetrieveMonitorInfoTask(serviceQuery, httpClient, monitorInfo);

        task.run();
        final List<MonitorInfo.Bundle> systemInfo = monitorInfo.getSystemInfo(systemName, null);
        assertEquals(0, systemInfo.size());
    }

    @Test
    public void shouldHandleInventoryIdFetchFailure() {

        final String serviceUri = "http://some-service-uri";
        final String systemName = "System-xyz";

        final HttpClient httpClient = Mockito.mock(HttpClient.class);
        final MonitorInfo monitorInfo = new MonitorInfo();
        final ServiceQuery serviceQuery = Mockito.mock(ServiceQuery.class);
        final ServiceRecord service = Mockito.mock(ServiceRecord.class);
        final Set<ServiceRecord> services = Set.of(service);
        final Future<Set<ServiceRecord>> resolveResult = Future.success(services);

        final JsonObject systemData = new JsonObject(List.of(new JsonPair("a", JsonBoolean.TRUE)));
        final MockClientResponse systemDataResponse = new MockClientResponse()
            .status(HttpStatus.OK)
            .body(new SystemDataDto.Builder()
                .data(systemData)
                .build());
        final SystemRecord provider = Mockito.mock(SystemRecord.class);
        final InetSocketAddress address = new InetSocketAddress("1.1.1.1", 8443);
        when(serviceQuery.name("monitorable")).thenReturn(serviceQuery);
        // TODO: The lines below do not work with ar:kalix 0.6
        // when(serviceQuery.transports(TransportDescriptor.HTTP)).thenReturn(serviceQuery);
        // when(serviceQuery.encodings(EncodingDescriptor.JSON)).thenReturn(serviceQuery);
        when(service.provider()).thenReturn(provider);
        when(service.uri()).thenReturn(serviceUri);
        when(provider.name()).thenReturn(systemName);
        when(provider.socketAddress()).thenReturn(address);

        final HttpClientRequest inventoryIdRequest = new HttpClientRequest()
            .method(HttpMethod.GET)
            .uri(service.uri() + "/inventoryid")
            .header("accept", "application/json");
        final HttpClientRequest systemDataRequest = new HttpClientRequest()
            .method(HttpMethod.GET)
            .uri(service.uri() + "/systemdata")
            .header("accept", "application/json");
        final Throwable error = new Throwable("Some error");
        when(httpClient.send(any(InetSocketAddress.class), argThat(new RequestMatcher(inventoryIdRequest))))
            .thenReturn(Future.failure(error));
        when(httpClient.send(any(InetSocketAddress.class), argThat(new RequestMatcher(systemDataRequest))))
            .thenReturn(Future.success(systemDataResponse));
        when(serviceQuery.resolveAll()).thenReturn(resolveResult);

        final RetrieveMonitorInfoTask task = new RetrieveMonitorInfoTask(serviceQuery, httpClient, monitorInfo);

        task.run();
        final List<MonitorInfo.Bundle> systemInfo = monitorInfo.getSystemInfo(systemName, null);

        assertEquals(1, systemInfo.size());
        assertNull(systemInfo.get(0).inventoryId);
        assertEquals(systemData, systemInfo.get(0).systemData);
    }

    @Test
    public void shouldHandleSystemDataFailure() {

        final String serviceUri = "http://some-service-uri";
        final String systemName = "System-xyz";

        final HttpClient httpClient = Mockito.mock(HttpClient.class);
        final MonitorInfo monitorInfo = new MonitorInfo();
        final ServiceQuery serviceQuery = Mockito.mock(ServiceQuery.class);
        final ServiceRecord service = Mockito.mock(ServiceRecord.class);
        final Set<ServiceRecord> services = Set.of(service);
        final Future<Set<ServiceRecord>> resolveResult = Future.success(services);
        final String inventoryId = "ABC";
        final MockClientResponse inventoryIdResponse = new MockClientResponse()
            .status(HttpStatus.OK)
            .body(new InventoryIdDto.Builder()
                .id("ABC")
                .build());

        final SystemRecord provider = Mockito.mock(SystemRecord.class);
        final InetSocketAddress address = new InetSocketAddress("1.1.1.1", 8443);
        when(serviceQuery.name("monitorable")).thenReturn(serviceQuery);
        // TODO: The lines below do not work with ar:kalix 0.6
        // when(serviceQuery.transports(TransportDescriptor.HTTP)).thenReturn(serviceQuery);
        // when(serviceQuery.encodings(EncodingDescriptor.JSON)).thenReturn(serviceQuery);
        when(service.provider()).thenReturn(provider);
        when(service.uri()).thenReturn(serviceUri);
        when(provider.name()).thenReturn(systemName);
        when(provider.socketAddress()).thenReturn(address);

        final HttpClientRequest inventoryIdRequest = new HttpClientRequest()
            .method(HttpMethod.GET)
            .uri(service.uri() + "/inventoryid")
            .header("accept", "application/json");
        final HttpClientRequest systemDataRequest = new HttpClientRequest()
            .method(HttpMethod.GET)
            .uri(service.uri() + "/systemdata")
            .header("accept", "application/json");
        final Throwable error = new Throwable("Some error");
        when(httpClient.send(any(InetSocketAddress.class), argThat(new RequestMatcher(inventoryIdRequest))))
            .thenReturn(Future.success(inventoryIdResponse));
        when(httpClient.send(any(InetSocketAddress.class), argThat(new RequestMatcher(systemDataRequest))))
            .thenReturn(Future.failure(error));
        when(serviceQuery.resolveAll()).thenReturn(resolveResult);

        final RetrieveMonitorInfoTask task = new RetrieveMonitorInfoTask(serviceQuery, httpClient, monitorInfo);

        task.run();
        final List<MonitorInfo.Bundle> systemInfo = monitorInfo.getSystemInfo(systemName, null);

        assertEquals(1, systemInfo.size());
        assertEquals(inventoryId, systemInfo.get(0).inventoryId);
        assertNull(systemInfo.get(0).systemData);
    }

}
