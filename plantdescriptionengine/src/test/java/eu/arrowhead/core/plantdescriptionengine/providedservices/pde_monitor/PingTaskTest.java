package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor;

import eu.arrowhead.core.plantdescriptionengine.ApiConstants;
import eu.arrowhead.core.plantdescriptionengine.alarms.Alarm;
import eu.arrowhead.core.plantdescriptionengine.alarms.AlarmCause;
import eu.arrowhead.core.plantdescriptionengine.alarms.AlarmManager;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.InMemoryPdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStoreException;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.ConnectionDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PdeSystem;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PdeSystemDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PortDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.SystemPortDto;
import eu.arrowhead.core.plantdescriptionengine.utils.MockClientResponse;
import eu.arrowhead.core.plantdescriptionengine.utils.RequestMatcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.arkalix.ServiceRecord;
import se.arkalix.SystemRecord;
import se.arkalix.net.http.HttpMethod;
import se.arkalix.net.http.client.HttpClient;
import se.arkalix.net.http.client.HttpClientRequest;
import se.arkalix.query.ServiceQuery;
import se.arkalix.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class PingTaskTest {

    final String systemName = "xyz";
    final String serviceUri = "http://some-service-uri";
    final InetSocketAddress address = new InetSocketAddress("1.1.1.1", 8443);

    private HttpClient httpClient;
    private AlarmManager alarmManager;
    private PlantDescriptionTracker pdTracker;
    private ServiceQuery serviceQuery;
    private MockClientResponse response;
    private PingTask pingTask;

    @Before
    public void initEach() throws PdStoreException {
        httpClient = Mockito.mock(HttpClient.class);
        alarmManager = new AlarmManager();
        pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());
        serviceQuery = Mockito.mock(ServiceQuery.class);
        response = new MockClientResponse();
        pingTask = new PingTask(serviceQuery, httpClient, alarmManager, pdTracker);
    }

    @Test
    public void shouldClearSystemInactive() {

        final ServiceRecord service = Mockito.mock(ServiceRecord.class);
        final Set<ServiceRecord> services = Set.of(service);
        final Future<Set<ServiceRecord>> resolveResult = Future.success(services);
        final SystemRecord provider = Mockito.mock(SystemRecord.class);

        when(serviceQuery.name(ApiConstants.MONITORABLE_SERVICE_NAME)).thenReturn(serviceQuery);
        when(service.provider()).thenReturn(provider);
        when(service.uri()).thenReturn(serviceUri);
        when(provider.name()).thenReturn(systemName);
        when(provider.socketAddress()).thenReturn(address);

        final HttpClientRequest expectedRequest = new HttpClientRequest()
            .method(HttpMethod.GET)
            .uri(service.uri() + ApiConstants.MONITORABLE_PING_PATH)
            .header(ApiConstants.HEADER_ACCEPT, ApiConstants.APPLICATION_JSON);

        when(httpClient.send(any(InetSocketAddress.class), argThat(new RequestMatcher(expectedRequest))))
            .thenReturn(Future.success(response));
        when(serviceQuery.resolveAll()).thenReturn(resolveResult);

        alarmManager.raiseNoPingResponse(systemName);
        pingTask.run();
        assertTrue(alarmManager.getAlarms()
            .get(0)
            .clearedAt()
            .isPresent());
    }

    @Test
    public void shouldNotClearAlarmOnError() {

        final Throwable error = new Throwable("Some error");
        final Future<Set<ServiceRecord>> resolveResult = Future.failure(error);

        when(serviceQuery.resolveAll()).thenReturn(resolveResult);

        alarmManager.raiseNoPingResponse(systemName);
        pingTask.run();
        assertFalse(alarmManager.getAlarms()
            .get(0)
            .clearedAt()
            .isPresent());
    }

    @Test
    public void shouldRaiseSystemInactive() {

        final ServiceRecord service = Mockito.mock(ServiceRecord.class);
        final Set<ServiceRecord> services = Set.of(service);
        final Future<Set<ServiceRecord>> resolveResult = Future.success(services);
        final SystemRecord provider = Mockito.mock(SystemRecord.class);
        final Throwable error = new Throwable("Some error");

        when(serviceQuery.name(ApiConstants.MONITORABLE_SERVICE_NAME)).thenReturn(serviceQuery);
        when(service.provider()).thenReturn(provider);
        when(service.uri()).thenReturn(serviceUri);
        when(provider.name()).thenReturn(systemName);
        when(provider.socketAddress()).thenReturn(address);

        final HttpClientRequest expectedRequest = new HttpClientRequest()
            .method(HttpMethod.GET)
            .uri(service.uri() + ApiConstants.MONITORABLE_PING_PATH)
            .header(ApiConstants.HEADER_ACCEPT, ApiConstants.APPLICATION_JSON);

        when(httpClient.send(eq(address), argThat(new RequestMatcher(expectedRequest))))
            .thenReturn(Future.failure(error));
        when(serviceQuery.resolveAll()).thenReturn(resolveResult);

        pingTask.run();
        assertEquals(1, alarmManager.getActiveAlarmData(AlarmCause.NO_PING_RESPONSE).size());
    }

    @Test
    public void shouldRaiseSystemNotMonitorable() {

        final Future<Set<ServiceRecord>> resolveResult = Future.success(Collections.emptySet());
        final SystemRecord provider = Mockito.mock(SystemRecord.class);
        final PlantDescriptionEntryDto entryWithMonitoredSystem = getEntryWithMonitoredSystem();
        pdTracker.put(entryWithMonitoredSystem)
            .ifSuccess(result -> {
                when(serviceQuery.name(ApiConstants.MONITORABLE_SERVICE_NAME)).thenReturn(serviceQuery);
                when(provider.name()).thenReturn(systemName);
                when(provider.socketAddress()).thenReturn(address);
                when(serviceQuery.resolveAll()).thenReturn(resolveResult);
                pingTask.run();
                assertEquals(1, alarmManager.getActiveAlarmData(AlarmCause.NOT_MONITORABLE).size());
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldClearSystemNotMonitorable() {

        final ServiceRecord service = Mockito.mock(ServiceRecord.class);
        final Set<ServiceRecord> services = Set.of(service);
        final Future<Set<ServiceRecord>> resolveResult = Future.success(services);
        final SystemRecord provider = Mockito.mock(SystemRecord.class);
        final PlantDescriptionEntryDto entryWithMonitoredSystem = getEntryWithMonitoredSystem();
        final PdeSystem monitoredSystem = entryWithMonitoredSystem.systems().get(1);

        when(serviceQuery.name(ApiConstants.MONITORABLE_SERVICE_NAME)).thenReturn(serviceQuery);
        when(service.provider()).thenReturn(provider);
        when(service.uri()).thenReturn(serviceUri);
        when(provider.name()).thenReturn(systemName);
        when(provider.socketAddress()).thenReturn(address);

        final HttpClientRequest expectedRequest = new HttpClientRequest()
            .method(HttpMethod.GET)
            .uri(service.uri() + ApiConstants.MONITORABLE_PING_PATH)
            .header(ApiConstants.HEADER_ACCEPT, ApiConstants.APPLICATION_JSON);

        when(httpClient.send(any(InetSocketAddress.class), argThat(new RequestMatcher(expectedRequest))))
            .thenReturn(Future.success(response));
        when(serviceQuery.resolveAll()).thenReturn(resolveResult);

        pdTracker.put(entryWithMonitoredSystem)
            .ifSuccess(result -> {
                alarmManager.raise(Alarm.createSystemNotMonitorableAlarm(
                    monitoredSystem.systemId(),
                    monitoredSystem.systemName().orElse(null),
                    monitoredSystem.metadata()
                ));

                pingTask.run();
                assertTrue(alarmManager.getAlarms()
                    .get(0)
                    .clearedAt()
                    .isPresent()
                );
            })
            .onFailure(e -> fail());
    }


    private PlantDescriptionEntryDto getEntryWithMonitoredSystem() {
        final Instant now = Instant.now();
        final String portName = "monitorable";
        final String pdeId = "pde";
        final String monitoredId = "xyz";
        final PortDto pdePort = new PortDto.Builder()
            .consumer(true)
            .serviceDefinition(ApiConstants.MONITORABLE_SERVICE_NAME)
            .portName(portName)
            .build();
        final PortDto monitoredPort = new PortDto.Builder()
            .consumer(false)
            .serviceDefinition(ApiConstants.MONITORABLE_SERVICE_NAME)
            .portName(portName)
            .build();
        final PdeSystemDto pdeSystem = new PdeSystemDto.Builder()
            .systemId(pdeId)
            .systemName(ApiConstants.PDE_SYSTEM_NAME)
            .ports(List.of(pdePort))
            .build();
        final PdeSystemDto monitoredSystem = new PdeSystemDto.Builder()
            .systemId(monitoredId)
            .systemName(systemName)
            .ports(List.of(monitoredPort))
            .build();
        final ConnectionDto connection = new ConnectionDto.Builder()
            .consumer(new SystemPortDto.Builder()
                .systemId(pdeId)
                .portName(portName)
                .build()
            )
            .producer(new SystemPortDto.Builder()
                .systemId(monitoredId)
                .portName(portName)
                .build()
            )
            .build();
        return new PlantDescriptionEntryDto.Builder()
            .id(1)
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(List.of(pdeSystem, monitoredSystem))
            .createdAt(now)
            .updatedAt(now)
            .connections(List.of(connection))
            .build();
    }

}
