package eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.rulebackingstore;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileRuleStoreTest {

    private final String entryDirectory = "test-temp-data";

    private void deleteDirectory(File dir) {
        File[] allContents = dir.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
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
        final var store = new FileRuleStore(entryDirectory);
        final Set<Integer> rules = Set.of(1, 2, 3);

        store.setRules(rules);

        var storedRules = store.readRules();
        assertEquals(rules, storedRules);
    }

    @Test
    public void shouldRemoveRules() throws RuleStoreException {
        final var store = new FileRuleStore(entryDirectory);
        final Set<Integer> rules = Set.of(1, 2, 3);

        store.setRules(rules);
        store.removeAll();

        var storedRules = store.readRules();
        assertTrue(storedRules.isEmpty());
    }

}
