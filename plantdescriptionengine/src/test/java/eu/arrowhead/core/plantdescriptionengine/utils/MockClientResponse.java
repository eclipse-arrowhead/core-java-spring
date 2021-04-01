package eu.arrowhead.core.plantdescriptionengine.utils;

import se.arkalix.codec.Decoder;
import se.arkalix.net.BodyIncoming;
import se.arkalix.net.http.HttpHeaders;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.HttpVersion;
import se.arkalix.net.http.client.HttpClientConnection;
import se.arkalix.net.http.client.HttpClientRequest;
import se.arkalix.net.http.client.HttpClientResponse;
import se.arkalix.util.concurrent.Future;

import java.util.Objects;

/**
 * Mock HttpClientResponse implementation used for testing.
 */
public class MockClientResponse implements HttpClientResponse {

    private Object _body;
    private HttpStatus _status = HttpStatus.IM_A_TEAPOT;

    @Override
    public <T> Future<T> bodyTo(final Decoder<T> decoder) {
        Objects.requireNonNull(decoder, "Expected decoder.");
        @SuppressWarnings("unchecked") final T castBody = (T) _body;
        return Future.success(castBody);
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

    @Override
    public BodyIncoming body() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpClientResponse clearHeaders() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpClientConnection connection() {
        throw new UnsupportedOperationException();
    }
}