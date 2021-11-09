package eu.arrowhead.core.plantdescriptionengine.utils;

import org.mockito.ArgumentMatcher;
import se.arkalix.net.http.client.HttpClientRequest;

import java.util.Objects;

/**
 * Argument matcher used to compare HttpClientRequest arguments. Note that this
 * matcher does not perform a complete comparison between requests: The body is
 * ignored.
 */
public class RequestMatcher implements ArgumentMatcher<HttpClientRequest> {

    private final HttpClientRequest a;

    /**
     * Constructor
     *
     * @param request Request used for comparisons.
     */
    public RequestMatcher(final HttpClientRequest request) {
        Objects.requireNonNull(request, "Expected request.");
        a = request;
    }

    /**
     * Return true if the given request matches the one that this instance was
     * initialized with.
     */
    @Override
    public boolean matches(final HttpClientRequest b) {
        Objects.requireNonNull(b, "Expected request.");

        if (!a.uri().equals(b.uri())) {
            return false;
        }

        return a.method().equals(b.method());
    }
}
