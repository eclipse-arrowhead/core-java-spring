package eu.arrowhead.core.plantdescriptionengine.utils;

import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoWritable;
import se.arkalix.net.http.HttpHeaders;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.HttpVersion;
import se.arkalix.net.http.service.HttpServiceResponse;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Mock HttpServiceResponse implementation used for testing.
 */
public class MockServiceResponse implements HttpServiceResponse {

    private Object _body = null;
    private HttpStatus _status = HttpStatus.IM_A_TEAPOT;

    @Override
    public Optional<Object> body() {
        return Optional.of(_body);
    }

    @Override
    public HttpServiceResponse body(byte[] byteArray) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServiceResponse body(DtoEncoding encoding, DtoWritable data) {
        _body = data;
        return this;
    }

    @Override
    public HttpServiceResponse body(DtoEncoding encoding, List<DtoWritable> data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServiceResponse body(Path path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServiceResponse body(String string) {
        _body = string;
        return this;
    }

    @Override
    public HttpServiceResponse clearBody() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServiceResponse body(DtoWritable data) {
        _body = data;
        return this;
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
    public HttpServiceResponse status(HttpStatus status) {
        _status = status;
        return this;
    }

    @Override
    public HttpVersion version() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServiceResponse body(List<DtoWritable> arg0) {
        return null;
    }

}