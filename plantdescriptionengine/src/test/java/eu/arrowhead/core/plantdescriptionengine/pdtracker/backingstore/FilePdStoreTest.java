package eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore;

import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryDto;
import eu.arrowhead.core.plantdescriptionengine.utils.TestUtils;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for the {@link eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.FilePdStore}
 * class.
 */
public class FilePdStoreTest {

    private final String entryDirectory = "test-temp-data";
    private final int maxPdBytes = 200000;

    private void deleteDirectory(final File dir) {
        final File[] allContents = dir.listFiles();
        if (allContents != null) {
            for (final File file : allContents) {
                deleteDirectory(file);
            }
        }
        if (!dir.delete()) {
            throw new RuntimeException("Failed to delete directory.");
        }
    }

    @After
    public void removeTestDirectory() {
        deleteDirectory(new File(entryDirectory));
    }

    @Test
    public void ShouldReadWithoutEntries() throws PdStoreException {
        final PdStore store = new FilePdStore(entryDirectory, maxPdBytes);
        final List<PlantDescriptionEntryDto> storedEntries = store.readEntries();
        assertTrue(storedEntries.isEmpty());
    }

    @Test
    public void ShouldWriteEntries() throws PdStoreException {
        final PdStore store = new FilePdStore(entryDirectory, maxPdBytes);
        final List<Integer> entryIds = List.of(1, 2, 3);

        for (final int id : entryIds) {
            store.write(TestUtils.createEntry(id));
        }

        final List<PlantDescriptionEntryDto> storedEntries = store.readEntries();
        assertEquals(storedEntries.size(), entryIds.size());

        for (final PlantDescriptionEntryDto entry : storedEntries) {
            assertTrue(entryIds.contains(entry.id()));
        }
    }

    @Test
    public void ShouldRemoveEntries() throws PdStoreException {
        final PdStore store = new FilePdStore(entryDirectory, maxPdBytes);
        final List<Integer> entryIds = List.of(1, 2, 3);

        for (final int id : entryIds) {
            store.write(TestUtils.createEntry(id));
        }

        final int id0 = entryIds.get(0);
        store.remove(id0);

        final List<PlantDescriptionEntryDto> storedEntries = store.readEntries();
        assertEquals(storedEntries.size(), entryIds.size() - 1);

        for (final PlantDescriptionEntryDto entry : storedEntries) {
            assertTrue(entryIds.contains(entry.id()));
            assertNotEquals(entry.id(), id0);
        }
    }
}