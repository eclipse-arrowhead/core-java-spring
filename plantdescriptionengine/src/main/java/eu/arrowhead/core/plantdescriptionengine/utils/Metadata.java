package eu.arrowhead.core.plantdescriptionengine.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Metadata {

    private Metadata() {
    }

    /**
     * Returns
     *
     * @param a A metadata object.
     * @param b A metadata object.
     * @return True if a is a subset of b.
     */
    public static boolean isSubset(Map<String, String> a, Map<String, String> b) {
        for (String key : a.keySet()) {
            if (!b.containsKey(key) || !b.get(key).equals(a.get(key))) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param a A String to String map (system or port metadata).
     * @param b A String to String map (system or port metadata).
     * @return The union of maps a and b, where the values in b override the values
     * of a in case of collisions.
     */
    public static Map<String, String> merge(Map<String, String> a, Map<String, String> b) {
        Map<String, String> result = new HashMap<>();
        if (a != null) {
            for (var key : a.keySet()) {
                result.put(key, a.get(key));
            }
        }
        if (b != null) {
            for (var key : b.keySet()) {
                result.put(key, b.get(key));
            }
        }

        return result;
    }

    /**
     * Create a string representation of the given metadata on the form
     * "{keyA: valueA, keyB: valueB, ...}", with the keys appearing in
     * alphabetical order.
     *
     * @param metadata A metadata object.
     * @return A string representing the given metadata object.
     */
    public static String toString(Map<String, String> metadata) {
        StringBuilder stringBuilder = new StringBuilder();
        // Use a tree map to get the keys in sorted order.
        Map<String, String> sorted = new TreeMap<String, String>(metadata);

        for (final var entry : sorted.entrySet()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(",");
            }
            stringBuilder.append(entry.getKey());
            stringBuilder.append("=");
            stringBuilder.append(entry.getValue());
        }
        return stringBuilder.toString();
    }

}