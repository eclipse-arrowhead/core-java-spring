package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.MonitorInfoTracker;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.InMemoryPdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStoreException;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessage;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.ConnectionDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PdeSystemDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PortDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.SystemPortDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.Connection;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.MonitorPlantDescriptionEntry;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PlantDescriptionEntryList;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PortEntry;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.SystemEntry;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.SystemPort;
import eu.arrowhead.core.plantdescriptionengine.providedservices.requestvalidation.QueryParameter;
import eu.arrowhead.core.plantdescriptionengine.utils.MockRequest;
import eu.arrowhead.core.plantdescriptionengine.utils.MockServiceResponse;
import eu.arrowhead.core.plantdescriptionengine.utils.TestUtils;
import org.junit.Before;
import org.junit.Test;
import se.arkalix.ServiceInterface;
import se.arkalix.ServiceRecord;
import se.arkalix.SystemRecord;
import se.arkalix.codec.json.JsonBoolean;
import se.arkalix.codec.json.JsonObject;
import se.arkalix.codec.json.JsonPair;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpServiceRequest;
import se.arkalix.security.access.AccessPolicyType;
import se.arkalix.util.concurrent.Futures;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GetAllPlantDescriptionsTest {

    final Instant t1 = Instant.parse("2020-05-27T14:48:00.00Z");
    final Instant t2 = Instant.parse("2020-06-27T14:48:00.00Z");
    final Instant t3 = Instant.parse("2020-07-27T14:48:00.00Z");

    final Instant t4 = Instant.parse("2020-08-01T14:48:00.00Z");
    final Instant t5 = Instant.parse("2020-08-02T14:48:00.00Z");
    final Instant t6 = Instant.parse("2020-08-03T14:48:00.00Z");

    PlantDescriptionTracker pdTracker;
    GetAllPlantDescriptions handler;
    MockServiceResponse response;
    MonitorInfoTracker monitorInfoTracker;

    @Before
    public void initEach() throws PdStoreException {
        pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());
        monitorInfoTracker = new MonitorInfoTracker();
        handler = new GetAllPlantDescriptions(monitorInfoTracker, pdTracker);
        response = new MockServiceResponse();
    }

    @Test
    public void shouldRespondWithStoredEntries() {

        final List<Integer> entryIds = List.of(0, 1, 2, 3);
        final HttpServiceRequest request = new MockRequest();

        Futures.serialize(
            entryIds.stream().map(id -> pdTracker.put(TestUtils.createEntry(id)))
        )
            .flatMap(result -> handler.handle(request, response))
            .ifSuccess(result -> {
                final PlantDescriptionEntryList entries = (PlantDescriptionEntryList) response.getRawBody();
                assertEquals(entryIds.size(), entries.count());
                assertEquals(HttpStatus.OK, response.status().orElse(null));
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldPreserveConnections() {

        final Instant now = Instant.now();
        final List<ConnectionDto> connections = new ArrayList<>();
        final String consumerPortName = "consumer-port";
        final String consumerSystemId = "consumer-id";
        final String producerPortName = "provider-port";
        final String producerSystemId = "provider-id";
        connections.add(new ConnectionDto.Builder()
            .consumer(new SystemPortDto.Builder()
                .portName(consumerPortName)
                .systemId(consumerSystemId)
                .build())
            .producer(new SystemPortDto.Builder()
                .portName(producerPortName)
                .systemId(producerSystemId)
                .build())
            .build());

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(1)
            .plantDescription("Plant Description 1A")
            .active(false)
            .connections(connections)
            .createdAt(now)
            .updatedAt(now)
            .build();

        final HttpServiceRequest request = new MockRequest();

        pdTracker.put(entry)
            .flatMap(result -> handler.handle(request, response))
            .ifSuccess(result -> {
                final PlantDescriptionEntryList entries = (PlantDescriptionEntryList) response.getRawBody();
                final MonitorPlantDescriptionEntry retrievedEntry = entries.data().get(0);
                assertEquals(1, retrievedEntry.connections().size());

                final Connection connection = retrievedEntry.connections().get(0);

                final SystemPort consumer = connection.consumer();
                final SystemPort producer = connection.producer();

                assertEquals(consumer.systemId(), consumerSystemId);
                assertEquals(producer.systemId(), producerSystemId);

                assertEquals(consumer.portName(), consumerPortName);
                assertEquals(producer.portName(), producerPortName);

            })
            .onFailure(e -> fail());

    }

    @Test
    public void shouldPreservePorts() {

        final Instant now = Instant.now();
        final Map<String, String> metadata = Map.of("a", "b");
        final boolean isConsumer = true;
        final String portName = "Port-A";
        final String serviceDefinition = "servicexyz";
        final List<PortDto> ports = List.of(new PortDto.Builder()
            .consumer(isConsumer)
            .metadata(metadata)
            .portName(portName)
            .serviceInterface("HTTP-SECURE-JSON")
            .serviceDefinition(serviceDefinition)
            .build());

        final PdeSystemDto system = new PdeSystemDto.Builder()
            .systemName("sysa")
            .systemId("system_a")
            .ports(ports)
            .build();

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(1)
            .plantDescription("Plant Description 1A")
            .active(false)
            .systems(List.of(system))
            .createdAt(now)
            .updatedAt(now)
            .build();

        final HttpServiceRequest request = new MockRequest();

        pdTracker.put(entry)
            .flatMap(result -> handler.handle(request, response))
            .ifSuccess(result -> {
                final PlantDescriptionEntryList entries = (PlantDescriptionEntryList) response.getRawBody();
                final MonitorPlantDescriptionEntry retrievedEntry = entries.data().get(0);
                final SystemEntry retrievedSystem = retrievedEntry.systems().get(0);
                assertEquals(1, retrievedSystem.ports().size());

                final PortEntry retrievedPort = retrievedSystem.ports().get(0);
                assertEquals(isConsumer, retrievedPort.consumer().orElse(false));
                assertEquals(metadata, retrievedPort.metadata());
                assertEquals(portName, retrievedPort.portName());
                assertEquals(serviceDefinition, retrievedPort.serviceDefinition());

            })
            .onFailure(e -> fail());

    }

    @Test
    public void shouldExtendWithMonitorData() {

        final String systemName = "sysa";
        final PdeSystemDto system = new PdeSystemDto.Builder()
            .systemName(systemName)
            .systemId("system_a")
            .build();
        final Instant now = Instant.now();
        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(1)
            .plantDescription("Plant Description 1A")
            .active(false)
            .systems(List.of(system))
            .createdAt(now)
            .updatedAt(now)
            .build();

        final SystemRecord provider = SystemRecord.from(systemName, new InetSocketAddress("0.0.0.0", 5000));
        final ServiceRecord ServiceRecord = new ServiceRecord.Builder()
            .name("service-name")
            .uri("/abc")
            .provider(provider)
            .accessPolicyType(AccessPolicyType.NOT_SECURE)
            .interfaces(ServiceInterface.HTTP_SECURE_JSON)
            .build();

        final String inventoryId = "systemabc_inventory_id";
        final JsonObject systemData = new JsonObject(new JsonPair("a", JsonBoolean.TRUE));

        monitorInfoTracker.putInventoryId(ServiceRecord, inventoryId);
        monitorInfoTracker.putSystemData(ServiceRecord, systemData);

        final HttpServiceRequest request = new MockRequest();

        pdTracker.put(entry)
            .flatMap(result -> handler.handle(request, response))
            .ifSuccess(result -> {

                final PlantDescriptionEntryList returnedEntries = (PlantDescriptionEntryList) response.getRawBody();
                final MonitorPlantDescriptionEntry returnedEntry = returnedEntries.data().get(0);
                final SystemEntry returnedSystem = returnedEntry.systems().get(0);
                assertTrue(returnedSystem.inventoryId().isPresent());
                assertTrue(returnedSystem.systemData().isPresent());
                assertEquals(returnedSystem.inventoryId().get(), inventoryId);
                assertEquals(returnedSystem.systemData().get(), systemData);
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldSortByIdDescending() {

        final PlantDescriptionEntryDto entry1 = new PlantDescriptionEntryDto.Builder()
            .id(32)
            .plantDescription("Plant Description 1")
            .active(false)
            .createdAt(t1)
            .updatedAt(t4)
            .build();
        final PlantDescriptionEntryDto entry2 = new PlantDescriptionEntryDto.Builder()
            .id(2)
            .plantDescription("Plant Description 2")
            .active(false)
            .createdAt(t2)
            .updatedAt(t6)
            .build();
        final PlantDescriptionEntryDto entry3 = new PlantDescriptionEntryDto.Builder()
            .id(8)
            .plantDescription("Plant Description 3")
            .active(false)
            .createdAt(t3)
            .updatedAt(t5)
            .build();

        final HttpServiceRequest request = MockRequest.getSortRequest(
            QueryParameter.ID, QueryParameter.DESC
        );

        pdTracker.put(entry1)
            .flatMap(result -> pdTracker.put(entry2))
            .flatMap(result -> pdTracker.put(entry3))
            .flatMap(result -> handler.handle(request, response))
            .ifSuccess(result -> {
                final PlantDescriptionEntryList entries = (PlantDescriptionEntryList) response.getRawBody();

                assertEquals(HttpStatus.OK, response.status().orElse(null));
                assertEquals(3, entries.count());

                int previousId = entries.data().get(0).id();
                for (int i = 1; i < entries.count(); i++) {
                    final MonitorPlantDescriptionEntry entry = entries.data().get(i);
                    assertTrue(entry.id() <= previousId);
                    previousId = entry.id();
                }
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldSortByCreatedAtAscending() {

        final PlantDescriptionEntryDto entry1 = new PlantDescriptionEntryDto.Builder()
            .id(32)
            .plantDescription("Plant Description 1")
            .active(false)
            .createdAt(t1)
            .updatedAt(t4)
            .build();
        final PlantDescriptionEntryDto entry2 = new PlantDescriptionEntryDto.Builder()
            .id(2)
            .plantDescription("Plant Description 2")
            .active(false)
            .createdAt(t2)
            .updatedAt(t6)
            .build();
        final PlantDescriptionEntryDto entry3 = new PlantDescriptionEntryDto.Builder()
            .id(8)
            .plantDescription("Plant Description 3")
            .active(false)
            .createdAt(t3)
            .updatedAt(t5)
            .build();

        final HttpServiceRequest request = MockRequest.getSortRequest(
            QueryParameter.CREATED_AT, QueryParameter.ASC
        );

        pdTracker.put(entry1)
            .flatMap(result -> pdTracker.put(entry2))
            .flatMap(result -> pdTracker.put(entry3))
            .flatMap(result -> handler.handle(request, response))
            .ifSuccess(result -> {
                final PlantDescriptionEntryList entries = (PlantDescriptionEntryList) response.getRawBody();
                assertEquals(HttpStatus.OK, response.status().orElse(null));
                assertEquals(3, entries.count());

                Instant previousTimestamp = entries.data().get(0).createdAt();
                for (int i = 1; i < entries.count(); i++) {
                    final MonitorPlantDescriptionEntry entry = entries.data().get(i);
                    assertTrue(entry.createdAt().compareTo(previousTimestamp) >= 0);
                    previousTimestamp = entry.createdAt();
                }
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldSortByUpdatedAtDescending() {

        final PlantDescriptionEntryDto entry1 = new PlantDescriptionEntryDto.Builder()
            .id(32)
            .plantDescription("Plant Description 1")
            .active(false)
            .createdAt(t1)
            .updatedAt(t4)
            .build();
        final PlantDescriptionEntryDto entry2 = new PlantDescriptionEntryDto.Builder()
            .id(2)
            .plantDescription("Plant Description 2")
            .active(false)
            .createdAt(t2)
            .updatedAt(t6)
            .build();
        final PlantDescriptionEntryDto entry3 = new PlantDescriptionEntryDto.Builder()
            .id(8)
            .plantDescription("Plant Description 3")
            .active(false)
            .createdAt(t3)
            .updatedAt(t5)
            .build();

        final HttpServiceRequest request = MockRequest.getSortRequest(
            QueryParameter.UPDATED_AT, QueryParameter.DESC
        );

        pdTracker.put(entry1)
            .flatMap(result -> pdTracker.put(entry2))
            .flatMap(result -> pdTracker.put(entry3))
            .flatMap(result -> handler.handle(request, response))
            .ifSuccess(result -> {
                final PlantDescriptionEntryList entries = (PlantDescriptionEntryList) response.getRawBody();

                assertTrue(response.status().isPresent());
                assertEquals(HttpStatus.OK, response.status().get());
                assertEquals(3, entries.count());

                Instant previousTimestamp = entries.data().get(0).updatedAt();
                for (int i = 1; i < entries.count(); i++) {
                    final MonitorPlantDescriptionEntry entry = entries.data().get(i);
                    assertTrue(entry.updatedAt().compareTo(previousTimestamp) < 0);
                    previousTimestamp = entry.updatedAt();
                }
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldFilterEntries() {

        final List<Integer> entryIds = List.of(0, 1, 2);
        final int activeEntryId = 3;
        final Instant now = Instant.now();
        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParam(QueryParameter.ACTIVE, true)
            .build();

        Futures.serialize(
            entryIds.stream().map(id -> pdTracker.put(TestUtils.createEntry(id)))
        )
            .flatMap(result -> pdTracker.put(new PlantDescriptionEntryDto.Builder()
                .id(activeEntryId)
                .plantDescription("Plant Description 1B")
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build())
            )
            .flatMap(result -> handler.handle(request, response))
            .ifSuccess(result -> {
                assertEquals(HttpStatus.OK, response.status().orElse(null));
                final PlantDescriptionEntryList entries = (PlantDescriptionEntryList) response.getRawBody();
                assertEquals(1, entries.count());
                assertEquals(entries.data().get(0).id(), activeEntryId, 0);
            })
            .onFailure(e -> fail());

    }

    @Test
    public void shouldPaginate() {

        final List<Integer> entryIds = Arrays.asList(32, 11, 25, 3, 24, 35);

        final int page = 1;
        final int itemsPerPage = 2;
        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParam(QueryParameter.SORT_FIELD, QueryParameter.ID)
            .queryParam(QueryParameter.PAGE, page)
            .queryParam(QueryParameter.ITEM_PER_PAGE, itemsPerPage)
            .build();

        Futures.serialize(
            entryIds.stream().map(id -> pdTracker.put(TestUtils.createEntry(id)))
        )
            .flatMap(result -> handler.handle(request, response))
            .ifSuccess(result -> {
                final PlantDescriptionEntryList entries = (PlantDescriptionEntryList) response.getRawBody();
                assertEquals(entryIds.size(), entries.count());
                assertEquals(HttpStatus.OK, response.status().orElse(null));

                // Sort the entry ID:s, so that their order will match that of
                // the response data.
                Collections.sort(entryIds);

                for (int i = 0; i < itemsPerPage; i++) {
                    final int index = page * itemsPerPage + i;
                    assertEquals((int) entryIds.get(index), entries.data().get(i).id());
                }

            })
            .onFailure(e -> fail());

    }

    @Test
    public void shouldRejectNegativePage() {

        final int page = -1;
        final int itemsPerPage = 2;
        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParam(QueryParameter.PAGE, page)
            .queryParam(QueryParameter.ITEM_PER_PAGE, itemsPerPage)
            .build();

        handler.handle(request, response).ifSuccess(result -> {
            final String expectedErrorMessage = "<Query parameter 'page' must be greater than or equal to 0, got " +
                page + ".>";
            final String actualErrorMessage = ((ErrorMessage) response.getRawBody()).error();
            assertEquals(expectedErrorMessage, actualErrorMessage);
            assertEquals(HttpStatus.BAD_REQUEST, response.status().orElse(null));

        }).onFailure(e -> fail());
    }

    @Test
    public void shouldRequireItemPerPage() {

        final int page = 4;
        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParam(QueryParameter.PAGE, page)
            .build();

        handler.handle(request, response).ifSuccess(result -> {
            final String expectedErrorMessage = "<Missing parameter 'item_per_page'.>";
            final String actualErrorMessage = ((ErrorMessage) response.getRawBody()).error();
            assertEquals(expectedErrorMessage, actualErrorMessage);
            assertEquals(HttpStatus.BAD_REQUEST, response.status().orElse(null));

        }).onFailure(e -> fail());
    }

}