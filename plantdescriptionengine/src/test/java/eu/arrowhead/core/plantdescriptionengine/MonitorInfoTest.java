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

import static org.junit.jupiter.api.Assertions.*;

public class MonitorInfoTest {

    private ServiceDescription createServiceDescription(final Map<String, String> metadata) {
        var provider = new ProviderDescription("Provider-system", new InetSocketAddress("0.0.0.0", 5000));
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

        String systemName = "System A";
        String serviceDefinition = "Service A";
        Map<String, String> metadata = Map.of("a", "1", "b", "2");

        var info = new MonitorInfo.Bundle(systemName, serviceDefinition, metadata, null, null);

        Map<String, String> systemMetadata = Map.of("a", "1");
        Map<String, String> serviceMetadata = Map.of("b", "2");

        assertTrue(info.matchesPortMetadata(systemMetadata, serviceMetadata));
    }

    @Test
    public void shouldNotMatch() {

        String systemName = "System A";
        String serviceDefinition = "Service A";
        Map<String, String> metadata = Map.of("a", "1", "b", "2");
        var info = new MonitorInfo.Bundle(systemName, serviceDefinition, metadata, null, null);

        Map<String, String> systemMetadata = Map.of("a", "x");
        Map<String, String> serviceMetadata = Map.of("b", "2");

        assertFalse(info.matchesPortMetadata(systemMetadata, serviceMetadata));
    }

    @Test
    public void subsetShouldMatch() {

        String systemName = "System A";
        String serviceDefinition = "Service A";
        Map<String, String> metadata = Map.of("a", "1", "b", "2", "c", "3");

        var info = new MonitorInfo.Bundle(systemName, serviceDefinition, metadata, null, null);

        Map<String, String> systemMetadata = Map.of("a", "1");
        Map<String, String> serviceMetadata = Map.of("b", "2");

        assertTrue(info.matchesPortMetadata(systemMetadata, serviceMetadata));
    }

    @Test
    public void supersetShouldNotMatch() {

        String systemName = "System A";
        String serviceDefinition = "Service A";
        Map<String, String> metadata = Map.of("a", "1", "b", "2");

        var info = new MonitorInfo.Bundle(systemName, serviceDefinition, metadata, null, null);

        Map<String, String> systemMetadata = Map.of("a", "1");
        Map<String, String> serviceMetadata = Map.of("b", "2", "c", "3");

        assertFalse(info.matchesPortMetadata(systemMetadata, serviceMetadata));
    }

    @Test
    public void shouldRequireServiceMetadata() {

        String systemName = "System A";
        String serviceDefinition = "Service A";
        Map<String, String> metadata = Map.of("a", "1");

        var info = new MonitorInfo.Bundle(systemName, serviceDefinition, metadata, null, null);

        Map<String, String> systemMetadata = Map.of("a", "1");
        Map<String, String> serviceMetadata = new HashMap<>();

        assertFalse(info.matchesPortMetadata(systemMetadata, serviceMetadata));
        assertFalse(info.matchesPortMetadata(systemMetadata, null));
    }

    @Test
    public void serviceShouldOverrideSystem() {

        String systemName = "System A";
        String serviceDefinition = "Service A";
        Map<String, String> metadata = Map.of("a", "1");

        var info = new MonitorInfo.Bundle(systemName, serviceDefinition, metadata, null, null);

        Map<String, String> systemMetadata = Map.of("a", "2");
        Map<String, String> serviceMetadata = Map.of("a", "1");

        assertTrue(info.matchesPortMetadata(systemMetadata, serviceMetadata));

        systemMetadata = Map.of("a", "1");
        serviceMetadata = Map.of("a", "2");

        assertFalse(info.matchesPortMetadata(systemMetadata, serviceMetadata));

    }

