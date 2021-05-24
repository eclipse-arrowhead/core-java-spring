package eu.arrowhead.core.plantdescriptionengine.utils;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public final class Metadata {

    private Metadata() {
    }

    /**
     * Returns
     *
     * @param a A metadata object.
     * @param b A metadata object.
     * @return True if a is a subset of b.
     */
    public static boolean isSubset(final Map<String, String> a, final Map<String, String> b) {

        Objects.requireNonNull(a, "Expected metadata as first argument.");
        Objects.requireNonNull(b, "Expected metadata as second argument.");

        for (final String key : a.keySet()) {
            if (!b.containsKey(key) || !b.get(key).equals(a.get(key))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Create a string representation of the given metadata on the form "{keyA:
     * valueA, keyB: valueB, ...}", with the keys appearing in alphabetical
     * order.
     *
     * @param metadata A metadata object.
     * @return A string representing the given metadata object.
     */
    public static String toString(final Map<String, String> metadata) {

        Objects.requireNonNull(metadata, "Expected metadata.");

        final StringBuilder stringBuilder = new StringBuilder();
        // Use a tree map to get the keys in sorted order.
        final Map<String, String> sorted = new TreeMap<>(metadata);

        stringBuilder.append("{");
        for (final Map.Entry<String, String> entry : sorted.entrySet()) {
            if (stringBuilder.length() > 1) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(entry.getKey());
            stringBuilder.append("=");
            stringBuilder.append(entry.getValue());
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

}