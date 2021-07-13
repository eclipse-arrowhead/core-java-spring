package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor;

import eu.arrowhead.core.plantdescriptionengine.ApiConstants;
import eu.arrowhead.core.plantdescriptionengine.MonitorInfo;
import eu.arrowhead.core.plantdescriptionengine.MonitorInfoTracker;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.monitorable.dto.InventoryIdDto;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.monitorable.dto.SystemDataDto;
import eu.arrowhead.core.plantdescriptionengine.utils.MockClientResponse;
import eu.arrowhead.core.plantdescriptionengine.utils.RequestMatcher;
import org.junit.Test;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

public class RetrieveMonitorInfoTaskTest {

    final String serviceUri = "http://some_service_uri";
    final String systemName = "System-xyz";
    final String serviceName = "someservice";

    @Test
    public void shouldRetrieveMonitorInfo() {

        final HttpClient httpClient = Mockito.mock(HttpClient.class);
        final MonitorInfoTracker monitorInfoTracker = new MonitorInfoTracker();
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

        final JsonObject systemData = new JsonObject(new JsonPair("a", JsonBoolean.TRUE));
        final MockClientResponse systemDataResponse = new MockClientResponse()
            .status(HttpStatus.OK)
            .body(new SystemDataDto.Builder()
                .data(systemData)
                .build());
        final SystemRecord provider = Mockito.mock(SystemRecord.class);
        final InetSocketAddress address = new InetSocketAddress("1.1.1.1", 8443);
        when(serviceQuery.name(ApiConstants.MONITORABLE_SERVICE_NAME)).thenReturn(serviceQuery);
        when(service.name()).thenReturn(serviceName);
        when(service.provider()).thenReturn(provider);
        when(service.uri()).thenReturn(serviceUri);
        when(provider.name()).thenReturn(systemName);
        when(provider.socketAddress()).thenReturn(address);

        final HttpClientRequest inventoryIdRequest = new HttpClientRequest()
            .method(HttpMethod.GET)
            .uri(service.uri() + ApiConstants.MONITORABLE_ID_PATH)
            .header(ApiConstants.HEADER_ACCEPT, ApiConstants.APPLICATION_JSON);
        final HttpClientRequest systemDataRequest = new HttpClientRequest()
            .method(HttpMethod.GET)
            .uri(service.uri() + ApiConstants.MONITORABLE_SYSTEM_DATA_PATH)
            .header(ApiConstants.HEADER_ACCEPT, ApiConstants.APPLICATION_JSON);
        when(httpClient.send(any(InetSocketAddress.class), argThat(new RequestMatcher(inventoryIdRequest))))
            .thenReturn(Future.success(inventoryIdResponse));
        when(httpClient.send(any(InetSocketAddress.class), argThat(new RequestMatcher(systemDataRequest))))
            .thenReturn(Future.success(systemDataResponse));
        when(serviceQuery.resolveAll()).thenReturn(resolveResult);

        final RetrieveMonitorInfoTask task = new RetrieveMonitorInfoTask(serviceQuery, httpClient, monitorInfoTracker);

        task.run();
        final List<MonitorInfo> systemInfo = monitorInfoTracker.getSystemInfo(systemName, null);

        assertEquals(1, systemInfo.size());
        assertEquals(inventoryId, systemInfo.get(0).inventoryId);
        assertEquals(systemData, systemInfo.get(0).systemData);
    }

    @Test
    public void shouldHandleServiceRequestError() {

        final String systemName = "System-xyz";

        final HttpClient httpClient = Mockito.mock(HttpClient.class);
        final MonitorInfoTracker monitorInfoTracker = new MonitorInfoTracker();
        final ServiceQuery serviceQuery = Mockito.mock(ServiceQuery.class);
        final Throwable error = new Throwable("Some error");
        final Future<Set<ServiceRecord>> resolveResult = Future.failure(error);
        when(serviceQuery.resolveAll()).thenReturn(resolveResult);


        final RetrieveMonitorInfoTask task = new RetrieveMonitorInfoTask(serviceQuery, httpClient, monitorInfoTracker);

        task.run();
        final List<MonitorInfo> systemInfo = monitorInfoTracker.getSystemInfo(systemName, null);
        assertEquals(0, systemInfo.size());
    }

