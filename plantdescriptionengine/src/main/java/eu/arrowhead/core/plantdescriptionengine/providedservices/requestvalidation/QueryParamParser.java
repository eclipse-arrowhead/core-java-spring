package eu.arrowhead.core.plantdescriptionengine.providedservices.requestvalidation;

import se.arkalix.net.http.service.HttpServiceRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Class for parsing and validating the query parameters of
 * HttpServiceRequests.
 */
public class QueryParamParser {

    private final List<QueryParameter> required;
    private final List<QueryParameter> accepted;

    private final Map<IntParameter, Integer> intValues = new HashMap<>();
    private final Map<BooleanParameter, Boolean> boolValues = new HashMap<>();
    private final Map<StringParameter, String> stringValues = new HashMap<>();

    private final List<ParseError> errors = new ArrayList<>();

    /**
     * Constructs an instance of this class. The query parameters of the
     * provided request are immediately parsed and validated according to the
     * query parameter requirements specified by the two first arguments.
     * <p>
     * All of the parameters specified in {@code required} must be present, and
     * all of their requirements fulfilled, for the request to be considered
     * valid.
     * <p>
     * The parameters in {@code accepted} may be left out of the request, but if
     * present, must fulfill their requirements.
     * <p>
     * If the parameters are invalid, a {@code #ParseError} is thrown.
     * <p>
     * If the parameters are valid, their values will be accessible via the
     * method {@code getValue}.
     *
     * @param required A list of all query parameters that are required for this
     *                 request to be considered valid, with specific constraints
     *                 for each one. May be null.
     * @param accepted A list of accepted query parameters. May be null.
     * @param request  The head and body of an incoming HTTP request.
     */
    public QueryParamParser(
        final List<QueryParameter> required,
        final List<QueryParameter> accepted,
        final HttpServiceRequest request
    ) throws ParseError {

        Objects.requireNonNull(request, "Expected request.");

        this.required = (required == null) ? new ArrayList<>() : required;
        this.accepted = (accepted == null) ? new ArrayList<>() : accepted;

        parse(request);

        if (hasError()) {
            throw getCompoundError();
        }
    }

    public boolean hasError() {
        return !errors.isEmpty();
    }

    /**
     * Stores information about a single query parameter requirement violation.
     *
     * @param error The error to report.
     */
    void report(final ParseError error) {
        errors.add(error);
    }

    /**
     * Validates and parses the query parameters of the given request.
     *
     * @param request The request to parse.
     */
    private void parse(final HttpServiceRequest request) {
        for (final QueryParameter param : required) {
            param.parse(request, this, true);
        }
        for (final QueryParameter param : accepted) {
            param.parse(request, this, false);
        }
    }

    void put(final IntParameter key, final Integer value) {
        intValues.put(key, value);
    }

    void put(final BooleanParameter key, final Boolean value) {
        boolValues.put(key, value);
    }

    void put(final StringParameter key, final String value) {
        stringValues.put(key, value);
    }

    /**
     * @param param A {@code QueryParam} that has been parsed by this instance.
     * @return An {@code Optional} that contains the value of the query
     * parameter if it was present in the parsed {@code HttpServiceRequest} or
     * if the parameter has a default value.
     */
    public Optional<Boolean> getValue(final BooleanParameter param) {
        Objects.requireNonNull(param, "Expected parameter.");
        return Optional.ofNullable(boolValues.get(param));
    }

    /**
     * @param param A {@code QueryParam} that has been parsed by this instance.
     * @return An {@code Optional} that contains the value of the query
     * parameter if it was present in the parsed {@code HttpServiceRequest} or
     * if the parameter has a default value.
     */
    public Optional<Integer> getValue(final IntParameter param) {
        Objects.requireNonNull(param, "Expected parameter.");
        return Optional.ofNullable(intValues.get(param));
    }

    /**
     * @param param A {@code QueryParam} that has been parsed by this instance.
     * @return An {@code Optional} that contains the value of the query
     * parameter if it was present in the parsed {@code HttpServiceRequest} or
     * if the parameter has a default value.
     */
    public Optional<String> getValue(final StringParameter param) {
        Objects.requireNonNull(param, "Expected parameter.");
        return Optional.ofNullable(stringValues.get(param));
    }

    /**
     * @param param A {@code QueryParam} that has been parsed by this instance.
     * @return The value of the query parameter. It is the caller's
     * responsibility to ensure that this value was present in the parsed {@code
     * HttpServiceRequest} or that the parameter has a default value.
     */
    public boolean getRequiredValue(final BooleanParameter param) {
        Objects.requireNonNull(param, "Expected parameter.");
        return boolValues.get(param);
    }

    /**
     * @param param A {@code QueryParam} that has been parsed by this instance.
     * @return The value of the query parameter. It is the caller's
     * responsibility to ensure that this value was present in the parsed {@code
     * HttpServiceRequest} or that the parameter has a default value.
     */
    public int getRequiredValue(final IntParameter param) {
        Objects.requireNonNull(param, "Expected parameter.");
        return intValues.get(param);
    }

    /**
     * @param param A {@code QueryParam} that has been parsed by this instance.
     * @return The value of the query parameter. It is the caller's
     * responsibility to ensure that this value was present in the parsed {@code
     * HttpServiceRequest} or that the parameter has a default value.
     */
    public String getRequiredValue(final StringParameter param) {
        Objects.requireNonNull(param, "Expected parameter.");
        return stringValues.get(param);
    }

    /**
     * @return A compound error describing all individual errors that occurred
     * during parsing.
     */
    public ParseError getCompoundError() {
        final List<String> errorMessages = new ArrayList<>();
        for (final ParseError error : errors) {
            errorMessages.add("<" + error.getMessage() + ">");
        }
        return new ParseError(String.join(", ", errorMessages));
    }

}