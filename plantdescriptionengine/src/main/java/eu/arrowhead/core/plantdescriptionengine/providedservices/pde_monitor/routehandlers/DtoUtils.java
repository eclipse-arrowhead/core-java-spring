package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.MonitorInfo;
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
import java.util.Map;
import java.util.Objects;

public final class DtoUtils {

    private static final Logger logger = LoggerFactory.getLogger(DtoUtils.class);

    private DtoUtils() {
    }

    /**
     * Converts the provided list of {@link eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.Connection}
     * to a list of {@link eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.Connection}
     * objects.
     *
     * @param connections A list of connections adhering to the mgmt package
     *                    format.
     * @return A list of connections adhering to the monitor package format.
     */
    private static List<ConnectionDto> mgmtToMonitor(final List<? extends Connection> connections) {
        final List<ConnectionDto> result = new ArrayList<>();

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
     * @param system      The source system which will be supplemented with
     *                    monitor info.
     * @param monitorInfo Object used for keeping track of inventory data of
     *                    monitorable systems.
     * @return A Plant Description Entry System with all the information
     * contained in the {@code system} argument, supplemented with any relevant
     * info in the {@code monitorInfo} argument.
     */
    private static SystemEntryDto extend(final PdeSystem system, final MonitorInfo monitorInfo) {

        final List<MonitorInfo.Bundle> systemInfoList = monitorInfo.getSystemInfo(
            system.systemName().orElse(null),
            system.metadata().orElse(null)
        );

        final List<PortEntryDto> ports = new ArrayList<>();

        // Add all ports. If there is monitor info bound to any of the ports,
        // add it to the resulting port and remove it from the system info list.
        for (final Port port : system.ports()) {

            // 'consumer' defaults to false when no value is set:
            final boolean isConsumer = port.consumer().orElse(false);

            final PortEntryDto.Builder portBuilder = new PortEntryDto.Builder()
                .portName(port.portName())
                .serviceInterface(port.serviceInterface().orElse(null))
                .serviceDefinition(port.serviceDefinition())
                .consumer(isConsumer)
                .metadata(port.metadata().orElse(null));

            // Possibly add monitor info to ports where this system is the
            // provider:
            if (!isConsumer) {

                for (final MonitorInfo.Bundle info : systemInfoList) {

                    final boolean matchesServiceDefinition = info.serviceDefinition.equals(port.serviceDefinition());
                    final boolean matchesPort = port.metadata().isPresent() &&
                        Metadata.isSubset(port.metadata().get(), info.serviceMetadata);

                    if (matchesServiceDefinition && matchesPort) {

                        portBuilder.systemData(info.systemData);
                        portBuilder.inventoryId(info.inventoryId);

                        systemInfoList.remove(info);
                        break;
                    }
                }

            }

            ports.add(portBuilder.build());
        }

        final SystemEntryDto.Builder systemBuilder = new SystemEntryDto.Builder()
            .systemId(system.systemId())
            .metadata(system.metadata().orElse(null))
            .ports(ports);

        // If there is any monitor info left, it may belong to the system
        // itself, not a specific port.
        for (final MonitorInfo.Bundle infoBundle : systemInfoList) {
            final Map<String, String> metadata = system.metadata().orElse(null);
            if (infoBundle.matchesSystemMetadata(metadata)) {
                systemBuilder.inventoryId(infoBundle.inventoryId).systemData(infoBundle.systemData);
                break;
            } else {
                logger.warn("Unmatched data in MonitorInfo");
            }
        }
        return systemBuilder.build();
    }

    /**
     * Returns a Plant Description Entry supplemented with monitor info.
     * <p>
     * This function is the glue between the mgmt and monitor packages, letting
     * data flow from one one to the other.
     *
     * @param entry       The source entry on which the new one will be based.
     * @param monitorInfo Object used for keeping track of inventory data of
     *                    monitorable systems.
     * @return A PlantDescriptionEntry with all the information contained in the
     * {@code entry} argument, supplemented with any relevant info in the {@code
     * monitorInfo} argument.
     */
    public static MonitorPlantDescriptionEntryDto extend(
        final PlantDescriptionEntry entry,
        final MonitorInfo monitorInfo
    ) {

        Objects.requireNonNull(entry, "Expected entry.");
        Objects.requireNonNull(monitorInfo, "Expected MonitorInfo.");

        final List<SystemEntryDto> systems = new ArrayList<>();

        for (final PdeSystem system : entry.systems()) {
            systems.add(extend(system, monitorInfo));
        }

        final List<ConnectionDto> connections = mgmtToMonitor(entry.connections());

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