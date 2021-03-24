package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PdeSystemTest {

    @Test
    public void shouldReturnDeactivatedCopy() {

        final String portNameA = "port_a";
        final String portNameB = "port_b";
        final String portNameC = "port_c";

        final String serviceA = "ser-a";
        final String serviceB = "ser-b";
        final String serviceC = "ser-c";

        final var metadataA = Map.of("x", "1");
        final var metadataB = Map.of("y", "2");
        final var metadataC = Map.of("z", "3");

        final var serviceInterface = "HTTP-SECURE-JSON";

        final List<PortDto> ports = List.of(
            new PortBuilder()
                .portName(portNameA)
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceA)
                .consumer(true)
                .metadata(metadataA)
                .build(),
            new PortBuilder()
                .portName(portNameB)
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceB)
                .metadata(metadataB)
                .consumer(false)
                .build(),
            new PortBuilder()
                .portName(portNameC)
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceC)
                .metadata(metadataC)
                .consumer(true)
                .build());

        final PdeSystemDto system = new PdeSystemBuilder()
            .systemId("Sys-X")
            .ports(ports)
            .build();

        final var portA = system.getPort(portNameA);
        final var portB = system.getPort(portNameB);
        final var nullPort = system.getPort("Nonexistent");

        assertEquals(portNameA, portA.portName());
        assertEquals(portNameB, portB.portName());

        assertEquals(serviceInterface, portA.serviceInterface().orElse(null));
        assertEquals(serviceInterface, portB.serviceInterface().orElse(null));

        assertTrue(portA.consumer().orElse(false));
        assertFalse(portB.consumer().orElse(true));

        assertEquals(metadataA, portA.metadata().orElse(null));
        assertEquals(metadataB, portB.metadata().orElse(null));

        assertEquals(serviceA, portA.serviceDefinition());
        assertEquals(serviceB, portB.serviceDefinition());

        assertNull(nullPort);
    }

    @Test
    public void shouldTellIfSystemHasPort() {

        final String portNameA = "port_a";
        final String portNameB = "port_b";
        final String portNameC = "port_c";
        final String portNameD = "port_d";
        final String portNameE = "port_e";

        final String serviceA = "ser-a";
        final String serviceB = "ser-b";
        final String serviceC = "ser-c";

        final List<PortDto> ports = List.of(
            new PortBuilder()
                .portName(portNameA)
                .serviceInterface("HTTP-SECURE-JSON")
                .serviceDefinition(serviceA)
                .consumer(true)
                .build(),
            new PortBuilder()
                .portName(portNameB)
                .serviceInterface("HTTP-SECURE-JSON")
                .serviceDefinition(serviceB)
                .consumer(false)
                .build(),
            new PortBuilder()
                .portName(portNameC)
                .serviceInterface("HTTP-SECURE-JSON")
                .serviceDefinition(serviceC)
                .consumer(true)
                .build());

        final PdeSystemDto system = new PdeSystemBuilder()
            .systemId("Sys-Y")
            .ports(ports)
            .build();

        assertTrue(system.hasPort(portNameA));
        assertTrue(system.hasPort(portNameB));
        assertTrue(system.hasPort(portNameC));

        assertFalse(system.hasPort(portNameD));
        assertFalse(system.hasPort(portNameE));

    }
}
