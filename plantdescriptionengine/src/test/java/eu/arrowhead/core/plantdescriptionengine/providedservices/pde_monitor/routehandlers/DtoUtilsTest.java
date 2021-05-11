package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.MonitorInfo;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PdeSystemDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntry;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PortDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.MonitorPlantDescriptionEntry;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.MonitorPlantDescriptionEntryDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PortEntry;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.SystemEntry;
import org.junit.jupiter.api.Test;
import se.arkalix.ServiceInterface;
import se.arkalix.ServiceRecord;
import se.arkalix.SystemRecord;
import se.arkalix.codec.json.JsonBoolean;
import se.arkalix.codec.json.JsonObject;
import se.arkalix.codec.json.JsonPair;
import se.arkalix.security.access.AccessPolicyType;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DtoUtilsTest {

    @Test
    public void shouldExtendWithMonitorData() {

        final String systemName = "System A";

        final Map<String, String> metadata = Map.of("a", "b");
        final String portName = "Port-A";
        final String serviceDefinition = "Service-AC";
        final String serviceInterface = "HTTP-SECURE-JSON";
        final List<PortDto> ports = List.of(
            // Port B and C will *not* be complemented by monitor info:
            new PortDto.Builder()
                .metadata(Map.of("i", "j")) // Differs from service A
                .portName("Port-C")
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceDefinition) // Same as service A
                .build(),
            new PortDto.Builder()
                .metadata(Map.of("x", "y")) // Differs from service A
                .portName("Port-B")
                .serviceInterface(serviceInterface)
                .serviceDefinition("Service-B") // Differs from service A
                .build(),
            // Port A will be complemented by monitor info:
            new PortDto.Builder()
                .metadata(metadata)
                .portName(portName)
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceDefinition)
                .build());

        final PdeSystemDto system = new PdeSystemDto.Builder()
            .systemName(systemName)
            .systemId("system_a")
            .ports(ports)
            .build();

        final Instant now = Instant.now();
        final PlantDescriptionEntry entry = new PlantDescriptionEntryDto.Builder()
            .id(1)
            .plantDescription("Plant Description 1A")
            .active(false)
            .include(new ArrayList<>())
            .systems(List.of(system))
            .connections(new ArrayList<>())
            .createdAt(now)
            .updatedAt(now)
            .build();

        final SystemRecord provider = SystemRecord.from(systemName, new InetSocketAddress("0.0.0.0", 5000));
        final ServiceRecord ServiceRecord = new ServiceRecord.Builder()
            .name(serviceDefinition)
            .metadata(metadata)
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

        final MonitorPlantDescriptionEntry extendedEntry = DtoUtils.extend(entry, monitorInfo);
        final SystemEntry extendedSystem = extendedEntry.systems().get(0);

        final PortEntry extendedPortA = extendedSystem.ports().get(2);
        final PortEntry extendedPortB = extendedSystem.ports().get(1);
        final PortEntry extendedPortC = extendedSystem.ports().get(0);

        assertEquals(inventoryId, extendedPortA.inventoryId().orElse(null));
        assertEquals(systemData, extendedPortA.systemData().orElse(null));

        assertTrue(extendedPortB.inventoryId().isEmpty());
        assertTrue(extendedPortB.systemData().isEmpty());

        assertTrue(extendedPortC.inventoryId().isEmpty());
        assertTrue(extendedPortC.systemData().isEmpty());
    }

    @Test
    public void shouldExtendWithoutMonitorData() {

        final String systemName = "System A";

        final Map<String, String> metadata = Map.of("a", "b");
        final String portName = "Port-A";
        final String serviceDefinition = "Service-A";
        final String serviceInterface = "HTTP-SECURE-JSON";
        final List<PortDto> ports = List
            .of(new PortDto.Builder()
                .metadata(metadata)
                .portName(portName)
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceDefinition)
                .build());
        final PdeSystemDto system = new PdeSystemDto.Builder()
            .systemName(systemName)
            .systemId("system_a")
            .ports(ports)
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

        final MonitorInfo monitorInfo = new MonitorInfo();

        final MonitorPlantDescriptionEntryDto extendedEntry = DtoUtils.extend(entry, monitorInfo);
        final SystemEntry extendedSystem = extendedEntry.systems().get(0);
        final PortEntry extendedPort = extendedSystem.ports().get(0);
        assertTrue(extendedPort.inventoryId().isEmpty());
        assertTrue(extendedPort.systemData().isEmpty());
        assertEquals(serviceInterface, extendedPort.serviceInterface().orElse(null));
    }

    /**
     * Test that consumer ports are not supplemented with monitor info. In fact,
     * Plant Descriptions with consumer ports containing metadata are not
     * allowed. This is enforced by the {@link eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.PlantDescriptionValidator}.
     */
    @Test
    public void shouldNotAddMonitorDataToConsumerPort() {

        final String systemName = "System A";

        final Map<String, String> metadata = Map.of("a", "b");
        final String portName = "Port-A";
        final String serviceDefinition = "Service-AC";
        final String serviceInterface = "HTTP-SECURE-JSON";
        final List<PortDto> ports = List.of(
            new PortDto.Builder()
                .metadata(metadata)
                .portName(portName)
                .consumer(true)
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceDefinition)
                .build());

        final PdeSystemDto system = new PdeSystemDto.Builder()
            .systemName(systemName)
            .systemId("system_a")
            .ports(ports)
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

        final SystemRecord provider = SystemRecord.from(systemName, new InetSocketAddress("0.0.0.0", 5000));
        final ServiceRecord ServiceRecord = new ServiceRecord.Builder()
            .name(serviceDefinition)
            .metadata(metadata)
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

        final MonitorPlantDescriptionEntryDto extendedEntry = DtoUtils.extend(entry, monitorInfo);
        final SystemEntry extendedSystem = extendedEntry.systems().get(0);
        final PortEntry extendedPortA = extendedSystem.ports().get(0);

        assertTrue(extendedPortA.inventoryId().isEmpty());
        assertTrue(extendedPortA.inventoryId().isEmpty());
    }

    /**
     * In this test, the MonitorInfo instance contains data that can not be
     * matched to the system itself, since its metadata differs from that of the
     * system.
     */
    @Test
    public void shouldNotMatchInfoToSystem() {

        final String systemName = "System A";

        final Map<String, String> metadata = Map.of("a", "b");
        final String serviceDefinition = "Service-AC";

        final PdeSystemDto system = new PdeSystemDto.Builder()
            .systemName(systemName)
            .metadata(Map.of("foo", "bar"))
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

        final SystemRecord provider = SystemRecord.from(systemName, new InetSocketAddress("0.0.0.0", 5000));
        final ServiceRecord ServiceRecord = new ServiceRecord.Builder()
            .name(serviceDefinition)
            .metadata(metadata)
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

        final MonitorPlantDescriptionEntryDto extendedEntry = DtoUtils.extend(entry, monitorInfo);
        final SystemEntry extendedSystem = extendedEntry.systems().get(0);
        assertTrue(extendedSystem.inventoryData().isEmpty());
        assertTrue(extendedSystem.systemData().isEmpty());
    }

}