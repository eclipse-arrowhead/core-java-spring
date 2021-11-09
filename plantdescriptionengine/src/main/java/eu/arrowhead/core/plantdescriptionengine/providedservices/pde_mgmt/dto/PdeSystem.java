package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto;

import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static se.arkalix.dto.DtoCodec.JSON;

/**
 * Data Transfer Object (DTO) interface for plant description systems.
 */
@DtoReadableAs(JSON)
@DtoWritableAs(JSON)
@DtoToString
public interface PdeSystem {

    String systemId();

    Optional<String> systemName();

    Map<String, String> metadata();

    List<Port> ports();

    /**
     * @param portName The name of a port.
     * @return The port with the given name, or null if it does not exist.
     */
    default Port getPort(final String portName) {
        Objects.requireNonNull(portName, "Expected port name.");
        for (final Port port : ports()) {
            if (port.portName().equals(portName)) {
                return port;
            }
        }
        return null;
    }

    /**
     * @return True if the system has a port with the given name.
     */
    default boolean hasPort(final String portName) {
        for (final Port port : ports()) {
            if (port.portName().equals(portName)) {
                return true;
            }
        }
        return false;
    }
}