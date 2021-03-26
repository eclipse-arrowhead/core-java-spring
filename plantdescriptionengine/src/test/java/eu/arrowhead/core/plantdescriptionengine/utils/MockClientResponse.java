package eu.arrowhead.core.plantdescriptionengine.utils;

import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoReadable;
import se.arkalix.net.http.HttpHeaders;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.HttpVersion;
import se.arkalix.net.http.client.HttpClientRequest;
import se.arkalix.net.http.client.HttpClientResponse;
import se.arkalix.util.concurrent.FutureProgress;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Mock HttpClientResponse implementation used for testing.
 */
public class MockClientResponse implements HttpClientResponse {

    private Object _body;
    private HttpStatus _status = HttpStatus.IM_A_TEAPOT;

    @Override
    public <R extends DtoReadable> FutureProgress<R> bodyAs(final DtoEncoding encoding, final Class<R> class_) {
        Objects.requireNonNull(encoding, "Expected encoding.");
        Objects.requireNonNull(class_, "Expected class.");
        @SuppressWarnings("unchecked") final R castBody = (R) _body;
        return new MockFutureProgress<>(castBody);
    }

    @Override
    public FutureProgress<byte[]> bodyAsByteArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <R extends DtoReadable> FutureProgress<List<R>> bodyAsList(
        final DtoEncoding encoding,
        final Class<R> class_
    ) {
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
    public HttpHeaders headers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpClientRequest request() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpStatus status() {
        return _status;
    }

    @Override
    public HttpVersion version() {
        throw new UnsupportedOperationException();
    }

    public MockClientResponse status(final HttpStatus status) {
        Objects.requireNonNull(status, "Expected status.");
        _status = status;
        return this;
    }

    public MockClientResponse body(final Object data) {
        Objects.requireNonNull(data, "Expected data.");
        _body = data;
        return this;
    }
}