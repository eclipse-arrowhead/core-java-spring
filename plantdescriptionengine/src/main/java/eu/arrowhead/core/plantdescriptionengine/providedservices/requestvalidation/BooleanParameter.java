package eu.arrowhead.core.plantdescriptionengine.providedservices.requestvalidation;

import se.arkalix.net.http.service.HttpServiceRequest;

import java.util.Objects;
import java.util.Optional;

/**
 * An instance of this class embodies a set of requirements placed on a single
 * HttpServiceRequest query parameter, where the value is expected to be a
 * boolean. Used in conjunction with QueryParamParser for validating and parsing
 * query parameters.
 */
public class BooleanParameter extends QueryParameter {

    private final Boolean defaultValue;

    public BooleanParameter(final Builder builder) {
        super(Objects.requireNonNull(builder, "Expected builder."));
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
        if (!(String.valueOf(true).equals(value) || String.valueOf(false).equals(value))) {
            parser.report(new ParseError("Query parameter '" + name + "' must be true or false, got '" + value + "'."));
        }

        parser.put(this, Boolean.valueOf(value));
    }

    public static class Builder extends QueryParameter.Builder<Builder> {

        private Boolean defaultValue;

        /**
         * @param value A default value for the constructed parameter.
         * @return This instance.
         */
        public Builder defaultValue(final boolean value) {
            defaultValue = value;
            return this;
        }

        public BooleanParameter build() {
            return new BooleanParameter(this);
        }

        @Override
        public Builder self() {
            return this;
        }
    }
}