package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PdeSystemTest {

    @Test
    public void shouldReturnDeactivatedCopy() {

        final String portNameA = "port_a";
        final String portNameB = "port_b";
        final String portNameC = "port_c";

        final String serviceA = "ser-a";
        final String serviceB = "ser-b";
        final String serviceC = "ser-c";

        final Map<String, String> metadataA = Map.of("x", "1");
        final Map<String, String> metadataB = Map.of("y", "2");
        final Map<String, String> metadataC = Map.of("z", "3");

        final String serviceInterface = "HTTP-SECURE-JSON";

        final List<PortDto> ports = List.of(
            new PortDto.Builder()
                .portName(portNameA)
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceA)
                .consumer(true)
                .metadata(metadataA)
                .build(),
            new PortDto.Builder()
                .portName(portNameB)
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceB)
                .metadata(metadataB)
                .consumer(false)
                .build(),
            new PortDto.Builder()
                .portName(portNameC)
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceC)
                .metadata(metadataC)
                .consumer(true)
                .build());

        final PdeSystemDto system = new PdeSystemDto.Builder()
            .systemId("Sys-X")
            .ports(ports)
            .build();

        final Port portA = system.getPort(portNameA);
        final Port portB = system.getPort(portNameB);
        final Port nullPort = system.getPort("Nonexistent");

        assertEquals(portNameA, portA.portName());
        assertEquals(portNameB, portB.portName());

        assertEquals(serviceInterface, portA.serviceInterface().orElse(null));
        assertEquals(serviceInterface, portB.serviceInterface().orElse(null));

        assertTrue(portA.consumer().orElse(false));
        assertFalse(portB.consumer().orElse(true));

        assertEquals(metadataA, portA.metadata());
        assertEquals(metadataB, portB.metadata());

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
            new PortDto.Builder()
                .portName(portNameA)
                .serviceInterface("HTTP-SECURE-JSON")
                .serviceDefinition(serviceA)
                .consumer(true)
                .build(),
            new PortDto.Builder()
                .portName(portNameB)
                .serviceInterface("HTTP-SECURE-JSON")
                .serviceDefinition(serviceB)
                .consumer(false)
                .build(),
            new PortDto.Builder()
                .portName(portNameC)
                .serviceInterface("HTTP-SECURE-JSON")
                .serviceDefinition(serviceC)
                .consumer(true)
                .build());

        final PdeSystemDto system = new PdeSystemDto.Builder()
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
