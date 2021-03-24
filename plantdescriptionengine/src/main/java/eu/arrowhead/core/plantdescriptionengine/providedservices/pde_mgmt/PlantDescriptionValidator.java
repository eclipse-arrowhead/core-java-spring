package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt;

import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.Connection;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PdeSystem;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntry;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.Port;
import eu.arrowhead.core.plantdescriptionengine.utils.Metadata;

import java.util.*;

/**
 * Class for validating the Plant Descriptions.
 */
public class PlantDescriptionValidator {

    final Map<Integer, PlantDescriptionEntry> entries;
    final List<String> blacklist = List.of("unknown");
    private final List<String> errors = new ArrayList<>();

    /**
     * @param systemName Name of a system
     * @param metadata   Metadata describing a system
     * @return A string uniquely identifying the system with the given
     * name / metadata combination.
     */
    private String uniqueIdentifier(PdeSystem system) {
        String systemName = system.systemName().orElse("");
        Map<String, String> metadata = system.metadata().orElse(null);
        String result = systemName + "{";
        if (metadata != null && !metadata.isEmpty()) {
            result += Metadata.toString(metadata);
        }
        return result + "}";
    }

    /**
     * Constructor.
     *
     * @param entries Object mapping ID:s to Plant Description Entries.
     */
    public PlantDescriptionValidator(Map<Integer, PlantDescriptionEntry> entries) {

        this.entries = entries;

        checkSelfReferencing();
        if (hasError()) {
            return;
        }

        ensureInclusionsExist();
        if (hasError()) {
            return;
        }

        checkForDuplicateInclusions();
        if (hasError()) {
            return;
        }

        checkInclusionCycles();
        if (hasError()) {
            return;
        }

        validateConnections();

        ensureIdentifiableSystems();

        validatePorts();
    }

    /**
     * Ensure that each system in every entry is uniquely identifiable, either
     * by a name or by metadata.
     */
    private void ensureIdentifiableSystems() {
        final var systems = new ArrayList<PdeSystem>();
        for (var entry : entries.values()) {
            systems.addAll(entry.systems());
        }

        Set<String> uniqueIdentifiers = new HashSet<>();

        for (final var system : systems) {

            Optional<Map<String, String>> metadata = system.metadata();
            boolean hasMetadata = metadata.isPresent() && !metadata.get().isEmpty();

            String uid = uniqueIdentifier(system);

            if (!uniqueIdentifiers.add(uid)) {
                errors.add("System with ID '" + system.systemId() +
                    "' cannot be uniquely identified by its name/metadata combination.");
            }

            if (system.systemName().isEmpty() && !hasMetadata) {
                errors.add("Contains a system with neither a name nor metadata to identify it.");
            }

            if (blacklist.contains(system.systemId().toLowerCase())) {
                errors.add("'" + system.systemId() + "' is not a valid system ID.");
            }
        }

    }

    /**
     * Any inclusion cycles originating from the given entry is reported.
     */
    private void checkInclusionCycles() {
        for (var entry : entries.values()) {
            if (cycleOriginatesAtEntry(entry)) {
                errors.add("Contains cycle.");
                return;
            }
        }
    }

    private boolean cycleOriginatesAtEntry(PlantDescriptionEntry entry) {

        var visitedEntries = new HashSet<Integer>();
        var queue = new LinkedList<PlantDescriptionEntry>();

        queue.add(entry);
        while (queue.size() > 0) {
            entry = queue.pop();
            if (!visitedEntries.add(entry.id())) {
                return true;
            }
            for (var included : entry.include()) {
                queue.add(entries.get(included));
            }
        }
        return false;
    }

    /**
     * If any entry lists its own ID in its include list, this is reported as an
     * error.
     */
    private void checkSelfReferencing() {
        for (var entry : entries.values()) {
            for (int id : entry.include()) {
                if (id == entry.id()) {
                    errors.add("Entry includes itself.");
                    return;
                }
            }
        }
    }

    /**
     * If any of the Plant Description ID:s in the entries' include lists is not
     * present in the Plant Description Tracker, this is reported as an error.
     */
    private void ensureInclusionsExist() {
        for (var entry : entries.values()) {
            for (int includedId : entry.include()) {
                if (!entries.containsKey(includedId)) {
                    errors.add(
                        "Error in include list: Entry '" + includedId + "' is required by entry '" + entry.id() + "'.");
                }
            }
        }
    }

    private void checkForDuplicateInclusions() {
        for (var entry : entries.values()) {
            final List<Integer> includes = entry.include();

            // Check for duplicates
            HashSet<Integer> uniqueIds = new HashSet<>();
            HashSet<Integer> duplicates = new HashSet<>();

            for (int id : includes) {
                if (!uniqueIds.add(id)) {
                    duplicates.add(id);
                }
            }

            for (int id : duplicates) {
                errors.add("Entry with ID '" + id + "' is included more than once.");
            }
        }
    }

