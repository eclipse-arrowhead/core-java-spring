package eu.arrowhead.core.plantdescriptionengine.providedservices.requestvalidation;

import se.arkalix.net.http.service.HttpServiceRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * An instance of this class embodies a set of requirements placed on a single
 * HttpServiceRequest query parameter, where the value is expected to be a
 * string. Used in conjunction with QueryParamParser for validating and parsing
 * query parameters.
 */
public class StringParameter extends QueryParameter {

    private final List<String> legalValues;
    private final String defaultValue;

    /**
     * {@inheritDoc}
     */
    private StringParameter(Builder builder) {
        super(builder);
        this.legalValues = builder.legalValues;
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

        if (legalValues != null && !legalValues.contains(value)) {
            parser.report(new ParseError(value + " is not a legal value for parameter " + name + "."));
        }

        parser.put(this, value);
    }

    public static class Builder extends QueryParameter.Builder<Builder> {

        private List<String> legalValues = null;
        private String defaultValue = null;

        /**
         * @param values A list of legal values for the constructed parameter.
         * @return This instance.
         */
        public Builder legalValues(String... values) {
            this.legalValues = Arrays.asList(values);
            return this;
        }

        /**
         * @param values A list of legal values for the constructed parameter.
         * @return This instance.
         */
        public Builder legalValues(List<String> values) {
            this.legalValues = values;
            return this;
        }

        /**
         * @param s A default value to use for the constructed parameter.
         * @return This instance.
         */
        public Builder defaultValue(String s) {
            this.defaultValue = s;
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