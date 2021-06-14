package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt;

import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.Connection;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PdeSystem;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntry;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.Port;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.SystemPort;
import eu.arrowhead.core.plantdescriptionengine.utils.Metadata;
import eu.arrowhead.core.plantdescriptionengine.utils.SystemNameVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class for validating Plant Descriptions.
 */
public class PlantDescriptionValidator {

    final List<String> blacklist = List.of("unknown");
    private final List<String> errors = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param entries Plant Description entries to be validated.
     */
    public PlantDescriptionValidator(final Map<Integer, ? extends PlantDescriptionEntry> entries) {

        Objects.requireNonNull(entries, "Expected entries.");

        for (PlantDescriptionEntry entry : entries.values()) {
            validateInIsolation(entry);
        }

        if (hasError()) {
            return;
        }

        // Find the currently active Plant Description, if any.
        PlantDescriptionEntry activeEntry = entries.values().stream()
            .filter(PlantDescriptionEntry::active)
            .min(PlantDescriptionValidator::compareByMostRecentlyUpdated)
            .orElse(null);

        if (activeEntry == null) {
            return;
        }

        final Set<PlantDescriptionEntry> activeEntries;
        try {
            activeEntries = getIncludeChain(activeEntry, entries);
        } catch (ValidationException e) {
            errors.add(e.getMessage());
            return;
        }

        ensureIdentifiableSystems(activeEntries);
        validateConnections(activeEntries);
    }

    /**
     * Constructor
     *
     * @param entries Plant Description entries to be validated.
     */
    public PlantDescriptionValidator(final PlantDescriptionEntry... entries) {
        this(Arrays.stream(entries)
            .collect(Collectors.toMap(PlantDescriptionEntry::id, entry -> entry)));
    }

    private static int compareByMostRecentlyUpdated(
        final PlantDescriptionEntry a,
        final PlantDescriptionEntry b
    ) {
        return b.updatedAt().compareTo(a.updatedAt());
    }

    /**
     * Perform all validation checks that can be performed on a Plant
     * Description entry in isolation.
     *
     * @param entry A Plant Description entry to validate.
     */
    private void validateInIsolation(final PlantDescriptionEntry entry) {
        checkSelfReferencing(entry);
        checkForDuplicateInclusions(entry);
        validatePorts(entry);
        validate(entry.systems());
    }

    private void validate(final List<PdeSystem> systems) {
        for (final PdeSystem system : systems) {
            ensureLegalSystemId(system.systemId());
            ensureLegalSystemName(system);
            checkIfIdentifiable(system);
        }
    }

    /**
     * Report an error if the given system does not contain a valid identifier
     * (either metadata or system name)
     *
     * @param system A system to validate.
     */
    private void checkIfIdentifiable(final PdeSystem system) {
        if (system.systemName().isEmpty() && system.metadata().isEmpty()) {
            errors.add("Contains a system with neither a name nor metadata to identify it.");
        }
    }

    private void ensureLegalSystemId(final String systemId) {
        if (blacklist.contains(systemId.toLowerCase())) {
            errors.add("'" + systemId + "' is not a valid system ID.");
        }
    }

    private void ensureLegalSystemName(final PdeSystem system) {
        if (system.systemName().isEmpty()) {
            return;
        }
        final String systemName = system.systemName().get();
        if (!SystemNameVerifier.isValid(systemName)) {
            errors.add("'" + systemName + "' is not a valid system name.");
        }
    }

    /**
     * @param system A Plant Description Entry system.
     * @return A string uniquely identifying the system with the given name /
     * metadata combination.
     */
    private String uniqueIdentifier(final PdeSystem system) {
        final String systemName = system.systemName().orElse("");
        final Map<String, String> metadata = system.metadata();
        String result = systemName + "{";
        if (!metadata.isEmpty()) {
            result += Metadata.toString(metadata);
        }
        return result + "}";
    }

    /**
     * Ensure that each system in every given entry is uniquely identifiable,
     * either by a name or by metadata.
     *
     * @param entries A set of Plant Description Entries.
     */
    private void ensureIdentifiableSystems(final Set<PlantDescriptionEntry> entries) {

        final ArrayList<PdeSystem> systems = new ArrayList<>();

        for (final PlantDescriptionEntry entry : entries) {
            systems.addAll(entry.systems());
        }

        final Set<String> uniqueIdentifiers = new HashSet<>();

        for (final PdeSystem system : systems) {
            final String uid = uniqueIdentifier(system);
            if (!uniqueIdentifiers.add(uid)) {
                errors.add("System with ID '" + system.systemId() +
                    "' cannot be uniquely identified by its name/metadata combination.");
            }
        }
    }

