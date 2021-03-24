package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.alarms.AlarmManager;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessage;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarmDto;
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
    public GetPdeAlarm(AlarmManager alarmManager) {
        Objects.requireNonNull(alarmManager, "Expected Alarm Manager.");
        this.alarmManager = alarmManager;
    }

    /**
     * Handles an HTTP call to acquire the PDE Alarm specified by the id path
     * parameter.
     *
     * @param request  HTTP request object.
     * @param response HTTP response containing the current
     *                 PlantDescriptionEntryList.
     */
    @Override
    public Future<HttpServiceResponse> handle(final HttpServiceRequest request, final HttpServiceResponse response) {

        String idString = request.pathParameter(0);
        int id;

        try {
            id = Integer.parseInt(idString);
        } catch (NumberFormatException e) {
            response.status(HttpStatus.BAD_REQUEST);
            response.body(ErrorMessage.of("'" + idString + "' is not a valid PDE Alarm ID."));
            return Future.success(response);
        }

        final PdeAlarmDto alarm = alarmManager.getAlarmDto(id);

        if (alarm == null) {
            response.status(HttpStatus.NOT_FOUND);
            response.body(ErrorMessage.of("PDE Alarm with ID '" + id + "' not found."));
            return Future.success(response);
        }

        response.status(HttpStatus.OK).body(alarm);
        return Future.success(response);
    }
}