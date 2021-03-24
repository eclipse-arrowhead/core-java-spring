package eu.arrowhead.core.plantdescriptionengine.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.dto.binary.BinaryWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public class DtoWriter implements BinaryWriter {

    private static final Logger logger = LoggerFactory.getLogger(DtoWriter.class);

    private final OutputStream writer;

    public DtoWriter(OutputStream writer) {
        Objects.requireNonNull(writer, "Expected OutputStream");
        this.writer = writer;
    }

    public int writeOffset() {
        throw new UnsupportedOperationException();
    }

    public void writeOffset(final int offset) {
        throw new UnsupportedOperationException();
    }

    public void write(final byte b) {
        try {
            writer.write(b);
        } catch (IOException e) {
            logger.error("DTO write failure", e);
        }
    }

    public void write(final byte[] bytes) {
        try {
            writer.write(bytes);
        } catch (IOException e) {
            logger.error("DTO write failure", e);
        }
    }

    @Override
    public int writableBytes() {
        throw new UnsupportedOperationException();
    }
}