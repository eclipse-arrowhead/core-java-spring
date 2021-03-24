package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto;

import eu.arrowhead.core.plantdescriptionengine.utils.Metadata;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * Data Transfer Object (DTO) interface for plant description systems.
 */
@DtoReadableAs(JSON)
@DtoWritableAs(JSON)
@DtoToString
public interface PdeSystem {

    String systemId();

    Optional<String> systemName();

    Optional<Map<String, String>> metadata();

    List<Port> ports();

    /**
     * @param portName The name of a port.
     * @return The port with the given name, or null if it does not exist.
     */
    default Port getPort(String portName) {
        for (var port : ports()) {
            if (port.portName().equals(portName)) {
                return port;
            }
        }
        return null;
    }

    /**
     * @return The union of service and port metadata. In case of overlaps between
     * the two sets, service metadata has precedence.
     */
    default Map<String, String> portMetadata(String portName) {
        Port port = getPort(portName);
        return Metadata.merge(metadata().orElse(null), port.metadata().orElse(null));
    }

    /**
     * @return True if the system has a port with the given name.
     */
    default boolean hasPort(String portName) {
        for (var port : ports()) {
            if (port.portName().equals(portName)) {
                return true;
            }
        }
        return false;
    }
}