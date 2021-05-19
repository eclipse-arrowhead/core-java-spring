package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitorable.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitorable.dto.SystemDataDto;
import se.arkalix.codec.CodecType;
import se.arkalix.codec.json.JsonObject;
import se.arkalix.codec.json.JsonPair;
import se.arkalix.codec.json.JsonString;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpRouteHandler;
import se.arkalix.net.http.service.HttpServiceRequest;
import se.arkalix.net.http.service.HttpServiceResponse;
import se.arkalix.util.concurrent.Future;

import java.util.Objects;

public class GetSystemData implements HttpRouteHandler {

    final String systemName;

    /**
     * Class constructor.
     *
     * @param systemName Name of the system providing this service.
     */
    public GetSystemData(final String systemName) {
        this.systemName = systemName;
    }

    @Override
    public Future<HttpServiceResponse> handle(
        HttpServiceRequest request,
        HttpServiceResponse response
    ) {
        Objects.requireNonNull(request, "Expected request.");
        Objects.requireNonNull(response, "Expected response.");

        JsonObject data = new JsonObject(new JsonPair("name", new JsonString(systemName)));
        response
            .status(HttpStatus.OK)
            .body(new SystemDataDto.Builder().data(data).build(), CodecType.JSON);
        return Future.done();
    }

}