    /**
     * Retrieves the complete include chain of the specified entry from among
     * the given list of all entries.
     *
     * @param entry      Plant Description entry whose include chain will be
     *                   retrieved.
     * @param allEntries A list containing all Plant Description entries in
     *                   which to search for included entries.
     * @throws ValidationException If there is an include cycle, or if an
     *                             included entry is not present in
     *                             {@code allEntries}.
     */
    private Set<PlantDescriptionEntry> getIncludeChain(
        final PlantDescriptionEntry entry,
        final Map<Integer, ? extends PlantDescriptionEntry> allEntries
    ) throws ValidationException {

        final HashSet<PlantDescriptionEntry> visitedEntries = new HashSet<>();
        final LinkedList<PlantDescriptionEntry> queue = new LinkedList<>();

        queue.add(entry);

        while (!queue.isEmpty()) {
            final PlantDescriptionEntry nextEntry = queue.pop();

            if (!visitedEntries.add(nextEntry)) {
                throw new ValidationException("Error in include list: Cycle detected.");
            }

            for (final Integer includedId : nextEntry.include()) {
                if (!allEntries.containsKey(includedId)) {
                    throw new ValidationException("Error in include list: Entry '" + includedId + "' is required by entry '" + nextEntry.id() + "'.");
                }
                final PlantDescriptionEntry includedEntry = allEntries.get(includedId);
                queue.add(includedEntry);
            }
        }

        return visitedEntries;
    }

    /**
     * If the given entry lists its own ID in its include list, this is reported
     * as an error.
     */
    private void checkSelfReferencing(final PlantDescriptionEntry entry) {
        for (final int id : entry.include()) {
            if (id == entry.id()) {
                errors.add("Entry includes itself.");
                return;
            }
        }
    }

    private void checkForDuplicateInclusions(final PlantDescriptionEntry entry) {
        final List<Integer> includes = entry.include();

        // Check for duplicates
        final HashSet<Integer> uniqueIds = new HashSet<>();
        final HashSet<Integer> duplicates = new HashSet<>();

        for (final int id : includes) {
            if (!uniqueIds.add(id)) {
                duplicates.add(id);
            }
        }

        duplicates.forEach(id -> errors.add("Entry with ID '" + id + "' is included more than once."));
    }

    private void validateConnections(final Set<PlantDescriptionEntry> entries) {

        final ArrayList<PdeSystem> systems = new ArrayList<>();
        final ArrayList<Connection> connections = new ArrayList<>();

        for (final PlantDescriptionEntry entry : entries) {
            systems.addAll(entry.systems());
            connections.addAll(entry.connections());
        }

        for (final Connection connection : connections) {
            validateConnection(connection, systems);
        }
    }

    private void validateConnection(final Connection connection, final ArrayList<PdeSystem> systems) {
        PdeSystem consumerSystem = null;
        PdeSystem producerSystem = null;

        Port consumerPort = null;
        Port producerPort = null;

        if (connection.priority().orElse(0) < 0) { // TODO: Check for max value as well.
            errors.add("A connection has a negative priority.");
        }

        final SystemPort producer = connection.producer();
        final SystemPort consumer = connection.consumer();

        final String producerId = producer.systemId();
        final String consumerId = consumer.systemId();

        for (final PdeSystem system : systems) {

            final boolean isProducerSystem = producerId.equals(system.systemId());
            final boolean isConsumerSystem = consumerId.equals(system.systemId());

            if (isProducerSystem) {
                final String portName = producer.portName();
                producerSystem = system;
                producerPort = system.getPort(portName);
                if (producerPort == null) {
                    errors.add("Connection refers to the missing producer port '" + portName + "'");
                } else if (producerPort.consumer().orElse(false)) {
                    errors.add("Invalid connection, '" + portName + "' is not a producer port.");
                }
            } else if (isConsumerSystem) {
                final String portName = consumer.portName();
                consumerSystem = system;
                consumerPort = system.getPort(portName);
                if (consumerPort == null) {
                    errors.add("Connection refers to the missing consumer port '" + portName + "'");
                } else if (!consumerPort.consumer().orElse(false)) {
                    errors.add("Invalid connection, '" + portName + "' is not a consumer port.");
                }
            }
        }

        if (producerSystem == null) {
            errors.add("A connection refers to the missing system '" + producerId + "'");
        }
        if (consumerSystem == null) {
            errors.add("A connection refers to the missing system '" + consumerId + "'");
        }

        if (producerPort != null && consumerPort != null) {
            // Ensure that service interfaces match
            if (!producerPort.serviceInterface().equals(consumerPort.serviceInterface())) {
                errors.add("The service interfaces of ports '" +
                    consumerPort.portName() + "' and '" + producerPort.portName() + "' do not match."
                );
            }
            // Ensure that service definitions match
            if (!producerPort.serviceDefinition().equals(consumerPort.serviceDefinition())) {
                errors.add("The service definitions of ports '" +
                    consumerPort.portName() + "' and '" + producerPort.portName() + "' do not match."
                );
            }
        }
    }

