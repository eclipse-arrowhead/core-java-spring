package eu.arrowhead.core.plantdescriptionengine.utils;

import se.arkalix.codec.Decoder;
import se.arkalix.net.BodyIncoming;
import se.arkalix.net.http.HttpHeaders;
import se.arkalix.net.http.HttpMethod;
import se.arkalix.net.http.HttpVersion;
import se.arkalix.net.http.service.HttpServiceConnection;
import se.arkalix.net.http.service.HttpServiceRequest;
import se.arkalix.util.concurrent.Future;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Mock HttpServiceRequest implementation used for testing.
 */
public class MockRequest implements HttpServiceRequest {

    private final List<String> _pathParameters;
    private final Object body;
    private final Map<String, List<String>> _queryParameters;

    public MockRequest() {
        _pathParameters = new ArrayList<>();
        body = null;
        _queryParameters = new HashMap<>();
    }

    public MockRequest(final Builder builder) {
        Objects.requireNonNull(builder, "Expected builder.");
        _pathParameters = builder.pathParameters;
        body = builder.body;
        _queryParameters = builder.queryParameters;
    }

    @Override
    public <T> Future<T> bodyTo(final Decoder<T> decoder) {
        Objects.requireNonNull(decoder, "Expected decoder.");
        @SuppressWarnings("unchecked") final T castBody = (T) body;
        return Future.success(castBody);
    }

    @Override
    public HttpHeaders headers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpMethod method() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String path() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> pathParameters() {
        return _pathParameters;
    }

    @Override
    public Map<String, List<String>> queryParameters() {
        return _queryParameters;
    }

    @Override
    public HttpVersion version() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServiceRequest clearHeaders() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServiceConnection connection() {
        throw new UnsupportedOperationException();
    }

    public static class Builder {
        private List<String> pathParameters = new ArrayList<>();
        private Object body;
        private Map<String, List<String>> queryParameters;

        public Builder pathParameters(final List<String> pathParameters) {
            Objects.requireNonNull(pathParameters, "Expected path parameters.");
            this.pathParameters = pathParameters;
            return this;
        }

        public Builder body(final Object body) {
            Objects.requireNonNull(body, "Expected body.");
            this.body = body;
            return this;
        }

        public Builder queryParameters(final Map<String, List<String>> queryParameters) {
            Objects.requireNonNull(queryParameters, "Expected query parameters.");
            this.queryParameters = queryParameters;
            return this;
        }

        public MockRequest build() {
            return new MockRequest(this);
        }
    }

    @Override
    public BodyIncoming body() {
        throw new UnsupportedOperationException();
    }

}