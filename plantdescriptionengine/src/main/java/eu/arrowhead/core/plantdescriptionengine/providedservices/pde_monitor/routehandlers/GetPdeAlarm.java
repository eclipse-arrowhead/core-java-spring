package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.alarms.AlarmManager;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessage;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarmDto;
import se.arkalix.codec.CodecType;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpRouteHandler;
import se.arkalix.net.http.service.HttpServiceRequest;
import se.arkalix.net.http.service.HttpServiceResponse;
import se.arkalix.util.concurrent.Future;

import java.util.Objects;

/**
 * Handles HTTP requests to retrieve a specific PDE Alarm.
 */
public class GetPdeAlarm implements HttpRouteHandler {

    private final AlarmManager alarmManager;

    /**
     * Constructor.
     *
     * @param alarmManager Object used for managing PDE alarms.
     */
    public GetPdeAlarm(final AlarmManager alarmManager) {
        Objects.requireNonNull(alarmManager, "Expected Alarm Manager.");
        this.alarmManager = alarmManager;
    }

    /**
     * Handles an HTTP call to acquire the PDE Alarm specified by the id path
     * parameter.
     *
     * @param request  HTTP request object.
     * @param response HTTP response containing the current {@code
     *                 PlantDescriptionEntryList}.
     */
    @Override
    public Future<HttpServiceResponse> handle(final HttpServiceRequest request, final HttpServiceResponse response) {
        Objects.requireNonNull(request, "Expected request.");
        Objects.requireNonNull(response, "Expected response.");

        final String idString = request.pathParameter(0);
        final int id;

        try {
            id = Integer.parseInt(idString);
        } catch (final NumberFormatException e) {
            response
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorMessage.of("'" + idString + "' is not a valid PDE Alarm ID."), CodecType.JSON);
            return Future.success(response);
        }

        final PdeAlarmDto alarm = alarmManager.getAlarmDto(id);

        if (alarm == null) {
            response
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorMessage.of("PDE Alarm with ID '" + id + "' not found."), CodecType.JSON);
        } else {
            response
                .status(HttpStatus.OK)
                .body(alarm, CodecType.JSON);
        }

        return Future.success(response);
    }
}