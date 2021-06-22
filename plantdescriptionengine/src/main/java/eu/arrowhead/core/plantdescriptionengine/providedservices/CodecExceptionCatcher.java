package eu.arrowhead.core.plantdescriptionengine.providedservices;

import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessage;
import se.arkalix.codec.CodecException;
import se.arkalix.codec.CodecType;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpCatcherHandler;
import se.arkalix.net.http.service.HttpServiceRequest;
import se.arkalix.net.http.service.HttpServiceResponse;
import se.arkalix.util.concurrent.Future;

import java.util.Objects;

public class CodecExceptionCatcher implements HttpCatcherHandler<CodecException> {

    @Override
    public Future<?> handle(
        CodecException throwable,
        HttpServiceRequest request,
        HttpServiceResponse response
    ) {
        Objects.requireNonNull(throwable, "Expected throwable.");
        Objects.requireNonNull(request, "Expected request.");
        Objects.requireNonNull(response, "Expected response.");

        response.status(HttpStatus.BAD_REQUEST)
            .body(ErrorMessage.of(throwable.getMessage()), CodecType.JSON);
        return Future.done();
    }
}