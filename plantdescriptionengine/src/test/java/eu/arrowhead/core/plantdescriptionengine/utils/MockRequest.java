package eu.arrowhead.core.plantdescriptionengine.utils;

import se.arkalix.description.ConsumerDescription;
import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoReadable;
import se.arkalix.net.http.HttpHeaders;
import se.arkalix.net.http.HttpMethod;
import se.arkalix.net.http.HttpVersion;
import se.arkalix.net.http.service.HttpServiceRequest;
import se.arkalix.util.concurrent.FutureProgress;

import java.io.InputStream;
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
    public <R extends DtoReadable> FutureProgress<R> bodyAs(final DtoEncoding encoding, final Class<R> class_) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FutureProgress<byte[]> bodyAsByteArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <R extends DtoReadable> FutureProgress<List<R>> bodyAsList(final DtoEncoding encoding, final Class<R> class_) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FutureProgress<? extends InputStream> bodyAsStream() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FutureProgress<String> bodyAsString() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FutureProgress<Path> bodyTo(final Path path, final boolean append) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <R extends DtoReadable> FutureProgress<R> bodyAs(final Class<R> class_) {
        @SuppressWarnings("unchecked") final R castBody = (R) body;
        Objects.requireNonNull(class_, "Expected class.");
        return new MockFutureProgress<>(castBody);
    }

    @Override
    public <R extends DtoReadable> FutureProgress<List<R>> bodyAsList(final Class<R> class_) {
        throw new UnsupportedOperationException();
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
    public ConsumerDescription consumer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpVersion version() {
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

}