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
import java.util.Objects;

public class SystemMismatchDetector implements PlantDescriptionUpdateListener, SystemUpdateListener {

    private static final Logger logger = LoggerFactory.getLogger(SystemMismatchDetector.class);

    private final PlantDescriptionTracker pdTracker;
    private final SystemTracker systemTracker;
    private final AlarmManager alarmManager;

    public SystemMismatchDetector(PlantDescriptionTracker pdTracker, SystemTracker systemTracker,
                                  AlarmManager alarmManager) {
        Objects.requireNonNull(pdTracker, "Expected Plant Description Tracker");
        Objects.requireNonNull(systemTracker, "Expected System Tracker");
        Objects.requireNonNull(alarmManager, "Expected Alarm Manager");

        this.pdTracker = pdTracker;
        this.systemTracker = systemTracker;
        this.alarmManager = alarmManager;
    }

    /**
     * Start monitoring Plant Descriptions and registered systems, raising alarms
     * whenever there is a mismatch, and clearing alarms when issues are solved.
     */
    public void run() {
        pdTracker.addListener(this);
        systemTracker.addListener(this);

        // Initial check for mismatches:
        updateAlarms();
    }

    @Override
    public void onPlantDescriptionAdded(PlantDescriptionEntry entry) {
        logger.debug("Entry '" + entry.plantDescription() + "' added, checking for inconsistencies...");
        updateAlarms();
    }

    @Override
    public void onPlantDescriptionUpdated(PlantDescriptionEntry entry) {
        logger.debug("Entry '" + entry.plantDescription() + "' updated, checking for inconsistencies...");
        updateAlarms();
    }

    @Override
    public void onPlantDescriptionRemoved(PlantDescriptionEntry entry) {
        logger.debug("Entry '" + entry.plantDescription() + "' removed, checking for inconsistencies...");
        updateAlarms();
    }

    @Override
    public void onSystemAdded(SrSystem system) {
        logger.debug("System '" + system.systemName() + "' added, checking for inconsistencies...");
        updateAlarms();
    }

    @Override
    public void onSystemRemoved(SrSystem system) {
        logger.debug("System '" + system.systemName() + "' removed, checking for inconsistencies...");
        updateAlarms();
    }

    /**
     * @param entrySystem      A system in a Plant Description Entry.
     * @param registeredSystem A system retrieved from the Service registry.
     * @return True if the two objects represent the same real-world system,
     * false otherwise.
     */
    private boolean systemsMatch(PdeSystem entrySystem, SrSystem registeredSystem) {

        if (entrySystem.metadata().isPresent()) {
            if (registeredSystem.metadata().isEmpty()) {
                return false;
            }

            final var entryMetadata = entrySystem.metadata().get();
            final var srMetadata = registeredSystem.metadata().get();

            if (!Metadata.isSubset(entryMetadata, srMetadata)) {
                return false;
            }
        }

        // At this point, we know that either of these two statements must hold:
        // a) The entry system has metadata which matches that of the SR system.
        // b) The entry system has no metadata, in which case it *must* have a
        //    systemName (as per the spec).

        if (entrySystem.systemName().isPresent()) {
            String entryName = entrySystem.systemName().get();
            return entryName.equals(registeredSystem.systemName());
        }

        return true;
    }

    /**
     * @param alarm  An alarm.
     * @param system A system retrieved from the Service registry.
     * @return True if the alarm refers to the given system, false otherwise.
     */
    private boolean alarmMatchesSrSystem(Alarm alarm, SrSystem system) {

        boolean namesMatch = alarm.systemName != null && alarm.systemName.equals(system.systemName());
        boolean metadataMatches = alarm.metadata != null && system.metadata().isPresent() && Metadata
            .isSubset(alarm.metadata, system.metadata().get());

        if (alarm.systemName != null && !namesMatch) {
            return false;
        }

        if (alarm.metadata != null && !metadataMatches) {
            return false;
        }

        return namesMatch || metadataMatches;
    }

