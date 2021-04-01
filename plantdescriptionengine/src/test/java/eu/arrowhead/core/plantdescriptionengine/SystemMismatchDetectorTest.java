package eu.arrowhead.core.plantdescriptionengine;

import eu.arrowhead.core.plantdescriptionengine.alarms.AlarmManager;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.dto.SrSystem;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.dto.SrSystemDto;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.InMemoryPdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStoreException;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PdeSystemDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarmDto;
import eu.arrowhead.core.plantdescriptionengine.utils.MockSystemTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.arkalix.net.http.client.HttpClient;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SystemMismatchDetectorTest {

    private PlantDescriptionTracker pdTracker;
    private MockSystemTracker systemTracker;
    private AlarmManager alarmManager;
    private SystemMismatchDetector detector;

    private SrSystem getSrSystem(final String systemName) {
        return new SrSystemDto.Builder()
            .id(0)
            .systemName(systemName)
            .address("0.0.0.0")
            .port(5000)
            .authenticationInfo(null)
            .createdAt(Instant.now()
                .toString())
            .updatedAt(Instant.now()
                .toString())
            .build();
    }

    private PdeSystemDto getSystem(final String name, final String id) {
        return new PdeSystemDto.Builder()
            .systemId(id)
            .systemName(name)
            .build();
    }

    private PlantDescriptionEntryDto getPdEntry(final String... systemNames) {
        final List<PdeSystemDto> systems = Stream.of(systemNames).map(name ->
            new PdeSystemDto.Builder()
                .systemId(name + "-ID")
                .systemName(name)
                .build()
        ).collect(Collectors.toList());

        return new PlantDescriptionEntryDto.Builder()
            .id(1)
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(systems)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    }

    @BeforeEach
    public void initEach() throws PdStoreException {
        pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());
        final HttpClient httpClient = new HttpClient.Builder().insecure().build();
        systemTracker = new MockSystemTracker(httpClient, new InetSocketAddress("0.0.0.0", 5000));
        alarmManager = new AlarmManager();
        detector = new SystemMismatchDetector(pdTracker, systemTracker, alarmManager);
    }

    @Test
    public void shouldNotReportErrors() throws PdStoreException {
        final String systemName = "System Z";

        pdTracker.put(getPdEntry(systemName));
        systemTracker.addSystem(getSrSystem(systemName));
        detector.run();

        final List<PdeAlarmDto> alarms = alarmManager.getAlarms();
        assertEquals(0, alarms.size());
    }

    /**
     * No alarm should be raised if two systems have the same system name, as
     * long as there is metadata available to differentiate between them.
     */
    // TODO: Add this test
    // @Test
    // public void shouldAllowSameSystemName() throws PdStoreException {

    //     final var systemName = "System A";
    //     final var systemA1 = new PdeSystemDto.Builder()
    //         .systemId("systemA1")
    //         .systemName(systemName)
    //         .metadata(Map.of("x", "1"))
    //         .build();
    //     final var systemA2 = new PdeSystemDto.Builder()
    //         .systemId("systemA2")
    //         .systemName(systemName)
    //         .build();

    //     final var entry = new PlantDescriptionEntryDto.Builder()
    //         .id(1)
    //         .plantDescription("Plant Description 1A")
    //         .active(true)
    //         .systems(List.of(systemA1, systemA2))
    //         .createdAt(Instant.now())
    //         .updatedAt(Instant.now())
    //         .build();

    //     pdTracker.put(entry);
    //     systemTracker.addSystem(getSrSystem(systemName));
    //     detector.run();

    //     final var alarms = alarmManager.getAlarms();
    //     assertEquals(0, alarms.size());
    // }
    @Test
    public void shouldReportNotRegistered() throws PdStoreException {
        detector.run();

        final String systemName = "System A";
        pdTracker.put(getPdEntry(systemName));
        final List<PdeAlarmDto> alarms = alarmManager.getAlarms();
        assertEquals(1, alarms.size());
        final PdeAlarmDto alarm = alarms.get(0);
        assertTrue(alarm.systemName().isPresent());
        assertEquals(systemName, alarm.systemName().get());
        assertFalse(alarm.clearedAt().isPresent());
        assertEquals("warning", alarm.severity());
        assertEquals("System named '" + systemName + "' cannot be found in the Service Registry.", alarm.description());
        assertFalse(alarm.acknowledged());
        assertFalse(alarm.clearedAt().isPresent());
    }

    @Test
    public void shouldRaiseTwoAlarms() throws PdStoreException {
        detector.run();

        final String systemIdA = "Sys-A";
        final String systemNameB = "System B";

        final PdeSystemDto system = new PdeSystemDto.Builder()
            .systemId(systemIdA)
            .metadata(Map.of("x", "1"))
            .build();

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(1)
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(List.of(system))
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        final SrSystemDto srSystem = new SrSystemDto.Builder()
            .id(0)
            .systemName("System B")
            .address("0.0.0.0")
            .port(5000)
            .authenticationInfo(null)
            .createdAt(Instant.now()
                .toString())
            .updatedAt(Instant.now()
                .toString())
            .build();

        systemTracker.addSystem(srSystem);
        pdTracker.put(entry);

        final List<PdeAlarmDto> alarms = alarmManager.getAlarms();
        assertEquals(2, alarms.size());
        final PdeAlarmDto alarm1 = alarms.get(0);
        final PdeAlarmDto alarm2 = alarms.get(1);

        assertEquals("System named '" + systemNameB + "' is not present in the active Plant Description.", alarm1
            .description());
        assertEquals("System with ID '" + systemIdA + "' cannot be found in the Service Registry.", alarm2
            .description());
    }

    @Test
    public void shouldReportSystemNotInPd() throws PdStoreException {

        final String systemNameA = "System A";
        final String systemNameB = "System B";

        // Create a plant description that only specifies System A.
        pdTracker.put(getPdEntry(systemNameA));
        systemTracker.addSystem(getSrSystem(systemNameA));
        systemTracker.addSystem(getSrSystem(systemNameB));

        detector.run();

        final List<PdeAlarmDto> alarms = alarmManager.getAlarms();
        assertEquals(1, alarms.size());
        final PdeAlarmDto alarm = alarms.get(0);
        assertTrue(alarm.systemName().isPresent());
        assertEquals(systemNameB, alarm.systemName().get());
        assertFalse(alarm.clearedAt().isPresent());
        assertEquals("warning", alarm.severity());
        assertEquals("System named '" + systemNameB + "' is not present in the active Plant Description.",
            alarm.description());
        assertFalse(alarm.acknowledged());
        assertFalse(alarm.clearedAt().isPresent());
    }

    @Test
    public void shouldClearWhenSystemIsRegistered() throws PdStoreException {

        final String systemNameA = "System A";
        final String systemNameB = "System B";

        final PdeSystemDto systemA = getSystem(systemNameA, "a");
        final PdeSystemDto systemB = getSystem(systemNameB, "b");

        final PlantDescriptionEntryDto pdeEntry = new PlantDescriptionEntryDto.Builder()
            .id(1)
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(List.of(systemA, systemB))
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        pdTracker.put(pdeEntry);
        systemTracker.addSystem(getSrSystem(systemNameA));

        // System B is missing, an alarm is created.
        detector.run();

        // System B is added, so the alarm should be cleared.
        systemTracker.addSystem(getSrSystem(systemNameB));

        final List<PdeAlarmDto> alarms = alarmManager.getAlarms();
        assertEquals(1, alarms.size());
        final PdeAlarmDto alarm = alarms.get(0);
        assertTrue(alarm.systemName().isPresent());
        assertEquals(systemNameB, alarm.systemName().get());
        assertTrue(alarm.clearedAt().isPresent());
        assertEquals("cleared", alarm.severity());
        assertEquals("System named '" + systemNameB + "' cannot be found in the Service Registry.",
            alarm.description());
        assertFalse(alarm.acknowledged());
    }

    @Test
    public void shouldStillReportIfOneIsCleared() throws PdStoreException {

        final String systemNameA = "System A";
        final String systemNameB = "System B";

        final PlantDescriptionEntryDto pdeEntry = new PlantDescriptionEntryDto.Builder()
            .id(1)
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems()
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        pdTracker.put(pdeEntry);
        systemTracker.addSystem(getSrSystem(systemNameA));
        systemTracker.addSystem(getSrSystem(systemNameB));

        // System A and B are missing from PD, two alarms are created.
        detector.run();

        // System B is removed from the Service Registry, so one of the alarms
        // should be cleared.
        systemTracker.remove(systemNameB);

        final List<PdeAlarmDto> alarms = alarmManager.getAlarms();
        assertEquals(2, alarms.size());
        final PdeAlarmDto alarmA = alarms.get(0);
        final PdeAlarmDto alarmB = alarms.get(1);

        assertTrue(alarmA.systemName().isPresent());
        assertEquals(systemNameA, alarmA.systemName().get());
        assertTrue(alarmA.clearedAt().isEmpty());
        assertTrue(alarmB.systemName().isPresent());
        assertEquals(systemNameB, alarmB.systemName().get());
        assertTrue(alarmB.clearedAt().isPresent());
    }

    @Test
    public void shouldClearWhenPdIsRemoved() throws PdStoreException {
        detector.run();

        final String systemName = "System C";
        final PlantDescriptionEntryDto entry = getPdEntry(systemName);
        pdTracker.put(entry);
        pdTracker.remove(entry.id());

        final List<PdeAlarmDto> alarms = alarmManager.getAlarms();
        assertEquals(1, alarms.size());
        final PdeAlarmDto alarm = alarms.get(0);
        assertTrue(alarm.systemName().isPresent());
        assertEquals(systemName, alarm.systemName().get());
        assertTrue(alarm.clearedAt().isPresent());
        assertEquals("cleared", alarm.severity());
        assertEquals("System named '" + systemName + "' cannot be found in the Service Registry.", alarm.description());
        assertFalse(alarm.acknowledged());
    }

    @Test
    public void shouldClearWhenPdIsAdded() throws PdStoreException {
        detector.run();

        final String systemName = "System D";

        systemTracker.addSystem(getSrSystem(systemName));
        pdTracker.put(getPdEntry(systemName));

        final List<PdeAlarmDto> alarms = alarmManager.getAlarms();
        assertEquals(1, alarms.size());
        final PdeAlarmDto alarm = alarms.get(0);
        assertTrue(alarm.systemName().isPresent());
        assertEquals(systemName, alarm.systemName().get());
        assertTrue(alarm.clearedAt().isPresent());
        assertEquals("cleared", alarm.severity());
        assertEquals("System named '" + systemName + "' is not present in the active Plant Description.",
            alarm.description());
        assertFalse(alarm.acknowledged());
    }

    @Test
    public void shouldClearWhenSystemIsRemoved() {
        detector.run();

        final String systemName = "System D";

        systemTracker.addSystem(getSrSystem(systemName));
        systemTracker.remove(systemName);

        final List<PdeAlarmDto> alarms = alarmManager.getAlarms();
        assertEquals(1, alarms.size());
        final PdeAlarmDto alarm = alarms.get(0);
        assertTrue(alarm.systemName().isPresent());
        assertEquals(systemName, alarm.systemName().get());
        assertTrue(alarm.clearedAt().isPresent());
        assertEquals("cleared", alarm.severity());
        assertEquals("System named '" + systemName + "' is not present in the active Plant Description.",
            alarm.description());
        assertFalse(alarm.acknowledged());
    }

    @Test
    public void shouldClearWhenPdIsUpdated() throws PdStoreException {

        // The systems added in the PD and Service Registry are crafted to
        // tickle several corners of the codebase. More specifically, System C
        // is not significant for the outcome of the test, but it helps improve
        // code coverage.

        final String systemNameA = "System A";
        final String systemNameB = "System B";
        final String systemNameC = "System C";

        final SrSystemDto srSystemA = new SrSystemDto.Builder()
            .id(0)
            .systemName(systemNameA)
            .metadata(Map.of("x", "1", "y", "2"))
            .address("0.0.0.1")
            .port(5003)
            .authenticationInfo(null)
            .createdAt(Instant.now()
                .toString())
            .updatedAt(Instant.now()
                .toString())
            .build();

        final SrSystemDto srSystemC = new SrSystemDto.Builder()
            .id(0)
            .systemName(systemNameC)
            .metadata(Map.of("x", "1", "y", "2", "z", "3"))
            .address("0.0.0.3")
            .port(5003)
            .authenticationInfo(null)
            .createdAt(Instant.now()
                .toString())
            .updatedAt(Instant.now()
                .toString())
            .build();

        final SrSystem srSystemB = getSrSystem(systemNameB);

        final PdeSystemDto systemA = getSystem(systemNameA, "a");
        final PdeSystemDto systemB = new PdeSystemDto.Builder()
            .systemId("Sys-B")
            .systemName(systemNameB)
            .build();

        // System with metadata that matches System C in the service registry,
        // but not System A (whose metadata is only a subset).
        final PdeSystemDto systemC = new PdeSystemDto.Builder()
            .systemId("Sys-C")
            .metadata(Map.of("x", "1", "y", "2", "z", "3"))
            .build();

        final PlantDescriptionEntryDto entryWithTwoSystems = new PlantDescriptionEntryDto.Builder()
            .id(1)
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(List.of(systemB, systemC))
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        pdTracker.put(entryWithTwoSystems);

        systemTracker.addSystem(srSystemC);
        systemTracker.addSystem(srSystemB);
        systemTracker.addSystem(srSystemA);

        detector.run();

        final PlantDescriptionEntryDto entryWithThreeSystems = new PlantDescriptionEntryDto.Builder().id(1)
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(List.of(systemB, systemC, systemA))
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        pdTracker.put(entryWithThreeSystems);

        final List<PdeAlarmDto> alarms = alarmManager.getAlarms();
        assertEquals(1, alarms.size());
        final PdeAlarmDto alarm = alarms.get(0);
        assertTrue(alarm.systemName().isPresent());
        assertEquals(systemNameA, alarm.systemName().get());
        assertTrue(alarm.clearedAt().isPresent());
        assertEquals("cleared", alarm.severity());
        assertEquals("System named '" + systemNameA + "' is not present in the active Plant Description.",
            alarm.description());
        assertFalse(alarm.acknowledged());
    }

    @Test
    public void shouldClearWhenPdIsUpdatedWithMetadata() throws PdStoreException {

        final String systemNameA = "System A";

        final PlantDescriptionEntryDto entryWithOneSystem = new PlantDescriptionEntryDto.Builder().id(1)
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(List.of(getSystem(systemNameA, "a")))
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        pdTracker.put(entryWithOneSystem);

        final String systemNameB = "System B";
        final SrSystemDto srSystemB = new SrSystemDto.Builder()
            .id(38)
            .systemName(systemNameB)
            .address("0.0.2.1")
            .port(5022)
            .authenticationInfo(null)
            .metadata(Map.of("a", "1", "b", "2"))
            .createdAt(Instant.now()
                .toString())
            .updatedAt(Instant.now()
                .toString())
            .build();

        systemTracker.addSystem(getSrSystem(systemNameA));
        systemTracker.addSystem(srSystemB);

        // An unnamed system is missing from the PD.

        detector.run();

        final PdeSystemDto systemB = new PdeSystemDto.Builder()
            .systemId("b")
            .metadata(Map.of("b", "2")) // Subset of the SR system metadata
            .build();

        final PlantDescriptionEntryDto entryWithTwoSystems = new PlantDescriptionEntryDto.Builder().id(1)
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(List.of(getSystem(systemNameA, "a"), systemB))
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        pdTracker.put(entryWithTwoSystems);

        // The unnamed system is no longer missing from the active PD.

        final List<PdeAlarmDto> alarms = alarmManager.getAlarms();
        assertEquals(1, alarms.size());
        final PdeAlarmDto alarm = alarms.get(0);
        assertTrue(alarm.systemName().isPresent());
        assertEquals(systemNameB, alarm.systemName().get());
        assertTrue(alarm.clearedAt().isPresent());
        assertEquals("cleared", alarm.severity());
        assertEquals("System named '" + systemNameB + "' is not present in the active Plant Description.",
            alarm.description());
        assertFalse(alarm.acknowledged());
    }

    @Test
    public void shouldReportWhenSystemCannotBeUniquelyIdentified() throws PdStoreException {

        final String systemId = "System X";
        final PdeSystemDto systemA = new PdeSystemDto.Builder()
            .systemId(systemId)
            .metadata(Map.of("a", "1", "b", "2"))
            .build();

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder().id(1)
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(List.of(systemA))
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        // System with matching metadata:
        final SrSystemDto srSystemA = new SrSystemDto.Builder()
            .id(0)
            .systemName("System A")
            .metadata(Map.of("a", "1", "b", "2", "c", "3"))
            .address("0.0.0.0")
            .port(5000)
            .authenticationInfo(null)
            .createdAt(Instant.now()
                .toString())
            .updatedAt(Instant.now()
                .toString())
            .build();

        // Another system with matching metadata:
        final SrSystemDto srSystemB = new SrSystemDto.Builder()
            .id(0)
            .systemName("System B")
            .metadata(Map.of("a", "1", "b", "2", "d", "4"))
            .address("0.0.0.1")
            .port(5001)
            .authenticationInfo(null)
            .createdAt(Instant.now()
                .toString())
            .updatedAt(Instant.now()
                .toString())
            .build();

        pdTracker.put(entry);
        systemTracker.addSystem(srSystemA);
        systemTracker.addSystem(srSystemB);
        detector.run();

        final List<PdeAlarmDto> alarms = alarmManager.getAlarms();
        assertEquals(1, alarms.size());
        final PdeAlarmDto alarm = alarms.get(0);
        assertTrue(alarm.systemName().isEmpty());
        assertTrue(alarm.clearedAt().isEmpty());
        assertEquals("warning", alarm.severity());
        assertEquals("System with ID '" + systemId + "' cannot be uniquely identified in the Service Registry.",
            alarm.description());
        assertFalse(alarm.acknowledged());

    }

    @Test
    public void shouldClearWhenSystemBecomesUnique() throws PdStoreException {

        final String systemId = "System X";
        final PdeSystemDto systemA = new PdeSystemDto.Builder()
            .systemId(systemId)
            .metadata(Map.of("a", "1", "b", "2"))
            .build();

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder().id(1)
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(List.of(systemA))
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        // System with matching metadata:
        final Map<String, String> systemAMetadata = Map.of("a", "1", "b", "2", "c", "3");
        final SrSystemDto srSystemA = new SrSystemDto.Builder()
            .id(0)
            .systemName("System A")
            .metadata(systemAMetadata)
            .address("0.0.0.0")
            .port(5000)
            .authenticationInfo(null)
            .createdAt(Instant.now()
                .toString())
            .updatedAt(Instant.now()
                .toString())
            .build();

        // Another system with matching metadata:
        final SrSystemDto srSystemB = new SrSystemDto.Builder()
            .id(0)
            .systemName("System B")
            .metadata(Map.of("a", "1", "b", "2", "d", "4"))
            .address("0.0.0.1")
            .port(5001)
            .authenticationInfo(null)
            .createdAt(Instant.now()
                .toString())
            .updatedAt(Instant.now()
                .toString())
            .build();

        pdTracker.put(entry);
        systemTracker.addSystem(srSystemA);
        systemTracker.addSystem(srSystemB);
        detector.run();
        systemTracker.remove(srSystemA.systemName(), systemAMetadata);

        final List<PdeAlarmDto> alarms = alarmManager.getAlarms();
        assertEquals(1, alarms.size());
        final PdeAlarmDto alarm = alarms.get(0);
        assertTrue(alarm.systemName().isEmpty());
        assertTrue(alarm.clearedAt().isPresent());
        assertEquals("cleared", alarm.severity());
        assertEquals("System with ID '" + systemId + "' cannot be uniquely identified in the Service Registry.",
            alarm.description());
        assertFalse(alarm.acknowledged());

    }
}