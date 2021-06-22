package eu.arrowhead.core.plantdescriptionengine.utils;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SystemNameVerifier {

    public static final int COMMON_NAME_PART_MAX_LENGTH = 63;
    public static final String COMMON_NAME_PART_PATTERN_STRING = "^[a-z](?:[0-9a-z-]*[0-9a-z])?$";
    private static final Pattern commonNamePartPattern;

    static {
        commonNamePartPattern = Pattern.compile(COMMON_NAME_PART_PATTERN_STRING);
    }

    private SystemNameVerifier() {
    }

    public static boolean isValid(final String name) {
        Objects.requireNonNull(name, "Expected name");

        if (name.length() > COMMON_NAME_PART_MAX_LENGTH) {
            return false;
        }

        final Matcher matcher = commonNamePartPattern.matcher(name);
        return matcher.matches();
    }
}