    /**
     * @param alarm       An alarm.
     * @param entrySystem A system in a Plant Description Entry.
     * @return True if the alarm refers to the given system, false otherwise.
     */
    private boolean alarmMatchesPdSystem(Alarm alarm, PdeSystem system) {

        boolean namesMatch = alarm.systemName != null && alarm.systemName.equals(system.systemName().orElse(null));
        boolean metadataMatches = alarm.metadata != null && system.metadata().isPresent() && Metadata
            .isSubset(system.metadata().get(), alarm.metadata);

        if (system.systemName().isPresent()) {
            if (!namesMatch) {
                return false;
            }
        }

        if (system.metadata().isPresent()) {
            if (!metadataMatches) {
                return false;
            }
        }

        return namesMatch || metadataMatches;
    }

    /**
     * Checks that the systems in the Service Registry match those in the
     * currently active Plant Description. An alarm is raised for every
     * mismatch.
     */
    private void updateAlarms() {
        final List<SrSystem> registeredSystems = systemTracker.getSystems();
        final PlantDescriptionEntry activeEntry = pdTracker.activeEntry();
        List<PdeSystem> pdSystems = new ArrayList<>();

        if (activeEntry != null) {
            pdSystems.addAll(pdTracker.getActiveSystems());
        }

        clearAlarms(registeredSystems, pdSystems);
        raiseAlarms(registeredSystems, pdSystems);
    }

    private void raiseAlarms(List<SrSystem> registeredSystems, List<PdeSystem> pdSystems) {
        for (final var entrySystem : pdSystems) {

            long numMatches = registeredSystems.stream()
                .filter(registeredSystem -> systemsMatch(entrySystem, registeredSystem)).count();

            if (numMatches == 0) {
                alarmManager.raiseSystemNotRegistered(
                    entrySystem.systemId(),
                    entrySystem.systemName().orElse(null),
                    entrySystem.metadata().orElse(null)
                );
            }

            if (numMatches > 1) {
                alarmManager.raiseMultipleMatches(
                    entrySystem.systemId(),
                    entrySystem.systemName().orElse(null),
                    entrySystem.metadata().orElse(null)
                );
            }

        }

        // For each registered system...
        for (final SrSystem registeredSystem : registeredSystems) {

            boolean presentInPd = pdSystems.stream().anyMatch(entrySystem ->
                systemsMatch(entrySystem, registeredSystem)
            );

            if (!presentInPd) {
                alarmManager.raiseSystemNotInDescription(
                    registeredSystem.systemName(),
                    registeredSystem.metadata().orElse(null)
                );
            }
        }
    }

    /**
     * Clear any alarms for which the underlying issue has been resolved.
     *
     * @param registeredSystems A list of systems found in the Service registry.
     * @param pdSystems         A list of systems in the active plant description.
     */
    private void clearAlarms(List<SrSystem> registeredSystems, List<PdeSystem> pdSystems) {
        final List<Alarm> notInDescriptionAlarms = alarmManager.getActiveAlarmData(AlarmCause.systemNotInDescription);

        for (final var alarm : notInDescriptionAlarms) {

            boolean presentInRegistry = registeredSystems.stream().anyMatch(system ->
                alarmMatchesSrSystem(alarm, system)
            );

            boolean presentInPd = pdSystems.stream().anyMatch(system ->
                alarmMatchesPdSystem(alarm, system)
            );

            if (!presentInRegistry || presentInPd) {
                alarmManager.clearAlarm(alarm);
                continue;
            }
        }

        final List<Alarm> notFoundInSrAlarms = alarmManager.getActiveAlarmData(List.of(
            AlarmCause.systemNotRegistered,
            AlarmCause.multipleMatches
        ));

        for (final var alarm : notFoundInSrAlarms) {
            long numMatches = registeredSystems.stream()
                .filter(registeredSystem -> alarmMatchesSrSystem(alarm, registeredSystem))
                .count();
            boolean uniqueInSr = numMatches == 1;

            boolean presentInPd = pdSystems.stream().anyMatch(system ->
                alarmMatchesPdSystem(alarm, system)
            );

            if (uniqueInSr || !presentInPd) {
                alarmManager.clearAlarm(alarm);
                break;
            }
        }
    }

}
