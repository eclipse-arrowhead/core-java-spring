package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.alarms.AlarmManager;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessage;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarm;
import eu.arrowhead.core.plantdescriptionengine.utils.MockRequest;
import eu.arrowhead.core.plantdescriptionengine.utils.MockServiceResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpServiceRequest;
import se.arkalix.net.http.service.HttpServiceResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GetPdeAlarmTest {

    @Test
    public void shouldRetrieveAlarm() {

        final var alarmManager = new AlarmManager();

        alarmManager.raiseSystemNotInDescription("System A", null);
        final var alarm = alarmManager.getAlarms().get(0);

        final HttpServiceRequest request = new MockRequest.Builder().pathParameters(List.of(String.valueOf(alarm.id())))
            .build();
        final HttpServiceResponse response = new MockServiceResponse();
        final var handler = new GetPdeAlarm(alarmManager);

        try {
            handler.handle(request, response).ifSuccess(result -> {
                assertEquals(HttpStatus.OK, response.status().orElse(null));
                assertTrue(response.body().isPresent());
                final var retrievedAlarm = (PdeAlarm) response.body().get();
                assertEquals(alarm.systemName().orElse(null), retrievedAlarm.systemName().orElse(null));
                assertEquals(alarm.severity(), retrievedAlarm.severity());
                assertEquals(alarm.acknowledged(), retrievedAlarm.acknowledged());
            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void shouldRejectInvalidId() {

        final String invalidEntryId = "Invalid ID";
        final HttpServiceRequest request = new MockRequest.Builder()
            .pathParameters(List.of(invalidEntryId))
            .build();
        final HttpServiceResponse response = new MockServiceResponse();
        final var handler = new GetPdeAlarm(new AlarmManager());

        try {
            handler.handle(request, response).ifSuccess(result -> {
                assertEquals(HttpStatus.BAD_REQUEST, response.status().orElse(null));
                String expectedErrorMessage = "'" + invalidEntryId + "' is not a valid PDE Alarm ID.";
                assertTrue(response.body().isPresent());
                String actualErrorMessage = ((ErrorMessage) response.body().get()).error();
                assertEquals(expectedErrorMessage, actualErrorMessage);
            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void shouldRejectNonexistentId() {

        final String nonexistentId = "31";
        final HttpServiceRequest request = new MockRequest.Builder()
            .pathParameters(List.of(nonexistentId))
            .build();
        final HttpServiceResponse response = new MockServiceResponse();
        final var handler = new GetPdeAlarm(new AlarmManager());

        try {
            handler.handle(request, response).ifSuccess(result -> {
                assertEquals(HttpStatus.NOT_FOUND, response.status().orElse(null));
                String expectedErrorMessage = "PDE Alarm with ID '" + nonexistentId + "' not found.";
                assertTrue(response.body().isPresent());
                String actualErrorMessage = ((ErrorMessage) response.body().get()).error();
                assertEquals(expectedErrorMessage, actualErrorMessage);
            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            assertNull(e);
        }
    }
}