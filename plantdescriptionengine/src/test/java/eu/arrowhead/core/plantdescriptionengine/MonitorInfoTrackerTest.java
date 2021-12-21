package eu.arrowhead.core.plantdescriptionengine;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.arkalix.ServiceInterface;
import se.arkalix.ServiceRecord;
import se.arkalix.SystemRecord;
import se.arkalix._internal.DefaultSystemRecord;
import se.arkalix.codec.json.JsonBoolean;
import se.arkalix.codec.json.JsonObject;
import se.arkalix.codec.json.JsonPair;
import se.arkalix.security.access.AccessPolicyType;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;


public class MonitorInfoTrackerTest {

    private final String providerSystemName = "Provider-system";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ServiceRecord createServiceRecord(final Map<String, String> systemMetadata) {

        final SystemRecord provider = new DefaultSystemRecord(
            providerSystemName,
            null,
            new InetSocketAddress("0.0.0.0", 5000),
            systemMetadata);

        return new ServiceRecord.Builder()
            .name("service-a")
            .provider(provider)
            .uri("/test")
            .accessPolicyType(AccessPolicyType.NOT_SECURE)
            .interfaces(ServiceInterface.HTTP_SECURE_JSON)
            .build();
    }

    private ServiceRecord createServiceRecord() {
        return createServiceRecord(new HashMap<>());
    }

    @Test
    public void shouldStoreSystemData() {

        final Map<String, String> metadata = Map.of("name", "abc");
        final ServiceRecord ServiceRecord = createServiceRecord(metadata);

        final JsonObject systemData = new JsonObject(new JsonPair("a", JsonBoolean.TRUE));
        final MonitorInfoTracker monitorInfoTracker = new MonitorInfoTracker();
        monitorInfoTracker.putSystemData(ServiceRecord, systemData);

        final List<MonitorInfo> systemInfoList = monitorInfoTracker.getSystemInfo(providerSystemName, null);
        assertEquals(1, systemInfoList.size());
        final MonitorInfo systemInfo = systemInfoList.get(0);
        assertEquals("{[a: true]}", systemInfo.systemData.toString());
    }

    @Test
    public void shouldRetrieveSystemDataWithMetadata() {

        final Map<String, String> metadata = Map.of("name", "abc");
        final ServiceRecord ServiceRecord = createServiceRecord(metadata);

        final JsonObject systemData = new JsonObject(new JsonPair("a", JsonBoolean.TRUE));
        final MonitorInfoTracker monitorInfoTracker = new MonitorInfoTracker();
        monitorInfoTracker.putSystemData(ServiceRecord, systemData);

        final List<MonitorInfo> systemInfoList = monitorInfoTracker.getSystemInfo(null, metadata);
        assertEquals(1, systemInfoList.size());
        final MonitorInfo systemInfo = systemInfoList.get(0);
        assertEquals("{[a: true]}", systemInfo.systemData.toString());
    }

    @Test
    public void shouldStoreInventoryId() {
        final Map<String, String> metadataA = Map.of("name", "a");
        final Map<String, String> metadataB = Map.of("name", "b");
        final String systemNameA = "System-a";
        final String systemNameB = "System-b";

        final SystemRecord providerA = SystemRecord.from(systemNameA, new InetSocketAddress("0.0.0.0", 5000));
        final ServiceRecord serviceA = new ServiceRecord.Builder()
            .name("service-a")
            .provider(providerA)
            .uri("/test")
            .accessPolicyType(AccessPolicyType.NOT_SECURE)
            .interfaces(ServiceInterface.HTTP_SECURE_JSON)
            .metadata(metadataA)
            .build();

        final SystemRecord providerB = SystemRecord.from(systemNameB, new InetSocketAddress("0.0.0.0", 5001));
        final ServiceRecord serviceB = new ServiceRecord.Builder()
            .name("service-b")
            .provider(providerB)
            .uri("/test")
            .accessPolicyType(AccessPolicyType.NOT_SECURE)
            .interfaces(ServiceInterface.HTTP_SECURE_JSON)
            .metadata(metadataB)
            .build();

        final String inventoryIdA = "id-A";
        final String inventoryIdB = "id-B";

        final MonitorInfoTracker monitorInfoTracker = new MonitorInfoTracker();
        monitorInfoTracker.putInventoryId(serviceA, inventoryIdA);
        monitorInfoTracker.putInventoryId(serviceB, inventoryIdB);

        final List<MonitorInfo> systemInfoList = monitorInfoTracker.getSystemInfo(serviceB.provider().name(), null);
        assertEquals(1, systemInfoList.size());
        final MonitorInfo systemInfo = systemInfoList.get(0);
        assertEquals(inventoryIdB, systemInfo.inventoryId);
    }

    @Test
    public void shouldRetrieveBySystemName() {

        final Map<String, String> metadata = Map.of("x", "y");
        final ServiceRecord ServiceRecord = createServiceRecord(metadata);

        final String inventoryId = "id-4567";
        final MonitorInfoTracker monitorInfo = new MonitorInfoTracker();
        monitorInfo.putInventoryId(ServiceRecord, inventoryId);
        final List<MonitorInfo> systemInfoList = monitorInfo.getSystemInfo(ServiceRecord.provider().name(), null);
        assertEquals(1, systemInfoList.size());

        final MonitorInfo systemInfo = systemInfoList.get(0);
        assertEquals(inventoryId, systemInfo.inventoryId);
    }

    @Test
    public void shouldOverwriteData() {

        final ServiceRecord service = createServiceRecord();
        final MonitorInfoTracker monitorInfoTracker = new MonitorInfoTracker();

        final String oldInventoryId = "id-1234";
        monitorInfoTracker.putInventoryId(service, oldInventoryId);

        final String newInventoryId = "id-5678";
        monitorInfoTracker.putInventoryId(service, newInventoryId);

        final List<MonitorInfo> systemInfoList = monitorInfoTracker.getSystemInfo(service.provider().name(), null);
        assertEquals(1, systemInfoList.size());

        final MonitorInfo systemInfo = systemInfoList.get(0);
        assertEquals(newInventoryId, systemInfo.inventoryId);
    }

    @Test
    public void shouldThrowWhenArgsAreMissing() {
        final MonitorInfoTracker monitorInfoTracker = new MonitorInfoTracker();
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Either system name or metadata must be present.");
        monitorInfoTracker.getSystemInfo(null, null);
    }
}
