package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.routehandlers;

import eu.arrowhead.core.plantdescriptionengine.alarms.AlarmManager;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessage;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarm;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarmUpdateDto;
import eu.arrowhead.core.plantdescriptionengine.utils.MockRequest;
import eu.arrowhead.core.plantdescriptionengine.utils.MockServiceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.arkalix.net.http.HttpStatus;
import se.arkalix.net.http.service.HttpServiceRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class UpdatePdeAlarmTest {

    private AlarmManager alarmManager;
    private UpdatePdeAlarm handler;
    private MockServiceResponse response;

    @BeforeEach
    public void initEach() {
        alarmManager = new AlarmManager();
        handler = new UpdatePdeAlarm(alarmManager);
        response = new MockServiceResponse();
    }

    @Test
    public void shouldAcknowledgeAlarm() {

        final String systemNameA = "abc";

        alarmManager.raiseSystemNotInDescription(systemNameA, null);
        final PdeAlarm alarm = alarmManager.getAlarms().get(0);
        assertFalse(alarm.acknowledged());

        final HttpServiceRequest request = new MockRequest.Builder()
            .pathParameters(List.of(String.valueOf(alarm.id())))
            .body(new PdeAlarmUpdateDto.Builder()
                .acknowledged(true)
                .build())
            .build();

        handler.handle(request, response).ifSuccess(result -> {
            final PdeAlarm updatedAlarm = (PdeAlarm) response.getRawBody();
            assertEquals(HttpStatus.OK, response.status().orElse(null));
            assertTrue(updatedAlarm.acknowledged());
            assertTrue(updatedAlarm.acknowledgedAt().isPresent());
        }).onFailure(e -> fail());
    }

    @Test
    public void shouldNotUnacknowledgeAlarm() {

        final String systemNameA = "abc";

        alarmManager.raiseSystemNotInDescription(systemNameA, null);
        final PdeAlarm alarm = alarmManager.getAlarms().get(0);
        alarmManager.acknowledge(alarm.id());

        final HttpServiceRequest request = new MockRequest.Builder()
            .pathParameters(List.of(String.valueOf(alarm.id())))
            .body(new PdeAlarmUpdateDto.Builder()
                .acknowledged(false)
                .build())
            .build();

        handler.handle(request, response).ifSuccess(result -> {
            final ErrorMessage errorMessage = (ErrorMessage) response.getRawBody();
            String expectedErrorMessage = "Cannot unacknowledge an acknowledged alarm.";
            assertEquals(HttpStatus.BAD_REQUEST, response.status().orElse(null));
            assertEquals(expectedErrorMessage, errorMessage.error());
        }).onFailure(e -> fail());

    }

    @Test
    public void shouldRejectInvalidId() {

        final String invalidEntryId = "Invalid ID";
        final HttpServiceRequest request = new MockRequest.Builder().pathParameters(List.of(invalidEntryId))
            .body(new PdeAlarmUpdateDto.Builder()
                .acknowledged(true)
                .build())
            .build();

        handler.handle(request, response).ifSuccess(result -> {
            final String expectedErrorMessage = "'" + invalidEntryId + "' is not a valid PDE Alarm ID.";
            final String actualErrorMessage = ((ErrorMessage) response.getRawBody()).error();
            assertEquals(HttpStatus.BAD_REQUEST, response.status().orElse(null));
            assertEquals(expectedErrorMessage, actualErrorMessage);
        })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldRejectNonexistentId() {

        final String nonexistentId = "31";
        final HttpServiceRequest request = new MockRequest.Builder().pathParameters(List.of(nonexistentId))
            .body(new PdeAlarmUpdateDto.Builder()
                .acknowledged(true)
                .build())
            .build();

        handler.handle(request, response).ifSuccess(result -> {
            final String expectedErrorMessage = "PDE Alarm with ID '" + nonexistentId + "' not found.";
            final String actualErrorMessage = ((ErrorMessage) response.getRawBody()).error();
            assertEquals(HttpStatus.NOT_FOUND, response.status().orElse(null));
            assertEquals(expectedErrorMessage, actualErrorMessage);
        })
            .onFailure(e -> fail());

    }

    @Test
    public void shouldNotChangeAlarm() {

        alarmManager.raiseSystemNotInDescription("SystemA", null);
        final PdeAlarm alarm = alarmManager.getAlarms().get(0);
        assertFalse(alarm.acknowledged());

        final HttpServiceRequest request = new MockRequest.Builder()
            .pathParameters(List.of(String.valueOf(alarm.id())))
            .body(new PdeAlarmUpdateDto.Builder().build())
            .build();

        handler.handle(request, response).ifSuccess(result -> {
            final PdeAlarm updatedAlarm = (PdeAlarm) response.getRawBody();
            assertEquals(HttpStatus.OK, response.status().orElse(null));
            assertFalse(updatedAlarm.acknowledged());
        })
            .onFailure(e -> fail());

    }
}