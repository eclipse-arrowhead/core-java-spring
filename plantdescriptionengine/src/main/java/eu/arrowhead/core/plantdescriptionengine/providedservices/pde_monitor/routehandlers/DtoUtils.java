package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.MonitorInfo;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.Connection;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PdeSystem;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public final class DtoUtils {

    private static final Logger logger = LoggerFactory.getLogger(DtoUtils.class);

    private DtoUtils() {
    }

    /**
     * Converts the provided list of
     * {@link eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.Connection}
     * to a list of
     * {@link eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.Connection}
     * objects.
     *
     * @param connections A list of connections adhering to the mgmt package format.
     * @return A list of connections adhering to the monitor package format.
     */
    private static List<ConnectionDto> mgmtToMonitor(List<Connection> connections) {
        List<ConnectionDto> result = new ArrayList<>();

        for (var connection : connections) {
            var consumerPort = new SystemPortBuilder()
                .portName(connection.consumer().portName())
                .systemId(connection.consumer().systemId())
                .build();
            var producerPort = new SystemPortBuilder()
                .portName(connection.producer().portName())
                .systemId(connection.producer().systemId())
                .build();

            var connectionCopy = new ConnectionBuilder()
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
     * @param system      The source system which will be supplemented with monitor
     *                    info.
     * @param monitorInfo Object used for keeping track of inventory data of
     *                    monitorable systems.
     * @return A Plant Description Entry System with all the information contained
     * in the {@code system} argument, supplemented with any relevant info
     * in the {@code monitorInfo} argument.
     */
    private static SystemEntryDto extend(PdeSystem system, MonitorInfo monitorInfo) {

        List<MonitorInfo.Bundle> systemInfoList = monitorInfo.getSystemInfo(system.systemName().orElse(null),
            system.metadata().orElse(null));

        List<PortEntryDto> ports = new ArrayList<>();

        // Add all ports. If there is monitor info bound to any of the ports,
        // add it to the resulting port and remove it from the system info list.
        for (var port : system.ports()) {

            // 'consumer' defaults to false when no value is set:
            boolean isConsumer = port.consumer().orElse(false);

            var portBuilder = new PortEntryBuilder()
                .portName(port.portName())
                .serviceInterface(port.serviceInterface().orElse(null))
                .serviceDefinition(port.serviceDefinition())
                .consumer(isConsumer)
                .metadata(port.metadata()
                    .orElse(null));

            // Only add monitor info to ports where this system is the
            // provider:
            if (!isConsumer) {

                MonitorInfo.Bundle serviceMonitorInfo = null;

                for (var info : systemInfoList) {

                    boolean matchesServiceDefinition = info.serviceDefinition.equals(port.serviceDefinition());
                    boolean matchesPort = info.matchesPortMetadata(system.metadata().orElse(null),
                        port.metadata().orElse(null));

                    if (matchesServiceDefinition && matchesPort) {
                        serviceMonitorInfo = info;
                        systemInfoList.remove(info);
                        break;
                    }
                }

                if (serviceMonitorInfo != null) {
                    portBuilder.systemData(serviceMonitorInfo.systemData);
                    portBuilder.inventoryId(serviceMonitorInfo.inventoryId);
                }
            }

            ports.add(portBuilder.build());
        }

        var systemBuilder = new SystemEntryBuilder().systemId(system.systemId())
            .metadata(system.metadata().orElse(null)).ports(ports);

        // If there is any monitor info left, it may belong to the system
        // itself, not a specific port.
        for (var infoBundle : systemInfoList) {
            if (infoBundle.matchesSystemMetadata(system.metadata().orElse(null))) {
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
     * Note that the resulting copy will be an instance of
     * {@link eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PlantDescriptionEntryDto},
     * while the source entry is a
     * {@link eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntry}
     * instance. This function is the glue between the mgmt and monitor packages,
     * letting data flow from one one to the other.
     *
     * @param entry       The source entry on which the new one will be based.
     * @param monitorInfo Object used for keeping track of inventory data of
     *                    monitorable systems.
     * @return A PlantDescriptionEntry with all the information contained in the
     * {@code entry} argument, supplemented with any relevant info in the
     * {@code monitorInfo} argument.
     */
    public static PlantDescriptionEntryDto extend(
        eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntry entry,
        MonitorInfo monitorInfo) {
        List<SystemEntryDto> systems = new ArrayList<>();

        for (var system : entry.systems()) {
            systems.add(extend(system, monitorInfo));
        }

        List<ConnectionDto> connections = mgmtToMonitor(entry.connections());

        return new PlantDescriptionEntryBuilder()
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