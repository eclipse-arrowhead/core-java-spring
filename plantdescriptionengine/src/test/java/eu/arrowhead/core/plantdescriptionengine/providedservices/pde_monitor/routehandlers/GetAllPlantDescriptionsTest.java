package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.MonitorInfo;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.InMemoryPdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStoreException;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessage;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.*;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.Connection;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PlantDescriptionEntryList;
import eu.arrowhead.core.plantdescriptionengine.utils.MockRequest;
import eu.arrowhead.core.plantdescriptionengine.utils.MockServiceResponse;
import eu.arrowhead.core.plantdescriptionengine.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import se.arkalix.description.ProviderDescription;
import se.arkalix.description.ServiceDescription;
import se.arkalix.descriptor.InterfaceDescriptor;
import se.arkalix.descriptor.SecurityDescriptor;
import se.arkalix.dto.json.value.JsonBoolean;
import se.arkalix.dto.json.value.JsonObject;
import se.arkalix.dto.json.value.JsonPair;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpServiceRequest;
import se.arkalix.net.http.service.HttpServiceResponse;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class GetAllPlantDescriptionsTest {

    @Test
    public void shouldRespondWithStoredEntries() throws PdStoreException {

        final List<Integer> entryIds = List.of(0, 1, 2, 3);
        final var pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());

        for (final int id : entryIds) {
            pdTracker.put(TestUtils.createEntry(id));
        }

        final var monitorInfo = new MonitorInfo();

        final GetAllPlantDescriptions handler = new GetAllPlantDescriptions(monitorInfo, pdTracker);
        final HttpServiceRequest request = new MockRequest();
        final HttpServiceResponse response = new MockServiceResponse();

        try {
            handler.handle(request, response).ifSuccess(result -> {
                assertEquals(HttpStatus.OK, response.status().orElse(null));
                assertTrue(response.body().isPresent());
                final var entries = (PlantDescriptionEntryList) response.body().get();
                assertEquals(entryIds.size(), entries.count());
            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void shouldPreserveConnections() throws PdStoreException {

        final var pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());

        final Instant now = Instant.now();
        final List<ConnectionDto> connections = new ArrayList<>();
        final String consumerPortName = "consumer-port";
        final String consumerSystemId = "consumer-id";
        final String producerPortName = "provider-port";
        final String producerSystemId = "provider-id";
        connections.add(new ConnectionBuilder()
            .consumer(new SystemPortBuilder()
                .portName(consumerPortName)
                .systemId(consumerSystemId)
                .build())
            .producer(new SystemPortBuilder()
                .portName(producerPortName)
                .systemId(producerSystemId)
                .build())
            .build());

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryBuilder()
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

        final var monitorInfo = new MonitorInfo();

        final GetAllPlantDescriptions handler = new GetAllPlantDescriptions(monitorInfo, pdTracker);
        final HttpServiceRequest request = new MockRequest();
        final HttpServiceResponse response = new MockServiceResponse();

        try {
            handler.handle(request, response).ifSuccess(result -> {
                assertTrue(response.body().isPresent());
                final var entries = (PlantDescriptionEntryList) response.body().get();
                final var retrievedEntry = entries.data().get(0);
                assertEquals(1, retrievedEntry.connections().size());

                Connection connection = retrievedEntry.connections().get(0);

                var consumer = connection.consumer();
                var producer = connection.producer();

                assertEquals(consumer.systemId(), consumerSystemId);
                assertEquals(producer.systemId(), producerSystemId);

                assertEquals(consumer.portName(), consumerPortName);
                assertEquals(producer.portName(), producerPortName);

            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void shouldPreservePorts() throws PdStoreException {

        final var pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());

        final Instant now = Instant.now();
        final Map<String, String> metadata = Map.of("a", "b");
        final boolean isConsumer = true;
        final String portName = "Port-A";
        final String serviceDefinition = "Service-A";
        final List<PortDto> ports = List.of(new PortBuilder()
            .consumer(isConsumer)
            .metadata(metadata)
            .portName(portName)
            .serviceInterface("HTTP-SECURE-JSON")
            .serviceDefinition(serviceDefinition)
            .build());

        final PdeSystemDto system = new PdeSystemBuilder()
            .systemName("System A")
            .systemId("system_a")
            .ports(ports)
            .build();

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryBuilder()
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

        final var monitorInfo = new MonitorInfo();

        final GetAllPlantDescriptions handler = new GetAllPlantDescriptions(monitorInfo, pdTracker);
        final HttpServiceRequest request = new MockRequest();
        final HttpServiceResponse response = new MockServiceResponse();

        try {
            handler.handle(request, response).ifSuccess(result -> {
                assertTrue(response.body().isPresent());
                final var entries = (PlantDescriptionEntryList) response.body().get();
                final var retrievedEntry = entries.data().get(0);
                final var retrievedSystem = retrievedEntry.systems().get(0);
                assertEquals(1, retrievedSystem.ports().size());

                final var retrievedPort = retrievedSystem.ports().get(0);
                assertEquals(isConsumer, retrievedPort.consumer().orElse(false));
                assertEquals(metadata, retrievedPort.metadata().orElse(null));
                assertEquals(portName, retrievedPort.portName());
                assertEquals(serviceDefinition, retrievedPort.serviceDefinition());

            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void shouldExtendWithMonitorData() throws PdStoreException {

        final var pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());
        final String systemName = "System A";
        final PdeSystemDto system = new PdeSystemBuilder()
            .systemName(systemName)
            .systemId("system_a")
            .build();
        final Instant now = Instant.now();
        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryBuilder()
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
        final var provider = new ProviderDescription(systemName, new InetSocketAddress("0.0.0.0", 5000));
        ServiceDescription serviceDescription = new ServiceDescription.Builder()
            .name("service-name")
            .uri("/abc")
            .security(SecurityDescriptor.NOT_SECURE)
            .provider(provider)
            .interfaces(InterfaceDescriptor.HTTP_SECURE_JSON)
            .build();

        final String inventoryId = "system_a_inventory_id";
        JsonObject systemData = new JsonObject(List.of(new JsonPair("a", JsonBoolean.TRUE)));

        final var monitorInfo = new MonitorInfo();
        monitorInfo.putInventoryId(serviceDescription, inventoryId);
        monitorInfo.putSystemData(serviceDescription, systemData);

        final GetAllPlantDescriptions handler = new GetAllPlantDescriptions(monitorInfo, pdTracker);
        final HttpServiceRequest request = new MockRequest();
        final HttpServiceResponse response = new MockServiceResponse();

        try {
            handler.handle(request, response).ifSuccess(result -> {
                assertTrue(response.body().isPresent());
                final var returnedEntries = (PlantDescriptionEntryList) response.body().get();
                final var returnedEntry = returnedEntries.data().get(0);
                final var returnedSystem = returnedEntry.systems().get(0);
                assertTrue(returnedSystem.inventoryId().isPresent());
                assertTrue(returnedSystem.systemData().isPresent());
                assertEquals(returnedSystem.inventoryId().get(), inventoryId);
                assertEquals(returnedSystem.systemData().get(), systemData);
            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void shouldSortEntries() throws PdStoreException {
        final var pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());

        final Instant createdAt1 = Instant.parse("2020-05-27T14:48:00.00Z");
        final Instant createdAt2 = Instant.parse("2020-06-27T14:48:00.00Z");
        final Instant createdAt3 = Instant.parse("2020-07-27T14:48:00.00Z");

        final Instant updatedAt1 = Instant.parse("2020-08-01T14:48:00.00Z");
        final Instant updatedAt2 = Instant.parse("2020-08-03T14:48:00.00Z");
        final Instant updatedAt3 = Instant.parse("2020-08-02T14:48:00.00Z");

        final PlantDescriptionEntryDto entry1 = new PlantDescriptionEntryBuilder()
            .id(32)
            .plantDescription("Plant Description 1")
            .active(false)
            .include(new ArrayList<>())
            .systems(new ArrayList<>())
            .connections(new ArrayList<>())
            .createdAt(createdAt1)
            .updatedAt(updatedAt1)
            .build();
        final PlantDescriptionEntryDto entry2 = new PlantDescriptionEntryBuilder()
            .id(2)
            .plantDescription("Plant Description 2")
            .active(false)
            .include(new ArrayList<>())
            .systems(new ArrayList<>())
            .connections(new ArrayList<>())
            .createdAt(createdAt2)
            .updatedAt(updatedAt2)
            .build();
        final PlantDescriptionEntryDto entry3 = new PlantDescriptionEntryBuilder()
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

        final var monitorInfo = new MonitorInfo();

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
        final HttpServiceResponse response1 = new MockServiceResponse();
        final HttpServiceResponse response2 = new MockServiceResponse();
        final HttpServiceResponse response3 = new MockServiceResponse();

        try {
            handler.handle(idDescendingRequest, response1).flatMap(result -> {
                assertTrue(response1.status().isPresent());
                assertEquals(HttpStatus.OK, response1.status().get());

                assertTrue(response1.body().isPresent());
                final var entries = (PlantDescriptionEntryList) response1.body().get();
                assertEquals(numEntries, entries.count());

                int previousId = entries.data().get(0).id();
                for (int i = 1; i < entries.count(); i++) {
                    final var entry = entries.data().get(i);
                    assertTrue(entry.id() <= previousId);
                    previousId = entry.id();
                }

                return handler.handle(creationAscendingRequest, response2);
            }).flatMap(result -> {
                assertTrue(response2.status().isPresent());
                assertEquals(HttpStatus.OK, response2.status().get());

                assertTrue(response2.body().isPresent());
                final var entries = (PlantDescriptionEntryList) response2.body().get();
                assertEquals(numEntries, entries.count());

                Instant previousTimestamp = entries.data().get(0).createdAt();
                for (int i = 1; i < entries.count(); i++) {
                    final var entry = entries.data().get(i);
                    assertTrue(entry.createdAt().compareTo(previousTimestamp) >= 0);
                    previousTimestamp = entry.createdAt();
                }

                return handler.handle(updatesDescendingRequest, response3);
            }).ifSuccess(result -> {
                assertTrue(response3.status().isPresent());
                assertEquals(HttpStatus.OK, response3.status().get());

                assertTrue(response3.body().isPresent());
                final var entries = (PlantDescriptionEntryList) response3.body().get();
                assertEquals(numEntries, entries.count());

                Instant previousTimestamp = entries.data().get(0).updatedAt();
                for (int i = 1; i < entries.count(); i++) {
                    final var entry = entries.data().get(i);
                    assertTrue(entry.updatedAt().compareTo(previousTimestamp) < 0);
                    previousTimestamp = entry.updatedAt();
                }
            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void shouldFilterEntries() throws PdStoreException {

        final List<Integer> entryIds = List.of(0, 1, 2);
        final int activeEntryId = 3;
        final var pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());

        for (final int id : entryIds) {
            pdTracker.put(TestUtils.createEntry(id));
        }

        final var monitorInfo = new MonitorInfo();
        final Instant now = Instant.now();
        pdTracker.put(new PlantDescriptionEntryBuilder()
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
        final HttpServiceResponse response = new MockServiceResponse();

        try {
            handler.handle(request, response).ifSuccess(result -> {
                assertEquals(HttpStatus.OK, response.status().orElse(null));
                assertTrue(response.body().isPresent());
                final var entries = (PlantDescriptionEntryList) response.body().get();
                assertEquals(1, entries.count());
                assertEquals(entries.data().get(0).id(), activeEntryId, 0);
            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void shouldPaginate() throws PdStoreException {

        final List<Integer> entryIds = Arrays.asList(32, 11, 25, 3, 24, 35);
        final var pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());

        for (int id : entryIds) {
            pdTracker.put(TestUtils.createEntry(id));
        }

        final var handler = new GetAllPlantDescriptions(new MonitorInfo(), pdTracker);
        final HttpServiceResponse response = new MockServiceResponse();
        final int page = 1;
        final int itemsPerPage = 2;
        final HttpServiceRequest request = new MockRequest.Builder().queryParameters(Map.of("sort_field", List.of("id"),
            "page", List.of(String.valueOf(page)), "item_per_page", List.of(String.valueOf(itemsPerPage)))).build();

        try {
            handler.handle(request, response).ifSuccess(result -> {
                assertEquals(HttpStatus.OK, response.status().orElse(null));
                assertTrue(response.body().isPresent());
                var entries = (PlantDescriptionEntryList) response.body().get();
                assertEquals(itemsPerPage, entries.count());

                // Sort the entry ID:s, so that their order will match that of
                // the response data.
                Collections.sort(entryIds);

                for (int i = 0; i < itemsPerPage; i++) {
                    int index = page * itemsPerPage + i;
                    assertEquals((int) entryIds.get(index), entries.data().get(i).id());
                }

            }).onFailure(Assertions::assertNull);
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void shouldRejectNegativePage() throws PdStoreException {

        final var pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());
        final var handler = new GetAllPlantDescriptions(new MonitorInfo(), pdTracker);
        final HttpServiceResponse response = new MockServiceResponse();
        final int page = -1;
        final int itemsPerPage = 2;
        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParameters(
                Map.of("page", List.of(String.valueOf(page)), "item_per_page", List.of(String.valueOf(itemsPerPage))))
            .build();

        try {
            handler.handle(request, response).ifSuccess(result -> {
                assertEquals(HttpStatus.BAD_REQUEST, response.status().orElse(null));
                assertTrue(response.body().isPresent());
                String expectedErrorMessage = "<Query parameter 'page' must be greater than 0, got " + page + ".>";
                String actualErrorMessage = ((ErrorMessage) response.body().get()).error();
                assertEquals(expectedErrorMessage, actualErrorMessage);

            }).onFailure(Assertions::assertNull);
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void shouldRequireItemPerPage() throws PdStoreException {

        final var pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());

        final var handler = new GetAllPlantDescriptions(new MonitorInfo(), pdTracker);
        final HttpServiceResponse response = new MockServiceResponse();
        final int page = 4;
        final HttpServiceRequest request = new MockRequest.Builder()
            .queryParameters(Map.of("page", List.of(String.valueOf(page))))
            .build();

        try {
            handler.handle(request, response).ifSuccess(result -> {
                assertEquals(HttpStatus.BAD_REQUEST, response.status().orElse(null));
                assertTrue(response.body().isPresent());
                String expectedErrorMessage = "<Missing parameter 'item_per_page'.>";
                String actualErrorMessage = ((ErrorMessage) response.body().get()).error();
                assertEquals(expectedErrorMessage, actualErrorMessage);

            }).onFailure(Assertions::assertNull);
        } catch (Exception e) {
            assertNull(e);
        }
    }

}