package eu.arrowhead.core.plantdescriptionengine.providedservices.requestvalidation;

import se.arkalix.net.http.service.HttpServiceRequest;

import java.util.Optional;

/**
 * An instance of this class embodies a set of requirements placed on a single
 * HttpServiceRequest query parameter, where the value is expected to be a
 * boolean. Used in conjunction with QueryParamParser for validating and parsing
 * query parameters.
 */
public class BooleanParameter extends QueryParameter {

    private final Boolean defaultValue;

    /**
     * {@inheritDoc}
     */
    public BooleanParameter(Builder builder) {
        super(builder);
        this.defaultValue = builder.defaultValue;
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
            if (defaultValue != null) {
                parser.put(this, defaultValue);
            }
            return;
        }

        for (var param : requiredParameters) {
            param.parse(request, parser, true);
        }

        String value = possibleValue.get();
        if (!(value.equals("true") || value.equals("false"))) {
            parser.report(new ParseError("Query parameter '" + name + "' must be true or false, got '" + value + "'."));
        }

        parser.put(this, value.equals("true"));
    }

    public static class Builder extends QueryParameter.Builder<Builder> {

        private Boolean defaultValue = null;

        /**
         * @param value A default value for the constructed parameter.
         * @return This instance.
         */
        public Builder defaultValue(boolean value) {
            this.defaultValue = value;
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