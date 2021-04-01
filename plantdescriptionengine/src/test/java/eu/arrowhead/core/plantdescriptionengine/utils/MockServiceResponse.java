package eu.arrowhead.core.plantdescriptionengine.utils;

import se.arkalix.codec.MediaType;
import se.arkalix.codec.MultiEncodable;
import se.arkalix.codec.ToCodecType;
import se.arkalix.net.BodyOutgoing;
import se.arkalix.net.http.HttpHeaders;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.HttpVersion;
import se.arkalix.net.http.service.HttpServiceResponse;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * Mock HttpServiceResponse implementation used for testing.
 */
public class MockServiceResponse implements HttpServiceResponse {

    private Object _body;
    private HttpStatus _status = HttpStatus.IM_A_TEAPOT;

    @Override
    public Optional<BodyOutgoing> body() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServiceResponse body(final byte[] byteArray) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MockServiceResponse body(final MultiEncodable encodable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServiceResponse body(final Path path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MockServiceResponse body(final String string) {
        _body = string;
        return this;
    }

    @Override
    public MockServiceResponse body(final MultiEncodable encodable, final ToCodecType toCodecType) {
        _body = encodable;
        return this;
    }


    @Override
    public HttpServiceResponse clearBody() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServiceResponse clearHeaders() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpHeaders headers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<HttpStatus> status() {
        return Optional.of(_status);
    }

    @Override
    public MockServiceResponse status(final HttpStatus status) {
        Objects.requireNonNull(status, "Expected status.");
        _status = status;
        return this;
    }

    @Override
    public HttpServiceResponse contentType(MediaType arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<MediaType> contentType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServiceResponse body(BodyOutgoing body) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServiceResponse header(CharSequence arg0, CharSequence arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<HttpVersion> version() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServiceResponse version(HttpVersion arg0) {
        throw new UnsupportedOperationException();
    }

    public Object getRawBody() {
        return _body;
    }
}