package eu.arrowhead.core.plantdescriptionengine.providedservices.requestvalidation;

import se.arkalix.net.http.service.HttpServiceRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * An instance of this class embodies a set of requirements placed on a single
 * HttpServiceRequest query parameter, where the value is expected to be a
 * string. Used in conjunction with QueryParamParser for validating and parsing
 * query parameters.
 */
public final class StringParameter extends QueryParameter {

    private final List<String> legalValues;
    private final String defaultValue;

    private StringParameter(final Builder builder) {
        super(builder);
        legalValues = builder.legalValues;
        defaultValue = builder.defaultValue;
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
            if (defaultValue != null) {
                parser.put(this, defaultValue);
            }
            return;
        }

        for (final QueryParameter param : requiredParameters) {
            param.parse(request, parser, true);
        }

        final String value = possibleValue.get();

        if (legalValues != null && !legalValues.contains(value)) {
            parser.report(new ParseError(value + " is not a legal value for parameter " + name + "."));
        }

        parser.put(this, value);
    }

    public static class Builder extends QueryParameter.Builder<Builder> {

        private List<String> legalValues;
        private String defaultValue;

        /**
         * @param values A list of legal values for the constructed parameter.
         * @return This instance.
         */
        public Builder legalValues(final String... values) {
            legalValues = Arrays.asList(values);
            return this;
        }

        /**
         * @param values A list of legal values for the constructed parameter.
         * @return This instance.
         */
        public Builder legalValues(final List<String> values) {
            Objects.requireNonNull(values, "Expected values.");
            legalValues = values;
            return this;
        }

        /**
         * @param defaultValue A default value to use for the constructed
         *                     parameter.
         * @return This instance.
         */
        public Builder defaultValue(final String defaultValue) {
            Objects.requireNonNull(defaultValue, "Expected default value.");
            this.defaultValue = defaultValue;
            return this;
        }

        public StringParameter build() {
            return new StringParameter(this);
        }

        @Override
        public Builder self() {
            return this;
        }
    }
}