    private void validatePorts(final PlantDescriptionEntry entry) {
        entry.systems().forEach(this::validatePorts);
    }

    private void validatePorts(final PdeSystem system) {
        ensureUniquePorts(system);

        for (final Port port : system.ports()) {
            ensureValidServiceDefinition(port.serviceDefinition());
            ensureNoConsumerPortMetadata(port);
        }
    }

    private void ensureValidServiceDefinition(String serviceDefinition) {
        // TODO: Allow uppercase characters and whitespace, but trim and
        // transform to lowercase when storing PD:s instead.
        if (!serviceDefinition.trim().toLowerCase().equals(serviceDefinition)) {
            errors.add("Invalid service definition '" + serviceDefinition +
                "', only lowercase characters and no whitespace is allowed.");
        }
    }

    private void ensureNoConsumerPortMetadata(final Port port) {
        if (port.consumer().orElse(false)) {
            ensureNoMetadata(port);
        }
    }

    private void ensureNoMetadata(Port port) {
        final Map<String, String> metadata = port.metadata();
        if (!metadata.isEmpty()) {
            errors.add("Port '" + port.portName() + "' is a consumer port, it must not have any metadata.");
        }
    }

    /**
     * Ensures that the given system's ports are all unique.
     * <p>
     * The PDE must be able to differentiate between the ports of a system. When
     * multiple ports share the same service definition, they must have
     * different metadata. This method ensures that this property holds.
     * <p>
     *
     * @param system The system whose ports will be validated.
     */
    private void ensureUniquePorts(final PdeSystem system) {

        final Map<String, Integer> portsPerService = new HashMap<>();
        final Set<String> portNames = new HashSet<>();

        // Map serviceDefinitions to lists of metadata:
        final Map<String, List<Map<String, String>>> metadataPerService = new HashMap<>();

        for (final Port port : system.ports()) {

            final String portName = port.portName();
            if (portNames.contains(portName)) {
                errors.add("Duplicate port name '" + portName + "' in system '" + system.systemId() + "'");
            }

            portNames.add(portName);

            final String serviceDefinition = port.serviceDefinition();
            final Integer numPorts = portsPerService.getOrDefault(serviceDefinition, 0);
            portsPerService.put(serviceDefinition, numPorts + 1);

            if (!metadataPerService.containsKey(serviceDefinition)) {
                metadataPerService.put(serviceDefinition, new ArrayList<>());
            }

            if (!port.metadata().isEmpty()) {
                metadataPerService.get(serviceDefinition).add(port.metadata());
            }
        }

        for (final String serviceDefinition : portsPerService.keySet()) {

            final int numPorts = portsPerService.getOrDefault(serviceDefinition, 0);
            final int numMetadata = metadataPerService.get(serviceDefinition).size();

            // Ensure that there is metadata to differentiate between ports when
            // multiple ports share service definition:
            if (numPorts > numMetadata + 1) {
                errors.add(system.systemId() + " has multiple ports with service definition '" + serviceDefinition
                    + "' without metadata.");
            }

            // Ensure that the metadata is unique within each serviceDefinition:
            final List<Map<String, String>> serviceMetadata = metadataPerService.get(serviceDefinition);
            if (serviceMetadata.size() > 1) {
                final HashSet<Map<String, String>> uniqueMetadata = new HashSet<>(serviceMetadata);
                if (uniqueMetadata.size() < serviceMetadata.size()) {
                    errors.add(system.systemId() + " has duplicate metadata for ports with service definition '"
                        + serviceDefinition + "'");
                }
            }
        }
    }

    /**
     * @return A human-readable description of any errors in a Plant
     * Description.
     */
    public String getErrorMessage() {
        final List<String> errorMessages = errors.stream()
            .map(error -> "<" + error + ">")
            .collect(Collectors.toList());
        return String.join(", ", errorMessages);
    }

    public boolean hasError() {
        return !errors.isEmpty();
    }

}