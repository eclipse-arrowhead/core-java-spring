package eu.arrowhead.core.plantdescriptionengine;

import eu.arrowhead.core.plantdescriptionengine.alarms.Alarm;
import eu.arrowhead.core.plantdescriptionengine.alarms.AlarmCause;
import eu.arrowhead.core.plantdescriptionengine.alarms.AlarmManager;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.SystemTracker;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.SystemUpdateListener;
import eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.dto.SrSystem;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionUpdateListener;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PdeSystem;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntry;
import eu.arrowhead.core.plantdescriptionengine.utils.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SystemMismatchDetector implements PlantDescriptionUpdateListener, SystemUpdateListener {

    private static final Logger logger = LoggerFactory.getLogger(SystemMismatchDetector.class);

    private final PlantDescriptionTracker pdTracker;
    private final SystemTracker systemTracker;
    private final AlarmManager alarmManager;

    public SystemMismatchDetector(
        final PlantDescriptionTracker pdTracker,
        final SystemTracker systemTracker,
        final AlarmManager alarmManager
    ) {
        Objects.requireNonNull(pdTracker, "Expected Plant Description Tracker");
        Objects.requireNonNull(systemTracker, "Expected System Tracker");
        Objects.requireNonNull(alarmManager, "Expected Alarm Manager");

        this.pdTracker = pdTracker;
        this.systemTracker = systemTracker;
        this.alarmManager = alarmManager;
    }

    /**
     * Start monitoring Plant Descriptions and registered systems, raising
     * alarms whenever there is a mismatch, and clearing alarms when issues are
     * solved.
     */
    public void run() {
        pdTracker.addListener(this);
        systemTracker.addListener(this);

        // Initial check for mismatches:
        updateAlarms();
    }

    @Override
    public void onPlantDescriptionAdded(final PlantDescriptionEntry entry) {
        Objects.requireNonNull(entry, "Expected entry.");
        logger.debug("Entry '" + entry.plantDescription() + "' added, checking for inconsistencies...");
        updateAlarms();
    }

    @Override
    public void onPlantDescriptionUpdated(
        final PlantDescriptionEntry updatedEntry,
        final PlantDescriptionEntry oldEntry
    ) {
        Objects.requireNonNull(updatedEntry, "Expected entry.");
        logger.debug("Entry '" + updatedEntry.plantDescription() + "' updated, checking for inconsistencies...");
        updateAlarms();
    }

    @Override
    public void onPlantDescriptionRemoved(final PlantDescriptionEntry entry) {
        Objects.requireNonNull(entry, "Expected entry.");
        logger.debug("Entry '" + entry.plantDescription() + "' removed, checking for inconsistencies...");
        updateAlarms();
    }

    @Override
    public void onSystemAdded(final SrSystem system) {
        Objects.requireNonNull(system, "Expected system.");
        logger.debug("System '" + system.systemName() + "' added, checking for inconsistencies...");
        updateAlarms();
    }

    @Override
    public void onSystemRemoved(final SrSystem system) {
        Objects.requireNonNull(system, "Expected system.");
        logger.debug("System '" + system.systemName() + "' removed, checking for inconsistencies...");
        updateAlarms();
    }

    /**
     * @param entrySystem      A system in a Plant Description Entry.
     * @param registeredSystem A system retrieved from the Service registry.
     * @return True if the two objects represent the same real-world system,
     * false otherwise.
     */
    private boolean systemsMatch(final PdeSystem entrySystem, final SrSystem registeredSystem) {

        if (!Metadata.isSubset(entrySystem.metadata(), registeredSystem.metadata())) {
            return false;
        }

        // At this point, we know that either of these two statements must hold:
        // a) The entry system has metadata which matches that of the SR system.
        // b) The entry system has no metadata, in which case it *must* have a
        //    systemName (as per the spec).

        if (entrySystem.systemName().isPresent()) {
            final String entryName = entrySystem.systemName().get();
            return entryName.equals(registeredSystem.systemName());
        }

        return true;
    }

    /**
     * @param alarm  An alarm.
     * @param system A system retrieved from the Service registry.
     * @return True if the alarm refers to the given system, false otherwise.
     */
    private boolean alarmMatchesSrSystem(final Alarm alarm, final SrSystem system) {

        final String alarmSystemName = alarm.getSystemName();
        final Map<String, String> alarmMetadata = alarm.getMetadata();
        final boolean namesMatch = alarmSystemName != null && alarmSystemName.equals(system.systemName());
        final boolean metadataMatches = Metadata.isSubset(alarmMetadata, system.metadata());

        if (alarmSystemName != null && !namesMatch) {
            return false;
        }

        if (!alarmMetadata.isEmpty() && !metadataMatches) {
            return false;
        }

        return namesMatch || metadataMatches;
    }

    /**
     * @param alarm  An alarm.
     * @param system A system as described by a Plant Description Entry.
     * @return True if the alarm refers to the given system, false otherwise.
     */
    private boolean alarmMatchesPdSystem(final Alarm alarm, final PdeSystem system) {

        String alarmSystemName = alarm.getSystemName();
        if (alarmSystemName != null && system.systemName().isPresent()) {
            if (!alarmSystemName.equals(system.systemName().get())) {
                return false;
            }
        }

        Map<String, String> alarmMetadata = alarm.getMetadata();

        return system.metadata().isEmpty() || Metadata.isSubset(system.metadata(), alarmMetadata);

    }

    /**
     * Checks that the systems in the Service Registry match those in the
     * currently active Plant Description. An alarm is raised for every
     * mismatch.
     */
    private void updateAlarms() {
        final List<SrSystem> registeredSystems = systemTracker.getSystems();
        final PlantDescriptionEntry activeEntry = pdTracker.activeEntry();
        final List<PdeSystem> pdSystems = new ArrayList<>();

        if (activeEntry != null) {
            pdSystems.addAll(pdTracker.getActiveSystems());
        }

        clearAlarms(registeredSystems, pdSystems);
        raiseAlarms(registeredSystems, pdSystems);
    }

    private void raiseAlarms(
        final List<? extends SrSystem> registeredSystems,
        final List<? extends PdeSystem> pdSystems
    ) {
        for (final PdeSystem entrySystem : pdSystems) {

            final long numMatches = registeredSystems.stream()
                .filter(registeredSystem -> systemsMatch(entrySystem, registeredSystem)).count();

            if (numMatches == 0) {
                alarmManager.raiseSystemNotRegistered(
                    entrySystem.systemId(),
                    entrySystem.systemName().orElse(null),
                    entrySystem.metadata()
                );
            }

            if (numMatches > 1) {
                alarmManager.raiseMultipleMatches(
                    entrySystem.systemId(),
                    entrySystem.systemName().orElse(null),
                    entrySystem.metadata()
                );
            }

        }

        // For each registered system...
        for (final SrSystem registeredSystem : registeredSystems) {

            final boolean presentInPd = pdSystems.stream().anyMatch(entrySystem ->
                systemsMatch(entrySystem, registeredSystem)
            );

            if (!presentInPd) {
                alarmManager.raiseSystemNotInDescription(registeredSystem.systemName(), registeredSystem.metadata());
            }
        }
    }

    /**
     * Clear any alarms for which the underlying issue has been resolved.
     *
     * @param registeredSystems A list of systems found in the Service
     *                          registry.
     * @param pdSystems         A list of systems in the active plant
     *                          description.
     */
    private void clearAlarms(
        final List<? extends SrSystem> registeredSystems,
        final List<? extends PdeSystem> pdSystems
    ) {
        final List<Alarm> notInDescriptionAlarms = alarmManager.getActiveAlarmData(
            AlarmCause.SYSTEM_NOT_IN_DESCRIPTION
        );

        for (final Alarm alarm : notInDescriptionAlarms) {

            final boolean presentInRegistry = registeredSystems.stream().anyMatch(system ->
                alarmMatchesSrSystem(alarm, system)
            );

            final boolean presentInPd = pdSystems.stream().anyMatch(system ->
                alarmMatchesPdSystem(alarm, system)
            );

            if (!presentInRegistry || presentInPd) {
                alarmManager.clearAlarm(alarm);
            }
        }

        final List<Alarm> notFoundInSrAlarms = alarmManager.getActiveAlarmData(List.of(
            AlarmCause.SYSTEM_NOT_REGISTERED,
            AlarmCause.MULTIPLE_MATCHES
        ));

        for (final Alarm alarm : notFoundInSrAlarms) {
            final long numMatches = registeredSystems.stream()
                .filter(registeredSystem -> alarmMatchesSrSystem(alarm, registeredSystem))
                .count();
            final boolean uniqueInSr = numMatches == 1;

            final boolean presentInPd = pdSystems.stream().anyMatch(system ->
                alarmMatchesPdSystem(alarm, system)
            );

            if (uniqueInSr || !presentInPd) {
                alarmManager.clearAlarm(alarm);
                break;
            }
        }
    }

}
