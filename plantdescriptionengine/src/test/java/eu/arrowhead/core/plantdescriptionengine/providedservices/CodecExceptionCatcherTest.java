package eu.arrowhead.core.plantdescriptionengine.providedservices;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import org.junit.Test;
import org.mockito.Mockito;
import eu.arrowhead.core.plantdescriptionengine.providedservices.dto.ErrorMessage;
import eu.arrowhead.core.plantdescriptionengine.utils.MockRequest;
import eu.arrowhead.core.plantdescriptionengine.utils.MockServiceResponse;
import se.arkalix.codec.CodecException;
import se.arkalix.net.http.HttpStatus;

public class CodecExceptionCatcherTest {

    @Test
    public void shouldAllowValidNames() {
        final MockServiceResponse response = new MockServiceResponse();
        final MockRequest request = new MockRequest.Builder()
            .build();

        CodecExceptionCatcher catcher = new CodecExceptionCatcher();
        final String errorMessage = "Some exception";
        CodecException e = Mockito.mock(CodecException.class);
        when(e.getMessage()).thenReturn(errorMessage);

        catcher.handle(e, request, response);

        assertEquals(HttpStatus.BAD_REQUEST, response.status().orElse(null));
        final ErrorMessage resultingMessage = (ErrorMessage) response.getRawBody();
        assertEquals(errorMessage, resultingMessage.error());

    }

}
