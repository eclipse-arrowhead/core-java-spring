package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.alarms.AlarmManager;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessage;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessageDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarm;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarmUpdateDto;
import se.arkalix.codec.CodecType;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpRouteHandler;
import se.arkalix.net.http.service.HttpServiceRequest;
import se.arkalix.net.http.service.HttpServiceResponse;
import se.arkalix.util.concurrent.Future;

import java.util.Objects;

/**
 * Handles HTTP requests to update PDE Alarms.
 */
public class UpdatePdeAlarm implements HttpRouteHandler {

    private final AlarmManager alarmManager;

    /**
     * Constructor.
     *
     * @param alarmManager Object used for managing PDE alarms.
     */
    public UpdatePdeAlarm(final AlarmManager alarmManager) {
        Objects.requireNonNull(alarmManager, "Expected Alarm Manager.");
        this.alarmManager = alarmManager;
    }

    /**
     * Handles an HTTP request to update the Alarm specified by the id parameter
     * with the information in the request body.
     *
     * @param request  HTTP request containing a PlantDescriptionUpdate.
     * @param response HTTP response containing the updated entry.
     */
    @Override
    public Future<HttpServiceResponse> handle(final HttpServiceRequest request, final HttpServiceResponse response) {

        Objects.requireNonNull(request, "Expected request.");
        Objects.requireNonNull(response, "Expected response.");

        return request.bodyTo(PdeAlarmUpdateDto::decodeJson)
            .map(newFields -> {
                final String idString = request.pathParameter(0);
                final int id;

                try {
                    id = Integer.parseInt(idString);
                } catch (final NumberFormatException e) {

                    ErrorMessageDto errorMessage = ErrorMessage.of("'" + idString + "' is not a valid PDE Alarm ID.");
                    response
                        .status(HttpStatus.BAD_REQUEST)
                        .body(errorMessage, CodecType.JSON);

                    return response.status(HttpStatus.BAD_REQUEST);
                }

                final PdeAlarm alarm = alarmManager.getAlarmDto(id);
                if (alarm == null) {
                    ErrorMessageDto errorMessage = ErrorMessage.of("PDE Alarm with ID '" + idString + "' not found.");
                    return response
                        .status(HttpStatus.NOT_FOUND)
                        .body(errorMessage, CodecType.JSON);
                }

                if (newFields.acknowledged().isPresent()) {
                    boolean alreadyAcknowledged = alarm.acknowledged();
                    boolean newAcknowledgedValue = newFields.acknowledged().get();
                    if (!alreadyAcknowledged && newAcknowledgedValue) {
                        alarmManager.acknowledge(id);
                    } else if (alreadyAcknowledged && !newAcknowledgedValue) {
                        ErrorMessageDto errorMessage = ErrorMessage.of(
                            "Cannot unacknowledge an acknowledged alarm."
                        );
                        return response
                            .status(HttpStatus.BAD_REQUEST)
                            .body(errorMessage, CodecType.JSON);
                    }
                }

                return response
                    .status(HttpStatus.OK)
                    .body(alarmManager.getAlarmDto(id), CodecType.JSON);

            });
    }
}