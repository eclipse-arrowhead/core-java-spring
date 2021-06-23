package eu.arrowhead.core.plantdescriptionengine.utils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Collections;

public class SystemNameVerifierTest {

    @Test
    public void shouldAllowValidNames() {
        assertTrue(SystemNameVerifier.isValid("orchestrator"));
        assertTrue(SystemNameVerifier.isValid("sys1"));
        assertTrue(SystemNameVerifier.isValid("sys1-abc"));
    }

    @Test
    public void shouldDisallowCapitalLetters() {
        assertFalse(SystemNameVerifier.isValid("Orchestrator"));
        assertFalse(SystemNameVerifier.isValid("orcheStrator"));
        assertFalse(SystemNameVerifier.isValid("orchestratoR"));
    }

    @Test
    public void shouldDisallowUnderscore() {
        assertFalse(SystemNameVerifier.isValid("orche_strator"));
        assertFalse(SystemNameVerifier.isValid("_orchestrator"));
        assertFalse(SystemNameVerifier.isValid("orchestrator_"));
    }

    @Test
    public void shouldDisallowWhitespace() {
        assertFalse(SystemNameVerifier.isValid(" orchestrator"));
        assertFalse(SystemNameVerifier.isValid(" orches trator"));
        assertFalse(SystemNameVerifier.isValid(" orchestrator"));
        assertFalse(SystemNameVerifier.isValid(" orchestrator "));
    }

    @Test
    public void shouldDisallowLongNames() {
        final String longName = String.join("", Collections.nCopies(64, "x"));
        assertFalse(SystemNameVerifier.isValid(longName));
    }
}
