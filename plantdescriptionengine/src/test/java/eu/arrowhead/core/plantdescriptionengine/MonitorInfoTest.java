package eu.arrowhead.core.plantdescriptionengine;

import org.junit.jupiter.api.Test;
import se.arkalix.description.ProviderDescription;
import se.arkalix.description.ServiceDescription;
import se.arkalix.descriptor.InterfaceDescriptor;
import se.arkalix.descriptor.SecurityDescriptor;
import se.arkalix.dto.json.value.JsonBoolean;
import se.arkalix.dto.json.value.JsonObject;
import se.arkalix.dto.json.value.JsonPair;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MonitorInfoTest {

    private ServiceDescription createServiceDescription(final Map<String, String> metadata) {
        final ProviderDescription provider = new ProviderDescription(
            "Provider-system",
            new InetSocketAddress("0.0.0.0", 5000)
        );
        return new ServiceDescription.Builder()
            .name("service-a")
            .provider(provider)
            .uri("/test")
            .security(SecurityDescriptor.NOT_SECURE)
            .interfaces(List.of(InterfaceDescriptor.HTTP_SECURE_JSON))
            .metadata(metadata)
            .build();
    }

    private ServiceDescription createServiceDescription() {
        return createServiceDescription(new HashMap<>());
    }

    @Test
    public void shouldMatch() {

        final String systemName = "System A";
        final String serviceDefinition = "Service A";
        final Map<String, String> metadata = Map.of("a", "1", "b", "2");

        final MonitorInfo.Bundle info = new MonitorInfo.Bundle(systemName, serviceDefinition, metadata, null, null);

        final Map<String, String> systemMetadata = Map.of("a", "1");
        final Map<String, String> serviceMetadata = Map.of("b", "2");

        assertTrue(info.matchesPortMetadata(systemMetadata, serviceMetadata));
    }

    @Test
    public void shouldNotMatch() {

        final String systemName = "System A";
        final String serviceDefinition = "Service A";
        final Map<String, String> metadata = Map.of("a", "1", "b", "2");
        final MonitorInfo.Bundle info = new MonitorInfo.Bundle(systemName, serviceDefinition, metadata, null, null);

        final Map<String, String> systemMetadata = Map.of("a", "x");
        final Map<String, String> serviceMetadata = Map.of("b", "2");

        assertFalse(info.matchesPortMetadata(systemMetadata, serviceMetadata));
    }

    @Test
    public void subsetShouldMatch() {

        final String systemName = "System A";
        final String serviceDefinition = "Service A";
        final Map<String, String> metadata = Map.of("a", "1", "b", "2", "c", "3");

        final MonitorInfo.Bundle info = new MonitorInfo.Bundle(systemName, serviceDefinition, metadata, null, null);

        final Map<String, String> systemMetadata = Map.of("a", "1");
        final Map<String, String> serviceMetadata = Map.of("b", "2");

        assertTrue(info.matchesPortMetadata(systemMetadata, serviceMetadata));
    }

    @Test
    public void supersetShouldNotMatch() {

        final String systemName = "System A";
        final String serviceDefinition = "Service A";
        final Map<String, String> metadata = Map.of("a", "1", "b", "2");

        final MonitorInfo.Bundle info = new MonitorInfo.Bundle(systemName, serviceDefinition, metadata, null, null);

        final Map<String, String> systemMetadata = Map.of("a", "1");
        final Map<String, String> serviceMetadata = Map.of("b", "2", "c", "3");

        assertFalse(info.matchesPortMetadata(systemMetadata, serviceMetadata));
    }

    @Test
    public void shouldRequireServiceMetadata() {

        final String systemName = "System A";
        final String serviceDefinition = "Service A";
        final Map<String, String> metadata = Map.of("a", "1");

        final MonitorInfo.Bundle info = new MonitorInfo.Bundle(systemName, serviceDefinition, metadata, null, null);

        final Map<String, String> systemMetadata = Map.of("a", "1");
        final Map<String, String> serviceMetadata = new HashMap<>();

        assertFalse(info.matchesPortMetadata(systemMetadata, serviceMetadata));
        assertFalse(info.matchesPortMetadata(systemMetadata, null));
    }

    @Test
    public void serviceShouldOverrideSystem() {

        final String systemName = "System A";
        final String serviceDefinition = "Service A";
        final Map<String, String> metadata = Map.of("a", "1");

        final MonitorInfo.Bundle info = new MonitorInfo.Bundle(systemName, serviceDefinition, metadata, null, null);

        Map<String, String> systemMetadata = Map.of("a", "2");
        Map<String, String> serviceMetadata = Map.of("a", "1");

        assertTrue(info.matchesPortMetadata(systemMetadata, serviceMetadata));

        systemMetadata = Map.of("a", "1");
        serviceMetadata = Map.of("a", "2");

        assertFalse(info.matchesPortMetadata(systemMetadata, serviceMetadata));

    }

    @Test
    public void shouldStoreSystemData() {

        final Map<String, String> metadata = Map.of("name", "abc");
        final ServiceDescription serviceDescription = createServiceDescription(metadata);

        final JsonObject jsonObject = new JsonObject(List.of(new JsonPair("a", JsonBoolean.TRUE)));

        final MonitorInfo monitorInfo = new MonitorInfo();
        monitorInfo.putSystemData(serviceDescription, jsonObject);

        final Map<String, String> lookupMetadata = Map.of("name", "abc");
        final List<MonitorInfo.Bundle> systemInfoList = monitorInfo.getSystemInfo(null, lookupMetadata);
        assertEquals(1, systemInfoList.size());
        final MonitorInfo.Bundle systemInfo = systemInfoList.get(0);
        assertEquals("{[a: true]}", systemInfo.systemData.toString());
    }

    @Test
    public void shouldStoreInventoryId() {
        final Map<String, String> metadataA = Map.of("name", "a");
        final Map<String, String> metadataB = Map.of("name", "b");
        final String systemNameA = "System-a";
        final String systemNameB = "System-b";

        final ProviderDescription providerA = new ProviderDescription(systemNameA, new InetSocketAddress("0.0.0.0", 5000));
        final ServiceDescription serviceA = new ServiceDescription.Builder()
            .name("service-a")
            .provider(providerA)
            .uri("/test")
            .security(SecurityDescriptor.NOT_SECURE)
            .interfaces(List.of(InterfaceDescriptor.HTTP_SECURE_JSON))
            .metadata(metadataA)
            .build();

        final ProviderDescription providerB = new ProviderDescription(systemNameB, new InetSocketAddress("0.0.0.0", 5001));
        final ServiceDescription serviceB = new ServiceDescription.Builder()
            .name("service-b")
            .provider(providerB)
            .uri("/test")
            .security(SecurityDescriptor.NOT_SECURE)
            .interfaces(List.of(InterfaceDescriptor.HTTP_SECURE_JSON))
            .metadata(metadataB)
            .build();

        final String inventoryIdA = "id-A";
        final String inventoryIdB = "id-B";

        final MonitorInfo monitorInfo = new MonitorInfo();
        monitorInfo.putInventoryId(serviceA, inventoryIdA);
        monitorInfo.putInventoryId(serviceB, inventoryIdB);

        final List<MonitorInfo.Bundle> systemInfoList = monitorInfo.getSystemInfo(serviceB.provider().name(), null);
        assertEquals(1, systemInfoList.size());
        final MonitorInfo.Bundle systemInfo = systemInfoList.get(0);
        assertEquals(inventoryIdB, systemInfo.inventoryId);
    }

    @Test
    public void shouldRetrieveBySystemName() {

        final Map<String, String> metadata = Map.of("x", "y");
        final ServiceDescription serviceDescription = createServiceDescription(metadata);

        final String inventoryId = "id-4567";
        final MonitorInfo monitorInfo = new MonitorInfo();
        monitorInfo.putInventoryId(serviceDescription, inventoryId);
        final List<MonitorInfo.Bundle> systemInfoList = monitorInfo.getSystemInfo(serviceDescription.provider().name(), null);
        assertEquals(1, systemInfoList.size());

        final MonitorInfo.Bundle systemInfo = systemInfoList.get(0);
        assertEquals(inventoryId, systemInfo.inventoryId);
    }

    @Test
    public void shouldOverwriteData() {

        final ServiceDescription service = createServiceDescription();
        final MonitorInfo monitorInfo = new MonitorInfo();

        final String oldInventoryId = "id-1234";
        monitorInfo.putInventoryId(service, oldInventoryId);

        final String newInventoryId = "id-5678";
        monitorInfo.putInventoryId(service, newInventoryId);

        final List<MonitorInfo.Bundle> systemInfoList = monitorInfo.getSystemInfo(service.provider().name(), null);
        assertEquals(1, systemInfoList.size());

        final MonitorInfo.Bundle systemInfo = systemInfoList.get(0);
        assertEquals(newInventoryId, systemInfo.inventoryId);
    }

    @Test
    public void shouldMergeData() {
        final ServiceDescription service = createServiceDescription();
        final MonitorInfo monitorInfo = new MonitorInfo();

        final String inventoryId = "xyz";
        monitorInfo.putInventoryId(service, inventoryId);

        final JsonObject systemData = new JsonObject(List.of(new JsonPair("b", JsonBoolean.FALSE)));

        monitorInfo.putSystemData(service, systemData);

        final List<MonitorInfo.Bundle> systemInfoList = monitorInfo.getSystemInfo(service.provider().name(), null);
        assertEquals(1, systemInfoList.size());

        final MonitorInfo.Bundle systemInfo = systemInfoList.get(0);
        assertEquals(inventoryId, systemInfo.inventoryId);
        assertEquals("{[b: false]}", systemInfo.systemData.toString());
    }
}
