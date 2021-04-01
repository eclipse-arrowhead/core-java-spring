package eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore;

import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryDto;
import se.arkalix.dto.DtoReadException;
import se.arkalix.dto.DtoWriteException;
import se.arkalix.dto.binary.ByteArrayReader;
import se.arkalix.dto.binary.ByteArrayWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FilePdStore implements PdStore {

    // File path to the directory for storing JSON representations of plant
    // descriptions:
    private final String descriptionDirectory;

    /**
     * Class constructor.
     *
     * @param descriptionDirectory File path to the directory for storing Plant
     *                             Description.
     */
    public FilePdStore(final String descriptionDirectory) {
        Objects.requireNonNull(descriptionDirectory, "Expected path to Plant Description Entry directory");
        this.descriptionDirectory = descriptionDirectory;
    }

    /**
     * @return The file path to use for reading or writing a Plant Description
     * Entry to disk.
     */
    private Path getFilePath(final int entryId) {
        return Paths.get(descriptionDirectory, entryId + ".json");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PlantDescriptionEntryDto> readEntries() throws PdStoreException {
        final File directory = new File(descriptionDirectory);

        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new PdStoreException("Failed to create directory for storing Plant Descriptions.");
            }
        }

        final File[] directoryListing = directory.listFiles();

        // Read all Plant Description entries into memory.
        if (directoryListing == null) {
            throw new PdStoreException(new FileNotFoundException(
                "Failed to read Plant Descriptions from directory '" + descriptionDirectory + "'."));
        }

        final List<PlantDescriptionEntryDto> result = new ArrayList<>();
        for (final File child : directoryListing) {
            final byte[] bytes;
            try {
                bytes = Files.readAllBytes(child.toPath());
                result.add(PlantDescriptionEntryDto.readJson(new ByteArrayReader(bytes)));
            } catch (final DtoReadException | IOException e) {
                throw new PdStoreException(e);
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final PlantDescriptionEntryDto entry) throws PdStoreException {
        Objects.requireNonNull(entry, "Expected entry.");

        final Path path = getFilePath(entry.id());
        final File file;

        // Create the file and parent directories, if they do not already exist:
        try {
            Files.createDirectories(path.getParent());
            file = path.toFile();

            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new PdStoreException("Failed to create file for Plant Description Entries.");
                }
            }
        } catch (final IOException e) {
            throw new PdStoreException("Failed to create Plant Description Entry file.", e);
        }

        try (final FileOutputStream fileWriter = new FileOutputStream(file)) {
            final ByteArrayWriter byteArrayWriter = new ByteArrayWriter();
            entry.writeJson(byteArrayWriter);
            fileWriter.write(byteArrayWriter.asByteArray());
        } catch (final DtoWriteException | IOException e) {
            throw new PdStoreException("Failed to write rule to file", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final int entryId) throws PdStoreException {
        final Path filepath = getFilePath(entryId);
        try {
            Files.delete(filepath);
        } catch (final IOException e) {
            throw new PdStoreException("Failed to delete Plant Description Entry file", e);
        }
    }
}