package eu.arrowhead.core.plantdescriptionengine.providedservices.requestvalidation;

import se.arkalix.net.http.service.HttpServiceRequest;

import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

/**
 * An instance of this class embodies a set of requirements placed on a single
 * HttpServiceRequest query parameter, where the value is expected to be a
 * positive integer. Used in conjunction with QueryParamParser for validating
 * and parsing query parameters.
 */
public final class IntParameter extends QueryParameter {

    private final int minValue;

    private IntParameter(final Builder builder) {
        super(builder);
        minValue = builder.minValue;
    }

    /**
     * @return True if the provided string is a base 10 integer.
     */
    private static boolean isInteger(final String s) {
        final int radix = 10;
        boolean result = false;
        final Scanner scanner = new Scanner(s.trim());

        if (scanner.hasNextInt(radix)) {
            scanner.nextInt(radix);
            result = !scanner.hasNext();
        }

        scanner.close();
        return result;
    }

    @Override
    public void parse(final HttpServiceRequest request, final QueryParamParser parser, final boolean required) {

        Objects.requireNonNull(request, "Expected request.");
        Objects.requireNonNull(parser, "Expected parser.");

        final Optional<String> possibleValue = request.queryParameter(name);

        if (possibleValue.isEmpty()) {
            if (required) {
                parser.report(new ParseError("Missing parameter '" + name + "'."));
            }
            return;
        }

        for (final QueryParameter param : requiredParameters) {
            param.parse(request, parser, true);
        }

        final String value = possibleValue.get();

        if (!isInteger(value)) {
            parser.report(new ParseError(
                "Query parameter '" + name + "' must be a valid integer, got '" + value + "'."
            ));
            return;
        }

        final int intValue = Integer.parseInt(value);

        if (intValue < minValue) {
            parser.report(new ParseError(
                "Query parameter '" + name +
                    "' must be greater than or equal to " + minValue +
                    ", got " + intValue + "."
            ));
        }

        if (!parser.hasError()) {
            parser.put(this, intValue);
        }
    }

    public static class Builder extends QueryParameter.Builder<Builder> {

        private int minValue = Integer.MIN_VALUE;

        /**
         * @param i The minimum allowed value for the constructed parameter.
         * @return This instance.
         */
        public Builder min(final int i) {
            minValue = i;
            return this;
        }

        public IntParameter build() {
            return new IntParameter(this);
        }

        @Override
        public Builder self() {
            return this;
        }
    }

}