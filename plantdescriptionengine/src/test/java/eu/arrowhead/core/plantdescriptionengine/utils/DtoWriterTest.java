package eu.arrowhead.core.plantdescriptionengine.utils;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

public class DtoWriterTest {

    @Test
    public void shouldNotSupportWriteOffset() {
        final var writer = new DtoWriter(new ByteArrayOutputStream());
        assertThrows(UnsupportedOperationException.class,
            writer::writeOffset);
    }

    @Test
    public void shouldNotSupportWriteOffsetInt() {
        final var writer = new DtoWriter(new ByteArrayOutputStream());
        assertThrows(UnsupportedOperationException.class,
            () -> writer.writeOffset(1));
    }

    @Test
    public void shouldNotSupportWritableBytes() {
        final var writer = new DtoWriter(new ByteArrayOutputStream());
        assertThrows(UnsupportedOperationException.class,
            writer::writableBytes);
    }

    @Test
    public void shouldHandleWriteByteError() throws IOException {
        final OutputStream outputStream = Mockito.mock(OutputStream.class);
        byte b = 0;

        doThrow(new IOException("Mocked error")).when(outputStream).write(b);
        final var writer = new DtoWriter(outputStream);
        writer.write(b);
        // No exception is thrown.
    }

    @Test
    public void shouldHandleWriteBytesError() throws IOException {
        final OutputStream outputStream = Mockito.mock(OutputStream.class);
        doThrow(new IOException("Mocked error")).when(outputStream).write(any());
        final var writer = new DtoWriter(outputStream);
        final byte[] bytes = {0, 0, 0};
        writer.write(bytes);
        // No exception is thrown.
    }

}
