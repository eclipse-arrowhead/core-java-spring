package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.MonitorInfo;
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
import eu.arrowhead.core.plantdescriptionengine.utils.MockRequest;
import eu.arrowhead.core.plantdescriptionengine.utils.MockServiceResponse;
import eu.arrowhead.core.plantdescriptionengine.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import se.arkalix.ServiceInterface;
import se.arkalix.ServiceRecord;
import se.arkalix.SystemRecord;
import se.arkalix.codec.json.JsonBoolean;
import se.arkalix.codec.json.JsonObject;
import se.arkalix.codec.json.JsonPair;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpServiceRequest;
import se.arkalix.security.access.AccessPolicyType;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class GetAllPlantDescriptionsTest {

    @Test
    public void shouldRespondWithStoredEntries() throws PdStoreException {

        final List<Integer> entryIds = List.of(0, 1, 2, 3);
        final PlantDescriptionTracker pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());

        for (final int id : entryIds) {
            pdTracker.put(TestUtils.createEntry(id));
        }

        final MonitorInfo monitorInfo = new MonitorInfo();

        final GetAllPlantDescriptions handler = new GetAllPlantDescriptions(monitorInfo, pdTracker);
        final HttpServiceRequest request = new MockRequest();
        final MockServiceResponse response = new MockServiceResponse();

        try {
            handler.handle(request, response).ifSuccess(result -> {
                final PlantDescriptionEntryList entries = (PlantDescriptionEntryList) response.getRawBody();
                assertEquals(entryIds.size(), entries.count());
                assertEquals(HttpStatus.OK, response.status().orElse(null));
            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            fail();
        }
    }

    @Test
    public void shouldPreserveConnections() throws PdStoreException {

        final PlantDescriptionTracker pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());

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
            .include(new ArrayList<>())
            .systems(new ArrayList<>())
            .connections(connections)
            .createdAt(now)
            .updatedAt(now)
            .build();

        pdTracker.put(entry);

        final MonitorInfo monitorInfo = new MonitorInfo();

        final GetAllPlantDescriptions handler = new GetAllPlantDescriptions(monitorInfo, pdTracker);
        final HttpServiceRequest request = new MockRequest();
        final MockServiceResponse response = new MockServiceResponse();

        try {
            handler.handle(request, response).ifSuccess(result -> {

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

            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            fail();
        }
    }

    @Test
    public void shouldPreservePorts() throws PdStoreException {

        final PlantDescriptionTracker pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());

        final Instant now = Instant.now();
        final Map<String, String> metadata = Map.of("a", "b");
        final boolean isConsumer = true;
        final String portName = "Port-A";
        final String serviceDefinition = "Service-A";
        final List<PortDto> ports = List.of(new PortDto.Builder()
            .consumer(isConsumer)
            .metadata(metadata)
            .portName(portName)
            .serviceInterface("HTTP-SECURE-JSON")
            .serviceDefinition(serviceDefinition)
            .build());

        final PdeSystemDto system = new PdeSystemDto.Builder()
            .systemName("System A")
            .systemId("system_a")
            .ports(ports)
            .build();

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(1)
            .plantDescription("Plant Description 1A")
            .active(false)
            .include(new ArrayList<>())
            .systems(List.of(system))
            .connections(new ArrayList<>())
            .createdAt(now)
            .updatedAt(now)
            .build();

        pdTracker.put(entry);

        final MonitorInfo monitorInfo = new MonitorInfo();

        final GetAllPlantDescriptions handler = new GetAllPlantDescriptions(monitorInfo, pdTracker);
        final HttpServiceRequest request = new MockRequest();
        final MockServiceResponse response = new MockServiceResponse();

        try {
            handler.handle(request, response).ifSuccess(result -> {

                final PlantDescriptionEntryList entries = (PlantDescriptionEntryList) response.getRawBody();
                final MonitorPlantDescriptionEntry retrievedEntry = entries.data().get(0);
                final SystemEntry retrievedSystem = retrievedEntry.systems().get(0);
                assertEquals(1, retrievedSystem.ports().size());

                final PortEntry retrievedPort = retrievedSystem.ports().get(0);
                assertEquals(isConsumer, retrievedPort.consumer().orElse(false));
                assertEquals(metadata, retrievedPort.metadata());
                assertEquals(portName, retrievedPort.portName());
                assertEquals(serviceDefinition, retrievedPort.serviceDefinition());

            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            fail();
        }
    }

    @Test
    public void shouldExtendWithMonitorData() throws PdStoreException {

        final PlantDescriptionTracker pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());
        final String systemName = "System A";
        final PdeSystemDto system = new PdeSystemDto.Builder()
            .systemName(systemName)
            .systemId("system_a")
            .build();
        final Instant now = Instant.now();
        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(1)
            .plantDescription("Plant Description 1A")
            .active(false)
            .include(new ArrayList<>())
            .systems(List.of(system))
            .connections(new ArrayList<>())
            .createdAt(now)
            .updatedAt(now)
            .build();

        pdTracker.put(entry);
        final SystemRecord provider = SystemRecord.from(systemName, new InetSocketAddress("0.0.0.0", 5000));
        final ServiceRecord ServiceRecord = new ServiceRecord.Builder()
            .name("service-name")
            .uri("/abc")
            .provider(provider)
            .accessPolicyType(AccessPolicyType.NOT_SECURE)
            .interfaces(ServiceInterface.HTTP_SECURE_JSON)
            .build();

        final String inventoryId = "system_a_inventory_id";
        final JsonObject systemData = new JsonObject(new JsonPair("a", JsonBoolean.TRUE));

        final MonitorInfo monitorInfo = new MonitorInfo();
        monitorInfo.putInventoryId(ServiceRecord, inventoryId);
        monitorInfo.putSystemData(ServiceRecord, systemData);

        final GetAllPlantDescriptions handler = new GetAllPlantDescriptions(monitorInfo, pdTracker);
        final HttpServiceRequest request = new MockRequest();
        final MockServiceResponse response = new MockServiceResponse();

        try {
            handler.handle(request, response).ifSuccess(result -> {

                final PlantDescriptionEntryList returnedEntries = (PlantDescriptionEntryList) response.getRawBody();
                final MonitorPlantDescriptionEntry returnedEntry = returnedEntries.data().get(0);
                final SystemEntry returnedSystem = returnedEntry.systems().get(0);
                assertTrue(returnedSystem.inventoryId().isPresent());
                assertTrue(returnedSystem.systemData().isPresent());
                assertEquals(returnedSystem.inventoryId().get(), inventoryId);
                assertEquals(returnedSystem.systemData().get(), systemData);
            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            fail();
        }
    }

    @Test
    public void shouldSortEntries() throws PdStoreException {
        final PlantDescriptionTracker pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());

        final Instant createdAt1 = Instant.parse("2020-05-27T14:48:00.00Z");
        final Instant createdAt2 = Instant.parse("2020-06-27T14:48:00.00Z");
        final Instant createdAt3 = Instant.parse("2020-07-27T14:48:00.00Z");

        final Instant updatedAt1 = Instant.parse("2020-08-01T14:48:00.00Z");
        final Instant updatedAt2 = Instant.parse("2020-08-03T14:48:00.00Z");
        final Instant updatedAt3 = Instant.parse("2020-08-02T14:48:00.00Z");

        final PlantDescriptionEntryDto entry1 = new PlantDescriptionEntryDto.Builder()
            .id(32)
            .plantDescription("Plant Description 1")
            .active(false)
            .include(new ArrayList<>())
            .systems(new ArrayList<>())
            .connections(new ArrayList<>())
            .createdAt(createdAt1)
            .updatedAt(updatedAt1)
            .build();
        final PlantDescriptionEntryDto entry2 = new PlantDescriptionEntryDto.Builder()
            .id(2)
            .plantDescription("Plant Description 2")
            .active(false)
            .include(new ArrayList<>())
            .systems(new ArrayList<>())
            .connections(new ArrayList<>())
            .createdAt(createdAt2)
            .updatedAt(updatedAt2)
            .build();
        final PlantDescriptionEntryDto entry3 = new PlantDescriptionEntryDto.Builder()
            .id(8)
            .plantDescription("Plant Description 3")
            .active(false)
            .include(new ArrayList<>())
            .systems(new ArrayList<>())
            .connections(new ArrayList<>())
            .createdAt(createdAt3)
            .updatedAt(updatedAt3)
            .build();

        pdTracker.put(entry1);
        pdTracker.put(entry2);
        pdTracker.put(entry3);

        final int numEntries = pdTracker.getListDto().count();

        final MonitorInfo monitorInfo = new MonitorInfo();

        final GetAllPlantDescriptions handler = new GetAllPlantDescriptions(monitorInfo, pdTracker);
        final HttpServiceRequest idDescendingRequest = new MockRequest.Builder()
            .queryParameters(Map.of("sort_field", List.of("id"), "direction", List.of("DESC")))
            .build();
        final HttpServiceRequest creationAscendingRequest = new MockRequest.Builder()
            .queryParameters(Map.of("sort_field", List.of("createdAt"), "direction", List.of("ASC")))
            .build();
        final HttpServiceRequest updatesDescendingRequest = new MockRequest.Builder()
            .queryParameters(Map.of("sort_field", List.of("updatedAt"), "direction", List.of("DESC")))
            .build();

        final MockServiceResponse response1 = new MockServiceResponse();
        final MockServiceResponse response2 = new MockServiceResponse();
        final MockServiceResponse response3 = new MockServiceResponse();

        try {
            handler.handle(idDescendingRequest, response1).flatMap(result -> {
                assertTrue(response1.status().isPresent());
                assertEquals(HttpStatus.OK, response1.status().get());

                final PlantDescriptionEntryList entries = (PlantDescriptionEntryList) response1.getRawBody();
                assertEquals(numEntries, entries.count());

                int previousId = entries.data().get(0).id();
                for (int i = 1; i < entries.count(); i++) {
                    final MonitorPlantDescriptionEntry entry = entries.data().get(i);
                    assertTrue(entry.id() <= previousId);
                    previousId = entry.id();
                }

                return handler.handle(creationAscendingRequest, response2);
            }).flatMap(result -> {
                assertTrue(response2.status().isPresent());
                assertEquals(HttpStatus.OK, response2.status().get());

                final PlantDescriptionEntryList entries = (PlantDescriptionEntryList) response2.getRawBody();
                assertEquals(numEntries, entries.count());

                Instant previousTimestamp = entries.data().get(0).createdAt();
                for (int i = 1; i < entries.count(); i++) {
                    final MonitorPlantDescriptionEntry entry = entries.data().get(i);
                    assertTrue(entry.createdAt().compareTo(previousTimestamp) >= 0);
                    previousTimestamp = entry.createdAt();
                }

                return handler.handle(updatesDescendingRequest, response3);
            }).ifSuccess(result -> {
                assertTrue(response3.status().isPresent());
                assertEquals(HttpStatus.OK, response3.status().get());

                final PlantDescriptionEntryList entries = (PlantDescriptionEntryList) response3.getRawBody();
                assertEquals(numEntries, entries.count());

                Instant previousTimestamp = entries.data().get(0).updatedAt();
                for (int i = 1; i < entries.count(); i++) {
                    final MonitorPlantDescriptionEntry entry = entries.data().get(i);
                    assertTrue(entry.updatedAt().compareTo(previousTimestamp) < 0);
                    previousTimestamp = entry.updatedAt();
                }
            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            fail();
        }
    }

    @Test
    public void shouldFilterEntries() throws PdStoreException {

        final List<Integer> entryIds = List.of(0, 1, 2);
        final int activeEntryId = 3;
        final PlantDescriptionTracker pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());

        for (final int id : entryIds) {
            pdTracker.put(TestUtils.createEntry(id));
        }

        final MonitorInfo monitorInfo = new MonitorInfo();
        final Instant now = Instant.now();
        pdTracker.put(new PlantDescriptionEntryDto.Builder()
            .id(activeEntryId)
            .plantDescription("Plant Description 1B")
            .active(true)
            .include(new ArrayList<>())
            .systems(new ArrayList<>())
            .connections(new ArrayList<>())
            .createdAt(now)
            .updatedAt(now)
            .build());

        final GetAllPlantDescriptions handler = new GetAllPlantDescriptions(monitorInfo, pdTracker);
        final HttpServiceRequest request = new MockRequest.Builder().queryParameters(Map.of("active", List.of("true")))
            .build();
        final MockServiceResponse response = new MockServiceResponse();

        try {
            handler.handle(request, response).ifSuccess(result -> {
                assertEquals(HttpStatus.OK, response.status().orElse(null));

                final PlantDescriptionEntryList entries = (PlantDescriptionEntryList) response.getRawBody();
                assertEquals(1, entries.count());
                assertEquals(entries.data().get(0).id(), activeEntryId, 0);
            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            fail();
        }
    }

    @Test
    public void shouldPaginate() throws PdStoreException {

        final List<Integer> entryIds = Arrays.asList(32, 11, 25, 3, 24, 35);
        final PlantDescriptionTracker pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());

        for (final int id : entryIds) {
            pdTracker.put(TestUtils.createEntry(id));
        }

        final GetAllPlantDescriptions handler = new GetAllPlantDescriptions(new MonitorInfo(), pdTracker);
        final MockServiceResponse response = new MockServiceResponse();
        final int page = 1;
        final int itemsPerPage = 2;
        final HttpServiceRequest request = new MockRequest.Builder().queryParameters(Map.of("sort_field", List.of("id"),
            "page", List.of(String.valueOf(page)), "item_per_page", List.of(String.valueOf(itemsPerPage)))).build();

        try {
            handler.handle(request, response).ifSuccess(result -> {
                final PlantDescriptionEntryList entries = (PlantDescriptionEntryList) response.getRawBody();
                assertEquals(itemsPerPage, entries.count());
                assertEquals(HttpStatus.OK, response.status().orElse(null));

                // Sort the entry ID:s, so that their order will match that of
                // the response data.
                Collections.sort(entryIds);

                for (int i = 0; i < itemsPerPage; i++) {
                    final int index = page * itemsPerPage + i;
                    assertEquals((int) entryIds.get(index), entries.data().get(i).id());
                }

            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            fail();
        }
    }

    @Test
    public void shouldRejectNegativePage() throws PdStoreException {

        final PlantDescriptionTracker pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());
        final GetAllPlantDescriptions handler = new GetAllPlantDescriptions(new MonitorInfo(), pdTracker);
        final MockServiceResponse response = new MockServiceResponse();
        final int page = -1;
        final int itemsPerPage = 2;
        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParameters(
                Map.of("page", List.of(String.valueOf(page)), "item_per_page", List.of(String.valueOf(itemsPerPage))))
            .build();

        try {
            handler.handle(request, response).ifSuccess(result -> {
                final String expectedErrorMessage = "<Query parameter 'page' must be greater than 0, got " + page + ".>";
                final String actualErrorMessage = ((ErrorMessage) response.getRawBody()).error();
                assertEquals(expectedErrorMessage, actualErrorMessage);
                assertEquals(HttpStatus.BAD_REQUEST, response.status().orElse(null));

            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            fail();
        }
    }

    @Test
    public void shouldRequireItemPerPage() throws PdStoreException {

        final PlantDescriptionTracker pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());

        final GetAllPlantDescriptions handler = new GetAllPlantDescriptions(new MonitorInfo(), pdTracker);
        final MockServiceResponse response = new MockServiceResponse();
        final int page = 4;
        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParameters(Map.of("page", List.of(String.valueOf(page))))
            .build();

        try {
            handler.handle(request, response).ifSuccess(result -> {
                final String expectedErrorMessage = "<Missing parameter 'item_per_page'.>";
                final String actualErrorMessage = ((ErrorMessage) response.getRawBody()).error();
                assertEquals(expectedErrorMessage, actualErrorMessage);
                assertEquals(HttpStatus.BAD_REQUEST, response.status().orElse(null));

            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            fail();
        }
    }

}