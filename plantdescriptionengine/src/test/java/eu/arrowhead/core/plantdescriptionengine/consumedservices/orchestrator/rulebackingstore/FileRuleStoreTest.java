package eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.rulebackingstore;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileRuleStoreTest {

    private final String entryDirectory = "test-temp-data";
    private final int plantDescriptionId = 96;

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

    @AfterEach
    public void removeTestDirectory() {
        deleteDirectory(new File(entryDirectory));
    }

    @Test
    public void shouldWriteRules() throws RuleStoreException {
        final FileRuleStore store = new FileRuleStore(entryDirectory);
        final Set<Integer> rules = Set.of(1, 2, 3);

        store.setRules(plantDescriptionId, rules);

        final Set<Integer> storedRules = store.readRules(plantDescriptionId);
        assertEquals(rules, storedRules);
    }

    @Test
    public void shouldRemoveRules() throws RuleStoreException {
        final FileRuleStore store = new FileRuleStore(entryDirectory);
        final Set<Integer> rules = Set.of(1, 2, 3);

        store.setRules(plantDescriptionId, rules);
        store.removeRules(plantDescriptionId);

        final Set<Integer> storedRules = store.readRules(plantDescriptionId);
        assertTrue(storedRules.isEmpty());
    }

}
