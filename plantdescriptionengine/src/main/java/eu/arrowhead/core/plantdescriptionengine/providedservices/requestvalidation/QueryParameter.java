package eu.arrowhead.core.plantdescriptionengine.providedservices.requestvalidation;

import se.arkalix.net.http.service.HttpServiceRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An instance of this class embodies a set of requirements placed on a single
 * HttpServiceRequest query parameter. Used in conjunction with QueryParamParser
 * for validating and parsing query parameters.
 */
public abstract class QueryParameter {

    // Strings used as keys/values in query parameters.
    public static final String ACTIVE = "active";
    public static final String ITEM_PER_PAGE = "item_per_page";
    public static final String PAGE = "page";
    public static final String SYSTEM_NAME = "systemName";
    public static final String SORT_FIELD = "sort_field";
    public static final String ID = "id";
    public static final String RAISED_AT = "raisedAt";
    public static final String CREATED_AT = "createdAt";
    public static final String UPDATED_AT = "updatedAt";
    public static final String CLEARED_AT = "clearedAt";
    public static final String DIRECTION = "direction";
    public static final String ASC = "ASC";
    public static final String DESC = "DESC";
    public static final String SEVERITY = "severity";
    public static final String ACKNOWLEDGED = "acknowledged";

    protected final String name;
    protected final List<QueryParameter> requiredParameters = new ArrayList<>();

    /**
     * Class constructor.
     *
     * @param builder Builder instance used to configure the instance.
     */
    protected QueryParameter(final Builder<?> builder) {
        Objects.requireNonNull(builder, "Expected builder.");
        Objects.requireNonNull(builder.name, "No name has been set on builder instance.");

        name = builder.name;
        requiredParameters.addAll(builder.requiredParameters);
    }

    /**
     * Validate and parse the query parameter that this instance corresponds to.
     * If the parameter is present in the request, and it does not violate any
     * of the requirements imposed by this instance, it is stored in the given
     * parser object. Any requirement violations are reported using the parser's
     * {@code report} method.
     *
     * @param request  A HTTP service request.
     * @param parser   A query parameter parser instance.
     * @param required If true, this method will report an error if the
     *                 parameter is not present in the request.
     */
    public abstract void parse(HttpServiceRequest request, QueryParamParser parser, boolean required);

    /**
     * Helper class used for constructing Query Parameters.
     */
    protected abstract static class Builder<T extends Builder<T>> {
        protected final List<QueryParameter> requiredParameters = new ArrayList<>();
        protected String name;

        public abstract T self();

        /**
         * @param name A name to use for the constructed query parameter.
         * @return This instance.
         */
        public T name(final String name) {
            Objects.requireNonNull(name, "Expected name.");
            this.name = name;
            return self();
        }

        /**
         * @param param A query parameter that must be present if the one being
         *              constructed is.
         * @return This instance.
         */
        public T requires(final QueryParameter param) {
            Objects.requireNonNull(param, "Expected param.");
            requiredParameters.add(param);
            return self();
        }
    }
}