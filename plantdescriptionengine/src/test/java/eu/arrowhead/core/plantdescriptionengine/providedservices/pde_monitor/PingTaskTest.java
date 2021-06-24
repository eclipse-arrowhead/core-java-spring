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
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarmDto;
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
import java.util.Map;
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

    final Instant now = Instant.now();
    final String systemNameA = "sysa";
    final String systemNameB = "sysb";
    final String systemNameC = "sysc";
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
        when(provider.name()).thenReturn(systemNameA);
        when(provider.socketAddress()).thenReturn(address);

        final HttpClientRequest expectedRequest = new HttpClientRequest()
            .method(HttpMethod.GET)
            .uri(service.uri() + ApiConstants.MONITORABLE_PING_PATH)
            .header(ApiConstants.HEADER_ACCEPT, ApiConstants.APPLICATION_JSON);

        when(httpClient.send(any(InetSocketAddress.class), argThat(new RequestMatcher(expectedRequest))))
            .thenReturn(Future.success(response));
        when(serviceQuery.resolveAll()).thenReturn(resolveResult);

        alarmManager.raiseNoPingResponse(systemNameA);
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

        alarmManager.raiseNoPingResponse(systemNameA);
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
        when(provider.name()).thenReturn(systemNameA);
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
        final PlantDescriptionEntryDto entryWithMonitoredSystems = getEntryWithMonitoredSystems();
        pdTracker.put(entryWithMonitoredSystems)
            .ifSuccess(result -> {
                when(serviceQuery.name(ApiConstants.MONITORABLE_SERVICE_NAME)).thenReturn(serviceQuery);
                when(provider.name()).thenReturn(systemNameA);
                when(provider.socketAddress()).thenReturn(address);
                when(serviceQuery.resolveAll()).thenReturn(resolveResult);
                pingTask.run();
                assertEquals(3, alarmManager.getActiveAlarmData(AlarmCause.NOT_MONITORABLE).size());
            })
            .onFailure(e -> fail());
    }

    /**
     * A system that is not the PDE is consuming another's "monitorable"
     * service, but the PDE is not. No alarm should be raised.
     */
    @Test
    public void shouldNotRaiseAlarmA() {
        final Future<Set<ServiceRecord>> resolveResult = Future.success(Collections.emptySet());
        final PlantDescriptionEntryDto pdEntry = getEntryWitNonPdeSystemMonitoring();
        pdTracker.put(pdEntry)
            .ifSuccess(result -> {
                when(serviceQuery.name(ApiConstants.MONITORABLE_SERVICE_NAME)).thenReturn(serviceQuery);
                when(serviceQuery.resolveAll()).thenReturn(resolveResult);
                pingTask.run();
                assertEquals(0, alarmManager.getActiveAlarmData(AlarmCause.NOT_MONITORABLE).size());
            })
            .onFailure(e -> fail());
    }

    /**
     * A service is providing a service to the PDE, and has a "monitorable" port
     * (which is not connected to the PDE). No alarm should be raised.
     */
    @Test
    public void shouldNotRaiseAlarmB() {

        final String monitorablePortName = "monitorable";
        final String pdeId = "pde";
        final String systemId = "sysC";
        final String unrelatedService = "someservice";

        final PortDto monitorableConsumerPort = new PortDto.Builder()
            .consumer(true)
            .serviceDefinition(ApiConstants.MONITORABLE_SERVICE_NAME)
            .portName(monitorablePortName)
            .build();
        final PortDto monitorableProducerPort = new PortDto.Builder()
            .consumer(false)
            .serviceDefinition(ApiConstants.MONITORABLE_SERVICE_NAME)
            .portName(monitorablePortName)
            .build();
        final PortDto unrelatedConsumerPort = new PortDto.Builder()
            .consumer(false)
            .serviceDefinition(unrelatedService)
            .portName("somePortNameA")
            .build();
        final PortDto unrelatedProducerPort = new PortDto.Builder()
            .consumer(true)
            .serviceDefinition(unrelatedService)
            .portName("somePortNameB")
            .build();
        final PdeSystemDto pdeSystem = new PdeSystemDto.Builder()
            .systemId(pdeId)
            .systemName(ApiConstants.PDE_SYSTEM_NAME)
            .ports(monitorableConsumerPort, unrelatedConsumerPort)
            .build();
        final PdeSystemDto unmonitoredSystem = new PdeSystemDto.Builder()
            .systemId(systemId)
            .metadata(Map.of("x", "y"))
            .ports(monitorableProducerPort, unrelatedProducerPort)
            .build();
        final ConnectionDto unrelatedConnectionA = new ConnectionDto.Builder()
            .consumer(new SystemPortDto.Builder()
                .systemId(pdeId)
                .portName(unrelatedConsumerPort.portName())
                .build()
            )
            .producer(new SystemPortDto.Builder()
                .systemId(systemId)
                .portName(unrelatedProducerPort.portName())
                .build()
            )
            .build();
        PlantDescriptionEntryDto pdEntry = new PlantDescriptionEntryDto.Builder()
            .id(1)
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(
                pdeSystem,
                unmonitoredSystem
            )
            .createdAt(now)
            .updatedAt(now)
            .connections(unrelatedConnectionA)
            .build();

        final Future<Set<ServiceRecord>> resolveResult = Future.success(Collections.emptySet());
        pdTracker.put(pdEntry)
            .ifSuccess(result -> {
                when(serviceQuery.name(ApiConstants.MONITORABLE_SERVICE_NAME)).thenReturn(serviceQuery);
                when(serviceQuery.resolveAll()).thenReturn(resolveResult);
                pingTask.run();
                assertEquals(0, alarmManager.getActiveAlarmData(AlarmCause.NOT_MONITORABLE).size());
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldClearOneAlarm() {

        final ServiceRecord service = Mockito.mock(ServiceRecord.class);
        final Set<ServiceRecord> services = Set.of(service);
        final Future<Set<ServiceRecord>> resolveResult = Future.success(services);
        final SystemRecord provider = Mockito.mock(SystemRecord.class);
        final PlantDescriptionEntryDto entryWithMonitoredSystems = getEntryWithMonitoredSystems();
        final PdeSystem monitoredSystem = entryWithMonitoredSystems.systems().get(1);

        when(serviceQuery.name(ApiConstants.MONITORABLE_SERVICE_NAME)).thenReturn(serviceQuery);
        when(service.provider()).thenReturn(provider);
        when(service.uri()).thenReturn(serviceUri);
        when(provider.name()).thenReturn(systemNameA);
        when(provider.socketAddress()).thenReturn(address);

        final HttpClientRequest expectedRequest = new HttpClientRequest()
            .method(HttpMethod.GET)
            .uri(service.uri() + ApiConstants.MONITORABLE_PING_PATH)
            .header(ApiConstants.HEADER_ACCEPT, ApiConstants.APPLICATION_JSON);

        when(httpClient.send(
            any(InetSocketAddress.class),
            argThat(new RequestMatcher(expectedRequest))
        ))
        .thenReturn(Future.success(response));
        when(serviceQuery.resolveAll()).thenReturn(resolveResult);

        pdTracker.put(entryWithMonitoredSystems)
            .ifSuccess(result -> {
                alarmManager.raise(Alarm.createSystemNotMonitorableAlarm(
                    monitoredSystem.systemId(),
                    monitoredSystem.systemName().orElse(null),
                    monitoredSystem.metadata()
                ));

                pingTask.run();
                final List<PdeAlarmDto> alarms = alarmManager.getAlarms();
                assertEquals(3, alarms.size());
                PdeAlarmDto clearedAlarm = alarms.stream()
                    .filter(alarm -> alarm.clearedAt().isPresent())
                    .findFirst()
                    .orElse(null);

                assertEquals("System named '" + systemNameA + "' cannot be monitored.", clearedAlarm.description());
            })
            .onFailure(e -> fail());
    }

    private PlantDescriptionEntryDto getEntryWithMonitoredSystems() {
        final String monitorablePortName = "monitorable";
        final String pdeId = "pde";
        final String systemIdA = "sysA";
        final String systemIdB = "sysB";
        final String systemIdC = "sysC";

        final PortDto monitorableConsumerPort = new PortDto.Builder()
            .consumer(true)
            .serviceDefinition(ApiConstants.MONITORABLE_SERVICE_NAME)
            .portName(monitorablePortName)
            .build();
        final PortDto monitorableProducerPort = new PortDto.Builder()
            .consumer(false)
            .serviceDefinition(ApiConstants.MONITORABLE_SERVICE_NAME)
            .portName(monitorablePortName)
            .build();
        final PdeSystemDto pdeSystem = new PdeSystemDto.Builder()
            .systemId(pdeId)
            .systemName(ApiConstants.PDE_SYSTEM_NAME)
            .ports(monitorableConsumerPort)
            .build();
        final PdeSystemDto monitoredSystemA = new PdeSystemDto.Builder()
            .systemId(systemIdA)
            .systemName(systemNameA)
            .ports(monitorableProducerPort)
            .build();
        final PdeSystemDto monitoredSystemB = new PdeSystemDto.Builder()
            .systemId(systemIdB)
            .metadata(Map.of("x", "y"))
            .ports(monitorableProducerPort)
            .build();
        final PdeSystemDto monitoredSystemC = new PdeSystemDto.Builder()
            .systemId(systemIdC)
            .systemName(systemNameC)
            .ports(monitorableProducerPort)
            .build();
        final ConnectionDto monitorableConnectionA = new ConnectionDto.Builder()
            .consumer(new SystemPortDto.Builder()
                .systemId(pdeId)
                .portName(monitorablePortName)
                .build()
            )
            .producer(new SystemPortDto.Builder()
                .systemId(systemIdA)
                .portName(monitorablePortName)
                .build()
            )
            .build();
        final ConnectionDto monitorableConnectionB = new ConnectionDto.Builder()
            .consumer(new SystemPortDto.Builder()
                .systemId(pdeId)
                .portName(monitorablePortName)
                .build()
            )
            .producer(new SystemPortDto.Builder()
                .systemId(systemIdB)
                .portName(monitorablePortName)
                .build()
            )
            .build();
        final ConnectionDto monitorableConnectionC = new ConnectionDto.Builder()
            .consumer(new SystemPortDto.Builder()
                .systemId(pdeId)
                .portName(monitorablePortName)
                .build()
            )
            .producer(new SystemPortDto.Builder()
                .systemId(systemIdC)
                .portName(monitorablePortName)
                .build()
            )
            .build();
        return new PlantDescriptionEntryDto.Builder()
            .id(1)
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(
                pdeSystem,
                monitoredSystemA,
                monitoredSystemB,
                monitoredSystemC
            )
            .createdAt(now)
            .updatedAt(now)
            .connections(
                monitorableConnectionA,
                monitorableConnectionB,
                monitorableConnectionC
            )
            .build();
    }

    private PlantDescriptionEntryDto getEntryWitNonPdeSystemMonitoring() {
        final String pdeId = "pde";
        final String systemIdA = "sysA";
        final String systemIdB = "sysB";

        final PortDto monitorableConsumerPort = new PortDto.Builder()
            .consumer(true)
            .serviceDefinition(ApiConstants.MONITORABLE_SERVICE_NAME)
            .portName("mon-a")
            .build();
        final PortDto monitorableProducerPort = new PortDto.Builder()
            .consumer(false)
            .serviceDefinition(ApiConstants.MONITORABLE_SERVICE_NAME)
            .portName("mon-b")
            .build();

        final PdeSystemDto pdeSystem = new PdeSystemDto.Builder()
            .systemId(pdeId)
            .systemName(ApiConstants.PDE_SYSTEM_NAME)
            .ports(monitorableConsumerPort)
            .build();
        final PdeSystemDto systemA = new PdeSystemDto.Builder()
            .systemId(systemIdA)
            .systemName(systemNameA)
            .ports(monitorableProducerPort)
            .build();
        final PdeSystemDto systemB = new PdeSystemDto.Builder()
            .systemId(systemIdB)
            .systemName(systemNameB)
            .ports(monitorableProducerPort, monitorableConsumerPort)
            .build();
        final ConnectionDto connection = new ConnectionDto.Builder()
            .consumer(new SystemPortDto.Builder()
                .systemId(systemIdB)
                .portName(monitorableConsumerPort.portName())
                .build()
            )
            .producer(new SystemPortDto.Builder()
                .systemId(systemIdA)
                .portName(monitorableProducerPort.portName())
                .build()
            )
            .build();
        return new PlantDescriptionEntryDto.Builder()
            .id(1)
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(pdeSystem,
                systemA,
                systemB
            )
            .createdAt(now)
            .updatedAt(now)
            .connections(connection)
            .build();
    }

}