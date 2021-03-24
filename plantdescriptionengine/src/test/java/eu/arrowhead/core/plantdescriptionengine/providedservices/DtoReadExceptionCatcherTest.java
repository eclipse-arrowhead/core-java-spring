package eu.arrowhead.core.plantdescriptionengine.providedservices;

import eu.arrowhead.core.plantdescriptionengine.utils.MockRequest;
import eu.arrowhead.core.plantdescriptionengine.utils.MockServiceResponse;
import org.junit.jupiter.api.Test;
import se.arkalix.dto.DtoEncoding;
import se.arkalix.dto.DtoReadException;
import se.arkalix.net.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DtoReadExceptionCatcherTest {

    @Test
    public void shouldReturnAllEntries() {

        final var catcher = new DtoReadExceptionCatcher();

        String message = "Lorem Ipsum";
        String value = "ABC";
        int offset = 0;

        DtoReadException exception = new DtoReadException(DtoEncoding.JSON, message, value, offset);
        var request = new MockRequest.Builder().body("Body").build();
        var response = new MockServiceResponse();

        catcher.handle(exception, request, response);
        assertEquals(HttpStatus.BAD_REQUEST, response.status().orElse(null));
        assertTrue(response.body().isPresent());
        String body = response.body().get().toString();

        assertEquals("ErrorMessage{error='Failed to read JSON; cause: Lorem Ipsum `ABC` at offset 0'}", body);
    }
}
