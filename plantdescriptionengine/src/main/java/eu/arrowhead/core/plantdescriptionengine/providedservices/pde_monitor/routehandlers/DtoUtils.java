package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.ApiConstants;
import eu.arrowhead.core.plantdescriptionengine.MonitorInfo;
import eu.arrowhead.core.plantdescriptionengine.MonitorInfoTracker;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.Connection;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PdeSystem;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntry;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.Port;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.ConnectionDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.MonitorPlantDescriptionEntryDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PortEntryDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.SystemEntryDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.SystemPortDto;
import eu.arrowhead.core.plantdescriptionengine.utils.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DtoUtils {

    private static final Logger logger = LoggerFactory.getLogger(DtoUtils.class);

    private DtoUtils() {
    }

    /**
     * Converts the provided list of {@link eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.Connection}
     * to a list of {@link eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.ConnectionDto}
     * objects.
     *
     * @param connections A list of connections adhering to the mgmt package
     *                    format.
     * @return A list of connections adhering to the monitor package format.
     */
    private static List<ConnectionDto> mgmtToMonitor(final List<? extends Connection> connections) {
        final List<ConnectionDto> result = new ArrayList<>(connections.size());

        for (final Connection connection : connections) {
            final SystemPortDto consumerPort = new SystemPortDto.Builder()
                .portName(connection.consumer().portName())
                .systemId(connection.consumer().systemId())
                .build();
            final SystemPortDto producerPort = new SystemPortDto.Builder()
                .portName(connection.producer().portName())
                .systemId(connection.producer().systemId())
                .build();

            final ConnectionDto connectionCopy = new ConnectionDto.Builder()
                .consumer(consumerPort)
                .producer(producerPort)
                .build();

            result.add(connectionCopy);
        }
        return result;
    }

    /**
     * Returns a Plant Description Entry System supplemented with monitor info.
     *
     * @param system             The source system which will be supplemented
     *                           with monitor info.
     * @param monitorInfoTracker Object used for keeping track of monitor info
     *                           of Arrowhead systems.
     * @return A Plant Description Entry System with all the information
     * contained in the {@code system} argument, supplemented with any relevant
     * monitor info.
     */
    private static SystemEntryDto extend(final PdeSystem system, final MonitorInfoTracker monitorInfoTracker) {

        final List<MonitorInfo> monitorInfoList = monitorInfoTracker.getSystemInfo(
            system.systemName().orElse(null),
            system.metadata()
        );

        final List<PortEntryDto> ports = new ArrayList<>();

        // Add all ports. If there is monitor info bound to any of the ports,
        // add it to that port and remove it from the system info list.
        for (final Port port : system.ports()) {

            // 'consumer' defaults to false when no value is set:
            final boolean isConsumer = port.consumer().orElse(false);
            final boolean isMonitorablePort = (port.serviceDefinition().equals(ApiConstants.MONITORABLE_SERVICE_NAME));

            final PortEntryDto.Builder portBuilder = new PortEntryDto.Builder()
                .portName(port.portName())
                .serviceInterface(port.serviceInterface().orElse(null))
                .serviceDefinition(port.serviceDefinition())
                .consumer(isConsumer)
                .metadata(port.metadata());

            if (!isConsumer && !isMonitorablePort) {

                for (final MonitorInfo info : monitorInfoList) {

                    final boolean hasMetadata = !port.metadata().isEmpty();
                    final boolean matchesPort = Metadata.isSubset(port.metadata(), info.serviceMetadata);

                    if (hasMetadata && matchesPort) {

                        portBuilder.systemData(info.systemData);
                        portBuilder.inventoryId(info.inventoryId);

                        monitorInfoList.remove(info);
                        break;
                    }
                }

            }

            ports.add(portBuilder.build());
        }

        final SystemEntryDto.Builder systemBuilder = new SystemEntryDto.Builder()
            .systemId(system.systemId())
            .metadata(system.metadata())
            .ports(ports);

        // If there is any monitor info left, it belongs to the system itself.
        if (monitorInfoList.size() == 1) {
            final MonitorInfo info = monitorInfoList.get(0);
            systemBuilder.inventoryId(info.inventoryId).systemData(info.systemData);
        } else if (monitorInfoList.size() > 1) {
            logger.warn("Unmatched data in MonitorInfo");
        }

        return systemBuilder.build();
    }

    /**
     * Returns a Plant Description Entry supplemented with monitor info.
     * <p>
     * This function is the glue between the mgmt and monitor packages, letting
     * data flow from one one to the other.
     *
     * @param entry              The source entry on which the new one will be
     *                           based.
     * @param monitorInfoTracker Object used for keeping track of monitor info
     *                           of Arrowhead systems.
     * @param pdTracker          An object that keeps track of Plant Description
     *                           Entries.
     * @return A PlantDescriptionEntry with all the information contained in the
     * {@code entry} argument, supplemented with any relevant info in the {@code
     * monitorInfo} argument.
     */
    public static MonitorPlantDescriptionEntryDto extend(
        final PlantDescriptionEntry entry,
        final MonitorInfoTracker monitorInfoTracker,
        final PlantDescriptionTracker pdTracker
    ) {

        Objects.requireNonNull(entry, "Expected entry.");
        Objects.requireNonNull(monitorInfoTracker, "Expected MonitorInfoTracker.");

        // Get all systems and connections, also ones that are included from
        // other Plant Descriptions:
        final List<PdeSystem> originalSystems = pdTracker.getAllSystems(entry);
        final List<Connection> originalConnections = pdTracker.getAllConnections(entry.id());

        final List<SystemEntryDto> systems = new ArrayList<>();

        for (final PdeSystem system : originalSystems) {
            systems.add(extend(system, monitorInfoTracker));
        }

        final List<ConnectionDto> connections = mgmtToMonitor(originalConnections);

        return new MonitorPlantDescriptionEntryDto.Builder()
            .id(entry.id())
            .plantDescription(entry.plantDescription())
            .active(entry.active())
            .include(entry.include())
            .systems(systems)
            .connections(connections)
            .createdAt(entry.createdAt())
            .updatedAt(entry.updatedAt())
            .build();
    }
}