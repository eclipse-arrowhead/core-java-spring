package eu.arrowhead.core.plantdescriptionengine.consumedservices.serviceregistry.dto;

import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;

import static se.arkalix.dto.DtoCodec.JSON;

/**
 * Data Transfer Object (DTO) interface for systems registered in the Service
 * Registry.
 */
@DtoReadableAs(JSON)
@DtoWritableAs(JSON)
@DtoToString
public interface SrSystem {

    Integer id();

    String systemName();

    String address();

    Integer port();

    Optional<String> authenticationInfo();

    Optional<String> createdAt();

    Optional<String> updatedAt();

    Map<String, String> metadata();

    default InetSocketAddress getAddress() {
        return new InetSocketAddress(address(), port());
    }

}
