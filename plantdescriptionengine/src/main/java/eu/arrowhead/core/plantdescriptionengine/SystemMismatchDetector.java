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
import se.arkalix.util.concurrent.Future;

import java.util.ArrayList;
import java.util.List;
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
    public Future<Void> onPlantDescriptionAdded(final PlantDescriptionEntry entry) {
        Objects.requireNonNull(entry, "Expected entry.");
        logger.debug("Entry '" + entry.plantDescription() + "' added, checking for inconsistencies...");
        updateAlarms();
        return Future.done();
    }

    @Override
    public Future<Void> onPlantDescriptionUpdated(
        final PlantDescriptionEntry updatedEntry,
        final PlantDescriptionEntry oldEntry
    ) {
        Objects.requireNonNull(updatedEntry, "Expected entry.");
        logger.debug("Entry '" + updatedEntry.plantDescription() + "' updated, checking for inconsistencies...");
        updateAlarms();
        return Future.done();
    }

    @Override
    public Future<Void> onPlantDescriptionRemoved(final PlantDescriptionEntry entry) {
        Objects.requireNonNull(entry, "Expected entry.");
        logger.debug("Entry '" + entry.plantDescription() + "' removed, checking for inconsistencies...");
        updateAlarms();
        return Future.done();
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

    private void updateAlarms() {
        final List<PdeSystem> pdSystems = new ArrayList<>();
        final PlantDescriptionEntry activeEntry = pdTracker.activeEntry();

        if (activeEntry != null) {
            pdSystems.addAll(pdTracker.getActiveSystems());
        }

        final List<SrSystem> registeredSystems = systemTracker.getSystems();
        List<Alarm> alarms = new ArrayList<>();

        for (final PdeSystem entrySystem : pdSystems) {

            final long numMatches = registeredSystems.stream()
                .filter(registeredSystem -> systemsMatch(entrySystem, registeredSystem)).count();

            if (numMatches == 0) {
                alarms.add(Alarm.createSystemNotRegisteredAlarm(
                    entrySystem.systemId(),
                    entrySystem.systemName().orElse(null),
                    entrySystem.metadata()
                ));
            }

            if (numMatches > 1) {
                alarms.add(Alarm.createMultipleMatchesAlarm(
                    entrySystem.systemId(),
                    entrySystem.systemName().orElse(null),
                    entrySystem.metadata()
                ));
            }
        }

        for (final SrSystem registeredSystem : registeredSystems) {

            final boolean presentInPd = pdSystems.stream().anyMatch(entrySystem ->
                systemsMatch(entrySystem, registeredSystem)
            );

            if (!presentInPd) {
                alarms.add(Alarm.createSystemNotInDescriptionAlarm(registeredSystem.systemName(), registeredSystem.metadata()));
            }
        }

        alarmManager.replaceAlarms(
            alarms,
            AlarmCause.SYSTEM_NOT_REGISTERED,
            AlarmCause.SYSTEM_NOT_IN_DESCRIPTION,
            AlarmCause.MULTIPLE_MATCHES
        );
    }
}
