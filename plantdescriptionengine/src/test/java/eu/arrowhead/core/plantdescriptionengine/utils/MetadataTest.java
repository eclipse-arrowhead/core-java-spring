package eu.arrowhead.core.plantdescriptionengine.utils;

import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MetadataTest {

    @Test
    public void shouldHandleNonemptyMetadata() {
        Map<String, String> a = Map.of(
            "x", "1",
            "y", "2",
            "z", "3"
        );

        Map<String, String> b = Map.of(
            "x", "1",
            "y", "2"
        );

        assertTrue(Metadata.isSubset(b, a));
        assertFalse(Metadata.isSubset(a, b));
    }

    @Test
    public void shouldHandleIdenticalMetadata() {
        Map<String, String> a = Map.of(
            "i", "alice",
            "j", "bob",
            "k", "cat"
        );

        assertTrue(Metadata.isSubset(a, a));
    }

    @Test
    public void shouldHandleEmptyMetadata() {
        Map<String, String> empty = Collections.emptyMap();
        Map<String, String> nonEmpty = Map.of(
            "i", "alice",
            "j", "bob",
            "k", "cat"
        );

        assertTrue(Metadata.isSubset(empty, empty));
        assertTrue(Metadata.isSubset(empty, nonEmpty));
        assertFalse(Metadata.isSubset(nonEmpty, empty));
    }

    @Test
    public void shouldHandleValueMismatches() {
        Map<String, String> a = Map.of(
            "x", "1",
            "y", "2",
            "z", "3"
        );

        Map<String, String> b = Map.of(
            "x", "1",
            "y", "2",
            "z", "4" // Mismatch
        );

        Map<String, String> c = Map.of(
            "x", "1",
            "y", "9" // Mismatch
        );

        assertFalse(Metadata.isSubset(b, a));
        assertFalse(Metadata.isSubset(c, a));
    }

    @Test
    public void shouldCreateStringRepresentation() {
        Map<String, String> metadata = Map.of(
            "name", "alice",
            "age", "32",
            "height", "169"
        );

        final String expected = "{age=32, height=169, name=alice}";
        assertEquals(expected, Metadata.toString(metadata));
    }

}
