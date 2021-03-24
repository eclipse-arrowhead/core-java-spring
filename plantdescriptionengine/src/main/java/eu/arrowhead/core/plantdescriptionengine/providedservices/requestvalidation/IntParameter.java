package eu.arrowhead.core.plantdescriptionengine.providedservices.requestvalidation;

import se.arkalix.net.http.service.HttpServiceRequest;

import java.util.Optional;
import java.util.Scanner;

/**
 * An instance of this class embodies a set of requirements placed on a single
 * HttpServiceRequest query parameter, where the value is expected to be a
 * positive integer. Used in conjunction with QueryParamParser for validating
 * and parsing query parameters.
 */
public class IntParameter extends QueryParameter {

    private final int minValue;

    /**
     * {@inheritDoc}
     */
    private IntParameter(Builder builder) {
        super(builder);
        this.minValue = builder.minValue;
    }

    /**
     * @return True if the provided string is a base 10 integer.
     */
    private static boolean isInteger(String s) {
        int radix = 10;
        boolean result = false;
        Scanner scanner = new Scanner(s.trim());

        if (scanner.hasNextInt(radix)) {
            scanner.nextInt(radix);
            result = !scanner.hasNext();
        }

        scanner.close();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void parse(HttpServiceRequest request, QueryParamParser parser, boolean required) {

        Optional<String> possibleValue = request.queryParameter(name);

        if (possibleValue.isEmpty()) {
            if (required) {
                parser.report(new ParseError("Missing parameter '" + name + "'."));
            }
            return;
        }

        for (var param : requiredParameters) {
            param.parse(request, parser, true);
        }

        String value = possibleValue.get();

        if (!isInteger(value)) {
            parser
                .report(new ParseError("Query parameter '" + name + "' must be a valid integer, got '" + value + "'."));
            return;
        }

        int intValue = Integer.parseInt(value);

        if (intValue < minValue) {
            parser.report(new ParseError(
                "Query parameter '" + name + "' must be greater than " + minValue + ", got " + intValue + "."));
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
        public Builder min(int i) {
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