    @Test
    public void shouldHandleInventoryIdFetchFailure() {
        final HttpClient httpClient = Mockito.mock(HttpClient.class);
        final MonitorInfoTracker monitorInfoTracker = new MonitorInfoTracker();
        final ServiceQuery serviceQuery = Mockito.mock(ServiceQuery.class);
        final ServiceRecord service = Mockito.mock(ServiceRecord.class);
        final Set<ServiceRecord> services = Set.of(service);
        final Future<Set<ServiceRecord>> resolveResult = Future.success(services);

        final JsonObject systemData = new JsonObject(new JsonPair("a", JsonBoolean.TRUE));
        final MockClientResponse systemDataResponse = new MockClientResponse()
            .status(HttpStatus.OK)
            .body(new SystemDataDto.Builder()
                .data(systemData)
                .build());
        final SystemRecord provider = Mockito.mock(SystemRecord.class);
        final InetSocketAddress address = new InetSocketAddress("1.1.1.1", 8443);
        when(serviceQuery.name(ApiConstants.MONITORABLE_SERVICE_NAME)).thenReturn(serviceQuery);
        when(service.name()).thenReturn(serviceName);
        when(service.provider()).thenReturn(provider);
        when(service.uri()).thenReturn(serviceUri);
        when(provider.name()).thenReturn(systemName);
        when(provider.socketAddress()).thenReturn(address);

        final HttpClientRequest inventoryIdRequest = new HttpClientRequest()
            .method(HttpMethod.GET)
            .uri(service.uri() + ApiConstants.MONITORABLE_ID_PATH)
            .header(ApiConstants.HEADER_ACCEPT, ApiConstants.APPLICATION_JSON);
        final HttpClientRequest systemDataRequest = new HttpClientRequest()
            .method(HttpMethod.GET)
            .uri(service.uri() + ApiConstants.MONITORABLE_SYSTEM_DATA_PATH)
            .header(ApiConstants.HEADER_ACCEPT, ApiConstants.APPLICATION_JSON);
        final Throwable error = new Throwable("Some error");
        when(httpClient.send(any(InetSocketAddress.class), argThat(new RequestMatcher(inventoryIdRequest))))
            .thenReturn(Future.failure(error));
        when(httpClient.send(any(InetSocketAddress.class), argThat(new RequestMatcher(systemDataRequest))))
            .thenReturn(Future.success(systemDataResponse));
        when(serviceQuery.resolveAll()).thenReturn(resolveResult);

        final RetrieveMonitorInfoTask task = new RetrieveMonitorInfoTask(serviceQuery, httpClient, monitorInfoTracker);

        task.run();
        final List<MonitorInfo> systemInfo = monitorInfoTracker.getSystemInfo(systemName, null);

        assertEquals(1, systemInfo.size());
        assertNull(systemInfo.get(0).inventoryId);
        assertEquals(systemData, systemInfo.get(0).systemData);
    }

    @Test
    public void shouldHandleSystemDataFailure() {
        final HttpClient httpClient = Mockito.mock(HttpClient.class);
        final MonitorInfoTracker monitorInfoTracker = new MonitorInfoTracker();
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
        when(serviceQuery.name(ApiConstants.MONITORABLE_SERVICE_NAME)).thenReturn(serviceQuery);
        when(service.name()).thenReturn(serviceName);
        when(service.provider()).thenReturn(provider);
        when(service.uri()).thenReturn(serviceUri);
        when(provider.name()).thenReturn(systemName);
        when(provider.socketAddress()).thenReturn(address);

        final HttpClientRequest inventoryIdRequest = new HttpClientRequest()
            .method(HttpMethod.GET)
            .uri(service.uri() + ApiConstants.MONITORABLE_ID_PATH)
            .header(ApiConstants.HEADER_ACCEPT, ApiConstants.APPLICATION_JSON);
        final HttpClientRequest systemDataRequest = new HttpClientRequest()
            .method(HttpMethod.GET)
            .uri(service.uri() + ApiConstants.MONITORABLE_SYSTEM_DATA_PATH)
            .header(ApiConstants.HEADER_ACCEPT, ApiConstants.APPLICATION_JSON);
        final Throwable error = new Throwable("Some error");
        when(httpClient.send(any(InetSocketAddress.class), argThat(new RequestMatcher(inventoryIdRequest))))
            .thenReturn(Future.success(inventoryIdResponse));
        when(httpClient.send(any(InetSocketAddress.class), argThat(new RequestMatcher(systemDataRequest))))
            .thenReturn(Future.failure(error));
        when(serviceQuery.resolveAll()).thenReturn(resolveResult);

        final RetrieveMonitorInfoTask task = new RetrieveMonitorInfoTask(serviceQuery, httpClient, monitorInfoTracker);

        task.run();
        final List<MonitorInfo> systemInfo = monitorInfoTracker.getSystemInfo(systemName, null);

        assertEquals(1, systemInfo.size());
        assertEquals(inventoryId, systemInfo.get(0).inventoryId);
        assertNull(systemInfo.get(0).systemData);
    }

}
