package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitorable.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitorable.dto.InventoryIdDto;
import se.arkalix.codec.CodecType;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpRouteHandler;
import se.arkalix.net.http.service.HttpServiceRequest;
import se.arkalix.net.http.service.HttpServiceResponse;
import se.arkalix.util.concurrent.Future;

import java.util.Objects;

public class GetInventoryId implements HttpRouteHandler {

    @Override
    public Future<HttpServiceResponse> handle(
        HttpServiceRequest request,
        HttpServiceResponse response
    ) {
        Objects.requireNonNull(request, "Expected request.");
        Objects.requireNonNull(response, "Expected response.");

        response
            .status(HttpStatus.OK)
            .body(new InventoryIdDto.Builder().build(), CodecType.JSON);
        return Future.done();
    }

}