    @Test
    public void shouldStoreSystemData() {

        Map<String, String> metadata = Map.of("name", "abc");
        ServiceDescription serviceDescription = createServiceDescription(metadata);

        JsonObject jsonObject = new JsonObject(List.of(new JsonPair("a", JsonBoolean.TRUE)));

        var monitorInfo = new MonitorInfo();
        monitorInfo.putSystemData(serviceDescription, jsonObject);

        Map<String, String> lookupMetadata = Map.of("name", "abc");
        var systemInfoList = monitorInfo.getSystemInfo(null, lookupMetadata);
        assertEquals(1, systemInfoList.size());
        var systemInfo = systemInfoList.get(0);
        assertEquals("{[a: true]}", systemInfo.systemData.toString());
    }

    @Test
    public void shouldStoreInventoryId() {
        Map<String, String> metadataA = Map.of("name", "a");
        Map<String, String> metadataB = Map.of("name", "b");
        String systemNameA = "System-a";
        String systemNameB = "System-b";

        var providerA = new ProviderDescription(systemNameA, new InetSocketAddress("0.0.0.0", 5000));
        var serviceA = new ServiceDescription.Builder()
            .name("service-a")
            .provider(providerA)
            .uri("/test")
            .security(SecurityDescriptor.NOT_SECURE)
            .interfaces(List.of(InterfaceDescriptor.HTTP_SECURE_JSON))
            .metadata(metadataA)
            .build();

        var providerB = new ProviderDescription(systemNameB, new InetSocketAddress("0.0.0.0", 5001));
        var serviceB = new ServiceDescription.Builder()
            .name("service-b")
            .provider(providerB)
            .uri("/test")
            .security(SecurityDescriptor.NOT_SECURE)
            .interfaces(List.of(InterfaceDescriptor.HTTP_SECURE_JSON))
            .metadata(metadataB)
            .build();

        String inventoryIdA = "id-A";
        String inventoryIdB = "id-B";

        var monitorInfo = new MonitorInfo();
        monitorInfo.putInventoryId(serviceA, inventoryIdA);
        monitorInfo.putInventoryId(serviceB, inventoryIdB);

        var systemInfoList = monitorInfo.getSystemInfo(serviceB.provider().name(), null);
        assertEquals(1, systemInfoList.size());
        var systemInfo = systemInfoList.get(0);
        assertEquals(inventoryIdB, systemInfo.inventoryId);
    }

    @Test
    public void shouldRetrieveBySystemName() {

        Map<String, String> metadata = Map.of("x", "y");
        ServiceDescription serviceDescription = createServiceDescription(metadata);

        String inventoryId = "id-4567";
        var monitorInfo = new MonitorInfo();
        monitorInfo.putInventoryId(serviceDescription, inventoryId);
        var systemInfoList = monitorInfo.getSystemInfo(serviceDescription.provider().name(), null);
        assertEquals(1, systemInfoList.size());

        var systemInfo = systemInfoList.get(0);
        assertEquals(inventoryId, systemInfo.inventoryId);
    }

    @Test
    public void shouldOverwriteData() {

        ServiceDescription service = createServiceDescription();
        var monitorInfo = new MonitorInfo();

        String oldInventoryId = "id-1234";
        monitorInfo.putInventoryId(service, oldInventoryId);

        String newInventoryId = "id-5678";
        monitorInfo.putInventoryId(service, newInventoryId);

        var systemInfoList = monitorInfo.getSystemInfo(service.provider().name(), null);
        assertEquals(1, systemInfoList.size());

        var systemInfo = systemInfoList.get(0);
        assertEquals(newInventoryId, systemInfo.inventoryId);
    }

    @Test
    public void shouldMergeData() {
        ServiceDescription service = createServiceDescription();
        var monitorInfo = new MonitorInfo();

        String inventoryId = "xyz";
        monitorInfo.putInventoryId(service, inventoryId);

        JsonObject systemData = new JsonObject(List.of(new JsonPair("b", JsonBoolean.FALSE)));

        monitorInfo.putSystemData(service, systemData);

        var systemInfoList = monitorInfo.getSystemInfo(service.provider().name(), null);
        assertEquals(1, systemInfoList.size());

        var systemInfo = systemInfoList.get(0);
        assertEquals(inventoryId, systemInfo.inventoryId);
        assertEquals("{[b: false]}", systemInfo.systemData.toString());
    }
}
