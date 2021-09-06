package eu.arrowhead.core.plantdescriptionengine;

import eu.arrowhead.core.plantdescriptionengine.alarms.Alarm;
import eu.arrowhead.core.plantdescriptionengine.alarms.AlarmCause;
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
import org.junit.Before;
import org.junit.Test;
import se.arkalix.net.http.client.HttpClient;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

    @Before
    public void initEach() throws PdStoreException {
        pdTracker = new PlantDescriptionTracker(new InMemoryPdStore());
        final HttpClient httpClient = new HttpClient.Builder().insecure().build();
        systemTracker = new MockSystemTracker(httpClient, new InetSocketAddress("0.0.0.0", 5000));
        alarmManager = new AlarmManager();
        detector = new SystemMismatchDetector(pdTracker, systemTracker, alarmManager);
    }

    @Test
    public void shouldNotReportErrors() {
        final String systemName = "System Z";

        pdTracker.put(getPdEntry(systemName))
            .ifSuccess(result -> {
                systemTracker.addSystem(getSrSystem(systemName));
                detector.run();

                final List<PdeAlarmDto> alarms = alarmManager.getAlarms();
                assertEquals(0, alarms.size());
            })
            .onFailure(e -> fail());
    }

    /**
     * No alarm should be raised if two systems have the same system name, as
     * long as there is metadata available to differentiate between them.
     */
    @Test
    public void shouldAllowSameSystemName() {

        Map<String, String> metadataA = Map.of("x", "1");
        Map<String, String> metadataB = Map.of("y", "1");

        final String systemName = "systemabc";
        final PdeSystemDto systemA1 = new PdeSystemDto.Builder()
            .systemId("systemA1")
            .systemName(systemName)
            .metadata(metadataA)
            .build();
        final PdeSystemDto systemA2 = new PdeSystemDto.Builder()
            .systemId("systemA2")
            .systemName(systemName)
            .metadata(metadataB)
            .build();

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(1)
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(List.of(systemA1, systemA2))
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        Instant now = Instant.now();

        SrSystem srSystemA = new SrSystemDto.Builder()
            .id(0)
            .systemName(systemName)
            .address("0.0.0.0")
            .port(5000)
            .metadata(metadataA)
            .createdAt(now.toString())
            .updatedAt(now.toString())
            .build();
        SrSystem srSystemB = new SrSystemDto.Builder()
            .id(1)
            .systemName(systemName)
            .address("0.0.0.1")
            .port(5001)
            .metadata(metadataB)
            .createdAt(now.toString())
            .updatedAt(now.toString())
            .build();

        pdTracker.put(entry)
            .ifSuccess(result -> {
                systemTracker.addSystem(srSystemA);
                systemTracker.addSystem(srSystemB);
                detector.run();

                final List<PdeAlarmDto> alarms = alarmManager.getAlarms();
                assertEquals(0, alarms.size());
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldReportNotRegistered() {
        detector.run();

        final String systemName = "systemabc";
        pdTracker.put(getPdEntry(systemName))
            .ifSuccess(result -> {
                final List<Alarm> alarms = alarmManager.getActiveAlarmData(AlarmCause.SYSTEM_NOT_REGISTERED);
                assertEquals(1, alarms.size());
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldRaiseTwoAlarms() {
        detector.run();

        final String systemIdA = "Sys-A";
        final String systemNameB = "systemxyz";

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
            .systemName(systemNameB)
            .address("0.0.0.0")
            .port(5000)
            .authenticationInfo(null)
            .createdAt(Instant.now()
                .toString())
            .updatedAt(Instant.now()
                .toString())
            .build();

        systemTracker.addSystem(srSystem);
        pdTracker.put(entry)
            .ifSuccess(result -> {
                final List<PdeAlarmDto> alarms = alarmManager.getAlarms();
                assertEquals(2, alarms.size());
                final PdeAlarmDto alarm1 = alarms.get(0);
                final PdeAlarmDto alarm2 = alarms.get(1);

                assertEquals("System named '" + systemNameB + "' is not present in the active Plant Description.", alarm1
                    .description());
                assertEquals("System with ID '" + systemIdA + "' cannot be found in the Service Registry.", alarm2
                    .description());
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldReportSystemNotInPd() {

        final String systemNameA = "systemabc";
        final String systemNameB = "systemxyz";

        // Create a plant description that only specifies systemabc.
        pdTracker.put(getPdEntry(systemNameA))
            .ifSuccess(result -> {
                systemTracker.addSystem(getSrSystem(systemNameA));
                systemTracker.addSystem(getSrSystem(systemNameB));

                detector.run();

                final List<Alarm> alarms = alarmManager.getActiveAlarmData(AlarmCause.SYSTEM_NOT_IN_DESCRIPTION);
                assertEquals(1, alarms.size());
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldClearWhenSystemIsRegistered() {

        final String systemNameA = "systemabc";
        final String systemNameB = "systemxyz";

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

        pdTracker.put(pdeEntry)
            .ifSuccess(result -> {
                systemTracker.addSystem(getSrSystem(systemNameA));
                detector.run();
                assertEquals(1, alarmManager.getActiveAlarmData(AlarmCause.SYSTEM_NOT_REGISTERED).size());
                systemTracker.addSystem(getSrSystem(systemNameB));
                assertEquals(0, alarmManager.getActiveAlarmData(AlarmCause.SYSTEM_NOT_REGISTERED).size());
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldStillReportIfOneIsCleared() {

        final String systemNameA = "systemabc";
        final String systemNameB = "systemxyz";

        final PlantDescriptionEntryDto pdeEntry = new PlantDescriptionEntryDto.Builder()
            .id(1)
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems()
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        pdTracker.put(pdeEntry)
            .ifSuccess(result -> {
                systemTracker.addSystem(getSrSystem(systemNameA));
                systemTracker.addSystem(getSrSystem(systemNameB));

                // Two systems are missing from PD, two alarms are created.
                detector.run();

                // One system is removed from the Service Registry, so one of the alarms
                // should be cleared.
                systemTracker.remove(systemNameB);

                final List<PdeAlarmDto> alarms = alarmManager.getAlarms();
                assertEquals(2, alarms.size());
                final PdeAlarmDto alarmA = alarms.get(0);
                final PdeAlarmDto alarmB = alarms.get(1);

                assertEquals(systemNameA, alarmA.systemName().orElse(null));
                assertTrue(alarmA.clearedAt().isEmpty());
                assertEquals(systemNameB, alarmB.systemName().orElse(null));
                assertTrue(alarmB.clearedAt().isPresent());
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldClearWhenPdIsRemoved() {
        detector.run();

        final String systemName = "System C";
        final PlantDescriptionEntryDto entry = getPdEntry(systemName);
        pdTracker.put(entry)
            .flatMap(result -> {
                assertEquals(1, alarmManager.getActiveAlarmData(AlarmCause.SYSTEM_NOT_REGISTERED).size());
                return pdTracker.remove(entry.id());
            })
            .ifSuccess(result -> assertEquals(0, alarmManager.getActiveAlarmData(AlarmCause.SYSTEM_NOT_REGISTERED).size()))
            .onFailure(e -> fail());
    }

    @Test
    public void shouldClearWhenPdIsAdded() {
        detector.run();
        final String systemName = "System D";
        systemTracker.addSystem(getSrSystem(systemName));
        assertEquals(1, alarmManager.getActiveAlarmData(AlarmCause.SYSTEM_NOT_IN_DESCRIPTION).size());
        pdTracker.put(getPdEntry(systemName))
            .ifSuccess(result -> assertEquals(0, alarmManager.getActiveAlarmData(AlarmCause.SYSTEM_NOT_IN_DESCRIPTION).size()))
            .onFailure(e -> fail());
    }

    @Test
    public void shouldClearWhenSystemIsRemoved() {
        detector.run();
        final String systemName = "System D";
        systemTracker.addSystem(getSrSystem(systemName));
        assertEquals(1, alarmManager.getActiveAlarmData(AlarmCause.SYSTEM_NOT_IN_DESCRIPTION).size());
        systemTracker.remove(systemName);
        assertEquals(0, alarmManager.getActiveAlarmData(AlarmCause.SYSTEM_NOT_IN_DESCRIPTION).size());
    }

    @Test
    public void shouldClearWhenPdIsUpdated() {

        // The systems added in the PD and Service Registry are crafted to
        // tickle several corners of the codebase. More specifically, System C
        // is not significant for the outcome of the test, but it helps improve
        // code coverage.

        final String systemNameA = "systemabc";
        final String systemNameB = "systemxyz";
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

        pdTracker.put(entryWithTwoSystems)
            .ifSuccess(result -> {
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

                assertEquals(1, alarmManager.getActiveAlarmData(AlarmCause.SYSTEM_NOT_IN_DESCRIPTION).size());
                pdTracker.put(entryWithThreeSystems);
                assertEquals(0, alarmManager.getActiveAlarmData(AlarmCause.SYSTEM_NOT_IN_DESCRIPTION).size());
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldClearWhenPdIsUpdatedWithMetadata() {
        final Instant now = Instant.now();
        final String systemNameA = "systemabc";

        final PlantDescriptionEntryDto entryWithOneSystem = new PlantDescriptionEntryDto.Builder().id(1)
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(List.of(getSystem(systemNameA, "a")))
            .createdAt(now)
            .updatedAt(now)
            .build();

        pdTracker.put(entryWithOneSystem)
            .flatMap(result -> {
                final String systemNameB = "systemxyz";
                final SrSystemDto srSystemB = new SrSystemDto.Builder()
                    .id(38)
                    .systemName(systemNameB)
                    .address("0.0.2.1")
                    .port(5022)
                    .authenticationInfo(null)
                    .metadata(Map.of("a", "1", "b", "2"))
                    .createdAt(now.toString())
                    .updatedAt(now.toString())
                    .build();

                systemTracker.addSystem(getSrSystem(systemNameA));
                systemTracker.addSystem(srSystemB);

                detector.run();

                final PdeSystemDto namedSystem = getSystem(systemNameA, "a");
                final PdeSystemDto unnamedSystem = new PdeSystemDto.Builder()
                    .systemId("b")
                    .metadata(Map.of("b", "2")) // Subset of the SR system metadata
                    .build();

                final PlantDescriptionEntryDto entryWithBothSystems = new PlantDescriptionEntryDto.Builder().id(1)
                    .plantDescription("Plant Description 1A")
                    .active(true)
                    .systems(List.of(namedSystem, unnamedSystem))
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

                assertEquals(1, alarmManager.getActiveAlarmData(AlarmCause.SYSTEM_NOT_IN_DESCRIPTION).size());
                return pdTracker.put(entryWithBothSystems);
            })
            .ifSuccess(result -> assertEquals(0, alarmManager.getActiveAlarmData(AlarmCause.SYSTEM_NOT_IN_DESCRIPTION).size()))
            .onFailure(e -> fail());
    }

    @Test
    public void shouldReportWhenSystemCannotBeUniquelyIdentified() {

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
            .systemName("systemabc")
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
            .systemName("systemxyz")
            .metadata(Map.of("a", "1", "b", "2", "d", "4"))
            .address("0.0.0.1")
            .port(5001)
            .authenticationInfo(null)
            .createdAt(Instant.now()
                .toString())
            .updatedAt(Instant.now()
                .toString())
            .build();

        pdTracker.put(entry)
            .ifSuccess(result -> {
                systemTracker.addSystem(srSystemA);
                systemTracker.addSystem(srSystemB);
                detector.run();

                assertEquals(1, alarmManager.getActiveAlarmData(AlarmCause.MULTIPLE_MATCHES).size());
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldClearWhenSystemBecomesUnique() {

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
            .systemName("systemabc")
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
            .systemName("systemxyz")
            .metadata(Map.of("a", "1", "b", "2", "d", "4"))
            .address("0.0.0.1")
            .port(5001)
            .authenticationInfo(null)
            .createdAt(Instant.now()
                .toString())
            .updatedAt(Instant.now()
                .toString())
            .build();

        pdTracker.put(entry)
            .ifSuccess(result -> {
                systemTracker.addSystem(srSystemA);
                systemTracker.addSystem(srSystemB);
                detector.run();

                assertEquals(1, alarmManager.getActiveAlarmData(AlarmCause.MULTIPLE_MATCHES).size());
                systemTracker.remove(srSystemA.systemName(), systemAMetadata);
                assertEquals(0, alarmManager.getActiveAlarmData(AlarmCause.MULTIPLE_MATCHES).size());
            })
            .onFailure(e -> fail());
    }
}