    /**
     * Validates the connections of a Plant Description Entry.
     */
    private void validateConnections() {

        final var systems = new ArrayList<PdeSystem>();
        final var connections = new ArrayList<Connection>();

        for (var entry : entries.values()) {
            systems.addAll(entry.systems());
            connections.addAll(entry.connections());
        }

        for (var connection : connections) {

            PdeSystem consumerSystem = null;
            PdeSystem producerSystem = null;

            Port consumerPort = null;
            Port producerPort = null;

            if (connection.priority().orElse(0) < 0) { // TODO: Check for max value as well.
                errors.add("A connection has a negative priority.");
            }

            final var producer = connection.producer();
            final var consumer = connection.consumer();

            final String producerId = producer.systemId();
            final String consumerId = consumer.systemId();

            for (var system : systems) {

                boolean isProducerSystem = producerId.equals(system.systemId());
                boolean isConsumerSystem = consumerId.equals(system.systemId());

                if (isProducerSystem) {
                    String portName = producer.portName();
                    producerSystem = system;
                    producerPort = system.getPort(portName);
                    if (producerPort == null) {
                        errors.add("Connection refers to the missing producer port '" + portName + "'");
                    } else if (producerPort.consumer().orElse(false)) {
                        errors.add("Invalid connection, '" + portName + "' is not a producer port.");
                    }
                } else if (isConsumerSystem) {
                    String portName = consumer.portName();
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
    }

    /**
     * Ensures that all entries' systems ports are unique.
     */
    private void validatePorts() {
        for (var entry : entries.values()) {
            for (var system : entry.systems()) {
                ensureUniquePorts(system);
            }

            // Check that no consumer port has metadata.
            for (var system : entry.systems()) {
                ensureNoConsumerPortMetadata(system);
            }
        }
    }

    /**
     * For each consumer port in the system, ensure that no metadata is present.
     *
     * @param system The system whose ports will be validated.
     */
    private void ensureNoConsumerPortMetadata(PdeSystem system) {
        for (var port : system.ports()) {
            if (port.consumer().orElse(false)) {
                boolean hasMetadata = port.metadata().isPresent() && !port.metadata().get().isEmpty();
                if (hasMetadata) {
                    errors.add("Port '" + port.portName() + "' is a consumer port, it must not have any metadata.");
                }
            }
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

        Map<String, Integer> portsPerService = new HashMap<>();
        Set<String> portNames = new HashSet<>();

        // Map serviceDefinitions to lists of metadata:
        Map<String, List<Map<String, String>>> metadataPerService = new HashMap<>();

        for (var port : system.ports()) {

            String portName = port.portName();
            if (portNames.contains(portName)) {
                errors.add("Duplicate port name '" + portName + "' in system '" + system.systemId() + "'");
            }

            portNames.add(portName);

            final String serviceDefinition = port.serviceDefinition();
            Integer numPorts = portsPerService.getOrDefault(serviceDefinition, 0);
            portsPerService.put(serviceDefinition, numPorts + 1);

            if (!metadataPerService.containsKey(serviceDefinition)) {
                metadataPerService.put(serviceDefinition, new ArrayList<>());
            }

            if (port.metadata().isPresent()) {
                metadataPerService.get(serviceDefinition).add(port.metadata().get());
            }
        }

        for (String serviceDefinition : portsPerService.keySet()) {

            int numPorts = portsPerService.getOrDefault(serviceDefinition, 0);
            int numMetadata = metadataPerService.get(serviceDefinition).size();

            // Ensure that there is metadata to differentiate between ports when
            // multiple ports share service definition:
            if (numPorts > numMetadata + 1) {
                errors.add(system.systemId() + " has multiple ports with service definition '" + serviceDefinition
                    + "' without metadata.");
            }

            // Ensure that the metadata is unique within each serviceDefinition:
            List<Map<String, String>> serviceMetadata = metadataPerService.get(serviceDefinition);
            if (serviceMetadata.size() > 1) {
                var uniqueMetadata = new HashSet<>(serviceMetadata);
                if (uniqueMetadata.size() < serviceMetadata.size()) {
                    errors.add(system.systemId() + " has duplicate metadata for ports with service definition '"
                        + serviceDefinition + "'");
                }
            }
        }
    }

    /**
     * @return A human-readable description of any errors in the Plant Description.
     */
    public String getErrorMessage() {
        List<String> errorMessages = new ArrayList<>();
        for (String error : errors) {
            errorMessages.add("<" + error + ">");
        }
        return String.join(", ", errorMessages);
    }

    public boolean hasError() {
        return errors.size() > 0;
    }

}