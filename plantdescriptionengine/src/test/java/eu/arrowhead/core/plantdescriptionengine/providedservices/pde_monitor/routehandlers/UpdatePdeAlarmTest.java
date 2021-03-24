package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.alarms.AlarmManager;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessage;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarm;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarmUpdateBuilder;
import eu.arrowhead.core.plantdescriptionengine.utils.MockRequest;
import eu.arrowhead.core.plantdescriptionengine.utils.MockServiceResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpServiceRequest;
import se.arkalix.net.http.service.HttpServiceResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UpdatePdeAlarmTest {

    @Test
    public void shouldAcknowledgeAlarm() {

        final String systemNameA = "System A";

        final var alarmManager = new AlarmManager();

        alarmManager.raiseSystemNotInDescription(systemNameA, null);
        final var alarm = alarmManager.getAlarms().get(0);
        assertFalse(alarm.acknowledged());

        final HttpServiceRequest request = new MockRequest.Builder()
            .pathParameters(List.of(String.valueOf(alarm.id())))
            .body(new PdeAlarmUpdateBuilder()
                .acknowledged(true)
                .build())
            .build();
        final HttpServiceResponse response = new MockServiceResponse();
        final var handler = new UpdatePdeAlarm(alarmManager);

        try {
            handler.handle(request, response).ifSuccess(result -> {
                assertEquals(HttpStatus.OK, response.status().orElse(null));
                assertTrue(response.body().isPresent());
                final var updatedAlarm = (PdeAlarm) response.body().get();
                assertTrue(updatedAlarm.acknowledged());
                assertTrue(updatedAlarm.acknowledgedAt().isPresent());
            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void shouldRejectInvalidId() {

        final String invalidEntryId = "Invalid ID";
        final HttpServiceRequest request = new MockRequest.Builder().pathParameters(List.of(invalidEntryId))
            .body(new PdeAlarmUpdateBuilder()
                .acknowledged(true)
                .build())
            .build();
        final HttpServiceResponse response = new MockServiceResponse();
        final var handler = new UpdatePdeAlarm(new AlarmManager());

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
        final HttpServiceRequest request = new MockRequest.Builder().pathParameters(List.of(nonexistentId))
            .body(new PdeAlarmUpdateBuilder()
                .acknowledged(true)
                .build())
            .build();
        final HttpServiceResponse response = new MockServiceResponse();
        final var handler = new UpdatePdeAlarm(new AlarmManager());

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

    @Test
    public void shouldNotChangeAlarm() {

        final var alarmManager = new AlarmManager();

        alarmManager.raiseSystemNotInDescription("SystemA", null);
        final var alarm = alarmManager.getAlarms().get(0);
        assertFalse(alarm.acknowledged());

        final HttpServiceRequest request = new MockRequest.Builder()
            .pathParameters(List.of(String.valueOf(alarm.id())))
            .body(new PdeAlarmUpdateBuilder().build())
            .build();
        final HttpServiceResponse response = new MockServiceResponse();
        final var handler = new UpdatePdeAlarm(alarmManager);

        try {
            handler.handle(request, response).ifSuccess(result -> {
                assertEquals(HttpStatus.OK, response.status().orElse(null));
                assertTrue(response.body().isPresent());
                final var updatedAlarm = (PdeAlarm) response.body().get();
                assertFalse(updatedAlarm.acknowledged());
            }).onFailure(Assertions::assertNull);
        } catch (final Exception e) {
            assertNull(e);
        }
    }
}