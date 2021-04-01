package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.alarms.AlarmManager;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessage;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarm;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarmDto;
import eu.arrowhead.core.plantdescriptionengine.utils.MockRequest;
import eu.arrowhead.core.plantdescriptionengine.utils.MockServiceResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpServiceRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class GetPdeAlarmTest {

    @Test
    public void shouldRetrieveAlarm() {

        final AlarmManager alarmManager = new AlarmManager();

        alarmManager.raiseSystemNotInDescription("System A", null);
        final PdeAlarmDto alarm = alarmManager.getAlarms().get(0);

        final HttpServiceRequest request = new MockRequest.Builder().pathParameters(List.of(String.valueOf(alarm.id())))
            .build();
        final MockServiceResponse response = new MockServiceResponse();
        final GetPdeAlarm handler = new GetPdeAlarm(alarmManager);

        try {
            handler.handle(request, response).ifSuccess(result -> {
                assertEquals(HttpStatus.OK, response.status().orElse(null));
                final PdeAlarm retrievedAlarm = (PdeAlarm) response.getRawBody();
                assertEquals(alarm.systemName().orElse(null), retrievedAlarm.systemName().orElse(null));
                assertEquals(alarm.severity(), retrievedAlarm.severity());
                assertEquals(alarm.acknowledged(), retrievedAlarm.acknowledged());
            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            fail();
        }
    }

    @Test
    public void shouldRejectInvalidId() {

        final String invalidEntryId = "Invalid ID";
        final HttpServiceRequest request = new MockRequest.Builder()
            .pathParameters(List.of(invalidEntryId))
            .build();
        final MockServiceResponse response = new MockServiceResponse();
        final GetPdeAlarm handler = new GetPdeAlarm(new AlarmManager());

        try {
            handler.handle(request, response).ifSuccess(result -> {
                assertEquals(HttpStatus.BAD_REQUEST, response.status().orElse(null));
                final String expectedErrorMessage = "'" + invalidEntryId + "' is not a valid PDE Alarm ID.";

                final String actualErrorMessage = ((ErrorMessage) response.getRawBody()).error();
                assertEquals(expectedErrorMessage, actualErrorMessage);
            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            fail();
        }
    }

    @Test
    public void shouldRejectNonexistentId() {

        final String nonexistentId = "31";
        final HttpServiceRequest request = new MockRequest.Builder()
            .pathParameters(List.of(nonexistentId))
            .build();
        final MockServiceResponse response = new MockServiceResponse();
        final GetPdeAlarm handler = new GetPdeAlarm(new AlarmManager());

        try {
            handler.handle(request, response).ifSuccess(result -> {
                assertEquals(HttpStatus.NOT_FOUND, response.status().orElse(null));
                final String expectedErrorMessage = "PDE Alarm with ID '" + nonexistentId + "' not found.";
                final String actualErrorMessage = ((ErrorMessage) response.getRawBody()).error();
                assertEquals(expectedErrorMessage, actualErrorMessage);
            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            fail();
        }
    }
}